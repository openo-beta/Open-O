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

package ca.openosp.openo.utility;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.common.gzip.GZIPInInterceptor;
import org.apache.cxf.transport.common.gzip.GZIPOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.logging.log4j.Logger;

public class CxfClientUtils {
    private static Logger logger = MiscUtils.getLogger();
    private static long connectionTimeout = 1500L;
    private static long receiveTimeout = 4000L;
    private static boolean useGZip = true;
    private static int gZipThreshold = 0;

    public CxfClientUtils() {
    }

    public static void initSslFromConfig() {
    }

    private static void initialiseFromConfigXml() {
        try {
            connectionTimeout = Long.parseLong(ConfigXmlUtils.getPropertyString("misc", "ws_client_connection_timeout_ms"));
        } catch (Throwable var6) {
        }

        try {
            receiveTimeout = Long.parseLong(ConfigXmlUtils.getPropertyString("misc", "ws_client_receive_timeout_ms"));
        } catch (Throwable var5) {
        }

        try {
            String booleanString = ConfigXmlUtils.getPropertyString("misc", "ws_client_use_gzip");
            if (booleanString != null) {
                useGZip = Boolean.parseBoolean(booleanString);
            }
        } catch (Throwable var4) {
        }

        try {
            gZipThreshold = Integer.parseInt(ConfigXmlUtils.getPropertyString("misc", "ws_client_gzip_threshold_bytes"));
        } catch (Throwable var3) {
        }

        // Security fix: removed support for allow_all_ssl_certificates to prevent CWE-295 vulnerability
        boolean allowAllSsl = Boolean.parseBoolean(ConfigXmlUtils.getPropertyString("misc", "allow_all_ssl_certificates"));
        if (allowAllSsl) {
            logger.warn("SECURITY WARNING: allow_all_ssl_certificates is deprecated and ignored for security reasons. " +
                       "Configure proper trust stores for self-signed certificates instead.");
            // Do not call the insecure method anymore
        }

        logger.info("CxfClientUtils using : connectionTimeout=" + connectionTimeout + ", receiveTimeout=" + receiveTimeout + ", useGZip=" + useGZip + ", gZipThreshold=" + gZipThreshold);
    }

    public static void configureClientConnection(Object wsPort) {
        Client cxfClient = ClientProxy.getClient(wsPort);
        HTTPConduit httpConduit = (HTTPConduit) cxfClient.getConduit();
        configureSsl(httpConduit);
        configureTimeout(httpConduit);
        if (useGZip) {
            configureGzip(cxfClient);
        }

    }

    public static void configureGzip(Client cxfClient) {
        cxfClient.getInInterceptors().add(new GZIPInInterceptor());
        cxfClient.getOutInterceptors().add(new GZIPOutInterceptor(gZipThreshold));
    }

    public static void configureTimeout(HTTPConduit httpConduit) {
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnection(ConnectionType.KEEP_ALIVE);
        httpClientPolicy.setConnectionTimeout(connectionTimeout);
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setReceiveTimeout(receiveTimeout);
        httpConduit.setClient(httpClientPolicy);
    }

    public static void configureSsl(HTTPConduit httpConduit) {
        TLSClientParameters tslClientParameters = httpConduit.getTlsClientParameters();
        if (tslClientParameters == null) {
            tslClientParameters = new TLSClientParameters();
        }

        // Security fix: removed insecure TrustAllManager and properly configure TLS
        // Use default trust managers which validate certificates against the JVM trust store
        // Do not set custom trust managers - let CXF use the default secure ones
        
        // Security fix: enable CN check for hostname verification (was disabled before)
        tslClientParameters.setDisableCNCheck(false);
        
        // Security fix: use secure TLS protocol instead of obsolete SSLv3
        tslClientParameters.setSecureSocketProtocol("TLS");
        
        // Use default certificate validation
        httpConduit.setTlsClientParameters(tslClientParameters);
    }

    public static void addWSS4JAuthentication(Object user, String password, Object wsPort) {
        Client cxfClient = ClientProxy.getClient(wsPort);
        cxfClient.getOutInterceptors().add(new AuthenticationOutWSS4JInterceptor(user, password));
    }

    static {
        initialiseFromConfigXml();
    }
}
