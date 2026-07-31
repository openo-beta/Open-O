#!/usr/bin/env python3
"""Local DHDR stub endpoint for runtime validation of the OpenO DHDR viewer.

Stands in for the Ontario Health DHDR FHIR service so the EMR can be exercised end to end on a
developer machine. Serves the DHDR IG (ca.on.dhdr.r4 4.0.3-snapshot9) examples, routes to a
scenario by the patient HCN in patient.identifier, and records every inbound request so the
query the EMR *sent* can be asserted on afterwards.

This is a test double. It validates our code against IG-conformant example data; it says nothing
about what Ontario Health actually returns. See DHDR_RUNTIME_HARNESS.md.
"""

import json
import os
import re
import sys
from datetime import datetime, timedelta
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

HERE = os.path.dirname(os.path.abspath(__file__))
EXAMPLES = os.path.abspath(os.path.join(
    HERE, "..", "igcheck", "x-snapshot9", "package", "examples"))
REQUEST_LOG = os.path.join(HERE, "requests.jsonl")
PORT = int(os.environ.get("DHDR_STUB_PORT", "8099"))

# HCN -> scenario. Mirrors how OMD's ValScenarios key behaviour to specific test patients
# (patientPB2, PK2, ...), so a scenario is selected by searching that patient in the EMR.
HCN_SCENARIOS = {
    "9876543225": "paged",       # FAKE-Jones,  demographic 1  -> 2-page walk (UC02/DHDR02.01)
    "1111111165": "single",      # FAKE-GEORGE, demographic 3  -> one page, no next link
    # To reach the crafted MedicationDispense+MedicationAdministration mix, temporarily map an HCN
    # here to "medadmin" (proved #40: MedicationAdministration is silently dropped).
    "6786837356": "covax_down",  # FAKE-Oscar,  demographic 21 -> UC11 COVaxON OperationOutcome
    "5414494118": "consent",     # FAKE-Flor,   demographic 22 -> UC03 consent block
    "3030248733": "empty",       # FAKE-Abram,  demographic 23 -> zero results (DHDR02.04)
    "6261030772": "https_systems",  # FAKE-Lorrie, demographic 24 -> crafted, see below
    "2783935017": "grouping",    # FAKE-Earline, demographic 25 -> crafted, UC06 grouping (steps 29/30)
    # To reach the crafted out-of-order group fixture, temporarily map an HCN here to
    # "unsorted_group" (proved the DHDR04.02/07.02 head-and-order defect; see the ledger).
    "9158502921": "group_order",  # FAKE-Leannon, demographic 28 -> crafted, DHDR04.02/07.02 BETWEEN-group order
    "9427586558": "nurse_reason",  # FAKE-Ozzie, demographic 26 -> crafted, nurse prescriber + 2-element reasonCode
    # FAKE-Neomi, demographic 27 -> crafted, post-unblock data + CONSENT_TEMP_UNBLOCK outcome
    "9719988648": "consent_unblocked",
    "3675825061": "no_pharmacist_licence",  # FAKE-Kandice, demographic 33 -> crafted, see below
    # Mapped permanently 2026-07-30. All four were reachable only by temporarily repointing another
    # patient's HCN, which the comments above kept telling the next person to do -- and a baseline set
    # that has to be produced by editing the stub is not a baseline. Demographics 34-37 were unused.
    "1514824580": "bad_entries",       # FAKE-Sang, demographic 34 -> per-entry isolation (a null entry + an unresolvable one)
    "9189689706": "minimal_record",    # FAKE-Herman, demographic 35 -> every optional element dropped
    "2667862660": "missing_mandatory",  # FAKE-Dannette, demographic 36 -> mandatory-field audit warnings
    "3017343148": "unsorted_group",    # FAKE-Antonio, demographic 37 -> group members oldest-first
    # The one case BP4's parameter check exists to catch, and it cannot be produced any other way:
    # a service that accepts the request but reports having applied a different date range. Drive it
    # with "Search All" or a hand-typed range, NOT the default: dhdr.default_search_days is itself
    # 120, so on a default search the echo coincides with the request and nothing fires - correctly,
    # but the run then proves nothing.
    "6767960543": "narrowed_window",   # FAKE-Chance, demographic 38 -> self link echoes BP5's 120-day default
    # BP1's category rule, all eight inputs. The contained Patient matches demographic 39's record
    # exactly, so no DHDR03.02 banner fires and category routing is the only thing on the screen.
    "4038154518": "category_routing",  # FAKE-Rosendo, demographic 39 -> 7 drug-side + 1 service-side
    # BP16. Demographic 12 has sex 'O' and was the only way to reach the non-binary comparison: every
    # non-M/F patient in the seed data has an empty HIN, so this HCN was added to that record.
    "4222222229": "gender_codeset",    # FAKE-Jessica, demographic 12 -> DHDR 'other' + 'unknown'
    # #82: the only fixture with non-completed dispense states; everything else we hold is "completed".
    "1085806462": "dispense_status",   # FAKE-Janet, demographic 42 -> completed + 4 invalid states
    # Bundle.total completeness: states 8 matches, delivers 3, offers no next link.
    "4980543590": "truncated",         # FAKE-Marla, demographic 43 -> silent truncation
    # The only fixture populating MedicationDispense.type; closes the DHDR07.01(c)/(d) coverage gap.
    "5610089932": "service_type",      # FAKE-Andera, demographic 44 -> 4 type shapes
    # OMD-supplied validation captures are WITHHELD from this branch. Four further HCN routes
    # (four `omd_*` scenarios, names withheld) mapped to real responses
    # collected by OMD's Validation Team. Both the captures and their filenames carry patient-style
    # names, so neither the routes nor the files are published here. The crafted fixtures below
    # reconstruct every structural case those captures exercised.
}

