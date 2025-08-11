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
package org.oscarehr.integration.mchcv;

import ca.ontario.health.ebs.EbsFault;
import ca.ontario.health.edt.EDTDelegate;
import ca.ontario.health.hcv.Faultexception;
import ca.ontario.health.hcv.HCValidation;
import ca.ontario.health.hcv.HcvRequest;
import ca.ontario.health.hcv.HcvResults;
import ca.ontario.health.hcv.Requests;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.logging.log4j.Logger;
import org.oscarehr.integration.ebs.client.ng.EdtClientBuilder;
import org.oscarehr.integration.ebs.client.ng.EdtClientBuilderConfig;
import org.oscarehr.integration.ebs.client.ng.DownloadInInterceptor;
import org.oscarehr.util.MiscUtils;

import oscar.OscarProperties;

public class OnlineHCValidator implements HCValidator {
    private static Logger logger = MiscUtils.getLogger();

    private HCValidation validation;
    private EdtClientBuilder builder;

    public OnlineHCValidator() {
        OscarProperties properties = OscarProperties.getInstance();
        EdtClientBuilderConfig config = new EdtClientBuilderConfig();
        config.setLoggingRequired(!Boolean.valueOf(properties.getProperty("hcv.logging.skip")));
        config.setKeystoreUser(properties.getProperty("hcv.keystore.user"));
        config.setKeystorePassword(properties.getProperty("hcv.keystore.pass"));
        config.setUserNameTokenUser(properties.getProperty("hcv.service.user"));
        config.setUserNameTokenPassword(properties.getProperty("hcv.service.pass"));
        config.setServiceUrl(properties.getProperty("hcv.service.url"));
        config.setConformanceKey(properties.getProperty("hcv.service.conformanceKey"));
        config.setServiceId(properties.getProperty("hcv.service.id"));

        setBuilder(new EdtClientBuilder(config));
        setExternalClientKeystoreFilename(properties.getProperty("hcv.service.clientKeystore.properties"));
        validation = builder.build(HCValidation.class);

        //if (!config.isLoggingRequired()) { removeDownloadInInterceptor(validation); }

        // One consolidated log
        System.out.println("OnlineHCValidator initialized with: " +
            "loggingRequired=" + config.isLoggingRequired() +
            ", keystoreUser=" + config.getKeystoreUser() +
            ", userNameTokenUser=" + config.getUserNameTokenUser() +
            ", serviceUrl=" + config.getServiceUrl() +
            ", conformanceKey=" + config.getConformanceKey() +
            ", serviceId=" + config.getServiceId() +
            ", externalClientKeystore=" + properties.getProperty("hcv.service.clientKeystore.properties"));
    }

    @Override
    public HCValidationResult validate(String healthCardNumber, String versionCode) {
        return validate(healthCardNumber, versionCode, null);
    }

    @Override
    public HCValidationResult validate(String healthCardNumber, String versionCode, String serviceCode) {
        System.out.println("Starting validate() with HCN: " + healthCardNumber + ", Version: " + versionCode + ", ServiceCode: " + serviceCode);

        Requests requests = new Requests();
        HcvRequest request = new HcvRequest();
        request.setHealthNumber(healthCardNumber);
        request.setVersionCode(versionCode);

        if (serviceCode != null && !serviceCode.isEmpty()) {
            request.getFeeServiceCodes().add(serviceCode);
            System.out.println("Added service code to request: " + serviceCode);
        }

        requests.getHcvRequest().add(request);

        HcvResults results = null;
        HCValidationResult result = null;
        try {
            results = validate(requests, "en");
            if (results != null) {
                System.out.println("Validation response received, parsing result...");
                result = HCValidator.createSingleResult(results, 0);
            } else {
                System.out.println("Validation returned null results.");
            }
        } catch (Faultexception e) {
            System.out.println("Faultexception caught during validation.");
            System.out.println("Exception Message: " + e.getMessage());

            // Print the EbsFault details if available
            EbsFault fault = e.getFaultInfo();
            if (fault != null) {
                System.out.println("EBS Fault Code: " + fault.getCode());
                System.out.println("EBS Fault Message: " + fault.getMessage());
            } else {
                System.out.println("EBS Fault Info is null.");
            }

            result = new HCValidationResult();
            result.setEbsFault(e.getFaultInfo());
            result.setResponseCode(NOT_VALID_RESPONSE_CODE);
        }

        System.out.println("Returning validation result: " + (result != null ? result.getResponseCode() : "null"));
        return result;
    }

