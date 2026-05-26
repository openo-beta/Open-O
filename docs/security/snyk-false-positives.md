# Snyk Code - False Positive Triage Reference

This document records Snyk Code findings that have been manually reviewed and determined to be false positives or not applicable. It serves as the authoritative reference for ignoring these findings in the Snyk UI and for onboarding reviewers who encounter them in future scans.

**Last updated:** 2026-04-20
**Reviewed by:** LiamStanziani

---

## How to use this document

1. Run `npx snyk code test --json > /tmp/snyk-results.json` to get current findings
2. Cross-reference findings against the tables below by file and line number
3. If a finding matches an entry here, mark it as ignored in the Snyk UI with the reason from the "Verdict" column
4. If a finding does NOT match any entry, investigate it as a potentially real vulnerability

**Note:** Line numbers may shift after code changes. Match by file + code pattern, not line number alone. Please also check the .snyk file if you cannot find the alert in either the open or ignored statuses, as it could have been excluded from the report generation entirely. 

This is a snapshot of the state of the project's Snyk alerts as of the **Last updated** date, if you think an ignored change is actually an issue (or is changed to turn into an issue), you can re-open the alert to show outside of the ignored status.

---

## Pasting ignore reasons into the Snyk UI

The Snyk UI ignore-reason field rejects submissions containing certain special characters, failing with "There was a problem ignoring the issue {issueID}" (see Snyk KB article [SK-20](https://support.snyk.io/s/article/Error-when-trying-to-ignore-issues-through-Snyk-UI)).

**Characters to strip or substitute before pasting:**
- Backticks (`` ` ``) — substitute with single quotes (`'`)
- Asterisks (`*`)
- Curly braces (`{` and `}`)
- Backslashes (`\`)

Regex fragments like `^[A-Za-z0-9]{1,10}$` are a common trigger because they combine curly braces and other metacharacters. Prefer a plain-English description in the ignore reason (e.g., "regex requiring 1-10 alphanumeric chars only") and keep the literal regex here in this document for reference.

The verdict and "why it's safe" text below is authored for human readability with full code formatting. When copying into Snyk, sanitize on the way in; the document itself should remain the authoritative, fully-formatted reference.

---

## Path Traversal (java/PT) - CWE-22

**Scan date:** 2026-04-09
**Branch:** `security/phase-2-pt-remediation`
**Total findings:** 17 (6 HIGH, 10 MEDIUM, 1 LOW)
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

#### 13-14. EFormExportZip.java (2 findings)

| File | Line | Fingerprint |
|------|------|-------------|
| `EFormExportZip.java` | 246 | `85252fbe...` |
| `EFormExportZip.java` | 257 | `610589a0...` |

**Verdict:** **False positive — already patched with PathValidationUtils in commit `e6eb66a`**

These lines are downstream of `PathValidationUtils.validatePath()` calls at lines 227 and 259 (added in the first batch of fixes). Snyk still flags them because it can't trace sanitization through the utility method.

---

#### 15-16. PathNetController.java (2 findings)

| File | Line | Fingerprint |
|------|------|-------------|
| `PathNetController.java` | 80 | `461bf4d8...` |
| `PathNetController.java` | 131 | `a80c5bd8...` |

**Verdict:** **Not applicable — CLI tool, not web-exposed**

Standalone `main()` program for BC PathNet lab integration. Reads a properties file path from command-line argument. Operator-controlled input.

---

### LOW findings

#### 17. LabPDFCreatorTest.java:150

| Field | Value |
|-------|-------|
| Fingerprint | `56699818...` |
| Severity | LOW |
| Verdict | **Not applicable — test code** |

Test file in `src/test/`. Not deployed to production. Snyk itself tags this as `java/PT/test`.

---

## SQL Injection (java/Sqli) - CWE-89

**Scan date:** 2026-04-17 (post-ParameterizedClause refactor)
**Total findings:** 31 (27 HIGH, 4 MEDIUM, 0 LOW after test-code `exclude:`)
**Genuine vulnerabilities:** 0

### Root cause of false positives

The codebase uses several patterns that Snyk cannot trace as sanitizers:
- `SqlUtils.validateNumericId()` — enforces `^[0-9]+$`
- `SqlUtils.validateTableName()` — enforces `^[a-zA-Z0-9_]+$`
- `SqlUtils.validateReportParameter()` — enforces `^[a-zA-Z0-9_ \-/:.,%]*$` on
  admin-defined report filter values
- `SqlUtils.parameterizeToken()` — converts `${var}` templates into `?`-bound
  PreparedStatement parameters
- `ParameterizedClause` carrier — immutable `(sql, params)` pair returned from
  `RptReportCreator.getWhereValueClauseParameterized()`. Used by the report
  flow to bind every user-supplied filter value while keeping admin-defined
  structural SQL concatenated. See `RptFormQuery.getQueryStr()`.
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
| Fingerprints | `10bd945a...`, `268c65c1...` |
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

#### 15. RptDownloadCSVServlet.java (8 findings: lines 135, 368, 386, 425, 487, 541, 614, 652)

| Field | Value |
|-------|-------|
| Fingerprints | `ba5fcbdd...` (135, warning), `d5d25694...` (368), `253b6633...` (386), `7e1e091b...` (425), `306e4cd1...` (487), `78bfd491...` (541), `b96467c9...` (614), `b2d2beda...` (652) |
| Severity | 1 MEDIUM + 7 HIGH |
| Verdict | **False positive — all user filter values bound via `ParameterizedClause` + `?` placeholders** |

**Flagged code (representative — line 135):**
```java
Vector vecFieldValue = (new RptReportCreator()).query(
    reportQuery.sql(), vecFieldCaption, reportQuery.params().toArray());
```

**Representative flagged code (one of the demoReport subqueries, line 368):**
```java
ResultSet rs = DBHandler.GetPreSQL(subQuery, subWhere.params().toArray());
```

**Why it's safe:** After the ParameterizedClause refactor, every user-supplied filter value in `demoReport()` and the legacy `formReport()` path is bound via PreparedStatement:

- `reportId` validated at line 115 via `SqlUtils.isNumericId(reportId)` — rejects non-numeric
- Filter values pass through `SqlUtils.validateReportParameter()` (defence in depth,
  per OWASP's recommendation to layer parameterization and input validation)
- All filter-value substitution happens via
  `RptReportCreator.getWhereValueClauseParameterized()`, which returns a
  `ParameterizedClause` (`(sql-with-?-placeholders, params)` pair)
- The three filter accumulators (`demoFilter`, `specFilter`, `arFilter`) are
  `ParameterizedClause` instances, combined with `.combine(" and ", other)` that
  concatenates SQL while preserving params ordering
- IN-list demographic/form IDs use `SqlUtils.inClausePlaceholders(n)` with the
  IDs bound as params, not concatenated
- The only strings still concatenated into SQL are admin-controlled identifiers
  (table names, column names, ORDER BY, field lists from `sDemoSelect` /
  `sARSelect` / `sSpecSelect` which are built from a fixed `Properties` map)

Snyk Code's taint engine flags every `.query()` / `GetPreSQL()` call site because
it tracks `request.getParameter()` as taint and cannot trace through the carrier
object even though the values are bound as PreparedStatement parameters.

---

#### 16. RptFormQuery.java:116

| Field | Value |
|-------|-------|
| Fingerprint | `0ef0a44e...` |
| Severity | HIGH |
| Verdict | **False positive — admin-configured structure + ParameterizedClause binding** |

**Flagged code:**
```java
List<Integer> ids = reportCreator.getRltSubQueryIds(subQuery);
```

**Why it's safe:** `getQueryStr()` returns a `ParameterizedClause` assembled from:
- admin-configured report structure (table names, joins, GROUP BY) — not user
  input, pulled from the `reportConfig` / `reportFilter` tables
- user-supplied filter values, bound as `?` parameters via
  `getWhereValueClauseParameterized()`

The final `reportCreator.getRltSubQueryIds(subQuery)` call runs the subQuery via
`DBHandler.GetPreSQL(subQuery.sql(), subQuery.params().toArray())` and collects
returned IDs. Defence-in-depth via `SqlUtils.validateReportParameter()` on
user values.

---

#### 17. reportResult.jsp:56

| Field | Value |
|-------|-------|
| Fingerprint | `1c1ce86e...` |
| Severity | MEDIUM |
| Verdict | **False positive — same flow as #15** |

**Flagged code:**
```java
Vector vecFieldValue = (new RptReportCreator()).query(
    reportQuery.sql(), vecFieldCaption, reportQuery.params().toArray());
```

**Why it's safe:** Same parameterized pipeline as RptDownloadCSVServlet. Receives
a `ParameterizedClause` from `RptFormQuery.getQueryStr()` and passes both the SQL
text and the bound params array to `RptReportCreator.query()`. `reportId` is
validated via `Integer.parseInt()` at the top of the JSP (throws
`NumberFormatException` on invalid input, caught and returns an error page).

---

#### 18. reportBCARDemo2.jsp (7 findings: lines 230, 248, 287, 344, 392, 460, 498)

| Field | Value |
|-------|-------|
| Fingerprints | `c6e48dff...` (230), `7a7fc121...` (248), `28a35b80...` (287), `e1ee77d2...` (344), `1cbc0ab6...` (392), `41498391...` (460), `d49f0415...` (498) |
| Severity | 7 HIGH |
| Verdict | **False positive — mirrors RptDownloadCSVServlet.demoReport() post-refactor** |

**Flagged code (representative — line 230):**
```java
vecFieldValue = (new RptReportCreator()).query(sql, vecFieldName, demoFilter.params().toArray());
```

**Why it's safe:** This JSP is the HTML-rendering counterpart to
`RptDownloadCSVServlet.demoReport()` (which emits CSV). Both share the same
filter-building and query-execution pipeline. All user filter values flow
through `SqlUtils.validateReportParameter()` and then into
`ParameterizedClause`-backed bound parameters. See #15 justification for the
full reasoning — the two paths are line-for-line parallel.

---

### LOW findings

#### 19-27. SchemaUtils.java — now suppressed by `.snyk` (9 findings, previously 18-26)

These 9 previously-LOW findings in `src/test/java/.../SchemaUtils.java` are no
longer returned by `snyk code test` after `src/test/**` was added to the
`exclude:` block in `/.snyk`. No per-finding work needed.

Prior content (kept for historical reference):

| Lines | Fingerprints |
|-------|-------------|
| 233, 278, 283, 288, 289, 290, 396, 402, 424 | `48790065...`, `b9954bb4...`, `fa2a536f...`, `2f90c6aa...`, `01625114...`, `6624a05b...`, `80215550...`, `d0785410...`, `e55adca6...` |

**Verdict:** **Not applicable — test utility code**

All findings are in `src/test/java/.../SchemaUtils.java`, a test utility for schema management. The string-concatenated SQL uses values from `DatabaseMetaData.getTables()` (JVM metadata API) and `INFORMATION_SCHEMA` queries — not user input. This code is never deployed to production. Snyk itself tags these as `java/Sqli/test`.

---

## Cross-Site Scripting (java/XSS, javascript/DOMXSS) - CWE-79

**Scan date:** 2026-04-17 (post-encoder-context fixes + nosniff defence-in-depth)
**Branch:** `security/additional-phase-2-fixes-snyk`
**Total findings:** 19 (10 java/XSS, 9 javascript/DOMXSS)
**Genuine vulnerabilities:** 0
**Starting count:** 4,000+ (99.5% reduction achieved via OWASP output encoding)

**Note on line numbers:** Several line numbers shifted by +1 after the branch
added `X-Content-Type-Options: nosniff` headers as defence-in-depth. The nosniff
header does not change Snyk's verdict (it's a browser-side protection, not a
sanitizer), but is genuinely useful against content-sniffing XSS.

### Root cause of remaining false positives

Snyk Code's data-flow analysis tracks HTTP parameters through variables and method calls to output sinks, but does not recognize these mitigations:
- **Content-Type headers** -- `application/json` or `application/octet-stream` prevents browser HTML rendering
- **Binary file streaming** -- `ServletOutputStream.write(byte[])` with non-HTML content types
- **Localhost-only servlets** -- request origin restricted to `127.0.0.1`
- **Admin-authored content** -- eForm HTML templates created by system administrators
- **Third-party libraries** -- jQuery DataTables and auto-generated JavaDoc

---

### java/XSS -- JSON responses with application/json Content-Type (2 findings)

#### 1-2. ManageDocument2Action.java:278, :299

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- JSON response with application/json Content-Type** |

**Why it's safe:** Both methods set `response.setContentType("application/json")` and `X-Content-Type-Options: nosniff` before writing. Browsers will not interpret the response as HTML. Data is serialized via Jackson, which JSON-escapes string values (quotes, backslashes, control chars); combined with the JSON content type and nosniff header, the response cannot be rendered as HTML by the browser.

**Scope of verdict:** This justification covers the *server-side* reflected-XSS class only. If a client-side JS consumer sinks these JSON values directly into the DOM as HTML without escaping, that is a separate DOM-XSS finding against the JS and must be evaluated on its own.

---

### java/XSS -- Binary file streaming (4 findings)

#### 3. GenericDownload.java:116

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- binary download with application/octet-stream** |

**Why it's safe:** Content-Type defaults to `application/octet-stream`. Content-Disposition: attachment forces download. `X-Content-Type-Options: nosniff` prevents browsers from sniff-upgrading to text/html. File path validated via `PathValidationUtils.validatePath()`.

#### 4-5. documentGetFile.jsp:117, documentGetFile.jsp (consultation):100

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- binary file stream with non-HTML Content-Type** |

**Why it's safe:** Content-Type set to `application/octet-stream` before write. Content-Disposition header with sanitized filename (CRLF stripped). `X-Content-Type-Options: nosniff` added to prevent content sniffing. Binary data cannot execute as HTML.

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

#### 9. admin/keygen/createKey.jsp:95

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- server-generated key with text/plain Content-Type** |

**Why it's safe:** Content-Type set to text/plain. Content-Disposition forces download. `X-Content-Type-Options: nosniff` added to prevent content sniffing. Output is server-generated RSA key material, not user input.

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

These 4 previously-flagged findings are now suppressed by the `exclude:` block
in `/.snyk` (`src/main/webapp/js/jquery*.js`, `src/main/webapp/library/**`,
`docs/static/javadoc/**`). No per-finding work needed.

#### 14-16. jquery.dataTables.js:1068, :3055, :3365

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **Not applicable -- third-party library (jQuery DataTables), excluded via .snyk** |

Third-party library, cannot be modified without forking. Known DataTables DOM manipulation patterns.

#### 17. docs/static/javadoc/search-page.js:38

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **Not applicable -- auto-generated JavaDoc, excluded via .snyk** |

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

#### 22. oscarMDSIndex.js:130 -- script.src assignment

| Field | Value |
|-------|-------|
| Severity | WARNING |
| Verdict | **False positive -- same-origin validated script source** |

**Why it's safe:** Before assignment on line 130, `script.src` is parsed via `new URL(script.src, window.location.origin)` and compared by `.origin` to `window.location.origin` (lines 128-129). Cross-origin URLs, `javascript:` URLs (opaque `null` origin), `data:` URLs (opaque `null` origin), and protocol-relative URLs resolving cross-origin are all rejected with `console.warn` and an early `return`.

**Scope of verdict:** Covers the script.src flow only. The sibling inline-script branch (line 140, `newScript.textContent = script.textContent`) and the `insertAdjacentHTML` call at line 117 rely on a different trust mechanism -- see entry 23 below.

#### 23. oscarMDSIndex.js:117 -- appendHtmlWithScripts insertAdjacentHTML

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- trusted OWASP-encoded JSP response** |

**Why it's safe:** `html` is the text response of a same-origin `fetch()` to one of two JSPs reachable from the switch at `oscarMDSIndex.js:653-662`:
- `lab/CA/ALL/labDisplayAjax.jsp` (HL7 path)
- `documentManager/showDocument.jsp` (DOC path)

Both JSPs OWASP-encode all reflected request parameters (`segmentID`, `providerNo`, `searchProviderNo`, `demoName`, etc.) using `Encode.forHtmlAttribute(String.valueOf(...))` and `Encode.forJavaScript(String.valueOf(...))` at every sink. `insertAdjacentHTML` does not execute `<script>` tags inserted this way (scripts are re-created separately at line 123 with the same-origin `src` check covered by entry 22). Event-handler injection vectors (`<img onerror=...>`, `<svg onload=...>`, inline `on*` attributes) are prevented by the JSP encoding layer.

**Scope of verdict:** Relies on the feeding JSPs remaining OWASP-encoded. If a new `type` branch is added to the switch at lines 653-662 pointing to a JSP without equivalent encoding, this verdict does not transfer. The `MDS`, `CML`, and fallback branches currently set `url = ""` (dead paths) -- tracked as tech-debt to remove.

#### 24. oscarMDSIndex.js:1578, :1588 -- patientId HTML concatenation

| Field | Value |
|-------|-------|
| Severity | HIGH |
| Verdict | **False positive -- patientId enforced numeric upstream** |

**Why it's safe:** `patientId` here originates from the `patientIds` array, which is populated client-side from `patientIdStr`. `patientIdStr` is built server-side in `DmsInboxManage2Action.java:564-569` (and `InboxManagerImpl.java:367-372`) by iterating the keys of a `patientDocs` Hashtable. Those keys are `demographic_no` primary-key values (schema type `INT`) plus a `-1` "Not Assigned" sentinel. No user-input string reaches this path; values are integer demographic identifiers by construction.

**Scope of verdict:** The numeric invariant is upstream-only -- there is no client-side type check at the sink. If a server-side change ever allows non-numeric values into `patientIdStr`, this becomes a DOM XSS. Refactor tracked separately to replace string-concat HTML generation in `createNewDocEle` / `createNewHL7Ele` with DOM-API element creation or `textContent` assignment.

---