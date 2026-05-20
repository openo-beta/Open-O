# G1 — OLIS local smoke test plan

**Purpose.** End-to-end exercise of OLIS-touching surfaces on the `olis-validation` branch after the ~30 fixes in Tracks A-D. Catches anything that regressed in the noise, **plus** confirms that non-OLIS popup-callback flows healed by A32's COOP change (`mode: same-origin → same-origin-allow-popups`) are actually working again.

**When to run.** Before any external F1 conformance work begins. Ideally on a fresh devcontainer with seeded demo data.

**Time budget.** ~45 min if everything passes. Add another 30 min per regression to triage into an A-track entry.

## Pre-flight

```bash
# 1. Confirm you're on the right branch + clean state
git status
git log --oneline -5

# 2. Confirm olis_simulate is on (so we don't need a real OLIS connection)
grep -E "^olis_simulate" src/main/resources/oscar_mcmaster.properties
# expect: olis_simulate=yes

# 3. Confirm COOP header is the popup-friendly value
curl -s -I http://localhost:8080/oscar/olis/Search.do | grep -i cross-origin-opener
# expect: Cross-Origin-Opener-Policy: same-origin-allow-popups

# 4. Clean build + deploy
make clean && make install
# wait for "Tomcat started." then for HTTP 200 on /oscar/index.jsp
```

## Section 1 — OLIS Z01 / Patient Query Preview flow

**Setup:** sign in as `openodoc / openo2025 / 2025`. Upload `docs/olis/sample-response.hl7` via `/oscar/olis/Simulate.jsp` so the simulator returns this fixture for any query.

1. **Run a Z01 query.**
   `/oscar/olis/Search.jsp` → Date period `2024-01-01` to `2024-12-31` → Patient field `9999999999` → Search.
   - Expect: 1 unmatched row showing "JANE Q DOE / 9999999999" with red "Unmatched" + `here.gif` icon.
   - Bug to watch for: blank table / "No results found" → simulator session lost or rebuild wiped session attribute.

2. **Inspect the row before clicking.**
   Open the eye-icon preview pane.
   - Expect: full `labDisplayOLIS.jsp` content with name "JANE Q DOE", HIN 9999999999, DOB, sex, address, every PID field rendered cleanly (no `<span>` markup leaks).
   - Bug to watch for: any literal `<span>`/`<u>`/`<br />` text → D3 work regressed; any `&nbsp;` / `&lt;` showing literally → A1/A26/A28/A29 regressed.

3. **Match flow — full popup chain.**
   Click `here.gif` → popup opens. URL must end in `&from=olis1` (A32).
   In popup, type "Jones" → Search → click button "1274".
   - Expect: chart for Aleshia Jones opens; DB shows `OLISResults.demographicNo = 1274` for the row's uuid.
   - DB check:
     ```bash
     mysql -h db -uroot -ppassword oscar -e "SELECT uuid, demographicNo, status FROM OLISResults ORDER BY id DESC LIMIT 1;"
     ```
   - Bug to watch for: `demographicNo = NULL` → A32 fix didn't deploy, or `saveMatch` didn't fire (check browser Network tab for `AddToInbox.do?method=saveMatch` request).

4. **Re-query and confirm matched state visible.**
   Re-run the Z01 query.
   - Expect: same row now shows "Matched" (not red Unmatched); JANE Q DOE is a hyperlink to Aleshia's chart.
   - Demographics preservation check: the row should still display "JANE Q DOE / 9999999999" (the OLIS-side PID), **not** "Aleshia Jones".

5. **Add-to-Inbox of the matched row.**
   Tick the Add-to-Inbox checkbox → click Process. Confirm the bulk-process succeeds.
   - DB check: a new `hl7TextInfo` row exists; a `patientLabRouting` row links the lab to demographic 1274.
   - The previously-matched OLIS-side PID is preserved in `hl7_text_message` (base64) and still re-parses on view.

6. **Sign-off and Remove paths.**
   Re-run query, tick the Sign-off checkbox on a fresh-content fixture → process. Then tick Remove on another → process.
   - For Sign-off: `Hl7TextInfo` has `result_status='F'`/acknowledged; OscarLog has the audit row.
   - For Remove: `OLISResults.status='removed'`; OscarLog has the **manual** removal audit row (`Removing Type: User`, `Removing User: <provider>`, not "System") — C1 work.

## Section 2 — Z04 / Practitioner Query inbox flow

Z04 is harder to exercise in the simulator (no UI button to poll on-demand). Two acceptable paths:

**Option A — manually invoke a Z04 import path.** Easier if available; check if `OLISPollingUtil.pollZ04Query` has a manual-trigger admin page.

**Option B — verify the inbox match path directly with an existing HL7 lab** (this is what the Playwright verification did). Pick an existing HL7 lab, force it unmatched, exercise the inbox match flow.

