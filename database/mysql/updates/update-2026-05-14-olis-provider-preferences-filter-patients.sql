-- OLIS C3: per-provider unmatched-routing override (OLIS02.03)
-- Nullable — NULL inherits the system-level OLISSystemPreferences.filterPatients default.
  ALTER TABLE OLISProviderPreferences ADD COLUMN filterPatients tinyint(1) DEFAULT NULL;