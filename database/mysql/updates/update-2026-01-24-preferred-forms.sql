-- Add tables for provider-selected preferred forms in patient chart
-- These junction tables store encounter forms and eForms that providers
-- want quick access to in the echart left navbar

-- Junction table for preferred encounter forms in chart
CREATE TABLE IF NOT EXISTS `ProviderPreferenceChartForm` (
    `providerNo` varchar(6) NOT NULL,
    `chartForm` varchar(128) NOT NULL,
    KEY `fk_ppcf_provider` (`providerNo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Junction table for preferred eForms in chart
-- Note: Uses appointmentScreenEForm column name to match EformLink embedded class
CREATE TABLE IF NOT EXISTS `ProviderPreferenceChartEForm` (
    `providerNo` varchar(6) NOT NULL,
    `appointmentScreenEForm` int(11) NOT NULL,
    `eFormName` varchar(255) DEFAULT NULL,
    KEY `fk_ppcef_provider` (`providerNo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