```bash
mysql -h db -uroot -ppassword oscar -e "
  -- find a recent HL7 lab
  SELECT lab_no, demographic_no FROM patientLabRouting WHERE lab_type='HL7' ORDER BY lab_no DESC LIMIT 3;
  -- pick one (e.g., 176), force unmatched:
  UPDATE patientLabRouting SET demographic_no=0 WHERE lab_no=176 AND lab_type='HL7';
"
```

1. Open the unmatched lab: `/oscar/lab/CA/ALL/labDisplay.jsp?segmentID=176`.
   - Expect: lab loads, patient header shows the OLIS PID values (not "Unmatched" only).
2. Click the match icon → popup opens. URL should **not** have `from=olis1` (this is the inbox flow, not the OLIS preview).
3. Search Jones → click 1274 → chart opens.
4. DB check:
   ```bash
   mysql -h db -uroot -ppassword oscar -e "SELECT lab_no, demographic_no FROM patientLabRouting WHERE lab_no=176;"
   # expect: demographic_no=1274
   ```
5. Reopen lab 176 → confirm OLIS-side PID still renders (original name, original HIN), not Aleshia Jones.
6. **Restore:** `UPDATE patientLabRouting SET demographic_no=1 WHERE lab_no=176 AND lab_type='HL7';`

## Section 3 — Forward / Print / PDF surfaces

Verify the consult-print, lab-PDF, and forward-OLIS paths still work post-fixes.

1. **Forward an OLIS result** — open a matched OLIS lab, click Forward (A2 / A17 fix).
   - Expect: forwarding form loads with submit button visible and functional.
   - Bug to watch for: missing submit button → A2 regressed (this is your specific test case from earlier).
2. **Print an OLIS lab as PDF** — open a matched OLIS lab, click Print or the PDF link.
   - Expect: PDF renders with full PID (name, HIN, DOB, sex, address), all OBR/OBX content, NTE comments, doctor names in clean text (no `<span>`), license numbers in subscript styling (D3 work).
3. **Consult-print path** — generate a consult document that includes this OLIS lab attachment.
   - Expect: PDF generation succeeds, OLIS lab content renders correctly within the consult document. A21 / A1 surfaces.

## Section 4 — Non-OLIS popup-callback flows (post-A32 healing check)

This is the big new section. The A32 COOP fix is global; any popup-callback flow that was silently broken since 2026-03-15 should be healed. Spot-check the obvious ones:

1. **MDS lab match popup** — open a non-OLIS HL7 lab in `oscarMDS/` (any lab in the inbox that isn't OLIS), click the match icon. Confirm the popup flow lets you match a patient and the routing actually updates.
2. **Demographic search popup** — wherever the EMR uses `popupPage()` to open `/demographic/search.do`-style flows. Match patient, verify selection round-trips back to the opener.
3. **Document inbox match** — open the document inbox (`/oscar/dms/inboxManage.do` or similar), match a doc to a patient via popup, confirm the link persists.
4. **Other `popupPage()` callers** — there are several JSPs that define their own `popupPage` (search with `grep -rn "function popupPage" src/main/webapp/`). Test 2-3 of them at random and confirm any popup→opener callback works.

For each of these: the pattern is "click → popup → select → opener-side callback fires → DB reflects the change." If any of them silently no-op the same way OLIS was, that's a non-OLIS bug that was hidden since 2026-03-15 and is **not** healed by A32 — log it as its own ticket (likely needs a similar `from=olis1`-equivalent param fix in the relevant action).

## Section 5 — Quick audit & cleanup checks

1. **OLIS Audit Log viewer** — `/oscar/olis/auditLog.jsp` or similar. Confirm it loads and shows recent queries, matches, removals, blocked-content overrides (A8 / C1 / C2 / C3 work).
2. **OLIS Preferences** — `/oscar/provider/olis_preferences.jsp`. Confirm the "Unmatched Patient Results" dropdown (C3) renders with three states (system default / unclaimed / inbox) and saves correctly.
3. **OLIS Nomenclature Search** — `/oscar/olis/Search.jsp` test-code/test-request fields. Type 3+ chars in either field, confirm jQuery UI autocomplete returns ≤25 suggestions in <200 ms (D2b). Confirm the chip strip works (add/remove). Confirm submitted form carries the wire-code (LOINC for results, TR-prefixed for requests), not the Hibernate PK.
4. **OLIS Nomenclature Import (admin)** — `/oscar/admin/olis/NomenclatureImport.jsp` (or the link in admin.jsp). Try uploading an XLSX and confirm the Added/Updated/Deprecated report renders.

## Triage rubric

For anything that fails:

- **OLIS-specific bug** → new A-track entry in `docs/olis/readiness-plan.md` (next number after A32).
- **Non-OLIS popup-flow bug surfaced by A32 healing** → separate ticket outside the OLIS readiness plan; tag as "post-Jakarta-migration regression."
- **Already-known item** (matches an existing A/B/C/D entry) → reopen that entry, add reproduction context.

## Sign-off

When Sections 1-5 all pass, the OLIS branch is ready for F1 conformance review. Capture a brief write-up of any non-OLIS popup healing observed, since that's evidence the COOP change wasn't OLIS-only and supports the value of the work for downstream coordination.