# Crafted variant, no IG example covers it. Medication-consumer.json fixes the three eHealth
# Ontario drug systems to https://, but every shipped example emits http://. A profile-conformant
# service would therefore blank generic name + AHFS class + sub-class at once, and DIN-only test
# data would hide it (ca-hc-din is genuinely http:// in both). This proves the endsWith() match at
# index.jsp:2065-2071 is scheme-agnostic.
HTTPS_SYSTEMS = {
    "http://ehealthontario.ca/fhir/NamingSystem/ca-drug-gen-name":
        "https://ehealthontario.ca/fhir/NamingSystem/ca-drug-gen-name",
    "http://ehealthontario.ca/fhir/NamingSystem/ca-on-drug-class-ahfs":
        "https://ehealthontario.ca/fhir/NamingSystem/ca-on-drug-class-ahfs",
    "http://ehealthontario.ca/fhir/NamingSystem/ca-on-drug-subclass-ahfs":
        "https://ehealthontario.ca/fhir/NamingSystem/ca-on-drug-subclass-ahfs",
}


def to_https_systems(bundle):
    """Rewrite the three eHealth Ontario drug coding systems to their profile-fixed https:// form."""
    for entry in bundle.get("entry", []):
        for contained in entry.get("resource", {}).get("contained", []):
            if contained.get("resourceType") != "Medication":
                continue
            for coding in contained.get("code", {}).get("coding", []):
                if coding.get("system") in HTTPS_SYSTEMS:
                    coding["system"] = HTTPS_SYSTEMS[coding["system"]]
    return bundle
DEFAULT_SCENARIO = "single"


def load(name):
    with open(os.path.join(EXAMPLES, name)) as fh:
        return json.load(fh)


def rewrite_links(bundle, base, page, has_next, search_id=None):
    """Rewrite the IG's `[base]` links to this stub, and set the page the walk is on.

    The viewer follows paging by presence of a `next` link (index.jsp), sending back the bundle id
    as `search-id` and its own incrementing `page`. Bundle.id IS the search-id in the IG examples:
    in BundleSearch.json the self (page=3) and next (page=4) links carry the same search-id, and it
    equals that bundle's id. So one search keeps ONE id for every page of its walk, and `search_id`
    pins it here - the page-2 fixture is a different example file with its own id, which would
    otherwise hand the viewer a changing search-id that no real service would send.
    """
    sid = search_id or bundle.get("id", "")
    bundle["id"] = sid
    qs = ("patient.identifier=https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-patient-hcn|"
          "12345678&_sort=-whenprepared&search-id=" + sid)
    links = [{"relation": "self", "url": f"{base}/MedicationDispense?{qs}&page={page}"}]
    if has_next:
        links.append({"relation": "next",
                      "url": f"{base}/MedicationDispense?{qs}&page={page + 1}"})
    bundle["link"] = links
    return bundle


