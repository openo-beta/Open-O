/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlType
 */
package ca.openosp.openo.hospitalReportManager.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="reportContent", propOrder={"textContent", "media"})
public class ReportContent {
    @XmlElement(name="TextContent")
    protected String textContent;
    @XmlElement(name="Media")
    protected byte[] media;

    public String getTextContent() {
        return this.textContent;
    }

    public void setTextContent(String value) {
        this.textContent = value;
    }

    public byte[] getMedia() {
        return this.media;
    }

    public void setMedia(byte[] value) {
        this.media = value;
    }
}
