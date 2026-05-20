# OLIS Validation — Changes Overview

A coworker-facing summary of OLIS-related UI and functionality changes shipped from the `olis-validation` branch. Each section explains **what changed**, **why**, and **what's user-visible** at a level you can share without needing the detailed readiness-plan context.

For internal technical depth (commit refs, file paths, test traces, follow-ups), see `docs/olis/readiness-plan.md`.

---

## 1. OLIS lab "Forward" UI replaced with the modern modal-dialog flow

### What changed (user-visible)

Before, clicking **Forward** on an OLIS lab opened a small popup window that had **no submit button** — you could pick a provider in the popup but there was no way to actually send. The Forward action was effectively a dead end on OLIS labs.

After this change, clicking **Forward** on an OLIS lab opens the **same modern modal dialog** that the non-OLIS HL7 lab display has used since 2021:

- In-page modal (jQuery UI dialog) instead of a separate browser popup window
- Provider autocomplete (start typing a name)
- Forward List, Favorites, and working **Forward** / **Cancel** buttons
- The dialog matches the look and feel of the rest of the EMR's modern dialogs

The **Print** button on the OLIS lab also got fixed in the same pass — it had been silently downloading an HTML error page disguised as a PDF; now it downloads a real PDF.

### Why

The original popup-window flow was disabled by a March 2021 commit (`ecfcffd9e6`, "fix-buld-inbox-forwarding") that commented out the popup's submit button and rewrote the backend to require JSON instead of form-encoded data. A new modal-dialog flow was added to the non-OLIS lab display at the same time — but the OLIS-specific `labDisplayOLIS.jsp` was left on the old broken path. Three failure modes stacked: missing button, mismatched payload format, missing modern dialog wiring.

The fix migrates `labDisplayOLIS.jsp` onto the same modern flow the non-OLIS path has been using for years. Functionally identical to the non-OLIS Forward flow.

### Note on sibling lab pages

The same March 2021 commit also broke the Forward button on **BC PathNet**, **Ontario CML**, **MDS multi-lab views**, and a few other related surfaces. A sweep fix for those was attempted (commit `579968aa08`) and reverted (`b93db70a35`) per maintainer feedback — these paths have been silently broken for ~5 years without user reports, and clinicians have likely adapted to routing via the inbox bulk-forward / canonical `labDisplay.jsp` / eChart-based workflows. Conservative call: don't touch unreported broken areas. Reactivate if any user reports them. **Only the OLIS-specific case (A2) was fixed in this branch.**

### Technical notes (for reviewers)

- Replaced jQuery 1.3.2 → jQuery 1.12.0 + jQuery-UI 1.12.1 + matching CSS theme in `labDisplayOLIS.jsp`
- Loaded `oscarMDSIndex.js`, added `ctx` global so its `ForwardSelectedRows()` resolves OLIS context
- Both Forward buttons now call `ForwardSelectedRows('<segmentID>:HL7', '', '')` against the existing modern endpoint
- Deleted the dead local `window.ForwardSelectedRows` shadow + the unused `<form name="reassignForm">` declaration
- Bonus: `printPDF()` was setting form action to relative `"PrintOLISLab.do"` which resolved via `<base href="/oscar/">` to `/oscar/PrintOLISLab.do` → 404. Now uses absolute `<%=request.getContextPath()%>/lab/CA/ALL/PrintOLISLab.do`.

### Files touched

- `src/main/webapp/lab/CA/ALL/labDisplayOLIS.jsp`

---

## 2. OLIS Search page now uses type-ahead search instead of a 51,000-option dropdown

### What changed (user-visible)

Before, when you opened **OLIS → Search**, the page loaded for several seconds because it rendered the entire OLIS nomenclature into two `<select multiple>` fields:

- **Test Result Code** — 48,200 options
- **Test Request Code** — 3,000 options

Selecting codes meant scrolling through ~51,000 entries while ctrl-clicking. In practice nobody used these fields — they were too slow to load and too unwieldy to navigate.

After this change, both fields are now **type-ahead search inputs**:

