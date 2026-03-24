-- OMA Uninsured Services Fees for Ontario Private Billing
-- OMD Conformance Requirement PC13.19
-- Adds 34 OMA fee codes to the billingservice table and maps them to the PRIVATE billing form
-- See: https://github.com/openo-beta/Open-O/issues/2381

-- Deactivate legacy placeholder PRIVATE form mappings (replaced by OMA codes below)
UPDATE ctl_billingservice SET status = 'I' WHERE servicetype = 'PRI' AND service_code = 'A007A' AND service_group_name IN (' Group 1 Name', ' Group 2 Name', ' Group 3 Name');

-- Billing service codes (private codes use underscore prefix, region=NULL)
INSERT INTO billingservice (service_compositecode,service_code,description,value,percentage,billingservice_date,specialty,region,anaesthesia,termination_date,displaystyle,sliFlag,gstFlag) VALUES
     ('','_OMA_A003','General Assessment','253.35','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_A007','Intermediate Assessment','110.10','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_A110','Periodic oculo-visual assessment (by a General/Family practitioner)','141.85','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_C01','Patient interview for practice admission','128.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F01','Form completion for physicals for schools, camps, pre-school, daycare, university/educational institutions','37.25','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F02','Form completion for physicals for pre-employment certification of fitness/fitness clubs or hospital/nursing home employee','49.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F03','Children''s Aid Society (CAS) application for prospective foster parent','252.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F04','CRA Disability Tax Credit Certificate (form T2201)','150.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F05','Insurance Certificate OCF- 3 Disability Certificate','262.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F06','Insurance Certificate OCF-18 Treatment Plan','278.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F07','Insurance Certificate OCF-23 Treatment Confirmation','262.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F08','Attending Physician''s Statement','200.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F09','Insurance Medical Examination (assessment and report)','200.00','0.00','2026-03-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F10','Medical Report for a CPP Disability Benefit (SCISP-2519)','85.00','0.00','2026-03-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F11','CPP Narrative Medical Report','150.00','0.00','2026-03-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F12','Medical certificate employment insurance sickness benefits INS5140','52.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F13','Travel cancellation insurance form','164.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F14','Life insurance death certificate','50.00','0.00','2026-03-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F15','Insurance Certificate OCF-19 Determination of Catastrophic Impairment','155.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F16','System-Specific or Disease Specific Questionnaire','125.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F17','System-Specific Examination','152.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F18','Assessments: Clarification Report','300.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F19','Assessments: Full Narrative Report','350.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F20','Assessments: Independent Medical Examination','200.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F21','Terminal Illness Medical Attestation for a Disability Benefit (ISP2530B)','85.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F22','Reassessment Medical Report (ISP2509)','25.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F23','Scannable Impairment Evaluation (IMPAIR)','50.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F24','Medical Report – Recurrence of the Same Medical Problem (ISP2525)','25.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_F25','Drivers medical examination (form only)','77.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_G010','Urinalysis – without microscopy','7.65','0.00','2026-03-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_N01','Sick notes (includes return to work/school notes)','26.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_N02','Fitness to work notes','50.00','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_RECOR','Electronic Transfer of Records','30.00','0.00','2026-03-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0),
     ('','_OMA_RX','Dispensing service fee','20.75','0.00','2026-01-01',NULL,NULL,NULL,'9999-12-31',NULL,0,0);

-- Map billing codes to the PRIVATE billing form with group organization
-- Group1 = Forms, Group2 = Assessments, Group3 = Procedures
INSERT INTO ctl_billingservice (servicetype_name,servicetype,service_code,service_group_name,service_group,status,service_order) VALUES
     ('PRIVATE','PRI','_OMA_A003','Assessments','Group2','A',1),
     ('PRIVATE','PRI','_OMA_A007','Assessments','Group2','A',2),
     ('PRIVATE','PRI','_OMA_A110','Assessments','Group2','A',3),
     ('PRIVATE','PRI','_OMA_F01','Forms','Group1','A',2),
     ('PRIVATE','PRI','_OMA_F02','Forms','Group1','A',3),
     ('PRIVATE','PRI','_OMA_F03','Forms','Group1','A',4),
     ('PRIVATE','PRI','_OMA_F04','Forms','Group1','A',5),
     ('PRIVATE','PRI','_OMA_F05','Forms','Group1','A',6),
     ('PRIVATE','PRI','_OMA_F06','Forms','Group1','A',7),
     ('PRIVATE','PRI','_OMA_F07','Forms','Group1','A',8),
     ('PRIVATE','PRI','_OMA_F08','Assessments','Group2','A',5),
     ('PRIVATE','PRI','_OMA_F09','Assessments','Group2','A',6),
     ('PRIVATE','PRI','_OMA_F10','Assessments','Group2','A',7),
     ('PRIVATE','PRI','_OMA_F11','Assessments','Group2','A',8),
     ('PRIVATE','PRI','_OMA_F12','Assessments','Group2','A',4),
     ('PRIVATE','PRI','_OMA_F13','Forms','Group1','A',11),
     ('PRIVATE','PRI','_OMA_F14','Forms','Group1','A',12),
     ('PRIVATE','PRI','_OMA_F15','Forms','Group1','A',13),
     ('PRIVATE','PRI','_OMA_F16','Assessments','Group2','A',10),
     ('PRIVATE','PRI','_OMA_F17','Assessments','Group2','A',11),
     ('PRIVATE','PRI','_OMA_F18','Assessments','Group2','A',12),
     ('PRIVATE','PRI','_OMA_F19','Assessments','Group2','A',13),
     ('PRIVATE','PRI','_OMA_F20','Assessments','Group2','A',14),
     ('PRIVATE','PRI','_OMA_F21','Forms','Group1','A',14),
     ('PRIVATE','PRI','_OMA_F22','Assessments','Group2','A',15),
     ('PRIVATE','PRI','_OMA_F23','Assessments','Group2','A',16),
     ('PRIVATE','PRI','_OMA_F24','Assessments','Group2','A',17),
     ('PRIVATE','PRI','_OMA_F25','Assessments','Group2','A',9),
     ('PRIVATE','PRI','_OMA_G010','Procedures','Group3','A',2),
     ('PRIVATE','PRI','_OMA_N01','Forms','Group1','A',9),
     ('PRIVATE','PRI','_OMA_N02','Forms','Group1','A',10),
     ('PRIVATE','PRI','_OMA_RECOR','Forms','Group1','A',1),
     ('PRIVATE','PRI','_OMA_RX','Procedures','Group3','A',1);
