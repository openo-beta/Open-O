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


package ca.openosp.openo.mds.data;

import org.apache.commons.lang3.StringUtils;
import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.utility.DbConnectionFilter;
import ca.openosp.openo.utility.SpringUtils;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.Tuple;

/**
 * CategoryData provides comprehensive data aggregation and statistical analysis for laboratory results
 * within the MDS (Medical Data Systems) laboratory provider interface in the Canadian healthcare system.
 * <p>
 * This class serves as the primary data access and analysis layer for laboratory document and result management,
 * integrating with multiple Canadian laboratory systems including HL7 message processing, HRM (Hospital Report Manager)
 * documents, and MDS-specific lab data formats. It provides sophisticated patient matching capabilities,
 * abnormal result detection, and comprehensive statistical reporting for healthcare providers.
 * <p>
 * Key functionality includes:
 * <ul>
 * <li>Multi-source lab data aggregation (HL7, documents, HRM reports)</li>
 * <li>Patient demographic searching with fuzzy matching capabilities</li>
 * <li>Provider-specific lab routing and status management</li>
 * <li>Abnormal result detection and priority classification</li>
 * <li>Date-based filtering with configurable search criteria</li>
 * <li>Statistical analysis for lab workflow optimization</li>
 * </ul>
 * <p>
 * This class integrates with OpenO EMR's security framework requiring appropriate lab access privileges
 * and maintains audit trails for all patient health information access in compliance with Canadian
 * healthcare privacy regulations (PIPEDA).
 *
 * @since June 18, 2011
 */
public class CategoryData {

    /** DAO for accessing system preferences related to lab display and date search configurations */
    private final SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(SystemPreferencesDao.class);

    /** EntityManagerFactory for managing database connections for complex HRM document queries */
    private final EntityManagerFactory entityManagerFactory = SpringUtils.getBean(EntityManagerFactory.class);
    /** EntityManager for executing native SQL queries against HRM document tables */
    private final EntityManager entityManager = entityManagerFactory.createEntityManager();

    /** Total count of all document-type lab results (including HRM documents) */
    private int totalDocs = 0;
    /** Total count of all HL7-based laboratory results */
    private int totalLabs = 0;
    /** Count of HL7 lab results that are not matched to any patient demographic */
    private int unmatchedLabs = 0;
    /** Count of document-type results that are not matched to any patient demographic */
    private int unmatchedDocs = 0;
    /** Combined total of all documents and labs (totalDocs + totalLabs) */
    private int totalNumDocs = 0;
    /** Count of results flagged as abnormal across all lab types */
    private int abnormalCount = 0;
    /** Count of results flagged as normal (calculated as totalNumDocs - abnormalCount) */
    private int normalCount = 0;
    /** Count of HRM documents that are unmatched to patient demographics */
    private int unmatchedHRMCount = 0;
    /** Count of HRM documents that are successfully matched to patient demographics */
    private int matchedHRMCount = 0;

    /** Map of demographic IDs to PatientInfo objects containing aggregated patient lab counts */
    private HashMap<Integer, PatientInfo> patients;

    /**
     * Gets the total count of all document-type laboratory results including HRM documents.
     *
     * @return int total document count
     */
    public int getTotalDocs() {
        return totalDocs;
    }

    /**
     * Gets the total count of all HL7-based laboratory results.
     *
     * @return int total HL7 lab count
     */
    public int getTotalLabs() {
        return totalLabs;
    }

    /**
     * Gets the count of HL7 laboratory results that could not be matched to patient demographics.
     *
     * @return int unmatched HL7 lab count
     */
    public int getUnmatchedLabs() {
        return unmatchedLabs;
    }

    /**
     * Gets the count of document-type results that could not be matched to patient demographics.
     *
     * @return int unmatched document count
     */
    public int getUnmatchedDocs() {
        return unmatchedDocs;
    }

    /**
     * Gets the count of HRM (Hospital Report Manager) documents that are unmatched to patient demographics.
     *
     * @return int unmatched HRM document count
     */
    public int getUnmatchedHRMCount() {
        return unmatchedHRMCount;
    }

    /**
     * Gets the count of HRM (Hospital Report Manager) documents successfully matched to patient demographics.
     *
     * @return int matched HRM document count
     */
    public int getMatchedHRMCount() {
        return matchedHRMCount;
    }

    /**
     * Gets the combined total count of all documents and laboratory results.
     *
     * @return int total count (totalDocs + totalLabs)
     */
    public int getTotalNumDocs() {
        return totalNumDocs;
    }

    /**
     * Gets the count of laboratory results flagged as abnormal across all result types.
     *
     * @return int abnormal result count
     */
    public int getAbnormalCount() {
        return abnormalCount;
    }

