package ca.openosp.openo.PMmodule.caisi_integrator;

import ca.openosp.openo.caisi_integrator.dao.FacilityIdDemographicIssueCompositePk;
import java.util.List;
import java.io.Serializable;

/**
 * Serializable wrapper for demographic issue deletion operations in CAISI integrator.
 *
 * <p>This wrapper class packages the demographic number and associated issue keys
 * for batch deletion operations across integrated healthcare facilities. It enables
 * the safe transmission of deletion requests for patient clinical issues while
 * maintaining referential integrity across multiple facilities.</p>
 *
 * <p>The wrapper ensures that when clinical issues are deleted or updated in one
 * facility, the corresponding cached copies in the integrated network can be
 * properly synchronized, maintaining accurate patient care information across
 * the healthcare system.</p>
 *
 * @see FacilityIdDemographicIssueCompositePk
 * @see CaisiIntegratorUpdateTask
 * @since 2009
 */
public class DeleteCachedDemographicIssuesWrapper implements Serializable
{
    private Integer demographicNo;
    private List<FacilityIdDemographicIssueCompositePk> keys;

    /**
     * Default constructor for serialization framework.
     *
     * @since 2009
     */
    public DeleteCachedDemographicIssuesWrapper() {
    }

    /**
     * Constructs wrapper with demographic number and issue keys for deletion.
     *
     * @param demographicNo Integer the patient demographic number
     * @param keys List&lt;FacilityIdDemographicIssueCompositePk&gt; the issue keys to delete
     * @since 2009
     */
    public DeleteCachedDemographicIssuesWrapper(final Integer demographicNo, final List<FacilityIdDemographicIssueCompositePk> keys) {
        this.demographicNo = demographicNo;
        this.keys = keys;
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
     * Gets the list of issue composite keys to delete.
     *
     * @return List&lt;FacilityIdDemographicIssueCompositePk&gt; the issue keys
     * @since 2009
     */
    public List<FacilityIdDemographicIssueCompositePk> getKeys() {
        return this.keys;
    }

    /**
     * Sets the list of issue composite keys to delete.
     *
     * @param keys List&lt;FacilityIdDemographicIssueCompositePk&gt; the issue keys to set
     * @since 2009
     */
    public void setKeys(final List<FacilityIdDemographicIssueCompositePk> keys) {
        this.keys = keys;
    }
}
