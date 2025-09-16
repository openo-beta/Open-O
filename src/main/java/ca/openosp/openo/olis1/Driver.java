//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import ca.openosp.openo.messenger.data.MsgMessageData;

import org.apache.logging.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import ca.openosp.openo.commn.dao.OLISQueryLogDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.model.OLISQueryLog;
import ca.openosp.openo.commn.model.OscarLog;
import ca.openosp.openo.commn.model.OscarMsgType;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.olis.OLISProtocolSocketFactory;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.olis1.queries.Query;

import ca.ssha._2005.hial.ArrayOfError;
import ca.ssha._2005.hial.ArrayOfString;
import ca.ssha._2005.hial.Response;
import ca.ssha.www._2005.hial.OLISStub;
import ca.ssha.www._2005.hial.OLISStub.HIALRequest;
import ca.ssha.www._2005.hial.OLISStub.HIALRequestSignedRequest;
import ca.ssha.www._2005.hial.OLISStub.OLISRequest;
import ca.ssha.www._2005.hial.OLISStub.OLISRequestResponse;
import ca.openosp.OscarProperties;
import ca.openosp.openo.messenger.data.MsgProviderData;

/**
 * Core driver class for Ontario Laboratories Information System (OLIS) integration.
 *
 * This class handles the primary communication with the OLIS web service, including:
 * - Building and signing HL7 query messages
 * - Submitting queries to the OLIS service endpoint
 * - Verifying and parsing signed responses
 * - Managing security certificates and keystores
 * - Handling error notifications and logging
 *
 * The OLIS system provides laboratory test results from participating Ontario laboratories
 * to authorized healthcare providers. This integration uses HL7 v2.5 messages wrapped in
 * XML and secured with CMS/PKCS#7 digital signatures.
 *
 * Security considerations:
 * - All communications are encrypted using SSL/TLS
 * - Messages are digitally signed using provider certificates
 * - Responses are verified using OLIS certificates
 * - PHI data is protected throughout the process
 *
 * @since 2008-01-01
 */
public class Driver {

    /**
     * Data access object for general OSCAR system logging.
     * Used to track all OLIS query operations for audit purposes.
     */
    private static OscarLogDao logDao = (OscarLogDao) SpringUtils.getBean(OscarLogDao.class);

    /**
     * Data access object for OLIS-specific query logging.
     * Maintains detailed records of all OLIS queries including provider, patient, and query type.
     */
    private static OLISQueryLogDao olisQueryLogDao = SpringUtils.getBean(OLISQueryLogDao.class);

    /**
     * Logger instance for diagnostic and error logging.
     * Uses Log4j2 for consistent application-wide logging.
     */
    private static final Logger logger = MiscUtils.getLogger();


