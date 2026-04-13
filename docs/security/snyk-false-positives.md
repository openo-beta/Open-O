# Snyk Code - False Positive Triage Reference

This document records Snyk Code findings that have been manually reviewed and determined to be false positives or not applicable. It serves as the authoritative reference for ignoring these findings in the Snyk UI and for onboarding reviewers who encounter them in future scans.

**Last updated:** 2026-04-13
**Reviewed by:** LiamStanziani

---

## How to use this document

1. Run `npx snyk code test --json > /tmp/snyk-results.json` to get current findings
2. Cross-reference findings against the tables below by file and line number
3. If a finding matches an entry here, mark it as ignored in the Snyk UI with the reason from the "Verdict" column
4. If a finding does NOT match any entry, investigate it as a potentially real vulnerability

**Note:** Line numbers may shift after code changes. Match by file + code pattern, not line number alone.

---

## Path Traversal (java/PT) - CWE-22

**Scan date:** 2026-04-09
**Branch:** `security/phase-2-pt-remediation`
**Total findings:** 19 (6 HIGH, 12 MEDIUM, 1 LOW)
**Genuine vulnerabilities:** 0 (all resolved in prior commits on this branch)

### Root cause of false positives

Snyk Code's inter-procedural taint analysis cannot propagate sanitization through `PathValidationUtils` method boundaries. Internally, `PathValidationUtils` uses `File.getCanonicalPath()` + `String.startsWith()` — a pattern Snyk recognizes as a sanitizer when inlined — but not when called through a utility method. See `docs/path-validation-utils.md` for full API documentation.

If Snyk Custom Rules are available (Team/Enterprise plan), register these as sanitizers in **Org Settings > Snyk Code > Custom Rules**:
- `PathValidationUtils.validatePath()`
- `PathValidationUtils.validateExistingPath()`
- `PathValidationUtils.validateUpload()`
- `PathValidationUtils.isInAllowedTempDirectory()`

### HIGH findings

#### 1. PrintDemoChartLabel2Action.java:195

| Field | Value |
|-------|-------|
| Fingerprint | `149368f6...` |
| Severity | HIGH |
| Verdict | **False positive — hardcoded whitelist map** |

**Flagged code:**
```java
ins = new FileInputStream(System.getProperty("user.home") + File.separator + labelFile);
```

**Why it's safe:** `labelFile` is resolved from a hardcoded 2-entry map (lines 155-157):
```java
nameToFileMap.put("ChartLabel", "Chartlabel.xml");
nameToFileMap.put("SexualHealthClinicLabel", "SexualHealthClinicLabel.xml");
```
User input (`labelName` parameter) is used as the map key. If the key doesn't match, `labelFile` is null and the method returns at line 165. The value is always one of two hardcoded filenames — no traversal possible.

---

#### 2. DocumentPreview2Action.java:329

| Field | Value |
|-------|-------|
| Fingerprint | `0dcab4e8...` |
| Severity | HIGH |
| Verdict | **False positive — validated by toRealPath() + validateExistingPath()** |

**Flagged code:**
```java
InputStream inputStream = Files.newInputStream(canonicalPdfPath);
```