    /**
     * Gets the count of laboratory results flagged as normal (calculated value).
     *
     * @return int normal result count
     */
    public int getNormalCount() {
        return normalCount;
    }

    /**
     * Gets the map of patient information indexed by demographic ID.
     * <p>
     * This map contains aggregated patient data including lab counts, document counts,
     * and HRM document counts for each patient demographic.
     *
     * @return HashMap<Integer, PatientInfo> map of demographic IDs to patient information
     */
    public HashMap<Integer, PatientInfo> getPatients() {
        return patients;
    }

    /**
     * Gets a sorted list of patient information objects.
     * <p>
     * Returns patient information sorted alphabetically by last name, then first name.
     * This is useful for display purposes where consistent ordering is required.
     *
     * @return List<PatientInfo> sorted list of patient information, empty list if no patients
     */
    public List<PatientInfo> getPatientList() {
        if (patients == null || patients.isEmpty()) {
            return Collections.emptyList();
        }
        List<PatientInfo> patientInfoList = new ArrayList<>(patients.values());
        Collections.sort(patientInfoList);
        return patientInfoList;
    }

    /** Patient's last name for search filtering (supports partial matching with wildcards) */
    private String patientLastName;
    /** Provider number for filtering results to specific healthcare provider */
    private String searchProviderNo;
    /** Laboratory result status filter (N=New, A=Acknowledged, F=Filed) */
    private String status;
    /** Abnormal result status filter (abnormalOnly, normalOnly, or all) */
    private String abnormalStatus;
    /** Patient's first name for search filtering (supports partial matching with wildcards) */
    private String patientFirstName;
    /** Patient's health insurance number for search filtering */
    private String patientHealthNumber;
    /** Flag indicating if this is a patient-specific search */
    private boolean patientSearch;
    /** Flag indicating if results should be filtered by specific provider */
    private boolean providerSearch;
    /** SQL fragment for date filtering on document-type lab results */
    private String documentDateSql = "";
    /** SQL fragment for abnormal status filtering on document-type lab results */
    private String documentAbnormalSql = "";
    /** SQL fragment for joining document tables when date or abnormal filtering is needed */
    private String documentJoinSql = "";
    /** SQL fragment for date filtering on HL7 lab results */
    private String labDateSql = "";
    /** SQL fragment for abnormal status filtering on HL7 lab results */
    private String labAbnormalSql = "";
    /** SQL fragment for date filtering on HRM documents */
    private String hrmDateSql = "";
    /** SQL fragment for provider filtering on HRM documents */
    private String hrmProviderSql = "";
    /** SQL fragment for HRM document viewed status filtering */
    private String hrmViewed = "";
    /** SQL fragment for HRM document sign-off status filtering */
    private String hrmSignedOff = "";

