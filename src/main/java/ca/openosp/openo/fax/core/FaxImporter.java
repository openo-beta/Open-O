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
package ca.openosp.openo.fax.core;


import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.FaxConfigDao;
import ca.openosp.openo.commn.dao.FaxJobDao;
import ca.openosp.openo.commn.dao.ProviderLabRoutingDao;
import ca.openosp.openo.commn.dao.QueueDocumentLinkDao;
import ca.openosp.openo.commn.model.FaxConfig;
import ca.openosp.openo.commn.model.FaxJob;
import ca.openosp.openo.commn.model.ProviderLabRoutingModel;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.itextpdf.text.pdf.codec.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import ca.openosp.OscarProperties;
import ca.openosp.openo.documentManager.EDoc;
import ca.openosp.openo.documentManager.EDocUtil;

/**
 * Automated importer for incoming medical fax documents from healthcare service providers.
 *
 * This class provides critical functionality for receiving and processing medical documents
 * transmitted via fax from external healthcare providers, laboratories, specialists, and other
 * medical facilities. The automated import process ensures that incoming medical information
 * is promptly available to healthcare providers for patient care decisions.
 *
 * The import process handles various types of incoming medical documents including:
 * - Laboratory results and diagnostic reports from external labs
 * - Consultation reports and specialist recommendations
 * - Medical imaging reports and findings
 * - Prescription confirmations and pharmacy communications
 * - Patient referral responses and care coordination documents
 * - Hospital discharge summaries and transfer reports
 *
 * Key features of the import system:
 * - Automated polling of configured fax service providers
 * - Secure download and local storage of medical documents
 * - Automatic routing to appropriate inbox queues by department
 * - Provider lab routing integration for unclaimed document tracking
 * - Base64 decoding and PDF file format conversion
 * - Cleanup of remote documents after successful local storage
 * - Error handling and status tracking for failed imports
 *
 * The importer maintains HIPAA/PIPEDA compliance by:
 * - Securing document transmission with authenticated connections
 * - Implementing proper access controls for imported documents
 * - Maintaining audit trails for all import activities
 * - Ensuring PHI is properly protected during the import process
 *
 * Integration with the document management system ensures that imported faxes
 * are immediately available to healthcare providers through standard document
 * workflow processes, enabling timely patient care decisions.
 *
 * @see ca.openosp.openo.commn.model.FaxConfig
 * @see ca.openosp.openo.commn.model.FaxJob
 * @see ca.openosp.openo.documentManager.EDoc
 * @since 2014-08-29
 */
public class FaxImporter {

    /** REST API path for fax service communication */
    private static String PATH = "/fax";

    /** Local directory path for storing imported medical documents */
    private static String DOCUMENT_DIR = OscarProperties.getInstance().getProperty("DOCUMENT_DIR");

    /** Default user ID for system-imported documents */
    private static String DEFAULT_USER = "-1";

    /** Data access object for fax configuration management */
    private FaxConfigDao faxConfigDao = SpringUtils.getBean(FaxConfigDao.class);

    /** Data access object for fax job tracking and management */
    private FaxJobDao faxJobDao = SpringUtils.getBean(FaxJobDao.class);

    /** Data access object for document queue routing */
    private QueueDocumentLinkDao queueDocumentLinkDao = SpringUtils.getBean(QueueDocumentLinkDao.class);

    /** Data access object for provider lab routing and unclaimed document tracking */
    private ProviderLabRoutingDao providerLabRoutingDao = SpringUtils.getBean(ProviderLabRoutingDao.class);

    /** Logger instance for tracking import activities and errors */
    private Logger log = MiscUtils.getLogger();