# Scenarios that model an ordinary search and therefore honour the date range, the way the real
# service does. The crafted fixtures are deliberately NOT in here: they exist to exercise parsing,
# rendering and paging edge cases, and their entries are 2016-dated, so date-filtering them would
# quietly empty the very bundles those tests depend on and turn a parse test into a no-op.
DATE_FILTERED = {"single", "paged", "https_systems"}

# BP5: "If no 'date range' is sent by the EMR then DHDR returns dispense events fro the last 120 days."
# Modelling this matters more than it looks. Without it the stub returns EVERYTHING when no lower bound
# arrives, which is more generous than production -- the wrong direction for a conformance harness to err,
# because a viewer that overstates the period it retrieved looks correct against a service that never
# narrows. Applied only to the DATE_FILTERED scenarios, for the same reason they are the only ones
# filtered at all: the crafted fixtures are 2016-dated and exist to exercise parsing, not date windows.
DEFAULT_LOOKBACK_DAYS = 120

# BP15: every DHDR search hits PCR first, and "if at least one of the parametres is not matched IN PCR
# then the EMR receives msg (Patient not found)". Only once PCR matches is the DHDR searched, on HCN alone.
#
# This models the gate rather than serving a canned response, so it behaves the way PCR does: edit a
# demographic's date of birth in the EMR and the rejection appears on its own. Only HCNs listed below are
# gated -- every other patient passes straight through, so the scenarios driven before this was added
# behave exactly as they did.
#
# The error codes and their display strings are verbatim from the IG's `CodeSystem-error-code.json`, and
# the OperationOutcome shape is copied from its `OperationOutcomeSearch-example.json`. The ONE invention
# is the HTTP status: no document we hold says what a PCR rejection returns. 404 is a guess chosen to sit
# alongside the consent block's documented 403 -- do not read it as spec.
PCR_DOB = {
    "1579303915": "1975-03-14",  # FAKE-Terresa, demographic 39+1 -> EMR holds 1997-09-29, so this mismatches
}
PCR_ABSENT = {
    "1416049756",                # FAKE-Sherman -> HCN itself unknown to PCR
}
PCR_STATUS = 404


def operation_outcome(code, display, text):
    """Build a PCR rejection OperationOutcome in the IG's published shape.

    Structure from `OperationOutcomeSearch-example.json`; `code`/`display` must come from
    `CodeSystem-error-code.json` rather than being invented, so what the viewer renders is what the real
    service would send.
    """
    return {
        "resourceType": "OperationOutcome",
        "id": "OperationOutcome-pcr-" + code.lower().replace("_", "-"),
        "meta": {
            "lastUpdated": datetime.now().isoformat(timespec="milliseconds"),
            "profile": ["http://ontariohealth.ca/fhir/ehr/StructureDefinition/profile-operationOutcome"],
        },
        "text": {"status": "generated",
                 "div": '<div xmlns="http://www.w3.org/1999/xhtml">%s</div>' % text},
        "issue": [{
            "severity": "error",
            "code": "processing",
            "details": {
                "coding": [{"system": "http://ontariohealth.ca/fhir/ehr/CodeSystem/error-code",
                            "code": code, "display": display}],
                "text": text,
            },
        }],
    }


