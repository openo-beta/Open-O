//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1.queries;

import java.util.LinkedList;
import java.util.List;

import ca.openosp.openo.olis1.parameters.OBR16;
import ca.openosp.openo.olis1.parameters.OBR22;
import ca.openosp.openo.olis1.parameters.OBR25;
import ca.openosp.openo.olis1.parameters.OBR28;
import ca.openosp.openo.olis1.parameters.OBR4;
import ca.openosp.openo.olis1.parameters.OBR7;
import ca.openosp.openo.olis1.parameters.OBX3;
import ca.openosp.openo.olis1.parameters.ORC4;
import ca.openosp.openo.olis1.parameters.PID3;
import ca.openosp.openo.olis1.parameters.PV117;
import ca.openosp.openo.olis1.parameters.PV17;
import ca.openosp.openo.olis1.parameters.QRD7;
import ca.openosp.openo.olis1.parameters.ZBE4;
import ca.openosp.openo.olis1.parameters.ZBE6;
import ca.openosp.openo.olis1.parameters.ZBR2;
import ca.openosp.openo.olis1.parameters.ZBR3;
import ca.openosp.openo.olis1.parameters.ZBR4;
import ca.openosp.openo.olis1.parameters.ZBR6;
import ca.openosp.openo.olis1.parameters.ZPD1;
import ca.openosp.openo.olis1.parameters.ZPD3;
import ca.openosp.openo.olis1.parameters.ZRP1;

/**
 * Z01 Query - Retrieve Laboratory Information for Patient by Demographics.
 *
 * This query type searches for laboratory results using patient demographic information
 * as the primary search criteria. It's the most commonly used query type for retrieving
 * patient-specific laboratory results from OLIS.
 *
 * The Z01 query supports a comprehensive set of search parameters including:
 * - Patient identifiers (health card number, MRN, etc.)
 * - Date ranges for specimen collection or result reporting
 * - Specific test codes or result types
 * - Provider filters (ordering, attending, admitting)
 * - Laboratory filters (performing, reporting)
 * - Result status filters
 *
 * Mandatory parameters:
 * - Patient identifier (PID3) - At least one patient identifier
 * - Requesting HIC (ZRP1) - Health Information Custodian identifier
 *
 * The query results include all matching laboratory reports for the specified patient
 * within the given criteria, subject to privacy and consent restrictions.
 *
 * @since 2008-01-01
 */
public class Z01Query extends Query {

    /**
     * Start and end timestamp for the query date range.
     * Defines the time period for which results should be retrieved.
     */
    private OBR22 startEndTimestamp = null;

    /**
     * Earliest and latest observation date/time filter.
     * Limits results to observations within this time window.
     */
    private OBR7 earliestLatestObservationDateTime = null;

    /**
     * Quantity limited request parameter.
     * Specifies the maximum number of results to return.
     */
    private QRD7 quantityLimitedRequest = null;

    /**
     * Requesting Health Information Custodian identifier (mandatory).
     * Identifies the healthcare facility or organization making the request.
     */
    private ZRP1 requestingHic = new ZRP1();

    /**
     * Consent parameter for viewing blocked information.
     * Required when accessing privacy-protected results.
     */
    private ZPD1 consentToViewBlockedInformation = null;

    /**
     * Patient consent block all indicator.
     * Indicates if patient has blocked all information sharing.
     */
    private ZPD3 patientConsentBlockAllIndicator = null;

    /**
     * Specimen collector filter.
     * Limits results to specimens collected by specific providers.
     */
    private ZBR3 specimenCollector = null;

    /**
     * Performing laboratory filter.
     * Limits results to tests performed by specific laboratories.
     */
    private ZBR6 performingLaboratory = null;

    /**
     * Exclude performing laboratory filter.
     * Excludes results from specified laboratories.
     */
    private ZBE6 excludePerformingLaboratory = null;

    /**
     * Reporting laboratory filter.
     * Limits results to reports from specific laboratories.
     */
    private ZBR4 reportingLaboratory = null;