1. Click the field, start typing (2+ characters), wait <120ms.
2. Top 25 matching nomenclature codes appear as suggestions.
3. Click a suggestion to add it as a chip below the field.
4. Each chip has an `×` to remove it.
5. Add as many chips as the search supports (still capped at 200 result codes / 100 request codes by OLIS).

The rest of the OLIS Search page is unchanged — the patient field, date pickers, lab dropdowns, and Z01/Z02/Z04/etc. tabs all behave as before.

### Why

Two reasons:

1. **Performance.** Loading 51,000 options into the DOM on every page open was the cause of the long-observed "OLIS Search is slow" complaint. The query itself only accepts a small number of codes, so eagerly rendering the entire catalog was the wrong UI pattern for a list this size.
2. **Usability.** Even after the load completed, finding "Glucose" in 49,000 alphabetically-sorted options by scrolling was impractical. Type-ahead search lets you find any code in seconds.

### Technical notes (for reviewers)

- New AJAX endpoint `/olis/NomenclatureSearch.do` (`OLISNomenclatureSearch2Action`) returns top-25 matches as JSON. Backed by new `findByNameLike(term, limit)` methods on both nomenclature DAOs. Security gate: `_lab` read privilege, same as the rest of OLIS Search.
- Form submission shape is **unchanged**: each chip carries a hidden `<input name="testResultCode">` (or `testRequestCode`). `OLISSearch2Action.getParameterValues(...)` reads the same names it always did. No server-side query logic changed.
- The autocomplete library is **jQuery UI 1.12.1** (already used elsewhere in OpenO, e.g. `appointment/addappointment.jsp`). The patient field next to ours still uses YUI (pre-existing scope), so the page now carries both — intentional, the libraries don't conflict.
- Measured page render: **~94 ms** / **71 KB** / **208 options** (down from ~51,200).

### Follow-up bundled in (same area, found during D2b smoke-test)

**OBX-3 / OBR-4 wire-code bug fix.** The legacy dropdown passed the local Hibernate primary key (e.g. `2977`) as the OLIS wire-format code, instead of the actual LOINC code (e.g. `TR13070-8`). OLIS would have rejected those messages. This was effectively dormant because the legacy UI was so unusable that no real searches got submitted with bad codes — but it would have failed F1 conformance testing. Fixed by changing the AJAX endpoint to return the real LOINC / OLIS code; the form-submission path is otherwise unchanged.

Spec match: result codes now sent as `14683-7^...^HL79902` (LOINC) and request codes as `TR10481-0^...^HL79901` (OLIS), matching OLIS Interface Specification §10.2.5.14 / §10.2.5.15 examples.

### Files touched

- `src/main/java/ca/openosp/openo/olis/OLISNomenclatureSearch2Action.java` *(new)*
- `src/main/java/ca/openosp/openo/olis/dao/OLISResultNomenclatureDao.java`
- `src/main/java/ca/openosp/openo/olis/dao/OLISRequestNomenclatureDao.java`
- `src/main/webapp/WEB-INF/classes/struts.xml`
- `src/main/webapp/olis/Search.jsp`

---

## 3. OLIS Search lab/SCC dropdowns: full roster + type-ahead picker

### What changed (user-visible)

Before, every "pick a lab" field on OLIS Search and OLIS Preferences was a `<select>` with the same hard-coded 3 entries — **Gamma-Dynacare**, **CML**, **LifeLabs** — backed by a 3-line Java enum. That's 8 dropdowns on **OLIS → Search** (Specimen Collector, Performing Laboratory, Exclude Performing Laboratory, Reporting Laboratory, Exclude Reporting Laboratory, Test Request Placer, Destination Laboratory, Ordering Facility) plus 2 on **provider → OLIS Preferences** (Default Reporting Lab, Default Exclude Reporting Lab).

After this change, every one of those 10 fields is now a **type-ahead picker** backed by the full Ontario roster:

1. Click the field, start typing (2+ characters of any lab or specimen-collection-centre name).
2. Suggestions show up as `LifeLabs — Toronto [3001]` — name, city, and licence number.
3. Click one to add it as a chip; chip × clears the selection.
4. Pre-existing user preferences (saved Default Reporting Lab) load as a pre-selected chip on page open.

Picker scope per field:

