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
| A22 | InboxHub web list leaks OpenO-synthesized `<span>` markup in the "Requesting Client" column | ✅ Fixed — `HtmlTextCleaner.toPlainText` applied at 3 Hl7textResultsData population sites | TBD |
| A23 | Lab display JSPs (`labDisplay.jsp` + `labDisplayOLIS.jsp`) leak `<span>` markup in provider/CC/attending/admitting fields | ✅ Fixed — `HtmlTextCleaner.toPlainText` wrapped before `Encode.forHtml` at 6 render sites | TBD |

**Net status (this dev environment):** all session-surfaced bugs (A1, A2, A3, A4, A5, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A18, A20, A21, A22, A23) closed; A17 intentionally deferred.
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

---

## Track B — Spec gaps, JSP-only quick wins

### B1: Results.jsp preview enhancements
- **Closes:** OLIS01.02, OLIS01.03, OLIS03.02, OLIS03.03, OLIS04.10
- **Scope:**
  - Add Lab Name column to preview table (`Results.jsp:767-778`)
  - Add per-row matched/unmatched indicator (currently only `here.gif` icon at `Results.jsp:827-840`)
  - Add per-row blocked indicator (page-level `hasBlockedContent` exists at `OLISResults2Action.java:476-490` but no per-row marker)
  - Add Practitioner filter dropdown + wire into `filterResults()` (`Results.jsp:230-283`, `:599-727`)
- **Files:** `Results.jsp`, possibly `OLISResults2Action.java` for new aggregator
- **Labels:** `type: feature`, `priority: medium`, `area: olis`, `compliance`

---

## Track C — Backend / schema (one ticket per fix area)

