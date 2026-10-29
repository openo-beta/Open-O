-- Choosing prescription signatures: each provider keeps a list of signatures, and each
-- prescription keeps the signature it was first printed with.

-- The signatures in each provider's list. The default stays in providerExt.signature.
CREATE TABLE IF NOT EXISTS providerSignature (
  id int(11) NOT NULL AUTO_INCREMENT,
  provider_no varchar(6) NOT NULL,
  signature varchar(255) NOT NULL,
  lastUpdateUser varchar(6) NULL,
  lastUpdateDate datetime NOT NULL,
  PRIMARY KEY (id),
  KEY idx_providerSignature_provider (provider_no)
);

-- Existing signatures become each provider's first entry; blank ones count as none.
INSERT INTO providerSignature (provider_no, signature, lastUpdateUser, lastUpdateDate)
SELECT pe.provider_no, MAX(TRIM(pe.signature)), 'oscar', NOW()
FROM providerExt pe
WHERE pe.signature IS NOT NULL AND TRIM(pe.signature) <> ''
  AND NOT EXISTS (SELECT 1 FROM providerSignature ps WHERE ps.provider_no = pe.provider_no)
GROUP BY pe.provider_no;

-- The signature printed on the prescription, saved at its first print, fax or PDF.
ALTER TABLE prescription ADD COLUMN prescriber_signature varchar(255) NULL AFTER textView;

-- Existing prescriptions keep the signature saved with them: line 1 of textView, since 2004.
UPDATE prescription
SET prescriber_signature = LEFT(SUBSTRING_INDEX(textView, CHAR(10), 1), 255)
WHERE prescriber_signature IS NULL AND textView IS NOT NULL
  AND TRIM(SUBSTRING_INDEX(textView, CHAR(10), 1)) <> '';

-- A blank line 1 means the signature was blank when saved, which now prints the prescriber's name.
-- Prescriptions add "Dr. " to the first name unless it already has it (RxProviderData).
UPDATE prescription p
JOIN provider pr ON pr.provider_no = p.provider_no
SET p.prescriber_signature = LEFT(CONCAT(IF(LOCATE(BINARY 'Dr.', pr.first_name) > 0, '', 'Dr. '), pr.first_name, ' ', pr.last_name), 255)
WHERE p.prescriber_signature IS NULL AND p.textView IS NOT NULL AND p.textView <> '';

-- Rows with no text copy get the prescriber's signature as it is now, or their name.
UPDATE prescription p
JOIN provider pr ON pr.provider_no = p.provider_no
SET p.prescriber_signature = LEFT(COALESCE(
      (SELECT MAX(NULLIF(TRIM(pe.signature), '')) FROM providerExt pe WHERE pe.provider_no = p.provider_no),
      CONCAT(IF(LOCATE(BINARY 'Dr.', pr.first_name) > 0, '', 'Dr. '), pr.first_name, ' ', pr.last_name)), 255)
WHERE p.prescriber_signature IS NULL;