| Field | Roster scope |
|---|---|
| Specimen Collector | Specimen Collection Centres only |
| Performing / Exclude Performing / Reporting / Exclude Reporting / Test Request Placer / Destination | Laboratories only |
| Ordering Facility | Labs + SCCs |
| OLIS Preferences — Default Reporting / Exclude Reporting | Laboratories only |

The full roster contains **273 Laboratories** (OID `2.16.840.1.113883.3.59.1`) + **994 Specimen Collection Centres** (OID `2.16.840.1.113883.3.59.2`) — 1,267 rows total. Same problem-shape as the nomenclature dropdown that D2b solved, just at smaller scale.

### Why

The 3-entry hard-coded list was a maintainability time-bomb in two directions:

1. **Coverage.** Ontario has 273 licensed laboratories and ~1,000 collection centres. A list of 3 is not a "list of participating laboratories" in any meaningful conformance reading — clinics who order from any of the other ~270 labs were silently locked out of filtering their queries.
2. **Correctness.** While replacing the enum, we discovered the existing entry **`CML("5407", "CML")` was wrong**. Licence `5407` in the official eHealth Ontario Lab/SCC Extract is **LifeLabs Mississauga**, not CML — and the real CML HealthCare Inc. is licence `3855`, classified as a Specimen Collection Centre, not a Laboratory. Anyone who happened to pick "CML" in the dropdown was actually filtering against LifeLabs Mississauga. The Lab/SCC importer self-resolves this — the bad row drops away when the enum is deleted and the roster comes from the canonical source.

### Same pattern as D2a (nomenclature)

This is the second time we've applied the "admin XLSX importer + DB-backed typeahead" pattern in this branch:

| | D2a (nomenclature) | This change (lab/SCC) |
|---|---|---|
| Source distribution | OLIS Nomenclatures XLSX | Lab/SCC Extract XLSX |
| Row count | ~52,800 (49K result + 3.5K request) | 1,267 (273 Lab + 994 SCC) |
| Cadence | Quarterly releases | On-demand refresh |
| Replaced UI | 51K-option `<select multiple>` | 3-option `<select>` enum |

The two importers share architecture: stdlib `java.util.zip` + SAX for XLSX parsing (no Apache POI), upsert on a natural key, deprecate-on-absence, `_admin/w` gate.

### New admin page

A new admin link at **Admin → OLIS — Import Lab/SCC Roster** that accepts the official Lab/SCC Extract XLSX downloaded from <https://ehealthontario.on.ca/en/support/lab-results/olis-whats-new/olis-client-support>. After upload, the page shows a report:

| | Added | Updated | Total touched |
|---|---|---|---|
| Laboratories | … | … | … |
| Specimen Collection Centres | … | … | … |

Rows present before the import but absent from the new file are marked INACTIVE — so they stop appearing in the typeahead pickers, but their history is preserved in the DB.

### Choices to flag

- **Single table with a `facilityClass` column** ('LAB' / 'SCC') rather than two tables with identical schemas. The source extract is one sheet with one row shape — only the OID column distinguishes Lab from SCC. Mirroring that in the DB avoids physical duplication; if the two classes ever need diverging columns, a JPA `@Inheritance` swap can be added without a data migration.
- **Picker semantics per field** (table above) — based on which OLIS query parameter the field maps to (e.g. `@ZBR.3` Specimen Collector is by spec an SCC; `@ZBR.4` / `@ZBR.6` Reporting/Performing are Labs). Worth confirming during conformance review that the field-class mapping matches OntarioMD's expectations. The pickers don't *enforce* class — the back-end just passes the licence number through to OLIS, which accepts whichever class is appropriate per field. If a clinic wants Ordering Facility to default to Labs-only, that's a one-line change.
- **Match on name + city, search on substring** — typing "lifelabs toronto" doesn't currently narrow to "LifeLabs in Toronto" because the search is name-only, with city shown as a secondary disambiguator in the suggestion label. Considered combining into a composite search column but kept simple for v1.

### What needs to happen during deploy

