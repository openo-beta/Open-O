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

    public OlisLabChildResultSortable() {
    }

    public OlisLabChildResultSortable(int index, String status, String name, String sensitivity, int commentCount, String sortKey, String susceptibility) {
        this.index = index;
        this.status = status;
        this.name = name;
        this.sensitivity = sensitivity;
        this.commentCount = commentCount;
        this.sortKey = sortKey;
        this.susceptibility = susceptibility;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSensitivity() {
        return sensitivity;
    }
    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getSortKey() {
        return sortKey;
    }
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public String getSusceptibility() {
        return susceptibility;
    }
    public void setSusceptibility(String susceptibility) {
        this.susceptibility = susceptibility;
    }

    public static final Comparator<OlisLabChildResultSortable> CHILD_RESULT_COMPARATOR = new Comparator<OlisLabChildResultSortable>() {
        @Override
        public int compare(OlisLabChildResultSortable o1, OlisLabChildResultSortable o2) {
            return o1.sortKey.compareTo(o2.sortKey);
        }
    };
}
