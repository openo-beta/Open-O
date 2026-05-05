-- Indexes for consultationRequests table to improve query performance
-- Addresses slow consultation page load times by optimizing common query patterns

-- Index for status + referral date filtering (common filter pattern)
CREATE INDEX IF NOT EXISTS idx_consult_status_referaldate ON consultationRequests (status, referalDate);

-- Index for team-based queries with status and date filtering (main team listing query)
CREATE INDEX IF NOT EXISTS idx_consult_sendto_status_referaldate ON consultationRequests (sendTo, status, referalDate);

-- Index for provider workload queries
CREATE INDEX IF NOT EXISTS idx_consult_providerno_status_referaldate ON consultationRequests (providerNo, status, referalDate);

-- Index for appointment date search mode
CREATE INDEX IF NOT EXISTS idx_consult_status_appointmentdate ON consultationRequests (status, appointmentDate);

-- Index for demographic lookups (heavily used)
CREATE INDEX IF NOT EXISTS idx_consult_demographicno ON consultationRequests (demographicNo);

-- Index for service foreign key lookups
CREATE INDEX IF NOT EXISTS idx_consult_serviceid ON consultationRequests (serviceId);

-- Index for specialist foreign key lookups
CREATE INDEX IF NOT EXISTS idx_consult_specid ON consultationRequests (specId);

-- Index for sync/update tracking queries
CREATE INDEX IF NOT EXISTS idx_consult_lastupdatedate ON consultationRequests (lastUpdateDate);