1. **Run the schema migration** in `database/mysql/updates/update-2026-05-20-olis-facility.sql` before redeploying the WAR.
2. After deploy, an admin should go to **Admin → OLIS — Import Lab/SCC Roster**, download the current Lab/SCC Extract XLSX from <https://ehealthontario.on.ca/en/support/lab-results/olis-whats-new/olis-client-support>, and upload it. Until this runs, the typeahead pickers will return no results (the table is empty on fresh deploy).
3. Existing user preferences that hold the now-deleted `CML` licence `5407` will continue to resolve — `5407` is still a valid Ontario licence; it just no longer maps to CML. They'll display as "LifeLabs — Mississauga [5407]" after the import.

### Closes OLIS04.03

This satisfies **OLIS04.03 — Provide List of Participating Laboratories** by replacing the 3-entry hardcoded list with the full Ontario roster, refreshable on demand from the canonical source. See `docs/olis/requirements-analysis.md` OLIS04.03 for the verbatim spec match and the verification trail.

### Files touched

- `database/mysql/updates/update-2026-05-20-olis-facility.sql` *(new — creates the `OLISFacility` table + indexes)*
- `database/mysql/olis/olisinit.sql` *(adds the `CREATE TABLE OLISFacility` for fresh installs — append the same DDL)*
- `src/main/java/ca/openosp/openo/olis/model/OLISFacility.java` *(new entity)*
- `src/main/java/ca/openosp/openo/olis/dao/OLISFacilityDao.java` *(new DAO — upsert lookup, name-prefix autocomplete, batch deprecate)*
- `src/main/java/ca/openosp/openo/olis/OLISFacilityImport2Action.java` *(new — XLSX upload + upsert + deprecate-on-absence)*
- `src/main/java/ca/openosp/openo/olis/OLISFacilitySearch2Action.java` *(new — AJAX endpoint for the pickers)*
- `src/main/java/ca/openosp/openo/olis/model/OLISParticipatingLab.java` *(deleted — replaced by the DB-backed entity)*
- `src/main/webapp/olis/FacilityImport.jsp` *(new — upload form + report view)*
- `src/main/webapp/olis/Search.jsp` *(8 dropdowns → typeahead pickers; reads preference defaults from the new DAO)*
- `src/main/webapp/provider/olis_preferences.jsp` *(2 dropdowns → typeahead pickers)*
- `src/main/webapp/WEB-INF/classes/struts.xml` *(adds `olis/FacilitySearch` + `olis/FacilityImport` routes)*
- `src/main/webapp/admin/admin.jsp` *(adds Lab/SCC importer link in the legacy admin tab)*
- `src/main/webapp/administration/leftNav.jspf` *(same in the modern admin left-nav)*

---

## 4. Admin can now refresh the OLIS nomenclature from the official XLSX

### What changed (user-visible)

A new admin page at **Admin → OLIS — Import Nomenclature** that accepts the official "OLIS Nomenclatures V{X.YY}_PROD.xlsx" file downloaded from eHealth Ontario and refreshes the two local OLIS lookup tables (`OLISResultNomenclature` + `OLISRequestNomenclature`).

After upload, the page shows a report:

| | Added | Updated | Deprecated | Total touched |
|---|---|---|---|---|
| Test Result codes | … | … | … | … |
| Test Request codes | … | … | … | … |

The page is gated on the `_admin` write privilege.

### Why

Two operational realities forced this:

1. **The current local OLIS nomenclature in OpenO is from March 2023** (file version V2.69). The live distribution is V3.03_PROD from April 2026 — meaning OpenO has been **>3 years behind**.
2. **Staying current is operationally pressing.** Each quarterly release deprecates, renames, or re-aligns codes; clinics running stale nomenclature risk message rejection or display mismatch when they encounter codes that no longer match the live catalog. Hand-transcribing CSVs (how the current 2023 baseline got into OpenO) doesn't scale to a quarterly cadence — which is why OpenO has missed 12 consecutive releases.

Each release brings ~300 row deltas across adds/changes/deprecations. The XLSX importer does in minutes what manual CSV reseed has been failing to do for 3 years.

### Detail on the change semantics

The official distribution uses three change types per row:

- **Add** — new code, valid from its Effective Date forward.
- **Change** — attributes updated (e.g. LOINC realignment, preferred-name change).
- **Deprecate** — code retired at its End Date; in some cases a successor code is noted.

