# OLIS Requirements Analysis

---

# Section 2.1: OLIS Preload Query

---

## OLIS01.01 \- Create and Send OLIS Preload Queries On-Demand

**Requirement:** EMR must support creation and transmission of the OLIS Preload Query on an on-demand basis. Requesting HIC and Query Initiating User credentials must auto-fill. Must provide ability to set Start/End Timestamp (@OBR.22).

**Status: Meets Requirements**

**Reasoning: I**mplemented in OLISSearch2Action.java:584-664. The Z04 query is fully supported with: auto-fill of Requesting HIC credentials from logged-in provider (Search.jsp:133-135), Start/End Timestamp via OBR22 parameter (OLISSearch2Action.java:587-613), and ZRP1 segment for Requesting HIC (OLISSearch2Action.java:629-645). The UI form at Search.jsp:787-832 provides the Z04 query interface with provider selection, date range, and quantity limit options

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/OLISSearch2Action.java:584-664** \- Z04Query construction  
- **src/main/java/ca/openosp/openo/olis1/queries/Z04Query.java** \- Z04 query model  
- **src/main/webapp/olis/Search.jsp:133-135 \-** auto-fill of Requesting HIC credentials from logged-in provider  
- **src/main/webapp/olis/Search.jsp:787-832** \- Z04 UI form

## OLIS01.02 \- Preview Laboratory Information Before Saving

**Requirement:** EMR must allow users to preview laboratory information returned by OLIS Preload Query prior to permanently saving into the patient chart and/or Requesting HIC inbox. Preview must include: Patient FN/LN/DOB/Gender/HCN, Collection Date (OBR.7), Category, Test Request Name/Status (OBR.4/OBR.25), Test Result Info (OBX.3/5/6/8/11), Practitioner info, Lab Name, Matched/Unmatched status. Abnormal Flag must display without opening the report.

**Status: Closed (2026-05-19)** — was "Partially Done" until the Results.jsp preview enhancements landed.

