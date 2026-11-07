-- Footer for outgoing emails, kept apart from the body.
-- The body is what gets charted as a note in the patient's chart. The footer is sent to the
-- recipient only, so clinic boilerplate such as a booking link or an unsubscribe line stays out
-- of the chart. Rows written before this column read back as an empty footer.
ALTER TABLE emailLog ADD COLUMN footer BLOB DEFAULT NULL AFTER body;
