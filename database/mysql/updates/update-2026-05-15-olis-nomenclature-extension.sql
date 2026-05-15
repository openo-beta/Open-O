ALTER TABLE OLISResultNomenclature
      ADD COLUMN effectiveDate DATE NULL,
      ADD COLUMN endDate DATE NULL,
      ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
      ADD COLUMN externalCodeVersion VARCHAR(8) NULL,
      ADD COLUMN successorCode VARCHAR(10) NULL,
      ADD INDEX idx_OLISResultNomenclature_status_endDate (status, endDate),
      ADD INDEX idx_OLISResultNomenclature_nameId (nameId);
  
  ALTER TABLE OLISRequestNomenclature
      ADD COLUMN effectiveDate DATE NULL,
      ADD COLUMN endDate DATE NULL,
      ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
      ADD COLUMN externalCodeVersion VARCHAR(8) NULL,
      ADD COLUMN successorCode VARCHAR(10) NULL,
      ADD INDEX idx_OLISRequestNomenclature_status_endDate (status, endDate),
      ADD INDEX idx_OLISRequestNomenclature_nameId (nameId);