**Why it's safe:** Two-layer validation before this line is reached:
1. Line 288: `pdfPath.toRealPath()` resolves all `..` components and symlinks to the actual canonical path (throws IOException if file doesn't exist)
2. Line 304: `PathValidationUtils.validateExistingPath(canonicalPdfPath.toFile(), baseDir)` checks containment against a whitelist of 4 allowed directories (`DOCUMENT_DIR`, `TMP_DIR`, `eform_image`, `java.io.tmpdir`)
3. Line 314: If validation fails all directories, returns 403

---

#### 3. EctConsultationFormFax2Action.java:191

| Field | Value |
|-------|-------|
| Fingerprint | `b1bbb0f2...` |
| Severity | HIGH |
| Verdict | **False positive — system-generated path, not user input** |

**Flagged code:**
```java
Files.deleteIfExists(faxPdf);
```

**Why it's safe:** `faxPdf` is generated server-side:
1. Line 119: `faxPdf = documentAttachmentManager.renderConsultationFormWithAttachments(request, response)` — internal rendering engine creates the file
2. Line 126-127: Copied to OscarDocuments via `nioFileManager.copyFileToOscarDocuments()`

The user controls which consultation to fax (via `reqId`), but the file path is constructed entirely by the rendering engine. No user-provided string reaches the path.

---

#### 4. Fax2Action.java:317

| Field | Value |
|-------|-------|
| Fingerprint | `cf1bc6df...` |
| Severity | HIGH |
| Verdict | **False positive — validated by FaxManager.resolveAndValidateFilePath()** |

**Flagged code:**
```java
InputStream inputStream = Files.newInputStream(outfile);
```

**Why it's safe:** `outfile` is set via one of two validated paths:
- **Image path** (line 282): `faxManager.getFaxPreviewImage()` — system-generated
- **PDF path** (line 294): `faxManager.resolveAndValidateFilePath(faxFilePath)` which calls:
  1. `validateFilePath()` — rejects `..` and `~` patterns
  2. `PathValidationUtils.validateExistingPath()` against `DOCUMENT_DIR`
  3. Falls back to `isInAllowedTempDirectory()` for temp files
  4. Throws `SecurityException` (caught at line 296 → 403) or `IOException` (caught at line 304 → 404) on failure

---

#### 5. RunClinicalReport2Action.java:110

| Field | Value |
|-------|-------|
| Fingerprint | `40ee9005...` |
| Severity | HIGH |
| Verdict | **False positive — not a file operation** |

**Flagged code:**
```java
MiscUtils.getLogger().debug("n" + n + " " + n.hasReplaceableValues());
```

**Why it's safe:** This is a debug log statement. `n` is a `Numerator` object from `reports.getNumeratorById(numeratorId)`. No file path construction or I/O occurs at or near this line. Snyk misidentified the taint flow.

---

#### 6. incomingDocs.jsp:189

| Field | Value |
|-------|-------|
| Fingerprint | `c1f4bf35...` |
| Severity | HIGH |
| Verdict | **False positive — all params validated by IncomingDocUtil via PathValidationUtils + whitelist** |

**Flagged code:**
```java
IncomingDocUtil.doPagesAction(pdfAction, queueIdStr, pdfDir, pdfName, pdfPageNumber, pdfExtractPageNumber, vLocale);
```

**Why it's safe:** `doPagesAction()` calls `getIncomingDocumentFilePathName()` which validates all path-relevant parameters:
- `queueId`: `PathValidationUtils.validatePath(queueId, baseDir)`
- `pdfDir`: Strict whitelist — only `Fax`, `Mail`, `File`, `Refile` accepted; throws `IllegalArgumentException` for anything else
- `pdfName`: `PathValidationUtils.validatePath(pdfName, baseDir)`

---

### MEDIUM findings

#### 7-12. CXF-generated WS Client files (6 findings)

| File | Line | Fingerprint |
|------|------|-------------|
| `DemographicWs_DemographicWsPort_Client.java` | 22 | `b5c02324...` |
| `FacilityWs_FacilityWsPort_Client.java` | 21 | `a2c51057...` |
| `HnrWs_HnrWsPort_Client.java` | 23 | `73c01920...` |
| `ProgramWs_ProgramWsPort_Client.java` | 21 | `7de18101...` |
| `ProviderWs_ProviderWsPort_Client.java` | 21 | `7061b3eb...` |
| `ReferralWs_ReferralWsPort_Client.java` | 20 | `30ced4d4...` |

**Verdict:** **Not applicable — CLI tools, not web-exposed**

All are CXF-generated standalone clients with `public static void main(String[] args)`. They read `args[0]` as a WSDL file path/URL. These are operator-run development/integration tools. An operator executing a CLI tool is already a trusted user with filesystem access.

---

#### 13-14. Database import CLI utilities (2 findings)

| File | Line | Fingerprint |
|------|------|-------------|
| `importCPP.java` | 18 | `88617fb6...` |
| `importCasemgmt.java` | 55 | `ea63215b...` |

**Verdict:** **Not applicable — CLI tools in `database/mysql/`, not web-exposed**

Same pattern as above. Both are standalone `main()` programs that accept a properties file path from the command line. Operator-controlled input.

---

#### 15-16. EFormExportZip.java (2 findings)

| File | Line | Fingerprint |
|------|------|-------------|
| `EFormExportZip.java` | 246 | `85252fbe...` |
| `EFormExportZip.java` | 257 | `610589a0...` |

**Verdict:** **False positive — already patched with PathValidationUtils in commit `e6eb66a`**

These lines are downstream of `PathValidationUtils.validatePath()` calls at lines 227 and 259 (added in the first batch of fixes). Snyk still flags them because it can't trace sanitization through the utility method.

---

#### 17-18. PathNetController.java (2 findings)

| File | Line | Fingerprint |
|------|------|-------------|
| `PathNetController.java` | 80 | `461bf4d8...` |
| `PathNetController.java` | 131 | `a80c5bd8...` |

**Verdict:** **Not applicable — CLI tool, not web-exposed**

Standalone `main()` program for BC PathNet lab integration. Reads a properties file path from command-line argument. Operator-controlled input.

---

### LOW findings

#### 19. LabPDFCreatorTest.java:150

| Field | Value |
|-------|-------|
| Fingerprint | `56699818...` |
| Severity | LOW |
| Verdict | **Not applicable — test code** |

Test file in `src/test/`. Not deployed to production. Snyk itself tags this as `java/PT/test`.

---

## SQL Injection (java/Sqli) - CWE-89

**Scan date:** 2026-04-09
**Total findings:** 26 (12 HIGH, 5 MEDIUM, 9 LOW)
**Genuine vulnerabilities:** 0

### Root cause of false positives

The codebase uses several patterns that Snyk cannot trace as sanitizers:
- `SqlUtils.validateNumericId()` — enforces `^[0-9]+$`
- `SqlUtils.validateTableName()` — enforces `^[a-zA-Z0-9_]+$`
- Whitelist `Set.of(...)` checks on column/orderBy names in DAO methods
- `Integer.parseInt()` used as implicit numeric validation
- `FrmData.executeWithValidatedTable()` — table name validated by contract
- Admin-configured report templates loaded from DB (trusted source, not user input)

### HIGH findings

#### 1. FormForward2Action.java:91

| Field | Value |
|-------|-------|
| Fingerprint | `9ba60745...` |
| Severity | HIGH |
| Verdict | **False positive — numeric validation + parameterized DAO** |

**Flagged code:**
```java
formPath = frmData.getShortcutFormValue(demographicNo, strFrm);
```

**Why it's safe:**
- `demographicNo` validated at line 77 via `SqlUtils.validateNumericId(demographicNo, "demographic_no")` — enforces `^[0-9]+$`
- `strFrm` (formName) is used as a lookup key via `encounterFormDao.findByFormName(formName)` which uses parameterized HQL
- Inside `getShortcutFormValue()`, all SQL uses `DBHandler.GetPreSQL()` with `?` parameter binding
- Dynamic table names sourced from DB (`encounterForm.getFormTable()`) are validated with `SqlUtils.validateTableName()` before use

---

#### 2. FrmSetupForm2Action.java:277

| Field | Value |
|-------|-------|
| Fingerprint | `cc3684f5...` |
| Severity | HIGH |
| Verdict | **False positive — regex-validated table name + PreparedStatement** |

**Flagged code:**
```java
String sql = "SELECT * FROM form" + formName + " WHERE ID=? AND demographic_no=?";
PreparedStatement ps = connection.prepareStatement(sql);
```

**Why it's safe:**
- `formName` validated at line 266 by `isValidFormName()` which enforces regex `^[a-zA-Z0-9_]+$` (defined at lines 324-336)
- `formId` and `demographicNo` are bound via `ps.setInt()` — parameterized
- Dynamic table name is unavoidable (form tables are named `form{FormName}`) but the regex restricts to alphanumeric + underscore only

---

#### 3. RunClinicalReport2Action.java:110

| Field | Value |
|-------|-------|
| Fingerprint | `5b76f70b...` |
| Severity | HIGH |
| Verdict | **False positive — HashMap lookup, not SQL** |

**Flagged code:**
```java
Numerator n = reports.getNumeratorById(numeratorId);
```

**Why it's safe:** `getNumeratorById()` performs a `HashMap.get(id)` on an in-memory structure loaded from XML config files at startup (`loadReportsFromFile()`). No SQL is executed at this line. The user-provided `numeratorId` is used as a HashMap key — unrecognized IDs return null. Snyk is tracing taint through the object but the flagged line itself performs no database operation.

---

#### 4-5. SQLReporter.java:86, 134

| Field | Value |
|-------|-------|
| Fingerprints | `bb558712...`, `0f56345a...` |
| Severity | HIGH |
| Verdict | **False positive — parameterized template system** |

**Flagged code:**
```java
ResultSet rs = DBHandler.GetPreSQL(sql, prepared.getParamsArray());
```

**Why it's safe:**
- `templateId` validated as numeric via `Integer.parseInt(templateId)` at line 114
- Report SQL is loaded from admin-configured templates via `getReportTemplateNoParam(templateId)` — trusted source
- User-provided parameter values are bound via `getParameterizedSQL(parameterMap)` which returns a `PreparedSQL` object containing the SQL with `?` placeholders and a params array
- `DBHandler.GetPreSQL()` executes via PreparedStatement with parameter binding

---

#### 6. forwardshortcutname.jsp:47

| Field | Value |
|-------|-------|
| Fingerprint | `1085d7b8...` |
| Severity | HIGH |
| Verdict | **False positive — parameterized DAO lookups downstream** |

**Flagged code:**
```java
String[] formPath = (new FrmData()).getShortcutFormValue(request.getParameter("demographic_no"), strFrm);
```

**Why it's safe:** Same method as Finding #1. Inside `getShortcutFormValue()`:
- `demoNo` used via `DBHandler.GetPreSQL("... WHERE demographic_no=? AND form_name=? ...", demoNo, searchFormName)` — parameterized
- `formName` looked up via `encounterFormDao.findByFormName()` — parameterized HQL
- Dynamic table names validated with `SqlUtils.validateTableName()`

---

#### 7. demo_select.jsp:70

| Field | Value |
|-------|-------|
| Fingerprint | `439ff1d7...` |
| Severity | HIGH |
| Verdict | **False positive — whitelist-validated column/orderBy names** |

**Flagged code:**
```java
List<Demographic> demographics = dao.findByField(column, (Object) keyword, orderby, ...);
```

**Why it's safe:** `DemographicDaoImpl.findByField()` (line 2489) validates both `fieldName` and `orderBy` against a hardcoded whitelist:
```java
Set<String> validFields = Set.of(
    "LastName", "FirstName", "DemographicNo", "ChartNo", "Hin",
    "Address", "Phone", "Sex", "DateOfBirth",
    "last_name", "first_name", "demographic_no", "chart_no", "hin",
    "address", "phone", "sex", "date_of_birth"
);
```
Invalid values default to `"LastName"`. The `keyword` value is bound via HQL parameter binding.

---

#### 8. LabReqReport.jsp:65

| Field | Value |
|-------|-------|
| Fingerprint | `e21115e0...` |
| Severity | HIGH |
| Verdict | **False positive — integer validation on concatenated value** |

**Flagged code:**
```java
conData.labReportGenerate(pros, mons);
```

**Why it's safe:** Inside `labReportGenerate()`:
- `days` (mons) is validated with `verifyInt(days)` at line 92 — throws `IllegalArgumentException` if not an integer
- The `days` value is used in `INTERVAL ... month` syntax which cannot be parameterized in MySQL, but integer validation prevents injection
- `providerNo` and `demoNo` are bound via parameterized queries (`:providerNo`, `:demoNo` named parameters)

---

#### 9. LookupCodeEdit2Action.java:79

| Field | Value |
|-------|-------|
| Fingerprint | `38e9b2a6...` |
| Severity | HIGH |
| Verdict | **False positive — strict regex validation** |

**Flagged code:**
```java
LookupTableDefValue tableDef = lookupManager.GetLookupTableDef(tableId);
```

**Why it's safe:**
- `tableId` extracted from request parameter `id` (split on `:`)
- Validated at line 64 against `VALID_TABLE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,10}$")`
- Throws `IllegalArgumentException` if validation fails
- Downstream lookup uses parameterized HQL

---

#### 10. LookupCodeList2Action.java:59

| Field | Value |
|-------|-------|
| Fingerprint | `b916a127...` |
| Severity | HIGH |
| Verdict | **False positive — strict regex validation** |

**Flagged code:**
```java
LookupTableDefValue tableDef = lookupManager.GetLookupTableDef(tableId);
```

**Why it's safe:** Same pattern as #9. `tableId` validated with `validateTableId()` using regex `^[A-Za-z0-9]{1,10}$`. Downstream uses parameterized HQL.

---

#### 11-12. LookupList2Action.java:82, 98

| Field | Value |
|-------|-------|
| Fingerprints | `edc14522...`, `1a4d1e42...` |
| Severity | HIGH |
| Verdict | **False positive — strict regex validation** |

**Flagged code:**
```java
// Line 82
List lst = lookupManager.LoadCodeList(tableId, true, parentCode, null, null);
// Line 98
LookupTableDefValue tableDef = lookupManager.GetLookupTableDef(tableId);
```

**Why it's safe:** Same pattern. `tableId` validated at line 75 with regex `^[A-Za-z0-9]{1,10}$`. Access control check at line 76 restricts to allowed table codes. Downstream uses parameterized HQL.

---

### MEDIUM findings

#### 13. FrmData.java:70

| Field | Value |
|-------|-------|
| Fingerprint | `898ae9ef...` |
| Severity | MEDIUM |
| Verdict | **False positive — validated table name + PreparedStatement** |

**Flagged code:**
```java
String sql = sqlTemplate.replace("{TABLE}", validatedTable);
Connection conn = DbConnectionFilter.getThreadLocalDbConnection();
```

**Why it's safe:** `executeWithValidatedTable()` requires callers to validate table names via `SqlUtils.validateTableName()` before calling. The method uses `PreparedStatement` with `ps.setObject(i+1, params[i])` for all value parameters. Only the table name is substituted into the template, and it's restricted to `^[a-zA-Z0-9_]+$`.

---

#### 14. FrmXmlUpload2Action.java:103

| Field | Value |
|-------|-------|
| Fingerprint | `530bb895...` |
| Severity | MEDIUM |
| Verdict | **False positive — validated inputs + parameterized execution** |

**Flagged code:**
```java
JDBCUtil.toDataBase(zis, entry.getName());
```

**Why it's safe:** Comments at lines 94-97 explain: formName validated with `VALID_NAME_PATTERN`, demographicNo with `^[0-9]+$`, and values are bound via PreparedStatement in `JDBCUtil.toDataBase()`.

---

#### 15. RptDownloadCSVServlet.java:130

| Field | Value |
|-------|-------|
| Fingerprint | `62e90006...` |
| Severity | MEDIUM |
| Verdict | **False positive — numeric ID + admin-configured templates** |

**Flagged code:**
```java
Vector vecFieldValue = (new RptReportCreator()).query(reportSql, vecFieldCaption);
```

**Why it's safe:**
- `reportId` validated at line 110 via `SqlUtils.isNumericId(reportId)` — rejects non-numeric
- `reportSql` constructed from admin-configured templates via `RptFormQuery.getQueryStr()`
- User parameter values validated via regex `^[a-zA-Z0-9_ \-/:.,%]*$` in `getQueryValue()`

---

#### 16. RptFormQuery.java:93

| Field | Value |
|-------|-------|
| Fingerprint | `04c2183e...` |
| Severity | MEDIUM |
| Verdict | **False positive — admin-configured structure + regex-validated values** |

**Flagged code:**
```java
String rltSubQuery = reportCreator.getRltSubQuery(subQuery);
```

**Why it's safe:** The query structure (table names, joins, fields) comes from admin-configured report definitions stored in the database — not user input. User-supplied parameter values are validated via regex `^[a-zA-Z0-9_ \-/:.,%]*$` in `getQueryValue()` before substitution.

---

#### 17. reportResult.jsp:55

| Field | Value |
|-------|-------|
| Fingerprint | `7c5dfeb0...` |
| Severity | MEDIUM |
| Verdict | **False positive — Integer.parseInt() validation + admin templates** |

**Flagged code:**
```java
Vector vecFieldValue = (new RptReportCreator()).query(reportSql, vecFieldCaption);
```

**Why it's safe:** Same report execution path as #15. `reportId` validated via `Integer.parseInt()` at line 39 (throws NumberFormatException on invalid input, caught and returns error). Report SQL from admin-configured templates.

---

### LOW findings

#### 18-26. SchemaUtils.java (9 findings)

| Lines | Fingerprints |
|-------|-------------|
| 233, 278, 283, 288, 289, 290, 396, 402, 424 | `48790065...`, `b9954bb4...`, `fa2a536f...`, `2f90c6aa...`, `01625114...`, `6624a05b...`, `80215550...`, `d0785410...`, `e55adca6...` |

**Verdict:** **Not applicable — test utility code**

All findings are in `src/test/java/.../SchemaUtils.java`, a test utility for schema management. The string-concatenated SQL uses values from `DatabaseMetaData.getTables()` (JVM metadata API) and `INFORMATION_SCHEMA` queries — not user input. This code is never deployed to production. Snyk itself tags these as `java/Sqli/test`.

---

## Cross-Site Scripting (java/XSS, javascript/DOMXSS) - CWE-79

**Scan date:** 2026-04-13
**Branch:** `security/phase-2-xss-remediation`
**Total findings:** 22 (13 java/XSS, 9 javascript/DOMXSS)
**Genuine vulnerabilities:** 0
**Starting count:** 4,000+ (99.5% reduction achieved via OWASP output encoding)

### Root cause of remaining false positives

Snyk Code's data-flow analysis tracks HTTP parameters through variables and method calls to output sinks, but does not recognize these mitigations:
- **Content-Type headers** -- `application/json` or `application/octet-stream` prevents browser HTML rendering
- **Binary file streaming** -- `ServletOutputStream.write(byte[])` with non-HTML content types
- **Localhost-only servlets** -- request origin restricted to `127.0.0.1`
- **Admin-authored content** -- eForm HTML templates created by system administrators
- **Third-party libraries** -- jQuery DataTables and auto-generated JavaDoc

---

### java/XSS -- JSON responses with application/json Content-Type (2 findings)

#### 1-2. ManageDocument2Action.java:277, :297

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- JSON response with application/json Content-Type** |

**Why it's safe:** Both methods set `response.setContentType("application/json")` before writing. Browsers will not interpret the response as HTML. Data serialized via Jackson ObjectMapper which auto-escapes JSON string values.

---

### java/XSS -- Binary file streaming (4 findings)

#### 3. GenericDownload.java:115

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- binary download with application/octet-stream** |

**Why it's safe:** Content-Type defaults to `application/octet-stream`. Content-Disposition: attachment forces download. File path validated via `PathValidationUtils.validatePath()`.

#### 4-5. documentGetFile.jsp:102, documentGetFile.jsp (consultation):85

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- binary file stream with non-HTML Content-Type** |

**Why it's safe:** Content-Type set to `application/octet-stream` before write. Content-Disposition header with sanitized filename (CRLF stripped). Binary data cannot execute as HTML.

#### 6. EFormViewForPdfGenerationServlet.java:116

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- localhost-only servlet with CSP headers** |

**Why it's safe:** Servlet rejects non-localhost requests (127.0.0.1 check, returns 403). Has Content-Security-Policy and X-Content-Type-Options headers. Renders eForm HTML for server-side PDF generation only.

---

### java/XSS -- Admin-authored eForm HTML (2 findings)

#### 7-8. efmformadd_data.jsp:154, efmshowform_data.jsp:153

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- intentional HTML rendering of admin-authored clinical forms** |

**Why it's safe:** eForm HTML templates are created by system administrators through the eForm management interface. Rendering raw HTML is the intended behavior -- eForms ARE HTML forms. Access requires authentication and `_eform` security privilege.

---

### java/XSS -- Server-generated content with validated inputs (5 findings)

#### 9. admin/keygen/createKey.jsp:94

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- server-generated key with text/plain Content-Type** |

**Why it's safe:** Content-Type set to text/plain. Content-Disposition forces download. Output is server-generated RSA key material, not user input.

#### 10. setProviderAvailability.jsp:177

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- admin-managed provider data** |

**Why it's safe:** Value comes from UserProperty table (admin-managed). Fallback is a static HTML string. Not user-controlled.

#### 11. scheduleflipview.jsp:313

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- admin-managed site data with sanitized provider_no** |

**Why it's safe:** getSiteHTML() uses database-stored site names/colors (admin-managed). curProvider_no sanitized with `replaceAll("[^a-zA-Z0-9]", "")`. Other values OWASP-encoded.

#### 12-13. demo_select.jsp:167, :170

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- URL parameters encoded with Encode.forUriComponent()** |

**Why it's safe:** Pagination URLs encode all request parameters (keyword, postTo, column) via `Encode.forUriComponent()`. Snyk tracks original parameter flow but doesn't recognize URI encoding as sanitization.

---

### javascript/DOMXSS -- Third-party libraries (4 findings)

#### 14-16. jquery.dataTables.js:1068, :3055, :3365

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **Not applicable -- third-party library (jQuery DataTables)** |

Third-party library, cannot be modified without forking. Known DataTables DOM manipulation patterns. Risk accepted.

#### 17. docs/static/javadoc/search-page.js:38

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **Not applicable -- auto-generated JavaDoc** |

Generated by maven-javadoc-plugin. Regenerated on each build. Not hand-written code.

---

### javascript/DOMXSS -- Trusted server-rendered HTML (5 findings)

#### 18. noteProgram.js:201

| Field | Value |
|-------|-------|
| Severity | WARNING |
| Verdict | **False positive -- trusted AJAX response from authenticated endpoint** |

**Why it's safe:** Note content from authenticated CaseManagementEntry.do endpoint. Contains server-formatted line breaks. Using .text() would break display of existing clinical notes.

#### 19. topnav.js:14

| Field | Value |
|-------|-------|
| Severity | WARNING |
| Verdict | **False positive -- trusted server-rendered navigation HTML** |

**Why it's safe:** Response from tabAlertsRefresh.jsp renders badge/count markup via custom JSP tags. Server-generated HTML from authenticated endpoint.

#### 20-21. dashboardDisplayController.js:124, drilldownDisplayController.js:63

| Field | Value |
|-------|-------|
| Severity | WARNING |
| Verdict | **False positive -- trusted server-rendered dashboard HTML** |

**Why it's safe:** Authenticated dashboard endpoints return server-rendered indicator panels. HTML generated by JSP templates, not user input.

#### 22. oscarMDSIndex.js:130

| Field | Value |
|-------|-------|
| Severity | WARNING |
| Verdict | **False positive -- same-origin validated script source** |

**Why it's safe:** Same-origin URL validation added to ensure script.src only accepts scripts from the current origin.

---