The local schema previously held only `id`, `name`, `nameId` per code — no way to distinguish active from deprecated, no way to enforce validity windows. Five new columns were added to both nomenclature tables:

- `effectiveDate` (DATE) — earliest date a code is valid
- `endDate` (DATE) — date past which the code is retired (NULL = no end)
- `status` (VARCHAR, `ACTIVE` / `INACTIVE`) — derived from the distribution's `Workflow Status Indicator` + `Validation Status Indicator`
- `externalCodeVersion` (VARCHAR) — audit field, captures which OLIS release a row was last refreshed against
- `successorCode` (VARCHAR, nullable) — placeholder for replacement-code linkage when codes are renumbered (populated in a future change once we wire it through the change-log sheet)

The autocomplete on OLIS Search (D2b) now filters to `status = 'ACTIVE' AND (endDate IS NULL OR endDate >= CURRENT_DATE)`, so users no longer see deprecated codes as selectable suggestions.

### Verified end-to-end

Imported `OLIS Nomenclatures V3.03_PROD.xlsx` (April 23, 2026 release) against a fresh dev DB. First-run counts:

- **49,325 result codes** processed → 7,116 added, 41,426 updated, 783 newly deprecated
- **3,463 request codes** processed → 421 added, 2,996 updated, 46 newly deprecated

Second-run idempotency check: all rows treated as updates, zero new deprecations. Date parsing verified — sample row `100746-7 Glucose` correctly imported with `effectiveDate = 2023-03-09`.

### What needs to happen during deploy

1. **Run the schema migration** in `database/mysql/updates/update-2026-05-15-olis-nomenclature-extension.sql` before redeploying the WAR.
2. After deploy, an admin should immediately go to **Admin → OLIS — Import Nomenclature**, download the current V3.03_PROD (or newer) XLSX from <https://ehealthontario.on.ca/en/OLIS-nomenclature/>, and upload it. Without this step, the deprecated-code filter has nothing to filter against — the old 2023 baseline rows all stay marked ACTIVE.

### Fresh-install baseline refreshed too

The two seed CSVs that `database/mysql/olis/olisinit.sql` bulk-loads were also regenerated from V3.03_PROD (April 2026) — they had been frozen at V2.69 (March 2023). Fresh installs now land on Apr 2026 codes instead of Mar 2023 codes, ~7,100 newer codes available immediately.

The CSVs now carry the **full set of fields** the autocomplete filter needs: `(nameId, name, status, effectiveDate, endDate, externalCodeVersion)` for results and `(nameId, name, category, status, effectiveDate, endDate, externalCodeVersion)` for requests. Status is derived from the XLSX's *Workflow Status Indicator* + *Validation Status Indicator* fields (matching the Java importer's logic exactly), and dates are emitted as `YYYY-MM-DD` with `\N` for nulls.

**Why the extra columns vs. just the 2-3 minimum:** without `status` and `endDate` populated at fresh-install time, every code defaults to `ACTIVE` with no end date. That means the autocomplete shows deprecated codes as selectable — and OLIS will reject queries containing them, silently. The only thing protecting a fresh install from this state is the admin remembering to run the XLSX importer on day 1, which is the kind of "forever-orphan" state that small clinic deploys often end up in. Carrying the full column set in the CSV makes a fresh install conformant out of the box.

**Logic-duplication note:** the Python regen script that converts the XLSX → CSV mirrors the Java importer's `deriveStatus` and `parseDateCell` rules byte-for-byte (same precedence, same date format list — Excel serial, `yyyy-MM-dd`, `M/d/yyyy`, all non-lenient). If the Java logic ever changes, the regen script needs to follow. Both implementations derive from the same XLSX schema and are validated against each other (the importer's "added/updated/deprecated" counts match the CSV-derived status counts exactly on a fresh load).

**Regenerating the CSVs for a future release:** the standalone script lives at `database/mysql/olis/regenerate-csvs-from-xlsx.py` (stdlib-only, ~250 lines). Run it whenever a new OLIS Nomenclatures distribution is released to refresh the seed CSVs:

