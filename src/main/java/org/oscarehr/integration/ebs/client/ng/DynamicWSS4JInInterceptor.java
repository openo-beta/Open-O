package org.oscarehr.integration.ebs.client.ng;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom interceptor that dynamically configures WSS4J based on message content
 */
public class DynamicWSS4JInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {
    
    private final EdtClientBuilder clientBuilder;
    
    public DynamicWSS4JInInterceptor(EdtClientBuilder clientBuilder) {
        super(Phase.PRE_PROTOCOL);
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            Document doc = message.getContent(Document.class);
            boolean hasEncryptedContent = hasEncryptedContent(doc);
            
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

    private boolean hasEncryptedContent(Document doc) {
        if (doc == null) return false;
        
        // Check for any encryption-related elements
        NodeList encryptedData = doc.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
        NodeList encryptedKey = doc.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedKey");
        
        return encryptedData.getLength() > 0 || encryptedKey.getLength() > 0;
    }
}
