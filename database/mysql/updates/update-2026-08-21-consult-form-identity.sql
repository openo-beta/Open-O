ALTER TABLE consultdocs
    ADD COLUMN form_table varchar(50) DEFAULT NULL;

UPDATE consultdocs
SET form_table = 'formAnnual'
WHERE doctype = 'F'
  AND form_table IS NULL;
