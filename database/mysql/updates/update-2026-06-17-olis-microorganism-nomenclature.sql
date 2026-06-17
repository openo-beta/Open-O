-- OLIS microorganism nomenclature (CV06 coded-organism display).
-- Maps an OLIS microorganism code (microbiology result sent as a coded entry:
-- OBX value type CE, coding system HL79905, code in OBX-5.1) to a human-readable
-- organism name. The display path resolves microorganismCode -> alternateName1.
-- Refreshed at runtime via Admin -> "OLIS - Import Nomenclature" (the importer now
-- ingests the "OLIS List of Microorganisms" sheet when present).

CREATE TABLE IF NOT EXISTS OLISMicroorganismNomenclature (
  id INT NOT NULL AUTO_INCREMENT,
  microorganismCode VARCHAR(32),
  microorganismType VARCHAR(64),
  taxonomicLevel VARCHAR(64),
  microorganismName VARCHAR(512),
  alternateName1 VARCHAR(512),
  alternateName2 VARCHAR(512),
  shortName VARCHAR(255),
  source VARCHAR(128),
  externalLink VARCHAR(512),
  reportable VARCHAR(16),
  reportableContext VARCHAR(128),
  effectiveStartDate VARCHAR(20),
  effectiveEndDate VARCHAR(20),
  changeNote VARCHAR(512),
  comments VARCHAR(512),
  PRIMARY KEY(id),
  INDEX idx_OLISMicroorganismNomenclature_code (microorganismCode)
);