    /**
     * Constructs a CategoryData instance for comprehensive laboratory data analysis.
     * <p>
     * This constructor initializes all search criteria and builds dynamic SQL fragments for
     * filtering laboratory results across multiple data sources (HL7, documents, HRM).
     * The date search type is configurable via system preferences to use either service/observation
     * dates or received/created dates.
     *
     * @param patientLastName String patient's last name for filtering (null or empty for no filter)
     * @param patientFirstName String patient's first name for filtering (null or empty for no filter)
     * @param patientHealthNumber String patient's health insurance number for filtering (null or empty for no filter)
     * @param patientSearch boolean true if searching for specific patients, false for provider-wide search
     * @param providerSearch boolean true if results should be filtered by specific provider
     * @param searchProviderNo String provider number for filtering (null or empty for all providers)
     * @param status String lab result status filter ("N", "A", "F", or empty for all)
     * @param abnormalStatus String abnormal result filter ("abnormalOnly", "normalOnly", or "all")
     * @param startDate String start date for filtering in YYYY-MM-DD format (null or empty for no start limit)
     * @param endDate String end date for filtering in YYYY-MM-DD format (null or empty for no end limit)
     * @since June 18, 2011
     */
    public CategoryData(String patientLastName, String patientFirstName, String patientHealthNumber, boolean patientSearch,
                        boolean providerSearch, String searchProviderNo, String status, String abnormalStatus,
                        String startDate, String endDate) {

        this.patientLastName = patientLastName;
        this.searchProviderNo = searchProviderNo;
        this.status = status;
        this.patientFirstName = patientFirstName;
        this.patientHealthNumber = patientHealthNumber;
        this.patientSearch = patientSearch;
        this.providerSearch = providerSearch;
		this.abnormalStatus = abnormalStatus;

        // Initialize system preferences DAO for retrieving lab display preferences
        SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(SystemPreferencesDao.class);
        // Default to service/observation date search if preference not configured
        String dateSearchType = "serviceObservation";

        SystemPreferences systemPreferences = systemPreferencesDao.findPreferenceByName(SystemPreferences.LAB_DISPLAY_PREFERENCE_KEYS.inboxDateSearchType);
        if (systemPreferences != null) {
            if (systemPreferences.getValue() != null && !systemPreferences.getValue().isEmpty()) {
                dateSearchType = systemPreferences.getValue();
            }
        }

        // Build start date filter SQL fragments based on configured date search type
        if (startDate != null && !startDate.equals("")) {
            if (dateSearchType.equals("receivedCreated")) {
                // Use document content/creation dates for filtering
                documentDateSql += "AND doc.contentdatetime >= '" + startDate + " 00:00:00' ";
                labDateSql += "AND message.created >= '" + startDate + " 00:00:00' ";
            } else {
                // Use service/observation dates for filtering (default behavior)
                documentDateSql += "AND doc.observationdate >= '" + startDate + " 00:00:00' ";
                labDateSql += "AND info.obr_date >= '" + startDate + " 00:00:00' ";
            }
            // HRM documents always use timeReceived for date filtering
            hrmDateSql += "AND h.timeReceived >= '" + startDate + " 00:00:00' ";
        }

        // Build end date filter SQL fragments based on configured date search type
        if (endDate != null && !endDate.equals("")) {
            if (dateSearchType.equals("receivedCreated")) {
                // Use document content/creation dates for filtering
                documentDateSql += "AND doc.contentdatetime <= '" + endDate + " 23:59:59' ";
                labDateSql += "AND message.created <= '" + endDate + " 23:59:59' ";
            } else {
                // Use service/observation dates for filtering (default behavior)
                documentDateSql += "AND doc.observationdate <= '" + endDate + " 23:59:59' ";
                labDateSql += "AND info.obr_date <= '" + endDate + " 23:59:59' ";
            }
            // HRM documents always use timeReceived for date filtering
            hrmDateSql += "AND h.timeReceived <= '" + endDate + " 23:59:59' ";
        }

        // Build abnormal status filter SQL fragments for document and lab results
        if (abnormalStatus != null && !abnormalStatus.equals("all")) {
            if (abnormalStatus.equals("abnormalOnly")) {
                // Filter to show only abnormal results
                documentAbnormalSql = " AND doc.abnormal = TRUE ";
                labAbnormalSql = " AND info.result_status = 'A' ";
            } else if (abnormalStatus.equals("normalOnly")) {
                // Filter to show only normal results (including NULL values)
                documentAbnormalSql = " AND (doc.abnormal = FALSE OR doc.abnormal IS NULL) ";
                labAbnormalSql = " AND (info.result_status IS NULL OR info.result_status != 'A') ";
            }
        }

        // Add document table join only when date or abnormal filtering is needed
        if (!documentDateSql.equals("") || !documentAbnormalSql.equals("")) {
            documentJoinSql = " LEFT JOIN document doc ON cd.document_no = doc.document_no";
        }

        // Build provider filter SQL fragments for HRM documents
        if (providerSearch) {
            if (searchProviderNo.equals("0")) {
                // Special case: "0" maps to "-1" for unassigned provider filtering
                hrmProviderSql += " AND hp.providerNo ='-1'";
            } else {
                // Filter by specific provider number
                hrmProviderSql += " AND hp.providerNo ='" + searchProviderNo + "' ";
            }
        }

        // Build HRM document status filter SQL fragments based on workflow status
        hrmViewed = " AND hp.viewed = 1 ";
        hrmSignedOff = " AND hp.signedOff = 0 ";
        if (status == null || status.equalsIgnoreCase("N")) {
            // New/unread status - no viewed restriction
            hrmViewed = "";
        } else if (status.equalsIgnoreCase("A") || status.equalsIgnoreCase("F")) {
            // Acknowledged/Filed status - must be signed off
            hrmSignedOff = " AND hp.signedOff = 1 ";
        } else if (status.isEmpty()) {
            // Empty status - show all regardless of viewed/signed status
            hrmViewed = "";
            hrmSignedOff = "";
        }

        // Initialize all counters to zero
        totalDocs = 0;
        totalLabs = 0;
        unmatchedLabs = 0;
        unmatchedDocs = 0;
        totalNumDocs = 0;
        abnormalCount = 0;
        normalCount = 0;

        // Initialize patient information map
        patients = new HashMap<Integer, PatientInfo>();

    }

