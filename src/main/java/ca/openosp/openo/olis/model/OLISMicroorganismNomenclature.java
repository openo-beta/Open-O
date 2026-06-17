//CHECKSTYLE:OFF
package ca.openosp.openo.olis.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * OLIS microorganism nomenclature catalog entry. Maps an OLIS microorganism code
 * (sent in a microbiology result as a coded entry, OBX value type {@code CE},
 * coding system {@code HL79905}, code in OBX-5.1) to a human-readable organism
 * name for display (CV06 micro/culture results, CV04 §13.1 coded entries).
 *
 * <p>Columns mirror the "OLIS List of Microorganisms" sheet of the OLIS
 * Nomenclatures distribution. The display path needs only
 * {@code microorganismCode} → {@code alternateName1}; the remaining fields are
 * carried for catalog parity. Derived from the oscarpro
 * {@code org.oscarehr.olis.model.OlisMicroorganismNomenclature} (GPLv2),
 * namespace-migrated and trimmed to the columns present in the V3.04 sheet.</p>
 *
 * @since 2026-06-17
 */
@Entity
public class OLISMicroorganismNomenclature extends AbstractModel<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String microorganismCode;
    private String microorganismType;
    private String taxonomicLevel;
    private String microorganismName;
    private String alternateName1;
    private String alternateName2;
    private String shortName;
    private String source;
    private String externalLink;
    private String reportable;
    private String reportableContext;
    private String effectiveStartDate;
    private String effectiveEndDate;
    private String changeNote;
    private String comments;

    public OLISMicroorganismNomenclature() {
        super();
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getMicroorganismCode() {
        return microorganismCode;
    }

    public void setMicroorganismCode(String microorganismCode) {
        this.microorganismCode = microorganismCode;
    }

    public String getMicroorganismType() {
        return microorganismType;
    }

    public void setMicroorganismType(String microorganismType) {
        this.microorganismType = microorganismType;
    }

    public String getTaxonomicLevel() {
        return taxonomicLevel;
    }

    public void setTaxonomicLevel(String taxonomicLevel) {
        this.taxonomicLevel = taxonomicLevel;
    }

    public String getMicroorganismName() {
        return microorganismName;
    }

    public void setMicroorganismName(String microorganismName) {
        this.microorganismName = microorganismName;
    }

    public String getAlternateName1() {
        return alternateName1;
    }

    public void setAlternateName1(String alternateName1) {
        this.alternateName1 = alternateName1;
    }

    public String getAlternateName2() {
        return alternateName2;
    }

    public void setAlternateName2(String alternateName2) {
        this.alternateName2 = alternateName2;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getReportable() {
        return reportable;
    }

    public void setReportable(String reportable) {
        this.reportable = reportable;
    }

    public String getReportableContext() {
        return reportableContext;
    }

    public void setReportableContext(String reportableContext) {
        this.reportableContext = reportableContext;
    }

    public String getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(String effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public String getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(String effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public String getChangeNote() {
        return changeNote;
    }

    public void setChangeNote(String changeNote) {
        this.changeNote = changeNote;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
