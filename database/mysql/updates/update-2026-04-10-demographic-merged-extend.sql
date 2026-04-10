-- Extend demographic_merged to carry the new merge-event fields required by
-- DemographicMerge (formerly DemographicMergeEvent). All columns are nullable
-- so existing rows written by the old merge code are unaffected.
ALTER TABLE demographic_merged
  ADD COLUMN primary_demographic_no   INT(10)      NULL AFTER deleted,
  ADD COLUMN secondary_demographic_no VARCHAR(500) NULL AFTER primary_demographic_no,
  ADD COLUMN merged_demographic_no    INT(10)      NULL AFTER secondary_demographic_no,
  ADD COLUMN event_type               VARCHAR(20)  NULL AFTER merged_demographic_no,
  ADD COLUMN provider_no              VARCHAR(6)   NULL AFTER event_type,
  ADD COLUMN event_date               DATETIME     NULL AFTER provider_no;

CREATE INDEX idx_dm_merged_demo_no ON demographic_merged (merged_demographic_no);
CREATE INDEX idx_dm_event_type     ON demographic_merged (event_type);