    /**
     * Populates all laboratory result counts and patient information.
     * <p>
     * This method orchestrates the data collection process by:
     * <ul>
     * <li>Counting matched documents and HL7 lab results for patients</li>
     * <li>Counting HRM documents for matched patients</li>
     * <li>Counting unmatched documents and labs (if not patient-specific search)</li>
     * <li>Calculating abnormal vs normal result statistics</li>
     * </ul>
     * The method respects the configured search criteria including date ranges,
     * provider filters, and abnormal status filters.
     *
     * @throws SQLException if database operations fail during count retrieval
     */
    public void populateCountsAndPatients() throws SQLException {

        // Retrieve matched documents and labs for patients meeting search criteria
        totalDocs += getDocumentCountForPatientSearch();
        totalLabs += getLabCountForPatientSearch();

        // Retrieve HRM document counts for matched patients
        // Note: HRM filtering by abnormal status is not supported, so skip for abnormal-only searches
        if (abnormalStatus == null || !abnormalStatus.equals("abnormalOnly")) {
            matchedHRMCount = getHRMDocumentCountForPatient();
        }

        // Include matched HRM documents in total document count
        totalDocs += matchedHRMCount;

        // For provider-wide searches, also count unmatched documents and labs
        if (!patientSearch) {
            unmatchedDocs += getDocumentCountForUnmatched();
            unmatchedLabs += getLabCountForUnmatched();

            // Retrieve unmatched HRM document counts
            // Note: HRM filtering by abnormal status is not supported
            if (abnormalStatus == null || !abnormalStatus.equals("abnormalOnly")) {
                unmatchedHRMCount += getHRMDocumentCountForUnmatched();
            }

            // Include all unmatched results in totals
            totalDocs += unmatchedHRMCount;
            totalDocs += unmatchedDocs;
            totalLabs += unmatchedLabs;
        }

        // Calculate total count across all result types
        totalNumDocs = totalDocs + totalLabs;

        // Count abnormal results across all laboratory data types
        abnormalCount = getAbnormalCount(true);

        // Calculate normal count by subtraction (more efficient than separate query)
        normalCount = totalNumDocs - abnormalCount;
    }

    /**
     * Gets the configured date search type preference for laboratory filtering.
     * <p>
     * The date search type determines which date fields are used when filtering laboratory
     * results by date range:
     * <ul>
     * <li>"serviceObservation" - Use service/observation dates (default)</li>
     * <li>"receivedCreated" - Use received/created dates</li>
     * </ul>
     * This setting affects both document-type and HL7 lab result filtering.
     *
     * @return String the configured date search type, defaults to "serviceObservation"
     */
    public String getDateSearchType() {
        SystemPreferences systemPreferences = systemPreferencesDao.findPreferenceByName(SystemPreferences.LAB_DISPLAY_PREFERENCE_KEYS.inboxDateSearchType);
        if (systemPreferences != null) {
            if (systemPreferences.getValue() != null && !systemPreferences.getValue().isEmpty()) {
                return systemPreferences.getValue();
            }
        }

        return "serviceObservation";
    }

    /**
     * Counts HL7 laboratory results that are not matched to any patient demographic.
     * <p>
     * This method identifies lab results that exist in the provider routing table but
     * do not have corresponding patient routing records, indicating they need manual
     * patient matching. The count respects all configured filters including provider,
     * status, date range, and abnormal status.
     *
     * @return int count of unmatched HL7 lab results
     * @throws SQLException if database query fails
     */
    public int getLabCountForUnmatched()
            throws SQLException {
        String dateSearchType = getDateSearchType();
        String sql = " SELECT HIGH_PRIORITY COUNT(DISTINCT accessionNum) as count "
                + " FROM providerLabRouting plr "
                + " LEFT JOIN patientLabRouting plr2 ON plr.lab_no = plr2.lab_no AND plr2.lab_type = 'HL7'"
                + " RIGHT JOIN hl7TextInfo info ON plr.lab_no = info.lab_no"
                + (dateSearchType.equals("receivedCreated") ? " RIGHT JOIN hl7TextMessage message ON plr.lab_no = message.lab_id" : "")
                + " WHERE plr.lab_type = 'HL7' "
                + (providerSearch ? " AND plr.provider_no = ? " : "")
                + " AND plr.status " + ("".equals(status) ? " IS NOT NULL " : " = ? ")
                + labAbnormalSql
                + labDateSql
                + " AND (plr2.demographic_no IS NULL"
                + " OR plr2.demographic_no = '0')";


        Connection c = DbConnectionFilter.getThreadLocalDbConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        
        int paramIndex = 1;
        if (providerSearch) ps.setString(paramIndex++, searchProviderNo);
        if (!"".equals(status)) ps.setString(paramIndex++, status);

        ResultSet rs = ps.executeQuery();

        return (rs.next() ? rs.getInt("count") : 0);
    }