    /**
     * Exclude reporting laboratory filter.
     * Excludes reports from specified laboratories.
     */
    private ZBE4 excludeReportingLaboratory = null;

    /**
     * Patient identifier (mandatory).
     * Primary patient identification for the query.
     */
    private PID3 patientIdentifier = new PID3();

    /**
     * Ordering practitioner filter.
     * Limits results to tests ordered by specific providers.
     */
    private OBR16 orderingPractitioner = null;

    /**
     * Copied-to practitioner filter.
     * Limits results to those copied to specific providers.
     */
    private OBR28 copiedToPractitioner = null;

    /**
     * Attending practitioner filter.
     * Limits results to specific attending physicians.
     */
    private PV17 attendingPractitioner = null;

    /**
     * Admitting practitioner filter.
     * Limits results to specific admitting physicians.
     */
    private PV117 admittingPractitioner = null;

    /**
     * Test result placer filter.
     * Limits results to specific order placers.
     */
    private ZBR2 testResultPlacer = null;

    /**
     * List of test request codes to filter by.
     * Limits results to specific test types.
     */
    private List<OBR4> testRequestCodeList = new LinkedList<OBR4>();

    /**
     * Placer group number filter.
     * Groups related test orders together.
     */
    private ORC4 placerGroupNumber = null;

    /**
     * List of test request status filters.
     * Limits results to specific status values.
     */
    private List<OBR25> testRequestStatusList = new LinkedList<OBR25>();

    /**
     * List of test result codes to filter by.
     * Limits results to specific result types.
     */
    private List<OBX3> testResultCodeList = new LinkedList<OBX3>();

    /**
     * Generates the HL7 query string for the Z01 query.
     *
     * Constructs the query parameter string by concatenating all non-null
     * parameters in the proper sequence, separated by "~" delimiters.
     * The order of parameters is significant and must follow OLIS specifications.
     *
     * @return String the formatted HL7 query parameter string
     * @since 2008-01-01
     */
    @Override
    public String getQueryHL7String() {
        String query = "";

        // Add time range filters
        if (startEndTimestamp != null)
            query += startEndTimestamp.toOlisString() + "~";

        if (earliestLatestObservationDateTime != null)
            query += earliestLatestObservationDateTime.toOlisString() + "~";

        // Add result quantity limit
        if (quantityLimitedRequest != null)
            query += quantityLimitedRequest.toOlisString() + "~";

        // Add mandatory requesting HIC
        if (requestingHic != null)
            query += requestingHic.toOlisString() + "~";

        // Add consent parameters
        if (consentToViewBlockedInformation != null)
            query += consentToViewBlockedInformation.toOlisString() + "~";

        if (patientConsentBlockAllIndicator != null)
            query += patientConsentBlockAllIndicator.toOlisString() + "~";

        // Add laboratory filters
        if (specimenCollector != null)
            query += specimenCollector.toOlisString() + "~";

        if (performingLaboratory != null)
            query += performingLaboratory.toOlisString() + "~";

        if (excludePerformingLaboratory != null)
            query += excludePerformingLaboratory.toOlisString() + "~";

        if (reportingLaboratory != null)
            query += reportingLaboratory.toOlisString() + "~";

        if (excludeReportingLaboratory != null)
            query += excludeReportingLaboratory.toOlisString() + "~";

        // Add mandatory patient identifier
        if (patientIdentifier != null)
            query += patientIdentifier.toOlisString() + "~";

        // Add practitioner filters
        if (orderingPractitioner != null)
            query += orderingPractitioner.toOlisString() + "~";

        if (copiedToPractitioner != null)
            query += copiedToPractitioner.toOlisString() + "~";

        if (attendingPractitioner != null)
            query += attendingPractitioner.toOlisString() + "~";

        if (admittingPractitioner != null)
            query += admittingPractitioner.toOlisString() + "~";

        if (testResultPlacer != null)
            query += testResultPlacer.toOlisString() + "~";

        // Add test code filters
        for (OBR4 testRequestCode : testRequestCodeList) {
            query += testRequestCode.toOlisString() + "~";
        }

        if (placerGroupNumber != null)
            query += placerGroupNumber.toOlisString() + "~";

        // Add status filters
        for (OBR25 testRequestStatus : testRequestStatusList) {
            query += testRequestStatus.toOlisString() + "~";
        }

        // Add result code filters
        for (OBX3 testResultCode : testResultCodeList) {
            query += testResultCode.toOlisString() + "~";
        }

        // Remove trailing delimiter
        if (query.endsWith("~")) {
            query = query.substring(0, query.length() - 1);
        }

        return query;
    }

