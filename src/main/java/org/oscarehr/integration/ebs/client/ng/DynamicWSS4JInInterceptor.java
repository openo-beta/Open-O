package org.oscarehr.integration.ebs.client.ng;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.logging.log4j.Logger;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.oscarehr.util.MiscUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Custom interceptor that dynamically configures WSS4J based on message content
 */
public class DynamicWSS4JInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {
    
    private final EdtClientBuilder clientBuilder;
    private static final Logger logger = MiscUtils.getLogger();
    
    public DynamicWSS4JInInterceptor(EdtClientBuilder clientBuilder) {
        super(Phase.PRE_PROTOCOL);
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            boolean hasEncryptedContent = hasEncryptedContent(message);
            
            Map<String, Object> wssProps;
            if (hasEncryptedContent) {
                // Use full configuration with decryption
                wssProps = clientBuilder.newWSSInInterceptorConfiguration();
            } else {
                // Use limited configuration without decryption
                wssProps = createNonDecryptionConfiguration(clientBuilder);
            }
            
            // Create and invoke WSS4J interceptor with appropriate configuration
            WSS4JInInterceptor wssInterceptor = new WSS4JInInterceptor(wssProps);
            wssInterceptor.handleMessage(message);
            
        } catch (Exception e) {
            throw new Fault(e);
        }
    }
    
    private Map<String, Object> createNonDecryptionConfiguration(EdtClientBuilder clientBuilder) {
        Map<String, Object> props = clientBuilder.newWSSInInterceptorConfiguration();

        // Only timestamp and signature verification - no decryption (updating WSHandlerConstants.ACTION prop)
        props.put(WSHandlerConstants.ACTION, WSHandlerConstants.TIMESTAMP + " " + WSHandlerConstants.SIGNATURE);

        return props;
    }

    private boolean hasEncryptedContent(SoapMessage message) {
        String xmlContent = getRawMessageString(message);

        // Check for any encryption-related elements        
        return xmlContent.contains("EncryptedData") && xmlContent.contains("EncryptedKey");
    }

    private String getRawMessageString(SoapMessage message) {
        String xml = "";
        InputStream is = message.getContent(InputStream.class);
        if (is == null) {
            return xml;
        }

        try (InputStream input = is;
            ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096]; // larger buffer is usually more efficient
            int len;
            while ((len = input.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            xml = bos.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.error("Error reading message content", e);
        }

        return xml;
    }
}