    /**
     * Counts laboratory results based on abnormal status flag.
     * <p>
     * This method counts HL7 laboratory results that are either abnormal or normal
     * based on the isAbnormal parameter. The count respects all search filters
     * including patient criteria, provider filtering, and status filtering.
     * Only HL7 lab results are counted - document-type results do not have
     * standardized abnormal flags in the HL7TextInfo table.
     *
     * @param isAbnormal boolean true to count abnormal results, false for normal results
     * @return int count of results matching the abnormal status criteria
     * @throws SQLException if database query fails
     */
    public int getAbnormalCount(boolean isAbnormal) throws SQLException {
        ResultSet rs;
        String sql;
        PreparedStatement ps = null;
        Connection c = DbConnectionFilter.getThreadLocalDbConnection();
        if (patientSearch) {
            sql = " SELECT HIGH_PRIORITY COUNT(1) as count "
                    + " FROM patientLabRouting cd, demographic d, providerLabRouting plr, hl7TextInfo info "
                    + " WHERE d.last_name" + (StringUtils.isEmpty(patientLastName) ? " IS NOT NULL " : " like ?  ")
                    + " 	AND d.first_name" + (StringUtils.isEmpty(patientFirstName) ? " IS NOT NULL " : " like ? ")
                    + " 	AND d.hin" + (StringUtils.isEmpty(patientHealthNumber) ? " IS NOT NULL " : " like ? ")
                    + " 	AND plr.status " + ("".equals(status) ? " IS NOT NULL " : " = ? ")
                    + (providerSearch ? "AND plr.provider_no = ? " : "")
                    + " 	AND plr.lab_type = 'HL7' "
                    + " 	AND cd.lab_type = 'HL7' "
                    + " 	AND cd.lab_no = plr.lab_no "
                    + " 	AND cd.demographic_no = d.demographic_no "
                    + " 	AND info.lab_no = plr.lab_no "
                    + " 	AND result_status " + (isAbnormal ? "" : "!") + "= 'A' ";
            ps = c.prepareStatement(sql);

            int paramIndex = 1;
            if (!StringUtils.isEmpty(patientLastName)) ps.setString(paramIndex++, "%" + patientLastName + "%");
            if (!StringUtils.isEmpty(patientFirstName)) ps.setString(paramIndex++, "%" + patientFirstName + "%");
            if (!StringUtils.isEmpty(patientHealthNumber)) ps.setString(paramIndex++, "%" + patientHealthNumber + "%");
            if (!"".equals(status)) ps.setString(paramIndex++, status);
            if (providerSearch) ps.setString(paramIndex++, searchProviderNo);
        } else if (providerSearch || !"".equals(status)) { // providerSearch
            sql = "SELECT HIGH_PRIORITY COUNT(1) as count "
                    + " FROM providerLabRouting plr, hl7TextInfo info "
                    + " WHERE plr.status " + ("".equals(status) ? " IS NOT NULL " : " = ?")
                    + (providerSearch ? " AND plr.provider_no = ? " : " ")
                    + " AND plr.lab_type = 'HL7'  "
                    + " AND info.lab_no = plr.lab_no"
                    + " AND result_status " + (isAbnormal ? "" : "!") + "= 'A' ";
            ps = c.prepareStatement(sql);

            int paramIndex = 1;
            if (!"".equals(status)) ps.setString(paramIndex++, status);
            if (providerSearch) ps.setString(paramIndex++, searchProviderNo);
        } else {
            sql = " SELECT HIGH_PRIORITY COUNT(1) as count "
                    + " FROM hl7TextInfo info "
                    + " WHERE result_status " + (isAbnormal ? "" : "!") + "= 'A' ";
            ps = c.prepareStatement(sql);
        }
        rs = ps.executeQuery();
        return (rs.next() ? rs.getInt("count") : 0);
    }

    /**
     * Counts document-type laboratory results that are not matched to patient demographics.
     * <p>
     * This method identifies document-type lab results that exist in the control document
     * table with module_id = -1 (indicating unmatched status) and have provider routing
     * records but lack patient routing records. These documents require manual patient
     * matching before they can be properly filed.
     *
     * @return int count of unmatched document-type lab results
     * @throws SQLException if database query fails
     */
    public int getDocumentCountForUnmatched()
            throws SQLException {
        String sql = " SELECT HIGH_PRIORITY COUNT(1) as count "
                + " FROM ctl_document cd "
                + "LEFT JOIN providerLabRouting plr ON plr.lab_no = cd.document_no"
                + documentJoinSql
                + " WHERE plr.lab_type = 'DOC' "
                + " AND plr.status " + ("".equals(status) ? " IS NOT NULL " : " = ? ")
                + (providerSearch ? " AND plr.provider_no = ? " : "")
                + " AND 	cd.module_id = -1 "
                + documentAbnormalSql
                + documentDateSql;
        Connection c = DbConnectionFilter.getThreadLocalDbConnection();
        PreparedStatement ps = c.prepareStatement(sql);

        int paramIndex = 1;
        if (!"".equals(status)) ps.setString(paramIndex++, status);
        if (providerSearch) ps.setString(paramIndex++, searchProviderNo);
        ResultSet rs = ps.executeQuery();

        return (rs.next() ? rs.getInt("count") : 0);
    }

