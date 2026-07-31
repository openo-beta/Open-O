# DHDR spec working copy — provenance (verified 2026-07-16)

## Which package is authoritative

Simplifier guide: https://simplifier.net/guide/ca-on-dhdr-fhir-provider-iguide  → **"IG Version: v.4.0.3 Final"**
Guide slug `ca-on-dhdr-fhir-provider-iguide` == FHIR package id `ca.on.dhdr.r4`. Same artifact, two names.

The guide's Downloads page "here" link requests **`ca.on.dhdr.r4/4.0.3-snapshot5e`**, which does not exist.
Simplifier's resolver rejects it and falls back with:

> The requested version '4.0.3-snapshot5e' was not found. You have been taken to version '4.0.3-snapshot9'.

**=> `4.0.3-snapshot9` is what anyone following the guide actually receives. Treat it as the reference.**

Registry (https://packages.simplifier.net/ca.on.dhdr.r4): 14 versions — `0.9.0` + `4.0.3-snapshot1..13`.
`dist-tags.latest` = **`0.9.0`** (NOT a 4.0.3), which conflicts with the guide's "v4.0.3 Final".

## Working copies on disk

| dir | package | why kept |
|---|---|---|
| `igcheck/x-snapshot9/` | `ca.on.dhdr.r4@4.0.3-snapshot9` | **primary** — what the guide serves |
| `igcheck/x-4.0.3-snapshot13/` | `ca.on.dhdr.r4@4.0.3-snapshot13` | newest 4.0.3 |
| `igcheck/x-0.9.0/` | `ca.on.dhdr.r4@0.9.0` | registry `latest`; only version declaring the bare MedicationDispense SD |
| `igcheck/ehr-x-1.0.0-snapshot22/` | `ca.on.ehr.r4@1.0.0-snapshot22` | holds the **Response** profiles the guide links |
| `dhdr_ig/` | `ca.on.dhdr.r4@4.0.3-snapshot1` | STALE — the 2026-07-03 audit copy; superseded, keep only for audit traceability |

## Version inconsistencies found in the IG itself

- Guide Downloads link → `snapshot5e` (404, resolves to snapshot9).
- Guide's *profile* links → `ca.on.ehr.r4@1.0.0-snapshot22`.
- But `snapshot9/package.json` declares dep `ca.on.ehr.r4@1.0.0-snapshot16`.
- Our old audit copy (`snapshot1`) declared dep `ca.on.ehr.r4@1.0.0-snapshot4`.
- `ca.on.ehr.r4` registry `latest` = `1.0.1`.
=> four different ca.on.ehr.r4 pointers. Which one OMD validates against is unresolved.

## Parallel sources — THREE, and they have already diverged

The guide mixes **live-project** links with **pinned-package** links on the same page:

| source | mutable? | how the guide links it |
|---|---|---|
| project `ca-on-dhdr-r4` | **yes** — "Last updated 2026-01-30" | Examples (`simplifier.net/ca-on-dhdr-r4/<id>/~json`) + some Profiles (`scope=project:ca-on-dhdr-r4`) |
| package `ca.on.dhdr.r4` | no (14 pinned versions) | Downloads "here" → `snapshot5e` (404) → **snapshot9** |
| package `ca.on.ehr.r4` | no (31 versions) | Response profiles → `scope=package:ca.on.ehr.r4@1.0.0-snapshot22` |

**Proof they diverge:**
- `MedicationDispenseContained` and `DHDR-MedDisCont` → **404 in the live project**, yet still shipped in
  package snapshot1 *and* snapshot9. The project deleted examples the packages still distribute.
- Example counts churn: snapshot1 = 36, snapshot9 = 43, snapshot13 = **14** (pruned, not fixed —
  the `http://` samples were *deleted*, which is why snapshot13 greps "clean").
- `BundleDocumentMedicationDispenseResponse.json` has **3 different sha256** across snapshot1/9/13
  (f5765254…, 4e035ba3…, 7ffa57b5…) — same filename, different content per version.

**Where the `http://` form actually lives** (snapshot9, 6 files only):
`DHDR-MediactionSample.json`, `BundleResponseSample.json`, `DHDR-MedDisCont.json`,
`MedicationDispense-single-event-search-example.json`, `MedicationDispenseContained.json`,
`MedicationAdministration-search-example-single-event.json`.
Two of those are the *contained-resource* shape the OpenO parse walks — almost certainly what the
original parse was coded against. Both are now 404 in the live project.

Meanwhile `BundleDocumentMedicationDispenseResponse.json` — the Response bundle, closest to what the
EMR actually receives — uses **`https://`** in *all three* package versions. Profile and canonical
example agree; only the legacy standalone samples disagree.

**Limitation:** the live project's raw JSON could NOT be retrieved (Simplifier returns an SPA shell to
curl for `/~json`, `?download=true`, `$download`, and `Accept: application/fhir+json`; the WebFetch
summariser also could not see the JSON body). Existence is proven via 200/404; **content is not
diffed**. To diff, use the browser's "Download as JSON" button.

**Why this does not threaten the #22 fix:** the fix suffix-matches the trailing NamingSystem id, so it
is correct under *every* parallel source regardless of scheme. Resolving which version wins is a
certification question, not a correctness one. Rule going forward: **code against profiles, never
against `examples/`** — that is precisely how #22 was born.

