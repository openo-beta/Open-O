-- Replace demographic_merged with a new schema that supports the DemographicMerge
-- event model. The old table tracked simple parent->child links (demographic_no,
-- merged_to, deleted). The new table records full merge/unmerge events with primary,
-- secondary, and merged demographic numbers, event type, provider, and timestamp.
-- Old unused columns (demographic_no, merged_to, deleted, lastUpdateUser, lastUpdateDate)
-- are dropped entirely.
DROP TABLE IF EXISTS demographic_merged;

CREATE TABLE demographic_merged (
  id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  primary_demographic_no INT(10) NULL DEFAULT NULL,
  secondary_demographic_no VARCHAR(500) NULL DEFAULT NULL,
  merged_demographic_no INT(10) NULL DEFAULT NULL,
  event_type VARCHAR(20) NULL DEFAULT NULL,
  provider_no VARCHAR(6) NULL DEFAULT NULL,
  event_date DATETIME NULL DEFAULT NULL,
  INDEX idx_dm_merged_demo_no (merged_demographic_no),
  INDEX idx_dm_event_type (event_type)
);
