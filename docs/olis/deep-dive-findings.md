# OLIS Deep-Dive Findings

A re-audit of every OLIS conformance requirement against the **current** code,
after the C1 / C2 / C3 / C4 / B1 work landed. Cross-checks the claimed statuses
in `requirements-analysis.md` (written against an older code state) and the
ticket coverage in `readiness-plan.md`.

**Audit date:** 2026-05-14
**Method:** four parallel read-only code audits, one per requirement group, each
verifying claims against current source.

---

## 1. Headline

- **No regressions.** Every previously-claimed status holds or has improved.
- **Remediation landed cleanly.** C1/C2/C3/C4/B1 each moved their target requirements
  forward; most previously-"Partially Done" items in tracks B/C are now functionally Done
  (pending the deploy-time and F1 verifications already documented in the readiness plan).
- **One factual error found in `requirements-analysis.md`** — the OLIS04.09 "stricter than
  spec" note is wrong (see §3).
- **Remaining open items are concentrated and known** — they're verification/decision items
  (Track D/E/F) plus two genuine code-quality fragilities (§4).

---

## 2. Status map (current, post-remediation)

| ID | Requirement | Claimed | Current | Moved by |
|----|-------------|---------|---------|----------|
| OLIS01.01 | Create/send Preload (Z04) queries | Meets | ✅ Accurate | — |
| OLIS01.02 | Preview lab info before saving | Partial | ✅ Done | B1 (Lab Name + Match + Blocked columns) |
| OLIS01.03 | Filter/sort Preload preview | Partial | ✅ Done | B1 (practitioner filter) |
| OLIS01.04 | Act upon lab info (save/sign-off/remove) | Partial | ✅ Done | C1 (server-side remove + audit) |
| OLIS01.05 | Manage duplicates | Meets | ✅ Accurate | — |
| OLIS02.01 | Automated practitioner query (polling) | Meets | ✅ Accurate | — |
| OLIS02.02 | Results in inbox + patient chart | Meets | ✅ Accurate | — |
| OLIS02.03 | Configure display of unmatched results | Partial | ✅ Done | C3 (per-provider override) |
| OLIS02.04 | Manual practitioner query submission | Meets | ✅ Accurate (not independently re-traced this pass) | — |
| OLIS03.01 | Create/send Patient query on-demand | Meets | ✅ Accurate | — |
| OLIS03.02 | Preview Patient query results | Partial | ✅ Done | B1 — **see §3, corrects an audit discrepancy** |
| OLIS03.03 | Filter/sort Patient query preview | Partial | ✅ Done | B1 (practitioner filter) |
| OLIS03.04 | Act upon Patient query results | Partial | ✅ Done | C1 (server-side remove + audit) |
| OLIS03.05 | Manage duplicates from Patient query | Meets | ✅ Accurate | — |
| OLIS03.06 | Override patient consent directive | Partial | ✅ Done | C2 (Transaction ID in consent-override audit) |
| OLIS04.01 | Indicate test request status | Meets | ✅ Accurate | — |
| OLIS04.02 | Indicate test result status | Meets | ✅ Accurate | — |
| OLIS04.03 | List of participating laboratories | Partial | ⚠️ Partial — maintainability improved (C4), **completeness gap remains** | C4 (partial) |
| OLIS04.04 | Manage duplicates | Meets | ✅ Accurate | — |
| OLIS04.05 | Display alternate test name (not LOINC) | Partial | ⚠️ Partial — E1 verification still open | — |
| OLIS04.06 | Act upon unmatched patient results | Partial | ⚠️ Partial — D1 (needs product decision) | — |
| OLIS04.07 | Integrated OLIS interface | Meets | ✅ Accurate | — |
| OLIS04.08 | Maintain query parameter lists | Meets | ✅ Accurate — but see §4 (consumption-side perf) | — |
| OLIS04.09 | Automated patient matching | Meets *(w/ false note)* | ✅ Accurate — **`requirements-analysis.md` note is wrong, see §3** | — |
| OLIS04.10 | Identify blocked lab reports/results | Partial | ⚠️ Improved (B1 per-row column) — **but underlying detection is fragile, see §4** | B1 (partial) |
| OLIS05.01 | Manage HL7 error messages | Meets | ✅ Accurate | — |
| OLIS05.02 | Manage XML error messages | Meets | ✅ Accurate | — |
| OLIS05.03 | Manage SOAP error messages | Meets | ✅ Accurate | — |
| OLIS05.04 | Network error management | Meets | ✅ Accurate | — |
| OLIS06.01 | OLIS conformance testing | Not verified | 🔴 Not verified — external, Track F1 | — |
| OLIS06.02 | Log all messages sent/received | Partial | ⚠️ Improved (C2 closed the Transaction-ID gap) — **Gap 2 is a deliberate design choice, see §4** | C2 (partial) |
| OLIS06.03 | Log removed/rejected lab reports | Partial | ✅ Done — cosmetic terminology nit → F1 | C1 (manual-removal audit) |

