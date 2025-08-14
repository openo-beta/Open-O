package org.oscarehr.integration.ebs.client.ng;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.logging.log4j.Logger;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.oscarehr.util.MiscUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Custom interceptor that dynamically configures WSS4J based on message content
 */
public class DynamicWSS4JInInterceptor extends AbstractPhaseInterceptor<Message> {
    
    private final EdtClientBuilder clientBuilder;
    private static final Logger logger = MiscUtils.getLogger();
    
    public DynamicWSS4JInInterceptor(EdtClientBuilder clientBuilder) {
        super(Phase.RECEIVE);
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void handleMessage(Message message) {
        try {
            boolean hasEncryptedContent = hasEncryptedContent(message);
            
            Map<String, Object> wssProps = clientBuilder.newWSSInInterceptorConfiguration();
            if (!hasEncryptedContent) {
                // Use limited configuration without decryption
                // Only timestamp and signature verification - no decryption (updating WSHandlerConstants.ACTION prop)
                wssProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.TIMESTAMP + " " + WSHandlerConstants.SIGNATURE);
            }
            
            // Create and invoke WSS4J interceptor with appropriate configuration
            WSS4JInInterceptor wssInterceptor = new WSS4JInInterceptor(wssProps);
            message.getInterceptorChain().add(wssInterceptor);
            
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    private boolean hasEncryptedContent(Message message) {
        boolean hasEncrypted = false;
        try {
            InputStream is = message.getContent(InputStream.class);
            if (is == null) {
                return hasEncrypted;
            }

            // Copy stream to buffer so CXF can still read it later
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            String xml = bos.toString("UTF-8");

            // Reset stream so CXF can process the message normally
            message.setContent(InputStream.class, 
                new java.io.ByteArrayInputStream(bos.toByteArray()));

            return xml.contains("<wsse:EncryptedData") || xml.contains("<xenc:EncryptedData");
        } catch (Exception e) {
            logger.error("Error reading message content", e);
        }
        return hasEncrypted;
    }
}