    /**
     * Submits an OLIS query to retrieve laboratory results.
     *
     * This method performs the complete query lifecycle:
     * 1. Configures SSL/TLS truststore for secure communication
     * 2. Creates and signs the HL7 query message
     * 3. Submits to OLIS web service (or simulation if configured)
     * 4. Verifies and processes the signed response
     * 5. Logs all operations for audit compliance
     *
     * The method supports both production and simulation modes. In simulation mode,
     * responses are retrieved from session storage rather than the live service.
     *
     * @param loggedInInfo LoggedInInfo containing the authenticated provider information
     * @param request HttpServletRequest the current HTTP request (may be null for auto-polling)
     * @param query Query the OLIS query object containing search parameters
     * @return String the unsigned XML response from OLIS, or empty string on error
     * @throws SecurityException if SSL/TLS configuration fails
     * @since 2008-01-01
     */
    public static String submitOLISQuery(LoggedInInfo loggedInInfo, HttpServletRequest request, Query query) {

        try {
            query.setQueryExecutionDate(new Date());
            query.setInitiatingProviderNo(loggedInInfo.getLoggedInProviderNo());

            // Create the OLIS message with provider context and query parameters
            OLISMessage message = new OLISMessage(loggedInInfo.getLoggedInProvider(), query);

            // Configure SSL truststore for OLIS certificate validation
            if (OscarProperties.getInstance().getProperty("olis_truststore") != null) {
                System.setProperty("javax.net.ssl.trustStore", OscarProperties.getInstance().getProperty("olis_truststore").trim());
            } else {
                MiscUtils.getLogger().warn("OLIS requires a truststore to be setup. check olis_truststore property");
            }

            // Set truststore password for SSL certificate access
            if (OscarProperties.getInstance().getProperty("olis_truststore_password") != null) {
                System.setProperty("javax.net.ssl.trustStorePassword", OscarProperties.getInstance().getProperty("olis_truststore_password").trim());
            } else {
                MiscUtils.getLogger().warn("OLIS requires a truststore to be setup. check olis_truststore_password property");
            }

            OLISRequest olisRequest = new OLISRequest();
            olisRequest.setHIALRequest(new HIALRequest());
            String olisRequestURL = OscarProperties.getInstance().getProperty("olis_request_url", "https://olis.ssha.ca/ssha.olis.webservices.ER7/OLIS.asmx");
            OLISStub olis = new OLISStub(olisRequestURL);

            olisRequest.getHIALRequest().setClientTransactionID(message.getTransactionId());
            olisRequest.getHIALRequest().setSignedRequest(new HIALRequestSignedRequest());

            // Convert HL7 message to XML format required by OLIS
            // Replace newlines with carriage returns as per HL7 specification
            String olisHL7String = message.getOlisHL7String().replaceAll("\n", "\r");
            String msgInXML = String.format("<Request xmlns=\"http://www.ssha.ca/2005/HIAL\"><Content><![CDATA[%s]]></Content></Request>", olisHL7String);

            // Sign the request using appropriate certificate configuration
            // signData2 uses separate JKS keystore and returned certificate
            // signData uses combined PKCS12 keystore
            String signedRequest = null;

            if (OscarProperties.getInstance().getProperty("olis_returned_cert") != null) {
                signedRequest = Driver.signData2(msgInXML);
            } else {
                signedRequest = Driver.signData(msgInXML);
            }

            olisRequest.getHIALRequest().getSignedRequest().setSignedData(signedRequest);

            try {
                OscarLog logItem = new OscarLog();
                logItem.setAction("OLIS");
                logItem.setContent("query");
                logItem.setData(olisHL7String);

                logItem.setProviderNo(loggedInInfo.getLoggedInProviderNo());

                logDao.persist(logItem);

                OLISQueryLog olisQueryLog = new OLISQueryLog();
                olisQueryLog.setInitiatingProviderNo(loggedInInfo.getLoggedInProviderNo());
                olisQueryLog.setQueryExecutionDate(new Date());
                olisQueryLog.setQueryType(query.getQueryType().toString());
                olisQueryLog.setUuid(query.getUuid());
                olisQueryLog.setDemographicNo(query.getDemographicNo() != null ? Integer.parseInt(query.getDemographicNo()) : null);
                olisQueryLog.setRequestingHIC(query.getRequestingHICProviderNo());

                olisQueryLogDao.persist(olisQueryLog);

            } catch (Exception e) {
                MiscUtils.getLogger().error("Couldn't write log message for OLIS query", e);
            }

            // Handle simulation mode for testing without actual OLIS connectivity
            if (OscarProperties.getInstance().getProperty("olis_simulate", "no").equals("yes")) {
                if (request != null) {
                    String response = (String) request.getSession().getAttribute("olisResponseContent");
                    request.setAttribute("olisResponseContent", response);
                    request.getSession().setAttribute("olisResponseContent", response);
                    request.getSession().setAttribute("olisResponseQuery", query);
                    return response;
                }
                // Return empty string for auto-polling in simulation mode
                return "";
            } else {
                OLISRequestResponse olisResponse = olis.oLISRequest(olisRequest);

                String signedData = olisResponse.getHIALResponse().getSignedResponse().getSignedData();
                String unsignedData = Driver.unsignData(signedData);

                if (request != null) {
                    // Store request/response details for debugging display in checkOlis.jsp
                    request.setAttribute("msgInXML", msgInXML);
                    request.setAttribute("signedRequest", signedRequest);
                    request.setAttribute("signedData", signedData);
                    request.setAttribute("unsignedResponse", unsignedData);
                }

                // Write response to temp file for debugging purposes
                writeToFile(unsignedData);
                readResponseFromXML(loggedInInfo, request, unsignedData);

                return unsignedData;

            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Can't perform OLIS query due to exception.", e);
            if (request != null) {
                request.setAttribute("searchException", e);
            }

            notifyOlisError(loggedInInfo.getLoggedInProvider(), e.getMessage());
            return "";
        }
    }

    /**
     * Parses and processes the XML response from OLIS.
     *
     * This method performs the following:
     * 1. Preprocesses the XML to fix namespace issues
     * 2. Configures XML parser with XXE (XML External Entity) attack prevention
     * 3. Unmarshals the response using JAXB
     * 4. Extracts errors or content from the response
     * 5. Sets appropriate request attributes for JSP display
     *
     * Security measures include:
     * - Disabling external entities to prevent XXE attacks
     * - Disabling DTD processing
     * - Using secure XML factory configurations
     *
     * @param loggedInInfo LoggedInInfo containing the authenticated provider information
     * @param request HttpServletRequest to store response attributes for display (may be null)
     * @param olisResponse String the raw XML response from OLIS
     * @throws SecurityException if XML parser cannot be securely configured
     * @since 2008-01-01
     */
    public static void readResponseFromXML(LoggedInInfo loggedInInfo, HttpServletRequest request, String olisResponse) {

        // Fix namespace issues in OLIS response XML
        olisResponse = olisResponse.replaceAll("<Content", "<Content xmlns=\"\" ");
        olisResponse = olisResponse.replaceAll("<Errors", "<Errors xmlns=\"\" ");

        try {
            // Create DocumentBuilderFactory with XXE prevention
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            try {
                // Disable external entities
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                dbf.setXIncludeAware(false);
                dbf.setExpandEntityReferences(false);

            } catch (ParserConfigurationException e) {
                // FAIL SECURELY - don't process XML if we can't secure it
                logger.error("Failed to configure XXE prevention: {}", e.getMessage());
                throw new SecurityException("Cannot securely configure XML parser", e);
            }

            dbf.newDocumentBuilder();
            
            // Create SchemaFactory with XXE prevention
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Source schemaFile = new StreamSource(new File(OscarProperties.getInstance().getProperty("olis_response_schema")));
            factory.newSchema(schemaFile);

            JAXBContext jc = JAXBContext.newInstance("ca.ssha._2005.hial");
            Unmarshaller u = jc.createUnmarshaller();
            
            // Create secure XMLInputFactory for JAXB unmarshalling
            XMLInputFactory xif = XMLInputFactory.newInstance();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(olisResponse));
            
            @SuppressWarnings("unchecked")
            Response root = ((JAXBElement<Response>) u.unmarshal(xsr)).getValue();

            if (root.getErrors() != null) {
                List<String> errorStringList = new LinkedList<String>();

                // Process all error messages from OLIS response
                ArrayOfError errors = root.getErrors();
                List<ca.ssha._2005.hial.Error> errorList = errors.getError();

                for (ca.ssha._2005.hial.Error error : errorList) {
                    String errorString = "";
                    errorString += "ERROR " + error.getNumber() + " (" + error.getSeverity() + ") : " + error.getMessage();
                    MiscUtils.getLogger().debug(errorString);

                    ArrayOfString details = error.getDetails();
                    if (details != null) {
                        List<String> detailList = details.getString();
                        for (String detail : detailList) {
                            errorString += "\n" + detail;
                        }
                    }

                    errorStringList.add(errorString);
                }
                if (request != null) request.setAttribute("errors", errorStringList);
            } else if (root.getContent() != null) {
                if (request != null) request.setAttribute("olisResponseContent", root.getContent());
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Couldn't read XML from OLIS response.", e);
            notifyOlisError(loggedInInfo.getLoggedInProvider(), "Couldn't read XML from OLIS response." + "\n" + e);
        }
    }

    /**
     * Verifies and extracts content from a CMS/PKCS#7 signed message.
     *
     * This method:
     * 1. Decodes the Base64-encoded signed data
     * 2. Verifies the digital signature using the embedded certificate
     * 3. Extracts and returns the original unsigned content
     *
     * The verification ensures that:
     * - The message has not been tampered with
     * - The signature was created by the claimed signer (OLIS)
     * - The certificate chain is valid
     *
     * @param data String Base64-encoded CMS signed data from OLIS
     * @return String the original unsigned content, or null if verification fails
     * @throws Exception if signature verification fails
     * @since 2008-01-01
     */
    public static String unsignData(String data) {

        byte[] dataBytes = Base64.decode(data);

        try {

            CMSSignedData s = new CMSSignedData(dataBytes);
            Store certs = s.getCertificates();
            SignerInformationStore signers = s.getSignerInfos();
            @SuppressWarnings("unchecked")
            Collection<SignerInformation> c = signers.getSigners();
            Iterator<SignerInformation> it = c.iterator();
            while (it.hasNext()) {
                X509CertificateHolder cert = null;
                SignerInformation signer = it.next();
                Collection certCollection = certs.getMatches(signer.getSID());
                @SuppressWarnings("unchecked")
                Iterator<X509CertificateHolder> certIt = certCollection.iterator();
                cert = certIt.next();

                if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert)))
                    throw new Exception("Doesn't verify");
            }