### C1: Server-side OLIS Remove + manual-removal audit log (regression — see A9)
- **Closes:** OLIS01.04, OLIS03.04, OLIS06.03
- **Reframed as regression:** the Struts 1 OLIS action originally had `remove`, `bulkRemove`, `bulkProcess`, and the `logOLISRemoval` helper that together implement everything in this ticket's scope. They were dropped in commit `f90870dc15` (Dec 2024, Struts 1 cleanup). See A9 for full inventory of lost methods.
- **Scope (revised):**
  - **Port `bulkProcess` from `7aefabc840:src/main/java/org/oscarehr/olis/OLISAddToInboxAction.java`** — this is the exact server-side handler that the existing `bulkProcess()` JS in `Results.jsp:356-400` is calling. Reads the JSON `data` payload, iterates `remove_<uuid>` / `addToInbox_<uuid>` / `acknowledge_<uuid>` items.
  - **Port `remove` and `bulkRemove`** as supporting endpoints
  - **Port `logOLISRemoval`** for the manual-removal audit row (already includes Rejection Type=Manual + Rejecting User=<providerNo>, addressing OLIS06.03's hardcoded "System" gap)
  - Adapt from Struts 1 (`ActionForward xxx(mapping, form, request, response)`) to Struts 2 method signatures returning `String` result name; register methods in `struts.xml` if needed
- **Files:** `OLISAddToInbox2Action.java`, `struts.xml`
- **Effort:** materially smaller than original "design and implement" estimate — the code already exists in git, the work is mechanical port + smoke test.
- **Labels:** `type: bug`, `priority: high`, `compliance`, `regression`

### C2: OLIS Transaction ID logging
- **Closes:** OLIS03.06, OLIS06.02
- **Scope:**
  - DB migration: add `olisTransactionId` column to `OLISQueryLog` (`database/mysql/updates/update-YYYY-MM-DD-olis-query-log-transaction-id.sql`)
  - Extract OLIS Transaction ID from response in `Driver.java`
  - Move audit write to post-response, OR split into SENT row (pre-submit) + RECEIVED row (post-submit) keyed by `uuid`
  - Update `OLISSearch2Action.java:186-224` (consent override path) to write the post-response log
- **Files:** `OLISQueryLog.java`, `Driver.java`, `OLISSearch2Action.java`, new migration SQL
- **Labels:** `type: feature`, `priority: high`, `compliance`

### C3: Per-provider unmatched-routing config
- **Closes:** OLIS02.03
- **Scope:**
  - New `filterPatients` field on `OLISProviderPreferences` (DB migration + entity)
  - UI on `Preferences.jsp` for per-provider override
  - `MessageUploader.java:276-283` routing change: prefer per-provider setting, fall back to system-level
- **Files:** `OLISProviderPreferences.java`, `Preferences.jsp`, `MessageUploader.java`, new migration SQL
- **Labels:** `type: feature`, `priority: medium`, `compliance`

### C4: Participating-labs source
- **Closes:** OLIS04.03
- **Scope:** Replace hard-coded 3-lab list in `Search.jsp:377-447` (Gamma-Dynacare 5552, CML 5407, LifeLabs 5687) with a maintainable seed table or lookup
- **Files:** `Search.jsp`, possibly new DAO/seed
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
- **Decision:** keep CSV reseed model, build a one-shot batch importer, or build a sync framework
- **Affects scope significantly.** Current state: manual CSV updates of `OLISTestRequestNomenclature.csv` / `OLISTestResultNomenclature.csv`
- **Labels:** `type: discussion`, `needs-design`, `priority: low`

### D3: Structured doctor-name data from OLIS handler (replace synthesized `<span>` markup)
- **Decision:** refactor `OLISHL7Handler.getFullDocName` (and related getters) to return structured doctor-name data — eg. a record `DoctorName(name, prefix, licenseType, licenseNumber)` — and let each renderer decide how to display, instead of having the handler emit `<span style="...">` markup that consumers have to parse back out.
- **Current state (post A1/A21/A22/A23):** `OLISHL7Handler.getFullDocName:2689` synthesizes `<span style="margin-left:15px; font-size:8px; color:#333333;">{type} {licenseNum}</span>` markup. Every consumer that wants plain text has to strip it via Jsoup (`HtmlTextCleaner.toPlainText`). The PDF path also has bespoke logic in `OLISLabPDFCreator.getDoctorNamePhrase` that parses the markup back out to render the license number in subscript font.
- **Why this is worth doing:**
  - The data layer is encoding a *styling intent* as inline HTML, which is a category error — display concerns belong in renderers, not models.
  - Web display (`labDisplay.jsp`, `labDisplayOLIS.jsp`) currently can't honour the small-grey styling intent because `<c:out>` HTML-escapes the markup; we strip the markup instead. With structured data, the JSPs could render real CSS-styled `<span class="md-license">{type} {licenseNum}</span>` and clinicians would see the actual visual intent.
  - InboxHub (`Hl7textResultsData`) currently strips the markup at the population layer; with clean source data, the strip becomes a no-op.
  - Roughly 14 of the ~36 Jsoup call sites we added become removable (the doctor-name ones; comment-field sanitization stays — those handle genuine upstream HTML from the lab system, not OpenO synthesis).
- **A21's preparatory contribution:** consolidates OLIS PDF rendering paths into a single creator (`OLISLabPDFCreator`), so the structured-data migration is a single-renderer change rather than two. The 5 consult-flow sites A21 fixed now all route through `OLISLabPDFCreator.getDoctorNamePhrase` — that's the **one** PDF method that needs updating when the refactor lands.
- **Scope estimate:** 2-3 days of focused work.
  - **Day 1**: design structured shape (`DoctorName` record or similar), update `OLISHL7Handler.getFullDocName` + add `getDoctorNameStructured()`, ensure backward-compat via the existing flat-string getter.
  - **Day 2**: update `OLISLabPDFCreator.getDoctorNamePhrase` to consume structured data; verify subscript styling preserved in PDFs.
  - **Day 3**: optional cleanup of A22/A23 Jsoup wrappers + JSP upgrade to real CSS styling.
- **Migration flexibility (A22/A23 wrappers can stay):** the Jsoup wrappers we added in `Hl7textResultsData` (A22) and `labDisplay.jsp` / `labDisplayOLIS.jsp` (A23) become **defensive no-ops** once the source returns clean text. They don't need to be removed in the refactor — they just stop doing anything useful. Choose between:
  - **Lazy migration**: just refactor the handler + PDF renderer. Leave the JSP/InboxHub wrappers alone. Cheapest path. ~3 files.
  - **Thorough migration**: also drop the now-redundant wrappers, optionally upgrade JSP rendering to use structured data + CSS for proper subscript styling on the web. ~6 files.
- **Labels:** `type: refactor`, `needs-design`, `priority: low`, `tech-debt`

---

## Track E — Verification / decision (no code, sign-off only)

### E1: Verify OLIS04.05 returns "Alternate Name 1"
- Confirm whether `OLISResultNomenclature.getName()` (`OLISHL7Handler.java:1725`) corresponds to OLIS "Alternate Name 1" vs. the standard nomenclature name
- If not, scope a physician-preferred override
- **Labels:** `type: documentation`, `priority: low`

### E2: Document OLIS04.09 first-name strictness
- `MessageUploader.willOLISLabReportMatch()` (line 462) requires first name to match in addition to the spec's HCN + Sex + DOB + Last name
- Decision: relax to spec-strict, OR document as accepted deviation for OntarioMD review
- **Labels:** `type: documentation`, `priority: low`

---

## Track F — External / coordination

### F1: OntarioMD Conformance Testing
- **Closes:** OLIS06.01
- **Scope:** schedule, prep test transcripts, run conformance suite, manage feedback loop
- **Long pole** — open this immediately; the slow loop dwarfs the code work
- **Include in ticket body:** checklist of claimed-Meets vs claimed-Partial after fixes land (this is what OntarioMD will challenge first)
- **Labels:** `type: maintenance`, `priority: high` (because of lead time, not difficulty)

---

## Track G — Local audit (this work item)

### G1: Local OLIS smoke test against current branch
- Stand up OLIS locally and exercise the major flows (Z01 patient query, Z04 preload/practitioner query, Results preview, Save / Sign-off, Forward, PDF render)
- Triage anything new into Track A bug tickets
- The previous working version was on Struts 1; expect drift bugs from the Struts 2 migration

### G2: Synthetic-fixture quirk — NTE rendering vs. Z-segment placement
- **Not a bug in OpenO.** During A1 development we built `docs/olis/sample-response-a1-rich.hl7` with NTE comment segments interleaved between OBR and ZBR (`OBR → ZBR → NTE → ...`). Those NTEs didn't render because `OLISHL7Handler.getNTELocation` walks `terser.getFinder().getRoot().getNames()` which doesn't expose nested NTEs when custom Z-segments sit between the OBR and the NTE in the wire order.
- **Real OLIS data is unaffected.** The original A1 reporter's screenshot showed `&nbsp;` Specimen Comment content rendering correctly. The user's recent lab upload also rendered comments correctly during A21 verification. Production OLIS messages put NTEs in HAPI-expected positions.
- **If a future dev hand-crafts an HL7 fixture and finds comments not rendering**, the fix is to put the NTE segments in standard ORU_R01 positions (immediately after OBR before any OBX, between OBX, or at top-of-message before any OBR) — NOT to defensively rewrite `getNTELocation`. Malformed upstream HL7 would be an OLIS escalation, not an OpenO defensive-coding task.

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