    @Override
    public HcvResults validate(Requests requests, String local) throws Faultexception {
        System.out.println("Sending validation request to external service...");
        HcvResults results;
        try {
            results = validation.validate(requests, local);
            System.out.println("Validation call succeeded.");
        } catch (SOAPFaultException sfx) {
            logger.error("SOAPFaultException: ", sfx);
            SOAPFault soapFault = sfx.getFault();
            EbsFault ebsFault = new EbsFault();
            ebsFault.setCode(soapFault.getFaultCode());
            ebsFault.setMessage(soapFault.getFaultString());
            throw new Faultexception("", ebsFault);
        }
        return results;
    }

    public EdtClientBuilder getBuilder() {
        return builder;
    }

    private void setBuilder(EdtClientBuilder builder) {
        this.builder = builder;
    }

    /*
     * Set an external `clientKeystore.properties` by providing the path to the file.
     * If the path is not provided, it will default to `src/main/resources/clientKeystore.properties`.
     */
    private static void setExternalClientKeystoreFilename(String clientKeystorePropertiesPath) {
        System.out.println("=========================================================================================================================");
        if (clientKeystorePropertiesPath == null) {
            System.out.println("[WARN] No client keystore properties path provided. Skipping keystore setup.");
            return;
        }

        System.out.println("[INFO] Received client keystore properties path: " + clientKeystorePropertiesPath);

        Path signaturePropFile = Paths.get(clientKeystorePropertiesPath);
        System.out.println("[DEBUG] Resolved absolute path for client keystore: " + signaturePropFile.toAbsolutePath());

        if (Files.exists(signaturePropFile)) {
            File file = new File(clientKeystorePropertiesPath);
            try {
                String keystoreURL = file.toURI().toURL().toString();
                System.out.println("[INFO] Keystore properties file found. Setting client keystore filename to: " + keystoreURL);
                EdtClientBuilder.setClientKeystoreFilename(keystoreURL);

                // Load properties to get keystore details
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                }

                String keystorePath = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.file");
                String keystorePassword = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
                String keystoreType = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");

                if (keystorePath != null) {
                    System.out.println("[INFO] Loading keystore from: " + keystorePath + " (type= " + keystoreType + ")");
                    try (FileInputStream ksStream = new FileInputStream(keystorePath)) {
                        KeyStore ks = KeyStore.getInstance(keystoreType);
                        ks.load(ksStream, keystorePassword != null ? keystorePassword.toCharArray() : null);

                        Enumeration<String> aliases = ks.aliases();
                        while (aliases.hasMoreElements()) {
                            String alias = aliases.nextElement();
                            Certificate cert = ks.getCertificate(alias);
                            System.out.println("[CERT] Alias: " + alias);
                            if (cert != null) {
                                System.out.println(cert.toString());
                            } else {
                                System.out.println("  (no certificate found for alias)");
                            }
                        }
                    }
                } else {
                    System.out.println("[WARN] No 'org.apache.ws.security.crypto.merlin.keystore.file' found in properties.");
                }

            } catch (MalformedURLException e) {
                System.out.println("[ERROR] Malformed URL when converting client keystore path: " + clientKeystorePropertiesPath);
                e.printStackTrace(System.out);
            } catch (Exception e) {
                System.out.println("[ERROR] Failed to list certificates from keystore.");
                e.printStackTrace(System.out);
            }
        } else {
            System.out.println("[ERROR] Client keystore properties file not found at path: " + signaturePropFile.toAbsolutePath());
        }
        System.out.println("=========================================================================================================================");
    }


    /**
	 * Removes the DownloadInInterceptor from the in interceptors of the given proxy.
	 *
	 * Note: This prevents SOAP response logs. Comment this out if logs are needed for
	 * 		 conformance testing or debugging.
	 */
	public static void removeDownloadInInterceptor(HCValidation validation) {
		// Retrieve the client from the proxy
		Client client = ClientProxy.getClient(validation);

		// Retrieve the list of in interceptors
		List<Interceptor<? extends Message>> inInterceptors = client.getEndpoint().getInInterceptors();

		// Create a new list to hold interceptors without DownloadInInterceptor
		List<Interceptor<? extends Message>> newInterceptors = new ArrayList<Interceptor<? extends Message>>();

		// Iterate through the interceptors and exclude DownloadInInterceptor
		for (Interceptor<? extends Message> interceptor : inInterceptors) {
			if (!(interceptor instanceof DownloadInInterceptor)) {
				newInterceptors.add(interceptor);
			}
		}

		// Replace the old list with the new one
		client.getEndpoint().getInInterceptors().clear();
		client.getEndpoint().getInInterceptors().addAll(newInterceptors);
	}

}
