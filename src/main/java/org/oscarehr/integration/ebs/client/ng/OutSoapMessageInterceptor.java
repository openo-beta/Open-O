package org.oscarehr.integration.ebs.client.ng;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.io.CachedOutputStream;

public class OutSoapMessageInterceptor extends AbstractPhaseInterceptor<Message> {

    public OutSoapMessageInterceptor() {
        super(Phase.PRE_STREAM); // You can also try Phase.PRE_PROTOCOL or Phase.MARSHAL
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        final CachedOutputStream cachedOutputStream = new CachedOutputStream();
        message.setContent(OutputStream.class, cachedOutputStream);

        message.getInterceptorChain().add(new AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM_ENDING) {
            @Override
            public void handleMessage(Message message) throws Fault {
                StringBuilder messageInfo = new StringBuilder();

                try {
                    // Get the content of the message as an InputStream
                    InputStream is = message.getContent(InputStream.class);
                    if (is != null) {
                        // Copy the input stream to a CachedOutputStream so we can read it
                        CachedOutputStream cos = new CachedOutputStream();
                        IOUtils.copy(is, cos);
                        cos.flush();

                        // Reset the input stream to the beginning and set it back in the message
                        message.setContent(InputStream.class, cos.getInputStream());

                        // Convert the CachedOutputStream to a String (decrypted message)
                        String soapMessage = IOUtils.toString(cos.getInputStream(), "UTF-8");

                        // Print the decrypted SOAP message to the console
                        System.out.println("Decrypted SOAP Response Message: \n" + soapMessage);
                    } else {
                        System.out.println("No InputStream found in the message content.");
                    }
                } catch (Exception e) {
                    throw new Fault(e);
                }

                // Add message properties
                messageInfo.append("\nMessage Properties:\n");
                Map<String, Object> properties = message;
                for (Map.Entry<String, Object> property : properties.entrySet()) {
                    messageInfo.append(property.getKey()).append(": ").append(property.getValue()).append("\n");
                }

                System.out.println(messageInfo.toString());

                try {
                    SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
                    if (soapMessage != null) {
                        // Convert the SOAPMessage to a String (XML format)
                        StringWriter sw = new StringWriter();
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        transformer.transform(new DOMSource(soapMessage.getSOAPPart()), new StreamResult(sw));

                        String xmlMessage = sw.toString();
                        System.out.println("Decrypted SOAP Request XML: \n" + xmlMessage);
                    } else {
                        System.out.println("No SOAP Message found in the request.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
