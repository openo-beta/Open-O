package ca.openosp.openo.ws;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "helloWorld2", propOrder = { "arg0" })
public class HelloWorld2 implements Serializable
{
    private static final long serialVersionUID = 1L;
    protected String arg0;
    
    public String getArg0() {
        return this.arg0;
    }
    
    public void setArg0(final String arg0) {
        this.arg0 = arg0;
    }
}
