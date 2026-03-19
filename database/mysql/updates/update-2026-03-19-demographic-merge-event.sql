CREATE TABLE IF NOT EXISTS demographic_merge_event (
    id                       INT(10)      NOT NULL AUTO_INCREMENT,
    primary_demographic_no   INT(10)      NOT NULL,
    secondary_demographic_no VARCHAR(500) NOT NULL,
    merged_demographic_no    INT(10)      NOT NULL,
    event_type               VARCHAR(20)  NOT NULL,
    provider_no              VARCHAR(6)   NOT NULL,
    event_date               DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_dme_merged  (merged_demographic_no),
    INDEX idx_dme_primary (primary_demographic_no)
);
