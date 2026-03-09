-- Update Asthma Action Plan (AACP) dropdown options
-- Changes from generic Yes/No/NA to Provided/Revised/Reviewed per OMD DE16.098 conformance
-- Issue: https://github.com/openo-beta/Open-O/issues/2319

-- Create the Provided/Revised/Reviewed validation row if it doesn't already exist
INSERT INTO validations (name, regularExp)
SELECT 'Provided/Revised/Reviewed', 'Provided|Revised|Reviewed'
WHERE NOT EXISTS (
    SELECT 1 FROM validations WHERE name = 'Provided/Revised/Reviewed'
);

-- Point AACP to the Provided/Revised/Reviewed validation instead of the generic Yes/No/NA
UPDATE measurementType
SET validation = (SELECT id FROM validations WHERE name = 'Provided/Revised/Reviewed' LIMIT 1)
WHERE type = 'AACP';
