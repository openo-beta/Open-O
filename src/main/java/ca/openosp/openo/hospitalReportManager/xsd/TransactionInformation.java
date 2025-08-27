/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlRootElement
 *  javax.xml.bind.annotation.XmlType
 */
package ca.openosp.openo.hospitalReportManager.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import ca.openosp.openo.hospitalReportManager.xsd.PersonNameSimple;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="", propOrder={"messageUniqueID", "deliverToUserID", "provider"})
@XmlRootElement(name="TransactionInformation", namespace="cds")
public class TransactionInformation {
    @XmlElement(name="MessageUniqueID", namespace="cds", required=true)
    protected String messageUniqueID;
    @XmlElement(name="DeliverToUserID", namespace="cds", required=true)
    protected String deliverToUserID;
    @XmlElement(name="Provider", namespace="cds", required=true)
    protected PersonNameSimple provider;

    public String getMessageUniqueID() {
        return this.messageUniqueID;
    }

    public void setMessageUniqueID(String value) {
        this.messageUniqueID = value;
    }

    public String getDeliverToUserID() {
        return this.deliverToUserID;
    }

    public void setDeliverToUserID(String value) {
        this.deliverToUserID = value;
    }

    public PersonNameSimple getProvider() {
        return this.provider;
    }

    public void setProvider(PersonNameSimple value) {
        this.provider = value;
    }
}