```bash
cd database/mysql/olis
python3 regenerate-csvs-from-xlsx.py /path/to/OLIS_Nomenclatures_V{X.YY}_PROD.xlsx
```

The script writes the two CSVs into the current directory, prints row counts + status breakdowns, and emits the verification SQL you can paste into the DB to confirm the LOAD DATA INFILE works on a temp clone of the schema. Commit the resulting CSV diffs.

### Files touched

- `database/mysql/updates/update-2026-05-15-olis-nomenclature-extension.sql` *(new — adds 5 columns + indexes to both nomenclature tables)*
- `database/mysql/olis/olisinit.sql` *(adds the 5 new columns + indexes to the CREATE TABLE for fresh installs; updates the data-file comments to reference V3.03_PROD / April 2026)*
- `database/mysql/olis/OLISTestResultNomenclature.csv` *(regenerated from V3.03_PROD — 49,325 rows, up from 48,209)*
- `database/mysql/olis/OLISTestRequestNomenclature.csv` *(regenerated from V3.03_PROD — 3,463 rows, up from 3,041)*
- `src/main/java/ca/openosp/openo/olis/OLISNomenclatureImport2Action.java` *(new — Struts2 action, stdlib XLSX parser, upsert logic, `_admin` write gate)*
- `src/main/java/ca/openosp/openo/olis/model/OLISResultNomenclature.java` *(adds 5 fields)*
- `src/main/java/ca/openosp/openo/olis/model/OLISRequestNomenclature.java` *(adds 5 fields)*
- `src/main/java/ca/openosp/openo/olis/dao/OLISResultNomenclatureDao.java` *(autocomplete filter to ACTIVE + valid date window)*
- `src/main/java/ca/openosp/openo/olis/dao/OLISRequestNomenclatureDao.java` *(same)*
- `src/main/webapp/olis/NomenclatureImport.jsp` *(new — upload form + report view)*
- `src/main/webapp/WEB-INF/classes/struts.xml` *(new route)*
- `src/main/webapp/admin/admin.jsp` *(adds link in the OLIS section under Integrations — legacy admin tab)*
- `src/main/webapp/administration/leftNav.jspf` *(adds link in the OLIS section — modern admin left-nav; also loosens the visibility gate to include `olis_simulate=yes`, so dev environments without a real OLIS keystore can still reach the admin links — see note below)*

### Visibility gate note

Both admin surfaces gate the OLIS admin links (Preferences + Import Nomenclature) on whether OLIS is configured. The original gate was `olis_keystore.length() > 0` only — meaning dev environments running with `olis_simulate=yes` (a dev-only flag for the OLIS simulator) would not see the admin links. We loosened the gate to `olis_keystore.length() > 0 || olis_simulate=yes` so dev/test environments can reach these screens without needing a real keystore. Production behavior unchanged for clinics that have a real keystore configured. Anyone with `olis_simulate=yes` is by definition on a dev environment.

---

## 5. — OLIS Search Results preview table: new columns + new filter

### What changed (user-visible)

The **OLIS Search Results** preview table (the page you land on after running a Z01/Z02/etc. query) now shows four new columns and one new filter:

**New columns:**

| Column | What it shows | Why it's there |
|---|---|---|
| **Reporting Lab** | The lab that issued the report (e.g., LifeLabs, Gamma-Dynacare) | Was previously only available as an internal row attribute used by the filter dropdown — now visible to the user |
| **Performing Lab** | The lab that ran the test | Same as above; OLIS treats Reporting and Performing as distinct so both are shown |
| **Match** | Explicit "Matched" / "Unmatched" text | Match status was previously implied only by the `here.gif` icon next to the name — now spelled out |
| **Blocked** | Per-row indicator when the result has `ZPD-3=Y` (consent-blocked content) | OLIS04.10 requires per-row blocked indication, not just a page-level override prompt |

**New filter:**

- **Practitioner filter** — new dropdown that lets you narrow the result table to results involving a specific practitioner (ordering, copied-to, attending, or admitting). Multi-value substring match, mirroring the existing Category filter pattern.

The page went from 16 → 20 columns; existing filters and behavior are unchanged.

### Why

