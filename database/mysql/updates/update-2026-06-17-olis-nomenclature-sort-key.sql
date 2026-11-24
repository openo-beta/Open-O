-- OLIS nomenclature sort key (CV04/05/06/15 display-sequence ordering).
-- Adds the OLIS catalog "Sort Key" column to both nomenclature tables. It is the
-- fallback ordering key used by the OLIS result/request sort comparators when a
-- result/request carries no in-message sort key (ZBX.2 / ZBR.11).
-- Nullable: existing rows (seeded before this column) keep NULL until the next
-- Admin -> "OLIS - Import Nomenclature" run, or a re-seed from the updated CSVs.

ALTER TABLE OLISResultNomenclature
      ADD COLUMN sortKey VARCHAR(32) NULL;

ALTER TABLE OLISRequestNomenclature
      ADD COLUMN sortKey VARCHAR(32) NULL;
