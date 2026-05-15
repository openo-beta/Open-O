CREATE TABLE OLISResultNomenclature (
  id INT NOT NULL AUTO_INCREMENT,
  nameId  VARCHAR(10),
  name TEXT,
  effectiveDate DATE NULL,
  endDate DATE NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  externalCodeVersion VARCHAR(8) NULL,
  successorCode VARCHAR(10) NULL,
  PRIMARY KEY(id),  
  INDEX idx_OLISResultNomenclature_status_endDate (status, endDate),
  INDEX idx_OLISResultNomenclature_nameId (nameId)
);

-- Data file generated from OLIS Nomenclatures V3.03_PROD, released April 23, 2026
-- Source: https://ehealthontario.on.ca/en/OLIS-nomenclature/download/olis-nomenclatures/prod/v3.03
-- When a new version is released, admins should run Admin → "OLIS — Import Nomenclature"
LOAD DATA LOCAL INFILE 'OLISTestResultNomenclature.csv'
INTO TABLE OLISResultNomenclature
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '\"' 
LINES TERMINATED BY '\n'
(nameId, name, status, effectiveDate, endDate, externalCodeVersion);

CREATE TABLE OLISRequestNomenclature (
  id INT NOT NULL AUTO_INCREMENT,
  nameId  VARCHAR(10),
  name TEXT,
  category VARCHAR(20),
  effectiveDate DATE NULL,
  endDate DATE NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  externalCodeVersion VARCHAR(8) NULL,
  successorCode VARCHAR(10) NULL,
  PRIMARY KEY(id),  
  INDEX idx_OLISRequestNomenclature_status_endDate (status, endDate),
  INDEX idx_OLISRequestNomenclature_nameId (nameId)
);

-- Data file generated from OLIS Nomenclatures V3.03_PROD, released April 23, 2026
-- Source: https://ehealthontario.on.ca/en/OLIS-nomenclature/download/olis-nomenclatures/prod/v3.03
LOAD DATA LOCAL INFILE 'OLISTestRequestNomenclature.csv'
INTO TABLE OLISRequestNomenclature
FIELDS TERMINATED BY '\t'
OPTIONALLY ENCLOSED BY '\"' 
LINES TERMINATED BY '\n'
(nameId, name, category, status, effectiveDate, endDate, externalCodeVersion);

CREATE TABLE OLISProviderPreferences (
  providerId  VARCHAR(10),
  startTime VARCHAR(20),
  lastRun datetime,
  filterPatients tinyint(1),
  PRIMARY KEY(providerId)
);

CREATE TABLE OLISSystemPreferences (
  id INT NOT NULL AUTO_INCREMENT,
  startTime VARCHAR(20),
  endTime VARCHAR(20),
  pollFrequency INT,
  lastRun timestamp,
  filterPatients tinyint(1),
  PRIMARY KEY(id)
);

update OLISSystemPreferences set filterPatients=0;

CREATE TABLE OLISResults (
  id int(11) auto_increment,
  requestingHICProviderNo varchar(30),
  providerNo varchar(30),
  queryType varchar(20),
  results text,
  hash varchar(255),
  status varchar(10),
  uuid varchar(255),
  query varchar(255),
  demographicNo integer,
  queryUuid varchar(255),
  PRIMARY KEY(id)
);

CREATE TABLE OLISQueryLog (
  id int(11) auto_increment,
  initiatingProviderNo varchar(30),
  queryType varchar(20),
  queryExecutionDate datetime,
  uuid varchar(255),
  requestingHIC varchar(30),
  demographicNo integer,
  olisTransactionId varchar(255),
  PRIMARY KEY(id)
);
