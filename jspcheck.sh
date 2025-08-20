#!/bin/bash
# JSP Usage Analysis (Indexed)P Usage Analysis (Indexed)
# - Scans Java/HTML/JS/JSP/XML/props/yaml once, then aggregates per JSP
# - Supports Spring 3–5-style view resolution
# Requirements: bash 3.2+, GNU grep/sed/find/awk
# Usage: ./jspcheck.sh [webapp_dir] [java_src]

set -euo pipefail

# ---- Setup & checks ---------------------------------------------------------
if [[ -z "${BASH_VERSION:-}" ]]; then
  echo "Error: This script requires bash." >&2
  exit 1
fi

WEBAPP_DIR="${1:-src/main/webapp}"
JAVA_SRC="${2:-src/main/java}"
WEBAPP_DIR="${WEBAPP_DIR%/}"
JAVA_SRC="${JAVA_SRC%/}"

# Colors iff TTY
if [[ -t 1 ]]; then
  RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[1;33m'; BLUE=$'\033[0;34m'; NC=$'\033[0m'
else
  RED=; GREEN=; YELLOW=; BLUE=; NC=
fi

for cmd in grep sed find wc sort mktemp awk cut tr xargs; do
  command -v "$cmd" >/dev/null 2>&1 || { echo "Error: '$cmd' not found." >&2; exit 1; }
done

[[ -d "$WEBAPP_DIR" ]] || { echo -e "${RED}Error: Webapp dir not found: $WEBAPP_DIR${NC}" >&2; exit 1; }
[[ -d "$JAVA_SRC"   ]] || { echo -e "${RED}Error: Java src dir not found: $JAVA_SRC${NC}"   >&2; exit 1; }

tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT

# Helper: safe line count without breaking -e/-o pipefail
count_lines() { { "$@" 2>/dev/null || true; } | wc -l; }

echo -e "${BLUE}JSP Usage Analysis (Indexed)${NC}"
echo "============================="
echo "Webapp: $WEBAPP_DIR"
echo "Java:   $JAVA_SRC"
echo ""

# ---- Inventory JSP-like files ----------------------------------------------
all_jsps="$tmp/all_jsps.txt"
find "$WEBAPP_DIR" -type f \( -name '*.jsp' -o -name '*.jspf' -o -name '*.tag' \) -print \
  | sort > "$all_jsps"
total_jsps=$(wc -l < "$all_jsps")
if [[ "$total_jsps" -eq 0 ]]; then
  echo -e "${YELLOW}No JSP/JSPF/TAG files found in $WEBAPP_DIR${NC}"
  exit 0
fi

jsp_count=$(find "$WEBAPP_DIR" -type f -name '*.jsp'  -print | wc -l)
jspf_count=$(find "$WEBAPP_DIR" -type f -name '*.jspf' -print | wc -l)
tag_count=$(find "$WEBAPP_DIR" -type f -name '*.tag'  -print | wc -l)
echo "Found $jsp_count JSP, $jspf_count JSPF, $tag_count TAG ($total_jsps total)"
echo -e "${YELLOW}Note: errs on the side of 'USED' to avoid false negatives.${NC}"
echo ""

# ---- Extract Spring view names from Java -----------------------------------
echo -e "${BLUE}Extracting Spring view names and annotations...${NC}"
spring_views_file="$tmp/spring_views.txt"
{
  # return "view";
  grep -rh --include='*.java' 'return[[:space:]]\+"\([^"]\+\)"' "$JAVA_SRC" \
    | sed -n 's/.*return[[:space:]]\+"\([^"]\+\)".*/\1/p' || true
  # new ModelAndView("view")
  grep -rh --include='*.java' 'ModelAndView[[:space:]]*([[:space:]]*"\([^"]\+\)"' "$JAVA_SRC" \
    | sed -n 's/.*ModelAndView[[:space:]]*([[:space:]]*"\([^"]\+\)".*/\1/p' || true
  # setViewName("view")
  grep -rh --include='*.java' 'setViewName[[:space:]]*([[:space:]]*"\([^"]\+\)"' "$JAVA_SRC" \
    | sed -n 's/.*setViewName[[:space:]]*([[:space:]]*"\([^"]\+\)".*/\1/p' || true
  # @RequestMapping(..., view="view")
  grep -rh --include='*.java' '@RequestMapping.*view[[:space:]]*=[[:space:]]*"\([^"]\+\)"' "$JAVA_SRC" \
    | sed -n 's/.*view[[:space:]]*=[[:space:]]*"\([^"]\+\)".*/\1/p' || true
  # @Get/Post/etc Mapping(..., view="view")
  grep -rh --include='*.java' '@\(Get\|Post\|Put\|Delete\|Patch\)Mapping.*view[[:space:]]*=[[:space:]]*"\([^"]\+\)"' "$JAVA_SRC" \
    | sed -n 's/.*view[[:space:]]*=[[:space:]]*"\([^"]\+\)".*/\1/p' || true
  # addViewController(...).setViewName("view")
  grep -rh --include='*.java' 'addViewController.*\.setViewName[[:space:]]*([[:space:]]*"\([^"]\+\)"' "$JAVA_SRC" \
    | sed -n 's/.*setViewName[[:space:]]*([[:space:]]*"\([^"]\+\)".*/\1/p' || true
} | grep -vE '^(redirect:|forward:|classpath:|https?://|//|$)' \
  | sort -u > "$spring_views_file"
spring_view_count=$(wc -l < "$spring_views_file")
echo "Found $spring_view_count Spring view names"
echo ""

# ---- Properties & YAML (view-like values) ----------------------------------
echo -e "${BLUE}Checking properties and YAML files...${NC}"
properties_views_file="$tmp/properties_views.txt"
: > "$properties_views_file"

property_locations=(
  "src/main/resources"
  "src/test/resources"
  "$WEBAPP_DIR/WEB-INF"
  "$WEBAPP_DIR/WEB-INF/classes"
)

for location in "${property_locations[@]}"; do
  [[ -d "$location" ]] || continue
  while IFS= read -r prop_file; do
    { grep -hE '\b(view|jsp|page|template)[[:space:]]*[=:][[:space:]]*[^[:space:],;#]+' "$prop_file" \
        | sed -nE 's/.*[=:][[:space:]]*([^[:space:],;#}"]+).*/\1/p' || true; } \
      >> "$properties_views_file"
  done < <(find "$location" -type f \( -name '*.properties' -o -name '*.yml' -o -name '*.yaml' \) -print 2>/dev/null)
done

if [[ -s "$properties_views_file" ]]; then
  sort -u -o "$properties_views_file" "$properties_views_file"
  properties_view_count=$(wc -l < "$properties_views_file")
else
  properties_view_count=0
fi
echo "Found $properties_view_count view references in properties/YAML files"
echo ""

# ---- Build pattern lists for one-pass indexing ------------------------------
# For each JSP, we will look for several variants. We build a *global* pattern file,
# then grep once per area and cache outputs.

java_pat="$tmp/java_patterns.txt"
jsp_pat="$tmp/jsp_patterns.txt"
html_pat="$tmp/html_patterns.txt"
js_pat="$tmp/js_patterns.txt"
xml_pat="$tmp/xml_patterns.txt"

: > "$java_pat"; : > "$jsp_pat"; : > "$html_pat"; : > "$js_pat"; : > "$xml_pat"

while IFS= read -r jsp_file; do
  jsp_name="$(basename "$jsp_file")"
  jsp_name_no_ext="${jsp_name%.*}"
  jsp_path="${jsp_file#$WEBAPP_DIR/}"
  jsp_path_no_ext="${jsp_path%.*}"

  # Java: prefer quoted references to reduce false positives
  printf '%s\n' \
    "\"$jsp_name\"" "\"$jsp_path\"" "\"/$jsp_path\"" "\"$jsp_name_no_ext\"" "\"$jsp_path_no_ext\"" \
    "'$jsp_name'" "'$jsp_path'" "'/$jsp_path'" "'$jsp_name_no_ext'" "'$jsp_path_no_ext'" \
    >> "$java_pat"

  # JSP cross-refs (includes/forwards/etc). Fixed-string OK; we’ll exclude self later.
  printf '%s\n' "$jsp_name" "$jsp_path" "/$jsp_path" >> "$jsp_pat"

  # HTML refs: typically file name or path
  printf '%s\n' "$jsp_name" "$jsp_path" "/$jsp_path" >> "$html_pat"

  # JavaScript refs (quoted most of the time)
  printf '%s\n' \
    "'$jsp_name'" "\"$jsp_name\"" "'$jsp_path'" "\"$jsp_path\"" \
    "'/$jsp_path'" "\"/$jsp_path\"" "'$jsp_name_no_ext'" "\"$jsp_name_no_ext\"" \
    >> "$js_pat"

  # XML: name, path, or no-ext
  printf '%s\n' "$jsp_name" "$jsp_path" "$jsp_name_no_ext" "$jsp_path_no_ext" >> "$xml_pat"
done < "$all_jsps"

# Dedup patterns
sort -u -o "$java_pat" "$java_pat"
sort -u -o "$jsp_pat"  "$jsp_pat"
sort -u -o "$html_pat" "$html_pat"
sort -u -o "$js_pat"   "$js_pat"
sort -u -o "$xml_pat"  "$xml_pat"

# ---- One-pass indexing (the heavy lifting, done once) -----------------------
echo -e "${BLUE}Indexing codebase once for references...${NC}"

JAVA_ALL="$tmp/java_all_refs.txt"
JSP_ALL="$tmp/jsp_all_refs.txt"
HTML_ALL="$tmp/html_all_refs.txt"
JS_JS_ALL="$tmp/js_js_all_refs.txt"
JS_INLINE_ALL="$tmp/js_inline_all_refs.txt"
XML_ALL="$tmp/xml_all_refs.txt"

# Java
{ grep -r -H -n --include='*.java' -F -f "$java_pat" "$JAVA_SRC" || true; } > "$JAVA_ALL"

# JSP/JSPF/TAG (cross-refs)
{ grep -r -H -n -F -f "$jsp_pat" "$WEBAPP_DIR" --include='*.jsp' --include='*.jspf' --include='*.tag' || true; } > "$JSP_ALL"

# HTML
{ grep -r -H -n -i -F -f "$html_pat" "$WEBAPP_DIR" --include='*.html' --include='*.htm' || true; } > "$HTML_ALL"

# JS in .js files
{ grep -r -H -n -F -f "$js_pat" "$WEBAPP_DIR" --include='*.js' || true; } > "$JS_JS_ALL"

# Inline JS inside JSP/HTML
{ grep -r -H -n -F -f "$js_pat" "$WEBAPP_DIR" --include='*.jsp' --include='*.html' || true; } > "$JS_INLINE_ALL"

# XML (restrict to common config locations)
xml_config_locations=(
  "$WEBAPP_DIR/WEB-INF"
  "src/main/resources"
  "src/main/resources/META-INF"
  "src/main/resources/META-INF/spring"
)
: > "$XML_ALL"
for loc in "${xml_config_locations[@]}"; do
  [[ -d "$loc" ]] || continue
  { grep -r -H -n -F -f "$xml_pat" "$loc" --include='*.xml' || true; } >> "$XML_ALL"
done

# ---- Dynamic JSP patterns / annotations (informational) ---------------------
echo -e "${BLUE}Scanning for dynamic JSP patterns in Java...${NC}"
dyn_any=false
for pat in \
  'String.*\.jsp' 'StringBuilder.*\.jsp' 'getProperty.*jsp' \
  'request\.getParameter.*jsp' 'getAttribute.*jsp' \
  'RequestDispatcher.*[$+]' 'getRequestDispatcher.*\+' \
  '\.jsp.*\+' 'String\.format.*jsp' '@Value.*jsp'
do
  m="$(grep -r --include='*.java' -E "$pat" "$JAVA_SRC" 2>/dev/null || true)"
  if [[ -n "$m" ]]; then
    [[ $dyn_any == false ]] && { echo -e "${YELLOW}Dynamic JSP loading patterns found:${NC}"; dyn_any=true; }
    printf '%s\n' "$m" | awk 'NR<=2{print "  "$0}'
    mc=$(printf '%s\n' "$m" | wc -l); [[ $mc -gt 2 ]] && echo "  ... ($mc total matches for: $pat)"
  fi
done
[[ $dyn_any == false ]] && echo "No dynamic JSP patterns found"

echo -e "${BLUE}Checking for Spring Controller annotations...${NC}"
ann_any=false
for pat in \
  '@RequestMapping.*value.*\.jsp' \
  '@(Get|Post|Put|Delete|Patch)Mapping.*\.jsp' \
  '@Controller.*[[:space:]]*/.*\.jsp' \
  '@RestController.*[[:space:]]*/.*\.jsp'
do
  m="$(grep -r --include='*.java' -E "$pat" "$JAVA_SRC" 2>/dev/null || true)"
  if [[ -n "$m" ]]; then
    [[ $ann_any == false ]] && { echo -e "${YELLOW}JSP references in Spring annotations:${NC}"; ann_any=true; }
    printf '%s\n' "$m" | awk 'NR<=2{print "  "$0}'
  fi
done
[[ $ann_any == false ]] && echo "No JSP references in Spring annotations"
echo ""

# ---- TLD files --------------------------------------------------------------
echo -e "${BLUE}Checking Tag Library Descriptors...${NC}"
tld_files="$(find "$WEBAPP_DIR" -type f -name '*.tld' -print 2>/dev/null || true)"
tld_file_count=$(printf '%s\n' "$tld_files" | grep -c . || true)
[[ $tld_file_count -gt 0 ]] && echo "Found $tld_file_count TLD files" || echo "No TLD files found"
echo ""

# ---- Per-file aggregation from indexed results ------------------------------
echo -e "${BLUE}Analyzing JSP/JSPF/TAG usage...${NC}"
echo "=========================="
no_refs=0; has_refs=0; spring_views_hit=0

while IFS= read -r jsp_file; do
  jsp_name="$(basename "$jsp_file")"
  jsp_name_no_ext="${jsp_name%.*}"
  jsp_path="${jsp_file#$WEBAPP_DIR/}"
  jsp_path_no_ext="${jsp_path%.*}"

  echo -n "Checking: $jsp_path ... "

  # Build pattern list for this JSP
  java_pats=$(printf '%s\n' "\"$jsp_name\"" "\"$jsp_path\"" "\"/$jsp_path\"" "\"$jsp_name_no_ext\"" "\"$jsp_path_no_ext\"" \
                           "'$jsp_name'" "'$jsp_path'" "'/$jsp_path'" "'$jsp_name_no_ext'" "'$jsp_path_no_ext'")
  jsp_pats=$(printf '%s\n' "$jsp_name" "$jsp_path" "/$jsp_path")
  html_pats=$(printf '%s\n' "$jsp_name" "$jsp_path" "/$jsp_path")
  js_pats=$(printf '%s\n' \
              "'$jsp_name'" "\"$jsp_name\"" "'$jsp_path'" "\"$jsp_path\"" \
              "'/$jsp_path'" "\"/$jsp_path\"" "'$jsp_name_no_ext'" "\"$jsp_name_no_ext\"")
  xml_pats=$(printf '%s\n' "$jsp_name" "$jsp_path" "$jsp_name_no_ext" "$jsp_path_no_ext")

  total=0; details=""

  # Java refs
  java_refs=$(grep -F -f - "$JAVA_ALL" <<< "$java_pats" | wc -l || true)
  [[ $java_refs -gt 0 ]] && { total=$((total+java_refs)); details+="Java:$java_refs "; }

  # JSP cross-refs (exclude self by full path before colon)
  jsp_refs=$(grep -F -f - "$JSP_ALL" <<< "$jsp_pats" | awk -v self="$jsp_file" -F':' '$1!=self' | wc -l || true)
  [[ $jsp_refs -gt 0 ]] && { total=$((total+jsp_refs)); details+="JSP:$jsp_refs "; }

  # HTML refs
  html_refs=$(grep -F -f - "$HTML_ALL" <<< "$html_pats" | wc -l || true)
  [[ $html_refs -gt 0 ]] && { total=$((total+html_refs)); details+="HTML:$html_refs "; }

  # JS refs (.js and inline)
  js1=$(grep -F -f - "$JS_JS_ALL"    <<< "$js_pats" | wc -l || true)
  js2=$(grep -F -f - "$JS_INLINE_ALL"<<< "$js_pats" | awk -v self="$jsp_file" -F':' '$1!=self' | wc -l || true)
  js_refs=$((js1+js2))
  [[ $js_refs -gt 0 ]] && { total=$((total+js_refs)); details+="JS:$js_refs "; }

  # XML refs (unique files + list)
  xml_lines="$(grep -F -f - "$XML_ALL" <<< "$xml_pats" || true)"
  if [[ -n "$xml_lines" ]]; then
    xml_files=$(printf '%s\n' "$xml_lines" | cut -d: -f1 | sort -u)
    xml_refs=$(printf '%s\n' "$xml_files" | wc -l)
    xml_names=$(printf '%s\n' "$xml_files" | xargs -n1 basename | tr '\n' ' ')
    total=$((total+xml_refs)); details+="XML:$xml_refs(${xml_names}) "
  fi

  # Properties/YAML (any token hit -> +1)
  if [[ -s "$properties_views_file" ]] && \
     (grep -F -q "$jsp_name" "$properties_views_file" || \
      grep -F -q "$jsp_name_no_ext" "$properties_views_file" || \
      grep -F -q "$jsp_path" "$properties_views_file" || \
      grep -F -q "$jsp_path_no_ext" "$properties_views_file"); then
    total=$((total+1)); details+="Props:1 "
  fi

  # TLD refs for .tag files
  if [[ "${jsp_name##*.}" == "tag" && -n "$tld_files" ]]; then
    tld_refs=0
    while IFS= read -r tld; do
      [[ -f "$tld" ]] && grep -F -q "$jsp_name" "$tld" 2>/dev/null && ((tld_refs++)) || true
    done <<< "$tld_files"
    [[ $tld_refs -gt 0 ]] && { total=$((total+tld_refs)); details+="TLD:$tld_refs "; }
  fi

  # Spring view match
  sv=""
  if grep -F -x -q "$jsp_name_no_ext" "$spring_views_file"; then
    sv=" [Spring-View: $jsp_name_no_ext]"; ((spring_views_hit++))
  elif grep -F -x -q "$jsp_path_no_ext" "$spring_views_file"; then
    sv=" [Spring-View: $jsp_path_no_ext]"; ((spring_views_hit++))
  fi

  # Report
  if [[ $total -eq 0 && -z "$sv" ]]; then
    echo -e "${RED}UNUSED${NC}"
    ((no_refs++))
  elif [[ $total -eq 0 && -n "$sv" ]]; then
    echo -e "${YELLOW}SPRING VIEW (no static refs)${sv}${NC}"
    ((has_refs++))
  else
    if [[ $total -gt 10 ]]; then
      echo -e "${GREEN}USED${NC} (${details% })${sv} ${YELLOW}[High ref count]${NC}"
    else
      echo -e "${GREEN}USED${NC} (${details% })${sv}"
    fi
    ((has_refs++))
  fi
done < "$all_jsps"

# ---- Summary & unused list --------------------------------------------------
echo ""
echo -e "${BLUE}Summary${NC}"
echo "======="
echo "Total files analyzed: $total_jsps ($jsp_count JSP, $jspf_count JSPF, $tag_count TAG)"
echo -e "${GREEN}Files with references: $has_refs${NC}"
echo -e "${YELLOW}Spring views found: $spring_views_hit${NC}"
[[ -s "$XML_ALL" ]] && echo -e "${BLUE}XML refs indexed: $(wc -l < "$XML_ALL") lines${NC}"
[[ $properties_view_count -gt 0 ]] && echo -e "${BLUE}Properties/YAML references: $properties_view_count${NC}"
[[ $tld_file_count -gt 0 ]] && echo -e "${BLUE}TLD files found: $tld_file_count${NC}"
echo -e "${RED}Files with NO references: $no_refs${NC}"

if [[ $no_refs -gt 0 ]]; then
  echo ""
  echo -e "${BLUE}Files with no references (candidates for removal):${NC}"
  echo ""
  unused_files="$tmp/unused_files.txt"; : > "$unused_files"
  while IFS= read -r jsp_file; do
    jsp_name="$(basename "$jsp_file")"; jsp_path="${jsp_file#$WEBAPP_DIR/}"; jsp_name_no_ext="${jsp_name%.*}"
    any=false
    # Java quick
    if grep -F -q -e "\"$jsp_name\"" -e "\"$jsp_path\"" -e "\"$jsp_name_no_ext\"" "$JAVA_ALL"; then any=true; fi
    # JSP quick (exclude self)
    if [[ "$any" == false ]]; then
      if grep -F -q "$jsp_name" "$JSP_ALL" && ! grep -F -q "^$jsp_file:" "$JSP_ALL"; then any=true; fi
    fi
    # Spring views
    [[ "$any" == false ]] && grep -F -x -q "$jsp_name_no_ext" "$spring_views_file" && any=true
    # Properties
    [[ "$any" == false && -s "$properties_views_file" ]] && grep -F -q "$jsp_name_no_ext" "$properties_views_file" && any=true

    if [[ "$any" == false ]]; then
      echo "$jsp_file" | tee -a "$unused_files"
      # Risk hints
      risk=""
      case "$jsp_name" in
        error*|404*|500*|403*|401*) risk+="error-page " ;;
        index*|home*|default*|welcome*|main*) risk+="welcome-page " ;;
        login*|logout*|auth*|signin*|signout*|sso*) risk+="auth-page " ;;
        admin*|manage*|dashboard*|console*) risk+="admin-page " ;;
        test*|example*|sample*|demo*|temp*) risk+="test-page " ;;
        header*|footer*|nav*|menu*|sidebar*) risk+="template-fragment " ;;
      esac
      [[ "$jsp_path" =~ ^WEB-INF/ ]] && risk+="protected-location "
      [[ "${jsp_name##*.}" == "tag"  ]] && risk+="tag-file "
      [[ "${jsp_name##*.}" == "jspf" ]] && risk+="fragment "
      [[ -n "$risk" ]] && echo "   ⚠️  Risk factors: $risk" || echo "   ✅ Appears safe to remove"
    fi
  done < "$all_jsps"

  actual_unused=$(wc -l < "$unused_files")
  echo ""
  echo -e "${YELLOW}⚠️  Before removing any files:${NC}"
  echo "  1) Verify not referenced by external config / DB / descriptors / build / web server"
  echo "  2) Confirm not directly reachable via URL"
  echo "  3) Test in staging; back up first; consider deprecating then deleting"
  echo ""
  echo -e "${BLUE}Found $actual_unused genuinely unused files${NC}"
fi

echo ""
echo -e "${BLUE}Analysis complete!${NC}"