**Reasoning (original, preserved):** Implemented via Results.jsp which displays query results in preview format before user action. Results are stored in the OLISResults table (OLISResults2Action.java:126-145) and displayed with patient name, HCN, collection date, test details, status, abnormal flags, and match status. The OLISHL7Handler.java parses all required HL7 fields for display. Gaps: (1) **Lab Name is NOT displayed as a visible preview column** — the table headers in Results.jsp:767-778 (Patient Name / Health Number / DOB / Sex / Date of Test / Discipline / Tests / Status / Results / Abnormal / Practitioners) omit Reporting Lab and Performing Lab; both values are attached only as row attributes (Results.jsp:791-793) for the filter dropdowns. (2) **Matched/Unmatched status is implicit via icon only** — Results.jsp:827-840 renders the patient name as a link when matched and adds the \`here.gif\` icon when \`demographicNo\` is null; there is no explicit text indicator of match status.

**Status Update (2026-05-19):** Both gaps are now closed. Results.jsp now exposes Reporting Lab and Performing Lab as visible columns (not just row attributes), an explicit "Matched" / "Unmatched" text column alongside the `here.gif` icon, and a Blocked column per OLIS04.10. The preview table grew from 16 to 20 columns. Verified post-deploy.

**Key Implementation Files:**

- **src/main/webapp/olis/Results.jsp \-** Preview display with all required fields  
- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:64-174 \-** Struts action method for OLIS results  
- **src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java \-** Handles parsing all required HL7 fields

## OLIS01.03 \- Filter and Sort Preload Preview

**Requirement:** EMR must filter and sort laboratory information in Preload Preview. Must filter by: Patient FN/LN/HCN, Lab Name, Category, Test Request Status (OBR.25), Test Result Status (OBX.11), Practitioner info, Abnormal Flag (OBX.8), Reporting/Performing Laboratory, Test Request Code. Must sort by all preview fields

**Status: Closed (2026-05-19)** — was "Partially Done" until the Practitioner filter was added.

**Reasoning (original, preserved):** Filter definitions in Results.jsp:230-283, filter dropdowns in \`Results.jsp:599-727. Sorting via \`sortable.js\` (line 46\) applied to table (line 746). Gap: Practitioner info filter is NOT implemented \- no dropdown exists to filter by ordering/copied-to/attending/admitting practitioner. All other required filters are present: patientFilter, hcnFilter, labFilter (Reporting), performingLabFilter, categoryFilter, testRequestStatusFilter, resultStatusFilter, abnormalFilter, testRequestCodeFilter.

**Status Update (2026-05-19):** Gap is now closed. Results.jsp has a `practitionerFilter` dropdown (lines 243, 264-265, 278) populated from `result.getAllPractitioners()` with a `practitioner="a|b|c"` row attribute and substring-match wiring in `filterResults()`. The pattern mirrors the existing multi-value Category filter, so a result with multiple practitioners (ordering, copied-to, attending, admitting) can be matched by any one of them.

**Key Implementation Files:**

- **src/main/webapp/olis/Results.jsp:230-283 \-** Filter variable definitions and logic  
- **src/main/webapp/olis/Results.jsp:599-727 \-** Filter dropdowns  
- **src/main/webapp/olis/Results.jsp:746-780 \-** Sortable table with \`sortable.js\`

## OLIS01.04  \- Act Upon Laboratory Information (Save/Sign-off/Remove)

**Requirement:** EMR must allow user to act upon lab info before permanently saving. Must provide mechanisms to: Save, Sign off & save, Remove. User must be able to select individual test requests, individual test results, or all. Lab info must persist until action taken. Save \= displays in inbox (marked or unsigned). Sign off & save \= displays in chart (marked or signed).

**Status: Closed (2026-05-19)** — was "Partially Done" until the server-side remove + manual-removal audit logging was restored.

**Reasoning (original, preserved):** UI layer presents all three mechanisms via the \`bulkProcess()\` function (Results.jsp:356-400). Each row has 3 checkboxes: (1) Add to Inbox (\`addToInbox\_\<uuid\>\`), (2) Acknowledge/Sign-off (\`acknowledge\_\<uuid\>\`), (3) Remove (\`remove\_\<uuid\>\`). User selects desired actions per row, then clicks "Process Changes" button (Results.jsp:954). Save and Sign-off are server-side functional: \`OLISAddToInbox2Action.execute()\` reads the per-item \`uuid\` / \`file\` / \`ack\` parameters (lines 56-58) and routes through \`FileUploadCheck.addFile\` and \`CommonLabResultData.fileLabs\`. Gap: **the same \`OLISAddToInbox2Action\` is invoked for Remove and ignores it** — \`execute()\` has no \`remove\` branch and the JSON \`data\` payload sent by \`bulkProcess()\` is never read. So checked Remove rows are stripped from the on-page table client-side but the underlying \`OLISResults\` row is not deleted/marked-rejected server-side, and no audit log is written for the manual removal (this is the same gap independently called out under OLIS03.04 and OLIS06.03). Results persist in \`OLISResults\` table until user action is taken.

**Status Update (2026-05-19):** Gap is now closed. `OLISAddToInbox2Action` has been restored to include `remove()`, `bulkRemove()` (line 382), `bulkProcess()` (line 463), and the `logOLISRemoval()` helper (line 664) — these methods were dropped during the December 2024 Struts 1 cleanup and have now been re-ported and adapted to Struts 2. `bulkProcess()` reads the JSON `data` payload sent from `Results.jsp` and routes each item to the correct handler. Manual-removal audit rows write the real logged-in provider (`Removing Type: User`, not "System"), distinct from the system-initiated path that still correctly writes "System (automatic)". This same fix also closes the parallel OLIS06.03 gap.

**Key Implementation Files:**

- **src/main/webapp/olis/Results.jsp:356-396 \-** \`bulkProcess()\` function that handles all three action types  
- **src/main/webapp/olis/Results.jsp:954 \-** "Process Changes" button  
- **src/main/webapp/olis/Results.jsp:799-805 \-** Individual row checkboxes for each result  
-  **src/main/java/ca/openosp/openo/olis/OLISAddToInbox2Action.java \-** Struts processing

## OLIS01.05 \- Manage Duplicate Lab Reports/Results

**Requirement:** EMR must manage duplicate lab reports/results by: automatically rejecting duplicates and/or identifying and allowing manual removal. Must provide ability to sort and filter duplicates in Preload Preview. 

**Status: Meets Requirements**

**Reasoning:** Multi-level duplicate detection in OLISResults2Action.java:122-154: (1) \`OLISResultsDao.hasExistingResult()\` checks by provider, query type, accession number; (2) \`OLISUtils.isDuplicate()\` performs hash comparison against existing labs. Duplicates are logged via \`logOLISDuplicate()\` (lines 176-226) with detailed audit info. Note: the requirement allows "automatically rejecting duplicates **and/or** identifying and allowing manual removal" — this implementation chooses auto-rejection, and Results.jsp:785-787 \`continue\`s past duplicate rows before they're rendered into the sortable/filterable table. The "must sort and filter duplicates in preview" clause is therefore not applicable in the chosen design path (duplicates are not in the preview to be sorted/filtered).

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:122-154 \-** Ensuring new OLIS result does not already exist  
- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:176-226 \-** Logs OLIS duplicate information  
- **src/main/java/ca/openosp/openo/olis/OLISUtils.java \-** Checking if OLIS lab is a duplicate or not

---

Section 2.2: OLIS Practitioner Query  
---

## OLIS02.01 \- Automated Practitioner Query (Polling)

**Requirement:** EMR must support automated creation and transmission of OLIS Practitioner Query periodically (polling) for each practitioner. Must have a default polling interval at clinic level. Must allow users to set polling intervals and Start Timestamp per practitioner. Must auto-adjust Start Timestamp to latest OBR.22 for each query.

**Status: Meets Requirements**

**Reasoning:** Implemented via Spring scheduled task. applicationContextOLIS.xml:19-30 configures \`ScheduledExecutorFactoryBean\` to run \`OLISSchedulerJob\` every 60 seconds. The job checks system preferences (start/end time window, poll frequency at OLISSchedulerJob.java:68) and calls \`OLISPollingUtil.requestResults()\` when conditions are met (OLISSchedulerJob.java:83). System-level poll frequency managed via \`OLISSystemPreferences\` table. Provider-specific timestamps tracked in \`OLISProviderPreferences\` table and auto-updated from OBR.22 response (OLISPollingUtil.java:178,245).

**Key Implementation Files:**

- **src/main/resources/applicationContextOLIS.xml:19-30 \-** Spring scheduler configuration (runs every 60s)  
- **\`src/main/java/ca/openosp/openo/olis/OLISSchedulerJob.java:34-89\` \-** TimerTask that checks poll conditions  
- **\`src/main/java/ca/openosp/openo/olis/OLISPollingUtil.java\` \-** Executes actual Z04/Z06 queries  
- **\`src/main/java/ca/openosp/openo/olis/OLISPreferences2Action.java\` \-** Struts action for configuring preferences  
- **\`src/main/java/ca/openosp/openo/olis/model/OLISSystemPreferences.java\` \-** Clinic-level poll settings  
- **\`src/main/java/ca/openosp/openo/olis/model/OLISProviderPreferences.java\` \-** Provider-specific timestamps

## OLIS02.02 \- Results Available in Inbox and Patient Chart 

**Requirement:** EMR must make lab results from Practitioner Query available in the Requesting HIC inbox and patient chart. Automated match to patient chart based on demographics. Match to inbox based on Requesting HIC professional ID. The patient's name must be identified for each result (no hover). Must allow filter/sort by patient name. Results show only once in the patient chart and once in each HIC inbox. 

**Status: Meets Requirements**

**Reasoning:** Patient matching implemented via \`MessageUploader.willOLISLabReportMatch()\` called in OLISResults2Action.java:140-143. Results linked to Requesting HIC via \`requestingHICProviderNo\` field. Results displayed with patient name visible. Filter/sort by patient available in Results.jsp. Duplicate prevention ensures single display.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:140-143**  
- **src/main/java/ca/openosp/openo/olis/OLISPollingUtil.java \-** Handles import during automated polling  
- **src/main/java/ca/openosp/openo/lab/ca/all/upload/MessageUploader.java**

## OLIS02.03 \- Configure Display of Unmatched Patient Results

**Requirement:** EMR must allow users to configure where unmatched patient lab results are displayed: in Requesting HIC inbox OR a separate place for managing unmatched results. Configuration at practitioner/provider level minimum. Must not auto-create patient charts for unmatched results.

**Status: Closed (2026-05-19)** — was "Partially Done" until the per-provider preference was added.

**Reasoning (original, preserved):** System-level configuration exists via \`filterPatients\` setting in Preferences.jsp:257-263. When enabled, unmatched results are routed as "unclaimed" (MessageUploader.java:279); when disabled, they go to the Requesting HIC inbox (line 281). Patient charts are NOT auto-created for unmatched results. Gap: Configuration is system-level only, not at the practitioner/provider level as required ("practitioner/provider level minimum").

**Status Update (2026-05-19):** Gap is now closed. `OLISProviderPreferences` has a nullable `Boolean filterPatients` field (line 37) — three-state: `null` inherits the system default, `TRUE` routes unmatched results to the unclaimed worklist, `FALSE` routes them to the provider's inbox. `provider/olis_preferences.jsp` exposes this as a three-state "Unmatched Patient Results" dropdown (lines 127-129). The polling provider's ID is threaded from `OLISPollingUtil.pollZ04Query` to `parseAndImportResponse` to the OLIS upload handler's `parse()` overload to a new `MessageUploader.routeReport` overload. A non-null provider preference wins; `null` falls back to `OLISSystemPreferences.isFilterPatients()` (preserves existing behaviour for unchanged providers). Z06 facility polls pass `null` (no single-provider context) and use the same fallback.

**Key Implementation Files:**

- **src/main/webapp/olis/Preferences.jsp:257-263 \-** "Filter out patients not in system" checkbox  
- **src/main/java/ca/openosp/openo/lab/ca/all/upload/MessageUploader.java:276-283 \-** Routing logic  
- **src/main/java/ca/openosp/openo/olis/model/OLISSystemPreferences.java:77-82 \-** System-level setting

## 

## OLIS02.04 \- Manual Practitioner Query Submission

**Requirement:** EMR must allow users to manually submit practitioner queries for: single Requesting HIC, group of HICs, or all HICs. Manual run must not affect default interval. EMR users must be identified in the ZSH segment.

**Status: Meets Requirements**

**Reasoning:** Manual Z04 query submission via Search.jsp:787-832 and OLISSearch2Action.java:584-664. Supports multiple HIC selection at OLISSearch2Action.java:902-914 (loops and calls \`Driver.submitOLISQuery\` once per HIC). Manual queries don't affect system polling intervals (separate execution path). User identification in the ZSH segment is handled centrally in \`olis1/segments/ZSHSegment.java\` and built into every OLISMessage (not at the cited Z04 construction lines).

**Key Implementation Files:**

- **src/main/webapp/olis/Search.jsp:787-832 \-** Form for Z04 query submission  
- **src/main/java/ca/openosp/openo/olis/OLISSearch2Action.java:584-664 \-** Struts action logic for Z04 query type  
- **src/main/java/ca/openosp/openo/olis/OLISSearch2Action.java:902-914 \-** Multi-HIC submission loop  
- **src/main/java/ca/openosp/openo/olis1/segments/ZSHSegment.java \-** ZSH segment construction for user identification

Section 2.3: OLIS Patient Query  
---

## OLIS03.01 \- Create and Send Patient Query On-Demand

**Requirement:** EMR MUST be able to support the creation and manual transmission of OLIS patient queries on an on-demand basis *for each provider*. 

**Status: Meets Requirements**

**Reasoning:** The first guideline is that the OLIS query must be executed from the patient chart, *without* a separate login. Open-O meets this guideline, as the query can be accessed and executed directly from the eChart (found in Demographic.jsp). The second requirement is that the HIC credentials and patient identities MUST automatically be filled into the OLIS patient query. The patient credentials are auto-filled in when the patient chart is opened via the search bar (in Search.jsp), the provider credentials are auto-filled from the cached log-in information. Open-O meets the third requirement of providing the ability to enter a start and end timestamp in OLISSearch2Action.java. The EMR meets the fifth requirement of providing the following filters: ordering practitioner, copied-to practitioner, attending practitioner, admitting practitioner, reporting laboratory, performing laboratory, and test request code/status. These filters are applied to the query in Search.jsp. The EMR meets guideline 6 by allowing users to input parameters using text input and/or multi-select UI elements. 

Open-O also implements several guidelines that “may” be implemented like viewing the test result code and specimen collector. Viewing the destination laboratory is only available for Z05 queries, not Z01.

**Key Implementation Files:**

- **src/main/webapp/olis/Search.jsp**  
- **src/main/webapp/lab/DemographicLab.jsp**  
- **src/main/java/ca/openosp/openo/olis/OLISSearch2Action.java**

##  OLIS03.02 \- Preview Patient Query Results Before Saving

**Requirement:** EMR must allow preview of Patient Query results before permanently saving into the patient chart AND in the Requesting HIC inbox/work queue. Provider-specific, accessible only by Requesting HIC.

**Status: Closed (2026-05-19)** — was "Partially Done" until the Results.jsp preview enhancements landed.

**Reasoning (original, preserved):** Same Results.jsp preview implementation as Preload Query. Results are provider-specific via \`requestingHICProviderNo\` filtering. Same gaps as OLIS01.02: (1) Lab Name NOT displayed as visible column; (2) Match status implicit via icon only.

**Status Update (2026-05-19):** Same fix as OLIS01.02 — Patient Query and Preload Query share `Results.jsp`. See the OLIS01.02 Status Update for details.

**Key Implementation Files:** 

- **src/main/webapp/olis/Results.jsp**  
- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java**

## OLIS03.03 \- Filter and Sort Patient Query Preview

**Requirement:** EMR must filter and sort Patient Query Preview. Must be able to: (a) Filter on ALL preview fields from OLIS03.02, (b) Sort by ALL preview fields. Must auto-populate filters based on returned data.

**Status: Closed (2026-05-19)** — was "Partially Done" until the Practitioner filter was added.

**Reasoning (original, preserved):** Same filter/sort implementation as Preload Query in Results.jsp. Filter dropdowns dynamically populated. Same gap as OLIS01.03: Practitioner info filter NOT implemented \- no dropdown to filter by practitioner.

**Status Update (2026-05-19):** Same fix as OLIS01.03 — Patient Query and Preload Query share `Results.jsp`. See the OLIS01.03 Status Update for details.

**Key Implementation Files:**

- **src/main/webapp/olis/Results.jsp:240-283, 599-727**

## OLIS03.04 \- Act Upon Patient Query Results

**Requirement:** EMR must allow users to act upon Patient Query results before permanently saving. EMR MUST provide a mechanism to: (a) Save, (b) Sign off & save, (c) Remove. User MUST be able to select: individual test results, individual test requests, or all. Lab info MUST persist until action is taken. Save \= displays in inbox AND chart (marked unsigned). Sign off & save \= displays in chart only (marked signed-off).

**Status: Closed (2026-05-19)** — was "Partially Done" until the server-side remove + manual-removal audit logging was restored.

**Reasoning (original, preserved):** UI layer (Results.jsp) provides three checkboxes per row and the \`bulkProcess()\` JavaScript function. However, \`OLISAddToInbox2Action.java\`has no handling for the \`remove\` operation. The Struts action only handles single-item add-to-inbox via \`uuid\`, \`file\`, and \`ack\` parameters \- it should probably route based on the \`method\` parameter (not required but might help the code flow analysis) sent by the UI.

**Status Update (2026-05-19):** Same fix as OLIS01.04 — Patient Query and Preload Query share the same `OLISAddToInbox2Action`. See the OLIS01.04 Status Update for details.

**Key Implementation Files:**

- **src/main/webapp/olis/Results.jsp:356-400\` \-** bulkProcess() function  
- **src/main/webapp/olis/Results.jsp:799-805 \-** Individual row checkboxes  
- **src/main/java/ca/openosp/openo/olis/OLISAddToInbox2Action.java \-** Server-side processing

## 

## OLIS03.05 \- Manage Duplicates from Patient Query

**Requirement:** EMR must manage duplicate lab reports/results from Patient Query. Auto-reject and/or allow manual removal. Must sort and filter duplicates.

**Status: Meets Requirements**

**Reasoning:** Same duplicate detection as OLIS01.05 applies to all query types including Z01 Patient Query. As noted under OLIS01.05, duplicates are automatically rejected at Results.jsp:785-787 (the row is skipped via \`continue\` before rendering), so they are never present in the sortable/filterable preview table. The requirement allows auto-rejection as an acceptable strategy; the sort/filter clause is satisfied vacuously under this design path. 

**Key Implementation Files:**

- **Same as OLIS01.05**

## OLIS03.06 \- Override Patient Consent Directive

**Requirement:** EMR must allow override of patient consent directive to see blocked results. Must: visually indicate blocked results, allow override or accept as-is from the user, indicate consent from patient or substitute decision-maker. Must log: when this occured, who initiated the event, requesting HIC, patient/SDM authorization on the event, the EMR transaction ID, and the OLIS transaction ID.

**Status: Closed (2026-05-19)** — was "Partially Done" until the OLIS Transaction ID logging was added.

**Reasoning (original, preserved):** Override functionality implemented in OLISSearch2Action.java:186-224 (redo with force override). Audit log captures most required fields via \`OscarLog\` at lines 196-221. The Results.jsp:498-509 provides UI for consent override selection (patient/substitute). Gap: OLIS Transaction ID NOT logged \- logging happens at line 221 (persist) BEFORE the query is submitted at line 224, so the OLIS Transaction ID from the response cannot be captured. **Note:** this gap shares a root cause with OLIS06.02 — both stem from \`OLISQueryLog\` lacking an \`olisTransactionId\` field and the audit-log write happening before \`Driver.submitOLISQuery(...)\` returns. A single fix (add the field + write the log after the response, or split into SENT + RECEIVED rows keyed by \`uuid\`) closes both gaps.

**Status Update (2026-05-19):** Gap is now closed. `OLISQueryLog.olisTransactionId` field added (`src/main/java/ca/openosp/openo/commn/model/OLISQueryLog.java:65`). `Driver.extractOlisTransactionId()` reads MSA-2 from the ERP response via `OLISHL7Handler.getMsaControlId()`; `Driver.recordOlisTransactionId()` writes it back onto the SENT `OLISQueryLog` row post-response (Driver.java:174-175). The consent-override path in `OLISSearch2Action` was reordered so `Driver.submitOLISQuery` runs *before* the `OscarLog` consent-override row is persisted; the row's data block now appends `OLIS Transaction ID: <id>` read from the `olisTransactionId` request attribute. This same fix also closes the parallel OLIS06.02 gap.

**Important framing note:** the OLIS Interface Specification defines no OLIS-minted transaction ID. There is only the **Message Control ID (MSH-10)** set by the *initiating* EMR, and the HIAL envelope's Client Transaction ID which the spec mandates equal MSH-10. The query response (ERP) echoes that value back in **MSA-2** (§10.2.5.12.2.3). So what we log as "OLIS Transaction ID" is the request's Message Control ID *as acknowledged by OLIS in the response* — the correlation key for the exchange. Worth confirming during conformance review that this satisfies the spec's "OLIS transaction ID" language.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/OLISSearch2Action.java:186-224, 308-319**  
- **src/main/webapp/olis/Results.jsp:498-509**

Section 2.4: EMR Lab Management  
---

## OLIS04.01 \- Indicate Lab Test Request Status

**Requirement:** EMR must indicate lab test request status: F (Final), A (Some results available), P (Preliminary), C (Correction). Must be available in: Patient chart, Provider inbox, Patient Query Preview, Preload Query Preview. No hover for info.

**Status: Meets Requirements**

**Reasoning:** Test request status (OBR.25) parsed by \`OLISHL7Handler.java\` and displayed in Results.jsp. The \`getTestRequestStatuses()\` method (line 397\) iterates the OBR segments and extracts request status values; \`getTestRequestStatusMessage()\` (line 1363\) converts the single-character status codes (F/A/P/C/etc.) to display labels. Status codes are shown in preview and propagated to inbox/chart after import.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java:397 \-** \`getTestRequestStatuses()\`  
- **src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java:1363 \-** \`getTestRequestStatusMessage()\` code-to-label conversion  
- **src/main/webapp/olis/Results.jsp**

## OLIS04.02 \- Indicate Lab Test Result Status

**Requirement:** EMR must indicate lab test result status: F (Final), P (Preliminary), C (Correction). Must be available in the same locations as OLIS04.01.

**Status: Meets Requirements**

**Reasoning:**  OBX.11 (result status) parsed by OLISHL7Handler and displayed. The \`getTestResultStatuses()\` method (line 358\) iterates each OBX in each OBR and reads field 11 (\`getOBXField(x, y, 11, 0, 1)\`); \`getTestResultInfo()\` (line 373\) emits the same status alongside name/value/units/abnormal for per-row rendering. Same visibility as test request status.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java:358-371 \-** \`getTestResultStatuses()\`  
- **src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java:373-395 \-** \`getTestResultInfo()\` (per-OBX status passthrough)

## OLIS04.03 \- Provide List of Participating Laboratories

**Requirement:** EMR must provide a list of participating laboratories for query parameters: Reporting Laboratory (@ZBR.4), Exclude Reporting Laboratory (@ZBE.4), Performing Laboratory (@ZBR.6), Exclude Performing Laboratory (@ZBE.6). 

**Status: Closed (2026-05-20)** — was "Partial (2026-05-19)"; the full Ontario roster is now DB-backed and refreshable from the canonical Lab/SCC Extract.

**Reasoning (original, preserved):** Hard-coded lab list in Search.jsp:377-403 includes Gamma-Dynacare (5552), CML (5407), LifeLabs (5687). Gap: This is not a comprehensive list of all participating laboratories \- only 3 major labs are listed. The requirement seems to imply maintaining an updateable list from OLIS.

**Status Update (2026-05-20):** Both the completeness gap and the maintainability concern are now closed via a DB-backed roster table plus an admin XLSX importer that mirrors the nomenclature-importer pattern shipped earlier in this branch.

**Implementation:**

1. **New `OLISFacility` table** holds the full Ontario roster: 273 licensed Laboratories (OID `2.16.840.1.113883.3.59.1`) + 994 Specimen Collection Centres (OID `2.16.840.1.113883.3.59.2`). Single table with a `facilityClass` discriminator column ('LAB' / 'SCC') since the source extract is one sheet with one row shape and only the OID column distinguishes the two classes. Natural key: `(facilityClass, licenceNumber)`.
2. **New admin importer** at *Admin → OLIS — Import Lab/SCC Roster* (action `OLISFacilityImport2Action`, JSP `olis/FacilityImport.jsp`, `_admin/w` gate). Parses the official Lab/SCC Extract XLSX with stdlib zip + SAX (no Apache POI), upserts on the natural key, and pre-marks every existing row INACTIVE before the upsert so rows absent from the new extract decay automatically. Reports added/updated counts per class.
3. **New AJAX picker endpoint** `/olis/FacilitySearch.do` (action `OLISFacilitySearch2Action`) returns the top 25 active matches as JSON `{licence, name, city, facilityClass}` for case-insensitive substring search. Each picker passes `?class=LAB|SCC|ANY` based on which OLIS query parameter it maps to.
4. **All 8 `OLISParticipatingLab` dropdowns in `Search.jsp` plus the 2 in `provider/olis_preferences.jsp` converted to jQuery UI typeahead pickers** (input + hidden + chip strip). Field-to-class mapping: Specimen Collector → SCC; Reporting/Exclude Reporting/Performing/Exclude Performing/Destination/Test Request Placer → LAB; Ordering Facility → ANY. Saved user preferences pre-render as a chip on page open.
5. **`OLISParticipatingLab` enum deleted.** The hard-coded 3 entries (and the misattributed CML entry — see below) are gone; the data source is now the canonical eHealth Ontario extract.

**Silent bug found and fixed in passing:** the deleted enum's `CML("5407", "CML")` entry was wrong. Licence 5407 in the official extract is LifeLabs Mississauga, not CML. The real CML HealthCare Inc. is licence 3855, classified as a Specimen Collection Centre (OID `…59.2`), not a Laboratory. Any user who picked "CML" in the old dropdown was actually filtering against LifeLabs Mississauga. The bug self-resolves with the enum deletion — the canonical roster has both rows under the correct facility names.

**Spec match — verbatim:**

| Spec clause | Status | Evidence |
|---|---|---|
| List for Reporting Laboratory (@ZBR.4) | Closed | LAB-class typeahead on `Search.jsp` Reporting Laboratory field; 273 active Laboratory rows. |
| List for Exclude Reporting Laboratory (@ZBE.4) | Closed | LAB-class typeahead on `Search.jsp` Exclude Reporting Laboratory field. |
| List for Performing Laboratory (@ZBR.6) | Closed | LAB-class typeahead on `Search.jsp` Performing Laboratory field. |
| List for Exclude Performing Laboratory (@ZBE.6) | Closed | LAB-class typeahead on `Search.jsp` Exclude Performing Laboratory field. |
| List comes from OLIS | Closed | Source is the canonical eHealth Ontario Lab/SCC Extract at <https://ehealthontario.on.ca/en/support/lab-results/olis-whats-new/olis-client-support>; importer is admin-gated and rerunnable on each release. |

**Deployment note:** the schema migration in `database/mysql/updates/update-2026-05-20-olis-facility.sql` must be applied before redeploy. The table is empty on fresh deploy — the pickers will return no suggestions until an admin runs the importer once with a current Lab/SCC Extract. The fresh-install seed in `olisinit.sql` carries the same `CREATE TABLE` so new installs land with the table present (still empty until imported).

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/model/OLISFacility.java** — new entity
- **src/main/java/ca/openosp/openo/olis/dao/OLISFacilityDao.java** — upsert lookup + autocomplete + batch deprecate
- **src/main/java/ca/openosp/openo/olis/OLISFacilityImport2Action.java** — XLSX importer (mirrors `OLISNomenclatureImport2Action`)
- **src/main/java/ca/openosp/openo/olis/OLISFacilitySearch2Action.java** — AJAX endpoint for the pickers
- **src/main/webapp/olis/FacilityImport.jsp** — admin upload form + report view
- **src/main/webapp/olis/Search.jsp** — 8 dropdowns converted to typeahead pickers
- **src/main/webapp/provider/olis_preferences.jsp** — 2 dropdowns converted to typeahead pickers
- **database/mysql/updates/update-2026-05-20-olis-facility.sql** — schema migration
- **database/mysql/olis/olisinit.sql** — fresh-install `CREATE TABLE` (append)

## OLIS04.04  \- Manage Duplicate Lab Reports/Results

**Requirement:** EMR must manage duplicates in: Preload Preview, Patient Query Preview, Requesting HIC inbox, Patient chart. Duplicates defined as: same result via multiple channels (Commercial Labs \+ OLIS), same result for different HICs in the same clinic, same result already in EMR. Results show only once in the chart and once per HIC inbox. Must log duplicate handling per OLIS06.03. 

**Status: Meets Requirements**

**Reasoning:** Multi-level duplicate detection handles all scenarios: \`hasExistingResult()\` checks provider/query/accession, \`isDuplicate()\` uses hash comparison against existing labs including those from commercial lab feeds. Logging via \`logOLISDuplicate()\` includes all required audit fields.

**Key Implementation Files:**  
**\- src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:122-154, 176-226**

## OLIS04.05 \- Display Alternate Test Name (Not LOINC)

**Requirement:** EMR must display alternate, meaningful test name instead of LOINC. Must cross-reference to: Alternate Name 1 from OLIS Nomenclature, or locally mapped name preferred by physician. 

**Status: Closed (2026-05-19)** — was "Partially Done" pending verification; verified as Alternate Name 1.

**Reasoning (original, preserved):** Display name mapping via OLIS Nomenclature IS implemented. \`OLISHL7Handler.getOBXName()\` (lines 1718-1731) looks up the OBX identifier in \`OLISResultNomenclatureDao\` and returns \`resultNomenclature.getName()\` instead of the raw LOINC code; falls back to parsing the OBX-3 second component if no match. \`Results.jsp:905-908\` calls the same DAO directly to render \`orn.getName()\` per result row. Category mapping uses \`OLISRequestNomenclature\` similarly at \`OLISHL7Handler.java:1705-1709\`. Gap: There is no evidence of a "locally mapped name preferred by physician" override on top of the OLIS Nomenclature mapping — the requirement allows either as an OR, so this still likely meets the spec; needs confirmation that the \`getName()\` value corresponds to "Alternate Name 1" specifically (vs. the standard nomenclature name).

**Status Update (2026-05-19):** Verified. Evidence chain:

1. **OLIS Interface Specification §6.7.1.2** (`OLIS_Interface_Specifications_EN.pdf` line 1937): *"The OLIS Test Result Nomenclature includes a field named Alternate Name 1 that contains a suggested display name for each test result code. This data may be used as a starting point for selecting preferred test result names."*
2. **CSV seed inspection** (`database/mysql/olis/OLISTestResultNomenclature.csv`): the `name` column holds short display-friendly forms (`14749-6 → Glucose`, `2160-0 → Creatinine`). The LOINC Fully Specified Name for these codes is `Glucose:MCnc:Pt:Ser/Plas:Qn` and `Creatinine:MCnc:Pt:Ser/Plas:Qn`, confirming our `name` column is the Alternate Name 1 field, not the Fully Specified Name.
3. Whoever originally imported the OLIS distribution pre-filtered the source XLSX to `(LOINC code, Alternate Name 1)`. The admin nomenclature importer keeps this mapping (it still reads the "Result Alternate Name 1" column into `name`).

Conformance posture: positive. The EMR returns Alternate Name 1, which is the spec-recommended starting point, via `OLISResultNomenclature.getName()`. No physician-preferred override needed.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java:1705-1731 \-** Result/category name lookup against nomenclature DAOs  
- **src/main/webapp/olis/Results.jsp:905-908 \-** Renders \`OLISResultNomenclature.getName()\` per row  
- **src/main/java/ca/openosp/openo/olis/model/OLISRequestNomenclature.java**  
- **src/main/java/ca/openosp/openo/olis/model/OLISResultNomenclature.java**

## OLIS04.06 \- Act Upon Unmatched Patient Results

**Requirement:** EMR must allow the user to: match unmatched results to existing EMR patients, manage remaining unmatched results. Must be available for unmatched in Preload/Patient Query Preview and Practitioner Query results. Must preserve original OLIS demographics accessible from the UI.

**Status: Closed (2026-05-19)** — was "Partially Done"; verified against the verbatim spec text from the original OntarioMD Requirements PDF.

**Reasoning (original, preserved):** Results are stored with \`demographicNo\` when matched (OLISResults2Action.java:140-143). A general manual-match UI does exist via the MDS lab inbox flow: unmatched labs route through \`OpenEChart.jsp\` → \`oscarMDS/PatientSearch.jsp\` (form action \`PatientMatch.do\`, line 183\) → \`PatientMatch2Action\` → \`CommonLabResultData.updatePatientLabRouting()\`. Gap: This flow is part of the shared MDS lab inbox rather than an OLIS-specific workflow built into the OLIS Preview / Practitioner Query results screens, and there is no dedicated UI that shows the original OLIS demographics from the HL7 message side-by-side with EMR demographics during manual matching.

**Status Update (2026-05-19):**

**On the original Reasoning's "side-by-side during manual matching" gap:** after parsing the verbatim spec text from the original OntarioMD Requirements PDF, this was an extrapolation. The literal requirement says *preserve* OLIS demographics and make them *accessible from the EMR UI*, with no qualifier about during-matching or side-by-side display. The linguistic pattern matches "compile process must preserve source comments accessible from the output" — the predicate is *don't destroy + remain readable*, not *display during the operation*. The strictest constraint in the sentence is the "(all)" qualifier — preserve every PID field, not just name and HIN.

**Two-part regression discovered during verification, fixed in this branch:**

1. **`SearchPatient2Action` dropped `from=olis1` during the redirect.** Without it, the popup installed the legacy `updateLabDemoStatus(t1)` callback instead of the OLIS-aware `updateLabDemoStatus2(t1, t2)`, so `saveMatch` (which writes `OLISResults.demographicNo`) never fired. Fixed: `SearchPatient2Action.java:113, 148-150` now read and propagate `from`, OWASP-encoded.

2. **Struts 6.8.0's defaultStack `coop` interceptor at `mode=same-origin` was severing `window.opener` for popups since the 2026-03-15 Jakarta EE migration.** The opener (a Struts2 action) sent COOP `same-origin`; the popup target (PatientSearch.jsp, a raw JSP) sent none. The browser put them in separate browsing-context groups, so `window.opener` was null in the popup and `updateOpener`'s call to `window.opener.updateLabDemoStatus2(...)` threw silently. Fixed: `struts.xml:67` `mode` changed to `same-origin-allow-popups`. The global fix likely heals other popup-callback flows in the EMR too.

**Clause-by-clause verification against verbatim spec:**

| Spec clause | Status | Evidence |
|---|---|---|
| Match unmatched lab reports/results to an existing EMR patient | Closed | `OLISAddToInbox2Action.saveMatch()` line 300; verified end-to-end (OLISResults.demographicNo went NULL → 1274 in a recorded test). |
| Manage remaining unmatched patient lab reports/results | Closed | Unclaimed worklist plus the per-provider `OLISProviderPreferences.filterPatients` routing (see OLIS02.03). |
| Matching available for Preload Preview / Patient Query Preview | Closed | `Results.jsp:872-874` shows the `here.gif` match icon on every Unmatched row, opening `oscarMDS/SearchPatient.do?from=olis1`. |
| Matching available for Practitioner Query results | Closed | Practitioner-query polling imports labs directly to `hl7TextInfo` and `patientLabRouting`; match via the standard inbox flow. No OLIS-side state to sync because no `OLISResults` row exists for these imports — the inbox storage *is* the Practitioner Query result UI. Verified end-to-end (lab 176 went `demographic_no=0 → 1274` in a recorded test). |
| Matching preserves original patient demographics (all) accessible from UI | Closed | Raw HL7 is base64-stored in `hl7_text_message` and re-parsed on every render. Every PID field surfaces through `labDisplayOLIS.jsp`, the Results.jsp preview pane, and the PDF render. Matching never substitutes matched-EMR demographics for OLIS-side values. Verified: lab 176 matched to demographic 1274 (Aleshia Jones in the EMR) still renders the original PID name "FITCASE A TESTPATIENT" with HIN 9999999998. |

**When to revisit:** if the conformance review explicitly asks for OLIS-side demographics to appear in the match popup itself (the original assessor's stricter interpretation, not the literal spec text), build a `from=olis1`-gated info panel at the top of `PatientSearch.jsp` that re-fetches the OLIS row by UUID, re-parses the HL7, and renders an OLIS-side PID block above the EMR candidate list — roughly an 80-line JSP change plus a small backend thread-through. Until then, the implementation literally satisfies the spec as written and no conformance risk is identified.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:140-145**  
- **src/main/java/ca/openosp/openo/mds/pageUtil/PatientMatch2Action.java \-** Manual match action  
- **src/main/webapp/oscarMDS/PatientSearch.jsp:183 \-** Manual-match form (\`PatientMatch.do\`)

## OLIS04.07 \- Integrated OLIS Interface

**Requirement:** EMR user interface to OLIS must be integrated with EMR. Requiring separate login to a different system will not meet the requirement.

**Status: Meets Requirements**

**Reasoning:** OLIS functionality is fully integrated within OpenO EMR. Users access OLIS queries via JSP files, which call struts backend logic  \`/olis/Search.do\` and results via \`/olis/Results.do\`, etc, within the same authenticated EMR session. No separate login required.

**Key Implementation Files:**  
**\- src/main/webapp/olis/Search.jsp**  
**\- src/main/webapp/olis/Results.jsp**  
**\- struts.xml \-** Has configuration for OLIS struts actions

## OLIS04.08 \- Maintain Query Parameter Lists

**Requirement:** EMR must maintain a list of values for each query parameter (provided by OLIS) for: Preload Query, Practitioner Query, Patient Query. 

**Status: Meets Requirements**

**Reasoning:** \`OLISRequestNomenclature\` and \`OLISResultNomenclature\` tables store test request and result codes. DAOs provide retrieval (\`OLISRequestNomenclatureDao\`, \`OLISResultNomenclatureDao\`). Lists populated in Search.jsp:306-311 and displayed in dropdowns (lines 627-651). Database initialization scripts in \`database/mysql/olis/\` provide initial data. Note: the lists are maintained via manual updates of the \`OLISTestRequestNomenclature.csv\` / \`OLISTestResultNomenclature.csv\` seed files re-run against the database, which means there's no automated refresh from OLIS — operationally this could be a candidate for follow-up work to pull updates programmatically.

**Key Implementation Files:**  
**\- src/main/java/ca/openosp/openo/olis/dao/OLISRequestNomenclatureDao.java**  
**\- src/main/java/ca/openosp/openo/olis/dao/OLISResultNomenclatureDao.java**  
**\- src/main/webapp/olis/Search.jsp:306-311, 627-651**

## OLIS04.09 \- Automated Patient Matching

**Requirement:** EMR must support automated matching of lab results to EMR patient charts based on demographics. Matched \= HCN \+ Gender \+ DOB \+ Last name all match. Unmatched \= at least one doesn't match OR patient doesn't exist in the EMR. 

**Status: Meets Requirements**

**Reasoning:** Implemented via \`MessageUploader.willOLISLabReportMatch()\` (signature at MessageUploader.java:494) called in OLISResults2Action.java:140-143. The matching SQL keys on \`hin\` + \`last_name\` + \`year/month/date_of_birth\` + \`sex\` — exactly the spec's four criteria (HCN + Gender + DOB + Last name). \`firstName\` is passed into the method but is **not** referenced in the query, so a record matching the spec's four fields is matched regardless of first name. The implementation is spec-exact, not a deviation. *(Correction, 2026-05-14 deep-dive audit: an earlier draft of this analysis claimed the matcher was "stricter than spec" by additionally requiring first name — that was wrong; first name plays no part in the match. See \`deep-dive-findings.md\` §3a.)*

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:140-143**  
- **src/main/java/ca/openosp/openo/lab/ca/all/upload/MessageUploader.java**

## OLIS04.10 \- Identify Blocked Lab Reports/Results

**Requirement:** EMR must identify lab reports/results blocked due to patient consent directive. Must identify as sensitive/blocked in: Preload Preview, Patient Query Preview, and once permanently saved. Blocked status must be visible to all who can review. 

**Status: Closed (2026-05-19)** — was "Partially Done" until both the per-row indicator and the detection-path hardening landed.

**Reasoning (original, preserved):** OLISHL7Handler.java:257-259 provides \`isReportBlocked()\` to identify blocked reports from the parsed ZPD segment. \`OLISResults2Action.java:476-490\` aggregates this into a page-level \`hasBlockedContent\` flag, and \`Results.jsp:496-509\` surfaces a "Submit Override Consent" form when that aggregate flag is true. Gap: this is a **page-level** indicator (any-result-blocked across the batch), not a **per-row** indicator. The preview table column headers (Results.jsp:767-778: Patient Name / HCN / DOB / Sex / Date of Test / Discipline / Tests / Status / Results / Abnormal / Practitioners) include no Blocked column or icon to mark which specific result is sensitive/blocked. The spec requires identification at the individual report/result level visible to all reviewers, which the current page-level override prompt does not satisfy on its own. Permanent-save propagation of blocked status was not separately verified.

**Status Update (2026-05-19):** Both pieces are now closed.

1. **Per-row Blocked column** added to Results.jsp. Driven by `result.isReportBlocked()` (using `Boolean.TRUE.equals(...)`, null-safe). The existing page-level override prompt is unchanged; the column adds the per-row indication the spec requires.

2. **Detection fragility closed** (commit `3161c1fd50`). Previously `OLISHL7Handler.init()` walked `terser.getFinder().getRoot().getNames()`, which doesn't expose `ZPD` segments nested inside typed message structures — so blocked detection was conditional on the message resolving as `GenericMessage`. Real OLIS `ERP` responses always do, so production was unaffected, but the fragility was real. Fix: a custom `ModelClassFactory` in `OLISHL7Handler` forces every parsed message to resolve as `GenericMessage` regardless of MSH-9-3 (`src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java:160-174`). All segments now land flat at the message root, making the existing root-level walk correct by construction. The same fix also closes the NTE-segment rendering quirk identified in the same area.

Permanent-save propagation is by design: the raw HL7 (ZPD included) is base64-stored in `hl7_text_message` and re-parsed on every view (`labDisplayOLIS.jsp`, `OLISLabPDFCreator`, Results.jsp preview), so blocked status survives implicitly. **Side note (not a conformance gap):** there is no fast-query path for "show me all blocked labs" — it would require a base64-decode plus re-parse scan. Not a spec requirement; just an operational note.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java:257-259 \-** \`isReportBlocked()\`  
- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:476-490 \-** Aggregate \`hasBlockedContent\` flag  
- **src/main/webapp/olis/Results.jsp:496-509 \-** Page-level override-consent form (no per-row indicator)

Section 2.5: Error Management  
---

## OLIS05.01 \- Manage HL7 Error Messages

**Requirement:** EMR must manage HL7 error messages: provide notification with details, deliver to applicable recipients (user, admin). Must handle failures gracefully. 

**Status: Meets Requirements**

**Reasoning:** Error handling in Driver.java:253-275 parses HL7 error responses from OLIS. Errors extracted from \`ArrayOfError\` structure and displayed to the user via \`request.setAttribute("errors", errorStringList)\`. Admin notification via \`notifyOlisError()\` (lines 444-475). Application continues operating after errors. 

**Key Implementation Files:** 

- src/main/java/ca/openosp/openo/olis1/Driver.java:253-275, 444-475

## OLIS05.02 \- Manage XML Error Messages

**Requirement:** EMR must manage XML error messages with notification and delivery to applicable recipients (user, admin). Must handle failures gracefully. 

**Status: Meets Requirements**

**Reasoning:** XML error parsing in Driver.java:203-283. The \`readResponseFromXML()\` method handles XML structure including error elements. Exception handling ensures application stability.

**Key Implementation Files:** 

- src/main/java/ca/openosp/openo/olis1/Driver.java:203-283

## OLIS05.03 \- Manage SOAP Error Messages

**Requirement:** EMR must manage SOAP error messages with notification and delivery to applicable recipients (user, admin). Must handle failures gracefully.

**Status: Meets Requirements**

**Reasoning:** SOAP communication via OLISStub handles SOAP faults. The generic \`catch (Exception e)\` block in Driver.java:192-200 wraps the \`olis.oLISRequest(olisRequest)\` call at line 173, so SOAP faults flow through it: errors are logged via \`MiscUtils.getLogger().error(...)\`, set on the request as \`searchException\`, and admin/provider is notified via \`notifyOlisError()\`. Application returns gracefully.

**Key Implementation Files:** 

- src/main/java/ca/openosp/openo/olis1/Driver.java:192-200

## OLIS05.04 \- Network Error Management

**Requirement:** EMR must provide notification about network errors affecting retrieval. Must be able to retry and re-establish connectivity without human intervention.

**Status: Meets Requirements**

**Reasoning:** Network error notification implemented via exception handling and \`notifyOlisError()\`. Automatic retry without human intervention is provided by the scheduled poller (\`OLISSchedulerJob\`), which will retry failed queries on the next polling interval. The OntarioMD requirement specifies retry "at a later time" which the scheduled polling mechanism satisfies. No immediate retry within the same request, but this is not explicitly required by the specification. 

**Key Implementation Files:** 

- src/main/java/ca/openosp/openo/olis1/Driver.java:192-200  
- src/main/java/ca/openosp/openo/olis/OLISSchedulerJob.java \- Automatic retry via scheduled polling

Section 2.6: Audit/Certification/Other  
---

## OLIS06.01 \- OLIS Conformance Testing

**Requirement:** EMR must have successfully passed OLIS Conformance Testing.

**Status: External — pending OntarioMD conformance review (no change)**

**Reasoning (original, preserved):** This is a certification status requirement, not a code implementation requirement. Cannot be verified through code analysis \- requires documentation of OLIS conformance testing completion

**Status Update (2026-05-19):** No change — this is the OntarioMD conformance-review coordination item. Intentionally deferred external coordination; nothing actionable in code. The branch's local items are now at the point where conformance review is the next logical step.

## OLIS06.02 \- Log All Messages Sent/Received

**Requirement:** EMR must log all messages sent to/received from OLIS. Log must include: Transaction timestamp, Transaction type, Initiating User, Requesting HIC, External system (OLIS), EMR transaction ID, OLIS transaction ID.

**Status: Closed (2026-05-19)** — was "Partially Done" until the OLIS Transaction ID logging was added.

**Reasoning (original, preserved):** Logging in Driver.java:138-160 captures SENT messages only. Logged fields: Transaction timestamp (queryExecutionDate), Transaction type (queryType), Initiating User (initiatingProviderNo), Requesting HIC (requestingHIC), EMR transaction ID (uuid). Gaps: (1) OLIS Transaction ID is NOT logged \- no field exists in \`OLISQueryLog\` model and it's not extracted from the response; (2) Received messages are NOT logged \- logging happens before query submission at line 173, response is processed but not audit-logged. **Note:** OLIS03.06 has the same root cause — the audit row is persisted before \`Driver.submitOLISQuery(...)\` returns, so the response-side Transaction ID is unreachable. A single fix (add \`olisTransactionId\` to \`OLISQueryLog\` + write the log post-response, or split into SENT + RECEIVED rows keyed by \`uuid\`) closes both gaps.

**Status Update (2026-05-19):** Both gaps from the original Reasoning are addressed and the requirement is met as written.

**Original Gap 1 (OLIS Transaction ID not logged):** closed. Added `olisTransactionId` field to `OLISQueryLog` (line 65). The SENT row is persisted pre-submit (so a failed call still leaves an audit trail), then updated in place with the OLIS Transaction ID once the response arrives. `Driver.extractOlisTransactionId()` reads MSA-2 from the ERP response; `Driver.recordOlisTransactionId()` writes it back onto the same `OLISQueryLog` row keyed by `uuid`.

**Original Gap 2 (Received messages not logged):** closed. Both directions of the OLIS exchange are now captured — the request-side data (timestamp, type, user, HIC, EMR transaction ID) is persisted when the request is built; the response-side data (the OLIS Transaction ID acknowledged in MSA-2) is added to the same row when the response arrives. The two writes share a row keyed by `uuid` rather than producing two separate rows.

**Design choice flagged for awareness, not a gap:** the spec text says *"log all messages sent to/received from OLIS"* with a required field list. It enumerates fields, not row structure. We chose to record one row per OLIS conversation (sent + received correlated by `uuid`) rather than two separate rows. All required fields are captured either way. A strict reading might prefer two rows; if conformance review surfaces that, the schema and code paths can be split without changing the application logic. Worth a note during review but not a blocker.

**Important framing note (carried from OLIS03.06):** the OLIS Interface Specification defines no OLIS-minted transaction ID. What we log as "OLIS Transaction ID" is the request's Message Control ID (MSH-10) as acknowledged by OLIS in the ERP response's MSA-2. Worth confirming during conformance review that this satisfies the spec's "OLIS transaction ID" language.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis1/Driver.java:138-160 \-** Sent message logging only  
- **src/main/java/ca/openosp/openo/commn/model/OLISQueryLog.java \-** Missing \`olisTransactionId\` field

## OLIS06.03 \- Log Removed/Rejected Lab Reports

**Requirement:** EMR must log removed/rejected lab reports with: Query Date, Query Type, Query Initiating User, Requesting HIC, Removing User, Removal Date, Removal Reason, Removal Type (manual/system), Download source (OLIS), ORC.4 (accession), OBR.4 (test request), OBR.7 (collection date), OBR.22 (last update). 

**Status: Closed (2026-05-19)** — functional behaviour is in place; a cosmetic terminology alignment is flagged for conformance review.

**Reasoning (original, preserved):** Only automatic system rejections (duplicates) are logged via \`logOLISDuplicate()\` in OLISResults2Action.java:176-226. The auto-reject log itself is fairly complete: Query Date (line 181), Query Type (182), Requesting HIC (187), Initiating Provider (190), Rejecting User (191), Rejection Date (192), Rejection Reason (193), Rejection Type (194), Accession / Test Request / Collection Date / LastUpdate from ORC/OBR (199-205). Download source (OLIS) is captured on the audit row itself via \`oscarLog.setAction("OLIS")\` (line 211\) rather than in the human-readable data block. Gaps: (1) **Manual removals are NOT logged** — when users click the Remove checkbox and Process Changes, \`bulkProcess()\` strips rows from the UI but \`OLISAddToInbox2Action.execute()\` (lines 51-134) has no \`remove\` branch and never reads the JSON \`data\` payload, so no audit row is written for the manual removal; (2) **Removal Type is hard-coded "System"** at line 194 — there is no code path that ever writes "Manual", which would be needed once manual-removal logging is added.

**Status Update (2026-05-19):** Both gaps in the original Reasoning are now closed.

**Original Gap 1 (manual removals not logged):** closed. `OLISAddToInbox2Action.logOLISRemoval()` (line 664) is now called from the manual-remove path (line 653). It writes a distinct audit row: `Removing User: <logged-in provider>` (real provider, not "System"), `Removing Type: User`, `Removing Reason: Worklist Management`, `Removing Date`, plus Query Date / Type, Requesting HIC, Initiating Provider, and Accession / Test Request(s) / Collection Date / Last Update Date parsed from the HL7.

**Original Gap 2 ("System" hardcode):** resolved. The new `logOLISRemoval()` path writes `Removing Type: User` for manual removals; the existing `logOLISDuplicate()` path still correctly writes `Rejecting User: System (automatic)` for auto-rejects. There are now two distinct code paths producing two distinct row types.

**Cosmetic terminology note for conformance review:** the audit-row label strings use `Removing Type: User` and `Removing Reason: Worklist Management`, while OLIS06.03's field list names them "Removal Type (manual/system)" and "Removal Reason". The manual path uses `Removing Type` while the system path uses `Rejection Type` — internally consistent within each path but slightly different verbs. Functional behaviour is correct; the label wording could be aligned during conformance prep for easier reviewer matching.

**Key Implementation Files:**

- **src/main/java/ca/openosp/openo/olis/OLISResults2Action.java:176-226 \-** System duplicate rejection logging only  
- **src/main/webapp/olis/Results.jsp:375-380 \-** Manual remove just removes from UI, no audit logging


# 