//CHECKSTYLE:OFF
package ca.openosp.openo.olis.model;

import ca.openosp.openo.olis.OLISUtils;

import java.util.Comparator;
import java.util.Date;

/**
 * Sort key for a single OLIS test result (OBX) used to order results within a
 * test request per the OLIS display-sequence rules (CV04/05/06/15).
 *
 * <p>Derived from the oscarpro {@code org.oscarehr.olis.model.OlisLabResultSortable}
 * (GPLv2), namespace-migrated to {@code ca.openosp.openo}.</p>
 */
public class OlisLabResultSortable {
    private Integer setId = 0;
    private String subId = "";
    private String nomenclatureSortKey = "";
    private String alternateName = "";
    private boolean isAncillary = false;
    private Date releaseDate = new Date();
    private String zbxSortKey = "";

    public OlisLabResultSortable() {
    }

    public OlisLabResultSortable(Integer setId, String subId, String nomenclatureSortKey, String alternateName, boolean isAncillary, Date releaseDate, String zbxSortKey) {
        this.setId = setId;
        this.subId = subId;
        this.nomenclatureSortKey = nomenclatureSortKey;
        this.alternateName = alternateName;
        this.isAncillary = isAncillary;
        this.releaseDate = releaseDate;
        this.zbxSortKey = zbxSortKey;
    }

    public Integer getSetId() {
        return setId;
    }
    public void setSetId(Integer setId) {
        this.setId = setId;
    }

    public String getSubId() {
        return subId;
    }
    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getNomenclatureSortKey() {
        return nomenclatureSortKey;
    }
    public void setNomenclatureSortKey(String nomenclatureSortKey) {
        this.nomenclatureSortKey = nomenclatureSortKey;
    }

    public String getAlternateName() {
        return alternateName;
    }
    public void setAlternateName(String alternateName) {
        this.alternateName = alternateName;
    }

    public boolean isAncillary() {
        return isAncillary;
    }
    public void setIsAncillary(boolean isAncillary) {
        this.isAncillary = isAncillary;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getZbxSortKey() {
        return zbxSortKey;
    }
    public void setZbxSortKey(String zbxSortKey) {
        this.zbxSortKey = zbxSortKey;
    }

    /**
     * Orders OLIS results based on the rules provided by OLIS
     *
     * 1. If OBX.11 = Z (ancillary), then sort this test result first
     * 2. If multiple ancillary test results, sort by test result sort key (either from ZBX.2 or OLIS) within ancillary test results
     * 3. If no sort key, then sort by OLIS test result alternate name 1 then sort the following in ascending alphanumeric order:
     * 4. Test result sort key in HL7 message (ZBX.2)
     * 5. If no ZBX.2, then lookup and use OLIS nomenclature sort key
     * 6. If duplicate ZBX.2, then sort by OLIS nomenclature sort key (if no OLIS sort key, then lookup and use test result alternate name 1) within duplicates, then sort by ZBX.2 sort key
     * 7. If no OLIS sort key, then lookup and use OLIS test result alternate name 1
     * 8. If duplicate alternate name 1, then sort by observation sub-ID (OBX.4) within results that share the same alternate name 1, then sort by alternate name 1
     * 9. If no or duplicate sub-ID, then sort by test result release date/time (ZBX.1) within results that share the same sub-ID, then sort by alternate name 1
     * Note: OLIS will reject an HL7 message (data collection) if more than one OBX segment has the same value in OBX.3, OBX.4 and ZBX.1. Refer to OLIS FAQs, #42.
     */
    public static final Comparator<OlisLabResultSortable> OLIS_RESULT_COMPARATOR = new Comparator<OlisLabResultSortable>() {
        @Override
        public int compare(OlisLabResultSortable o1, OlisLabResultSortable o2) {
            // Checks if o1 and o2 are ancillary, if they both are, then further compares them however if only one is, then it comes first in the list
            if (o1.isAncillary() && o2.isAncillary()) {
                // If both are ancillary, checks if the ZBX sort key is the same, whether they are blank strings or not
                if (o1.getZbxSortKey().equals(o2.getZbxSortKey())) {
                    // If they are the same, compares them based on the alternate name
                    return OLISUtils.compareStringEmptyIsMore(o1.getAlternateName(), o2.getAlternateName());
                } else {
                    // If they aren't the same, compares them to each other
                    return OLISUtils.compareStringEmptyIsMore(o1.getZbxSortKey(), o2.getZbxSortKey());
                }
            } else if (o1.isAncillary()) {
                return -1;
            } else if (o2.isAncillary()) {
                return 1;
            } else {
                // For non ancillary results, first checks to see if the ZBX sort keys are the same, whether or not they are empty
                if (o1.getZbxSortKey().equals(o2.getZbxSortKey())) {
                    // If the ZBX sort keys are the same, then it checks the nomenclature sort keys are the same
                    if (o1.getNomenclatureSortKey().equals(o2.getNomenclatureSortKey())) {
                        // If the nomenclature sort keys are the same, then it checks if the alternate names are the same
                        if (o1.getAlternateName().equals(o2.getAlternateName())) {
                            // If the alternate names are the same, checks if the sub ids are the same
                            if (o1.getSubId().equals(o2.getSubId())) {
                                // If the sub ids are the same, use the result release date for the ordering
                                return o1.getReleaseDate().compareTo(o2.getReleaseDate());
                            } else {
                                // If the sub ids aren't the same, compares them, placing empty strings after strings with text
                                return OLISUtils.compareStringEmptyIsMore(o1.getSubId(), o2.getSubId());
                            }
                        } else {
                            // If the alternate names aren't the same, compares them, placing empty strings after strings with text
                            return OLISUtils.compareStringEmptyIsMore(o1.getAlternateName(), o2.getAlternateName());
                        }
                    } else {
                        // If the nomenclature sort keys aren't the same, compares them, placing empty strings after strings with text
                        return OLISUtils.compareStringEmptyIsMore(o1.getNomenclatureSortKey(), o2.getNomenclatureSortKey());
                    }
                } else {
                    // If the Zbx sort keys aren't the same, compares them, placing empty strings after strings with text
                    return OLISUtils.compareStringEmptyIsMore(o1.getZbxSortKey(), o2.getZbxSortKey());
                }
            }
        }
    };
}
