package ca.openosp.openo.commn.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * One signature in a provider's list. Each time they print a prescription, the provider picks
 * one of these or their prescriber name. Which one is their default is kept in
 * {@code providerExt.signature}, where chart notes, email notes and eForms read it.
 *
 * @since 2026-10-26
 */
@Entity
@Table(name = "providerSignature")
public class ProviderSignature extends AbstractModel<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "provider_no", nullable = false, length = 6)
    private String providerNo;

    @Column(nullable = false, length = 255)
    private String signature;

    @Column(length = 6)
    private String lastUpdateUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastUpdateDate;

    /**
     * Stamps the time of each save.
     */
    @PreUpdate
    @PrePersist
    protected void autoSetUpdateTime() {
        lastUpdateDate = new Date();
    }

    /**
     * @return Integer the signature's id
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @return String the number of the provider whose list this signature is in
     */
    public String getProviderNo() {
        return providerNo;
    }

    /**
     * @param providerNo String the number of the provider whose list this signature is in
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * @return String the text printed on the prescription
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature String the text printed on the prescription
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * @return String the number of the provider who last saved this signature
     */
    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    /**
     * @param lastUpdateUser String the number of the provider saving this signature
     */
    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    /**
     * @return Date when this signature was last saved
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }
}
