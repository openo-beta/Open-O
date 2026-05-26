#!/usr/bin/env bash
# scan-encoding-regressions.sh
#
# Scans JSP/JSPF/Java sources for likely regressions introduced by the bulk
# OWASP-encoding security PRs. Emits findings grouped by severity. Read-only;
# never modifies files.
#
# Usage:
#   bash scripts/security-audit/scan-encoding-regressions.sh [path]
#   bash scripts/security-audit/scan-encoding-regressions.sh --severity=high
#   bash scripts/security-audit/scan-encoding-regressions.sh --pattern=A1
#
# Background:
#   The bulk script (commits f452ecb75e, ada4c9a73f) wrapped output expressions
#   in Encode.forHtml(String.valueOf(...)) regardless of context, breaking
#   JSPs in many ways. Subsequent regression-fixes (2d1d655253) addressed
#   most cases but missed some. This scanner catches the rest.

set -uo pipefail

# Resolve repo root (script lives at scripts/security-audit/<this>)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Defaults
SCAN_PATH="$REPO_ROOT/src/main/webapp"
SEVERITY_FILTER=""    # high | medium | low (empty = all)
PATTERN_FILTER=""     # e.g. A1, B2 (empty = all)
OUTPUT_FILE=""        # write to file instead of stdout
SHOW_ALL=false        # show all matches, not just first 5
SHOW_HELP=false

# Parse flags & positional path
for arg in "$@"; do
    case "$arg" in
        --severity=*) SEVERITY_FILTER="${arg#*=}" ;;
        --pattern=*)  PATTERN_FILTER="${arg#*=}" ;;
        --output=*)   OUTPUT_FILE="${arg#*=}" ;;
        --all)        SHOW_ALL=true ;;
        -h|--help)    SHOW_HELP=true ;;
        --*)          ;;
        *)            SCAN_PATH="$arg" ;;
    esac
done

# Redirect output to file if requested
if [ -n "$OUTPUT_FILE" ]; then
    exec > "$OUTPUT_FILE"
    # Disable colors when writing to file
    RED=""; YELLOW=""; CYAN=""; BOLD=""; DIM=""; RESET=""
fi

if $SHOW_HELP; then
    sed -n '2,20p' "$0" | sed 's/^# \?//'
    exit 0
fi

# Colors (auto-disable if not a TTY)
if [ -t 1 ]; then
    RED=$'\033[31m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'
    BOLD=$'\033[1m'; DIM=$'\033[2m'; RESET=$'\033[0m'
else
    RED=""; YELLOW=""; CYAN=""; BOLD=""; DIM=""; RESET=""
fi

# Counts
declare -i HIGH_COUNT=0 MEDIUM_COUNT=0 LOW_COUNT=0 TOTAL_COUNT=0
declare -A PATTERN_COUNTS

# JSP/JSPF includes
JSP_FILES=( --include='*.jsp' --include='*.jspf' )

# ----- Helpers -----

# Should we report this finding?
should_report() {
    local id="$1" sev="$2"
    [ -n "$SEVERITY_FILTER" ] && [ "$sev" != "$SEVERITY_FILTER" ] && return 1
    [ -n "$PATTERN_FILTER" ]  && [ "$id" != "$PATTERN_FILTER" ]  && return 1
    return 0
}

# Emit a finding header. Returns the count of matches found.
# Args: id, severity, title, why, fix-hint, grep-cmd-fn-name
report_pattern() {
    local id="$1" sev="$2" title="$3" why="$4" fix="$5" cmd_fn="$6"

    should_report "$id" "$sev" || return 0

    # Run the search function and capture results
    local results
    results="$($cmd_fn 2>/dev/null)" || true
    [ -z "$results" ] && return 0

    local count
    count=$(printf '%s\n' "$results" | grep -c .)

    PATTERN_COUNTS[$id]=$count
    case "$sev" in
        high)   HIGH_COUNT=$((HIGH_COUNT + count));   local color="$RED" ;;
        medium) MEDIUM_COUNT=$((MEDIUM_COUNT + count)); local color="$YELLOW" ;;
        low)    LOW_COUNT=$((LOW_COUNT + count));    local color="$CYAN" ;;
    esac
    TOTAL_COUNT=$((TOTAL_COUNT + count))

    printf '\n%s%s[%s] %s%s%s — %d match(es)\n' "$color" "$BOLD" "$id" "$sev" " " "$RESET" "$count"
    printf '   %sWhy:%s %s\n' "$DIM" "$RESET" "$why"
    printf '   %sFix:%s %s\n' "$DIM" "$RESET" "$fix"
    if $SHOW_ALL; then
        printf '%s' "$results" | sed "s|^$REPO_ROOT/||" | sed 's/^/   /'
    else
        printf '%s' "$results" | head -n 5 | sed "s|^$REPO_ROOT/||" | sed 's/^/   /'
        if [ "$count" -gt 5 ]; then
            printf '   %s... and %d more (run with --pattern=%s --all)%s\n' "$DIM" "$((count - 5))" "$id" "$RESET"
        fi
    fi
}

