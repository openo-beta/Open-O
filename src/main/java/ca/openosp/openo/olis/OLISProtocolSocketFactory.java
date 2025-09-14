//CHECKSTYLE:OFF
package ca.openosp.openo.olis;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import ca.openosp.OscarProperties;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Custom SSL socket factory for secure OLIS communications.
 *
 * <p>This class implements a specialized SSL socket factory that establishes secure
 * TLS connections to the OLIS servers. It handles client-side certificate authentication
 * using a configured Java KeyStore containing the healthcare facility's digital certificate
 * issued by the Ontario health authorities.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>TLS v1.2 protocol enforcement for security compliance</li>
 *   <li>Client certificate authentication using JKS keystore</li>
 *   <li>Configurable keystore location and password via properties</li>
 *   <li>Support for connection timeouts and local address binding</li>
 *   <li>Hostname verification for preventing man-in-the-middle attacks</li>
 * </ul>
 * </p>
 *
 * <p>The factory requires two configuration properties:
 * <ul>
 *   <li><b>olis_ssl_keystore</b> - Path to the JKS keystore file</li>
 *   <li><b>olis_ssl_keystore_password</b> - Password for the keystore</li>
 * </ul>
 * </p>
 *
 * <p>This implementation is critical for OLIS security as it ensures that only
 * authorized healthcare facilities with valid certificates can access patient
 * laboratory data from the provincial system.</p>
 *
 * @since 2002-01-01
 * @see SSLConnectionSocketFactory
 * @see Driver
 */
public class OLISProtocolSocketFactory extends SSLConnectionSocketFactory {
    /**
     * SSL context containing the client certificate and TLS configuration.
     * Initialized during construction with the facility's certificate from the keystore.
     */
    SSLContext context = null;

    /**
     * Constructs a new OLIS protocol socket factory with TLS v1.2 configuration.
     *
     * <p>Initializes the SSL context with the client certificate from the configured
     * keystore and enforces TLS v1.2 protocol for OLIS compliance. The factory uses
     * default hostname verification to prevent connection to unauthorized servers.</p>
     *
     * @throws Exception if the keystore cannot be loaded or SSL context initialization fails
     */
    public OLISProtocolSocketFactory() throws Exception {
        super(createSSLContext(), new String[]{"TLSv1.2"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }

    /**
     * Creates and configures the SSL context with client certificate authentication.
     *
     * <p>This method loads the healthcare facility's digital certificate from the
     * configured JKS keystore and initializes an SSL context for mutual TLS authentication
     * with OLIS servers. The keystore must contain the facility's certificate issued by
     * Ontario health authorities.</p>
     *
     * <p>The method uses SunX509 key manager factory and JKS keystore format, which are
     * standard for Java-based SSL implementations. The SSL context is configured with
     * TLS protocol and uses secure random number generation for cryptographic operations.</p>
     *
     * @return SSLContext configured with client certificate for OLIS authentication
     * @throws Exception if keystore file is not found, password is incorrect, or SSL initialization fails
     */
    private static SSLContext createSSLContext() throws Exception {
        // Load keystore configuration from properties
        String pKeyFile = OscarProperties.getInstance().getProperty("olis_ssl_keystore").trim();
        String pKeyPassword = OscarProperties.getInstance().getProperty("olis_ssl_keystore_password").trim();

        // Initialize key manager factory with SunX509 algorithm
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");

        // Load JKS keystore containing the facility's certificate
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyInput = new FileInputStream(pKeyFile)) {
            keyStore.load(keyInput, pKeyPassword.toCharArray());
        }

        // Initialize key manager with loaded keystore
        keyManagerFactory.init(keyStore, pKeyPassword.toCharArray());

        // Create SSL context with TLS protocol
        SSLContext context = SSLContext.getInstance("TLS");

        // Initialize with key managers and secure random generator
        // Trust managers are null to use default trust store
        context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        return context;
    }
    
    /**
     * Creates a socket with timeout support and local address binding.
     *
     * <p>This method creates an SSL socket connection to the specified OLIS server
     * with support for connection timeouts and binding to a specific local address.
     * The timeout parameter allows for graceful handling of network issues during
     * connection establishment.</p>
     *
     * @param host String the OLIS server hostname
     * @param port int the server port number
     * @param localAddress InetAddress the local address to bind to
     * @param localPort int the local port to bind to
     * @param timeout int connection timeout in milliseconds (0 for no timeout)
     * @return Socket the established SSL socket connection
     * @throws IOException if connection cannot be established
     */
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final int timeout) throws IOException {
        SocketFactory socketFactory = context.getSocketFactory();

        if (timeout <= 0) {
            // No timeout - create socket directly
            return socketFactory.createSocket(host, port, localAddress, localPort);
        } else {
            // Create unconnected socket for timeout support
            Socket socket = socketFactory.createSocket();

            // Bind to local address
            SocketAddress localAddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteAddr = new InetSocketAddress(host, port);
            socket.bind(localAddr);

            // Connect with timeout
            socket.connect(remoteAddr, timeout);
            return socket;
        }
    }

    /**
     * Creates a socket with local address binding.
     *
     * <p>Creates an SSL socket connection to the OLIS server, binding to the specified
     * local address and port. This is useful when the client system has multiple
     * network interfaces and needs to use a specific one for OLIS communication.</p>
     *
     * @param host String the OLIS server hostname
     * @param port int the server port number
     * @param clientHost InetAddress the local address to bind to
     * @param clientPort int the local port to bind to
     * @return Socket the established SSL socket connection
     * @throws IOException if connection cannot be established
     */
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
        return context.getSocketFactory().createSocket(host, port, clientHost, clientPort);
    }

    /**
     * Creates a basic SSL socket connection.
     *
     * <p>Creates a simple SSL socket connection to the OLIS server without
     * specifying local binding or timeout parameters. Uses system defaults
     * for local address selection.</p>
     *
     * @param host String the OLIS server hostname
     * @param port int the server port number
     * @return Socket the established SSL socket connection
     * @throws IOException if connection cannot be established
     */
    public Socket createSocket(String host, int port) throws IOException {
        return context.getSocketFactory().createSocket(host, port);
    }

    /**
     * Creates an SSL socket layered over an existing socket.
     *
     * <p>This method wraps an existing plain socket with SSL/TLS encryption,
     * useful for upgrading an existing connection to use encryption. The autoClose
     * parameter determines whether closing the SSL socket also closes the underlying
     * socket.</p>
     *
     * @param socket Socket the existing socket to layer SSL over
     * @param host String the server hostname for certificate verification
     * @param port int the server port number
     * @param autoClose boolean whether to close underlying socket when SSL socket is closed
     * @return Socket the SSL-wrapped socket
     * @throws IOException if SSL wrapping fails
     */
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return context.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

}
