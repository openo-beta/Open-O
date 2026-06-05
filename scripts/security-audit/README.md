# Encoding Regression Scanner

`scan-encoding-regressions.sh` — read-only scanner that flags likely regressions
introduced by the bulk OWASP-encoding security PRs (commits `f452ecb75e`,
`ada4c9a73f`).

## Usage

```bash
# Full scan, console output
bash scripts/security-audit/scan-encoding-regressions.sh

# Save full report to file
bash scripts/security-audit/scan-encoding-regressions.sh \
  --output=scan-report.txt --all

# Show all matches for one pattern
bash scripts/security-audit/scan-encoding-regressions.sh \
  --pattern=A7 --all

# Filter by severity
bash scripts/security-audit/scan-encoding-regressions.sh --severity=high

# Scan a specific subdirectory
bash scripts/security-audit/scan-encoding-regressions.sh \
  src/main/webapp/oscarMDS
```

Exit code is **1** when HIGH severity findings are present (so it can gate CI),
**0** otherwise.

## Patterns

### HIGH severity — likely real bugs

| ID  | What it catches |
|-----|-----------------|
| A1  | `Encode.forHtml(... ? "<input...": "")` — markup gets escaped to text |
| A2  | `Encode.forHtml(...+"<tag>"+...)` — concatenated HTML being escaped |
| A3  | `[?&]param=<%=Encode.forJavaScript(...)%>` — wrong context, breaks URLs containing `&` |
| A4  | `var x = <%=Encode.forJavaScript(String.valueOf(boolean))%>;` — unquoted JS literal |
| A5  | `Encode.forHtml(*Html(...))` — encoding a method that returns HTML |
| A6  | Variable encoded with `forHtmlContent` then re-encoded at output (double encoding) |
| A7  | `Encode.forJavaScript(...date...)` — produces `\-` (Firefox warns) |
| A8  | `value="<%=request.getParameter(...)%>"` — raw user input in attribute |
| A9  | `<%=request.getParameter(...)%>` — raw user input anywhere (XSS) |
| A10 | `Encode.forJavaScript(year + "-" + month)` — same `\-` problem |
| A11 | `Encode.X(Encode.Y(...))` — chained encoders (double encoding) |
| A12 | `value=<%=...%>` — HTML attribute missing quotes |

### MEDIUM severity — wrong context, may break in edge cases

| ID  | What it catches |
|-----|-----------------|
| A7b | `Encode.forJavaScript(getName/getDescription/getCode/getAddress/...)` — values likely to contain hyphens |
| B1  | `var x = <%=Encode.forJavaScript(String.valueOf(...))%>;` — unquoted JS literal (broader) |
| B2  | `Encode.forUriComponent(URLEncoder.encode(...))` — double URL encoding |
| B3  | `Encode.forUriComponent(*path/url*)` — path slashes get encoded to `%2F` |
| B4  | `style="...<%=Encode.forHtml/forJavaScript/forUriComponent(...)%>..."` — wrong CSS context |
| B5  | `href="...<%=Encode.forJavaScript(...)%>..."` — should be `forUriComponent` |
| B6  | `Encode.forHtmlAttribute("<tag>...")` — markup escaped in attribute |
| B7  | `getProperty("DOCUMENT_DIR")` directly — bypasses helper |
| B8  | `new File(request.getParameter(...))` — path traversal candidate |
| B9  | SQL string-concat with `request.getParameter` (SQL injection risk) |
| B10 | `FileInputStream/Reader/etc.` on `request.getParameter` |
| B11 | `Encode.forHtml(...\".*\")` — HTML constructed inside encoder |

### LOW severity — cosmetic / inefficient

| ID  | What it catches |
|-----|-----------------|
| C1  | `Encode.forHtml(getChecked/getSelected(...))` — encoding a static safe string |
| C2  | `String.valueOf("literal")` — useless wrap |
| C3  | `Encode.X("literal")` — encoding a hardcoded string |
| C4  | `Encode.forJavaScript(*.getId/getNo(...))` — numeric IDs don't need JS escaping |
| C5  | `Encode.forHtml(*.getId/getNo(...))` — numeric IDs don't need HTML escaping |

## Adding New Patterns

Each pattern is a bash function `pattern_<ID>` that emits `file:line:content`
lines, and a `report_pattern` call that registers it with severity, title,
"why", and "fix" hint. See the top of the script for the structure.
