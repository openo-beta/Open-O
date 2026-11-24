-- OLIS specimen-source nomenclature (CT 9.4 specimen-type display).
-- Maps an OLIS specimen source code (a test request's specimen source, OBR-15-1-1)
-- to a human-readable specimen type (e.g. 24H -> "Urine 24 Hour"). The display path
-- resolves value -> description, falling back to the lab-supplied text (OBR-15-1-2)
-- when the code is absent from the catalog. Refreshed at runtime via
-- Admin -> "OLIS - Import Nomenclature" (the importer now ingests the "Source" sheet
-- when present).
CREATE TABLE IF NOT EXISTS OLISSourceNomenclature (
    id INT NOT NULL AUTO_INCREMENT,
    value VARCHAR(64),
    description VARCHAR(512),
    PRIMARY KEY(id),
    INDEX idx_OLISSourceNomenclature_value (value)
);