package ca.openosp.openo.PMmodule.caisi_integrator;

import java.util.List;
import java.io.Serializable;

/**
 * Serializable wrapper for program deletion operations in CAISI integrator.
 *
 * <p>This wrapper class packages a list of program IDs for batch deletion operations
 * across integrated healthcare facilities. It enables the safe transmission of program
 * deletion requests while maintaining referential integrity and proper cleanup of
 * program-related data across the integrated healthcare network.</p>
 *
 * <p>Program deletion is a critical operation that affects patient enrollment, service
 * delivery tracking, and resource allocation across multiple facilities. This wrapper
 * ensures that program deletions are properly coordinated and synchronized across all
 * integrated systems to maintain data consistency.</p>
 *
 * @see CaisiIntegratorUpdateTask
 * @see CaisiIntegratorManager
 * @since 2009
 */
public class ProgramDeleteIdWrapper implements Serializable
{
    private List<Integer> ids;

    /**
     * Default constructor initializing with null ID list.
     *
     * @since 2009
     */
    public ProgramDeleteIdWrapper() {
        this.ids = null;
    }

    /**
     * Constructs wrapper with the specified list of program IDs to delete.
     *
     * @param ids List&lt;Integer&gt; the program IDs to delete
     * @since 2009
     */
    public ProgramDeleteIdWrapper(final List<Integer> ids) {
        this.ids = ids;
    }

    /**
     * Gets the list of program IDs to delete.
     *
     * @return List&lt;Integer&gt; the program IDs, or null if not set
     * @since 2009
     */
    public List<Integer> getIds() {
        return this.ids;
    }

    /**
     * Sets the list of program IDs to delete.
     *
     * @param ids List&lt;Integer&gt; the program IDs to set
     * @since 2009
     */
    public void setIds(final List<Integer> ids) {
        this.ids = ids;
    }
}