# ----- Pattern definitions -----
# Each pattern is a function returning grep output (file:line:content).
# Group A: HIGH severity (likely real bugs, breaking UI or security)
# Group B: MEDIUM severity (probably wrong context)
# Group C: LOW severity (cosmetic / noisy / minor)

# ============================================================
# A. HIGH SEVERITY - actual bugs
# ============================================================

# A1: Encode.forHtml wrapping HTML markup with embedded ternary -- markup gets escaped to text
pattern_A1() {
    grep -rEn 'Encode\.forHtml\([^)]*\?[^)]*"<(input|form|a |div|span|td|tr|table|button|select|option|abbr|b>|i>|font)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A2: Encode.forHtml wrapping ENTIRE constructed HTML (including with user input)
pattern_A2() {
    grep -rEn 'Encode\.forHtml\([^)]*"<[a-z]+[^"]*"\s*\+' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A3: Encode.forJavaScript in URL parameter context (causes \- warnings, \x26 corrupts URLs)
pattern_A3() {
    grep -rEn '([?&]|&amp;)[a-zA-Z_][a-zA-Z0-9_]*=<%=Encode\.forJavaScript' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A4: Encode.forJavaScript on numeric/boolean/null result, used as JS literal (no surrounding quotes)
# Pattern: var x = <%=...%>;  (where ; immediately follows %>)
pattern_A4() {
    grep -rEn '=\s*<%=Encode\.forJavaScript\(String\.valueOf\((true|false|null|[a-zA-Z_]+\.size\(\)\s*[<>=]+|[0-9]+)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A5: Encode wrapping a method whose name ends with "Html" or starts with "get*Html"
pattern_A5() {
    grep -rEn 'Encode\.(forHtml|forJavaScript|forHtmlAttribute|forUriComponent)\([^)]*\.(get[A-Z][a-zA-Z]*[Hh]tml|popError[a-zA-Z]*Html|.*[Aa]sHtml|getEChartLinks)\s*\(' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A6: Double-encoding: variable that's already encoded with Encode.forHtmlContent earlier, then encoded again
# Heuristic: variable assigned with Encode.forHtmlContent, output via <%=Encode.forHtml(var)%>
pattern_A6() {
    # Find files with both patterns, then check if same var name appears
    grep -rln 'Encode\.forHtmlContent' "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | while read -r file; do
        awk '
            /=\s*Encode\.forHtmlContent\(/  { match($0, /[a-zA-Z_][a-zA-Z0-9_]*\s*=\s*Encode\.forHtmlContent/); if (RSTART) { sub(/\s*=.*/, "", $0); n=split($0,a," "); v=a[n]; vars[v]=NR } }
            /<%=Encode\.forHtml\(/          { for (v in vars) if (index($0, "Encode.forHtml(" v ")")) print FILENAME":"NR":"$0 }
        ' "$file" 2>/dev/null
    done
}

# A7: Encode.forJavaScript wrapping high-confidence date values (produces \- escapes that Firefox warns about)
# Tight pattern: date-format strings, java.util.Date methods, vars literally named "*Date"
pattern_A7() {
    grep -rEn 'Encode\.forJavaScript\([^)]*(currentDate|dateString|formatDate|toISOString|format\(new Date|new SimpleDateFormat|sumDate|"yyyy-MM-dd"|"yyyy/MM/dd"|"[0-9]{4}-[0-9]{2}-[0-9]{2}"|getBirthDate|appointmentDate|getApptDate|getServiceDate|getCurDate|getStartDate|getEndDate|observationDate|getDOB)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | grep -vE 'forJavaScriptAttribute|forJavaScriptBlock|forJavaScriptSource'
}

# A7b: Encode.forJavaScript wrapping values that MAY contain hyphens (medium confidence)
# Names, codes, identifiers that often contain hyphens
pattern_A7b() {
    grep -rEn 'Encode\.forJavaScript\([^)]*(getFirstName|getLastName|getFullName|getPatientName|getDoctorName|getProviderName|getDescription|getCode|getServiceCode|getDiagnosticCode|getICD|getSnomed|getReason|getComment|getAddress|getCity|getProvince|getEmail|getHin|hyphen)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | grep -vE 'forJavaScriptAttribute|forJavaScriptBlock|forJavaScriptSource'
}

# A10: Encode.forJavaScript producing literal hyphen escapes (yyyy-MM-dd etc.) - verified at runtime by checking if `\-` would be in output
# This pattern catches concatenated year-month-day expressions
pattern_A10() {
    grep -rEn 'Encode\.forJavaScript\([^)]*\+\s*"-"\s*\+' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A11: Encode used on a value that came from another encoder (chained encoding)
pattern_A11() {
    grep -rEn 'Encode\.\w+\([^)]*Encode\.\w+\(' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A12: Suspicious value attribute without quotes (HTML-attr injection risk)
pattern_A12() {
    grep -rEn 'value=<%=' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A8: HTML attribute value containing user data without forHtmlAttribute encoding
# value="<%=request.getParameter(...)%>" -- raw, no encoding at all
pattern_A8() {
    grep -rEn 'value=["'"'"']<%=\s*(request\.getParameter|String\.valueOf\(request\.getParameter)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# A9: JSP scriptlet output with NO encoding at all in a clearly user-data context
pattern_A9() {
    grep -rEn '<%=\s*request\.getParameter\([^)]+\)\s*%>' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | head -50
}

# ============================================================
# B. MEDIUM SEVERITY - wrong context but not always broken
# ============================================================

# B1: Encode.forJavaScript wrapping a String.valueOf(boolean) outputting as JS literal
pattern_B1() {
    grep -rEn 'var\s+\w+\s*=\s*<%=Encode\.forJavaScript\(String\.valueOf' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | grep -vE '<%=Encode\.forJavaScript\([^)]*\)%>";'
}

# B2: Double URL encoding: Encode.forUriComponent wrapping URLEncoder.encode result
pattern_B2() {
    grep -rEn 'Encode\.forUriComponent\([^)]*URLEncoder\.encode' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# B3: forUriComponent encoding a URL path (output contains slashes, would be wrongly %2F-encoded)
# Detected by looking at variable name patterns: *Path, *Url, *URL, *Uri
pattern_B3() {
    grep -rEn 'Encode\.forUriComponent\([^)]*(\.getPath\(\)|\.getURL\(\)|getUrl\(\)|[Pp]ath|[Uu]rl)\b' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# B4: Encoded value used inside CSS context (style="...<%=...%>...")
# Only flag forHtml/forJavaScript/forUriComponent — forHtmlAttribute inside style is OK-ish
# (the HTML-attribute encoding still prevents attribute escape, even if not CSS-strict)
pattern_B4() {
    grep -rEn 'style=["'"'"'][^"'"'"']*<%=Encode\.(forHtml|forJavaScript|forUriComponent)\(' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | grep -v 'forHtmlAttribute'
}

# B5: Encode.forJavaScript() inside an href URL (should be forUriComponent)
pattern_B5() {
    grep -rEn 'href=["'"'"'][^"'"'"']*<%=Encode\.forJavaScript' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# B6: forHtmlAttribute wrapping HTML markup (escapes the tags)
pattern_B6() {
    grep -rEn 'Encode\.forHtmlAttribute\([^)]*"<[a-z]' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# B7: Direct getProperty("DOCUMENT_DIR") usage — should use getDocumentDirectory()
pattern_B7() {
    grep -rEn '\.getProperty\("DOCUMENT_DIR"\)' \
        --include='*.java' --include='*.jsp' --include='*.jspf' \
        "$REPO_ROOT/src/main" 2>/dev/null | grep -vE 'OscarProperties\.java'
}

# B8: File operations on user input without PathValidationUtils
# Look for: new File(request.getParameter(...))
pattern_B8() {
    grep -rEn 'new\s+File\s*\([^)]*request\.getParameter' \
        --include='*.java' --include='*.jsp' --include='*.jspf' \
        "$REPO_ROOT/src/main" 2>/dev/null
}

# B9: SQL string concatenation with request.getParameter (SQL injection)
pattern_B9() {
    grep -rEn '(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE)[^"]*"\s*\+\s*request\.getParameter' \
        --include='*.java' --include='*.jsp' --include='*.jspf' \
        "$REPO_ROOT/src/main" 2>/dev/null
}

# B10: Direct java.io.FileInputStream / FileOutputStream / FileReader / FileWriter on user input
pattern_B10() {
    grep -rEn 'new\s+(FileInputStream|FileOutputStream|FileReader|FileWriter)\s*\([^)]*request\.getParameter' \
        --include='*.java' --include='*.jsp' --include='*.jspf' \
        "$REPO_ROOT/src/main" 2>/dev/null
}

# B11: Encode.forHtml(...) that includes a manually-built HTML fragment with " or '
# Pattern: Encode.forHtml(... + "\"" + ...) or "'" inside the encoded expression
pattern_B11() {
    grep -rEn 'Encode\.forHtml\([^)]*"\\"' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# ============================================================
# C. LOW SEVERITY - cosmetic / noisy
# ============================================================

# C1: Encode.forHtml around a static-string-returning helper (e.g., "checked"/"selected")
# Pattern: Encode.forHtml(getChecked(...)) or similar getCheckedValue type helpers
pattern_C1() {
    grep -rEn 'Encode\.forHtml\([^)]*(getChecked|getSelected|getDisabled|getReadonly)\s*\(' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# C2: Useless String.valueOf wrap on already-String values
pattern_C2() {
    grep -rEn 'String\.valueOf\(\s*"[^"]*"\s*\)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# C3: Encode methods used on hardcoded string literals
pattern_C3() {
    grep -rEn 'Encode\.\w+\(\s*"[a-zA-Z0-9_/.: -]*"\s*\)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null
}

# C4: forJavaScript on values likely to be int/long IDs (no real benefit, not wrong though)
pattern_C4() {
    grep -rEn '<%=Encode\.forJavaScript\(String\.valueOf\([a-zA-Z_]*\.(getId|getNo|getNumber|getDemographicNo|getProviderNo|getAppointmentNo|getBillingNo)\(\)\)\)%>' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | head -30
}

# C5: Encode.forHtml(String.valueOf(...)) where the value is a primitive Long/Integer (waste; just toString)
pattern_C5() {
    grep -rEn 'Encode\.forHtml\(String\.valueOf\([a-zA-Z_]+\.(getId|getNo)\(\)\)\)' \
        "${JSP_FILES[@]}" "$SCAN_PATH" 2>/dev/null | head -20
}

# ============================================================
# Main
# ============================================================

cat <<HEADER
${BOLD}OpenO Encoding Regression Scanner${RESET}
Scanning: ${SCAN_PATH/$REPO_ROOT/}
${SEVERITY_FILTER:+Severity filter: $SEVERITY_FILTER}
${PATTERN_FILTER:+Pattern filter: $PATTERN_FILTER}

Severity legend:
  ${RED}HIGH${RESET}    likely bug — broken UI, XSS bypass, or URL corruption
  ${YELLOW}MEDIUM${RESET}  wrong-context encoder; may break in edge cases
  ${CYAN}LOW${RESET}     cosmetic / inefficient / noisy but not broken
HEADER

echo
echo "${BOLD}========== HIGH SEVERITY ==========${RESET}"

report_pattern A1 high \
    "Encode.forHtml wraps HTML markup with embedded ternary" \
    "Whole HTML element gets escaped and rendered as text instead of as a tag." \
    "Drop the Encode.forHtml() wrapper, or properly encode just the user-input part inside the markup." \
    pattern_A1

report_pattern A2 high \
    "Encode.forHtml wrapping concatenated HTML" \
    "Tags get HTML-escaped, displayed as visible markup text." \
    "Drop the wrapper or restructure to encode only user-input portions." \
    pattern_A2

report_pattern A3 high \
    "Encode.forJavaScript in URL parameter context" \
    "forJavaScript escapes \`-\` to \\\\- (Firefox warns) and \`&\` to \\\\x26 (corrupts URL: ?a=b\\\\x26c=d decodes to ?a=b&c=d)." \
    "Replace with Encode.forUriComponent." \
    pattern_A3

report_pattern A4 high \
    "Encode.forJavaScript on bool/null/numeric, output as JS literal" \
    "var x = <%=Encode.forJavaScript(...)%>; without surrounding quotes — fragile if value isn't strictly numeric." \
    "Either compute the boolean server-side (\`<%=\"true\".equals(x)%>\`) or wrap with quotes for string literal." \
    pattern_A4

report_pattern A5 high \
    "Encoder wraps a method that returns HTML (name suggests HTML output)" \
    "Method like popErrorAndInfoMessagesAsHtml() returns HTML — encoding it displays the markup as text." \
    "Remove the wrapper. The method's return type indicates trusted HTML." \
    pattern_A5

report_pattern A6 high \
    "Double encoding: var encoded with forHtmlContent, then re-encoded at output" \
    "User sees literal &amp;lt;, &amp;gt;, &amp;#39; etc. instead of the original characters." \
    "Encode once. Either at assignment or at output, not both." \
    pattern_A6

report_pattern A7 high \
    "Encode.forJavaScript wrapping a date/time value" \
    "Hyphens in dates (yyyy-MM-dd) become \\\\- which Firefox flags as 'invalid escape sequence'." \
    "Use Encode.forUriComponent if it's in a URL, or compute the value as a number/literal." \
    pattern_A7

report_pattern A8 high \
    "HTML attribute value with raw request.getParameter" \
    "Untrusted input directly in HTML attribute — XSS risk via \" injection." \
    "Wrap with Encode.forHtmlAttribute()." \
    pattern_A8

report_pattern A9 high \
    "JSP output of request.getParameter with NO encoding" \
    "Reflected XSS — user input rendered raw into the page." \
    "Wrap with the appropriate encoder for the surrounding context." \
    pattern_A9

report_pattern A10 high \
    "Encode.forJavaScript on year/month/day concatenation" \
    "Each piece gets JS-escaped, then joined with hyphens — same \\- problem as dates." \
    "Build the date string first, then encode once with forUriComponent, OR use forUriComponent on each piece if URL context." \
    pattern_A10

report_pattern A11 high \
    "Chained encoders (encoder applied to encoder output)" \
    "Double-encoding: first encoder escapes characters, second encoder escapes the escaped chars again. Visible garbage." \
    "Pick ONE encoder appropriate for the final output context." \
    pattern_A11

report_pattern A12 high \
    "HTML attribute value missing quotes (value=<%=...%>)" \
    "Value not enclosed in quotes — attribute injection trivial via space character. forHtmlAttribute helps but quoted form is safer." \
    "Quote the attribute: value=\"<%=...%>\"" \
    pattern_A12

echo
echo "${BOLD}========== MEDIUM SEVERITY ==========${RESET}"

report_pattern A7b medium \
    "Encode.forJavaScript on values likely to contain hyphens (names, codes, addresses)" \
    "If the value contains a hyphen (e.g. 'O-Brien', '450-12-30'), forJavaScript escapes it to \\\\-, triggering Firefox 'invalid escape sequence' warnings." \
    "If output context is a URL, use forUriComponent. If it's a JS string literal (with quotes), forJavaScript is fine but produces noisy escapes for hyphens." \
    pattern_A7b

report_pattern B1 medium \
    "var x = <%=Encode.forJavaScript(String.valueOf(...))%>; (no quotes)" \
    "Output is unquoted in JS; if value isn't strictly numeric/boolean/null, it's a syntax error." \
    "Add surrounding quotes, or use Encode.forJavaScript for an unquoted-safe context." \
    pattern_B1

report_pattern B2 medium \
    "Double URL encoding (Encode.forUriComponent + URLEncoder.encode)" \
    "Value gets URL-encoded twice: %20 becomes %2520. Server decodes only once → bad value." \
    "Use only one URL encoder. Drop the inner URLEncoder.encode if forUriComponent is present." \
    pattern_B2

report_pattern B3 medium \
    "Encode.forUriComponent on a URL path (with slashes)" \
    "Path slashes get encoded to %2F, which routers may treat differently than literal /." \
    "Use Encode.forUri() for full URL paths, or no encoding for trusted internal paths." \
    pattern_B3

report_pattern B4 medium \
    "Encoded value inside style=\"...\" attribute" \
    "CSS context needs Encode.forCssString() or forCssUrl() — HTML/JS encoders may not protect from CSS-context injection." \
    "Use Encode.forCssString() inside style attributes." \
    pattern_B4

report_pattern B5 medium \
    "Encode.forJavaScript() inside an href URL" \
    "URL params should use forUriComponent. forJavaScript escapes & to \\\\x26 which corrupts the URL on parse." \
    "Replace with Encode.forUriComponent." \
    pattern_B5

report_pattern B6 medium \
    "Encode.forHtmlAttribute wrapping HTML tags" \
    "Tag chars get escaped, attribute value contains visible &lt;/&gt; markup as text." \
    "Drop wrapper for static markup, or split into structure-vs-data." \
    pattern_B6

report_pattern B7 medium \
    "Direct getProperty(\"DOCUMENT_DIR\") instead of getDocumentDirectory()" \
    "Bypasses helper that handles fallback to BASE_DOCUMENT_DIR, trim, and trailing separator." \
    "Replace with OscarProperties.getInstance().getDocumentDirectory()." \
    pattern_B7

report_pattern B8 medium \
    "new File(request.getParameter(...)) — path traversal candidate" \
    "User input flows directly into a File object. PathValidationUtils should mediate." \
    "Use PathValidationUtils.validatePath(filename, allowedDir)." \
    pattern_B8

report_pattern B9 medium \
    "SQL string concatenation with request.getParameter() (SQL injection risk)" \
    "User input concatenated into SQL statement. Use parameterized queries via PreparedStatement / JPA setParameter." \
    "Replace string-concat with placeholders and bind parameters." \
    pattern_B9

report_pattern B10 medium \
    "FileInputStream/FileOutputStream/FileReader/FileWriter on user input" \
    "Same path-traversal class as B8 but for stream constructors." \
    "Validate via PathValidationUtils.validatePath() first." \
    pattern_B10

report_pattern B11 medium \
    "Encode.forHtml() expression contains escaped double-quote (\\\")" \
    "Often a sign of HTML being constructed inside the encoded expression — encoding will escape the markup." \
    "Restructure: build the HTML outside the encoder, encode only user-data portions." \
    pattern_B11

echo
echo "${BOLD}========== LOW SEVERITY ==========${RESET}"

report_pattern C1 low \
    "Encode.forHtml on getChecked/getSelected helpers (returns 'checked'/'')" \
    "Helper returns a static safe string — encoding is unnecessary, just clutters output." \
    "Drop the wrapper for these specific helpers." \
    pattern_C1

report_pattern C2 low \
    "String.valueOf on a string literal" \
    "Useless wrap — String.valueOf(\"x\") = \"x\". Bulk script left these everywhere." \
    "Drop the String.valueOf for string-literal arguments." \
    pattern_C2

report_pattern C3 low \
    "Encoder applied to a hardcoded string literal" \
    "Encoding a literal you wrote is pointless — values are static and known-safe." \
    "Drop the wrapper for string literals." \
    pattern_C3

report_pattern C4 low \
    "Encode.forJavaScript on numeric ID accessors (getId, getNo, getDemographicNo, etc.)" \
    "Numeric IDs don't have any chars that need JS escaping — overhead with no benefit." \
    "Drop wrapper, or use String.valueOf alone for numeric IDs." \
    pattern_C4

report_pattern C5 low \
    "Encode.forHtml on numeric ID accessors" \
    "Numeric IDs don't have HTML-special chars. Encoding is wasted work." \
    "Drop wrapper, or use String.valueOf alone." \
    pattern_C5

# ----- Summary -----

cat <<SUMMARY

${BOLD}========== Summary ==========${RESET}
  ${RED}High:${RESET}    $HIGH_COUNT
  ${YELLOW}Medium:${RESET}  $MEDIUM_COUNT
  ${CYAN}Low:${RESET}     $LOW_COUNT
  Total:   $TOTAL_COUNT

Run with --pattern=<id> (e.g. --pattern=A1) to see all findings for a single pattern.
Run with --severity=<level> (high/medium/low) to filter.
SUMMARY

# Exit code: 1 if any HIGH findings, 0 otherwise. Useful for CI gating.
[ "$HIGH_COUNT" -gt 0 ] && exit 1 || exit 0
