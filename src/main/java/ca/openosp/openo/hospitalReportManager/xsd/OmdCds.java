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
import ca.openosp.openo.hospitalReportManager.xsd.PatientRecord;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="", propOrder={"patientRecord"})
@XmlRootElement(name="OmdCds", namespace="cds")
public class OmdCds {
    @XmlElement(name="PatientRecord", namespace="cds", required=true)
    protected PatientRecord patientRecord;

    public PatientRecord getPatientRecord() {
        return this.patientRecord;
    }

    public void setPatientRecord(PatientRecord value) {
        this.patientRecord = value;
    }
}
