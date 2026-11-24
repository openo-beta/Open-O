-- OLIS C2: OLIS Transaction ID logging (OLIS03.06, OLIS06.02)
-- Adds the OLIS-assigned Transaction ID (the request's MSH-10, echoed back in MSA-2 of the response) to the OLIS query audit log.
ALTER TABLE OLISQueryLog ADD COLUMN olisTransactionId varchar(255) DEFAULT NULL;