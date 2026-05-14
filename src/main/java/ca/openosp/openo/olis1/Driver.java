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
import ca.openosp.openo.lab.ca.all.parsers.Factory;
import ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler;
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

public class Driver {

    private static OscarLogDao logDao = (OscarLogDao) SpringUtils.getBean(OscarLogDao.class);
    //	private static OLISResultsDao olisResultsDao = SpringUtils.getBean(OLISResultsDao.class);
    private static OLISQueryLogDao olisQueryLogDao = SpringUtils.getBean(OLISQueryLogDao.class);

    private static final Logger logger = MiscUtils.getLogger();


    public static String submitOLISQuery(LoggedInInfo loggedInInfo, HttpServletRequest request, Query query) {

        try {
            query.setQueryExecutionDate(new Date());
            query.setInitiatingProviderNo(loggedInInfo.getLoggedInProviderNo());

            OLISMessage message = new OLISMessage(loggedInInfo.getLoggedInProvider(), query);

            if (OscarProperties.getInstance().getProperty("olis_truststore") != null) {
                System.setProperty("javax.net.ssl.trustStore", OscarProperties.getInstance().getProperty("olis_truststore").trim());
            } else {
                MiscUtils.getLogger().warn("OLIS requires a truststore to be setup. check olis_truststore property");
            }

            if (OscarProperties.getInstance().getProperty("olis_truststore_password") != null) {
                System.setProperty("javax.net.ssl.trustStorePassword", OscarProperties.getInstance().getProperty("olis_truststore_password").trim());
            } else {
                MiscUtils.getLogger().warn("OLIS requires a truststore to be setup. check olis_truststore_password property");
            }

            OLISRequest olisRequest = new OLISRequest();
            olisRequest.setHIALRequest(new HIALRequest());

            olisRequest.getHIALRequest().setClientTransactionID(message.getTransactionId());
            olisRequest.getHIALRequest().setSignedRequest(new HIALRequestSignedRequest());

            String olisHL7String = message.getOlisHL7String().replaceAll("\n", "\r");
            String msgInXML = String.format("<Request xmlns=\"http://www.ssha.ca/2005/HIAL\"><Content><![CDATA[%s]]></Content></Request>", olisHL7String);

            String signedRequest = null;

            if (OscarProperties.getInstance().getProperty("olis_returned_cert") != null) {
                signedRequest = Driver.signData2(msgInXML);
            } else {
                signedRequest = Driver.signData(msgInXML);
            }

            olisRequest.getHIALRequest().getSignedRequest().setSignedData(signedRequest);

            // Hoisted out of the audit-log try block so the response handler below can
            // enrich this same SENT row with the OLIS Transaction ID once it arrives.
            OLISQueryLog olisQueryLog = null;

            try {
                OscarLog logItem = new OscarLog();
                logItem.setAction("OLIS");
                logItem.setContent("query");
                logItem.setData(olisHL7String);

                logItem.setProviderNo(loggedInInfo.getLoggedInProviderNo());

                logDao.persist(logItem);

                olisQueryLog = new OLISQueryLog();
                olisQueryLog.setInitiatingProviderNo(loggedInInfo.getLoggedInProviderNo());
                olisQueryLog.setQueryExecutionDate(new Date());
                olisQueryLog.setQueryType(query.getQueryType().toString());
                olisQueryLog.setUuid(query.getUuid());
                String demoNoStr = query.getDemographicNo();
                olisQueryLog.setDemographicNo((demoNoStr != null && !demoNoStr.trim().isEmpty()) ? Integer.parseInt(demoNoStr.trim()) : null);
                olisQueryLog.setRequestingHIC(query.getRequestingHICProviderNo());

                olisQueryLogDao.persist(olisQueryLog);

            } catch (Exception e) {
                MiscUtils.getLogger().error("Couldn't write log message for OLIS query", e);
            }

            if (OscarProperties.getInstance().getProperty("olis_simulate", "no").equals("yes")) {
                if (request != null) {
                    String response = (String) request.getSession().getAttribute("olisResponseContent");
                    request.setAttribute("olisResponseContent", response);
                    request.getSession().setAttribute("olisResponseContent", response);
                    request.getSession().setAttribute("olisResponseQuery", query);

                    String olisTransactionId = extractOlisTransactionId(response);
                    recordOlisTransactionId(olisQueryLog, olisTransactionId);
                    if (olisTransactionId != null) {
                        request.setAttribute("olisTransactionId", olisTransactionId);
                    }

                    return response;
                }
                //this only happens for auto-polling when simulate is enabled
                return "";
            } else {
                String olisRequestURL = OscarProperties.getInstance().getProperty("olis_request_url", "https://olis.ssha.ca/ssha.olis.webservices.ER7/OLIS.asmx");
                OLISStub olis = new OLISStub(olisRequestURL);

                OLISRequestResponse olisResponse = olis.oLISRequest(olisRequest);

                String signedData = olisResponse.getHIALResponse().getSignedResponse().getSignedData();
                String unsignedData = Driver.unsignData(signedData);

                if (request != null) {
                    //these seem to just be for the checkOlis.jsp
                    request.setAttribute("msgInXML", msgInXML);
                    request.setAttribute("signedRequest", signedRequest);
                    request.setAttribute("signedData", signedData);
                    request.setAttribute("unsignedResponse", unsignedData);
                }

                writeToFile(unsignedData);    //not sure the point of this, other than debugging maybe
                readResponseFromXML(loggedInInfo, request, unsignedData);

                // OLIS06.02 / OLIS03.06: capture the OLIS-assigned Transaction ID from the
                // response and write it back onto the SENT audit row (keyed by query uuid).
                String olisTransactionId = extractOlisTransactionId(unsignedData);
                recordOlisTransactionId(olisQueryLog, olisTransactionId);
                if (request != null && olisTransactionId != null) {
                    request.setAttribute("olisTransactionId", olisTransactionId);
                }

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

    public static void readResponseFromXML(LoggedInInfo loggedInInfo, HttpServletRequest request, String olisResponse) {

        olisResponse = olisResponse.replaceAll("<Content", "<Content xmlns=\"\" ");
        olisResponse = olisResponse.replaceAll("<Errors", "<Errors xmlns=\"\" ");

        try {
            // Create DocumentBuilderFactory with XXE prevention
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            try {
                // Disable external entities to prevent XXE attacks
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                
                // Disable XInclude
                dbf.setXIncludeAware(false);
                
                // Disabled expansion of entity references
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

            // Skip schema loading when olis_response_schema is unset — without this guard,
            // new File(null) NPEs and tanks the entire response-parse path.
            if (OscarProperties.getInstance().getProperty("olis_response_schema") != null) {
                Source schemaFile = new StreamSource(new File(OscarProperties.getInstance().getProperty("olis_response_schema")));
                factory.newSchema(schemaFile);
            }

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

                // Read all the errors
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
     * Extracts the OLIS Transaction ID from an OLIS query response. Per the OLIS
     * Interface Specification (§10.2.5.12.2.3) OLIS does not mint a distinct transaction
     * identifier — the response (ERP) echoes the initiating request's Message Control ID
     * in the MSA-2 field, which is the correlation value logged for the OLIS06.02 /
     * OLIS03.06 audit trail. The value is read via the standard {@link OLISHL7Handler}
     * parser. The {@code response} argument may be either the unsigned XML envelope
     * ({@code <Response><Content>...HL7...</Content></Response>}) or a raw HL7 string
     * (the simulator path); the HL7 payload is unwrapped from the envelope before it is
     * handed to the parser.
     * <p>
     * Returns {@code null} on any parse failure, or when the response carries no MSA
     * segment (e.g. a bare {@code ORU} rather than an {@code ERP}) — OLIS06.02 /
     * OLIS03.06 audit enrichment must never break the query path.
     *
     * @param response String the unsigned OLIS response (XML envelope or raw HL7)
     * @return String the OLIS Transaction ID (MSA-2), or {@code null} if it cannot be extracted
     */
    static String extractOlisTransactionId(String response) {
        if (response == null || response.trim().isEmpty()) {
            return null;
        }
        try {
            // Unwrap the HL7 payload from the <Content>...</Content> envelope element if present.
            String hl7 = response;
            int contentStart = response.indexOf("<Content");
            int contentEnd = response.indexOf("</Content>");
            if (contentStart >= 0 && contentEnd > contentStart) {
                int payloadStart = response.indexOf('>', contentStart) + 1;
                hl7 = response.substring(payloadStart, contentEnd);
            }
            hl7 = hl7.replace("<![CDATA[", "").replace("]]>", "").trim();
            if (hl7.isEmpty()) {
                return null;
            }

            // Parse the HL7 with the standard OLIS handler and read MSA-2, the Message
            // Control ID of the request that OLIS is acknowledging in this response.
            OLISHL7Handler handler = (OLISHL7Handler) Factory.getHandler("OLIS_HL7", hl7);
            if (handler != null) {
                String olisTransactionId = handler.getMsaControlId();
                if (olisTransactionId != null && !olisTransactionId.trim().isEmpty()) {
                    return olisTransactionId.trim();
                }
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Couldn't extract OLIS Transaction ID from response", e);
        }
        return null;
    }

    /**
     * Writes the OLIS Transaction ID back onto the previously-persisted {@link OLISQueryLog}
     * SENT row. No-op when the row was never persisted (audit-log write failed earlier) or
     * when no Transaction ID could be extracted. Failures are logged and swallowed so that
     * audit enrichment never breaks the query path.
     *
     * @param olisQueryLog       OLISQueryLog the SENT audit row persisted before submission
     * @param olisTransactionId  String the OLIS Transaction ID extracted from the response
     */
    private static void recordOlisTransactionId(OLISQueryLog olisQueryLog, String olisTransactionId) {
        if (olisQueryLog == null || olisQueryLog.getId() == null || olisTransactionId == null) {
            return;
        }
        try {
            olisQueryLog.setOlisTransactionId(olisTransactionId);
            olisQueryLogDao.merge(olisQueryLog);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Couldn't record OLIS Transaction ID on query log", e);
        }
    }

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

    //Method uses a jks and a returned cert separately instead of needing to
    //import the cert into PKCS12 file.
    public static String signData2(String data) {
        X509Certificate cert = null;
        PrivateKey priv = null;
        KeyStore keystore = null;
        String pwd = OscarProperties.getInstance().getProperty("olis_ssl_keystore_password", "changeit");
        String result = null;
        try {
            Security.addProvider(new BouncyCastleProvider());

            keystore = KeyStore.getInstance("JKS");
            // Load the keystore
            keystore.load(new FileInputStream(OscarProperties.getInstance().getProperty("olis_keystore")), pwd.toCharArray());

            //Enumeration e = keystore.aliases();
            String name = "olis1";
            Enumeration e = keystore.aliases();
            while (e.hasMoreElements()) {
                name = (String) e.nextElement();

            }

            // Get the private key and the certificate
            priv = (PrivateKey) keystore.getKey(name, pwd.toCharArray());

            FileInputStream is = new FileInputStream(OscarProperties.getInstance().getProperty("olis_returned_cert"));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(is);

            // I'm not sure if this is necessary

            ArrayList<Certificate> certList = new ArrayList<Certificate>();
            certList.add(cert);

            Store certs = new JcaCertStore(certList);

            // Encrypt data
            CMSSignedDataGenerator sgen = new CMSSignedDataGenerator();

            // What digest algorithm i must use? SHA1? MD5? RSA?...
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(priv);
            sgen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                    .build(sha1Signer, cert));

            // I'm not sure this is necessary
            sgen.addCertificates(certs);

            // I think that the 2nd parameter need to be false (detached form)
            CMSSignedData csd = sgen.generate(new CMSProcessableByteArray(data.getBytes()), true);

            byte[] signedData = csd.getEncoded();
            byte[] signedDataB64 = Base64.encode(signedData);

            result = new String(signedDataB64);

        } catch (Exception e) {
            MiscUtils.getLogger().error("Can't sign HL7 message for OLIS", e);
        }
        return result;
    }

    public static String signData(String data) {
        X509Certificate cert = null;
        PrivateKey priv = null;
        KeyStore keystore = null;
        String pwd = "Olis2011";
        String result = null;
        try {
            Security.addProvider(new BouncyCastleProvider());

            keystore = KeyStore.getInstance("PKCS12", "SunJSSE");
            // Load the keystore
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

            // Get the private key and the certificate
            priv = (PrivateKey) keystore.getKey(name, pwd.toCharArray());
            cert = (X509Certificate) keystore.getCertificate(name);

            // I'm not sure if this is necessary

            ArrayList<Certificate> certList = new ArrayList<Certificate>();
            certList.add(cert);

            Store certs = new JcaCertStore(certList);

            // Encrypt data
            CMSSignedDataGenerator sgen = new CMSSignedDataGenerator();

            // What digest algorithm i must use? SHA1? MD5? RSA?...
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(priv);
            sgen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                    .build(sha1Signer, cert));


            // I'm not sure this is necessary
            sgen.addCertificates(certs);

            // I think that the 2nd parameter need to be false (detached form)
            CMSSignedData csd = sgen.generate(new CMSProcessableByteArray(data.getBytes()), true);

            byte[] signedData = csd.getEncoded();
            byte[] signedDataB64 = Base64.encode(signedData);

            result = new String(signedDataB64);

        } catch (Exception e) {
            MiscUtils.getLogger().warn("Can't sign HL7 message for OLIS. No valid keystore defined!");
        }
        return result;
    }

    private static void notifyOlisError(Provider provider, String errorMsg) {
        HashSet<String> sendToProviderList = new HashSet<String>();

        String providerNoTemp = "999998";
        sendToProviderList.add(providerNoTemp);

        if (provider != null) {
            // manual prompts always send to admin
            sendToProviderList.add(providerNoTemp);

            providerNoTemp = provider.getProviderNo();
            sendToProviderList.add(providerNoTemp);
        }

        // no one wants to hear about the problem
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
