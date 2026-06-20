package ca.ssha._2005.hial;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Error", propOrder = {"number", "severity", "message", "details"})
public class Error {

    @XmlElement(name = "Number")
    protected int number;

    @XmlElement(name = "Severity", required = true)
    protected String severity;

    @XmlElement(name = "Message", required = true)
    protected String message;

    @XmlElement(name = "Details")
    protected ArrayOfString details;

    public int getNumber() {
        return number;
    }

    public void setNumber(int value) {
        this.number = value;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String value) {
        this.severity = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        this.message = value;
    }

    public ArrayOfString getDetails() {
        return details;
    }

    public void setDetails(ArrayOfString value) {
        this.details = value;
    }
}
