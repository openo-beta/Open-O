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

package ca.openosp.openo.webserv;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.ServletInputStream;
import javax.servlet.ReadListener;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class WebServiceSessionInvalidatingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing
    }

    // Custom wrapper with cached body
    private static class CachedRequestWrapper extends HttpServletRequestWrapper {
        private byte[] cachedBody;
        private boolean bodyRead = false;

        public CachedRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            // Don't read the body in constructor - only when actually needed
        }

        private void cacheBodyIfNeeded() throws IOException {
            if (!bodyRead) {
                try (InputStream is = super.getInputStream()) {
                    cachedBody = is.readAllBytes();
                } catch (Exception e) {
                    cachedBody = new byte[0];
                }
                bodyRead = true;
            }
        }

        @Override
        public BufferedReader getReader() throws IOException {
            cacheBodyIfNeeded();
            String body = new String(cachedBody, StandardCharsets.UTF_8);
            return new BufferedReader(new StringReader(body));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            cacheBodyIfNeeded();
            
            return new ServletInputStream() {
                private ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
                
                @Override 
                public int read() throws IOException { 
                    return bais.read(); 
                }
                
                @Override 
                public int read(byte[] b) throws IOException {
                    return bais.read(b);
                }
                
                @Override 
                public int read(byte[] b, int off, int len) throws IOException {
                    return bais.read(b, off, len);
                }
                
                @Override 
                public boolean isFinished() { 
                    return bais.available() == 0; 
                }
                
                @Override 
                public boolean isReady() { 
                    return true; 
                }
                
                @Override 
                public void setReadListener(ReadListener listener) {
                    // For async processing - not typically needed for synchronous requests
                }
            };
        }

        public String getCachedBody() throws IOException {
            cacheBodyIfNeeded();
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }

    @Override
    public void doFilter(ServletRequest tmpRequest, ServletResponse tmpResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) tmpRequest;
        CachedRequestWrapper wrappedRequest = new CachedRequestWrapper(request);
        
        System.out.println("\n--------------------------------------------------- Incoming Request ---------------------------------------------------");
        System.out.println("Method: " + wrappedRequest.getMethod());
        System.out.println("Request URL: " + wrappedRequest.getRequestURL());
        System.out.println("Content-Type: " + wrappedRequest.getContentType());
        System.out.println("Content-Length: " + wrappedRequest.getContentLength());
        
        // Only log body for POST/PUT requests and only if there's content
        if (("POST".equalsIgnoreCase(wrappedRequest.getMethod()) ||
             "PUT".equalsIgnoreCase(wrappedRequest.getMethod())) &&
            wrappedRequest.getContentLength() > 0) {
            
            try {
                String body = wrappedRequest.getCachedBody();
                if (body != null && !body.trim().isEmpty()) {
                    System.out.println("Request Body:\n" + body);
                }
            } catch (Exception e) {
                System.out.println("Could not read request body: " + e.getMessage());
            }
        }

        try {
            chain.doFilter(wrappedRequest, tmpResponse);
        } finally {
            System.out.println("------------------------------------------------- End of Request -------------------------------------------------\n");
        }
    }

    @Override
    public void destroy() {
        // nothing
    }
}