## Verified facts (stable across snapshot1 / snapshot9 / snapshot13 / 0.9.0)

- `Medication-consumer.json` fixes, via `fixedUri`:
  - `drugGeneric.system`   = `https://ehealthontario.ca/fhir/NamingSystem/ca-drug-gen-name`
  - `drugClass.system`     = `https://ehealthontario.ca/fhir/NamingSystem/ca-on-drug-class-ahfs`
  - `drugSubclass.system`  = `https://ehealthontario.ca/fhir/NamingSystem/ca-on-drug-subclass-ahfs`
  - `drugIdentifier.system`= `http://hl7.org/fhir/NamingSystem/ca-hc-din`   (genuinely http)
- `ca.on.ehr.r4@1.0.0-snapshot22` `Medication-EHR.json` (`profile-medication`) element comments:
  "Generic medications will have a fixed value of https://ehealthontario.ca/... on coding.system"
- **The `examples/` in snapshot9 use BOTH schemes** for the same NamingSystems
  (http x7/7/7, https x5/1/1) — the examples are self-inconsistent. Any hardcoded scheme is fragile;
  suffix-matching the trailing NamingSystem id is the only robust read.
- CapabilityStatement `dhdr-consumer-ig-capabilitystatement.json` (identical snapshot1/9/13):
  - `composition.medicationdispense.whenprepared`: "Prefixes such as gt, lt, ge, le are allowed"
  - `_sort`: "If not indicated, the default sort order is descending by whenprepared"
- `0.9.0/MedicationDispense.json` declares SD url
  `http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-profile-MedicationDispense`
  => the bare canonical is a real profile, NOT the submission profile (that one ends `-submission`).

## Re-fetch commands

```bash
curl -s https://packages.simplifier.net/ca.on.dhdr.r4 | python3 -m json.tool   # version list
curl -sO https://packages.simplifier.net/ca.on.dhdr.r4/4.0.3-snapshot9         # guide-served
curl -sO https://packages.simplifier.net/ca.on.ehr.r4/1.0.0-snapshot22         # Response profiles
```


## Declared profiles that resolve nowhere (noted 2026-07-29, corrected same day)

**First conclusion was wrong and is retracted:** `ca.on.ehr.r4` is *not* missing. We hold
`igcheck/ehr-x-1.0.0-snapshot22` and `igcheck/ehr-x-1.0.1` — both **newer** than the `snapshot16`/`snapshot19`
the DHDR packages depend on. They were missed by a `find -iname "*ca.on.ehr*"`, because the directories are
named `ehr-x-*`; the package id only appears inside `package.json`. **Search by `package.json` name, not by
directory name.**

**The real finding, which survives with the dependency in hand.** Resolving the DHDR `snapshot9`
CapabilityStatements against the full closure (DHDR + `ca.on.ehr.r4`, 83 canonicals) leaves **5 of 7
declared profiles dangling**:

| Declared by | Resource | Canonical | Resolves? |
|---|---|---|---|
| `DHDR-server_Consumer.json` | MedicationDispense | `ca-on-medications-consumer-profile-MedicationDispense` | **yes** |
| `dhdr-server-provider_qeury.json` | MedicationDispense | `ca-on-medications-profile-MedicationDispense` | no |
| `dhdr-server-provider_qeury.json` | MedicationAdministration | `ca-on-medications-profile-MedicationAdministration` | no |
| `dhdr-consumer-ig-capabilitystatement.json` | Bundle | `profile-Bundle-document` | no |
| `dhdr-server-medicationData_*` (×3) | Bundle | `ca-on-medications-profile-Bundle-document` | no |

The cause is visible in the history: `0.9.0` bundled the `ca-on-medications-*` profiles directly and was
self-contained (3 declared, 0 dangling). The IG was later reorganised onto `ontariohealth.ca/fhir/ehr/…`
canonicals in `ca.on.ehr.r4`, **but the CapabilityStatements were never updated to the new names.**

**Why it matters for conclusions drawn here.** The profile governing the *EMR-facing query* response is one
of the dangling ones, and the two plausible resolutions disagree on a point we relied on:

- `0.9.0`'s copy of `ca-on-medications-profile-MedicationDispense` — `contained` **min=1**
- `ca.on.ehr.r4`'s `profile-medicationDispense` (the apparent successor) — `contained` **unconstrained**

So "the response must carry a contained Medication" is supported by the bundled *consumer* profile and by
the older copy, and **not** by the successor. Treat it as likely, not settled.

**Checked against the full closure and holding:** `MedicationDispense.type` is unconstrained in *every*
MedicationDispense profile available — DHDR consumer, DHDR submission, `0.9.0`'s `ca-on-medications-*`
(123 elements) and `ca.on.ehr.r4`'s `profile-medicationDispense` (134 elements, both versions) — and absent
from all 49 examples. That is the strongest form the check can take with what is published.

**Also corrected:** the DHDR error codes *are* documented. `ca.on.ehr.r4` ships `CodeSystem-ErrorCode`
(`ErrorCodeEHR`) with **59 concepts**, including `IN_0045 — "Invalid or missing query parameters"`. An
earlier note here saying the code list was undocumented was searching the DHDR packages only.