    /**
     * Counts HL7 laboratory results for patients matching search criteria and populates patient information.
     * <p>
     * This method performs a complex query that:
     * <ul>
     * <li>Finds HL7 lab results linked to patients via patient routing</li>
     * <li>Filters by patient demographics (name, health number)</li>
     * <li>Applies provider, status, and date range filters</li>
     * <li>Groups results by patient and accession number to avoid duplicates</li>
     * <li>Populates or updates PatientInfo objects with lab counts</li>
     * </ul>
     * The method integrates with the patients HashMap to maintain patient information
     * across multiple data source queries.
     *
     * @return int total count of matching HL7 lab results
     * @throws SQLException if database query fails
     */
    public int getLabCountForPatientSearch() throws SQLException {
        PatientInfo info;
        String dateSearchType = getDateSearchType();
        String sql = " SELECT HIGH_PRIORITY d.demographic_no, d.last_name, d.first_name, COUNT(*) as count "
                + " FROM patientLabRouting cd"
                + " LEFT JOIN demographic d ON cd.demographic_no = d.demographic_no"
                + " LEFT JOIN providerLabRouting plr ON cd.lab_no = plr.lab_no"
                + " LEFT JOIN hl7TextInfo info ON cd.lab_no = info.lab_no"
                + (dateSearchType.equals("receivedCreated") ? " LEFT JOIN hl7TextMessage message ON cd.lab_no = message.lab_id" : "")
                + " WHERE   d.last_name" + (StringUtils.isEmpty(patientLastName) ? " IS NOT NULL " : "  like ? ")
                + " 	AND d.first_name" + (StringUtils.isEmpty(patientFirstName) ? " IS NOT NULL " : " like ? ")
                + " 	AND d.hin" + (StringUtils.isEmpty(patientHealthNumber) ? " IS NOT NULL " : " like ? ")
                + " 	AND plr.lab_type = 'HL7' "
                + " 	AND cd.lab_type = 'HL7' "
                + " 	AND plr.status " + ("".equals(status) ? " IS NOT NULL " : " = ? ")
                + (dateSearchType.equals("receivedCreated") ? " AND message.lab_id IS NOT NULL " : " AND info.lab_no IS NOT NULL ")
                + (providerSearch ? " AND plr.provider_no = ? " : "")
                + labAbnormalSql
                + labDateSql
                + " GROUP BY demographic_no, info.accessionNum ";

        Connection c = DbConnectionFilter.getThreadLocalDbConnection();
        PreparedStatement ps = c.prepareStatement(sql);

        int paramIndex = 1;
        if (!StringUtils.isEmpty(patientLastName)) ps.setString(paramIndex++, "%" + patientLastName + "%");
        if (!StringUtils.isEmpty(patientFirstName)) ps.setString(paramIndex++, "%" + patientFirstName + "%");
        if (!StringUtils.isEmpty(patientHealthNumber)) ps.setString(paramIndex++, "%" + patientHealthNumber + "%");
        if (!"".equals(status)) ps.setString(paramIndex++, status);
        if (providerSearch) ps.setString(paramIndex++, searchProviderNo);
        ResultSet rs = ps.executeQuery();

        int totalCount = 0;
        while (rs.next()) {
            int id = rs.getInt("demographic_no");
			int count = rs.getInt("count");
            // Updating patient info if it already exists.
            if (patients.containsKey(id)) {
                info = patients.get(id);
        		info.setLabCount(count);
            }
            // Otherwise adding a new patient record.
            else {
                info = new PatientInfo(id, rs.getString("first_name"), rs.getString("last_name"));
        		info.setLabCount(count);
                patients.put(info.getId(), info);
            }
        	totalCount += count;
        }
        return totalCount;
    }

