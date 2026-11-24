package ca.ssha._2005.hial;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Response", propOrder = {"content", "errors"})
public class Response {

    @XmlElement(name = "Content")
    protected String content;

    @XmlElement(name = "Errors")
    protected ArrayOfError errors;

    public String getContent() {
        return content;
    }

    public void setContent(String value) {
        this.content = value;
    }

    public ArrayOfError getErrors() {
        return errors;
    }

    public void setErrors(ArrayOfError value) {
        this.errors = value;
    }
}