Closes four OntarioMD requirements (OLIS01.02, OLIS01.03, OLIS03.02, OLIS03.03, OLIS04.10) that called for these specific surface elements in the preview. The data was already in the system (Reporting/Performing Lab were attached as row attributes; match status was derivable from the demId; blocked was readable from the per-result handler; practitioner list was already aggregated). What was missing was the user-visible exposure — which is what these columns/filter add.

### Choices to flag

Several deliberate UX choices in this work — happy to revisit if any feel wrong:

- **Two lab columns, not one.** OLIS spec treats Reporting and Performing labs as distinct query parameters. We chose to surface both even though many results have the same value in each. Alternative: collapse into one column when equal.
- **"Matched" / "Unmatched" as text, not just the icon.** Previously the `here.gif` icon next to an unmatched name was the only signal. We added explicit text because the icon was easy to miss in a busy table. The icon stays — text + icon together.
- **Blocked column at row level, page-level override prompt unchanged.** OLIS04.10 specifically wants per-row indication; the existing modal that asks for an override password on first encounter still appears.
- **Practitioner filter is substring-match, multi-value.** A result can have ordering + copied-to + attending + admitting practitioners on the same row. We index all four into a `practitioner="a|b|c"` row attribute and the filter does substring matches across that — same pattern as the existing Category filter. Alternative: single-select per practitioner role.

### Files touched

- `src/main/webapp/olis/Results.jsp`

---

## 6. — Per-provider preference for routing unmatched OLIS results

### What changed (user-visible)

A new dropdown on each provider's **OLIS Preferences** page (`/oscar/provider/olis_preferences.jsp`):

> **Unmatched Patient Results:** [ Use system default | Filter to unclaimed worklist | Send to my inbox ]

When an OLIS lab arrives that can't be auto-matched to an EMR patient, where it lands now depends on the **polling provider's** preference (the provider whose Z04 query pulled it):

- **Use system default** *(default for existing providers — no behavior change)* — falls back to the existing `OLISSystemPreferences.isFilterPatients()` system-wide setting
- **Filter to unclaimed worklist** — unmatched results route to the unclaimed/zero-demographic bucket regardless of system default
- **Send to my inbox** — unmatched results route to *this provider's* inbox regardless of system default

The system-wide setting still exists and still governs the fallback case. Existing providers default to "Use system default" — no migration needed at the user level.

### Why

Closes **OLIS02.03**, which requires per-provider control over unmatched-result routing. Some clinics want every unmatched lab in one shared worklist (so a triage nurse can review all of them); others want each provider to handle their own incoming unmatched labs without depending on a shared queue. The system-wide-only preference forced everyone into one model.

### Choice to flag

We chose **"polling provider" semantics** over "ordering practitioner" semantics: an unmatched OLIS result is governed by the preference of the provider whose Z04 poll fetched it (which is semantically the Requesting HIC). We considered using the practitioners listed in OBR-16 / OBR-28 (ordering / copied-to), but those aren't the Requesting HIC, can be multiple per result, and would force a "split routing" model where one result scatters across the unclaimed bucket and multiple provider inboxes simultaneously. The polling-provider model is cleaner — one provider, one preference, one destination.

Worth confirming this matches how providers actually want it to work in practice — particularly in multi-provider clinics where one provider's Z04 might pull labs ordered by other providers.

### Technical notes (for reviewers)

- New nullable `Boolean filterPatients` field on `OLISProviderPreferences` (3-state — `null` inherits system default)
- Polling `providerNo` now threaded through: `OLISPollingUtil.pollZ04Query` → `parseAndImportResponse` → OLIS upload-handler `parse()` → new `MessageUploader.routeReport` overload → core method. The `MessageHandler.parse` interface (28 implementers) was not touched; threading goes through the *concrete* OLIS upload handler. Non-OLIS / non-polling callers pass `null` and keep today's system-level behavior.
- Z06 facility polls pass `null` (no single-provider context) — they fall back to the system default, as before.
- `OLISPoller` is unrelated dead code (live path is `OLISSchedulerJob` → `OLISPollingUtil`); intentionally left untouched.

### What needs to happen during deploy

The new `filterPatients` column needs a small DB migration applied to existing schemas before deployment. Fresh installs are handled in `olisinit.sql`.