    /**
     * Counts HL7 laboratory results for a specific patient demographic.
     * <p>
     * This method provides a targeted count of lab results for a single patient,
     * applying provider and status filters. It is typically used for patient-specific
     * views where the demographic ID is already known.
     *
     * @param demographicNo String the patient demographic ID to count results for
     * @return int count of HL7 lab results for the specified patient
     * @throws SQLException if database query fails or demographic ID is invalid
     */
    public int getLabCountForDemographic(String demographicNo) throws SQLException {
        String sql = " SELECT HIGH_PRIORITY d.demographic_no, last_name, first_name, COUNT(1) as count "
                + " FROM patientLabRouting cd,  demographic d, providerLabRouting plr "
                + " WHERE   d.demographic_no = " + demographicNo
                + " 	AND cd.demographic_no = d.demographic_no "
                + " 	AND cd.lab_no = plr.lab_no "
                + " 	AND plr.lab_type = 'HL7' "
                + " 	AND cd.lab_type = 'HL7' "
                + " 	AND plr.status " + ("".equals(status) ? " IS NOT NULL " : " = '" + status + "' ")
                + (providerSearch ? "AND plr.provider_no = '" + searchProviderNo + "' " : "")
                + " GROUP BY demographic_no ";

        Connection c = DbConnectionFilter.getThreadLocalDbConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        return (rs.next() ? rs.getInt("count") : 0);
    }

    /**
     * Counts document-type laboratory results for patients matching search criteria and populates patient information.
     * <p>
     * This method performs a comprehensive query that:
     * <ul>
     * <li>Finds document-type lab results linked to patients via control document table</li>
     * <li>Filters by patient demographics (name, health number)</li>
     * <li>Applies provider, status, date range, and abnormal status filters</li>
     * <li>Groups results by patient demographic to get per-patient counts</li>
     * <li>Creates or updates PatientInfo objects with document counts</li>
     * </ul>
     * The method integrates with the patients HashMap, creating new PatientInfo entries
     * or updating existing ones from previous HL7 lab queries.
     *
     * @return int total count of matching document-type lab results
     * @throws SQLException if database query fails
     */
    public int getDocumentCountForPatientSearch() throws SQLException {
        PatientInfo info;

        String sql = " SELECT HIGH_PRIORITY demographic_no, last_name, first_name, COUNT( distinct cd.document_no) as count "
                + " FROM ctl_document cd "
                + "LEFT JOIN demographic d  ON cd.module_id = d.demographic_no "
                + "LEFT JOIN providerLabRouting plr ON cd.document_no = plr.lab_no "
                + documentJoinSql
                + " WHERE   d.last_name" + (StringUtils.isEmpty(patientLastName) ? " IS NOT NULL " : " like ?  ")
                + " 	AND d.hin" + (StringUtils.isEmpty(patientHealthNumber) ? " IS NOT NULL " : " like ? ")
                + " 	AND d.first_name" + (StringUtils.isEmpty(patientFirstName) ? " IS NOT NULL " : " like ? ")
                + " 	AND plr.lab_type = 'DOC' "
                + " 	AND plr.status " + ("".equals(status) ? " IS NOT NULL " : " = ? ")
                + (providerSearch ? "AND plr.provider_no = ? " : "")
                + documentAbnormalSql
                + documentDateSql
                + " GROUP BY demographic_no ";
        Connection c = DbConnectionFilter.getThreadLocalDbConnection();
        PreparedStatement ps = c.prepareStatement(sql);

        int paramIndex = 1;
        if (!StringUtils.isEmpty(patientLastName)) ps.setString(paramIndex++, "%" + patientLastName + "%");
        if (!StringUtils.isEmpty(patientHealthNumber)) ps.setString(paramIndex++, "%" + patientHealthNumber + "%");
        if (!StringUtils.isEmpty(patientFirstName)) ps.setString(paramIndex++, "%" + patientFirstName + "%");
        if (!"".equals(status)) ps.setString(paramIndex++, status);
        if (providerSearch) ps.setString(paramIndex++, searchProviderNo);

        ResultSet rs = ps.executeQuery();

        int count = 0;
        while (rs.next()) {
            info = new PatientInfo(rs.getInt("demographic_no"), rs.getString("first_name"), rs.getString("last_name"));
            info.setDocCount(rs.getInt("count"));
            patients.put(info.getId(), info);
            count += info.getDocCount();
        }

        return count;
    }

