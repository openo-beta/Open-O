//CHECKSTYLE:OFF

package ca.openosp.openo.commn.service;

import ca.openosp.openo.commn.dao.FlowSheetCustomizationDao;
import ca.openosp.openo.commn.model.FlowSheetCustomization;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service layer for FlowSheetCustomization cascading rules.
 *
 * <p>
 * Enforces scope hierarchy where higher levels (clinic > provider > patient)
 * block customizations at lower levels. When a higher level creates a
 * customization,
 * lower levels cannot override it.
 * </p>
 *
 * @since 2025-12-23
 */
@Service
public class FlowSheetCustomizationService {

    private final FlowSheetCustomizationDao flowSheetCustomizationDao;
    private final SecurityInfoManager securityInfoManager;

    /**
     * Constructs the service with required dependencies.
     *
     * @param flowSheetCustomizationDao DAO for flowsheet customization operations
     * @param securityInfoManager       manager for security privilege checks
     */
    @Autowired
    public FlowSheetCustomizationService(
            FlowSheetCustomizationDao flowSheetCustomizationDao,
            SecurityInfoManager securityInfoManager) {
        this.flowSheetCustomizationDao = flowSheetCustomizationDao;
        this.securityInfoManager = securityInfoManager;
    }

    /**
     * Represents the scope hierarchy levels for flowsheet customizations.
     *
     * <p>
     * Higher rank values indicate higher authority levels. Clinic-level
     * customizations take precedence over provider-level, which take
     * precedence over patient-level.
     * </p>
     */
    public enum ScopeLevel {
        /** Patient-specific customization (lowest priority). */
        PATIENT(1),
        /** Provider-specific customization (medium priority). */
        PROVIDER(2),
        /** Clinic-wide customization (highest priority). */
        CLINIC(3);

        private final int rank;

        ScopeLevel(int rank) {
            this.rank = rank;
        }

        /**
         * Checks if this scope level is higher than another.
         *
         * @param other the scope level to compare against
         * @return true if this level has higher authority than the other
         */
        public boolean isHigherThan(ScopeLevel other) {
            return this.rank > other.rank;
        }

        /**
         * Returns the lowercase name of this scope level.
         *
         * @return "clinic", "provider", or "patient"
         */
        public String getName() {
            return this.name().toLowerCase();
        }
    }

    /**
     * Result object for cascading validation checks.
     */
    public static class CascadeCheckResult {
        private final boolean blocked;
        private final ScopeLevel blockingLevel;
        private final FlowSheetCustomization blockingCustomization;

        private CascadeCheckResult(boolean blocked, ScopeLevel blockingLevel,
                FlowSheetCustomization blockingCustomization) {
            this.blocked = blocked;
            this.blockingLevel = blockingLevel;
            this.blockingCustomization = blockingCustomization;
        }

        /**
         * Creates a result indicating the operation is allowed.
         *
         * @return CascadeCheckResult with blocked=false
         */
        public static CascadeCheckResult allowed() {
            return new CascadeCheckResult(false, null, null);
        }

        /**
         * Creates a result indicating the operation is blocked.
         *
         * @param blockingLevel         the scope level that is blocking
         * @param blockingCustomization the customization that is blocking
         * @return CascadeCheckResult with blocked=true
         */
        public static CascadeCheckResult blocked(ScopeLevel blockingLevel,
                FlowSheetCustomization blockingCustomization) {
            return new CascadeCheckResult(true, blockingLevel, blockingCustomization);
        }

        /**
         * Returns whether the operation is blocked.
         *
         * @return true if blocked by a higher-level customization
         */
        public boolean isBlocked() {
            return blocked;
        }

        /**
         * Returns the scope level that is blocking.
         *
         * @return the blocking ScopeLevel, or null if not blocked
         */
        public ScopeLevel getBlockingScopeLevel() {
            return blockingLevel;
        }

        /**
         * Returns the scope level name that is blocking (for backward compatibility).
         *
         * @return "clinic", "provider", or "patient", or null if not blocked
         */
        public String getBlockingLevel() {
            return blockingLevel != null ? blockingLevel.getName() : null;
        }

        /**
         * Returns the customization that is blocking.
         *
         * @return the blocking FlowSheetCustomization, or null if not blocked
         */
        public FlowSheetCustomization getBlockingCustomization() {
            return blockingCustomization;
        }
    }

