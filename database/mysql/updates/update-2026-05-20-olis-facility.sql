CREATE TABLE OLISFacility (
  id INT NOT NULL AUTO_INCREMENT,
  licenceNumber VARCHAR(10) NOT NULL,
  facilityClass VARCHAR(8) NOT NULL,
  name VARCHAR(255) NOT NULL,
  addressLine1 VARCHAR(80) NULL,
  addressLine2 VARCHAR(40) NULL,
  city VARCHAR(40) NULL,
  postalCode VARCHAR(10) NULL,
  oid VARCHAR(40) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY(id),
  UNIQUE KEY uk_OLISFacility_class_licence (facilityClass, licenceNumber),
  INDEX idx_OLISFacility_class_status_name (facilityClass, status, name)
);
