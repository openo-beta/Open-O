//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package ca.openosp.openo.PMmodule.caisi_integrator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.WSS4JConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * WS-Security interceptor for CAISI Integrator web service authentication.
 *
 * <p>This interceptor extends WSS4JOutInterceptor to provide specialized authentication
 * for CAISI (Client Access to Integrated Services Information) integrator web services.
 * It adds both username/password authentication via WS-Security standards and custom
 * SOAP headers containing the requesting provider information for healthcare provider
 * context tracking and audit compliance.</p>
 *
 * <p>The interceptor ensures that all healthcare data access through the integrator
 * is properly authenticated and includes the requesting provider's identifier for
 * audit trails and privacy compliance (HIPAA/PIPEDA requirements).</p>
 *
 * @see WSS4JOutInterceptor
 * @see CallbackHandler
 * @see CaisiIntegratorManager
 * @since February 12, 2009
 */
public class AuthenticationOutWSS4JInterceptorForIntegrator extends WSS4JOutInterceptor implements CallbackHandler {
    private static final String REQUESTING_CAISI_PROVIDER_NO_KEY = "requestingCaisiProviderNo";
    private static QName REQUESTING_CAISI_PROVIDER_NO_QNAME = new QName("http://oscarehr.org/caisi", REQUESTING_CAISI_PROVIDER_NO_KEY, "caisi");

    private String password = null;
    private String oscarProviderNo = null;

    /**
     * Constructs the authentication interceptor with healthcare provider credentials.
     *
     * <p>Initializes WS-Security with username/password authentication and configures
     * the provider context for healthcare audit compliance. The provider number is
     * used to track which healthcare provider is making requests through the integrator.</p>
     *
     * @param user String the username for integrator authentication
     * @param password String the password for integrator authentication
     * @param oscarProviderNo String the healthcare provider number making the request
     * @since February 12, 2009
     */
    public AuthenticationOutWSS4JInterceptorForIntegrator(String user, String password, String oscarProviderNo) {
        this.password = password;
        this.oscarProviderNo = oscarProviderNo;

        // Configure WS-Security properties for username token authentication
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        properties.put(WSHandlerConstants.USER, user);
        properties.put(WSHandlerConstants.PASSWORD_TYPE, WSS4JConstants.PW_TEXT);
        properties.put(WSHandlerConstants.PW_CALLBACK_REF, this);

        setProperties(properties);
    }

    /**
     * Handles password callback for WS-Security authentication.
     *
     * <p>Implementation of CallbackHandler interface that provides the password
     * for username token authentication when requested by the WS-Security framework.</p>
     *
     * @param callbacks Callback[] array of callbacks to handle
     * @throws IOException if I/O error occurs during callback handling
     * @throws UnsupportedCallbackException if callback type is not supported
     * @since February 12, 2009
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback wsPasswordCallback = (WSPasswordCallback) callback;
                wsPasswordCallback.setPassword(password);
            }
        }
    }

    /**
     * Handles outbound SOAP messages by adding provider context and security.
     *
     * <p>Intercepts outbound SOAP messages to add the requesting provider's identifier
     * as a custom SOAP header before applying WS-Security processing. This ensures
     * healthcare provider context is available for audit and authorization purposes.</p>
     *
     * @param message SoapMessage the outbound SOAP message to process
     * @throws Fault if message processing fails
     * @since February 12, 2009
     */
    public void handleMessage(SoapMessage message) throws Fault {
        addRequestingCaisiProviderNo(message, oscarProviderNo);
        super.handleMessage(message);
    }

    /**
     * Adds the requesting provider number as a SOAP header for healthcare audit tracking.
     *
     * <p>Creates and adds a custom SOAP header containing the healthcare provider's
     * identifier to enable proper audit trails and privacy compliance tracking
     * throughout the CAISI integration system.</p>
     *
     * @param message SoapMessage the SOAP message to add the header to
     * @param providerNo String the healthcare provider number to include
     * @since February 12, 2009
     */
    private static void addRequestingCaisiProviderNo(SoapMessage message, String providerNo) {
        List<Header> headers = message.getHeaders();

        if (providerNo != null) {
            headers.add(createHeader(REQUESTING_CAISI_PROVIDER_NO_QNAME, REQUESTING_CAISI_PROVIDER_NO_KEY, providerNo));
        }
    }

    /**
     * Creates a SOAP header with the specified qualified name and value.
     *
     * <p>Utility method for creating properly formatted SOAP headers with
     * the CAISI namespace for healthcare provider context information.</p>
     *
     * @param qName QName the qualified name for the header element
     * @param key String the local name for the header element
     * @param value String the text content for the header element
     * @return Header the created SOAP header
     * @since February 12, 2009
     */
    private static Header createHeader(QName qName, String key, String value) {
        Document document = DOMUtils.createDocument();

        Element element = document.createElementNS("http://oscarehr.org/caisi", "caisi:" + key);
        element.setTextContent(value);

        SoapHeader header = new SoapHeader(qName, element);
        return (header);
    }
}
