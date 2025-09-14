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


/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ca.openosp.openo.prescript.util;

import java.net.URL;

import org.apache.xmlrpc.AsyncCallback;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import ca.openosp.openo.utility.MiscUtils;


/**
 * Asynchronous XML-RPC callback implementation with configurable timeout support.
 * This class provides a mechanism to wait for XML-RPC responses with a maximum timeout
 * period, preventing indefinite blocking on slow or unresponsive web services.
 *
 * <p>The class implements the AsyncCallback interface and provides timeout functionality
 * for XML-RPC operations, particularly useful when integrating with external drug
 * reference services that may experience network latency or service interruptions.</p>
 *
 * <p>Typical usage pattern:</p>
 * <pre>
 * // Wait for 10 seconds maximum
 * TimingOutCallback callback = new TimingOutCallback(10 * 1000);
 * XmlRpcClient client = new XmlRpcClient(url);
 * client.executeAsync(methodName, parameters, callback);
 * try {
 *     Object result = callback.waitForResponse();
 *     // Process successful response
 * } catch (TimeoutException e) {
 *     // Handle timeout - service may be slow or unavailable
 * } catch (Exception e) {
 *     // Handle other service errors
 * }
 * </pre>
 *
 * <p>This implementation is thread-safe and uses Java's wait/notify mechanism
 * for efficient blocking and signaling between threads.</p>
 *
 * @since 2007-04-16
 */
public class TimingOutCallback implements AsyncCallback {
    /**
     * Exception thrown when an XML-RPC request exceeds the configured timeout period.
     * This exception extends XmlRpcException to maintain compatibility with existing
     * XML-RPC error handling patterns.
     */
    public static class TimeoutException extends XmlRpcException {
        private static final long serialVersionUID = 4875266372372105081L;

        /**
         * Creates a new TimeoutException with the specified error code and message.
         *
         * @param pCode int the error code (typically 0 for timeout)
         * @param message String descriptive message about the timeout
         */
        public TimeoutException(int pCode, String message) {
            super(pCode, message);
        }
    }

    /**
     * Maximum time to wait for a response, in milliseconds.
     */
    private final long timeout;

    /**
     * The result object returned by the XML-RPC call, if successful.
     * This field is public for direct access after waitForResponse() completes.
     */
    public Object result;

    /**
     * Any error that occurred during the XML-RPC call.
     */
    private Throwable error;

    /**
     * Flag indicating whether a response (success or error) has been received.
     */
    private boolean responseSeen;

    /**
     * Creates a new TimingOutCallback with the specified timeout period.
     *
     * @param pTimeout long maximum time to wait for response, in milliseconds
     */
    public TimingOutCallback(long pTimeout) {
        timeout = pTimeout;
    }

    /**
     * Waits for the XML-RPC response up to the configured timeout period.
     * This method blocks the calling thread until either a response is received
     * or the timeout period expires.
     *
     * @return Object the result returned by the XML-RPC call
     * @throws InterruptedException if the waiting thread is interrupted
     * @throws TimeoutException if no response is received within the timeout period
     * @throws Throwable if the XML-RPC call returned an error
     */
    public synchronized Object waitForResponse() throws Throwable {
        if (!responseSeen) {
            wait(timeout);
            if (!responseSeen) {
                throw new TimeoutException(0, "No response after waiting for " + timeout + " milliseconds.");
            }
        }
        if (error != null) {
            throw error;
        }
        return result;
    }

    /**
     * Handles error responses from XML-RPC calls.
     * This method is called automatically by the XML-RPC framework when an error occurs.
     *
     * @param pRequest XmlRpcRequest the original request that failed
     * @param pError Throwable the error that occurred during the call
     */
    public synchronized void handleError(XmlRpcRequest pRequest, Throwable pError) {
        responseSeen = true;
        error = pError;
        notify();
    }

    /**
     * Handles successful responses from XML-RPC calls.
     * This method is called automatically by the XML-RPC framework when a call succeeds.
     *
     * @param pRequest XmlRpcRequest the original request that succeeded
     * @param pResult Object the result returned by the XML-RPC call
     */
    public synchronized void handleResult(XmlRpcRequest pRequest, Object pResult) {
        responseSeen = true;
        result = pResult;
        notify();
    }

    /**
     * Alternative result handler for XML-RPC responses.
     * This method provides compatibility with different XML-RPC client implementations.
     *
     * @param arg0 Object the result returned by the XML-RPC call
     * @param arg1 URL the service endpoint URL
     * @param arg2 String additional response information
     */
    public synchronized void handleResult(Object arg0, URL arg1, String arg2) {
        responseSeen = true;
        MiscUtils.getLogger().debug("arg2" + arg2);
        result = arg0;
        this.notify();
    }

    /**
     * Alternative error handler for XML-RPC responses.
     * This method provides compatibility with different XML-RPC client implementations.
     *
     * @param arg0 Exception the error that occurred during the call
     * @param arg1 URL the service endpoint URL
     * @param arg2 String additional error information
     */
    public synchronized void handleError(Exception arg0, URL arg1, String arg2) {
        responseSeen = true;
        MiscUtils.getLogger().error("Error", arg0);
        error = arg0;
        this.notify();
    }
}
