package ca.openosp.openo.olis.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * One row from the eHealth Ontario Lab/SCC Extract — either a licensed
 * Laboratory (OID {@code 2.16.840.1.113883.3.59.1}) or a Specimen Collection
 * Centre (OID {@code 2.16.840.1.113883.3.59.2}).
 * <p>
 * Single table, discriminated by {@link #facilityClass} ("LAB" or "SCC")
 * because the source extract uses one sheet with identical columns and
 * derives class only from the OID column. Both classes feed the same set
 * of OLIS query-parameter dropdowns on {@code olis/Search.jsp}; the picker
 * filters by class at AJAX time via {@code OLISFacilitySearch2Action}.
 * <p>
 * Replaces the hard-coded 3-entry {@code OLISParticipatingLab} enum that
 * previously sourced all 8 Search.jsp dropdowns + 2 olis_preferences.jsp
 * dropdowns. See OLIS04.03 in {@code docs/olis/requirements-analysis.md}.
 *
 * @since 2026-05-20
 */
@Entity
public class OLISFacility extends AbstractModel<Integer> {

    public static final String CLASS_LAB = "LAB";
    public static final String CLASS_SCC = "SCC";
    public static final String CLASS_HOS = "HOS";

    public static final String OID_LAB = "2.16.840.1.113883.3.59.1";
    public static final String OID_SCC = "2.16.840.1.113883.3.59.2";
    public static final String OID_HOSP = "2.16.840.1.113883.3.59.3";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String licenceNumber;

    @Column(nullable = false)
    private String facilityClass;

    @Column(nullable = false)
    private String name;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;

    @Column(nullable = false)
    private String oid;

    @Column(nullable = false)
    private String status = "ACTIVE";

    /**
     * No-argument constructor required by JPA/Hibernate.
     *
     * @since 2026-05-20
     */
    public OLISFacility() {
        super();
    }

    /**
     * @return Integer the surrogate primary key, or {@code null} if not yet persisted
     * @since 2026-05-20
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @return String the facility's licence number (the natural key together with
     *         {@link #getFacilityClass()})
     * @since 2026-05-20
     */
    public String getLicenceNumber() {
        return licenceNumber;
    }

    /**
     * @param licenceNumber String the facility's licence number
     * @since 2026-05-20
     */
    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    /**
     * @return String the facility class, {@link #CLASS_LAB} or {@link #CLASS_SCC}
     * @since 2026-05-20
     */
    public String getFacilityClass() {
        return facilityClass;
    }

    /**
     * @param facilityClass String the facility class, {@link #CLASS_LAB} or {@link #CLASS_SCC}
     * @since 2026-05-20
     */
    public void setFacilityClass(String facilityClass) {
        this.facilityClass = facilityClass;
    }

    /**
     * @return String the facility name as it appears in the eHealth extract
     * @since 2026-05-20
     */
    public String getName() {
        return name;
    }

    /**
     * @param name String the facility name as it appears in the eHealth extract
     * @since 2026-05-20
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return String the first street-address line, or {@code null} if absent
     * @since 2026-05-20
     */
    public String getAddressLine1() {
        return addressLine1;
    }

    /**
     * @param addressLine1 String the first street-address line
     * @since 2026-05-20
     */
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * @return String the second street-address line, or {@code null} if absent
     * @since 2026-05-20
     */
    public String getAddressLine2() {
        return addressLine2;
    }

    /**
     * @param addressLine2 String the second street-address line
     * @since 2026-05-20
     */
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    /**
     * @return String the city, or {@code null} if absent
     * @since 2026-05-20
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city String the city
     * @since 2026-05-20
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return String the postal code, or {@code null} if absent
     * @since 2026-05-20
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @param postalCode String the postal code
     * @since 2026-05-20
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * @return String the HL7 OID identifying the facility class, {@link #OID_LAB}
     *         or {@link #OID_SCC}
     * @since 2026-05-20
     */
    public String getOid() {
        return oid;
    }

    /**
     * @param oid String the HL7 OID identifying the facility class, {@link #OID_LAB}
     *        or {@link #OID_SCC}
     * @since 2026-05-20
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * @return String the lifecycle status, e.g. {@code "ACTIVE"} or {@code "INACTIVE"};
     *         facilities absent from a re-import are marked {@code "INACTIVE"}
     * @since 2026-05-20
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status String the lifecycle status, e.g. {@code "ACTIVE"} or {@code "INACTIVE"}
     * @since 2026-05-20
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
