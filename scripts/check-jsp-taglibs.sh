#!/bin/bash

# check-jsp-taglibs.sh

echo "Checking JSP files for missing JSTL taglib declarations..."
echo

declare -A tags=(
    ["fmt"]="java.sun.com/jsp/jstl/fmt"
    ["c"]="java.sun.com/jsp/jstl/core"
    ["fn"]="java.sun.com/jsp/jstl/functions"
    ["sql"]="java.sun.com/jsp/jstl/sql"
    ["xml"]="java.sun.com/jsp/jstl/xml"
)

# Common taglib include file patterns
TAGLIB_INCLUDES="taglibs\.jsp|taglibs\.jspf|common-taglibs\.jsp"

found_issues=0

while IFS= read -r -d '' file; do
    missing=()
    
    # Check if file includes a taglibs file
    has_taglib_include=$(grep -E "include.*(${TAGLIB_INCLUDES})" "$file")
    
    for prefix in "${!tags[@]}"; do
        # Check if tag is used
        if grep -q "<${prefix}:" "$file"; then
            # Skip check if taglibs file is included
            if [ -n "$has_taglib_include" ]; then
                continue
            fi
            
            # Check if taglib is declared directly
            if ! grep -q "taglib.*${tags[$prefix]}" "$file"; then
                missing+=("$prefix")
            fi
        fi
    done
    
    if [ ${#missing[@]} -gt 0 ]; then
        echo "$file"
        echo "   Missing taglib(s): ${missing[*]}"
        echo
        ((found_issues++))
    fi
done < <(find . -name "*.jsp" -type f -print0)

echo "---"
if [ $found_issues -eq 0 ]; then
    echo "No issues found!"
else
    echo "Found $found_issues file(s) with missing taglib declarations"
fi