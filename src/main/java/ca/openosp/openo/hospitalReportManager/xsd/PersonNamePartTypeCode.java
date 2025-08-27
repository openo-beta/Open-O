/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlEnum
 *  javax.xml.bind.annotation.XmlType
 */
package ca.openosp.openo.hospitalReportManager.xsd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@XmlType(name="personNamePartTypeCode")
@XmlEnum
public enum PersonNamePartTypeCode {
    FAMC,
    GIV;


    public String value() {
        return this.name();
    }

    public static PersonNamePartTypeCode fromValue(String v) {
        return PersonNamePartTypeCode.valueOf(v);
    }
}
