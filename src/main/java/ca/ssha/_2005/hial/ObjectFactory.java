package ca.ssha._2005.hial;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private static final QName _Response_QNAME = new QName("http://www.ssha.ca/2005/HIAL", "Response");

    public ObjectFactory() {
    }

    public Response createResponse() {
        return new Response();
    }

    public Error createError() {
        return new Error();
    }

    public ArrayOfString createArrayOfString() {
        return new ArrayOfString();
    }

    public ArrayOfError createArrayOfError() {
        return new ArrayOfError();
    }

    @XmlElementDecl(namespace = "http://www.ssha.ca/2005/HIAL", name = "Response")
    public JAXBElement<Response> createResponse(Response value) {
        return new JAXBElement<Response>(_Response_QNAME, Response.class, null, value);
    }
}
