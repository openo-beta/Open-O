package org.oscarehr.integration.ebs.client.ng;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

        // ---- Handle attachments safely ----
        Object attObj = message.get(Message.ATTACHMENTS);
        if (attObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Attachment> attachments = (Collection<Attachment>) attObj;
            for (Attachment a : attachments) {
                System.out.println("Attachment ID: " + a.getId());
                try (InputStream attIs = a.getDataHandler().getInputStream()) {
                    System.out.println(IOUtils.readStringFromStream(attIs));
                } catch (IOException e) {
                    // swallow or log
                }
            }
        }

        // ---- Copy the message input stream so we can read it multiple times ----
        try {
            InputStream is = message.getContent(InputStream.class);
            if (is != null) {
                CachedOutputStream cos = new CachedOutputStream();
                IOUtils.copy(is, cos);
                cos.flush();

                // Put the stream back so CXF and later interceptors can still use it
                message.setContent(InputStream.class, cos.getInputStream());

                // Get message as String (once)
                String soapText = IOUtils.toString(cos.getInputStream(), StandardCharsets.UTF_8.name());
                System.out.println("Decrypted SOAP Response Message:\n" + soapText);
            } else {
                System.out.println("No InputStream found in the message content.");
            }
        } catch (Exception e) {
            throw new Fault(e);
        }

        // ---- Log message properties ----
        StringBuilder messageInfo = new StringBuilder("\nMessage Properties:\n");
        for (Map.Entry<String, Object> entry : message.entrySet()) {
            messageInfo.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        System.out.println(messageInfo.toString());

        // ---- SOAPMessage logging ----
        try {
            SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
            if (soapMessage != null) {
                StringWriter sw = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(soapMessage.getSOAPPart()), new StreamResult(sw));
                System.out.println("Decrypted SOAP Response XML:\n" + sw.toString());
            } else {
                System.out.println("No SOAP Message found in the response.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