### Files touched

- `src/main/java/ca/openosp/openo/olis/model/OLISProviderPreferences.java`
- `src/main/java/ca/openosp/openo/olis/OLISPollingUtil.java`
- `src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java` *(upload handler — `parse()` overload)*
- `src/main/java/ca/openosp/openo/lab/ca/all/upload/MessageUploader.java`
- `src/main/java/ca/openosp/openo/olis/OlisPreferences2Action.java`
- `src/main/webapp/provider/olis_preferences.jsp`
- New migration SQL + `database/mysql/olis/olisinit.sql` update

---

## 7. Display-fidelity cluster — markup leaks in OLIS lab display cleaned up

### What changed (user-visible)

Several places in the OLIS lab display used to render literal HTML/entity text where decoded clinical content should appear. All cleaned up:

| Where | What used to leak | What it shows now |
|---|---|---|
| **InboxHub "Requesting Client" column** | OpenO-synthesized `<span>` markup appearing as literal text inside the column | Plain provider name |
| **OLIS lab display — Ordering Provider / CC'd Doctors / Attending / Admitting fields** | Literal `<span class="md-license">…</span>` text wrapping the license number | Plain text (web view); subscript-styled license number preserved in the PDF render |
| **OLIS lab display — Home Address** | Literal `<br />` text appearing inside the address block | Multi-line address rendered as line breaks |
| **OLIS lab display — OBX test names** | Literal `<u>…</u>` markup showing up next to result names | Plain test name (underline styling, if any, comes from CSS — not from data-embedded markup) |
| **OLIS lab display — Report Comments** | Literal `&nbsp;` / `<span>` text inside comment blocks | Decoded clinical comment text |

### Why

These were all the same family of bug: data that arrives from OLIS (or that OpenO synthesizes from OLIS data) was being rendered into the JSP without first decoding HTML entities or stripping markup that exists for *internal* layout purposes. From the user's perspective, the lab display looked unprofessional and occasionally illegible. From a downstream-consumer perspective (consult PDFs, forwarded labs, printed reports), the same junk text was making it into output documents.

### Choice to flag — doctor name styling

The license-number rendering change is the only one with a real aesthetic decision: previously the OpenO-internal code synthesized `<span class="md-license">123456</span>` after the provider name (intended to render as subscript on web + PDF), and that markup was leaking as literal text in the JSP. The fix introduced a structured `DoctorName` parser that exposes name and license as separate fields, with each rendering surface choosing how to display them:

- **PDF (`OLISLabPDFCreator`):** name in main font + license in subscript font (same visual intent as before, now done properly via structured fields).
- **JSP (`labDisplayOLIS.jsp`, `labDisplay.jsp`, `Results.jsp`):** plain text — no subscript styling currently applied on the web.

If coworkers want the web view to also subscript-style the license number (matching the PDF), it's a small CSS-only follow-up: emit `<span class="md-license">` from the structured data + add the corresponding CSS rule. Easy to add later; flag this if you want it.

### Note on the rest of the system

Two strip sites were intentionally left in place as **defensive no-ops** on the DB-fed paths (`Hl7textResultsData.java:598` and `:847`, reading `hl7TextInfo.requestingProvider`). The reason: rows written before this fix have the old `<span>` markup baked into the DB column, and until a DB backfill or natural re-save runs, those strips guard the display. Inline comment in the code documents the asymmetry.

### Files touched

- `src/main/java/ca/openosp/openo/lab/ca/all/parsers/OLISHL7Handler.java` *(structured `DoctorName` parser)*
- `src/main/java/ca/openosp/openo/lab/ca/all/pageUtil/OLISLabPDFCreator.java` *(uses structured fields for subscript styling)*
- `src/main/webapp/lab/CA/ALL/labDisplayOLIS.jsp`, `labDisplay.jsp` *(strip sites dropped — parser data is clean)*
- `src/main/webapp/olis/Results.jsp` *(InboxHub preview)*
- `src/main/java/ca/openosp/openo/lab/ca/on/Hl7textResultsData.java` *(parser-fresh strip dropped; DB-fed strips kept defensive)*

---