---

## 3. Corrections to `requirements-analysis.md`

These are factual errors / stale claims found during the audit. **Not yet applied** — listed here for a deliberate doc-correction pass.

### 3a. OLIS04.09 — the "stricter than spec" note is wrong

`requirements-analysis.md` (OLIS04.09 reasoning) claims `MessageUploader.willOLISLabReportMatch()`
requires **first name** to match in addition to HCN + Sex + DOB + Last name, i.e. "stricter than spec."

**This is false.** The actual matching SQL in `willOLISLabReportMatch()` keys on
`hin` + `last_name` + `year/month/date_of_birth` + `sex` — the `firstName` parameter is passed
in but **never used in the query**. The implementation is **exactly spec-strict**, not stricter.

**Consequence for the readiness plan:** Track **E2** ("Document OLIS04.09 first-name strictness")
is built on this false premise — there is no first-name strictness to document or relax. E2
should be reframed to simply: *correct the OLIS04.09 note in `requirements-analysis.md`* (a
documentation fix, not a code decision).

### 3b. OLIS03.02 — audit discrepancy, now reconciled

One audit pass reported OLIS03.02's match-status indicator as "still icon-only." That is
**incorrect** — OLIS03.02 (Patient Query preview) and OLIS01.02 (Preload preview) are rendered
by the **same `Results.jsp`**, and B1 added the explicit **Match** column there. OLIS03.02 gets
the Lab Name, Match, and Blocked columns exactly as OLIS01.02 does. Status: **Done** (corrected
in the table above). No action needed beyond not trusting the stale "icon-only" wording.

---

## 4. Genuinely-open items after remediation

Everything here is either a known Track D/E/F item or a code-quality fragility worth recording.

### 4a. OLIS04.10 — `isReportBlocked()` detection is structurally fragile

B1 added a per-row **Blocked** column, and it works for **ERP** responses — which is what real
OLIS sends. But the underlying detection has a real fragility, **confirmed empirically** during
B1 testing:

- `OLISHL7Handler.init()` detects blocked status by walking
  `terser.getFinder().getRoot().getNames()` — which only exposes **root-level** segments.
- For a bare `ORU^R01^ORU_R01` message, HAPI's `PipeParser` resolves it to the **typed**
  `v24.message.ORU_R01` structure and nests the non-standard `ZPD` segment inside the patient
  group → the root-level walk never sees it → `parseZPDSegment()` never runs → blocked not detected.
  (`sample-response-blocked.hl7` reproduced exactly this — Blocked column stayed empty.)
- For an `ERP^Znn^ERP_R09` message, HAPI has no structure class → parses as a flat
  `GenericMessage` → `ZPD` sits at root → detected. (`sample-response-erp-blocked.hl7` worked.)

**Real-world risk: low** — real OLIS query responses are `ERP`, which parse flat. But it's the
**same root-cause family as G2** (the NTE structure-walker issue): `init()`'s root-only segment
walk is brittle for anything HAPI nests into a typed group. Worth a real `*_ERP_Response.HL7`
confirmation at F1, and a candidate for a future hardening pass on `OLISHL7Handler.init()`.

> Note: one audit pass asserted `NoValidation` makes `PipeParser` always produce a
> `GenericMessage`. That is **not correct** — `NoValidation` is a validation context, not a
> structure-resolution switch; `PipeParser` still resolves to a typed structure class when one
> exists. The empirical B1 test (ORU fails, ERP works) is the authority here.

**Permanent-save propagation (traced 2026-05-15):** verified by walking
Results.jsp → `OLISAddToInbox2Action` → upload-handler `OLISHL7Handler.parse()` →
`MessageUploader.routeReport()`. **Three findings:**

- `MessageUploader` has **zero references** to `reportBlocked`/`isReportBlocked()`/`isOBRBlocked()`;
  it never reads the parser's blocked state.
- `Hl7TextInfo` (the lab-metadata table) has **no `blocked` column**; the OLIS save path also
  does not write a `measurementsExt` `reportBlocked=Y` row (that key is only written by the CDS
  XML import path in `ImportDemographicDataAction42Action`, not by OLIS HL7 ingestion).
- **What does survive:** the full raw HL7 body — including the ZPD segment — is base64-stored
  to `hl7_text_message`. At view time, `labDisplayOLIS.jsp` (and `OLISLabPDFCreator`) call
  `Factory.getHandler(segmentID)` → re-decodes → re-instantiates `OLISHL7Handler` → re-runs
  `init()` → checks `isReportBlocked()` / `isOBRBlocked(obr)` to render the
  "Do Not Disclose Without Explicit Patient Consent" banner.

**Net:** blocked status **does propagate** through permanent save, but **implicitly** — via the
preserved raw HL7 + re-parse-on-view. For real OLIS data (ERP envelopes saved as-is), the same
`GenericMessage`-flat-ZPD path that worked at preview time also works at display time. For bare
ORU fixtures it would fail at display time the same way it fails at preview — same root cause.