    /**
     * Sets the start and end timestamp for the query date range.
     *
     * @param startEndTimestamp OBR22 the date range parameter
     * @since 2008-01-01
     */
    public void setStartEndTimestamp(OBR22 startEndTimestamp) {
        this.startEndTimestamp = startEndTimestamp;
    }

    /**
     * Sets the earliest and latest observation date/time filter.
     *
     * @param earliestLatestObservationDateTime OBR7 the observation date range
     * @since 2008-01-01
     */
    public void setEarliestLatestObservationDateTime(OBR7 earliestLatestObservationDateTime) {
        this.earliestLatestObservationDateTime = earliestLatestObservationDateTime;
    }

    /**
     * Sets the quantity limit for the number of results to return.
     *
     * @param quantityLimitedRequest QRD7 the quantity limit parameter
     * @since 2008-01-01
     */
    public void setQuantityLimitedRequest(QRD7 quantityLimitedRequest) {
        this.quantityLimitedRequest = quantityLimitedRequest;
    }

    /**
     * Sets the requesting Health Information Custodian identifier.
     *
     * This is a mandatory parameter that identifies the healthcare facility
     * or organization making the request.
     *
     * @param requestingHic ZRP1 the HIC identifier parameter
     * @since 2008-01-01
     */
    public void setRequestingHic(ZRP1 requestingHic) {
        this.requestingHic = requestingHic;
    }