    /**
     * Counts HRM (Hospital Report Manager) documents for patients matching search criteria and updates patient information.
     * <p>
     * This method queries the HRM document system to find documents that:
     * <ul>
     * <li>Are linked to patient demographics matching search criteria</li>
     * <li>Match the configured provider filters and view/sign-off status</li>
     * <li>Fall within the specified date range</li>
     * </ul>
     * HRM documents represent reports from hospital systems and require special handling
     * as they don't support abnormal status filtering. The method updates existing
     * PatientInfo objects with HRM document counts.
     *
     * @return int total count of matching HRM documents
     * @throws SQLException if database query fails
     */
    public int getHRMDocumentCountForPatient() throws SQLException {
        int count = 0;
        PatientInfo info;

		StringBuilder sql = new StringBuilder();
        sql.append("SELECT HIGH_PRIORITY d.demographic_no, d.first_name, d.last_name, COUNT(DISTINCT h.id) AS count ")
           .append(" FROM HRMDocumentToDemographic hd ")
           .append(" LEFT JOIN HRMDocumentToProvider hp ON hd.hrmDocumentId = hp.hrmDocumentId ")
           .append(" LEFT JOIN HRMDocument h ON hd.hrmDocumentId = h.id ")
           .append(" JOIN demographic d ON hd.demographicNo = d.demographic_no ")
           .append(" WHERE 1=1 ")
           .append(" AND d.last_name ").append(StringUtils.isNotEmpty(patientLastName) ? "LIKE :patientLastName " : "IS NOT NULL ")
           .append(" AND d.hin ").append(StringUtils.isNotEmpty(patientHealthNumber) ? "LIKE :patientHealthNumber " : "IS NOT NULL ")
           .append(" AND d.first_name ").append(StringUtils.isNotEmpty(patientFirstName) ? "LIKE :patientFirstName " : "IS NOT NULL ")
           .append(hrmViewed).append(hrmSignedOff).append(hrmDateSql).append(hrmProviderSql)
           .append(" GROUP BY d.demographic_no ");

        Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);

        // Set parameters only if they are not empty
        if (StringUtils.isNotEmpty(patientLastName)) {
            query.setParameter("patientLastName", "%" + patientLastName + "%");
        }
        if (StringUtils.isNotEmpty(patientHealthNumber)) {
            query.setParameter("patientHealthNumber", "%" + patientHealthNumber + "%");
        }
        if (StringUtils.isNotEmpty(patientFirstName)) {
            query.setParameter("patientFirstName", "%" + patientFirstName + "%");
        }

        // Executing the query and processing the results
		List<Tuple> results = query.getResultList();

		for (Tuple result : results) {
			Integer hrmCount = result.get("count", BigInteger.class).intValue(); // Extracting count as Integer
			Integer id = result.get("demographic_no", Integer.class); // Extracting demographicNo as Integer

			// Updating patient info if it already exists
            if (patients.containsKey(id)) {
                info = patients.get(id);
                info.setDocCount(info.getDocCount() + hrmCount);
				info.setHrmCount(hrmCount);
			} else {
				// Otherwise, adding a new patient record
				info = new PatientInfo(id, result.get("first_name", String.class), result.get("last_name", String.class));
                info.setDocCount(hrmCount);
				info.setHrmCount(hrmCount);
                patients.put(info.getId(), info);
            }

            count += hrmCount;
        }

        return count;
    }

    /**
     * Counts HRM (Hospital Report Manager) documents that are not matched to patient demographics.
     * <p>
     * This method identifies HRM documents that exist in the provider routing table
     * but do not have corresponding demographic links, indicating they need manual
     * patient matching. The count respects provider filters and view/sign-off status
     * but cannot filter by patient demographics (since they are unmatched).
     *
     * @return int count of unmatched HRM documents
     * @throws SQLException if database query fails
     */
    public int getHRMDocumentCountForUnmatched() throws SQLException {
        int count = 0;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT HIGH_PRIORITY COUNT(DISTINCT h.id) AS count ")
			.append(" FROM HRMDocumentToProvider hp ")
			.append(" LEFT JOIN HRMDocument h ON h.id = hp.hrmDocumentId ")
			.append(" LEFT JOIN HRMDocumentToDemographic hd ON hd.hrmDocumentId = hp.hrmDocumentId ")
			.append(" WHERE hd.hrmDocumentId IS NULL ")
			.append(hrmViewed)
			.append(hrmSignedOff)
			.append(hrmDateSql)
			.append(hrmProviderSql);

		// Create a native query using the entity manager
		Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);

		// Execute the query and retrieve the results
		List<Tuple> results = query.getResultList();

		// Process the results
		for (Tuple result : results) {
			Integer hrmCount = result.get("count", BigInteger.class).intValue(); // Extracting count as Integer
			count += hrmCount; // Accumulate the total count
        }

        return count;
    }

    /**
     * Generates a hash value representing the current state of category counts.
     * <p>
     * This method creates a unique identifier based on the current count values,
     * useful for detecting changes in laboratory data without expensive re-queries.
     * The hash incorporates:
     * <ul>
     * <li>Total number of all documents and labs</li>
     * <li>Total document count</li>
     * <li>Total HL7 lab count</li>
     * </ul>
     * The calculation uses ASCII values ('A', 'D', 'L') as multipliers to ensure
     * different count combinations produce unique hash values.
     *
     * @return Long hash value representing current category state
     */
    public Long getCategoryHash() {
        return Long.valueOf("" + (int) 'A' + totalNumDocs)
                + Long.valueOf("" + (int) 'D' + totalDocs)
                + Long.valueOf("" + (int) 'L' + totalLabs);
    }
}
