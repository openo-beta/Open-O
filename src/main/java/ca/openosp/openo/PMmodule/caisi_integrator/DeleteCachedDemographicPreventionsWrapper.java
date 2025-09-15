package ca.openosp.openo.PMmodule.caisi_integrator;

import java.util.List;
import java.io.Serializable;

/**
 * Serializable wrapper for demographic prevention deletion operations in CAISI integrator.
 *
 * <p>This wrapper class packages the demographic number and list of non-deleted prevention
 * IDs for synchronizing prevention record deletions across integrated healthcare facilities.
 * Rather than specifying which records to delete, it specifies which records should remain,
 * allowing the system to safely remove outdated prevention data while preserving current records.</p>
 *
 * <p>Prevention records include immunizations, screening tests, and other preventive care
 * measures that need to be synchronized across facilities to ensure comprehensive patient
 * care tracking and avoid duplicate preventive care procedures.</p>
 *
 * @see CaisiIntegratorUpdateTask
 * @see IntegratorFallBackManager
 * @since 2009
 */
public class DeleteCachedDemographicPreventionsWrapper implements Serializable
{
    private Integer demographicNo;
    private List<Integer> nonDeletedIds;

    /**
     * Default constructor for serialization framework.
     *
     * @since 2009
     */
    public DeleteCachedDemographicPreventionsWrapper() {
    }

    /**
     * Constructs wrapper with demographic number and list of prevention IDs to preserve.
     *
     * @param demographicNo Integer the patient demographic number
     * @param nonDeletedIds List&lt;Integer&gt; the prevention IDs that should not be deleted
     * @since 2009
     */
    public DeleteCachedDemographicPreventionsWrapper(final Integer demographicNo, final List<Integer> nonDeletedIds) {
        this.demographicNo = demographicNo;
        this.nonDeletedIds = nonDeletedIds;
    }

    /**
     * Gets the patient demographic number.
     *
     * @return Integer the demographic number
     * @since 2009
     */
    public Integer getDemographicNo() {
        return this.demographicNo;
    }

    /**
     * Sets the patient demographic number.
     *
     * @param demographicNo Integer the demographic number to set
     * @since 2009
     */
    public void setDemographicNo(final Integer demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Gets the list of prevention IDs that should not be deleted.
     *
     * @return List&lt;Integer&gt; the prevention IDs to preserve
     * @since 2009
     */
    public List<Integer> getNonDeletedIds() {
        return this.nonDeletedIds;
    }

    /**
     * Sets the list of prevention IDs that should not be deleted.
     *
     * @param nonDeletedIds List&lt;Integer&gt; the prevention IDs to preserve
     * @since 2009
     */
    public void setNonDeletedIds(final List<Integer> nonDeletedIds) {
        this.nonDeletedIds = nonDeletedIds;
    }
}
