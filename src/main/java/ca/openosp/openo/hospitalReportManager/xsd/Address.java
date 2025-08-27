/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlType
 */
package ca.openosp.openo.hospitalReportManager.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import ca.openosp.openo.hospitalReportManager.xsd.AddressStructured;
import ca.openosp.openo.hospitalReportManager.xsd.AddressType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="address", propOrder={"formatted", "structured"})
public class Address {
    @XmlElement(name="Formatted")
    protected String formatted;
    @XmlElement(name="Structured")
    protected AddressStructured structured;
    @XmlAttribute(required=true)
    protected AddressType addressType;

    public String getFormatted() {
        return this.formatted;
    }

    public void setFormatted(String value) {
        this.formatted = value;
    }

    public AddressStructured getStructured() {
        return this.structured;
    }

    public void setStructured(AddressStructured value) {
        this.structured = value;
    }

    public AddressType getAddressType() {
        return this.addressType;
    }

    public void setAddressType(AddressType value) {
        this.addressType = value;
    }
}