    /**
     * Checks if an action is blocked by a higher-level customization.
     *
     * @param flowsheet     String the flowsheet name
     * @param measurement   String the measurement name
     * @param action        String the action type (ADD, UPDATE, DELETE)
     * @param providerNo    String current provider number (empty string for clinic scope)
     * @param demographicNo String current demographic number ("0" for clinic/provider
     *                      scope)
     * @return CascadeCheckResult indicating whether the action is blocked
     */
    public CascadeCheckResult checkCascadingBlocked(
            String flowsheet, String measurement, String action,
            String providerNo, String demographicNo) {

        FlowSheetCustomization blocking = flowSheetCustomizationDao.findHigherLevelCustomization(
                flowsheet, measurement, action, providerNo, demographicNo);

        if (blocking == null) {
            return CascadeCheckResult.allowed();
        }

        ScopeLevel blockingLevel = getScopeLevel(blocking);
        return CascadeCheckResult.blocked(blockingLevel, blocking);
    }

    /**
     * Determines the scope level for a customization.
     *
     * @param custom FlowSheetCustomization the customization to check
     * @return the ScopeLevel (CLINIC, PROVIDER, or PATIENT), or null if custom is null
     */
    public ScopeLevel getScopeLevel(FlowSheetCustomization custom) {
        if (custom == null) {
            return null;
        }

        if (custom.getProviderNo() != null
                && "".equals(custom.getProviderNo())
                && "0".equals(custom.getDemographicNo())) {
            return ScopeLevel.CLINIC;
        } else if (custom.getDemographicNo() != null && !"0".equals(custom.getDemographicNo())) {
            return ScopeLevel.PATIENT;
        } else {
            return ScopeLevel.PROVIDER;
        }
    }

    /**
     * Determines the scope level name for a customization.
     *
     * @param custom FlowSheetCustomization the customization to check
     * @return "clinic", "provider", or "patient", or null if custom is null
     */
    public String getScopeLevelName(FlowSheetCustomization custom) {
        ScopeLevel level = getScopeLevel(custom);
        return level != null ? level.getName() : null;
    }

    /**
     * Validates that the user has permission for the requested scope.
     *
     * <p>
     * Clinic-level operations require admin privileges.
     * </p>
     *
     * @param loggedInInfo LoggedInInfo the logged-in user information
     * @param scope        String the requested scope ("clinic", "provider", or patient
     *                     demographic)
     * @throws SecurityException if clinic scope requested without admin role
     */
    public void validateScopePermission(LoggedInInfo loggedInInfo, String scope) {
        if ("clinic".equals(scope)) {
            if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", "w", null)) {
                throw new SecurityException("Clinic-level customization requires admin privileges");
            }
        }
    }

    /**
     * Resolves the current scope level from the parameters.
     *
     * @param currentScope         String the scope string ("clinic" or other)
     * @param currentDemographicNo String the demographic number ("0" or patient ID)
     * @return the resolved ScopeLevel
     */
    private ScopeLevel resolveCurrentScopeLevel(String currentScope, String currentDemographicNo) {
        boolean isCurrentPatientScope = currentDemographicNo != null && !"0".equals(currentDemographicNo);
        if (isCurrentPatientScope) {
            return ScopeLevel.PATIENT;
        }
        if ("clinic".equals(currentScope)) {
            return ScopeLevel.CLINIC;
        }
        return ScopeLevel.PROVIDER;
    }

    /**
     * Checks if a customization can be archived at the current scope level.
     *
     * <p>
     * Users cannot archive customizations created at a higher scope level.
     * </p>
     *
     * @param custom               FlowSheetCustomization the customization to check
     * @param currentScope         String the current scope ("clinic", "provider", or
     *                             patient demographic)
     * @param currentProviderNo    String the current provider number
     * @param currentDemographicNo String the current demographic number
     * @return CascadeCheckResult indicating whether archiving is blocked
     */
    public CascadeCheckResult checkCanArchive(
            FlowSheetCustomization custom,
            String currentScope, String currentProviderNo, String currentDemographicNo) {

        ScopeLevel custLevel = getScopeLevel(custom);
        if (custLevel == null) {
            return CascadeCheckResult.allowed();
        }

        ScopeLevel currentLevel = resolveCurrentScopeLevel(currentScope, currentDemographicNo);

        if (custLevel.isHigherThan(currentLevel)) {
            return CascadeCheckResult.blocked(custLevel, custom);
        }

        return CascadeCheckResult.allowed();
    }
}
