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

    public static final String OID_LAB = "2.16.840.1.113883.3.59.1";
    public static final String OID_SCC = "2.16.840.1.113883.3.59.2";

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

    public OLISFacility() {
        super();
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public String getFacilityClass() {
        return facilityClass;
    }

    public void setFacilityClass(String facilityClass) {
        this.facilityClass = facilityClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
