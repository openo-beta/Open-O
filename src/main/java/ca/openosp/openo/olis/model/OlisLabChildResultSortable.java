//CHECKSTYLE:OFF
package ca.openosp.openo.olis.model;

import java.util.Comparator;

/**
 * Sort key for an OLIS child result (e.g. an antibiotic sensitivity under a
 * microbiology culture) used to order child results by their sort key
 * (CV06 micro display).
 *
 * <p>Derived from the oscarpro {@code org.oscarehr.olis.model.OlisLabChildResultSortable}
 * (GPLv2), namespace-migrated to {@code ca.openosp.openo}.</p>
 */
public class OlisLabChildResultSortable {
    int index;
    String status;
    String name;
    String sensitivity;
    int commentCount;
    String sortKey = "";
    String susceptibility;

    /**
     * Constructs an empty child result sortable.
     */
    public OlisLabChildResultSortable() {
    }

    /**
     * Constructs a child result sortable with all fields populated.
     *
     * @param index int the ordinal index of the child result
     * @param status String the result status
     * @param name String the child result name
     * @param sensitivity String the sensitivity value
     * @param commentCount int the number of comments on the child result
     * @param sortKey String the sort key used to order child results
     * @param susceptibility String the susceptibility value
     */
    public OlisLabChildResultSortable(int index, String status, String name, String sensitivity, int commentCount, String sortKey, String susceptibility) {
        this.index = index;
        this.status = status;
        this.name = name;
        this.sensitivity = sensitivity;
        this.commentCount = commentCount;
        this.sortKey = sortKey;
        this.susceptibility = susceptibility;
    }

    /**
     * Returns the ordinal index of the child result.
     *
     * @return int the ordinal index of the child result
     */
    public int getIndex() {
        return index;
    }
    /**
     * Sets the ordinal index of the child result.
     *
     * @param index int the ordinal index of the child result
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the result status.
     *
     * @return String the result status
     */
    public String getStatus() {
        return status;
    }
    /**
     * Sets the result status.
     *
     * @param status String the result status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the child result name.
     *
     * @return String the child result name
     */
    public String getName() {
        return name;
    }
    /**
     * Sets the child result name.
     *
     * @param name String the child result name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the sensitivity value.
     *
     * @return String the sensitivity value
     */
    public String getSensitivity() {
        return sensitivity;
    }
    /**
     * Sets the sensitivity value.
     *
     * @param sensitivity String the sensitivity value
     */
    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Returns the number of comments on the child result.
     *
     * @return int the number of comments on the child result
     */
    public int getCommentCount() {
        return commentCount;
    }
    /**
     * Sets the number of comments on the child result.
     *
     * @param commentCount int the number of comments on the child result
     */
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * Returns the sort key used to order child results.
     *
     * @return String the sort key used to order child results
     */
    public String getSortKey() {
        return sortKey;
    }
    /**
     * Sets the sort key used to order child results.
     *
     * @param sortKey String the sort key used to order child results
     */
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * Returns the susceptibility value.
     *
     * @return String the susceptibility value
     */
    public String getSusceptibility() {
        return susceptibility;
    }
    /**
     * Sets the susceptibility value.
     *
     * @param susceptibility String the susceptibility value
     */
    public void setSusceptibility(String susceptibility) {
        this.susceptibility = susceptibility;
    }

    public static final Comparator<OlisLabChildResultSortable> CHILD_RESULT_COMPARATOR =
            Comparator.comparing(
                    OlisLabChildResultSortable::getSortKey,
                    Comparator.nullsFirst(String::compareTo));
}