def walk_total(scenario, params, base):
    """How many entries the whole search will deliver, across every page, after date filtering.

    A single-page scenario is just its own entry count. `paged` has to be summed over both pages,
    because Bundle.total describes the search and not the page - so page 1 must already state what
    page 2 will bring.
    """
    def delivered(bundle):
        if scenario in DATE_FILTERED:
            lo, hi = date_bounds(params)
            if lo is None:
                floor = datetime.now().date() - timedelta(days=DEFAULT_LOOKBACK_DAYS)
                lo = (floor.isoformat(), False)
            bundle = apply_date_filter(json.loads(json.dumps(bundle)), lo, hi)
        return len(bundle.get("entry", []) or [])

    if scenario == "paged":
        return delivered(load("BundleSearch.json")) + delivered(load("BundleResponseSample.json"))
    status, body = scenario_response(scenario, params, base)
    if status != 200 or not isinstance(body, dict):
        return 0
    return delivered(body)


def pcr_gate(hcn, params):
    """Return an OperationOutcome when PCR would refuse this search, else None."""
    if hcn in PCR_ABSENT:
        return operation_outcome("FHIR_PTNT_2002", "HCN does not exist in PCR",
                                 "HCN does not exist in PCR")
    expected = PCR_DOB.get(hcn)
    if expected is None:
        return None
    sent = (params.get("patient.birthdate") or [None])[0]
    if sent is None:
        # PCR cannot match a parameter that was never sent. Distinct from a mismatch, and the IG has
        # its own code for it.
        return operation_outcome("FHIR_PTNT_2004", "DOB does not exist in PCR for provided HCN",
                                 "DOB does not exist in PCR for provided HCN")
    if sent != expected:
        return operation_outcome(
            "FHIR_PTNT_2005", "Patient DOB provided does not match DOB in PCR for this HCN",
            "Patient DOB provided does not match DOB in PCR for this HCN")
    return None


def date_bounds(params):
    """Read the inclusive bounds out of the repeated `whenprepared` parameter.

    The EMR sends `ge<date>` and/or `le<date>` (DHDR02.05 requires inclusive bounds). `gt`/`lt` are
    accepted too so the stub still behaves if that ever regresses - exclusive by one day.
    """
    lo = hi = None
    for raw in params.get("whenprepared", []):
        v = raw.strip()
        prefix, value = v[:2], v[2:12]
        if prefix in ("ge", "gt"):
            lo = (value, prefix == "gt")
        elif prefix in ("le", "lt"):
            hi = (value, prefix == "lt")
    return lo, hi


def apply_date_filter(bundle, lo, hi):
    """Drop dated entries outside the requested window, mirroring server-side filtering.

    Only resources carrying a date are filtered. OperationOutcome entries are service notices, not
    dispense records, so they always survive - dropping them would hide the COVaxON/consent messages
    the viewer is required to show. An entry with no readable date also survives: the stub cannot
    judge it, and the malformed-entry fixtures rely on reaching the viewer.
    """
    if not isinstance(bundle, dict) or bundle.get("resourceType") != "Bundle":
        return bundle
    if lo is None and hi is None:
        return bundle

    kept = []
    for entry in bundle.get("entry", []):
        resource = entry.get("resource") or {}
        # whenPrepared is what `_sort=-whenprepared` refers to; fall back for other resource types.
        raw = (resource.get("whenPrepared") or resource.get("whenHandedOver")
               or resource.get("effectiveDateTime"))
        when = raw[:10] if isinstance(raw, str) else None
        if when is None:
            kept.append(entry)
            continue
        if lo is not None and (when <= lo[0] if lo[1] else when < lo[0]):
            continue
        if hi is not None and (when >= hi[0] if hi[1] else when > hi[0]):
            continue
        kept.append(entry)

    bundle["entry"] = kept
    if "total" in bundle:
        bundle["total"] = len(kept)
    return bundle


