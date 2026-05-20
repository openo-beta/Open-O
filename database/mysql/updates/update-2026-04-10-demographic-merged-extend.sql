-- Create a new table demographic_merged_event to record merge/unmerge audit events.
-- The existing demographic_merged table is left untouched so that a rollback to the
-- old merge code continues to work without any schema remediation.
CREATE TABLE IF NOT EXISTS demographic_merged_event (
  id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  primary_demographic_no INT(10) NULL DEFAULT NULL,
  secondary_demographic_no VARCHAR(500) NULL DEFAULT NULL,
  merged_demographic_no INT(10) NULL DEFAULT NULL,
  event_type VARCHAR(20) NULL DEFAULT NULL,
  provider_no VARCHAR(6) NULL DEFAULT NULL,
  event_date DATETIME NULL DEFAULT NULL,
  INDEX idx_dme_merged_demo_no (merged_demographic_no),
  INDEX idx_dme_event_type (event_type)
);
