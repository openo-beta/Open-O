-- HRM Sending Facility registry for OntarioMD HRM Validation
-- Maps Sending Facility IDs (as they appear in HRM XML <SendingFacility>)
-- to human-readable facility names. Required to display facility name
-- alongside the raw ID on HRM report views.
  CREATE TABLE IF NOT EXISTS `HRMSendingFacility` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `sendingFacilityId` varchar(50) NOT NULL,
    `facilityName` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_HRMSendingFacility_sfId` (`sendingFacilityId`)
  );