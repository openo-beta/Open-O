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

    /**
     * Constructs an empty microorganism nomenclature entry.
     */
    public OLISMicroorganismNomenclature() {
        super();
    }

    /**
     * Returns the primary key identifier.
     *
     * @return Integer the primary key identifier
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Returns the OLIS microorganism code.
     *
     * @return String the OLIS microorganism code
     */
    public String getMicroorganismCode() {
        return microorganismCode;
    }

    /**
     * Sets the OLIS microorganism code.
     *
     * @param microorganismCode String the OLIS microorganism code
     */
    public void setMicroorganismCode(String microorganismCode) {
        this.microorganismCode = microorganismCode;
    }

    /**
     * Returns the microorganism type.
     *
     * @return String the microorganism type
     */
    public String getMicroorganismType() {
        return microorganismType;
    }

    /**
     * Sets the microorganism type.
     *
     * @param microorganismType String the microorganism type
     */
    public void setMicroorganismType(String microorganismType) {
        this.microorganismType = microorganismType;
    }

    /**
     * Returns the taxonomic level.
     *
     * @return String the taxonomic level
     */
    public String getTaxonomicLevel() {
        return taxonomicLevel;
    }

    /**
     * Sets the taxonomic level.
     *
     * @param taxonomicLevel String the taxonomic level
     */
    public void setTaxonomicLevel(String taxonomicLevel) {
        this.taxonomicLevel = taxonomicLevel;
    }

    /**
     * Returns the microorganism name.
     *
     * @return String the microorganism name
     */
    public String getMicroorganismName() {
        return microorganismName;
    }

    /**
     * Sets the microorganism name.
     *
     * @param microorganismName String the microorganism name
     */
    public void setMicroorganismName(String microorganismName) {
        this.microorganismName = microorganismName;
    }

    /**
     * Returns the first alternate name.
     *
     * @return String the first alternate name
     */
    public String getAlternateName1() {
        return alternateName1;
    }

    /**
     * Sets the first alternate name.
     *
     * @param alternateName1 String the first alternate name
     */
    public void setAlternateName1(String alternateName1) {
        this.alternateName1 = alternateName1;
    }

    /**
     * Returns the second alternate name.
     *
     * @return String the second alternate name
     */
    public String getAlternateName2() {
        return alternateName2;
    }

    /**
     * Sets the second alternate name.
     *
     * @param alternateName2 String the second alternate name
     */
    public void setAlternateName2(String alternateName2) {
        this.alternateName2 = alternateName2;
    }

    /**
     * Returns the short name.
     *
     * @return String the short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the short name.
     *
     * @param shortName String the short name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Returns the source.
     *
     * @return String the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source.
     *
     * @param source String the source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the external link.
     *
     * @return String the external link
     */
    public String getExternalLink() {
        return externalLink;
    }

    /**
     * Sets the external link.
     *
     * @param externalLink String the external link
     */
    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    /**
     * Returns the reportable indicator.
     *
     * @return String the reportable indicator
     */
    public String getReportable() {
        return reportable;
    }

    /**
     * Sets the reportable indicator.
     *
     * @param reportable String the reportable indicator
     */
    public void setReportable(String reportable) {
        this.reportable = reportable;
    }

    /**
     * Returns the reportable context.
     *
     * @return String the reportable context
     */
    public String getReportableContext() {
        return reportableContext;
    }

    /**
     * Sets the reportable context.
     *
     * @param reportableContext String the reportable context
     */
    public void setReportableContext(String reportableContext) {
        this.reportableContext = reportableContext;
    }

    /**
     * Returns the effective start date.
     *
     * @return String the effective start date
     */
    public String getEffectiveStartDate() {
        return effectiveStartDate;
    }

    /**
     * Sets the effective start date.
     *
     * @param effectiveStartDate String the effective start date
     */
    public void setEffectiveStartDate(String effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    /**
     * Returns the effective end date.
     *
     * @return String the effective end date
     */
    public String getEffectiveEndDate() {
        return effectiveEndDate;
    }

    /**
     * Sets the effective end date.
     *
     * @param effectiveEndDate String the effective end date
     */
    public void setEffectiveEndDate(String effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    /**
     * Returns the change note.
     *
     * @return String the change note
     */
    public String getChangeNote() {
        return changeNote;
    }

    /**
     * Sets the change note.
     *
     * @param changeNote String the change note
     */
    public void setChangeNote(String changeNote) {
        this.changeNote = changeNote;
    }

    /**
     * Returns the comments.
     *
     * @return String the comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the comments.
     *
     * @param comments String the comments
     */
    public void setComments(String comments) {
        this.comments = comments;
    }
}