            CMSProcessableByteArray cpb = (CMSProcessableByteArray) s.getSignedContent();
            byte[] signedContent = (byte[]) cpb.getContent();
            String content = new String(signedContent);
            return content;
        } catch (Exception e) {
            MiscUtils.getLogger().error("error", e);
        }
        return null;

    }

    /**
     * Signs data using a JKS keystore and separate returned certificate.
     *
     * This method is the preferred signing approach when OLIS returns a certificate
     * after initial registration. It uses:
     * - JKS keystore for the private key
     * - Separate X.509 certificate file returned by OLIS
     * - SHA256withRSA signature algorithm (stronger than SHA1)
     *
     * The signed data conforms to CMS/PKCS#7 standards and includes:
     * - The original message content
     * - Digital signature
     * - Signer certificate
     *
     * Configuration requirements:
     * - olis_keystore: Path to JKS keystore file
     * - olis_ssl_keystore_password: Keystore password
     * - olis_returned_cert: Path to X.509 certificate from OLIS
     *
     * @param data String the message content to sign (XML-wrapped HL7)
     * @return String Base64-encoded CMS signed data, or null on error
     * @since 2011-01-01
     */
    public static String signData2(String data) {
        X509Certificate cert = null;
        PrivateKey priv = null;
        KeyStore keystore = null;
        String pwd = OscarProperties.getInstance().getProperty("olis_ssl_keystore_password", "changeit");
        String result = null;
        try {
            Security.addProvider(new BouncyCastleProvider());

            keystore = KeyStore.getInstance("JKS");
            // Load the JKS keystore containing the private key
            keystore.load(new FileInputStream(OscarProperties.getInstance().getProperty("olis_keystore")), pwd.toCharArray());

            // Find the key alias in the keystore (default: "olis1")
            String name = "olis1";
            Enumeration e = keystore.aliases();
            while (e.hasMoreElements()) {
                name = (String) e.nextElement();

            }

            // Extract the private key from the keystore
            priv = (PrivateKey) keystore.getKey(name, pwd.toCharArray());

            // Load the X.509 certificate returned by OLIS
            FileInputStream is = new FileInputStream(OscarProperties.getInstance().getProperty("olis_returned_cert"));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(is);

            // Create certificate store for CMS signature
            ArrayList<Certificate> certList = new ArrayList<Certificate>();
            certList.add(cert);

            Store certs = new JcaCertStore(certList);

            // Create CMS signed data generator
            CMSSignedDataGenerator sgen = new CMSSignedDataGenerator();

            // Configure SHA256withRSA signature algorithm (OLIS requirement)
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(priv);
            sgen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                    .build(sha1Signer, cert));

            // Add certificates to the signed message for verification
            sgen.addCertificates(certs);

            // Generate attached signature (content included in signed data)
            CMSSignedData csd = sgen.generate(new CMSProcessableByteArray(data.getBytes()), true);

            byte[] signedData = csd.getEncoded();
            byte[] signedDataB64 = Base64.encode(signedData);

            result = new String(signedDataB64);

        } catch (Exception e) {
            MiscUtils.getLogger().error("Can't sign HL7 message for OLIS", e);
        }
        return result;
    }

    /**
     * Signs data using a PKCS12 keystore containing both key and certificate.
     *
     * This is the legacy signing method used when the certificate is bundled
     * with the private key in a PKCS12 (.p12) file. Uses SHA1withRSA for
     * backward compatibility with older OLIS implementations.
     *
     * The method:
     * 1. Loads the PKCS12 keystore
     * 2. Extracts the private key and certificate
     * 3. Creates a CMS signed message
     * 4. Returns Base64-encoded result
     *
     * Configuration requirements:
     * - olis_keystore: Path to PKCS12 keystore file
     * - Password is hardcoded as "Olis2011" for legacy compatibility
     *
     * @param data String the message content to sign (XML-wrapped HL7)
     * @return String Base64-encoded CMS signed data, or null on error
     * @deprecated Use signData2() with separate certificate for better security
     * @since 2008-01-01
     */
    public static String signData(String data) {
        X509Certificate cert = null;
        PrivateKey priv = null;
        KeyStore keystore = null;
        String pwd = "Olis2011";
        String result = null;
        try {
            Security.addProvider(new BouncyCastleProvider());

            keystore = KeyStore.getInstance("PKCS12", "SunJSSE");
            // Load the PKCS12 keystore
            keystore.load(new FileInputStream(OscarProperties.getInstance().getProperty("olis_keystore")), pwd.toCharArray());

            Enumeration e = keystore.aliases();
            String name = "";

            if (e != null) {
                while (e.hasMoreElements()) {
                    String n = (String) e.nextElement();
                    if (keystore.isKeyEntry(n)) {
                        name = n;
                    }
                }
            }

            // Extract private key and certificate from PKCS12
            priv = (PrivateKey) keystore.getKey(name, pwd.toCharArray());
            cert = (X509Certificate) keystore.getCertificate(name);

            // Create certificate store for CMS signature
            ArrayList<Certificate> certList = new ArrayList<Certificate>();
            certList.add(cert);

            Store certs = new JcaCertStore(certList);

            // Create CMS signed data generator
            CMSSignedDataGenerator sgen = new CMSSignedDataGenerator();

            // Use SHA1withRSA for legacy OLIS compatibility
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(priv);
            sgen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                    .build(sha1Signer, cert));


            // Add certificates to the signed message for verification
            sgen.addCertificates(certs);

            // Generate attached signature (content included in signed data)
            CMSSignedData csd = sgen.generate(new CMSProcessableByteArray(data.getBytes()), true);

            byte[] signedData = csd.getEncoded();
            byte[] signedDataB64 = Base64.encode(signedData);

            result = new String(signedDataB64);

        } catch (Exception e) {
            MiscUtils.getLogger().warn("Can't sign HL7 message for OLIS. No valid keystore defined!");
        }
        return result;
    }

    /**
     * Sends error notifications to appropriate providers via internal messaging.
     *
     * This method creates system messages to notify providers when OLIS
     * queries fail. Notifications are sent to:
     * - The requesting provider (if available)
     * - System administrator (provider 999998)
     *
     * The notification includes:
     * - Timestamp of the failed attempt
     * - Detailed error message
     * - Context about the OLIS operation
     *
     * @param provider Provider the provider who initiated the query (may be null for auto-polling)
     * @param errorMsg String detailed error message to include in notification
     * @since 2008-01-01
     */
    private static void notifyOlisError(Provider provider, String errorMsg) {
        HashSet<String> sendToProviderList = new HashSet<String>();

        // Always notify system administrator (999998)
        String providerNoTemp = "999998";
        sendToProviderList.add(providerNoTemp);

        if (provider != null) {
            // Ensure admin is notified for manual queries
            sendToProviderList.add(providerNoTemp);

            providerNoTemp = provider.getProviderNo();
            sendToProviderList.add(providerNoTemp);
        }

        // Exit if no recipients configured
        if (sendToProviderList.size() == 0) return;

        String message = "OSCAR attempted to perform a fetch of OLIS data at " + new Date() + " but there was an error during the task.\n\nSee below for the error message:\n" + errorMsg;

        MsgMessageData messageData = new MsgMessageData();

        ArrayList<MsgProviderData> sendToProviderListData = new ArrayList<MsgProviderData>();
        for (String providerNo : sendToProviderList) {
            MsgProviderData mpd = new MsgProviderData();
            mpd.getId().setContactId(providerNo);
            mpd.getId().setClinicLocationNo(145);
            sendToProviderListData.add(mpd);
        }

        String sentToString = messageData.createSentToString(sendToProviderListData);
        messageData.sendMessage2(message, "OLIS Retrieval Error", "System", sentToString, "-1", sendToProviderListData, null, null, OscarMsgType.GENERAL_TYPE);
    }

    /**
     * Writes data to a temporary file for debugging purposes.
     *
     * Creates a uniquely named XML file in the system temp directory
     * containing the provided data. Useful for debugging OLIS responses
     * and troubleshooting integration issues.
     *
     * File naming uses random numbers to avoid collisions.
     * Files are not automatically cleaned up.
     *
     * @param data String the content to write to the temp file
     * @since 2008-01-01
     */
    static void writeToFile(String data) {
        try {
            File tempFile = new File(System.getProperty("java.io.tmpdir") + (Math.random() * 100) + ".xml");
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            pw.println(data);
            pw.flush();
            pw.close();
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
    }
}