def scenario_response(scenario, params, base):
    """Return (status, body) for a scenario. Paging is driven by the `page` query param."""
    page = int(params.get("page", ["1"])[0])

    if scenario == "covax_down":
        return 200, load("OperationOutcomeSearchCOVax.json")
    if scenario == "consent":
        # A consent block is a real refusal; the service answers 403 with the outcome.
        return 403, load("OperationOutcomeConsent.json")
    if scenario == "https_systems":
        return 200, rewrite_links(
            to_https_systems(load("BundleResponseSample.json")), base, page, False)
    # OMD capture routing removed for publication - see the note on the HCN table above. The
    # captures were served here as a single terminal page (self link only, no next).
    if scenario == "empty":
        return 200, {"resourceType": "Bundle", "id": "harness-empty-0001",
                     "type": "searchset", "total": 0, "link": [], "entry": []}
    if scenario == "medadmin":
        # Crafted: a searchset mixing a MedicationDispense (product) and a MedicationAdministration
        # (immunization). Tests whether the viewer handles MedicationAdministration at all.
        with open(os.path.join(HERE, "crafted_medadmin.json")) as fh:
            return 200, json.load(fh)
    if scenario == "consent_unblocked":
        # UC04 step 21: after a successful PCOI override the EMR re-queries, and DHDR returns the
        # data together with a CONSENT_TEMP_UNBLOCK OperationOutcome. Models the post-unblock state.
        with open(os.path.join(HERE, "crafted_consent_unblocked.json")) as fh:
            return 200, json.load(fh)
    if scenario == "bad_entries_paged":
        # Same malformed fixture served as a 2-page walk, to check that the skipped-record notice
        # is raised per page (processEntries runs once per page) and that paging still continues.
        with open(os.path.join(HERE, "crafted_bad_entries.json")) as fh:
            b = json.load(fh)
        return 200, rewrite_links(b, base, page, page <= 1, "harness-badpaged-0001")
    if scenario == "bad_entries":
        # Crafted: two well-formed dispenses around two that throw during construction (one entry
        # with no resource at all, one whose quantity-remaining extension has no valueQuantity).
        # Proves the viewer isolates a bad entry instead of truncating the list at it.
        with open(os.path.join(HERE, "crafted_bad_entries.json")) as fh:
            return 200, json.load(fh)
    if scenario == "missing_mandatory":
        with open(os.path.join(HERE, "crafted_missing_mandatory.json")) as fh:
            return 200, json.load(fh)
    if scenario == "minimal_record":
        # Crafted: every entry stripped to what the consumer profiles require, with all optional
        # elements dropped (strength, dose/frequency, days supply, pickup date, prescriber phone,
        # pharmacy fax, service type). Asks what the viewer renders when a legal record is sparse
        # rather than rich -- every other fixture descends from the fully-populated IG sample.
        with open(os.path.join(HERE, "crafted_minimal_record.json")) as fh:
            return 200, json.load(fh)

    if scenario == "service_type":
        # DHDR07.01(c)/(d) source the pharmacy service type and description from
        # MedicationDispense.type -- an element populated in NO IG example, crafted fixture or OMD
        # capture, so the primary read has never run against data and every service row we have ever
        # rendered came from the fallback. Four shapes: two codings (type + description), one coding,
        # text only, and absent. All four are service-category events, since type is only read there.
        with open(os.path.join(HERE, "crafted_service_type.json")) as fh:
            return 200, json.load(fh)

    if scenario == "truncated":
        # The case Bundle.total exists to catch and no other fixture can produce: the service states
        # it matched more resources than it delivered, and offers NO next link to fetch the rest. A
        # viewer that pages purely on the presence of `next` cannot tell this from a complete result,
        # so it renders a partial medication history and counts it as whole.
        b = json.load(open(os.path.join(HERE, "crafted_category_routing.json")))
        b["id"] = "CRAFTED-truncated"
        b["entry"] = b["entry"][:3]
        b["total"] = 8          # states 8, delivers 3
        b["link"] = []          # and offers no way to get the other 5
        return 200, b

    if scenario == "dispense_status":
        # MedicationDispense.status is 1..1 and mustSupport, and the FHIR-to-CDS mapping declares the
        # full medication-dispense-status value set for it -- but all 130 instances across every IG
        # example, crafted fixture and OMD capture are "completed", so nothing could exercise the
        # non-completed states. Five events: the completed control plus cancelled, entered-in-error,
        # stopped and declined. See the ledger's #82.
        with open(os.path.join(HERE, "crafted_dispense_status.json")) as fh:
            return 200, json.load(fh)

    if scenario == "gender_codeset":
        # BP16: the EMR must not coerce local gender values into the FHIR set. Every non-M/F patient in
        # the seed database had an empty HIN, so none could reach a search at all -- demographic 12 was
        # given an HCN (an empty field filled, revertible by clearing it) to make this testable. Two
        # events differing ONLY in gender: `other`, which should agree with the EMR's `O`, and `unknown`,
        # which genuinely differs and must be flagged. Everything else matches demographic 12's record.
        with open(os.path.join(HERE, "crafted_gender_codeset.json")) as fh:
            return 200, json.load(fh)

    if scenario == "category_routing":
        # Crafted: BP1's routing rule names THREE Drug-side inputs -- code="product", code blank/null,
        # and category missing entirely -- and no other fixture covers the last two. Even
        # crafted_minimal_record, which drops every other optional element, keeps category. Eight
        # entries differing ONLY in `category`: the two controls plus five ways of saying "no usable
        # code" plus an unsupported-but-in-codeset "device". Seven must land in the Drug views and one
        # in the PharmaService views; nothing may be dropped. The generic name carries the case label
        # so the screen is self-describing.
        with open(os.path.join(HERE, "crafted_category_routing.json")) as fh:
            return 200, json.load(fh)

    if scenario == "no_pharmacist_licence":
        # Crafted: three pharmacy-service events differing only in how the dispensing pharmacist is
        # identified -- fully identified (control), no pharmacist licence at all, and a licence with
        # a given name but no surname. Every other fixture, including the OMD captures, carries a
        # complete pharmacist on every service event, so the separator guards in the print's
        # pharmacist cell were unreachable and could not be verified. The viewer only resolves a
        # pharmacist from a Practitioner carrying ca-on-license-pharmacist, so the middle event
        # resolves no pharmacist at all rather than a nameless one.
        with open(os.path.join(HERE, "crafted_no_pharmacist_licence.json")) as fh:
            return 200, json.load(fh)

    if scenario == "unsorted_group":
        # Crafted: group members arrive OLDEST-FIRST, the order a server produces when it does
        # not honour the `_sort=-whenprepared` hint. Decides the two DHDR04.02 clauses that a
        # pre-sorted fixture cannot: "a group is represented by displaying only the most recent
        # event" and "events within an expanded group MUST be displayed in descending
        # chronological order" (DHDR07.03 for services).
        with open(os.path.join(HERE, "crafted_unsorted_group.json")) as fh:
            return 200, json.load(fh)

    if scenario == "group_order":
        # Crafted: TWO drug groups and TWO service groups, every event emitted oldest-first across
        # both, so the order each group is first encountered is the reverse of the order its head
        # date demands. Decides the one DHDR04.02/07.02 clause no single-group fixture can reach:
        # "Groups of drug dispense events MUST be ordered by Dispensed Date in descending
        # chronological order". Expect drugs Jun 1 before May 20, services Oct 5 before Sep 3.
        with open(os.path.join(HERE, "crafted_group_order.json")) as fh:
            return 200, json.load(fh)

    if scenario == "grouping":
        # Crafted: no IG example exercises grouping (BundleResponseSample has one product and one
        # service, so grouped and ungrouped render identically). 4 drug dispenses pairwise distinct
        # on the DHDR04.02 3-point match -> none may group; 3 pharmacy services where two share a
        # service type and one does not -> one group of 2 plus an ungrouped event (DHDR07.02).
        with open(os.path.join(HERE, "crafted_grouping.json")) as fh:
            return 200, json.load(fh)

    if scenario == "nurse_reason":
        # Crafted (IG-validated): nurse-practitioner prescriber (ca-on-license-nurse) + a 2-element
        # reasonCode. Validates the #2 (non-physician licence) and #4 (reasonCode array) fixes.
        with open(os.path.join(HERE, "crafted_nurse_reason.json")) as fh:
            return 200, json.load(fh)
    if scenario == "paged":
        # Two pages: BundleSearch (has next) then BundleResponseSample (terminal). Both pages are
        # one search, so both carry BundleSearch's id as the search-id.
        walk_id = load("BundleSearch.json")["id"]
        if page <= 1:
            return 200, rewrite_links(load("BundleSearch.json"), base, 1, True, walk_id)
        return 200, rewrite_links(load("BundleResponseSample.json"), base, page, False, walk_id)

    return 200, rewrite_links(load("BundleResponseSample.json"), base, page, False)


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt, *args):  # quieter default logging
        pass

    def do_OPTIONS(self):
        # Harness-only: lets the browser POST a captured artifact (a generated PDF, say) back to
        # disk so it can be inspected with pdftotext. Nothing to do with DHDR.
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_POST(self):
        if self.path.split("?")[0] != "/__capture":
            self.send_response(404)
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            return
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length)
        # ?name=<basename> writes into print-baselines/ instead of overwriting captured.bin, so one
        # page can capture several documents without a round trip to rename between each. Basename
        # only, [A-Za-z0-9._-], so a name off the wire cannot escape the directory.
        wanted = parse_qs(urlparse(self.path).query).get("name", [""])[0]
        safe = re.sub(r"[^A-Za-z0-9._-]", "", os.path.basename(wanted))
        if safe:
            outdir = os.path.join(HERE, "print-baselines")
            os.makedirs(outdir, exist_ok=True)
            out = os.path.join(outdir, safe)
        else:
            out = os.path.join(HERE, "captured.bin")
        with open(out, "wb") as fh:
            fh.write(body)
        sys.stderr.write("[stub] captured %d bytes -> %s\n" % (len(body), out))
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(b"ok")

    @staticmethod
    def echo_search_params(body, params, scenario, applied=None):
        """Echo the requested whenprepared bounds into the bundle's links, as the service does.

        BP4 requires the consumer to check the parameters the server reports it actually used, and
        those come back in the `self` link - BundleSearch.json carries
        `whenprepared=ge2018-02-24&whenprepared=le2019-02-25`. rewrite_links() builds a fixed query
        string with no date bounds at all, so without this every search would look like the service
        had dropped them and the viewer's check would fire for the wrong reason.

        `narrowed_window` echoes BP5's 120-day default instead of what was asked for, which is the
        silent substitution the check exists to catch.
        """
        links = body.get("link")
        if not isinstance(links, list):
            return body
        # `applied` is set when the service substituted its own bound (BP5's default). A real service
        # reports what it USED, not what it was asked for, so the substitution has to reach the self
        # link -- otherwise the viewer's BP4 check cannot see the narrowing that just happened.
        sent = applied if applied is not None else params.get("whenprepared", [])
        if scenario == "narrowed_window":
            hi = datetime.now().date()
            sent = ["ge%s" % (hi - timedelta(days=120)), "le%s" % hi]
        for link in links:
            url = link.get("url", "")
            if "?" not in url:
                continue
            head, qs = url.split("?", 1)
            kept = [p for p in qs.split("&") if not p.startswith("whenprepared=")]
            # After patient.identifier, where the IG examples put them.
            kept[1:1] = ["whenprepared=" + v for v in sent]
            link["url"] = head + "?" + "&".join(kept)
        return body

    def do_GET(self):
        parsed = urlparse(self.path)
        params = parse_qs(parsed.query, keep_blank_values=True)

        ident = params.get("patient.identifier", [""])[0]
        m = re.search(r"\|(\d+)", ident)
        hcn = m.group(1) if m else None
        scenario = HCN_SCENARIOS.get(hcn, DEFAULT_SCENARIO)

        # Record what the EMR actually sent. This is what the query-side requirements are
        # asserted against (inclusive ge/le dates, absent patient.gender, _sort, _count).
        record = {
            "at": datetime.now().isoformat(timespec="seconds"),
            "path": parsed.path,
            "hcn": hcn,
            "scenario": scenario,
            "params": {k: v for k, v in params.items()},
            "auth_bearer_present": self.headers.get("Authorization", "").startswith("Bearer "),
            "x_gtwy_client_id": self.headers.get("X-Gtwy-Client-Id"),
            "x_request_id": self.headers.get("X-Request-Id"),
        }
        with open(REQUEST_LOG, "a") as fh:
            fh.write(json.dumps(record) + "\n")

        if not parsed.path.rstrip("/").endswith("/MedicationDispense"):
            self.send_error(404, "only /MedicationDispense is stubbed")
            return

        base = f"http://localhost:{PORT}"

        # BP15: PCR runs BEFORE the DHDR is searched, so this gate sits ahead of scenario_response --
        # a refused search never reaches the repository and returns no bundle at all.
        refusal = pcr_gate(hcn, params)
        if refusal is not None:
            payload = json.dumps(refusal).encode()
            sys.stderr.write("[stub] %s -> PCR REFUSED %s\n"
                             % (hcn, refusal["issue"][0]["details"]["coding"][0]["code"]))
            self.send_response(PCR_STATUS)
            self.send_header("Content-Type", "application/fhir+json")
            self.send_header("Content-Length", str(len(payload)))
            self.send_header("X-Request-Id", self.headers.get("X-Request-Id", "harness-req"))
            self.end_headers()
            self.wfile.write(payload)
            return

        status, body = scenario_response(scenario, params, base)

        # The real service applies the date range; the EMR does no client-side date filtering (it
        # must display the full list it is given, DHDR05.01). Without this the stub returned its
        # fixture whatever range was asked for, which made every date search look broken.
        filtered_out = 0
        applied_bounds = None
        if scenario in DATE_FILTERED and status == 200:
            lo, hi = date_bounds(params)
            # BP5. A real service does not return everything when the lower bound is absent -- it
            # substitutes its own default and reports what it used, which is why the effective bounds
            # (not the requested ones) are what gets echoed into the self link below.
            if lo is None:
                floor = datetime.now().date() - timedelta(days=DEFAULT_LOOKBACK_DAYS)
                lo = (floor.isoformat(), False)
                applied_bounds = ["ge" + lo[0]] + (["le" + hi[0]] if hi else [])
            before = len(body.get("entry", []) or [])
            body = apply_date_filter(body, lo, hi)
            filtered_out = before - len(body.get("entry", []) or [])
            # The scenarios decide `next` by page number, before filtering, so a window that matches
            # nothing would still advertise another page and send the viewer on a walk through empty
            # pages. No real service offers a next page for an empty result set.
            if not body.get("entry") and isinstance(body.get("link"), list):
                body["link"] = [l for l in body["link"] if l.get("relation") != "next"]

        if status == 200:
            body = self.echo_search_params(body, params, scenario, applied_bounds)

        # Bundle.total is the count of resources matching the whole search, not the page. The IG's two
        # paged examples are separate documents and declare 63 and 2 for what the stub serves as ONE
        # walk, which no real service would do -- and a viewer that checks completeness against total
        # would then flag a fixture inconsistency as a defect. So the stub states the total the walk
        # actually delivers, after date filtering, exactly as a service reports what it matched.
        # `truncated` is the deliberate exception: it under-delivers against its own stated total.
        if status == 200 and isinstance(body, dict) and body.get("resourceType") == "Bundle" \
                and scenario != "truncated":
            body["total"] = walk_total(scenario, params, base)

        payload = json.dumps(body).encode()

        self.send_response(status)
        self.send_header("Content-Type", "application/fhir+json")
        self.send_header("Content-Length", str(len(payload)))
        self.send_header("X-Request-Id", self.headers.get("X-Request-Id", "harness-req"))
        self.send_header("X-LobTxId", "harness-lob-tx")
        self.send_header("X-Correlation-Id", "harness-correlation")
        self.end_headers()
        self.wfile.write(payload)

        sys.stderr.write(f"[stub] {hcn or '-'} -> {scenario} page="
                         f"{params.get('page', ['1'])[0]} -> {status}"
                         + (f" (date filter dropped {filtered_out})" if filtered_out else "") + "\n")
        sys.stderr.flush()


if __name__ == "__main__":
    if not os.path.isdir(EXAMPLES):
        sys.exit(f"examples not found at {EXAMPLES}")
    sys.stderr.write(f"[stub] serving DHDR examples from {EXAMPLES}\n")
    sys.stderr.write(f"[stub] listening on http://localhost:{PORT}/MedicationDispense\n")
    sys.stderr.write(f"[stub] request log: {REQUEST_LOG}\n")
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
