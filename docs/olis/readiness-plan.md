# OLIS Readiness Plan

Ticket structure for closing the gaps identified in `requirements-analysis.md`
plus the previously reported bugs in `bugs.md`.

> **Pin this analysis to a commit.** Line numbers in `requirements-analysis.md`
> will drift; the tracking epic should reference the SHA the analysis was performed
> against (current branch: `bug/encoder-updates-and-fixes`).

---

## Track A — Bug fixes (customer-visible, higher priority)

These are previously reported user-blocking bugs, separate from spec compliance work.

### Status overview

| ID  | Summary | Status | Commit(s) |
|-----|---------|--------|-----------|
| A1  | OLIS source HTML/entities rendered as literal text in consult PDFs | ✅ Fixed — `HtmlTextCleaner` (Jsoup-based) applied to both OLIS + generic lab PDF render paths | `1647213359` |
| A2  | Forward-OLIS-result submit button missing | ✅ Fixed — Forward + Print + jQuery-UI imports | `d7c1ee228b` |
| A3  | `axis2-transport-local` dependency removed by mistake | ✅ Fixed | `dcd2b54b20` |
| A4  | NumberFormatException in audit log on empty `demographicNo` | ✅ Fixed | `9ac31fca2b` |
| A5  | `OLISStub` bytecode incompatible with axis2 1.8.2 | ✅ Fixed — regenerated SOAP stub against axis2 1.8.2; retired pre-compiled jar in favour of source in `src/main/java/ca/ssha/www/_2005/hial/`; hand-ported the 6 JAXB payload classes in `ca.ssha._2005.hial` | TBD |
| A7  | OLISUploadSimulationData2Action throws on non-multipart | ✅ Subsumed by A11 | `09f62661c9` |
| A8  | OLIS Audit Log viewer broken end-to-end | ✅ Fixed across two commits (script-order/URL + viewLog method via A9) | `438227f210`, `f07aea1870` |
| A9  | OLIS Results-management methods missing since 2019 | ✅ Fixed (with DAO multi-sort companion) | `f07aea1870`, `54e618401b` |
| A10 | Dead `/oscarLab/FileLabs.do` JSP references | ✅ Fixed | `32530e613a` |
| A11 | Simulator multipart consumption (CVE-2024-53677 followup) | ✅ Fixed | `09f62661c9` |
| A12 | `Driver.readResponseFromXML` NPE on unset schema property | ✅ Fixed | `dd13c8ac75` |
| A13 | OLIS manual-match popup never fires saveMatch ajax | ✅ Fixed | `5bd9193bf2` |
| A14 | OLISHL7Handler crashes on minimal HL7 fixtures | ✅ Fixed — defensive guards | `a94e66496d` |
| A15 | OLISLabPDFCreator NPE on null address (empty PDF download) | ✅ Fixed — null guard | `8944948848` |
| A16 | ProviderData.searchProvider ArrayIndex on no-comma query | ✅ Fixed — bounds check | `0614c4cf9a` |
| A17 | BC/ON/MDS lab Forward buttons broken (same root cause as A2) | ⏸️ Deferred — broken since 2021, no user reports; revisit if reported | `579968aa08` (reverted in `b93db70a35`) |
| A18 | Pre-inbox OLIS Print returns 500 (broken `&&` in PrintOLISLab2Action makes uuid preview path dead code) | ✅ Fixed — corrected condition + added null guard | TBD |
| A20 | InboxHub NPE on OLIS labs (`LabResultData.getDateObj()` returns null because `dateTime` isn't populated) | ✅ Fixed — defensive null guard at deref site; deeper fix (populate `dateTime` for OLIS) deferred | `224f4422b6` |
| A21 | Consult-print path doesn't route OLIS labs through `OLISLabPDFCreator` | ✅ Fixed — added request-less constructor + static `getPdfBytes` to `OLISLabPDFCreator`; instanceof-branched all 5 sites (incl. `LabManagerImpl.renderLab` which is the actual user-visible path) | TBD |
| A22 | InboxHub web list leaks OpenO-synthesized `<span>` markup in the "Requesting Client" column | ✅ Fixed — initial `HtmlTextCleaner.toPlainText` strips at 3 Hl7textResultsData sites; **D3 then dropped 1 (parser-fresh) and root-caused the markup synthesis**, so the 2 DB-fed strips are now defensive no-ops for legacy data | TBD |
| A23 | Lab display JSPs (`labDisplay.jsp` + `labDisplayOLIS.jsp`) leak `<span>` markup in provider/CC/attending/admitting fields | ✅ Fixed — initial `HtmlTextCleaner.toPlainText` strips at 6 render sites; **D3 then dropped all 6 and the `HtmlTextCleaner` imports** since `OLISHL7Handler` now emits clean text | TBD |
| A24 | OLIS Add-to-Inbox: MD5-detected duplicates classified as `errorIds` (bulk) and skip audit row (single) | ✅ Fixed — both branches now mirror the `OLISUtils.isDuplicate` reference path (audit row + `status="duplicate"` + success classification) | TBD |
| A25 | `labDisplay.jsp` 500s with `handler.getMsgType()` NPE when `hl7TextInfo` references a missing `hl7TextMessage` row (orphaned `lab_no`) | ✅ Fixed — null-handler guard renders a friendly "Lab data not available" page with HTTP 404, instead of crashing the JSP | TBD |
| A26 | `labDisplayOLIS.jsp` Home Address renders `<br />` as literal text in the patient demographics block | ✅ Fixed — JSP-local root-cause fix: `displayAddressFieldIfNotNullOrEmpty` replaced with `getAddressField` (returns plain value, no `<br />` synthesis); template now emits `<br/>` as literal HTML after each conditionally non-empty `Encode.forHtml` line. Applied at all 5 address-rendering call sites in `labDisplayOLIS.jsp` | TBD |
| A27 | `OLISHL7Handler.renderAsFT` / `.renderAsNM` ArrayIndexOutOfBounds on OBX-3 with fewer than 5 colon-separated parts → 500 mid-page | ✅ Fixed — both methods now length-check the `split(":")` array before indexing `[4]`; returns `false` (correct default) on malformed OBX-3 | TBD |
| A28 | `labDisplayOLIS.jsp` OBX test names render literal `<u>...</u>` (and `<s>...</s>` on strikeout) instead of underlined/struck-out | ✅ Fixed — JSP-local root-cause fix: new `buildObxDisplayHtml` JSP helper returns pre-encoded safe HTML with `<u>`/`<s>` as template-literal tags and `obxName`/`abnormalNature` properly HTML-encoded inside; 7 render sites now emit it raw with no `Encode.forHtml*` wrap. Drops the `pre`/`post` scriptlet variables entirely | TBD |
| A29 | `labDisplayOLIS.jsp` Report Comments render literal `&nbsp;` / `<span>` text instead of decoded clinical text | ✅ Fixed for OLIS path — `HtmlTextCleaner.toPlainText(...)` wrap applied at 6 comment-render sites in `labDisplayOLIS.jsp`; matches A1's existing PDF-side defensive pattern. Non-OLIS `labDisplay.jsp` deferred (no observation of the pattern there + active security-PR work in flight) | TBD |
| A30 | Noisy ERROR-level stack traces on every OLIS lab view from `getFullDoctorName` (PV1 absent → HL7Exception) and `getMappedOBX` (empty OBR in `obxSortMap` → AIOOBE). All caught; visible behavior correct; logs cluttered, hides real signal | ✅ Fixed — segment-existence pre-check in `getFullDoctorName` short-circuits PV1-absent case; `getMappedOBX` bounds-checks the inner map size before indexing | TBD |
| A31 | `OLISHL7Handler.formatDate` / `.formatTime` / `.formatDateTime` would NPE on null and throw `StringIndexOutOfBoundsException` on over-length input. Preemptive — found during the defensive-sweep extension of A27's investigation, no observed crash trace | ✅ Fixed — null/empty check at entry, length-clamp to format-string length, try/catch returning `""` on any internal failure. Mirrors `formatDateTime`'s existing null-guard pattern, extended to its two siblings | TBD |

**Net status (this dev environment):** all session-surfaced bugs (A1, A2, A3, A4, A5, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A18, A20, A21, A22, A23, A24, A25, A26, A27, A28, A29, A30, A31) closed; A17 intentionally deferred; A29's `labDisplay.jsp` non-OLIS sites filed as a follow-up to be coordinated with the in-flight security PR.
**Track A open:** none at the moment.

### A1: OLIS source HTML/entities rendered as literal text in consult PDFs
- **Status:** ✅ Fixed in commit `1647213359` — centralised `HtmlTextCleaner.toPlainText` helper (Jsoup-based) applied across both PDF render paths.
- **Source:** Trello mvJ6Ef1N (#177), `bugs.md`; reporter's screenshots `bug_olis_picture_1.png` + `olis_bug_1_picture_2.png` (consult PDF page 15 of 22, FIT colorectal-screening result with Specimen Comment block).
- **Repro:**
  1. Consult that includes an OLIS lab result with a Specimen Comment that uses `&nbsp;` for whitespace and/or `<span style="...">` for inline styling
  2. Generate the consult PDF (combined PDF export path)
  3. Before fix: Specimen Comment block rendered as a wall of `&nbsp;` entities and visible `<span>` tags instead of readable text
- **Visible failure modes in the screenshots:**
  1. **Provider name fields** show `DR. BRENT RYAN CRAWFORD <span style="margin-left:15px; font-size:8px; color:#333333;">MD 109753</span>` — the registration-number suffix is meant to render as small grey text but appears as literal markup. Cosmetic, doctor name still readable.
  2. **Specimen Comment block** shows multi-paragraph clinical text where every word-boundary is `&nbsp;` instead of a space. Looks like: `Action&nbsp;required&nbsp;for&nbsp;you:&nbsp;Complete&nbsp;a&nbsp;new&nbsp;FIT&nbsp;for&nbsp;your&nbsp;patient.` Doctor-facing clinical guidance, previously unreadable. **This was the clinical-safety concern** — a missed FIT recall in a colorectal cancer screening context.
- **Root cause (not a regression):** OLIS source data legitimately contains HTML markup (the upstream lab system uses `<span>` tags for styling and `&nbsp;` for layout). Specifically, `OLISHL7Handler.getFullDocName` (line 2689) synthesises `<span style="margin-left:15px; font-size:8px; color:#333333;">` markup itself so the web display can style the registration number small/grey — so this isn't even pure upstream data, it's OpenO-emitted markup that the PDF generators couldn't parse back out. Combined with `<br>`-only stripping (no entity decoding, no general tag stripping) in 27+ render call sites across both PDF generators, any unanticipated markup or entity leaked through as literal text. Git history shows OpenO has been emitting and partially-stripping these fields since the OLIS handler's inception; this is the upstream-data + self-emitted-markup shape colliding with rudimentary `replaceAll` cleanup, not behavioural drift from a recent security tightening (cf. open-osp/Open-O PR #225 which dealt with a different class of regression).
- **Fix shape (shipped):**
  - New utility `ca.openosp.openo.utility.HtmlTextCleaner.toPlainText(String)` — Jsoup-based, single entry point. Strips all HTML tags, decodes entities (`&nbsp;`, `&lt;`, `&amp;`, etc.), preserves `<br>` as newlines, normalises whitespace.
  - Applied across **`OLISLabPDFCreator`** (12 call sites) — Collectors/OBR/OBX/Report Comments, OBX results (NM/SN/TX/FT/TM/DT/TS), observation header, OBX name, plus a Jsoup-based rewrite of `getDoctorNamePhrase` replacing the brittle `indexOf("<")` parser while preserving subscript MD-number styling.
  - Applied across **`LabPDFCreator`** (15 call sites) — covers the consult-print path (`ConsultationAttachDocs2Action`, `EctConsultationFormRequestPrintAction22Action`, `EctConsultationFormRequestPrintPdf`, `EctConsultationFormRequest2Action`), all 14 `<br>` strip sites plus the four `getDocName()`/`getCCDocs()` raw doctor-name dumps.
  - Unit tests `src/test-modern/java/ca/openosp/openo/utility/HtmlTextCleanerUnitTest.java` pin 9 input shapes (span markup, `&nbsp;`, `<br>` → newline, null/empty safety, plain-text passthrough, mixed markup, common entities, arbitrary unknown tags).
- **Verification (live):**
  - Standalone OLIS Print (`PrintOLISLab.do`): `DR. BRENT RYAN CRAWFORD MD 109753` renders cleanly, MD number subscript-styled. No `<span>` or `&nbsp;` in 1943-byte PDF.
  - Consult Print Preview (`RequestConsultation.do` with `submission=And Print Preview`): same doctor name renders cleanly inline (no subscript, since `LabPDFCreator` doesn't have the OLIS-specific splitter — see A21 for follow-up). 3622-byte PDF, no markup leakage.
  - `mvn test -Dtest=HtmlTextCleanerUnitTest` → 9 passing in 0.115s.
- **`&nbsp;` repro caveat:** the rich fixture `docs/olis/sample-response-a1-rich.hl7` populated NTE segments to reproduce the Specimen Comment leakage symptom, but those NTEs never reached the render path because of a separate HAPI structure-walker issue (see A19). The fix's `&nbsp;` cleanup is verified via unit test rather than end-to-end PDF inspection; trust the Jsoup contract — when real OLIS data reaches the render path, the same code that strips `<span>` will strip `&nbsp;`.
- **Why upstream push-back alone wasn't sufficient:** OLIS data shape is set by individual labs (LifeLabs, Dynacare, etc.) and their HL7 export tooling. Even if we filed with OntarioMD, the upstream change cycle is long. Local sanitization is the only short-term fix that actually delivers readable clinical text. Plus the `<span>` markup is OpenO's own emission anyway — purely a local problem.
- **Labels:** `type: bug`, `priority: critical`, `area: olis`, `clinical-safety`

### A2: Restore Forward-OLIS-result submit button
- **Status:** ✅ Fixed in commit `d7c1ee228b` — migrated `labDisplayOLIS.jsp` Forward to the modern jQuery-UI dialog flow + fixed `printPDF()` path resolution as a bonus.
- **Source:** Trello 5YEZvWp1 (#190), `bugs.md`
- **Repro:** click Forward on an OLIS lab → forwarding popup window renders → no submit button visible. Reporter assumed it was an OLIS-only forwarding flow; turned out the same dead end affected BC, ON CML, MDS, and 3 multi-lab views (see A17).
- **Root cause:** the March 2021 commit `ecfcffd9e6` ("fix-buld-inbox-forwarding") commented out the `<input id="submitButton">` in `oscarMDS/SelectProvider.jsp:161` and simultaneously rewrote `ReportReassign2Action.java` to require JSON instead of form-encoded data. The new modal-dialog flow (`ForwardSelectedRows()` in `share/javascript/oscarMDSIndex.js`) was wired into `lab/CA/ALL/labDisplay.jsp` and the bulk-inbox path, but NOT into the OLIS-specific `labDisplayOLIS.jsp`. So OLIS labs still opened the legacy `popupStart('SelectProvider.jsp', 'providerselect')` popup window which had no working submit button + a backend that rejected its payload. Three independent failure modes stacked, each masking the next.
- **Fix:** make `labDisplayOLIS.jsp` use the same modern flow as `labDisplay.jsp`:
  1. Replace jQuery 1.3.2 import with jQuery 1.12.0 + jQuery-UI 1.12.1 + matching CSS theme files.
  2. Add `ctx` global + load `oscarMDSIndex.js`.
  3. Both Forward buttons now call `ForwardSelectedRows('<segmentID>:HL7', '', '')`.
  4. Delete the dead local `window.ForwardSelectedRows` shadow + the unused `<form name="reassignForm">` declaration.
  5. **Bonus fix:** `printPDF()` was setting form action to relative `"PrintOLISLab.do"` which resolved (via `<base href="/oscar/">`) to `/oscar/PrintOLISLab.do` → 404. Now uses absolute `<%=request.getContextPath()%>/lab/CA/ALL/PrintOLISLab.do`.
- **Verified:** Forward button click opens modern modal dialog with provider autocomplete, Forward List, Favorites, and working "Forward"/"Cancel" buttons. Print button downloads valid `%PDF-1.4` document instead of HTML error fallback.
- **Files:** `src/main/webapp/lab/CA/ALL/labDisplayOLIS.jsp`
- **Note:** sibling JSPs with the same root cause (BC, ON CML, MDS, plus 3 multi-lab list views) were intentionally NOT touched — see A17 for the rationale.
- **Labels:** `type: bug`, `priority: high`, `area: olis`

### A14: OLISHL7Handler crashes on minimal HL7 fixtures
- **Status:** ✅ Fixed in commit `a94e66496d` — defensive initialization + bounds checks across multiple getters.
- **Source:** local audit, surfaced while testing the A2 Forward fix — `labDisplayOLIS.jsp` 500'd repeatedly on the simulator-uploaded sample HL7 even though the file parsed fine on upload.
- **Repro:** upload `docs/olis/sample-response.hl7` via the OLIS simulator → add to inbox → open the lab → 500 page. The minimal sample doesn't populate every optional OBR/PID/specimen field that the production-tuned handler assumes is present.
- **Root cause:** `OLISHL7Handler` has several internal `HashMap`/`ArrayList` fields that are only initialized inside specific parse methods (eg. `parsePIDSegment()`). When parsing skips a path (eg. `obrCount == 0` short-circuits in `init()`), those fields stay null and downstream getters NPE. Plus several getters do `list.get(0)` without bounds-checking, and `getOrderDate()` does `substring(0, 8)` without length-checking.
- **Fix:**
  1. `init()` pre-initializes `patientIdentifiers`, `patientAddresses`, `patientHomeTelecom`, `patientWorkTelecom` at the top — so getters never NPE even if `parsePIDSegment()` doesn't run.
  2. `init()` now also calls `parsePIDSegment()` inside the `obrCount == 0` early-return branch — patient data was previously being silently dropped on observation-less messages.
  3. `getObrStatus(int)` and `getObrSpecimenSource(int)` bounds-check against null + size before `list.get(index)`.
  4. `getOrderDate()` length-checks OBR-27-4 before `substring(0, 8)` (OBR-27 is optional per HL7 v2.4).
- **Files:** `src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java`
- **Note:** these are all defensive guards; behaviour on well-formed production messages is unchanged. Affects only the OLIS lab display + PDF generator code paths.
- **Labels:** `type: bug`, `priority: medium`, `area: olis`

### A15: OLISLabPDFCreator NPE on null address (empty PDF download)
- **Status:** ✅ Fixed in commit `8944948848` — null-guard in `getAddressFieldIfNotNullOrEmpty()`.
- **Source:** local audit, surfaced while testing Print after A2 — Print button "succeeded" (browser saved a file) but the file was 24KB of HTML error page disguised as PDF.
- **Repro:** open an OLIS lab → click Print → check downloaded "PDF" with `head -c 8 file.pdf` → not `%PDF-1.4`, it's HTML whitespace + the error page JSP.
- **Root cause:** `OLISLabPDFCreator.getFullAddress(HashMap address)` calls `getAddressFieldIfNotNullOrEmpty(address, key)` which immediately does `address.get(key)` without checking whether `address` is null. When the PDF iterates patient/provider/specimen addresses and one is null, NPE → `PrintOLISLab2Action` returns "error" → Struts redirects to the lab display JSP → browser receives HTML where it expected a PDF stream.
- **Fix:** added `if (address == null) return "";` at the top of `getAddressFieldIfNotNullOrEmpty()`.
- **Verified:** Print now downloads valid `%PDF-1.4` document (1.9KB for our minimal fixture, would be richer for real OLIS messages).
- **Files:** `src/main/java/ca/openosp/openo/lab/ca/all/pageUtil/OLISLabPDFCreator.java`
- **Labels:** `type: bug`, `priority: medium`, `area: olis`

### A16: ProviderData.searchProvider ArrayIndex on no-comma query
- **Status:** ✅ Fixed in commit `0614c4cf9a` — defensive bounds check.
- **Source:** local audit, surfaced while testing the modern Forward dialog's provider autocomplete after A2.
- **Repro:** open OLIS lab → click Forward → type a single word with no comma in the provider autocomplete (eg. "smith") → 500 error response from `/oscar/provider/SearchProvider.do`.
- **Root cause:** `ProviderData.searchProvider(String)` splits the query by `","` and unconditionally reads `array[1]` for firstname. A query without a comma produces a 1-element array → `ArrayIndexOutOfBoundsException`. This is a pre-existing bug affecting all callers of provider autocomplete, not just OLIS — but only surfaced in this session because the OLIS Forward dialog was newly functional.
- **Fix:** guard the firstname assignment with `if (array.length > 1)`.
- **Verified:** access log shows `POST /oscar/provider/SearchProvider.do HTTP/1.1 200` for partial queries that previously returned 500.
- **Files:** `src/main/java/ca/openosp/openo/providers/data/ProviderData.java`
- **Note:** shared utility; affects every provider autocomplete in the EMR. Strictly defensive — failed search now returns empty results instead of throwing. No path that previously worked stops working.
- **Labels:** `type: bug`, `priority: medium`, `area: providers`, `defensive`

### A17: Sibling lab Forward buttons broken (same root cause as A2)
- **Status:** ⏸️ Deferred — sweep was implemented in `579968aa08` then reverted in `b93db70a35` per maintainer feedback. Revisit if/when reported.
- **Source:** local audit, surfaced while diagnosing A2.
- **Affected files** (all broken since `ecfcffd9e6`, Mar 22 2021):
  - `src/main/webapp/lab/CA/ON/CMLDisplay.jsp` (2 Forward buttons)
  - `src/main/webapp/lab/CA/BC/labDisplay.jsp` (2 Forward buttons)
  - `src/main/webapp/oscarMDS/SegmentDisplay.jsp` (2 Forward buttons)
  - `src/main/webapp/lab/DemographicLab.jsp` (1 Forward button — additionally lost its `flaggedLabs` checkbox infrastructure in commit `9962308fd1`, Feb 2023)
  - `src/main/webapp/documentManager/previewDocHL7Inbox.jsp` (1 Forward button — `flaggedLabs` checkbox infrastructure appears to have been vestigial since inception)
  - `src/main/webapp/oscarMDS/documentsInQueues.jsp` (1 Forward button — never had a `<form name="reassignForm">` declaration in git history at all)
- **Diagnosis:** same as A2 — `popupStart('SelectProvider.jsp')` opens a popup window with no submit button (since 2021 commenting-out), AND the backend `ReportReassign2Action` requires JSON (rejects the legacy form payload).
- **Why deferred:** the affected paths have been silently broken for ~5 years without user reports. Maintainer assessment: clinicians have adapted (probably routing via the inbox bulk-forward, the canonical HL7 `labDisplay.jsp`, or eChart-based workflows). "Fixing" the dark UI affordances risks surprising users who adapted. Conservative healthcare-EMR philosophy: don't touch unreported broken areas. Also: we don't have BC PathNet / ON CML / MDS test fixtures locally so we can't actually verify behavioural equivalence for those deployments.
- **Reactivation criteria:** any user-reported ticket against one of these flows. Fix pattern is mechanical and well-tested via A2 (same 4-step JSP migration). DemographicLab/preview/documentsInQueues additionally need their checkbox infrastructure restored before the popup→dialog swap helps.
- **Labels:** `type: bug`, `area: lab`, `status: deferred`, `breaks-since-2021`

### A13: OLIS manual-match popup never fires saveMatch ajax (Results.jsp ↔ PatientSearch.jsp param mismatch)
- **Status:** ✅ Fixed in commit `5bd9193bf2` — Results.jsp's `showMatch()` now passes the uuid as both `segmentID` (for the form's hidden fields) and `labNo` (for PatientSearch.jsp's onclick handler).
- **Source:** local audit (Track G smoke test, 2026-05-11), surfaced while testing the saveMatch endpoint after the A9 port landed.
- **Repro:** open OLIS Results.jsp with a result that has no matched demographic (name column shows `here.gif` icon) → click the icon → patient-search popup opens → search for a patient → click the demographic-number submit button → popup navigates to the patient's eChart, BUT the opener window's row is never updated to show a clickable patient link AND `OLISResults.demographicNo` is never set in the DB.
- **Root cause:** parameter name mismatch between the two JSPs. `Results.jsp:158` opens the popup with the OLIS uuid as the `segmentID` query param (not `labNo`). `PatientSearch.jsp:346` renders the demographic-number button's onclick using `request.getParameter("labNo")` — which is null because the parent never set it. The onclick effectively becomes `updateOpener('', demoNo)` or `updateOpener('null', demoNo)` depending on how `Encode.forJavaScript(null)` resolves; either way the downstream `updateLabDemoStatus2` ajax call fires with an empty/garbage uuid and the server's saveMatch endpoint rejects it with a UUID-format validation failure (or never fires at all — access log shows no `?method=saveMatch` hits during the flow).
- **Verified:** the saveMatch server endpoint works correctly (added in A9 port). The OLISResults table shows demographicNo=NULL even after a manual-match click — confirmed via catalina.out (no "Invalid UUID provided to saveMatch" entries logged) and localhost_access_log (no `?method=saveMatch` URI hits).
- **Fix options:**
  1. **Minimal**: change `PatientSearch.jsp:346` to fall back to `segmentID` when `labNo` is null, behind the existing `from=olis1` guard.
  2. **Cleaner**: change `Results.jsp:158`'s `showMatch()` to pass the uuid as `labNo` (matching what PatientSearch.jsp expects). Less invasive on the shared MDS popup.
  3. **Properest**: introduce an OLIS-specific `oscarOLIS/SearchPatient.jsp` per OLIS04.06 (the spec asks for an OLIS-specific match UI that shows original OLIS demographics next to EMR demographics — see D1 in this plan).
- **Files:** `src/main/webapp/olis/Results.jsp:158` (showMatch JS), `src/main/webapp/oscarMDS/PatientSearch.jsp:346` (onclick handler)
- **Note:** this is the JSP-side companion to D1 (OLIS-specific manual-match UI). Whether D1 ends up being "minimal port-saveMatch only" or "design an OLIS-specific match UI" determines which of the fix options above lands.
- **Labels:** `type: bug`, `priority: medium`, `area: olis`, `compliance`

### A12: Driver.readResponseFromXML NPEs when olis_response_schema is unset
- **Status:** ✅ Fixed in commit `dd13c8ac75` — wrapped the schema load in a null-check, with a note that `factory.newSchema()` was already a side-effect-only check (the result was never bound to the Unmarshaller, so skipping it on missing config has no functional impact).
- **Source:** local audit (Track G smoke test, 2026-05-11), surfaced while exercising the simulate-error path of the OLIS simulator after the A11 multipart fix landed.
- **Repro:** with `olis_response_schema` unset in `oscar_mcmaster.properties` (its default state — the line is commented out at `:1391`), upload any file via `/olis/Simulate.jsp` with the "Simulate Error" checkbox checked → `java.lang.NullPointerException` thrown at `Driver.java:240` inside `new File(OscarProperties.getInstance().getProperty("olis_response_schema"))`. `getProperty` returns `null` and `new File(null)` blows up.
- **Root cause:** `Driver.readResponseFromXML` constructs a `StreamSource` over the schema file unconditionally — no null-guard.
- **The sister method already has the right pattern:** `OLISUtils.java:82-83` does `if (OscarProperties.getInstance().getProperty("olis_response_schema") != null) { schemaFile = new StreamSource(new File(...)); }`. Driver.java just needs to mirror it.
- **Fix:** ~3-line defensive null-check at `Driver.java:240` mirroring `OLISUtils.java:82-83`. If the property is unset, skip schema validation (lose the XSD step but the simulate-error path still functions; production deploys would set the property properly anyway).
- **Files:** `src/main/java/ca/openosp/openo/olis1/Driver.java:240`
- **Note:** orthogonal to the A11 simulator-upload fix and the A9 port. Strictly a dev-environment / hardening item.
- **Labels:** `type: bug`, `priority: low`, `area: olis`

### A11: OLISUploadSimulationData2Action multipart consumption regression (CVE-2024-53677 followup)
- **Status:** ✅ Fixed in commit `09f62661c9` — migrated the action to `UploadedFilesAware` matching the 30 sibling upload actions. Subsumes A7.
- **Source:** local audit (Track G smoke test, 2026-05-11) — discovered when behavioural testing of the A9 port surfaced 0 OLISResults rows despite the action appearing to return successfully.
- **Repro:** with `olis_simulate=yes`, upload any HL7 file via `/olis/Simulate.jsp` → upload "succeeds" silently (no error, no result message in red) → any subsequent OLIS search returns empty results. Database has 0 rows in `OLISResults` but `OLISQueryLog` shows the queries ran. No errors logged.
- **Root cause:** the CVE-2024-53677 mitigation in `struts.xml` (commit comment at struts.xml:18-34) replaced `FileUploadInterceptor` with `ActionFileUploadInterceptor`, which consumes the multipart body in the Struts 2 interceptor stack before `action.execute()` runs. `OLISUploadSimulationData2Action` was missed in the sweep of 30 sibling upload actions — it still called the legacy `new FileUpload(...).parseRequest(request)`, which sees an empty stream post-interceptor and silently set `simulationData` to null. The session attribute `olisResponseContent` never got set, so the subsequent search saw nothing to render.
- **Fix applied on this branch:** migrated to the now-standard pattern used by all 30 sibling upload actions — implement `UploadedFilesAware`, add `withUploadedFiles(List<UploadedFile>)` calling `PathValidationUtils.toFile`, add `setSimulateError(String)` setter for the checkbox field (intentionally String — Struts 2's default `BooleanConverter` doesn't recognise plain HTML checkbox's `on` value as truthy, would silently coerce to false). Also subsumes the prior A7 isMultipartContent guard (the new null-file check handles it more cleanly) and replaced the verbose error message with "Please select a file to upload before submitting." since Simulate.jsp is reached through the OLIS UI and "direct access" framing was unhelpful.
- **Audit confirmed:** only 1 missed action (this one). The 30 sibling actions are correctly migrated; the 2 remaining `parseRequest` callsites are plain Servlets that bypass Struts 2's interceptor stack and aren't affected.
- **Labels:** `type: bug`, `priority: high`, `area: olis`, `regression`

### A10: Dead `/oscarLab/FileLabs.do` references in OLIS-adjacent JSPs
- **Status:** ✅ Fixed in commit `32530e613a` — both JSPs updated to `/oscarMDS/FileLabs.do` matching the live struts mapping.
- **Source:** local audit (Track G — JSP dead-link sweep, 2026-05-08)
- **Repro:** clicking the "File Lab" / lab-routing button on `lab/DemographicLab.jsp` (reachable from the OLIS user flow — this is the patient lab tab where OLIS results land) → POSTs to `/oscarLab/FileLabs.do` which has no struts mapping → 404. Same dead URL also referenced in `documentManager/previewDocHL7Inbox.jsp:146`.
- **Root cause:** the action was originally mapped at `oscarLab/FileLabs` since the very first commit. During the Struts 2 migration (`ed4b1f2167`), the mapping was moved to `oscarMDS/FileLabs` (`<action name="oscarMDS/FileLabs" class="ca.openosp.openo.lab.pageUtil.FileLabs2Action">`). The two JSPs above still point at the old `/oscarLab/` path.
- **Fix:** two-line search/replace — change both JSPs from `/oscarLab/FileLabs.do` to `/oscarMDS/FileLabs.do`.
- **Files:** `src/main/webapp/lab/DemographicLab.jsp:179`, `src/main/webapp/documentManager/previewDocHL7Inbox.jsp:146`
- **Note:** not strictly OLIS, but reachable from the OLIS user flow and surfaced by the OLIS audit. Worth bundling with the A9/A8 restoration PR or fixing as a tiny standalone PR.
- **Labels:** `type: bug`, `priority: medium`, `regression`

### A9: OLIS Results-management methods exist in git history but never settled into develop's working tree
- **Status:** ✅ Fixed in commit `f07aea1870` (with DAO multi-column-sort companion `54e618401b`). Smoke-tested via simulator: bulkProcess + viewLog + checkbox-add flow all working end-to-end. See `olis-a9-commit-message.md` for the full restoration commit message.
- **Source:** local audit (Track G smoke test, 2026-05-08), discovered while triaging A8 + C1
- **What's missing:** six action methods + an audit-log helper that together implement Save / Sign-off / Remove / Bulk-process / Match / Audit-Log-view for OLIS results. Their absence on the current 2Action is the root cause of A8 (audit log viewer broken), C1 (server-side Remove not wired), and a chunk of D1 (manual-match flow).
- **Where the working code lives:** commit `7aefabc840` (Mar 12, 2019, "initial commit of OLIS updates" by Marc Dumontier) on Bitbucket OSCAREMR via `OSCAREMR-6671 / pull request #457`. **Note:** that PR # is from the old Bitbucket repo before the GitHub migration — it won't resolve in openo-beta's PR list. The code is accessible only via git history at that SHA.
- **Why they're not on develop now (corrected from the original "Struts 1 → 2 migration regression" framing):** PR #457 *did* land on develop in Mar 2019 but went through ~30 merge battles in 2019 alone. Marc Dumontier's branches kept restoring the 585-line version (with methods); Jason Gallagher's PHR / OSCAREMR-6608 work and Colcamex Resources' `oscar-bc-2019-1` line had branched from a base predating PR #457, and their merges into develop kept overwriting the file with the 109-line shell version (just `execute()`). By **late 2019 the 109-line version won permanently** and develop has had it ever since. **The Struts 1 → 2 migration commit `f90870dc15` (Dec 2024) only deleted the long-empty 109-line shell** — it didn't drop active functionality. The original "regression caused by recent migration" framing was wrong; this is **long-missing functionality from a 2019 merge-graph mess**, not a recent regression.
- **What was lost (full inventory from `7aefabc840`):**

  | Symbol | Visibility | Lines | Purpose | Tickets affected | Depends on |
  |---|---|---|---|---|---|
  | `saveMatch` | public action | ~30 | Match unmatched OLIS result to a demographic | OLIS04.06 / D1 | — |
  | `bulkAddToInbox` | public action | ~50 | Bulk add multiple results to inbox | OLIS01.04 / 03.04 / C1 | private `addToInbox` |
  | `viewLog` | public action | ~80 | JSON endpoint for OLIS Audit Log DataTable | A8 | `ColumnInfo` inner class |
  | `remove` (public) | public action | ~20 | Single-result manual remove (thin wrapper) | OLIS01.04 / 03.04 / 06.03 / C1 | private `remove` |
  | `bulkRemove` | public action | ~40 | Bulk manual remove | C1 | private `remove` |
  | `bulkProcess` | public action | ~40 | Handles the "Process Changes" button — the exact endpoint the `bulkProcess()` JS in `Results.jsp` is calling | C1 (THIS is the missing server-side handler) | private `addToInbox`, private `remove` |
  | `unspecified` | protected action | ~75 | Struts 1 DispatchAction default-fallback when no `method` param given. **NOT a clean port** — its behaviour overlaps with the existing 2Action `execute()`, but `execute()` is missing three real behaviours from `unspecified`: (1) reads `OLISResults` from DAO and writes file from DB if missing, (2) sets `result.setStatus("added")` after success, (3) honours `addToMyInbox=false` parameter to skip current user's inbox. The merge needs care — keep openo-beta's UUID validation + `PathValidationUtils` security additions. | C1 / OLIS01.04 / OLIS03.04 | — |
  | `addToInbox` | private helper | ~85 | **Actual add-to-inbox implementation** called by `bulkProcess` and `bulkAddToInbox` — these public methods cannot work without it | C1 | — |
  | `remove` (overload) | private helper | ~18 | **Actual remove implementation** called by public `remove`, `bulkRemove`, and `bulkProcess` | C1 | — |
  | `logOLISRemoval` | helper | ~50 | Audit log row write for manual removals (matches OLIS06.03's hardcoded "System" rejection-type gap) | OLIS06.03 | — |
  | `ColumnInfo` | inner class | ~25 | DataTables column metadata (index + data field) parsed from `columns[0][data]` request params; required by `viewLog` for sort handling | A8 | — |

- **What survived in OpenO and does NOT need re-porting:**
  - `execute()` (basic add-to-inbox via `uuid` / `file` / `ack` params) — but **needs the three `unspecified` behaviours merged in** (DAO/file write, status update, `addToMyInbox` flag).
  - `getDemographicIdFromLab` (private helper) — already in `OLISAddToInbox2Action`.
  - `logOLISDuplicate` (system-dedup audit) — lives on `OLISResults2Action` with a refactored signature `(LoggedInInfo, Query, String, String)` instead of the original `(LoggedInInfo, Date, String, String, String, Integer, String, String)`. Bodies functionally equivalent — verified by direct comparison; openo-beta's version is actually slightly better (handles all 8 query types via `query.getQueryType().toString()` rather than only Z01/Z04 via if-checks).
- **Implication:** the OLIS-Results-management UI surface has been **partly non-functional since late 2019** — clicking Remove / Process Changes / Save Match / OLIS Log all hit endpoints that 404 or fall through to the wrong method. **No current branch (`develop`, `main`, `maintenance`, any `*-release-version`) has working code for these flows** — verified at branch tips. Not surfaced earlier because (a) simulator was also broken via A3, so nobody actually clicked through these flows in dev, and (b) the broken behaviour is at least 6 years old, predating the current maintainer team.
- **Reference copies of the source:** stashed at `archive/struts1-reference/OLISAddToInboxAction.7aefabc840.java` (585-line working version) and `OLISAddToInboxAction.predeletion.java` (110-line shell that was deleted). Also confirmed canonical on `bitbucket/stable` (Bitbucket OSCAREMR `oscar.git`, last commit 2026-03-10) — 585 lines, same 6 methods + 2 helpers — meaning the working code is still live on the OSCAREMR fork's mainline.
- **Suggested fix:** single restoration PR — port the public methods + private helpers + `ColumnInfo` from `7aefabc840` into `OLISAddToInbox2Action` (or a new dedicated `OLISLog2Action` for `viewLog`+`ColumnInfo` if you want better separation). The work has **two distinct shapes**:
  1. **Mostly-mechanical conversion** for `saveMatch`, `bulkAddToInbox`, `viewLog`, `remove` (public+private), `bulkRemove`, `bulkProcess`, `addToInbox` (private), `logOLISRemoval`, `ColumnInfo`. Struts 1 → 2 boilerplate: drop `(ActionMapping, ActionForm, request, response)` params, use `ServletActionContext.getRequest()`, return `String` result name, swap `mapping.findForward(...)` for return strings, add `SecurityInfoManager.hasPrivilege()` checks per the 2Action pattern, register methods in `struts.xml`.
  2. **Careful merge into existing `execute()`** for the three `unspecified` behaviours — DAO-driven file recreation, `setStatus("added")` on `OLISResults`, `addToMyInbox` flag handling. Must preserve openo-beta's UUID-format validation + `PathValidationUtils` security additions.
- **Effort:** materially smaller than original C1/A8 estimates because the implementation already exists in git history. Revised to **~1 full day** (was "half-day" in earlier draft) — the inner-class + private-helper dependencies and the `unspecified` merge add real work beyond pure mechanical conversion. Still much smaller than the original "design and implement from scratch" framing of ~2-4 days.
- **Labels:** `type: bug`, `priority: high`, `area: olis`, `compliance`

### A8: OLIS Audit Log viewer is broken end-to-end
- **Status:** ✅ Fixed across two commits — script-order swap + AJAX URL typo in commit `438227f210`; the missing `viewLog` method restored as part of the A9 port in commit `f07aea1870`.
- **Source:** local audit (Track G smoke test, 2026-05-08)
- **Repro:** click "OLIS Log" link from `Results.jsp` → DataTables JS error in browser console (`can't access property "defaults", f is undefined`); behind that, AJAX call to a wrong URL that 404s on the server.
- **Three stacked issues at `src/main/webapp/olis/log.jsp`:**
  1. **Script load order is wrong (lines 57-60):** `dataTables.bootstrap.min.js` loads before `jquery.dataTables.min.js`, but the bootstrap integration script depends on the main DataTables script being defined first. Result: DataTables never initializes, all calls fail. **Fixed on this branch** by swapping the order.
  2. **AJAX URL typo (line 66):** points at `/olis1/AddToInbox.do?method=viewLog` but the struts namespace is `/olis/` (verified `struts.xml` has `<action name="olis/AddToInbox" class="ca.openosp.openo.olis.OLISAddToInbox2Action">`). The `olis1` prefix was likely a relic from when the package was `olis1` Java-side. **Fixed on this branch.**
  3. **The action has no `viewLog` method:** `OLISAddToInbox2Action` only has `execute()` + a private helper. Even with the URL corrected, the request will 404 because Struts2 can't find a method named `viewLog` on the class. **NOT fixed.** The HRM equivalent (`/hospitalReportManager/log.jsp` → `hrm.do?method=viewLog`) does have a dedicated method that returns DataTables-compatible JSON of the audit log rows; OLIS needs an analogous method on `OLISAddToInbox2Action` (or a new dedicated `OLISLog2Action`) that reads from `OscarLog` rows where `action='OLIS'` and returns paginated JSON matching DataTables' server-side mode.
- **Compliance relevance:** this is the user-facing surface for OLIS06.02 ("Log All Messages Sent/Received"). If the log is being written but the viewer is broken, the audit data is hidden — the compliance gap in OLIS06.02 has actually been *worse* than the analysis suggested because reviewers couldn't even read the rows that do exist.
- **Suggested follow-up ticket scope:** **regression — restore the `viewLog` method from `7aefabc840:src/main/java/org/oscarehr/olis/OLISAddToInboxAction.java`** (lines 185-265 of that historical file). The original implementation already does exactly what's needed: reads DataTables `start`/`length`/`order` params, queries `OscarLogDao.findByAction("OLIS", ...)`, joins against `ProviderDao` and `DemographicDao` for human-readable names, returns DataTables-shaped JSON. See A9 for the full migration regression scope — `viewLog` is one of six action methods dropped during the Struts 1 → 2 migration in commit `f90870dc15` (Dec 2024). Bundle this fix with the rest of the A9 restoration; do not implement from scratch.
- **Labels:** `type: bug`, `priority: medium`, `area: olis`, `compliance`

### A7: OLISUploadSimulationData2Action throws on non-multipart requests
- **Status:** ✅ Subsumed by A11 (commit `09f62661c9`) — the new `UploadedFilesAware` pattern's null-file check handles the direct-GET case more cleanly than the original `isMultipartContent` guard. Initial guard committed as `ee6906c8fd`, then folded into the A11 rewrite.
- **Source:** local audit (Track G smoke test, 2026-05-08)
- **Repro:** any GET to `/olis/UploadSimulationData.do` (direct URL, browser refresh after POST, back/forward navigation) → `org.apache.commons.fileupload.FileUploadBase$InvalidContentTypeException` logged; user gets a confusing blank page or stale data.
- **Root cause:** the action calls `upload.parseRequest(request)` (line 48) without first checking `ServletFileUpload.isMultipartContent(request)`. The form on `/olis/Simulate.jsp` uses `enctype="multipart/form-data"` so a direct submit works, but URL refresh / direct navigation hits this path.
- **Fix applied on this branch:** added an `isMultipartContent` guard up front; non-multipart requests now return SUCCESS with a friendly message instead of throwing.
- **Labels:** `type: bug`, `priority: low`, `area: olis`

### A4: NumberFormatException in audit log when demographicNo is empty string
- **Status:** ✅ Fixed in commit `9ac31fca2b` — added trim + non-empty check before parse in `Driver.java`.
- **Source:** local audit (Track G smoke test, 2026-05-08)
- **Repro:** open `/olis/Search.jsp` and click search with no fields filled (no patient selected) → `java.lang.NumberFormatException: For input string: ""` at `Driver.java:151` while writing `OLISQueryLog`. The error is caught (audit log is skipped), but the failed audit row is a real compliance gap (OLIS06.02).
- **Root cause:** `query.getDemographicNo() != null ? Integer.parseInt(query.getDemographicNo()) : null` — the null check doesn't catch empty strings. Z01 patient queries with no patient selected emit `""`, not null.
- **Fix applied on this branch:** added `!demoNoStr.trim().isEmpty()` check + trim before parse — `Driver.java:151-152`.
- **Labels:** `type: bug`, `priority: medium`, `area: olis`, `compliance`

### A5: OLISStub bytecode incompatible with axis2 1.8.2 (production OLIS path broken)
- **Status:** ✅ Fixed — regenerated the SOAP stub against axis2 1.8.2 via an opt-in `regen-olis-stub` Maven profile, committed the generated source into `src/main/java`, and retired the pre-compiled jar.
- **Source:** local audit (Track G smoke test, 2026-05-08)
- **Original repro:** with `olis_simulate=no`, fire any query → `java.lang.NoSuchMethodError: 'org.apache.axis2.transport.TransportSender org.apache.axis2.description.TransportOutDescription.getSender()'` at `OLISStub.oLISRequest`.
- **Root cause:** the pre-compiled OLIS SOAP stub at `local_repo/ca/ssha/www/olis-service/20111111/olis-service-20111111.jar` was generated against axis2 1.5.4 in 2011 (literal date "20111111" baked into the artifact version). Runtime axis2 has since moved 1.5.4 → 1.8.0 (commit `b6f8d3de1a`, Jul 2025) → 1.8.2 (commit `ee02d90ffb`, Jan 2026). `TransportOutDescription.getSender()` was removed/renamed across those upgrades, so the stub's compiled bytecode referenced a method that no longer exists in the runtime axis2 jars.
- **Why it sat dormant for ~7 months:** the simulator path (`olis_simulate=yes`) skips `OLISStub` entirely (per A3's deferred-construction fix), and the production OLIS endpoint (`olis.ssha.ca`) needs real certs + outbound network that dev environments don't have. So nothing on the path actually instantiated the broken stub between the axis2 bumps and the local audit catching it.
- **Why 1.8.2 (not 2.0.0):** the comment in `pom.xml` is explicit — 2.0.0 is the Jakarta EE version of axis2, but OpenO is still on `javax.servlet` / Spring 5 / Tomcat 9. Bumping the OLIS stub alone to 2.0.0 would put one library on the Jakarta side of the namespace split while everything else stays on javax — guaranteed runtime conflict. The axis2 → 2.0.0 + stub regen bundle belongs in the eventual Struts 6 / Jakarta migration, not this fix.
- **Fix applied on this branch:**
  1. Added `regen-olis-stub` Maven profile (`pom.xml`) — invokes `axis2-wsdl2code-maven-plugin:1.8.2` against `archive/olis.wsdl`. Opt-in (`-Pregen-olis-stub`), matching the existing `jspc` pattern; doesn't run on normal builds. Comment block in the profile documents the invocation and procedure for the next axis2 bump.
  2. Generated source committed to `src/main/java/ca/ssha/www/_2005/hial/OLISStub.java` (~3665 LoC). Reviewable diff instead of a binary swap. Future axis2 API breaks fail at `mvn compile` instead of runtime.
  3. Hand-ported 6 JAXB POJOs that were bundled into the 2011 jar alongside the stub — `Response`, `Error`, `ArrayOfError`, `ArrayOfString`, `ObjectFactory`, `package-info` — into `src/main/java/ca/ssha/_2005/hial/`. These parse the OLIS response payload (the inner content of `HIALResponse.SignedResponse.SignedData`) and don't depend on axis2; they were originally generated by `xjc` against an external schema we don't have on file. They're stable POJOs so hand-port is appropriate; the package-info comment captures the rationale.
  4. Removed the `<dependency>` on `ca.ssha.www:olis-service:20111111` from `pom.xml` and deleted the `local_repo/ca/ssha/` tree (9 files).
  5. Regenerated `dependencies-lock.json` + `dependencies-lock-modern.json` via `make lock` to drop the now-removed jar's integrity entry.
  6. Dropped `OLISCallbackHandler` from the 2011 jar — it was dead code (zero references in the OpenO source tree; WS-Security signing is done manually in `Driver.signData/unsignData`, not via callbacks).
- **Verification:** with `olis_simulate=no` and a fired query, the stack now goes all the way through `OLISStub.oLISRequest` → `AxisEngine.send` → `HTTPSender.send` → Apache HttpClient socket layer, failing only at the network step (`Connect to olis.ssha.ca:443 ... timed out`) because dev containers have no outbound route to Ontario Health. Compare to pre-fix: it died at stub construction with `NoSuchMethodError` before any network attempt. Two unrelated WARN lines remain in the log (`StAXDialectDetector` dialect probe + `ServiceDeployer` deployer warning) — both are harmless axis2 noise that were always there but only visible now that the request progresses far enough to log them.
- **Follow-up not blocking A5:** the underlying gap is that **there's no CI smoke test against the SOAP path**, which is why this regression sat dormant for 7 months. A real fix for that gap needs test certs + a sandbox OLIS endpoint, neither of which are available in this devcontainer. File separately if pursued.
- **Labels:** `type: bug`, `priority: high`, `area: olis`, `compliance`

### A3: OLIS query paths broken by missing `axis2-transport-local` dependency
- **Status:** ✅ Fixed in commit `dcd2b54b20` — restored `axis2-transport-local:1.8.2` in `pom.xml` with a DO-NOT-REMOVE comment, plus reordered `Driver.submitOLISQuery` to defer `OLISStub` construction so simulator mode never instantiates the SOAP client. Follow-up: regenerate the dependency lock file (`make lock`) before opening the PR.
- **Source:** local audit (Track G smoke test, 2026-05-08)
- **Repro:** set `olis_simulate=yes`, upload sample via `/olis/Simulate.jsp`, run any query → `org.apache.axis2.deployment.DeploymentException: org.apache.axis2.transport.local.LocalTransportSender` thrown from `Driver.submitOLISQuery`
- **Root cause:** Axis2's bundled `axis2_default.xml` (inside `axis2-kernel-1.8.2.jar`) registers `org.apache.axis2.transport.local.LocalTransportSender` as a transport sender. Without the `axis2-transport-local` artifact on the classpath, every `OLISStub` construction fails during `AxisConfigBuilder.processTransportSenders`. **Both the simulator path and the production OLIS query path were affected** — production hadn't surfaced the bug because dev environments don't have the certs / outbound network to reach `olis.ssha.ca`.
- **Regression source:** `axis2-transport-local` was removed in commit `c9bdd03e06` (Kate Yang, Jan 7, 2025) titled *"removed unused dependencies and upgraded some dependencies in the pom.xml"*. The artifact has no direct Java imports (it's referenced at runtime via Axis2's deployment XML) so static "is this dep used?" analysis missed it. Silent regression for ~16 months.
- **Fix applied on this branch:**
  1. Restored `axis2-transport-local:1.8.2` in `pom.xml` next to `axis2-adb` with a DO-NOT-REMOVE comment block explaining why static analysis flags it as unused.
  2. Reordered `Driver.submitOLISQuery` to defer `OLISStub` construction to the non-simulate `else` branch, so simulator mode never instantiates the SOAP client at all.
- **Follow-up:** regenerate the dependency lock file (`make lock`) before the PR is opened.
- **Labels:** `type: bug`, `priority: high`, `area: olis`

### A18: Pre-inbox OLIS Print returns 500 (broken `&&` in PrintOLISLab2Action makes uuid preview path dead code)
- **Status:** ✅ Fixed — corrected condition + added null guard in `PrintOLISLab2Action`.
- **Source:** local audit, surfaced during A1 testing.
- **Repro:** open an OLIS lab via Results.jsp **before** adding to inbox → click Print → 500 (null pointer on `labDisplay.jsp` at line 218). The user had to click "Add to Inbox" first, then Print worked.
- **Root cause (corrected):** initial guess was "no `hl7TextMessage` row exists before add-to-inbox" — but `PrintOLISLab2Action` was actually designed to handle that case via an in-memory `OLISResults2Action.searchResultsMap` keyed by uuid for the preview path. **However that branch was dead code** because line 51 had `if (segmentId == null && segmentId.equals("0"))` — the `&&` short-circuits on `segmentId == null`, so `.equals("0")` is never called. The condition is logically impossible to satisfy. Every print fell through to the else branch (`Factory.getHandler(segmentId)`) which returned null for not-yet-saved labs → line 58's `handler.getPatientName()` NPE'd → caught → returned "error" → forwarded to `/lab/CA/ALL/labDisplay.jsp` (per `struts.xml:440`) which is the **wrong** JSP for OLIS, so it NPE'd at its own line 218 trying to read request data that wasn't there.
- **Fix shape (shipped):** in `PrintOLISLab2Action.java:51`:
  - Replaced broken `if (segmentId == null && segmentId.equals("0"))` with `if ("0".equals(segmentId))`. The uuid preview path is no longer dead code.
  - Added explicit null-guard on `handler` after lookup — if both the in-memory cache miss AND the Factory lookup return null, log + return "error" cleanly instead of NPE'ing on line 58.
  - Added a comment block explaining the historical bug so a future reader doesn't undo the `==` → `equals` swap.
- **Verification:** Playwright test — click Print on an OLIS lab before adding to inbox. Should now either: (a) get a clean PDF if the search cache still has the entry by uuid, OR (b) get the error result (forwarded to labDisplay.jsp, which still NPEs but that's a separate bug — see follow-up note below).
- **Follow-up (could file as A24 if pursued):** the error-result mapping in `struts.xml:440` forwards to `labDisplay.jsp` which is the **non-OLIS** lab display JSP. Even with the null guard, hitting this error path renders the wrong JSP. Should be either `/lab/CA/ALL/labDisplayOLIS.jsp` or a dedicated error JSP. Out of scope for A18's main fix.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `defensive`

### A20: InboxHub NPE on OLIS labs (`LabResultData.getDateObj()` returns null because `dateTime` isn't populated)
- **Status:** ✅ Fixed — defensive null guard at the deref site. Root-cause fix (populate `dateTime` for OLIS labs in `CommonLabResultData`) deferred as separate work.
- **Source:** local audit, surfaced during A1 testing — added OLIS lab to inbox via standard flow, navigated to InboxHub, 500 page.
- **Repro:** any OLIS lab in inbox → open InboxHub for that provider → `NullPointerException: Cannot invoke "java.util.Date.toInstant()" because the return value of "ca.openosp.openo.lab.ca.on.LabResultData.getDateObj()" is null` thrown at `LabDataController.filterOldLabVersions:576`.
- **Root cause (corrected):** initial analysis suspected the labType branch in `LabResultData.getDateObj()` didn't match — but **`LabResultData.HL7TEXT` is literally the string `"HL7"`** (line 67: `public static String HL7TEXT = "HL7";`), so the branch does match for OLIS labs (which set labType `"HL7"`). The actual issue: inside the matched branch, `getDateTime()` returns `this.dateTime`, and that field isn't populated when `LabResultData` is built for OLIS labs via `CommonLabResultData.populateLabResultsData`. `time` is null → method returns null at line 355 → `LabDataController:576` deref-NPEs. Worse: line 580 already has a `(dateA == null || dateB == null) ? 5 : ...` guard that was dead code because the NPE happened upstream.
- **Fix shape (shipped):** defensive null guard at `LabDataController.java:576-577`. Lifts the existing `dateA == null` check up to where the deref happens, making the previously-dead line-580 guard actually work. ~10 lines including a comment explaining why getDateObj can be null. Zero behavioural change for non-null cases.
- **Deeper fix (deferred — file as A22 if pursued):** trace why `dateTime` isn't populated for OLIS labs in `CommonLabResultData` query paths. Likely either the SQL doesn't select an OLIS-specific date column, or there's a missing setter call after constructing `LabResultData`. The pragmatic guard above prevents the NPE; the deeper fix would make accession-version filtering work correctly for OLIS labs (currently they all fall into the "5 months apart" default → no versions get filtered out as duplicates).
- **Labels:** `type: bug`, `priority: medium`, `area: lab`, `defensive`

### A21: Consult-print path doesn't route OLIS labs through `OLISLabPDFCreator`
- **Status:** ✅ Fixed across all 5 lab-PDF render sites in the consult flow, plus the `LabManagerImpl.renderLab` central path that powers the consult Print Preview.
- **Source:** discovered during A1 fix verification — the original reporter's screenshot is from the consult-print path, which we initially assumed went through `OLISLabPDFCreator`. It doesn't.
- **Repro:** attach OLIS lab to consult → Print Preview → PDF shows the OLIS lab via `LabPDFCreator` styling (doctor name inline, single Comments section, no version info) instead of OLIS-specific styling (subscript MD, separate Report/OBR/OBX comment blocks, version display).
- **Root cause:** `CaseManagementPrint.java:381` correctly branches on `handler instanceof OLISHL7Handler` and routes to `OLISLabPDFCreator` for the eChart/CPP print path. But the consult-request path uses `LabPDFCreator` unconditionally in **four** sites without the instanceof check:
  - `ConsultationAttachDocs2Action.java:236` (attach-popup preview)
  - `EctConsultationFormRequestPrintAction22Action.java:163` (main consult print)
  - `EctConsultationFormRequestPrintPdf.java:293` (PDF generator helper)
  - `EctConsultationFormRequest2Action.java:574` (fax send)
- **Fix shape (shipped):**
  - Added `OLISLabPDFCreator(OutputStream, String segmentId)` constructor for non-request rendering paths (saved-lab only; segmentID=0 search preview still requires the request-based constructor).
  - Added static `OLISLabPDFCreator.getPdfBytes(String segmentId)` mirroring `LabPDFCreator.getPdfBytes`.
  - Instanceof-branched `OLISHL7Handler` at all 5 sites:
    - `ConsultationAttachDocs2Action:236` (attach preview)
    - `EctConsultationFormRequestPrintAction22Action:163` (consult print)
    - `EctConsultationFormRequestPrintPdf:293` (PDF helper)
    - `EctConsultationFormRequest2Action:574` (fax/HL7 send via static helper)
    - `LabManagerImpl.renderLab:100` (the central path used by `DocumentAttachmentManagerImpl.renderConsultationFormWithAttachments`, which is the actual user-visible consult Print Preview entry point — was initially missed)
  - The OLIS branch skips `addEmbeddedDocuments` (a LabPDFCreator/PathL7-specific feature) and reads raw PDF bytes from the temp file.
- **Why low priority despite affecting the path the original reporter screenshotted:** the A1 fix already removes the literal `<span>`/`&nbsp;` markup leakage on this path — clinical text is now readable. The remaining gap is purely aesthetic (subscript MD styling, comment layout, version header). Worth doing but not urgent.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `cosmetic`

### A22: InboxHub web list leaks OpenO-synthesized `<span>` markup in "Requesting Client" column
- **Status:** ✅ Fixed — `HtmlTextCleaner.toPlainText` applied at 3 `Hl7textResultsData` population sites (lines 597, 726, 843).
- **Source:** discovered during A20 verification — the OLIS lab in inbox rendered correctly (no NPE) but its "Requesting Client" cell contained `DR JOHN SMITH <span style="margin-left:15px; font-size:8px; color:#333333;">DRLIC DR1234</span>` as literal text in the table.
- **Repro:**
  1. Any OLIS lab in any provider's inbox
  2. Navigate to InboxHub (`/web/inboxhub/Inboxhub.do`) → ensure type filter includes Labs
  3. Observe the "Requesting Client" column for the OLIS lab shows literal `<span style="...">DRLIC NNNN</span>` markup
- **Root cause:** the `<span>` markup is emitted by `OLISHL7Handler.getFullDocName` (`OLISHL7Handler.java:2689`) for the registration-number subscript-styling intent on the PDF render path. That string is stored on `LabResultData.requestingClient` via `Hl7textResultsData` (lines 597, 726, 843) and `InboxResponse.java:130`. The InboxHub JSP renders it at `InboxhubListMode.jsp:74` via `<c:out value="${labResult.requestingClient}" />` — `<c:out>` HTML-escapes by default, so the angle brackets get rendered as `&lt;span&gt;` and appear as literal text in the cell.
- **Why A1 fix didn't cover this:** A1 targeted the **PDF rendering paths** (`OLISLabPDFCreator` + `LabPDFCreator`) which call `HtmlTextCleaner.toPlainText` before writing to iText. InboxHub renders directly from `LabResultData` getters into HTML via JSP — completely separate code path that the A1 fix never touched.
- **Fix shape (three candidates, in order of preference):**
  1. **Sanitize at the population site** in `Hl7textResultsData` (3 lines: 597, 726, 843) — wrap `hl7.getRequestingProvider()` / `info.getRequestingProvider()` with `HtmlTextCleaner.toPlainText(...)`. Scoped to HL7-text / OLIS labs which is exactly where the markup originates. **Recommended** — narrow blast radius, predictable.
  2. **Sanitize at `InboxResponse.java:130`** — wrap `inboxItem.getRequestingClient()` with `HtmlTextCleaner.toPlainText`. Covers the InboxHub path specifically but doesn't help if other (non-inbox) consumers read the same field.
  3. **Sanitize at the JSP** — would need a custom EL function or scriptlet, more invasive for one cell. Avoid.
- **Why the `<span>` styling intent doesn't transfer to the inbox table:** the subscript styling only makes sense in a PDF where the OLIS handler's web-display assumption is intentionally rendered via iText subscriptFont. In an HTML table cell, the styling would either need to be hand-translated to actual `<span>` elements (with CSS allowed), OR stripped — and stripping is the consistent choice here since `<c:out>` already escapes the markup.
- **Companion concern:** the same getter feeds other UI surfaces (eg. `oscar/oscarMDS/SegmentDisplay.jsp` via different controllers). A spot-check of those surfaces after this fix would be worthwhile to confirm we don't break a place that intentionally relied on the raw markup (unlikely but worth one grep pass). **Update:** spot-check did surface a real leak in the lab-display JSPs — see A23.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `area: inbox`, `cosmetic`

### A23: Lab display JSPs leak `<span>` markup in provider/CC/attending/admitting fields
- **Status:** ✅ Fixed — `HtmlTextCleaner.toPlainText` wrapped before `Encode.forHtml` at 6 render sites.
- **Source:** discovered during A22 verification — InboxHub list rendered cleanly after A22, but clicking into a lab opened `labDisplayOLIS.jsp` which still showed `<span style="...">MD #####</span>` markup in the Ordering Provider and CC Client cells.
- **Repro:**
  1. Open any OLIS lab via `labDisplayOLIS.jsp` (or any HL7 lab via `labDisplay.jsp`)
  2. Observe Ordering Provider field shows literal `<span style="margin-left:15px; font-size:8px; color:#333333;">MD #####</span>` text
  3. Same for "cc: Client" field (and Attending/Admitting Provider on `labDisplayOLIS.jsp`)
- **Root cause:** same OpenO-synthesized markup from `OLISHL7Handler.getFullDocName:2689` — separate render path from A22 (InboxHub) and A1 (PDF). The JSPs call `Encode.forHtml(String.valueOf(handler.getDocName()))` etc. which HTML-escapes the markup so it renders as literal `&lt;span&gt;` text instead of being interpreted as HTML.
- **Fix shape (shipped):** wrap with `HtmlTextCleaner.toPlainText` before `Encode.forHtml` at 6 sites. Strip first, then escape — order matters so any remaining special chars in the cleaned text are still HTML-safe.
  - `labDisplayOLIS.jsp`: lines 1177 (`getDocName`), 1256 (`getAttendingProviderName`), 1273 (`getAdmittingProviderName`), 1366 (`getCCDocs`)
  - `labDisplay.jsp`: lines 1696 (`getDocName`), 1708 (`getCCDocs`)
- **Companion concern:** `labDisplay.jsp:1627` renders `handler.getClientRef()` — different getter, not the `<span>`-emitting one based on a quick grep. Left unchanged. If real CML/MDS data shows similar leakage in that cell, expand the fix there too.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `area: lab`, `cosmetic`

### A27: `OLISHL7Handler.renderAsFT` / `.renderAsNM` 500 on OBX-3 with <5 colon-separated parts
- **Status:** ✅ Fixed — both `renderAsFT` and `renderAsNM` now length-check the `split(":")` array before indexing `[4]`; return `false` (the existing "not FT/NM" default) when the OBX-3 component-2 field has fewer than 5 colon-separated parts.
- **Source:** observed by the user when clicking an unforwarded OLIS lab from InboxHub for patient TESTPATIENT/FITCASE (`segmentID=176`). The page rendered up through the demographics and the OBR comment ("...is not suitable for analysis"), then the response was committed mid-stream and the `errorPage` directive at `labDisplayOLIS.jsp:17` forwarded to `errorpage.jsp` — which got **inlined into the response** because of the partial commit. Symptom: the page appeared to render normally but the body ended with an "OSCAR Error: 500" page. **No JasperException in the Tomcat log** — the `errorPage` directive swallows it. Required removing the directive to surface the real stack.
- **Repro:**
  1. InboxHub → click any unforwarded OLIS lab whose OBX-3 component-2 doesn't follow the OLIS-expected `code:LOINC:name:discipline:NAR|ORD|QN|...` 5-part shape (e.g. simpler fixture data, or any real lab missing the trailing nomenclature category)
  2. Browser opens `lab/CA/ALL/labDisplay.jsp?...&segmentID=<id>` → page renders patient demographics, then a 500 error page gets inlined where the test result rows should be
- **Root cause:**
  ```java
  public boolean renderAsFT(int i, int j) {
      String obxIdent = getOBXField(i, j, 3, 0, 2).split(":")[4];  // ← unguarded [4]
      return obxIdent != null && obxIdent.toUpperCase().startsWith("NAR");
  }
  ```
  `.split(":")[4]` assumes ≥5 parts. For our fixture's OBX-3-2 with only 1 part, `[4]` throws `ArrayIndexOutOfBoundsException: Index 4 out of bounds for length 1`. The exception propagates through the JSP and is swallowed silently by `errorPage` — no log, no useful diagnostic surface. `renderAsNM` (line 1809) has the identical pattern.
- **Fix shape (shipped):**
  ```java
  public boolean renderAsFT(int i, int j) {
      String[] parts = getOBXField(i, j, 3, 0, 2).split(":");
      if (parts.length < 5) return false;
      String obxIdent = parts[4];
      return obxIdent != null && obxIdent.toUpperCase().startsWith("NAR");
  }
  ```
  Same pattern in `renderAsNM`. `false` is a correct default — both methods are "is this OBX a Formatted Text / Numeric special case" predicates; absent the trailing category, treat as not-FT/not-NM.
- **Verification:** Playwright — segmentID=176 (TESTPATIENT/FITCASE, OBX-3-2 with single-part code) now renders fully: page size grew from 24271 bytes (truncated mid-render with inlined errorpage) to 29999 bytes (complete render), test result rows visible ("Fecal Immunochemical Test Result / Not detected / N / Final"), "END OF REPORT" footer present, no `Looks like something went wrong` text.
- **Build note:** initial deploys via plain `make install` left **stale `.class` files** in `target/classes/` so the fix wasn't actually reaching the JVM — required `make clean && make install` to recompile fresh. Per the standing memory rule. (Verified by `javap -c` showing the exception table on `getOBXField` only after the clean rebuild.)
- **Diagnostic technique noted for future:** the `errorPage` directive at `labDisplayOLIS.jsp:17` and at `labDisplay.jsp` swallows the actual exception silently — no JasperException reaches catalina.out. When investigating "500 with empty Tomcat log" on these JSPs, temporarily comment out the `errorPage` directive to surface the real stack via the global error handler. Restore after diagnosis.
- **Defensive-guard family:** A14 (minimal HL7 crashes), A15 (PDF null address), A18 (PrintOLISLab dead-code), A25 (orphaned hl7TextInfo), A27. Each is a "real data hits unguarded assumption" case — the OLIS handler historically assumes well-formed OntarioMD-conformant input, which our test fixtures don't always provide.
- **Bundled adjacent fix (same commit `9c2eef8455`):** `getOBXField` and `getOBXEDField` had `obrGroups.get(i)` **outside** their existing try/catch. If `i` is out of bounds for `obrGroups`, the IndexOutOfBoundsException escaped to the caller. Surfaced during A27 investigation — symptomatically masked by A27's own error path but a real fragility on its own. Both methods now wrap the `obrGroups.get(i)` lookup inside the existing try (exception range covers offsets 0-42 in the compiled bytecode), so any OBR-index out-of-bounds returns `""` cleanly instead of propagating.
- **Labels:** `type: bug`, `priority: medium` (user-blocking — labs unviewable), `area: olis`, `area: lab`, `defensive-guard`

### A26: `labDisplayOLIS.jsp` Home Address renders `<br />` markup as literal text
- **Status:** ✅ Fixed — JSP-local root-cause fix mirroring D3's principle. Helper now stays in the data layer; template owns presentation.
- **Source:** discovered while verifying A25's working-lab regression check (Playwright scan of segmentID=177's rendered page). Home Address cell text read literally: `123 MAIN ST<br /> M5H 2N2<br /> TORONTO, ON CAN<br />`.
- **Repro (pre-fix):**
  1. Open any OLIS lab via `labDisplayOLIS.jsp` whose patient has a populated Home Address
  2. Observe the "Home Address" cell in the demographics table — line breaks show as the literal text `<br />` instead of actual line breaks
- **Root cause:** `labDisplayOLIS.jsp:823-840` declared a `displayAddressFieldIfNotNullOrEmpty` helper that appended `<br />` to the field value (line 838). Call sites wrapped the helper return with `Encode.forHtml(String.valueOf(...))` — which HTML-escaped the trailing `<br />` to `&lt;br /&gt;`, defeating the intended line-break styling. `OLISHL7Handler.getPatientAddresses()` itself always returned clean per-field strings; the markup synthesis was purely the JSP-local helper.
- **Fix shape (shipped):** replaced the helper with `getAddressField(HashMap, String)` returning the raw value (or `""`). Each call site now captures the 5-6 address parts into locals and emits them as `<%=Encode.forHtml(value)%><%=value.isEmpty() ? "" : "<br/>"%>` — value is HTML-encoded data, `<br/>` is template-literal HTML the browser interprets. Same `Encode.forHtml(city)`/`Encode.forHtml(province)` conditional-comma logic preserved.
- **Updated 5 address-rendering blocks in `labDisplayOLIS.jsp`** — patient Home Address (around line 840-865), Ordering Facility address (around 1145-1165), Ordering Provider address (around 1180-1200), Performing Facility address (around 1300-1320), Reporting Facility address (around 1340-1360), and per-OBR Performing Facility address (around 1690-1715).
- **Verification (Playwright, 2026-05-15):** lab 176 Home Address now renders `45 SAMPLE RD / M5H 2N2 / TORONTO, ON / CAN` with real line breaks; visible text contains no `<br>` markup. HTML inspection confirms `<br/>` is present as a real tag, not as escaped text.
- **Same root-cause family as A28** (JSP-local markup synthesis). Both fixed in the same cleanup pass after D3.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `area: lab`, `cosmetic`

### A31: Preemptive defensive guards on `formatDate` / `formatTime` / `formatDateTime` (no observed crash)
- **Status:** ✅ Fixed — null/empty checks + length-clamping + try/catch returning `""`, applied uniformly to all three date-formatting helpers.
- **Source:** discovered during the defensive-sweep extension of A27's investigation. After fixing the OBX-3 `split(":")[4]` crash, did a broader grep of `OLISHL7Handler` for similar unguarded assumptions: `.charAt(0)`, `.substring(0, N)` with fixed N, `.split(...)[N]`, etc. Most call sites turned out to be already-guarded (either inside `try/catch` or behind explicit pre-checks like `key.indexOf(":") > 0`), but the three date-formatting helpers were genuinely unprotected.
- **Repro (preemptive — no observed live crash):**
  - `formatDate(null)` → NPE on `plain.length()`
  - `formatDate("")` → throws `StringIndexOutOfBoundsException` on `dateFormat.charAt(dateFormat.length() - 1)` because `dateFormat` becomes empty
  - `formatDate("20240115083000")` (14 chars — caller passes a TS instead of DT) → throws on `dateFormat.substring(0, plain.length())` because `plain.length()` > 8
  - Same shapes for `formatTime` (6-char format string)
  - `formatDateTime` was already partly guarded (had a null/empty check at top) but `plain.substring(14, 19)` could throw if `plain.length()` is 15-18 (partial timezone)
- **Reachability:** `getOBXDTResult` → `formatDate`, `getOBXTMResult` → `formatTime`, `getOBXTSResult` and timestamp accessors → `formatDateTime`. If any OBX returns an empty or odd-length date/time string, the prior code would 500. Not observed live with current fixtures, but real OLIS data has variation we don't control.
- **Fix shape (shipped):** for each of the three helpers:
  - Null/empty check at entry (already present on `formatDateTime`; added to siblings)
  - Length-clamp: if `plain.length() > <format-string-length>`, truncate to the format-string length
  - For `formatDateTime`'s offset-handling, treat length 15-18 (partial timezone) as "drop offset, keep 14-char body"
  - Wrap entire body in `try/catch` returning `""` for any other parse failure
- **Why preemptive matters here:** the surrounding A14/A15/A18/A25/A27/A30 family is the same pattern — real OLIS data has formatting variation we can't control, and the OLIS handler historically assumed well-formed input. A pre-conformance hardening pass on the obvious gaps cheapens later F1 surprise debugging.
- **Bundled adjacent insight (recorded for future sweeps):** the broader sweep audited 8+ candidate sites in `OLISHL7Handler`. The other patterns either were already guarded or operate on data the handler controls (HAPI segment names are always ≥3 chars, etc.) — only the three date helpers were genuinely under-guarded. Full audit details in the A31 commit body.
- **Labels:** `type: refactor`, `priority: low`, `area: olis`, `defensive-guard`, `preemptive`

### A30: Noisy ERROR-level stack traces on every OLIS lab view (no functional impact)
- **Status:** ✅ Fixed — segment-existence pre-check in `getFullDoctorName` + bounds-check in `getMappedOBX`. Same defensive-guard family as A14/A15/A18/A25/A27.
- **Source:** observed by the user when opening lab 177 — Tomcat catalina.out logged 3-5 ERROR-level stack traces per lab view, all caught silently and not visible to the user, but burying real diagnostic signal.
- **Two distinct triggers:**
  1. **`getFullDoctorName` HL7Exception** — `getAttendingProviderName` and `getAdmittingProviderName` always call `getFullDocName("/.PV1-7-")` and `getFullDocName("/.PV1-17-")` respectively. Most OLIS labs **don't carry PV1 segments** (PV1 is the optional Patient Visit segment, typically only present on inpatient labs). HAPI's terser tries to navigate to `/.PV1-7-...` and throws `HL7Exception: End of message reached while iterating without loop`. Caught at the public-getter level + logged as ERROR. Stack trace contains the full Tomcat/Struts filter chain (~100 lines) per occurrence.
  2. **`getMappedOBX` ArrayIndexOutOfBoundsException** — `obxSortMap.get(obr).keySet().toArray(...)` returns a 0-length array when the inner map for an OBR is empty (no OBXs collected). Indexing `keys[obx]` throws AIOOBE. Caught + logged as ERROR. Fires once per empty-OBR per render.
- **Repro:** open any OLIS lab without PV1 segments (most of them) — catalina.out gets ~3-5 ERROR-level stack traces per view. Lab 177 was the user's observation. Real-world OLIS data hits this consistently.
- **Why it matters despite "no visible bug":** when tailing logs during conformance testing, the noise hides real errors. Hard to spot a new issue when every page view emits 100+ lines of caught-and-logged stack trace.
- **Fix shape (shipped):**
  - **Issue 1 — `getFullDoctorName`:** new private `segmentExists(String docSeg)` helper that extracts the 3-character segment name from the terser path (e.g. `/.PV1-7-` → `PV1`) and checks `terser.getFinder().getRoot().getAll(name).length > 0`. `getFullDoctorName` short-circuits to an empty `DoctorName` when the segment doesn't exist — no HL7Exception thrown, no log entry.
  - **Issue 2 — `getMappedOBX`:** bounds-check the inner map before indexing. If `obxSortMap.get(obr)` is null or `obx` is out of bounds, return the original `obr` (same defensive return as the existing exception fallback) without provoking the AIOOBE.
- **Behavior preservation:** both return values match the pre-fix exception-fallback outputs (empty `DoctorName` / original `obr`). Real corruption that goes beyond "segment doesn't exist" / "empty inner map" still gets caught + logged as ERROR — only the **expected absence** noise is suppressed.
- **Verification (Playwright + log diff, 2026-05-15):** captured catalina.out line count before navigating to lab 177, then re-tailed after — **0 ERROR-level entries** from `OLISHL7Handler` for the render (previously 3-5). Visible rendering identical: Ordering Provider, OBX result rows, "END OF REPORT" footer all present. No error-page inline.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `area: logs`, `defensive-guard`

### A29: `labDisplayOLIS.jsp` Report Comments render literal `&nbsp;` / `<span>` text instead of decoded clinical text — incomplete A1
- **Status:** ✅ Fixed for the OLIS path — `HtmlTextCleaner.toPlainText(...)` wrap applied at 6 comment-render sites in `labDisplayOLIS.jsp`. Non-OLIS `labDisplay.jsp` deferred (see scope discussion below).
- **Source:** observed by the user during A26/A28 verification — lab 176's Report Comments cell rendered as `Action&nbsp;required&nbsp;for&nbsp;you:&nbsp;Complete&nbsp;a&nbsp;new&nbsp;FIT&nbsp;for&nbsp;your&nbsp;patient.&nbsp;The&nbsp;previous&nbsp;sample&nbsp;was&nbsp;received&nbsp;more&nbsp;than&nbsp;14&nbsp;days&nbsp;after&nbsp;collection&nbsp;and&nbsp;is&nbsp;not&nbsp;suitable&nbsp;for&nbsp;analysis.` That is the **exact text** the original A1 reporter screenshot showed in the FIT colorectal-cancer-screening clinical-safety case (per A1 entry line 59). Lab 176 is the A1-rich fixture — confirmed via MSH-10 = `OLISMSGA1RICH001`.
- **Wire-data investigation (DB inspection, 2026-05-15):** the raw HL7 stored in `hl7TextMessage.message` for lab 176 contains an NTE segment with HL7-conformant escape encoding:
  ```
  NTE|1||Action\T\nbsp;required\T\nbsp;for\T\nbsp;you:\T\nbsp;Complete\T\nbsp;a\T\nbsp;new\T\nbsp;FIT...
  ```
  `\T\` is the HL7 escape sequence for the `&` character (per HL7 v2 escape rules — `&` is the sub-component separator declared in MSH-2, so any literal `&` in data must be escape-encoded). HAPI's parser decodes `\T\` → `&` per spec, yielding `&nbsp;` in `getOBRComment()`. **The DB is not muddied; the upstream encoding is conformant.**
- **Render-path leak:** JSP comment-render sites call `Encode.forHtml(handler.getOBRComment(...))` etc. `Encode.forHtml` escapes the `&` to `&amp;`, turning `&nbsp;` into `&amp;nbsp;` which the browser displays as literal text. The PDF path doesn't have this problem — A1 wrapped its render calls with `HtmlTextCleaner.toPlainText` (Jsoup decodes `&nbsp;` to U+00A0 non-breaking space). The JSP path was out of A1's scope.
- **Render sites needing the wrap (6 in `labDisplayOLIS.jsp`):**
  - Line 1463 — `report.getComment()` (audit/admin context)
  - Line 1524 — `handler.getReportComment(i)` (repeated report comments)
  - Line 1784 — `handler.formatString(collectorsComment)` (Specimen Comment block — the A1 reporter's primary symptom site)
  - Line 1785 — `handler.getCollectorsCommentSourceOrganization(obr)` (source organization styled span)
  - Line 1817 — `obrComment` (OBR-level NTE comments)
  - Line 2137 — `handler.getOBXComment(obr, obx, l)` (per-OBX comments)
- **Why we can't easily do a D3-style root-cause fix at the handler:** the `&nbsp;`/`<span>` markup is in the genuine upstream wire data, not synthesized by OpenO. A1 chose render-layer translation deliberately because the PDF and HTML render contexts want different output shapes for the same source (PDF wants plain text; HTML wants the entities decoded to actual non-breaking spaces). Handler-level entity decode would help one path and hurt the other.
- **Fix shape (matches A1's existing PDF pattern):** wrap each of the 6 OLIS sites with `HtmlTextCleaner.toPlainText(...)` before `Encode.forHtml(...)`. Restore the `HtmlTextCleaner` import to `labDisplayOLIS.jsp` (it was dropped during D3's thorough cleanup). ~6 small edits.
- **Why this is clinically important** (not cosmetic — per the A1 entry framing): the original A1 reporter's complaint was a missed FIT recall for colorectal cancer screening. Doctor-facing clinical guidance like "Action required for you: Complete a new FIT for your patient" became unreadable when rendered with literal `&nbsp;` between every word. A1's PDF fix made the PDF readable; the web JSP path remained broken for the same reason — until A29.
- **Verification (Playwright, 2026-05-15):** lab 176 Report Comments cell now renders the FIT guidance as readable clinical text — `"Action required for you: Complete a new FIT for your patient. The previous sample was received more than 14 days after collection and is not suitable for analysis."` — with U+00A0 non-breaking spaces between words (Jsoup decoded `&nbsp;`; `Encode.forHtml` left U+00A0 alone). No `&nbsp;` in visible body text.
- **Empirical-evidence caveat (recorded for honesty):** the only known-real instance of this pattern is the **original A1 reporter's case** (one OLIS lab, one screenshot). The HL7 fixture used in the local dev environment (`lab 176`, MSH-10 `OLISMSGA1RICH001`) is our own replication of that reporter's data — not OntarioMD-official test data. We don't have empirical evidence that the pattern is widespread among other OLIS labs. The fix is therefore **defensive**, matching A1's same defensive choice for the PDF path. If the pattern turns out to be lab-specific, the wrap is harmless on labs that send plain text.
- **Scope choice: `labDisplayOLIS.jsp` only this session.** The non-OLIS render path (`labDisplay.jsp` — handles CML, BC PathNet, MDS, Excelleris, etc.) likely has the same `Encode.forHtml(...)` pattern around comment-render sites and would theoretically be affected if those upstream lab systems also encode formatted text with HTML entities. A1's PDF fix wrapped both OLIS + non-OLIS PDF paths defensively. Mirroring that on the web JSPs would be defensible, **but**: (a) we have no empirical observation of the pattern in non-OLIS wire data, (b) there's active in-flight security-PR work on `labDisplay.jsp` and overlapping changes risks merge conflicts and unwanted security-review surface. Deferred as a follow-up — directed scan of actual non-OLIS wire data + coordinated with the security-PR cycle.
- **Labels:** `type: bug`, `priority: medium` (clinical-safety inheritance from A1), `area: olis`, `area: lab`, `clinical-safety`

### A28: `labDisplayOLIS.jsp` OBX test names show literal `<u>...</u>` markup instead of underline
- **Status:** ✅ Fixed — JSP-local root-cause fix mirroring D3's principle. Pre-encoded safe HTML returned by a new helper; render sites emit raw.
- **Source:** observed by the user during D3 verification — test names in lab 176's results table rendered as `<u>Fecal Immunochemical Test Result</u>` instead of underlined. Confirmed via Playwright `innerText` inspection (tags appeared in visible text, meaning they were HTML-escaped at render rather than interpreted as HTML).
- **Repro (pre-fix):**
  1. Open any OLIS lab via `labDisplayOLIS.jsp` that has at least one OBX result
  2. Observe the test-name column — names appeared with literal `<u>...</u>` wrapping. On strikeout/deleted results, `<s>` was also added literally.
- **Root cause:** `labDisplayOLIS.jsp:1830-1841` built `obxDisplayName = pre + obxName + post + abnormalNature` where `pre = "<u>"` / `post = "</u>"` (plus `<s>` / `</s>` if strikeout) and `abnormalNature` got a `<span style="...">` wrap synthesized at line 1858. The composed string was then rendered via `Encode.forHtmlAttribute(obxDisplayName)` at 7 sites — HTML-escaping the styling tags so they became `&lt;u&gt;...&lt;/u&gt;` literal text. Same family as A22/A23/A26: JSP synthesizes presentation markup, then runs the result through an HTML encoder which escapes its own creation. Distinct from D3 only in that the markup source was the JSP itself, not a parser-layer Java method consumed by multiple renderers.
- **Fix shape (shipped):**
  - New JSP scriptlet helper `buildObxDisplayHtml(boolean strikeout, String obxName, String abnormalNatureRaw)` returns a pre-encoded safe HTML fragment. The `<u>`/`<s>` styling tags are **template literals** built directly into the string; the `obxName` and `abnormalNatureRaw` values are HTML-encoded inside the helper via `org.owasp.encoder.Encode.forHtml(...)`. Browser receives correct HTML with real tags + encoded data.
  - The scriptlet block at lines 1850-1861 now collapses to:
    ```jsp
    String abnormalNatureRaw = handler.getNatureOfAbnormalTest(obr, obx);
    String obxDisplayHtml = buildObxDisplayHtml(strikeout, obxName, abnormalNatureRaw);
    ```
  - The 6 attribute-context render sites (`Encode.forHtmlAttribute(String.valueOf(obxDisplayName))`) and the 1 HTML-context site (`Encode.forHtml(String.valueOf(obxDisplayName))`) all reduce to `<%=obxDisplayHtml%>` — no Encode wrap, because the helper already returns safe HTML.
  - Side benefit: the original code used `Encode.forHtmlAttribute` in HTML body context — wrong encoder for the position. The fix sidesteps that bug too.
- **Verification (Playwright, 2026-05-15):** lab 176 test name now renders with real `<u>Fecal Immunochemical Test Result</u>` HTML; `innerText` shows just `Fecal Immunochemical Test Result` (underlined visually); no `<u>` leak in visible body text.
- **Build gotcha encountered + recorded:** a Java `//` comment in the new scriptlet block originally contained `<%= obxDisplayHtml %>` as illustrative prose — the JSP parser tokenizes `<%=` regardless of comment context, so the comment got compiled as a scriptlet expression and broke the generated `_jspService` method. **Lesson:** never write `<%`, `<%=`, `<%!`, `<%@` inside JSP scriptlet Java comments; describe with prose instead.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `area: lab`, `cosmetic`

### A25: `labDisplay.jsp` 500s on orphaned `hl7TextInfo` row (missing `hl7TextMessage`)
- **Status:** ✅ Fixed — null-handler guard at `labDisplay.jsp:215-238` renders a friendly "Lab data not available" page (HTTP 404) instead of NPE'ing on the next deref of a null `MessageHandler`.
- **Source:** observed by the user when clicking an unforwarded OLIS lab from InboxHub — the inbox link constructed `?segmentID=173`, the JSP 500'd. Reproduced via Playwright (2026-05-15); root-caused via direct DB inspection.
- **Repro:**
  1. InboxHub → click any unforwarded OLIS lab whose `hl7TextInfo.lab_no` lacks a matching `hl7TextMessage.lab_id`
  2. Browser opens `lab/CA/ALL/labDisplay.jsp?...&segmentID=<orphan>` → `OSCAR Error: 500`
  3. Stack: `Factory.getHandler(segmentID)` NPEs at `Factory.java:74` because `hl7TextMessageDao.find()` returns null → fall-through to `getHandler("", "")` returns null → JSP line 219 (`handler.getMsgType()`) NPE → 500
- **Root cause (data + code):**
  - **Data:** no FK enforced between `hl7TextInfo.lab_no` and `hl7TextMessage.lab_id`. Partial deletes / data-cleanup operations that hit one table but not the other leave dangling references. Confirmed via DB inspection: `hl7TextInfo` rows 173/174/175 existed but matching `hl7TextMessage` rows did not.
  - **Code:** `Factory.getHandler(String segmentID)` returns null silently (it's caught, logged, falls through to `getHandler("","")` which is also null-valued because `DefaultGenericHandler` can't parse empty input). `labDisplay.jsp` never checks for null before deref at line 219 — so any caller hitting an orphan crashes the entire JSP.
- **Fix shape (shipped):** in `labDisplay.jsp` immediately after `handler = Factory.getHandler(segmentID);`, added a null guard that renders a friendly HTML notice with HTTP 404 and returns. Minimal scope — single JSP, single guard, no Factory contract change. Working labs (e.g. segmentID=177 here) still render normally; the OLIS `<jsp:forward>` at line 231-233 is unaffected because handler is non-null in the happy path.
- **Verification:** Playwright — segmentID=173 (orphan) now returns 404 with `<title>Lab data not available</title>`; segmentID=177 (intact) still returns 200 with `<title>JANE Q DOE Lab Results</title>`. Console: no JS errors, no `<jsp:forward>` chain breakage.
- **Why this is worth fixing despite being "dev-data corruption":**
  - In production, partial deletes happen — DBAs clean up storage, migrations time out partway, retention policies hit one table but not its sibling. Any of these produces the same orphan state.
  - The pre-fix failure mode crashes the *entire lab display flow* — an admin trying to triage a stale row brings down their session for that lab. Friendly fallback lets the user see *which* lab is broken and continue working.
- **Adjacent risk (not fixed):** `labDisplay.jsp:198-205` (the `showAll`/`multiID` branch) iterates `Factory.getHandler(segmentIDs[i])` and adds each result to the `handlers` list; if any element is null, `handlers.get(0)` could later NPE at line 287 `handler.getPatientName()`. Same root cause, lower-frequency path (multi-lab view). Worth a follow-up sweep if encountered.
- **Out of scope (data-layer):** the underlying `hl7TextInfo` / `hl7TextMessage` orphan rows should be cleaned up at the DB level, or a sweeper job added — but that's a data-integrity task separate from the user-facing crash fix.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `area: lab`, `defensive-guard`

### A24: OLIS Add-to-Inbox classifies MD5-detected duplicates inconsistently (`errorIds` vs `successIds`, missing audit row)
- **Status:** ✅ Fixed — both code paths in `OLISAddToInbox2Action` now mirror the `OLISUtils.isDuplicate` reference path at lines 565-571 (write `logOLISDuplicate` audit row, `result.setStatus("duplicate")`, merge, classify as success).
- **Source:** observed by the user when adding the first row of an OLIS search whose body content had already been uploaded into OpenO earlier. Add-to-Inbox returned `{"successIds":[],"errorIds":["<uuid>"]}` and **no relevant Tomcat log** appeared — consistent with the silent `FileUploadCheck.UNSUCCESSFUL_SAVE` branch rather than a thrown exception. Reproduced via Playwright (2026-05-15) — fresh-content rows succeeded (`successIds`), only the previously-uploaded row produced `errorIds`.
- **Root cause:** `OLISAddToInbox2Action.addToInbox()` has **two** duplicate-detection paths that classified the same conceptual outcome differently:
  - Line 565 — `OLISUtils.isDuplicate(file)` (accession/content match against `OLISResults`) → writes audit row, marks `status="duplicate"`, **adds to `successful`** ✅
  - Line 584 — `FileUploadCheck.addFile()` returning `UNSUCCESSFUL_SAVE` (MD5 match against `file_upload_check`) → writes audit row, **adds to `errors`** ❌
  - The MD5 path silently returns `-1` (only a DEBUG log) so the user sees a generic error JSON with nothing in the Tomcat log to explain it.
- **Companion bug in `executeAddSingle`** (single-uuid path, not exercised by `bulkProcess`): the `UNSUCCESSFUL_SAVE` branch returned `"Already Added"` (correct UX) but **skipped the `logOLISDuplicate` audit row and the `status="duplicate"` mark** — so the OLIS Audit Log missed any re-add attempt through that path.
- **When this fires in production (not just synthetic test data):** any time the same lab content is re-encountered via OLIS after having been ingested elsewhere — e.g. cleanup deleted the `OLISResults` row but `file_upload_check` (permanent) still has the MD5; or the same lab was uploaded via HL7 batch / fax / different OLIS query first. Low frequency but real.
- **Fix shape (shipped):** both branches now do `logOLISDuplicate` → `result.setStatus("duplicate")` → `olisResultsDao.merge(result)` → classify as success (`successful.add(...)` for bulk; `request.setAttribute("result", "Already Added")` for single). Two ~3-line edits in `OLISAddToInbox2Action.java`.
- **Not caused by the C2/B1/init() work** — pre-existing logic; surfaced because the user's test environment has prior fixture uploads in `file_upload_check`.
- **Labels:** `type: bug`, `priority: low`, `area: olis`, `cosmetic` (UX-confusing; no data loss — the duplicate was correctly detected, just mis-classified in the response payload)

---

## Track B — Spec gaps, JSP-only quick wins

### B1: Results.jsp preview enhancements
- **Status:** ✅ Code done (`Results.jsp` only — no `OLISResults2Action` aggregator needed; JSP compile/render pending a deploy-time verify).
- **Closes:** OLIS01.02, OLIS01.03, OLIS03.02, OLIS03.03, OLIS04.10
- **Scope — shipped (all in `Results.jsp`):**
  - **Lab Name columns** — added visible **Reporting Lab** and **Performing Lab** columns to the preview table (the values were previously only attached as row attributes for the filter dropdowns). Both shown since the spec treats them as distinct query parameters.
  - **Match column** — explicit "Matched" / "Unmatched" text column (reuses the `demId` already derived in the patient-name cell); previously match status was implicit via the `here.gif` icon only.
  - **Blocked column** — per-row indicator driven by `result.isReportBlocked()` (`Boolean.TRUE.equals(...)`, null-safe). This is the per-row marker OLIS04.10 wants; the existing page-level `hasBlockedContent` override prompt stays. No `OLISResults2Action` aggregator was needed — the per-result `OLISHL7Handler` already exposes `isReportBlocked()`.
  - **Practitioner filter** — new `practitionerFilter` dropdown populated from `result.getAllPractitioners()`, a `practitioner="a|b|c"` row attribute, and a substring match wired into `filterResults()` (mirrors the existing multi-value `category` filter pattern). Closes the one filter gap flagged in OLIS01.03 / OLIS03.03.
  - Preview table went from 16 → 20 columns; `tfoot` colspan bumped accordingly.
- **Files:** `Results.jsp`
- **Labels:** `type: feature`, `priority: medium`, `area: olis`, `compliance`

---

## Track C — Backend / schema (one ticket per fix area)

### C1: Server-side OLIS Remove + manual-removal audit log (regression — see A9)
- **Status:** ✅ Done — landed as part of the A9 restoration in commit `f07aea1870`. The server-side `remove` / `bulkRemove` / `bulkProcess` handlers and the `logOLISRemoval` audit helper are all live on `OLISAddToInbox2Action`, wired via Struts2 wildcard method invocation on `<action name="olis/AddToInbox">` (`struts.xml:448`), and smoke-tested via the simulator (bulkProcess + checkbox-add flow end-to-end — see A9).
- **Closes:** OLIS01.04, OLIS03.04, OLIS06.03
- **Reframed as regression:** the Struts 1 OLIS action originally had `remove`, `bulkRemove`, `bulkProcess`, and the `logOLISRemoval` helper that together implement everything in this ticket's scope. They were dropped in commit `f90870dc15` (Dec 2024, Struts 1 cleanup). See A9 for full inventory of lost methods.
- **Scope (revised) — all shipped:**
  - **`bulkProcess` ported from `7aefabc840:src/main/java/org/oscarehr/olis/OLISAddToInboxAction.java`** — the exact server-side handler the `bulkProcess()` JS in `Results.jsp:356-400` calls. Reads the JSON `data` payload, iterates `remove_<uuid>` / `addToInbox_<uuid>` / `acknowledge_<uuid>` items.
  - **`remove` and `bulkRemove` ported** as supporting endpoints — `remove` (`OLISAddToInbox2Action.java:634`) marks the `OLISResults` row `"removed"` and calls `logOLISRemoval`.
  - **`logOLISRemoval` ported** (`OLISAddToInbox2Action.java:658`) — writes a genuinely distinct manual-removal audit row: `Removing User: <logged-in provider>` (the real provider, not "System"), `Removing Type: User`, `Removing Reason: Worklist Management`, `Removing Date`, plus Query Date/Type, Requesting HIC, Initiating Provider, and Accession / Test Request(s) / Collection Date / Last Update Date parsed from the HL7. The system-initiated path (`logOLISDuplicate`, `OLISAddToInbox2Action.java:712`) still correctly writes `Rejecting User: System (automatic)` / `Rejection Type: System`.
  - Adapted from Struts 1 (`ActionForward xxx(mapping, form, request, response)`) to Struts 2 method signatures returning `String` result names.
- **OLIS06.03 "System" hardcode — verified resolved:** the original gap had two parts — (1) manual removals not logged at all, and (2) Removal Type hard-coded "System" with no code path ever writing a non-system value. Both are closed: there is now a distinct manual-removal code path (`logOLISRemoval`) that captures the actual logged-in provider and a non-"System" type. The auto-reject path remains "System". No further C1 work required.
- **Files:** `OLISAddToInbox2Action.java`, `struts.xml`
- **Follow-up carve-out (minor, → folded into F1):** the manual-removal audit row uses label strings `Removing Type: User` / `Removing Reason: Worklist Management`, while OLIS06.03's field list names them "Removal Type (manual/system)" / "Removal Reason", and the system path uses the parallel-but-different `Rejection Type`. This is a cosmetic conformance-labeling inconsistency, not a functional gap — flag it for terminology alignment during F1 OntarioMD conformance prep rather than reopening C1.
- **Labels:** `type: bug`, `priority: high`, `compliance`, `regression`

### C2: OLIS Transaction ID logging
- **Status:** ✅ Code + DB migration done (Java compiles clean; `update-2026-05-14-olis-query-log-transaction-id.sql` applied, `olisinit.sql` updated for fresh installs) — ⚠️ OLIS06.02 intent + ERP parsing pending verification (see below)
- **Closes:** OLIS03.06, OLIS06.02
- **Field-location finding (verified against the OLIS Interface Specification, R01.32/33):** there is **no OLIS-assigned transaction ID** in the query/response message flow. The spec defines a single identifier — the **Message Control ID (MSH-10)**, set by the *initiating* EMR — and the HIAL envelope's **Client Transaction ID**, which the spec mandates equal MSH-10. The query response (an **ERP** message) echoes that value back in **MSA-2** ("OLIS will populate this field with the value from MSH.10 ... of the message sent by the initiating system", §10.2.5.12.2.3). So the value captured is the request's Message Control ID **as acknowledged by OLIS in the response** — the correlation key for the exchange. An earlier draft of this work read response MSH-10 on the assumption it was OLIS-minted; the synthetic `sample-response.hl7` fixture (`MSH-10=OLISMSG00001`) reinforced that error. Both are corrected.
- **Scope — shipped:**
  - **`OLISQueryLog.java`** — added `olisTransactionId` field + getter/setter. Populated post-response; stays `null` if the query failed before a response was received, or if the response carried no MSA segment.
  - **`OLISHL7Handler.java`** — new `getMsaControlId()` getter reads **MSA-2** via the existing HAPI `terser`.
  - **`Driver.java`** — new `extractOlisTransactionId(String)` helper unwraps the HL7 payload from the unsigned `<Response><Content>…</Content></Response>` XML envelope (or accepts a raw-HL7 string from the simulator path), hands it to `Factory.getHandler("OLIS_HL7", …)`, and reads `getMsaControlId()`. New `recordOlisTransactionId(...)` writes it back onto the SENT `OLISQueryLog` row. The `OLISQueryLog` reference was hoisted out of the audit-log `try` block so the response handler can enrich the same row. Both the real and `olis_simulate` branches now record the ID and stash it on the request as the `olisTransactionId` attribute. All extraction/merge failures are logged and swallowed — audit enrichment never breaks the query path.
  - **`OLISSearch2Action.java`** — consent-override (`force`) path reordered: `Driver.submitOLISQuery` now runs *before* the `OscarLog` consent-override row is persisted, and the row's data block appends `OLIS Transaction ID: <id>` read from the `olisTransactionId` request attribute. Non-force redo path still submits via the `else` branch.
- **Design note:** chose "enrich the SENT row post-response" over "split into SENT + RECEIVED rows" — the SENT row is still persisted pre-submit (so a failed call still leaves an audit trail), then updated in place with the Transaction ID once the response arrives. Keeps a single row per query keyed by `uuid`, no schema churn beyond the one column.
- **Test fixtures (`docs/olis/`):** `sample-response-erp.hl7` (ERP envelope + MSA, `MSA-2 = 018fd1f1-…`), `sample-response-erp-blocked.hl7` (ERP + MSA + `ZPD-3=Y` blocked, `MSA-2 = 02a1c3d5-…`), `sample-response-blocked.hl7` (bare `ORU` + blocked, no MSA). Each carries a distinct `ORC-4-1` accession so it isn't deduped by `OLISResults2Action.hasExistingResult` (requestingHIC + queryType + accession).
- **✅ Smoke-tested in simulate mode:** the synthetic ERP fixture parsed cleanly through `OLISHL7Handler` and `getMsaControlId()` extracted MSA-2 → `OLISQueryLog.olisTransactionId` populated (verified in DB). So the MSA-2 path and basic ERP parsing work.
- **⚠️ Open verification items (→ F1):**
  - **OLIS06.02 intent.** The term "OLIS transaction ID" comes from the OMD OLIS *Functional Requirements* (not the Interface Spec). Since OLIS issues no distinct ID, confirm with OntarioMD whether logging the acknowledged MSA-2 satisfies the requirement, or whether that half of OLIS06.02 is N/A.
  - **Real ERP fixture still needed.** The synthetic fixtures were hand-built from the spec's §10.2.4.5 worked example; a real `*_ERP_Response.HL7` is still needed to confirm the exact `MSH-9` structure code and that `Utilities.separateMessages` handles a multi-PID ERP correctly.
  - **OLIS03.06 transaction-ID-on-consent-override not fully testable in simulate mode.** The consent-override redo (`redo=true&force=true`) calls `Driver.submitOLISQuery` again, but `OLISResults2Action` nulls `session.olisResponseContent` after the first search — so in simulate mode the redo sees no response and the consent-override `OscarLog` row gets no Transaction ID. The code path is correct for production (a real redo re-queries OLIS and gets a fresh ERP); to exercise it in simulate mode the fixture must be re-uploaded immediately before clicking "Submit Override Consent". Best confirmed against a real OLIS connection.
- **DB migration:** `database/mysql/updates/update-2026-05-14-olis-query-log-transaction-id.sql` adds the `olisTransactionId varchar(255)` column to `OLISQueryLog`; `olisinit.sql` updated to match for fresh installs. *(Migration file header comment still references "MSH-10 of the response" from the earlier draft — should read "MSA-2 of the ERP response"; cosmetic, fix on next touch of that file.)*
- **Files:** `OLISQueryLog.java`, `OLISHL7Handler.java`, `Driver.java`, `OLISSearch2Action.java`, `update-2026-05-14-olis-query-log-transaction-id.sql`, `olisinit.sql`
- **Labels:** `type: feature`, `priority: high`, `compliance`

### C3: Per-provider unmatched-routing config
- **Status:** ✅ Code done (Java compiles clean; JSP + simulate-flow verify pending a deploy).
- **Closes:** OLIS02.03
- **Approach chosen — "polling provider":** an unmatched OLIS result is governed by the preference of the provider whose Z04 poll fetched it (semantically the Requesting HIC — the poll builds the Requesting-HIC segment from that provider). Rejected the alternative of using the `docNums` practitioners (ordering/copied-to from `OBR-16`/`OBR-28`): those aren't the Requesting HIC, can be multiple, and would force "split" routing that scatters one unmatched result across the unclaimed bucket *and* inboxes simultaneously.
- **Scope — shipped:**
  - **`OLISProviderPreferences`** — new **nullable `Boolean filterPatients`** field (3-state: `null` inherits system default, `TRUE` → unclaimed worklist, `FALSE` → provider's inbox).
  - **Context threading** — the polling `providerNo` is now carried from `OLISPollingUtil.pollZ04Query` → `parseAndImportResponse` → the OLIS upload-handler `parse()` overload → a new `MessageUploader.routeReport` overload → the core method. `MessageHandler.parse` (a 28-implementer interface) was **not** touched — threading goes through the *concrete* OLIS upload handler. Every non-OLIS / non-polling caller passes `null` and keeps today's system-level behaviour. The Z06 facility poll passes `null` (no single-provider context).
  - **`MessageUploader`** routing (the `"OLIS_HL7" && demProviderNo=="0"` branch) — looks up the polling provider's `OLISProviderPreferences.filterPatients`; a non-null value wins, `null` falls back to `OLISSystemPreferences.isFilterPatients()`.
  - **`provider/olis_preferences.jsp`** — new 3-state "Unmatched Patient Results" dropdown (Use system default / Filter to unclaimed worklist / Send to my inbox); `OlisPreferences2Action.view()`+`save()` wired. `save()` was restructured so the `OLISProviderPreferences` row is loaded-or-created once and persists `filterPatients` even when no poll start-time was submitted (previously the row was only touched inside the `providerStartTime != null` branch).
- **Note:** `OLISPoller` is dead code (`startAutoFetch` never called; live path is `OLISSchedulerJob` → `OLISPollingUtil`) — intentionally left untouched.
- **Files:** `OLISProviderPreferences.java`, `OLISPollingUtil.java`, `OLISHL7Handler.java` (upload handler), `MessageUploader.java`, `OlisPreferences2Action.java`, `provider/olis_preferences.jsp`, new migration SQL, `olisinit.sql`
- **Labels:** `type: feature`, `priority: medium`, `compliance`

### C4: Participating-labs source
- **Status:** ✅ Maintainability refactor done (Java compiles clean; JSP scriptlet changes pending a deploy-time verify). **OLIS04.03 stays *Partially Done*** — see "Not addressed" below.
- **Addresses:** OLIS04.03 *maintainability* gap only — does **not** close OLIS04.03.
- **Approach chosen:** Java single source of truth (enum) rather than a DB seed table — the list (Gamma-Dynacare 5552, CML 5407, LifeLabs 5687) is small and rarely changes; an enum keeps it self-contained with no schema churn. Trade-off: changing the list needs a code deploy.
- **Scope — shipped:**
  - **New `ca.openosp.openo.olis.model.OLISParticipatingLab` enum** — single source of truth, holding `labNo` (dropdown option value) + `displayName`, plus `getOid()` deriving the fully-qualified OLIS object identifier.
  - **`Search.jsp`** — all **8** hard-coded lab dropdowns now iterate the enum (Specimen Collector, Performing / Exclude Performing, Reporting / Exclude Reporting, Test Request Placer, Destination Laboratory, Ordering Facility — the readiness-plan's original "377-447" estimate missed the Z05/Z06 query sections further down).
  - **`provider/olis_preferences.jsp`** — both lab dropdowns (Default Reporting / Default Exclude Reporting) iterate the enum.
- **Not addressed — OLIS04.03 completeness gap remains open:** this is a *maintainability* fix, not a *completeness* fix. It single-sources the **same 3 labs** that were already there — it does **not** add the full roster of participating laboratories, and it does **not** make the list updateable from OLIS (the Lab/SCC Extract sync the requirement hints at — D2-nomenclature-refresh-sized work). The enum makes adding labs a one-line edit, but closing OLIS04.03 still needs either the full participating-lab roster seeded, or a sync from the OLIS extract. Track as a follow-up / F1 decision.
- **Deliberately left out of scope:** `OLISUtils` keeps its own lab-OID constants (`CMLIndentifier`, etc.) for source-facility dedup matching — that set also includes Alpha Labs (5254), which is *not* a query-parameter dropdown option, so the two lists aren't the same membership and shouldn't be force-unified. Adding Alpha Labs (or more participating labs) to the dropdowns is now a one-line enum edit if/when confirmed as queryable.
- **Files:** `OLISParticipatingLab.java` (new), `Search.jsp`, `provider/olis_preferences.jsp`
- **Labels:** `type: feature`, `priority: medium`, `compliance`

---

## Track D — Needs product/UX decision before sizing

### D1: OLIS-specific manual-match UI
- **Closes:** OLIS04.06
- **Partial regression context (see A9):** the original Struts 1 OLISAddToInboxAction had a `saveMatch` action method that handled the OLIS-specific match-to-demographic flow. It was dropped in commit `f90870dc15` (Dec 2024, Struts 1 cleanup). Whether or not that flow is exactly the OLIS-specific UI the spec wants, restoring `saveMatch` is a prerequisite — without it, even the existing MDS `PatientMatch.do` flow can't update OLIS-specific routing/audit state from the Results.jsp side.
- **Decision still required:** build OLIS-specific match flow that shows original OLIS demographics next to EMR demographics, OR formally accept the (restored `saveMatch` + existing MDS `PatientMatch.do`) flow as sufficient. The decision determines whether D1 is just "port saveMatch" (small) or "port saveMatch + design new UI" (medium).
- **Existing MDS flow:** `OpenEChart.jsp` → `oscarMDS/PatientSearch.jsp:183` → `PatientMatch2Action`
- **Labels:** `type: discussion`, `needs-design`, `priority: low`, `regression`

### D2: Nomenclature programmatic refresh
- **Closes:** OLIS04.08
- **Splits cleanly into two sub-items** (both shipped 2026-05-15):
  - **D2a — refresh mechanism** ✅ **Done** — admin XLSX importer + 5-column schema extension + richer CSV bootstrap. See below.
  - **D2b — consumption-side performance** ✅ **Done** — see below.
- **D2a — current state.** Manual CSV updates of `OLISTestRequestNomenclature.csv` / `OLISTestResultNomenclature.csv`. Seed sourced from `https://ehealthontario.on.ca/en/olis-nomenclature/download/olis-nomenclatures/prod/v2.69` dated **March 10, 2023** — meaning OpenO is currently **>3 years behind** the live nomenclature (latest is V3.03_PROD, April 23, 2026).

#### D2a — research findings (2026-05-15, from `/tmp/OLIS` materials)

**Distribution model — what eHealth Ontario actually publishes:**
- **Single XLSX file** with 9 sheets covering Test Request (3,464 rows), Test Result (49,326 rows), Microorganism, Specimen Source, plus per-area change-log sheets.
- **Versioned downloads at predictable URLs:** `https://ehealthontario.on.ca/en/OLIS-nomenclature/download/olis-nomenclatures/prod/v{X.YY}` (the Stage 4 Vendor Guide references `v3.00`; we have `V3.03_PROD`).
- **Cadence:** roughly quarterly releases; V3.03_PROD = April 23, 2026 contained 19 Request adds + 42 Request changes + 57 Result adds + 126 Result changes + 68 Microorganism changes (≈310 row deltas per release).
- **Hard operational deadline:** each release notice says **"Review and remap by `<deadline ~7 days later>` to prevent message failure."** This is not advisory — non-conforming senders start having OLIS messages rejected after the deadline. OpenO has missed every deadline since v2.69 (March 2023).
- **Change semantics are mature.** Each change has a `Type of Change` column: `Add` (new code, valid from Effective Date), `Change` (attributes updated — e.g. LOINC realignment, preferred-name change), `Deprecate` (code retired at End Date; suggested replacement in Change Note).
- The Microsoft Access mapping tool (`OLIS_MAP_d.mdb` + `OLIS_MAP_s.mdb` + vendor-local `OLIS_MAP_Local_d.mdb`) is for vendors that have **their own internal codes and need to maintain old→new translations to OLIS**. OpenO doesn't need this layer — OpenO stores OLIS codes directly. The official-reference table `ApendA` does include an `OLD_OLIS_Test_Request_Code` column, confirming that OLIS occasionally re-numbers codes; this is what enables deprecation-with-successor flows.

**Schema gap — OpenO local tables vs official distribution:**

| Concept | OpenO `OLISResultNomenclature` | Distribution: Test Result Nomenclatures |
|---|---|---|
| OLIS code | `nameId` | `LOINC Code` |
| Display name | `name` (= "Alternate Name 1") | `LOINC Short Name` + `LOINC Fully Specified Name` + 3 Alternate Names |
| Validity window | ✗ | `Effective Date`, `End Date` |
| Status | ✗ | `Workflow Status` (RELEASED/etc.), `Validation Status` (ACTIVE/INACTIVE), `Registration Status` |
| Reportability | ✗ | `Reportable`, `Reportable Context` |
| Category / Sub-cat | ✗ | `Result Category`, `Result Sub-Cat` |
| Change provenance | ✗ | `Change Note`, `External Code Version` |

The local schema captures **3 of ~31 columns** — it has just enough for a wire-format identifier and a display string. It cannot today distinguish ACTIVE from deprecated codes, enforce effective-date windows, or surface a successor for a retired code.

#### D2a — options considered

Three options ordered by lift:

1. **A — Stay with CSV reseed.** Periodically transcribe the XLSX → CSV by hand and rerun `olisinit.sql`. Cheapest, but: the manual transcription is the same step that put us 3 years behind already, doesn't capture effective/end dates so we can't enforce deprecation, and risks transcription errors going unnoticed since 49K rows isn't humanly reviewable.

2. **B — Admin XLSX importer + schema extension (chosen, shipped).** Detailed below.

3. **C — Automated periodic sync from the eHealth Ontario URL.** A scheduled job hits the publish URL on a cadence (weekly), pulls the latest XLSX, computes the delta, applies. Best long-term but: depends on the URL being stable, programmatically accessible (the portal may gate downloads behind login), and free of per-vendor download-tracking terms. Material risk we can't resolve without OntarioMD/eHealth Ontario consultation. **Not done as the first step** — but Option B is a clean precursor; the same parser + upserter is reused when the URL question gets answered.

#### D2a — what shipped (Option B)

**Schema extension** (`database/mysql/olis/olisinit.sql` for fresh installs + `database/mysql/updates/update-2026-05-15-olis-nomenclature-extension.sql` for existing DBs):
- `effectiveDate` (DATE, nullable) — autocomplete and audit
- `endDate` (DATE, nullable) — codes past this date filtered from autocomplete
- `status` (VARCHAR(16) NOT NULL DEFAULT 'ACTIVE') — filter autocomplete to `ACTIVE` only
- `externalCodeVersion` (VARCHAR(8), nullable) — audit trail
- `successorCode` (VARCHAR(10), nullable) — placeholder for Deprecate-with-replacement flows (not yet populated; would derive from `OLD_OLIS_Test_Request_Code` in the Access mapping files)
- Indexes on `(status, endDate)` and `(nameId)` to keep `findByNameLike` cheap.

**Admin XLSX importer** (`OLISNomenclatureImport2Action`, `_admin/w` gated):
- Parses the XLSX with **stdlib zip + SAX** — not Apache POI. Reason: POI isn't in `pom.xml` (only the legacy HSSF `poi` artifact for `.xls` is; `poi-ooxml` would be a new transitive 5MB+ dependency). Stdlib gets us streaming parse of 49K rows for an admin-only path in ~100 lines without adding a dependency.
- Upserts row-by-row using `nameId` as the lookup key.
- Status derivation: `INACTIVE` if `Validation Status Indicator` is INACTIVE OR if `Workflow Status Indicator` is anything other than `RELEASED`. Otherwise `ACTIVE`. (Mirrored byte-for-byte in the CSV regen script.)
- Date parsing: Excel numeric serial → `yyyy-MM-dd` string → `M/d/yyyy` string, all **non-lenient** (so the V3.03 string `3/9/2023` doesn't get normalized into year 9 by `SimpleDateFormat` lenient mode — a bug we hit and fixed during smoke test).
- Returns an Added / Updated / Deprecated count per table for the admin to review against the release notes inside the 7-day deadline window.

**Richer fresh-install CSV** (the key D2a-vs-D2a-Option-B difference): the regenerated seed CSVs now carry the full filter-relevant column set — `(nameId, name, status, effectiveDate, endDate, externalCodeVersion)` for results and `(nameId, name, category, status, effectiveDate, endDate, externalCodeVersion)` for requests. Decision rationale: without status/dates at fresh-install time, every code defaults to ACTIVE and the autocomplete shows deprecated codes until an admin runs the importer. That "admin forgets on day 1" state would silently persist forever on small-clinic installs. Carrying the full set in the CSV makes a fresh install conformant out of the box. The cost is logic duplication between the Python regen script and the Java importer (`deriveStatus`, `parseDateCell`); the script's docstring flags the requirement to keep them in lock-step.

**Regen tooling** (`database/mysql/olis/regenerate-csvs-from-xlsx.py`): stdlib-only Python script that reads the official XLSX and writes the two TSVs in the directory it lives in. Reusable for future releases — ~10 minutes per refresh. Prints Added/Updated/Deprecated-equivalent counts + verification SQL.

**Verification:**
- Imported V3.03_PROD on dev: 49,325 result codes + 3,463 request codes processed; counts matched the change-log sheet exactly.
- Fresh-install LOAD-DATA-INFILE on a temp clone of the new schema: 48,521 ACTIVE / 804 INACTIVE results, 3,411 ACTIVE / 52 INACTIVE requests — same row state as the importer produces.
- Deprecated codes (`Differential; Blood`, `Blasts; Blood`) no longer appear in the autocomplete after the schema change.

**Out-of-scope nice-to-haves identified along the way (not blocking F1 conformance):**
- OpenO has no `OLISMicroorganism` / `OLISSpecimenSource` tables despite both being part of the distribution. Inbound HL7 messages reference these in OBX-5 (specimen) and microbiology results; treating them as opaque strings today probably means some downstream display fidelity loss. Worth a separate readiness-plan entry; not blocking F1 conformance for the SPQ/ERP scenarios per the Stage 4 Vendor Guide.
- OpenO's `OLISResultNomenclature.name` stores the **Alternate Name 1** (per E1). The distribution also publishes **Fully Specified Name** (e.g. `Glucose:MCnc:Pt:Ser/Plas:Qn`) which is the LOINC-canonical machine-readable name. Storing FSN alongside Alt Name 1 would let result rendering fall back to FSN when Alt Name 1 is blank (which it is for ~15K rows in the current distribution).
- The richer-autocomplete-search question: today `findByNameLike` matches only against `name` (= Alt Name 1). Extending the LIKE to also match Alt Name 2 / Alt Name 3 / LOINC Short Name would let users find "Vitamin B12" against a row whose Alt Name 1 is "Cobalamins". Needs schema extension (3 more columns) and a LIKE-with-OR clause.
- **Codes deleted-not-deprecated.** The importer's upsert means codes that existed in OpenO's previous baseline but are no longer in the published XLSX *stay in the DB marked ACTIVE forever*. V3.03 vs V2.69 has ~6,000 such removed-code rows. A future enhancement could mark "codes not seen in this import as INACTIVE" — but that's a destructive operation worth thinking about carefully (old archived labs referencing those codes would render with stale-data flags).
- **D2b — Search.jsp consumption-side performance** ✅ **Done (2026-05-15):**
  - **Was:** `olis/Search.jsp` called `OLISResultNomenclatureDao.findAll()` + `OLISRequestNomenclatureDao.findAll()` on every page load (~48,200 + ~3,000 rows = ~51,000 `<option>` elements rendered into two `<select multiple>` controls). Long-observed slow Search.jsp page load was pre-existing, not a regression.
  - **Now:** server-side autocomplete/typeahead. New AJAX endpoint `OLISNomenclatureSearch2Action` (`/olis/NomenclatureSearch.do`) returns top-25 matches via `OLISResultNomenclatureDao.findByNameLike(term, limit)` / `OLISRequestNomenclatureDao.findByNameLike(term, limit)`. Search.jsp UI replaced with `<input>`-driven jQuery UI autocomplete plus a chip strip holding hidden `testResultCode` / `testRequestCode` inputs so the existing `OLISSearch2Action.getParameterValues(...)` flow is unchanged.
  - **Library choice — jQuery UI 1.12.1 over YUI:** initial implementation mirrored the legacy YUI XHRDataSource pattern used by the patient field on the same page, but YUI has been end-of-life since 2014 and the first build hit a YUI quirk (URL `?` collision — see below). Swapped to jQuery UI autocomplete, which is the pattern used elsewhere in OpenO (`appointment/addappointment.jsp` etc.). Required also loading `jquery-3.6.4.min.js` because Search.jsp's existing `js/jquery.js` is jQuery 1.3.2 (from 2009) and jQuery UI 1.12.1 needs jQuery ≥1.7. The patient autocomplete next to our new fields still uses YUI — leaving that as pre-existing scope.
  - **Verified end-to-end (Playwright, devcontainer):**
    - Page render: 71 KB / ~94 ms / 208 `<option>` elements (down from ~51,200).
    - AJAX endpoint: 200 OK / <120 ms for both `type=result` and `type=request`.
    - Type → suggest → click → chip + hidden input + AC input cleared.
    - × on chip removes both the chip and its hidden input.
    - `FormData(Z01_form)` picks up multi-value `testResultCode[]` / `testRequestCode[]` as the action expects.
    - YUI patient autocomplete on the same page still functional after jQuery 3.6.4 was loaded (YUI doesn't touch the `$` global).
  - **YUI quirk caught during pre-swap smoke test (since obsolete):** YUI's XHRDataSource appended `?query=...` to a URL that already ended in `?type=result`, producing two `?` and `getParameter("query") == null`. The jQuery UI swap incidentally removed this whole class of issue (`$.getJSON(url, {query, type})` builds the query string correctly).
  - **Files:**
    - `src/main/java/ca/openosp/openo/olis/OLISNomenclatureSearch2Action.java` (new)
    - `src/main/java/ca/openosp/openo/olis/dao/OLISResultNomenclatureDao.java` (+`findByNameLike`)
    - `src/main/java/ca/openosp/openo/olis/dao/OLISRequestNomenclatureDao.java` (+`findByNameLike`)
    - `src/main/webapp/WEB-INF/classes/struts.xml` (+`olis/NomenclatureSearch` route)
    - `src/main/webapp/olis/Search.jsp` (drop `findAll()` calls, replace two `<select multiple>` blocks with jQuery UI autocomplete + chips, add jquery-3.6.4 + jquery-ui-1.12.1 script/link tags, drop unused imports including `Misc` / both nomenclature DAOs/models, add chip CSS)
  - **Follow-up — OBX-3 / OBR-4 wire-code fix** ✅ **Done (2026-05-15, follow-up commit):** the legacy `<select>` rendered `option value="<%=nomenclature.getId()%>"` (Hibernate auto-increment PK), and `OLISSearch2Action` passed that PK straight into `new OBX3(code, "HL79902")` / `new OBR4(code, "HL79901")`. OLIS spec §10.2.5.14/§10.2.5.15 examples make clear that component 1 must be the wire-format code (LOINC for results e.g. `14683-7`, TR-prefixed OLIS code for requests e.g. `TR10481-0`), not the local PK. Fixed by changing the AJAX endpoint to return `{code: nameId, name: ...}` and threading `code` through `addChip` → hidden input → form submit. `OLISSearch2Action` itself needed no changes — it trusts the value it receives. Verified end-to-end in Playwright: result-code chip now carries `62468-4` (LOINC) and request-code chip carries `TR13070-8` — matches spec exactly. This bug had no in-passing exception risk because the field was effectively unusable anyway (51K-option ctrl-click UI), so no real OLIS searches were ever submitted with the wrong code.
- **Labels:** `type: discussion`, `needs-design` (D2a only), `priority: low`

### D3: Structured doctor-name data from OLIS handler (replace synthesized `<span>` markup)
- **Status:** ✅ Done (thorough migration, 2026-05-15) — `OLISHL7Handler.DoctorName` nested class introduced; `getFullDocName` re-routed through structured parse + `toPlainText`; PDF renderer + 6 of 8 strip sites switched to clean source.
- **What shipped:**
  - **New `OLISHL7Handler.DoctorName` nested class** holding prefix / givenName / middleName / familyName / suffix / degree / licenseType / licenseNumber, with `getNamePart()` / `getLicensePart()` / `toPlainText()` / `isEmpty()` accessors.
  - **`getFullDoctorName(String docSeg)`** is now the canonical parser — returns `DoctorName`. The legacy `getFullDocName` shim delegates to `getFullDoctorName(...).toPlainText()` so it returns clean text (no `<span>`).
  - **Four new public structured methods** on `OLISHL7Handler`: `getDocNameStructured()`, `getAttendingProviderStructured()`, `getAdmittingProviderStructured()`, `getCCDocsStructured()` (the last returns `List<DoctorName>`).
  - **`OLISLabPDFCreator.getDoctorNamePhrase`** now takes `DoctorName` and renders `getNamePart()` in main font + `getLicensePart()` in `subscriptFont` from structured fields. No more Jsoup round-trip — the import was dropped.
  - **`getCCDocNamesPhrase`** now takes `List<DoctorName>` and iterates structured entries instead of splitting on commas.
  - **6 strip sites dropped (parser-fresh, never get markup):**
    - `labDisplayOLIS.jsp:1178` (`getDocName`), `:1257` (`getAttendingProviderName`), `:1274` (`getAdmittingProviderName`), `:1367` (`getCCDocs`)
    - `labDisplay.jsp:1716` (`getDocName`), `:1728` (`getCCDocs`)
    - `Hl7textResultsData.java:727` (`hl7.getRequestingProvider()`)
    - `HtmlTextCleaner` import dropped from both JSPs.
  - **2 strip sites intentionally retained (DB-fed, legacy data possible):**
    - `Hl7textResultsData.java:598` (`info.getRequestingProvider()` — `hl7TextInfo` DB column)
    - `Hl7textResultsData.java:847` (`requesting_client` from the same DB column)
    - These rows were written before D3 with `<span>` markup; until a backfill or natural re-save runs, the defensive strip stays. Adding a code comment at line ~727 explains the asymmetry.
- **Why we did the thorough variant:** the user signal was "remove some of the other areas that try to strip html markup that are no longer needed" — so we extended past the Lazy migration. The 2 DB-fed wrappers stay defensive; everything else is parser-fresh and confirmed-clean.
- **Verification (Playwright, 2026-05-15):** lab 176 + 177 + InboxHub list all rendered clean — no `<span>` text, no `&lt;span` HTML-escape leak, "Ordering Provider: DR. BRENT RYAN CRAWFORD MD 109753" (and similar for JANE/DR JOHN SMITH) rendering as plain text in the right places. PDF subscript styling preserved via structured `getNamePart` / `getLicensePart` split. `END OF REPORT` footer still present.
- **What this closes:**
  - A22 / A23 entries — wrappers either removed or now defensive no-ops. Listed as "✅ Fixed (resolved + wrappers removed by D3)" below.
  - **Does NOT address A26** (Home Address `<br />` leak in `labDisplayOLIS.jsp:843` helper). That's a separate JSP-local helper for the address block, not a parser output — same family but separate fix.
- **Future cleanup follow-ups (optional):**
  - JSP CSS upgrade: emit a real `<span class="md-license">` from the structured data + add corresponding CSS — would visually restore the small-grey subscript intent on the web (PDF already does this).
  - DB backfill: re-save existing `hl7TextInfo.requestingProvider` rows through the new clean code path so the 2 defensive strips at Hl7textResultsData:598/847 can also drop. Optional — no functional impact.
- **Labels:** `type: refactor`, `tech-debt`, `done`

---

## Track E — Verification / decision (no code, sign-off only)

### E1: OLIS04.05 nomenclature name field — verified as "Alternate Name 1"
- **Status:** ✅ Verified (2026-05-15). No code change needed; conformance posture positive.
- **Question (original):** does `OLISResultNomenclature.getName()` (`OLISHL7Handler.java:1725`) correspond to OLIS "Alternate Name 1" or to the LOINC Fully Specified Name? If FSN, we'd need to scope a physician-preferred display override.
- **Evidence chain:**
  - **OLIS Interface Specification §6.7.1.2** (`OLIS_Interface_Specifications_EN.pdf` line 1937): "The OLIS Test Result Nomenclature includes a field named **Alternate Name 1** that contains a suggested display name for each test result code. This data may be used as a starting point for selecting preferred test result names."
  - **Same spec §10.2.5.14.3.1.4** (line 11042): the wire-format test result code is paired with the LOINC Fully Specified Name on OBR/OBX (example: `6301-6^COAGULATION TISSUE FACTOR INDUCED.INR:RLTM:PT:PPP:QN:COAG^HL79902`). So the FSN is what travels on the wire.
  - **Our seed file** (`database/mysql/olis/OLISTestResultNomenclature.csv`, sourced from `https://ehealthontario.on.ca/en/olis-nomenclature/download/olis-nomenclatures/prod/v2.69` dated March 10, 2023) has exactly 2 columns: `(nameId, name)`. Sample rows: `14749-6 → Glucose`, `2160-0 → Creatinine`. The LOINC FSN for these codes would be `Glucose:MCnc:Pt:Ser/Plas:Qn` and `Creatinine:MCnc:Pt:Ser/Plas:Qn` respectively — our `name` is the short, display-friendly form, not the FSN. **Confirms our `name` column is "Alternate Name 1".**
  - Whoever originally imported the OLIS distribution pre-filtered it down to `(LOINC code, Alternate Name 1)`. The full OLIS-published file likely has more columns (FSN, deprecation flag, active dates, etc.) but our seed already extracted the display name OLIS recommends viewers use.
- **Conformance implication:** positive. The OLIS spec recommends Alternate Name 1 as the starting point for display; our handler returns that directly via `OLISResultNomenclature.getName()`. No physician-preferred override needed. The Fully Specified Name does still travel on the wire (in the OBX/OBR coded-element second component) and is available to consumers that want it.
- **Labels:** `type: documentation`, `priority: low`, `verified`

### E2: OLIS04.09 patient-matching criteria — verified spec-exact
- **Status:** ✅ Closed — verified during the deep-dive audit (`deep-dive-findings.md` §3a). `MessageUploader.willOLISLabReportMatch()` (`MessageUploader.java:494`) keys its SQL on `hin` + `last_name` + `year/month/date_of_birth` + `sex` — exactly the spec's HCN + Gender + DOB + Last name. `firstName` is passed in but never used in the query.
- **Original premise was wrong:** this item was opened on the belief that the matcher was "stricter than spec" by also requiring first name. It isn't — there is no deviation to document or relax. The stale claim has been corrected in `requirements-analysis.md`.
- **Labels:** `type: documentation`, `priority: low`

---

## Track F — External / coordination

### F1: OntarioMD Conformance Testing
- **Closes:** OLIS06.01
- **Scope:** schedule, prep test transcripts, run conformance suite, manage feedback loop
- **Long pole** — open this immediately; the slow loop dwarfs the code work
- **Include in ticket body:** checklist of claimed-Meets vs claimed-Partial after fixes land (this is what OntarioMD will challenge first)
- **Carried-in nit from C1:** align the manual-removal audit-row label strings (`Removing Type: User` / `Removing Reason: Worklist Management`) with OLIS06.03's spec terminology ("Removal Type (manual/system)" / "Removal Reason") and with the parallel `Rejection Type` labels on the system-reject path. Cosmetic, but it's the kind of wording OntarioMD will flag — cheap to fix in the same pass.
- **Carried-in from C2 — confirm OLIS06.02 transaction-ID intent:** the OLIS Interface Spec defines no OLIS-minted transaction ID; C2 logs the request's Message Control ID as acknowledged by OLIS in the ERP response (MSA-2). Confirm with OntarioMD that this satisfies "log the OLIS transaction ID", or that the requirement is N/A. Also obtain a real `*_ERP_Response.HL7` sample to verify `OLISHL7Handler` + `Utilities.separateMessages` parse the ERP message structure correctly (the local fixture is a bare `ORU`, not an `ERP`).
- **Labels:** `type: maintenance`, `priority: high` (because of lead time, not difficulty)

---

## Track G — Local audit (this work item)

### G1: Local OLIS smoke test against current branch
- Stand up OLIS locally and exercise the major flows (Z01 patient query, Z04 preload/practitioner query, Results preview, Save / Sign-off, Forward, PDF render)
- Triage anything new into Track A bug tickets
- The previous working version was on Struts 1; expect drift bugs from the Struts 2 migration

### G2: HAPI structure-walker misses nested segments — synthetic-fixture quirk (NTE rendering, `isReportBlocked()`)
- **Not a bug in OpenO.** During A1 development we built `docs/olis/sample-response-a1-rich.hl7` with NTE comment segments interleaved between OBR and ZBR (`OBR → ZBR → NTE → ...`). Those NTEs didn't render because `OLISHL7Handler.getNTELocation` walks `terser.getFinder().getRoot().getNames()` which doesn't expose nested NTEs when custom Z-segments sit between the OBR and the NTE in the wire order.
- **Real OLIS data is unaffected.** The original A1 reporter's screenshot showed `&nbsp;` Specimen Comment content rendering correctly. The user's recent lab upload also rendered comments correctly during A21 verification. Production OLIS messages put NTEs in HAPI-expected positions.
- **If a future dev hand-crafts an HL7 fixture and finds comments not rendering**, the fix is to put the NTE segments in standard ORU_R01 positions (immediately after OBR before any OBX, between OBX, or at top-of-message before any OBR) — NOT to defensively rewrite `getNTELocation`. Malformed upstream HL7 would be an OLIS escalation, not an OpenO defensive-coding task.
- **Same root cause affects `isReportBlocked()` — found in the B1 deep-dive (2026-05-14).** `OLISHL7Handler.init()` detects blocked status by walking the *same* `terser.getFinder().getRoot().getNames()` root-level segment list. For a bare `ORU^R01^ORU_R01` fixture, HAPI resolves the typed `v24.message.ORU_R01` structure and nests the non-standard `ZPD` segment inside the patient group — so the root-level walk misses it and `parseZPDSegment()` never runs (`sample-response-blocked.hl7` reproduced this: the B1 Blocked column stayed empty). An `ERP^Znn^ERP_R09` message has no HAPI structure class → parses flat as `GenericMessage` → `ZPD` sits at root → detected (`sample-response-erp-blocked.hl7` worked). **Real OLIS query responses are `ERP`, so real-world blocked-status detection works** — but `init()`'s root-only segment traversal is the shared brittleness behind both this and the NTE case. Same guidance: don't defensively rewrite the walker for a hand-crafted fixture; if a real `ERP` response ever nests `ZPD`, that's an F1 finding. A future hardening pass on `init()`'s segment traversal (recurse into groups instead of walking root names) would address both. See `deep-dive-findings.md` §4a.
- **Permanent-save propagation trace (2026-05-15) — same fragility, four sites.** Walked Results.jsp → `OLISAddToInbox2Action` → upload-handler `OLISHL7Handler.parse()` → `MessageUploader.routeReport()`. `MessageUploader` has **zero** references to `isReportBlocked()` / `isOBRBlocked()`, `Hl7TextInfo` has **no `blocked` column**, and no `measurementsExt reportBlocked=Y` row is written by the OLIS save path (that key is only set by the CDS XML import). Blocked status survives implicitly: the raw HL7 (ZPD included) is base64-stored to `hl7_text_message` and re-parsed on every view (`labDisplayOLIS.jsp`, `OLISLabPDFCreator`, plus B1 preview Results.jsp). All four call sites therefore share one fragility — they win or lose together with `init()`'s root-only walk. For real OLIS (ERP envelopes preserved as-is) all four work; for synthetic bare-ORU fixtures all four fail. A single `init()` hardening pass (recurse into typed groups) closes all four. **Side note (not a conformance gap):** there's no fast-query path for "show me all blocked labs" — would require a base64-decode + re-parse scan. See `deep-dive-findings.md` §4a.
- **✅ Closed (2026-05-15, commit `3161c1fd50`):** the proposed hardening landed in a slightly different form — instead of teaching `init()` to recurse into typed groups, the parser is now configured with a custom `ModelClassFactory` that forces every message to resolve as `GenericMessage` regardless of MSH-9-3. All segments therefore land flat at the message root, making the existing root-only walk correct by construction. **All four call sites now work for both bare `ORU^R01` and `ERP^Z01` fixtures.** This closes G2 (NTE rendering) and §4a (OLIS04.10 / `isReportBlocked()`) together.

### G3: Devcontainer doesn't load `over_ride_config.properties` at runtime (general dev-experience gap, not OLIS-specific)
- **Reclassified from A6.** Surfaced during the OLIS audit but not OLIS-scoped — affects any property override anyone wants to keep out of the checked-in primary file. File as a general dev-experience issue.
- **What works:** properties set in `src/main/resources/oscar_mcmaster.properties` are picked up at app startup exactly as expected.
- **What doesn't work:** setting the same property in `src/test/resources/over_ride_config.properties` instead (so it stays out of the checked-in primary file) is silently ignored at runtime — the override file is only loaded by the JUnit test infrastructure, not the running webapp.
- **Root cause:** `OscarProperties.java:181-194` constructor only loads `/oscar_mcmaster.properties` from the classpath at app startup. `over_ride_config.properties` is loaded only when the JVM is started with system property `-Doscar_override_properties=<path>` — which the dev container's `server` startup script doesn't set. The override file ends up being test-only infrastructure despite the name suggesting it's a general dev override.
- **Why this matters:** without the override mechanism working, devs have to edit `src/main/resources/oscar_mcmaster.properties` (a checked-in file) for any local-only configuration, then remember to revert before opening a PR. Easy to forget — leaks dev flags into review. Surfaced under OLIS because that's where the `olis_simulate=yes` flag had to be set, but the same gap will bite any other module that wants a local override.
- **Proper fix:** devcontainer `server`/`setenv.sh` tweak that exports `-Doscar_override_properties=/workspace/.../override.properties` (or similar) at JVM startup. Small dev-experience ticket.
- **Labels:** `type: documentation` / `dev-environment`, `priority: low`

---

## Sequencing recommendation

1. **Day 0**: open A1, A2, F1, D1, D2, E1, E2, G1 — get the slow-loop and decision items moving
2. **Sprint 1**: land B1 (JSP quick-win bundle); A1/A2 in parallel; G1 ongoing
3. **Sprint 2**: C1 + C2 — compliance-critical audit/logging fixes with shared review themes
4. **Sprint 3+**: C3, C4, then whatever D1/D2 resolve to

## Cross-cutting notes

- **Parent epic:** "OLIS OntarioMD Conformance Readiness" linking all tickets, body referencing `docs/olis/requirements-analysis.md` at a pinned commit SHA
- **Parallelization:** A1, A2, B1, C1, G1 are file-disjoint and can be assigned to different devs immediately. C2 should sequence after C1's PR lands (shared audit-log path).
- **Compliance-critical cluster:** C1 + C2 + C3 should all land before F1 conformance testing kicks off, otherwise the test cycle will surface them and add round-trip time.
