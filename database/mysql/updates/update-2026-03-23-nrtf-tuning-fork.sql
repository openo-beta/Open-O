--
-- Add NRTF (Neurological Exam 128Hz Tuning Fork D1) measurement type
-- for diabetes flowsheet compliance with OMD DE16.066
-- Fixes: https://github.com/openo-beta/Open-O/issues/2338
--

INSERT INTO `measurementType` (`type`, `typeDisplayName`, `typeDescription`, `measuringInstruction`, `validation`, `createDate`)
VALUES ('NRTF', 'Neurological exam: 128Hz tuning fork D1', 'Neurological exam: 128Hz tuning fork D1', 'Normal', '7', '2026-03-23 00:00:00')
ON DUPLICATE KEY UPDATE `typeDisplayName`='Neurological exam: 128Hz tuning fork D1';