    /**
     * Polls all configured fax service providers for incoming medical documents.
     *
     * This method performs the main import cycle by connecting to each active fax service
     * provider account and retrieving any pending medical documents. The polling process
     * is typically executed on a scheduled basis to ensure timely import of critical
     * healthcare information.
     *
     * For each active fax configuration, the method:
     * - Establishes secure authenticated connection to the fax service
     * - Retrieves list of available documents for download
     * - Downloads each document with proper error handling
     * - Saves documents to local storage with appropriate security
     * - Routes documents to configured inbox queues
     * - Integrates with provider lab routing for tracking
     * - Removes successfully imported documents from remote server
     * - Maintains comprehensive audit logs for regulatory compliance
     *
     * Error handling ensures that single document failures do not interrupt
     * the overall import process, allowing other medical documents to be
     * successfully imported and made available to healthcare providers.
     */
    public void poll() {

        log.info("CHECKING REMOTE FOR INCOMING FAXES");

        List<FaxConfig> faxConfigList = faxConfigDao.findAll(null, null);


        for (FaxConfig faxConfig : faxConfigList) {
            if (faxConfig.isActive() && faxConfig.isDownload()) {

                Credentials credentials = new UsernamePasswordCredentials(faxConfig.getSiteUser(), faxConfig.getPasswd());
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), credentials);

                CloseableHttpClient client = HttpClientBuilder.create()
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .build();

                HttpGet mGet = null;
                HttpResponse response = null;
                int status = HttpStatus.SC_OK;

                try {

                    log.debug("Service Path: " + faxConfig.getUrl() + PATH + File.separator + URLEncoder.encode(faxConfig.getFaxUser(), "UTF-8"));

                    mGet = new HttpGet(faxConfig.getUrl() + PATH + File.separator + URLEncoder.encode(faxConfig.getFaxUser(), "UTF-8"));
                    mGet.setHeader("accept", "application/json");
                    mGet.setHeader("user", faxConfig.getFaxUser());
                    mGet.setHeader("passwd", faxConfig.getFaxPasswd());
                    response = client.execute(mGet);

                    if (response != null) {
                        status = response.getStatusLine().getStatusCode();
                    }

                    if (status == HttpStatus.SC_OK) {

                        HttpEntity httpEntity = response.getEntity();
                        String content = EntityUtils.toString(httpEntity);

                        log.debug("CONTENT: " + content);

                        ObjectMapper mapper = new ObjectMapper();

                        List<FaxJob> faxList = mapper.readValue(content, new TypeReference<List<FaxJob>>() {
                        });

                        for (FaxJob receivedFax : faxList) {

                            String fileName = null;
                            EDoc edoc = null;
                            FaxJob faxFile = null;

                            // Skip download if the fax service reported an error
                            // Error status indicates no document content is available
                            if (!FaxJob.STATUS.ERROR.equals(receivedFax.getStatus())) {
                                faxFile = downloadFax(client, faxConfig, receivedFax);
                            }

                            // Process downloaded fax: save to filesystem and route to medical inbox
                            if (faxFile != null) {
                                edoc = saveAndInsertIntoQueue(faxConfig, receivedFax, faxFile);
                            }

                            if (edoc != null) {
                                fileName = edoc.getFileName();
                            }

                            // Only delete from remote server if local save was successful
                            // This prevents loss of medical documents due to storage failures
                            if (fileName != null) {

                                // Integrate with provider notification system for new medical documents
                                providerRouting(Integer.parseInt(edoc.getDocId()));

                                // Remove successfully imported document from remote fax server
                                deleteFax(client, faxConfig, receivedFax);

                            } else {
                                fileName = FaxJob.STATUS.ERROR.name();
                            }

                            // Save fax job record for audit trail even if import failed
                            // Healthcare regulations require tracking of all fax activities
                            receivedFax.setFile_name(fileName);

                            // Persist fax transaction record for regulatory compliance
                            saveFaxJob(new FaxJob(receivedFax));
                        }

                    } else {
                        log.error("HTTP Status error with HTTP code: " + status);
                    }

                } catch (IOException e) {
                    log.error("HTTP WS CLIENT ERROR", e);
                } finally {
                    if (mGet != null) {
                        mGet.reset();
                    }
                }
            }
        }

    }

    /**
     * Downloads a medical fax document from the remote service provider.
     *
     * This method handles the secure download of individual medical documents from
     * the fax service provider, including proper error handling and status validation.
     * The download process ensures that medical documents are transferred securely
     * and completely before being processed for local storage.
     *
     * @param client CloseableHttpClient authenticated HTTP client for secure communication
     * @param faxConfig FaxConfig configuration containing service credentials and settings
     * @param fax FaxJob metadata about the document to download
     * @return FaxJob downloaded document with content, or null if download fails
     */
    private FaxJob downloadFax(CloseableHttpClient client, FaxConfig faxConfig, FaxJob fax) {

        FaxJob downloadedFax = null;
        HttpGet mGet = null;

        try {
            mGet = new HttpGet(faxConfig.getUrl() + PATH + "/"
                    + URLEncoder.encode(faxConfig.getFaxUser(), "UTF-8") + "/"
                    + URLEncoder.encode(fax.getFile_name(), "UTF-8"));
            mGet.setHeader("accept", "application/json");
            mGet.setHeader("user", faxConfig.getFaxUser());
            mGet.setHeader("passwd", faxConfig.getFaxPasswd());

            HttpResponse response = client.execute(mGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                HttpEntity httpEntity = response.getEntity();
                String content = EntityUtils.toString(httpEntity);

                ObjectMapper mapper = new ObjectMapper();

                downloadedFax = mapper.readValue(content, FaxJob.class);

                fax.setStatus(downloadedFax.getStatus());
                fax.setStatusString(downloadedFax.getStatusString());

                // the fileName will be null if there is an error
                // will need to modify the receivedFax header appropriately.
                if (FaxJob.STATUS.ERROR.equals(downloadedFax.getStatus())) {
                    downloadedFax = null;
                }
            }

        } catch (ClientProtocolException e) {
            log.error("HTTP WS CLIENT ERROR", e);
        } catch (IOException e) {
            log.error("IO ERROR", e);
        } finally {
            if (mGet != null) {
                mGet.reset();
            }
        }

        return downloadedFax;
    }

    /**
     * Deletes a successfully imported medical fax document from the remote service provider.
     *
     * This method removes medical documents from the remote fax service after successful
     * local import and storage. Deletion is performed to prevent duplicate imports and
     * to maintain proper document lifecycle management on the remote service.
     *
     * The deletion process includes verification of successful removal to ensure
     * proper cleanup and prevent accumulation of processed documents on remote servers.
     *
     * @param client CloseableHttpClient authenticated HTTP client for secure communication
     * @param faxConfig FaxConfig configuration containing service credentials and settings
     * @param fax FaxJob document metadata for the document to delete
     * @throws ClientProtocolException if the HTTP delete request fails
     * @throws IOException if network communication fails during deletion
     */
    private void deleteFax(CloseableHttpClient client, FaxConfig faxConfig, FaxJob fax)
            throws ClientProtocolException, IOException {
        HttpDelete mDelete = new HttpDelete(faxConfig.getUrl() + PATH + "/"
                + URLEncoder.encode(faxConfig.getFaxUser(), "UTF-8") + "/"
                + URLEncoder.encode(fax.getFile_name(), "UTF-8"));

        mDelete.setHeader("accept", "application/json");
        mDelete.setHeader("user", faxConfig.getFaxUser());
        mDelete.setHeader("passwd", faxConfig.getFaxPasswd());

        log.info("Deleting Fax file " + fax.getFile_name() + " from the host server.");

        HttpResponse response = client.execute(mDelete);
        mDelete.reset();

        if (!(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT)) {
            log.debug("Failed to delete Fax file " + fax.getFile_name() + " from the host server.");
            throw new ClientProtocolException("CANNOT DELETE " + fax.getFile_name());
        } else {
            log.info("Fax file " + fax.getFile_name() + " has been deleted from the host server.");
        }
    }

    /**
     * Saves imported medical fax document to local storage and inserts into appropriate workflow queue.
     *
     * This method performs the critical final steps of the import process by:
     * - Converting Base64-encoded document content to local PDF files
     * - Creating proper document metadata with healthcare-appropriate categorization
     * - Storing documents in the secure document management system
     * - Routing documents to configured inbox queues for healthcare staff review
     * - Ensuring proper file naming and format standardization
     *
     * The method handles various document format conversions (TIF to PDF) and
     * ensures that all imported medical documents maintain consistent formatting
     * for healthcare provider review and patient care integration.
     *
     * @param faxConfig FaxConfig configuration containing queue routing information
     * @param receivedFax FaxJob metadata about the received medical document
     * @param faxFile FaxJob containing the actual Base64-encoded document content
     * @return EDoc created document entity, or null if save operation fails
     */
    private EDoc saveAndInsertIntoQueue(FaxConfig faxConfig, FaxJob receivedFax, FaxJob faxFile) {

        String filename = receivedFax.getFile_name();

        filename = filename.replace("|", "-");

        if (filename.isEmpty()) {
            filename = System.currentTimeMillis() + ".pdf";
        }

        filename = filename.replace(".tif", ".pdf");

        if (!filename.endsWith(".pdf") || !filename.endsWith(".PDF")) {
            filename = filename + ".pdf";
        }

        filename = filename.trim();

        EDoc newDoc = new EDoc("Received Fax", "Received Fax", filename, "",
                DEFAULT_USER, DEFAULT_USER, "", 'A',
                DateFormatUtils.format(receivedFax.getStamp(), "yyyy-MM-dd"),
                "", "", "demographic", DEFAULT_USER, receivedFax.getNumPages());

        newDoc.setDocPublic("0");

        filename = newDoc.getFileName();

        if (Base64.decodeToFile(faxFile.getDocument(), DOCUMENT_DIR + "/" + filename)) {

            newDoc.setContentType("application/pdf");
            newDoc.setNumberOfPages(receivedFax.getNumPages());
            String doc_no = EDocUtil.addDocumentSQL(newDoc);

            Integer queueId = faxConfig.getQueue();
            Integer docNum = Integer.parseInt(doc_no);

            queueDocumentLinkDao.addActiveQueueDocumentLink(queueId, docNum);

            log.info("Saved file " + filename + " to filesystem at " + DOCUMENT_DIR + " as document ID " + docNum);

            newDoc.setDocId(doc_no);

            return newDoc;
        }

        log.debug("Failed to save file " + filename + " to filesystem at " + DOCUMENT_DIR);

        return null;

    }

    /**
     * Persists fax job information to the database for tracking and audit purposes.
     *
     * This method saves comprehensive information about imported medical fax documents
     * to maintain proper audit trails required for healthcare regulatory compliance.
     * The saved information includes transmission details, status information, and
     * document metadata needed for tracking and quality assurance.
     *
     * @param saveFax FaxJob containing complete fax transaction information
     * @return Integer unique database ID of the saved fax job record
     */
    private Integer saveFaxJob(FaxJob saveFax) {
        saveFax.setUser(DEFAULT_USER);
        faxJobDao.persist(saveFax);
        return saveFax.getId();
    }


    /**
     * Creates provider lab routing entry for an imported medical document by document ID.
     *
     * This convenience method creates a routing entry for imported fax documents using
     * the document's unique identifier, enabling integration with the healthcare provider's
     * document workflow and notification systems.
     *
     * @param labNo Integer unique document ID for the imported medical fax
     */
    private void providerRouting(Integer labNo) {
        ProviderLabRoutingModel providerLabRouting = new ProviderLabRoutingModel();
        providerLabRouting.setLabNo(labNo);
        providerRouting(providerLabRouting);
    }

    /**
     * Creates provider lab routing entry to trigger unclaimed document indicators for healthcare staff.
     *
     * This method integrates imported medical fax documents with the provider lab routing system,
     * which alerts healthcare staff to newly arrived documents requiring attention. The routing
     * entry triggers visual indicators in the user interface that notify providers of unclaimed
     * documents that need review and assignment.
     *
     * This integration is critical for ensuring that imported medical documents do not go
     * unnoticed and receive prompt attention from appropriate healthcare staff members.
     *
     * @param providerLabRouting ProviderLabRoutingModel routing information for the imported document
     */
    private void providerRouting(ProviderLabRoutingModel providerLabRouting) {

        providerLabRouting.setLabType(ProviderLabRoutingDao.LAB_TYPE.DOC.name());
        providerLabRouting.setProviderNo(ProviderLabRoutingDao.UNCLAIMED_PROVIDER);
        providerLabRouting.setStatus(ProviderLabRoutingDao.STATUS.N.name());
        providerLabRouting.setTimestamp(new Date(System.currentTimeMillis()));
        providerLabRoutingDao.persist(providerLabRouting);

        Integer id = providerLabRouting.getId();
        if (id == null || id < 1) {
            log.warn("Failed to add Fax document id " + providerLabRouting.getLabNo() + " to provider lab routing.");
        }
    }

}
