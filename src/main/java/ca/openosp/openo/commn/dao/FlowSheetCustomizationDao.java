//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.FlowSheetCustomization;

public interface FlowSheetCustomizationDao extends AbstractDao<FlowSheetCustomization> {
    FlowSheetCustomization getFlowSheetCustomization(Integer id);

    List<FlowSheetCustomization> getFlowSheetCustomizations(String flowsheet, String provider, Integer demographic);

    List<FlowSheetCustomization> getFlowSheetCustomizations(String flowsheet, String provider);

    List<FlowSheetCustomization> getFlowSheetCustomizations(String flowsheet);

    /**
     * Gets customizations created at clinic level only (providerNo="" AND demographicNo="0").
     * Does not include provider-level or patient-level customizations.
     *
     * @param flowsheet the flowsheet name
     * @return list of clinic-level customizations
     * @since 2025-12-29
     */
    List<FlowSheetCustomization> getClinicLevelCustomizations(String flowsheet);

    /**
     * Gets customizations created at a specific provider level only (demographicNo="0").
     * Does not include clinic-level or patient-level customizations.
     *
     * @param flowsheet the flowsheet name
     * @param providerNo the provider number
     * @return list of provider-level customizations
     * @since 2025-12-29
     */
    List<FlowSheetCustomization> getProviderLevelCustomizations(String flowsheet, String providerNo);

    /**
     * Gets customizations created at a specific patient level.
     * Does not include clinic-level or provider-level customizations.
     *
     * <p>Note: Patient-level records may have different formats due to migration:
     * <ul>
     *   <li>New format: providerNo="" AND demographicNo=&lt;demographic&gt;</li>
     *   <li>Legacy format: providerNo=&lt;provider&gt; AND demographicNo=&lt;demographic&gt;</li>
     * </ul>
     * This method returns both formats.</p>
     *
     * @param flowsheet the flowsheet name
     * @param demographicNo the demographic number (must not be "0")
     * @return list of patient-level customizations
     * @since 2025-12-29
     */
    List<FlowSheetCustomization> getPatientLevelCustomizations(String flowsheet, String demographicNo);

    /**
     * Finds a customization at a higher scope level for a specific measurement and action.
     * Used to enforce cascading rules where higher levels (clinic/provider) block lower levels.
     *
     * <p>Scope hierarchy: clinic > provider > patient</p>
     * <ul>
     *   <li>For patient scope: checks clinic and provider levels</li>
     *   <li>For provider scope: checks clinic level only</li>
     *   <li>For clinic scope: returns null (nothing is higher)</li>
     * </ul>
     *
     * @param flowsheet the flowsheet name
     * @param measurement the measurement name
     * @param action the action type (ADD, UPDATE, DELETE)
     * @param providerNo current provider number (empty string for clinic scope)
     * @param demographicNo current demographic number ("0" for clinic/provider scope)
     * @return the blocking customization from a higher level, or null if none exists
     * @since 2025-12-23
     */
    FlowSheetCustomization findHigherLevelCustomization(
        String flowsheet, String measurement, String action,
        String providerNo, String demographicNo);
}
