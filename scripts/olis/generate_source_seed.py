#!/usr/bin/env python3
"""Generate the OLIS Source-nomenclature seed CSV from the OLIS Nomenclatures XLSX.

The "Source" sheet is a two-column lookup — Value (specimen source code) and
Description (display name) — used for CT 9.4 specimen-type display
(OLISHL7Handler resolves OBR-15-1-1 -> description). Output is tab-separated with
``\\N`` for nulls and no header, matching the LOAD DATA stanza in olisinit.sql and
the other OLIS seed CSVs.

Usage:
    python3 scripts/olis/generate_source_seed.py \\
        "OLIS Nomenclatures V3.04_PROD.xlsx" OLISSourceNomenclature.csv
"""
import csv
import sys

import openpyxl


def generate(xlsx_path: str, out_path: str) -> int:
    wb = openpyxl.load_workbook(xlsx_path, read_only=True, data_only=True)
    ws = wb["Source"]
    rows = ws.iter_rows(min_row=2, values_only=True)  # row 1 = header (Value, Description)
    count = 0
    with open(out_path, "w", newline="") as f:
        writer = csv.writer(f, delimiter="\t", lineterminator="\n",
                            quoting=csv.QUOTE_MINIMAL)
        for row in rows:
            if not row or row[0] is None:
                continue
            value = str(row[0]).strip()
            if not value:
                continue
            description = "" if (len(row) < 2 or row[1] is None) else str(row[1]).strip()
            writer.writerow([value, description if description else "\\N"])
            count += 1
    return count


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    n = generate(sys.argv[1], sys.argv[2])
    print(f"wrote {n} source-nomenclature rows -> {sys.argv[2]}")