    /**
     * Sets the consent parameter for viewing blocked information.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter
     * @since 2008-01-01
     */
    public void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation) {
        this.consentToViewBlockedInformation = consentToViewBlockedInformation;
    }

    /**
     * Sets the patient consent block all indicator.
     *
     * @param patientConsentBlockAllIndicator ZPD3 the block all indicator
     * @since 2008-01-01
     */
    public void setPatientConsentBlockAllIndicator(ZPD3 patientConsentBlockAllIndicator) {
        this.patientConsentBlockAllIndicator = patientConsentBlockAllIndicator;
    }

    /**
     * Sets the specimen collector filter.
     *
     * @param specimenCollector ZBR3 the specimen collector parameter
     * @since 2008-01-01
     */
    public void setSpecimenCollector(ZBR3 specimenCollector) {
        this.specimenCollector = specimenCollector;
    }

    /**
     * Sets the performing laboratory filter.
     *
     * @param performingLaboratory ZBR6 the performing laboratory parameter
     * @since 2008-01-01
     */
    public void setPerformingLaboratory(ZBR6 performingLaboratory) {
        this.performingLaboratory = performingLaboratory;
    }

    /**
     * Sets the exclude performing laboratory filter.
     *
     * @param excludePerformingLaboratory ZBE6 the exclusion parameter
     * @since 2008-01-01
     */
    public void setExcludePerformingLaboratory(ZBE6 excludePerformingLaboratory) {
        this.excludePerformingLaboratory = excludePerformingLaboratory;
    }

    /**
     * Sets the reporting laboratory filter.
     *
     * @param reportingLaboratory ZBR4 the reporting laboratory parameter
     * @since 2008-01-01
     */
    public void setReportingLaboratory(ZBR4 reportingLaboratory) {
        this.reportingLaboratory = reportingLaboratory;
    }

    /**
     * Sets the exclude reporting laboratory filter.
     *
     * @param excludeReportingLaboratory ZBE4 the exclusion parameter
     * @since 2008-01-01
     */
    public void setExcludeReportingLaboratory(ZBE4 excludeReportingLaboratory) {
        this.excludeReportingLaboratory = excludeReportingLaboratory;
    }

    /**
     * Sets the patient identifier.
     *
     * This is a mandatory parameter that identifies the patient whose
     * laboratory results are being queried.
     *
     * @param patientIdentifier PID3 the patient identifier parameter
     * @since 2008-01-01
     */
    public void setPatientIdentifier(PID3 patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    /**
     * Sets the ordering practitioner filter.
     *
     * @param orderingPractitioner OBR16 the ordering provider parameter
     * @since 2008-01-01
     */
    public void setOrderingPractitioner(OBR16 orderingPractitioner) {
        this.orderingPractitioner = orderingPractitioner;
    }

    /**
     * Sets the copied-to practitioner filter.
     *
     * @param copiedToPractitioner OBR28 the copied-to provider parameter
     * @since 2008-01-01
     */
    public void setCopiedToPractitioner(OBR28 copiedToPractitioner) {
        this.copiedToPractitioner = copiedToPractitioner;
    }

    /**
     * Sets the attending practitioner filter.
     *
     * @param attendingPractitioner PV17 the attending physician parameter
     * @since 2008-01-01
     */
    public void setAttendingPractitioner(PV17 attendingPractitioner) {
        this.attendingPractitioner = attendingPractitioner;
    }

    /**
     * Sets the admitting practitioner filter.
     *
     * @param admittingPractitioner PV117 the admitting physician parameter
     * @since 2008-01-01
     */
    public void setAdmittingPractitioner(PV117 admittingPractitioner) {
        this.admittingPractitioner = admittingPractitioner;
    }

    /**
     * Sets the test result placer filter.
     *
     * @param testResultPlacer ZBR2 the placer parameter
     * @since 2008-01-01
     */
    public void setTestResultPlacer(ZBR2 testResultPlacer) {
        this.testResultPlacer = testResultPlacer;
    }

    /**
     * Sets the placer group number filter.
     *
     * @param placerGroupNumber ORC4 the group number parameter
     * @since 2008-01-01
     */
    public void setPlacerGroupNumber(ORC4 placerGroupNumber) {
        this.placerGroupNumber = placerGroupNumber;
    }

    /**
     * Adds a test request code to the filter list.
     *
     * Multiple test codes can be added to search for specific test types.
     *
     * @param testRequestCode OBR4 the test code to add
     * @since 2008-01-01
     */
    public void addToTestRequestCodeList(OBR4 testRequestCode) {
        this.testRequestCodeList.add(testRequestCode);
    }

    /**
     * Adds a test request status to the filter list.
     *
     * Multiple status values can be added to filter by result status.
     *
     * @param testRequestStatus OBR25 the status to add
     * @since 2008-01-01
     */
    public void addToTestRequestStatusList(OBR25 testRequestStatus) {
        this.testRequestStatusList.add(testRequestStatus);
    }

    /**
     * Adds a test result code to the filter list.
     *
     * Multiple result codes can be added to search for specific result types.
     *
     * @param testResultCode OBX3 the result code to add
     * @since 2008-01-01
     */
    public void addToTestResultCodeList(OBX3 testResultCode) {
        this.testResultCodeList.add(testResultCode);
    }

    /**
     * Returns the query type identifier for this query.
     *
     * @return QueryType.Z01 indicating this is a patient demographics query
     * @since 2008-01-01
     */
    @Override
    public QueryType getQueryType() {
        return QueryType.Z01;
    }

    /**
     * Gets the requesting Health Information Custodian ID number.
     *
     * Extracts the HIC identifier from the ZRP1 parameter for use in
     * audit logging and authorization checks.
     *
     * @return String the HIC identifier number
     * @since 2008-01-01
     */
    public String getRequestingHicIdNumber() {
        return requestingHic.getIdNumber();
    }
}