**Two consequences worth flagging:**

1. **No fast-query path for blocked labs.** Can't SQL-filter "show me all blocked labs received
   this month" — every row would need to be base64-decoded and re-parsed. Not a conformance gap,
   but a usability/audit concern if blocked-lab reporting is ever wanted.
2. **The preview Blocked column, the display banner, the PDF render, and `isOBRBlocked()` all
   share one fragility.** They route through the same `init()` segment-walk. The B1 work, the
   `labDisplayOLIS` banner, and `OLISLabPDFCreator` all win or lose together. A single hardening
   pass on `init()` (recurse into typed-message structure groups) would fix all four sites at
   once.

**Closure (2026-05-15, commit `3161c1fd50`):** the proposed hardening landed in a slightly
different form — instead of teaching `init()` to recurse into typed groups, the parser is now
configured with a custom `ModelClassFactory` that forces every message to resolve as
`GenericMessage` regardless of MSH-9-3. All segments therefore land flat at the message root,
making the existing root-only segment-walk correct by construction. **All four call sites now
work for both bare `ORU^R01` and `ERP^Z01` fixtures.** Verified via Playwright on
`sample-response-blocked.hl7` (bare ORU) — Blocked column lights up in preview AND display banner.
Same fix also closes the related NTE-rendering fragility flagged in G2.

### 4b. OLIS06.02 — "received messages" half is a deliberate design choice

C2 closed the Transaction-ID gap. The second gap from `requirements-analysis.md` — "received
messages not logged as a distinct row" — remains, **by design**: C2 enriches the single SENT
`OLISQueryLog` row post-response rather than writing a separate RECEIVED row. This is functionally
sound (one row carries the full correlation: EMR uuid + OLIS Transaction ID). Whether it
satisfies the literal spec wording ("log all messages sent **to/received from** OLIS") is an
**F1 decision with OntarioMD** — already flagged in the C2 readiness-plan entry.

### 4c. OLIS04.03 — completeness gap (already documented under C4)

C4 single-sourced the lab list (maintainability) but did **not** make it comprehensive or
OLIS-synced. OLIS04.03 stays Partial. Tracked in the C4 entry; closing it is an F1 / D2-adjacent
decision.

### 4d. OLIS04.08 — consumption-side performance problem

Not a conformance gap, but surfaced during this work and recorded under **D2**: `Search.jsp`
bulk-loads ~48k+3k nomenclature rows and renders ~51k `<option>` elements on every page load —
the cause of the long-observed slow OLIS Search page. Strengthens the case for a nomenclature
restructuring pass (consumption side, distinct from D2's refresh-side question).

### 4e. Track D/E/F items (unchanged, still open)

- **D1** — OLIS-specific manual-match UI (OLIS04.06): needs product decision.
- **D2** — Nomenclature programmatic refresh (OLIS04.08): needs design decision; now also carries the consumption-side perf note.
- **E1** — Verify `OLISResultNomenclature.getName()` == OLIS "Alternate Name 1" (OLIS04.05).
- **E2** — Reframed: correct the OLIS04.09 note (see §3a) — no longer a "strictness" decision.
- **F1** — OntarioMD conformance testing; also the home for: real ERP fixture verification, the OLIS06.02 "received messages" decision, the OLIS06.03 terminology nit, the C2 MSA-2-intent confirmation.

---

## 5. Track coverage assessment

Every gap identified maps to an existing readiness-plan track — **no orphan gaps** were found.
The audit did surface two **documentation** corrections (§3) that aren't "tickets" so much as
fixes to the analysis record, plus the OLIS04.10 fragility (§4a) which is currently only
implied by G2 and deserves its own explicit note.

**Doc actions:**
1. ✅ **Applied** — corrected the OLIS04.09 note in `requirements-analysis.md` (§3a) and reframed
   E2 in `readiness-plan.md` as closed/verified-spec-exact.
2. ✅ **Applied** — extended `readiness-plan.md` G2 with an explicit bullet capturing the
   `isReportBlocked()` / nested-ZPD fragility from §4a (same root cause as the NTE case).
3. ⏳ **Open decision** — whether to refresh the stale "Partially Done" statuses in
   `requirements-analysis.md` for the items now Done, or leave that file as the historical
   record and treat this doc + the readiness plan as current truth. (Recommendation: leave
   `requirements-analysis.md` as the dated baseline; this doc + the readiness-plan track
   statuses are current truth.)

---

## 6. Bottom line

The remediation work holds up under re-audit. Of the ~33 OLIS requirements, the ones still
genuinely open are a small, well-understood set: three need product/verification decisions
(D1, D2, E1), one needs a doc correction (E2/§3a), two are F1 conformance-decision items
(OLIS06.02 Gap 2, OLIS06.03 wording), one is a low-risk code fragility to harden (OLIS04.10/§4a),
and OLIS06.01 is the conformance test itself. Nothing here is a surprise blocker — but §3a and
§4a are real findings that weren't on the board before this pass.
