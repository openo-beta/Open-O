package org.oscarehr.integration.ebs.client.ng;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.cxf.attachment.LazyAttachmentCollection;
import org.apache.cxf.binding.xml.interceptor.XMLMessageInInterceptor;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class DownloadInInterceptor extends AbstractPhaseInterceptor<Message> {

	public DownloadInInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		LazyAttachmentCollection attachments = (LazyAttachmentCollection) 
				message.get(org.apache.cxf.message.Message.ATTACHMENTS);
		if (attachments != null) {
			for(Attachment a : attachments.getLoadedAttachments()) {
				System.out.println(a.getId());
				try {
					System.out.println(IOUtils.readStringFromStream(a.getDataHandler().getInputStream()));
				} catch (IOException e) {
					// swallow
				}
			}
		}
		
		
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
                System.out.println("Decrypted SOAP Response XML: \n" + xmlMessage);
            } else {
                System.out.println("No SOAP Message found in the response.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		// SOAPMessage soapMessage = (SOAPMessage) message;
	}

}
