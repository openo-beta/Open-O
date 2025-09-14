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

package ca.openosp.openo.entities;

/**
 * Healthcare Laboratory Observation Request (HL7 OBR) segment entity.
 *
 * This entity represents the HL7 v2.x OBR (Observation Request) segment, which contains information
 * about laboratory test orders and requests. The OBR segment is a critical component in healthcare
 * messaging for communicating laboratory order details between healthcare systems.
 *
 * Key clinical information stored includes:
 * - Order identification (placer and filler order numbers)
 * - Service/test identification and priority
 * - Specimen collection and processing details
 * - Provider information and callback contacts
 * - Test timing and scheduling information
 * - Result status and reporting details
 *
 * This entity follows HL7 v2.x messaging standards and is used for laboratory information
 * system integration, supporting both inbound and outbound lab order processing.
 *
 * @see <a href="http://www.hl7.org/implement/standards/product_brief.cfm?product_id=185">HL7 v2.x Standard</a>
 * @see Hl7Obx for related observation results
 * @see Hl7Orc for common order segment
 * @since November 1, 2004
 */
public class Hl7Obr {
    /**
     * auto_increment
     */
    private int obrId;
    private int pidId;
    private String setId;
    private String placerOrderNumber;
    private String fillerOrderNumber;
    private String universalServiceId;
    private String priority;
    private String requestedDateTime;
    private String observationDateTime;
    private String observationEndDateTime;
    private String collectionVolume;
    private String collectorIdentifier;
    private String specimenActionCode;
    private String dangerCode;
    private String relevantClinicalInfo;
    private String specimenReceivedDateTime;
    private String specimenSource;
    private String orderingProvider;
    private String orderCallbackPhoneNumber;
    private String placersField1;
    private String palcersField2;
    private String fillerField1;
    private String fillerField2;
    private String resultsReportStatusChange;
    private String chargeToPractice;
    private String diagnosticServiceSectId;
    private String resultStatus;
    private String parentResult;
    private String quantityTiming;
    private String resultCopiesTo;
    private String parentNumber;
    private String transportationMode;
    private String reasonForStudy;
    private String principalResultInterpreter;
    private String assistantResultInterpreter;
    private String technician;
    private String transcriptionist;
    private String scheduledDateTime;
    private String transportArranged;
    private String numberOfSampleContainers;
    private String transportLogisticsOfCollectedSample;
    private String collectorComment;
    private String transportArrangementResponsibility;
    private String escortRequired;
    private String plannedPatientTransportComment;
    private String note;

    /**
     * Default constructor for HL7 OBR (Observation Request) segment entity.
     * Initializes all fields to their default values.
     */
    public Hl7Obr() {
    }

    /**
     * Complete constructor for HL7 OBR (Observation Request) segment entity.
     * Creates a fully initialized OBR segment with all HL7 standard fields.
     *
     * @param obrId                               int unique identifier for this OBR record
     * @param pidId                               int patient identifier linking to PID segment
     * @param setId                               String set ID for OBR segment (OBR.1)
     * @param placerOrderNumber                   String order number assigned by placer (OBR.2)
     * @param fillerOrderNumber                   String order number assigned by filler (OBR.3)
     * @param universalServiceId                  String universal service identifier (OBR.4)
     * @param priority                            String order priority (S=STAT, A=ASAP, R=Routine) (OBR.5)
     * @param requestedDateTime                   String date/time order was requested (OBR.6)
     * @param observationDateTime                 String date/time of observation (OBR.7)
     * @param observationEndDateTime              String end date/time of observation (OBR.8)
     * @param collectionVolume                    String specimen collection volume (OBR.9)
     * @param collectorIdentifier                 String identifier of specimen collector (OBR.10)
     * @param specimenActionCode                  String specimen action code (OBR.11)
     * @param dangerCode                          String danger/hazard code for specimen (OBR.12)
     * @param relevantClinicalInfo                String relevant clinical information (OBR.13)
     * @param specimenReceivedDateTime            String date/time specimen was received (OBR.14)
     * @param specimenSource                      String source of specimen (OBR.15)
     * @param orderingProvider                    String ordering provider information (OBR.16)
     * @param orderCallbackPhoneNumber            String callback phone number (OBR.17)
     * @param placersField1                       String placer field 1 (OBR.18)
     * @param palcersField2                       String placer field 2 (OBR.19) [Note: typo in original field name]
     * @param fillerField1                        String filler field 1 (OBR.20)
     * @param fillerField2                        String filler field 2 (OBR.21)
     * @param resultsReportStatusChange           String results report status change (OBR.22)
     * @param chargeToPractice                    String charge to practice indicator (OBR.23)
     * @param diagnosticServiceSectId             String diagnostic service section ID (OBR.24)
     * @param resultStatus                        String result status (OBR.25)
     * @param parentResult                        String parent result reference (OBR.26)
     * @param quantityTiming                      String quantity/timing information (OBR.27)
     * @param resultCopiesTo                      String result copies to (OBR.28)
     * @param parentNumber                        String parent order number (OBR.29)
     * @param transportationMode                  String transportation mode (OBR.30)
     * @param reasonForStudy                      String reason for study (OBR.31)
     * @param principalResultInterpreter          String principal result interpreter (OBR.32)
     * @param assistantResultInterpreter          String assistant result interpreter (OBR.33)
     * @param technician                          String technician information (OBR.34)
     * @param transcriptionist                    String transcriptionist information (OBR.35)
     * @param scheduledDateTime                   String scheduled date/time (OBR.36)
     * @param transportArranged                   String transport arrangement status (OBR.37)
     * @param numberOfSampleContainers            String number of sample containers (OBR.38)
     * @param transportLogisticsOfCollectedSample String transport logistics details (OBR.39)
     * @param collectorComment                    String collector comment (OBR.40)
     * @param transportArrangementResponsibility  String transport arrangement responsibility (OBR.41)
     * @param escortRequired                      String escort required indicator (OBR.42)
     * @param plannedPatientTransportComment      String planned patient transport comment (OBR.43)
     * @param note                                String additional notes or comments
     */
    public Hl7Obr(int obrId, int pidId, String setId, String placerOrderNumber,
                  String fillerOrderNumber, String universalServiceId,
                  String priority, String requestedDateTime,
                  String observationDateTime,
                  String observationEndDateTime,
                  String collectionVolume, String collectorIdentifier,
                  String specimenActionCode, String dangerCode,
                  String relevantClinicalInfo,
                  String specimenReceivedDateTime,
                  String specimenSource, String orderingProvider,
                  String orderCallbackPhoneNumber, String placersField1,
                  String palcersField2, String fillerField1, String fillerField2,
                  String resultsReportStatusChange,
                  String chargeToPractice, String diagnosticServiceSectId,
                  String resultStatus, String parentResult, String quantityTiming,
                  String resultCopiesTo, String parentNumber,
                  String transportationMode, String reasonForStudy,
                  String principalResultInterpreter,
                  String assistantResultInterpreter, String technician,
                  String transcriptionist, String scheduledDateTime
            , String transportArranged, String numberOfSampleContainers,
                  String transportLogisticsOfCollectedSample,
                  String collectorComment,
                  String transportArrangementResponsibility,
                  String escortRequired, String plannedPatientTransportComment,
                  String note) {
        this.obrId = obrId;
        this.pidId = pidId;
        this.setId = setId;
        this.placerOrderNumber = placerOrderNumber;
        this.fillerOrderNumber = fillerOrderNumber;
        this.universalServiceId = universalServiceId;
        this.priority = priority;
        this.requestedDateTime = requestedDateTime;
        this.observationDateTime = observationDateTime;
        this.observationEndDateTime = observationEndDateTime;
        this.collectionVolume = collectionVolume;
        this.collectorIdentifier = collectorIdentifier;
        this.specimenActionCode = specimenActionCode;
        this.dangerCode = dangerCode;
        this.relevantClinicalInfo = relevantClinicalInfo;
        this.specimenReceivedDateTime = specimenReceivedDateTime;
        this.specimenSource = specimenSource;
        this.orderingProvider = orderingProvider;
        this.orderCallbackPhoneNumber = orderCallbackPhoneNumber;
        this.placersField1 = placersField1;
        this.palcersField2 = palcersField2;
        this.fillerField1 = fillerField1;
        this.fillerField2 = fillerField2;
        this.resultsReportStatusChange = resultsReportStatusChange;
        this.chargeToPractice = chargeToPractice;
        this.diagnosticServiceSectId = diagnosticServiceSectId;
        this.resultStatus = resultStatus;
        this.parentResult = parentResult;
        this.quantityTiming = quantityTiming;
        this.resultCopiesTo = resultCopiesTo;
        this.parentNumber = parentNumber;
        this.transportationMode = transportationMode;
        this.reasonForStudy = reasonForStudy;
        this.principalResultInterpreter = principalResultInterpreter;
        this.assistantResultInterpreter = assistantResultInterpreter;
        this.technician = technician;
        this.transcriptionist = transcriptionist;
        this.scheduledDateTime = scheduledDateTime;
        this.transportArranged = transportArranged;
        this.numberOfSampleContainers = numberOfSampleContainers;
        this.transportLogisticsOfCollectedSample =
                transportLogisticsOfCollectedSample;
        this.collectorComment = collectorComment;
        this.transportArrangementResponsibility =
                transportArrangementResponsibility;
        this.escortRequired = escortRequired;
        this.plannedPatientTransportComment = plannedPatientTransportComment;
        this.note = note;
    }

    /**
     * Gets the unique OBR record identifier.
     * This is the database primary key for this OBR segment record.
     *
     * @return int the unique OBR record identifier
     */
    public int getObrId() {
        return obrId;
    }

    /**
     * Gets the patient identifier linking this OBR to the corresponding PID segment.
     * This creates the relationship between the observation request and the patient.
     *
     * @return int the patient identifier from the related PID segment
     */
    public int getPidId() {
        return pidId;
    }

    /**
     * Gets the set ID for this OBR segment (HL7 field OBR.1).
     * Used to identify different instances of OBR segments within a message.
     *
     * @return String the set ID, empty string if null
     */
    public String getSetId() {
        return (setId != null ? setId : "");
    }

    /**
     * Gets the placer order number (HL7 field OBR.2).
     * This is the order number assigned by the entity placing the order (typically the EMR).
     *
     * @return String the placer order number, empty string if null
     */
    public String getPlacerOrderNumber() {
        return (placerOrderNumber != null ? placerOrderNumber : "");
    }

    /**
     * Gets the filler order number (HL7 field OBR.3).
     * This is the order number assigned by the entity fulfilling the order (typically the lab).
     *
     * @return String the filler order number, empty string if null
     */
    public String getFillerOrderNumber() {
        return (fillerOrderNumber != null ? fillerOrderNumber : "");
    }

    /**
     * Gets the universal service identifier (HL7 field OBR.4).
     * This identifies the specific test or service being requested, often using LOINC codes.
     *
     * @return String the universal service identifier, empty string if null
     */
    public String getUniversalServiceId() {
        return (universalServiceId != null ? universalServiceId : "");
    }

    /**
     * Gets the order priority (HL7 field OBR.5).
     * Common values: S=STAT (urgent), A=ASAP (as soon as possible), R=Routine.
     *
     * @return String the order priority code, empty string if null
     */
    public String getPriority() {
        return (priority != null ? priority : "");
    }

    /**
     * Gets the requested date/time (HL7 field OBR.6).
     * The date and time when the order was requested.
     *
     * @return String the requested date/time, may be null
     */
    public String getRequestedDateTime() {
        return requestedDateTime;
    }

    /**
     * Gets the observation date/time (HL7 field OBR.7).
     * The date and time when the observation/test was performed.
     *
     * @return String the observation date/time, may be null
     */
    public String getObservationDateTime() {
        return observationDateTime;
    }

    /**
     * Gets the observation end date/time (HL7 field OBR.8).
     * The date and time when the observation/test was completed.
     *
     * @return String the observation end date/time, may be null
     */
    public String getObservationEndDateTime() {
        return observationEndDateTime;
    }

    /**
     * Gets the specimen collection volume (HL7 field OBR.9).
     * The volume of specimen to be collected for the test.
     *
     * @return String the collection volume, empty string if null
     */
    public String getCollectionVolume() {
        return (collectionVolume != null ? collectionVolume : "");
    }

    /**
     * Gets the collector identifier (HL7 field OBR.10).
     * Identifies the person or entity who collected the specimen.
     *
     * @return String the collector identifier, empty string if null
     */
    public String getCollectorIdentifier() {
        return (collectorIdentifier != null ? collectorIdentifier : "");
    }

    /**
     * Gets the specimen action code (HL7 field OBR.11).
     * Indicates the action to be taken with the specimen (e.g., A=Add, G=Generated).
     *
     * @return String the specimen action code, empty string if null
     */
    public String getSpecimenActionCode() {
        return (specimenActionCode != null ? specimenActionCode : "");
    }

    /**
     * Gets the danger code (HL7 field OBR.12).
     * Indicates any hazards or special precautions for handling the specimen.
     *
     * @return String the danger code, empty string if null
     */
    public String getDangerCode() {
        return (dangerCode != null ? dangerCode : "");
    }

    /**
     * Gets the relevant clinical information (HL7 field OBR.13).
     * Clinical information relevant to the interpretation of the test results.
     *
     * @return String the relevant clinical information, empty string if null
     */
    public String getRelevantClinicalInfo() {
        return (relevantClinicalInfo != null ? relevantClinicalInfo : "");
    }

    /**
     * Gets the specimenReceivedDateTime
     *
     * @return String specimenReceivedDateTime
     */
    public String getSpecimenReceivedDateTime() {
        return specimenReceivedDateTime;
    }

    /**
     * Gets the specimenSource
     *
     * @return String specimenSource
     */
    public String getSpecimenSource() {
        return (specimenSource != null ? specimenSource : "");
    }

    /**
     * Gets the orderingProvider
     *
     * @return String orderingProvider
     */
    public String getOrderingProvider() {
        return (orderingProvider != null ? orderingProvider : "");
    }

    /**
     * Gets the orderCallbackPhoneNumber
     *
     * @return String orderCallbackPhoneNumber
     */
    public String getOrderCallbackPhoneNumber() {
        return (orderCallbackPhoneNumber != null ? orderCallbackPhoneNumber : "");
    }

    /**
     * Gets the placersField1
     *
     * @return String placersField1
     */
    public String getPlacersField1() {
        return (placersField1 != null ? placersField1 : "");
    }

    /**
     * Gets the palcersField2
     *
     * @return String palcersField2
     */
    public String getPalcersField2() {
        return (palcersField2 != null ? palcersField2 : "");
    }

    /**
     * Gets the fillerField1
     *
     * @return String fillerField1
     */
    public String getFillerField1() {
        return (fillerField1 != null ? fillerField1 : "");
    }

    /**
     * Gets the fillerField2
     *
     * @return String fillerField2
     */
    public String getFillerField2() {
        return (fillerField2 != null ? fillerField2 : "");
    }

    /**
     * Gets the resultsReportStatusChange
     *
     * @return String resultsReportStatusChange
     */
    public String getResultsReportStatusChange() {
        return resultsReportStatusChange;
    }

    /**
     * Gets the chargeToPractice
     *
     * @return String chargeToPractice
     */
    public String getChargeToPractice() {
        return (chargeToPractice != null ? chargeToPractice : "");
    }

    /**
     * Gets the diagnosticServiceSectId
     *
     * @return String diagnosticServiceSectId
     */
    public String getDiagnosticServiceSectId() {
        return (diagnosticServiceSectId != null ? diagnosticServiceSectId : "");
    }

    /**
     * Gets the resultStatus
     *
     * @return String resultStatus
     */
    public String getResultStatus() {
        return (resultStatus != null ? resultStatus : "");
    }

    /**
     * Gets the parentResult
     *
     * @return String parentResult
     */
    public String getParentResult() {
        return (parentResult != null ? parentResult : "");
    }

    /**
     * Gets the quantityTiming
     *
     * @return String quantityTiming
     */
    public String getQuantityTiming() {
        return (quantityTiming != null ? quantityTiming : "");
    }

    /**
     * Gets the resultCopiesTo
     *
     * @return String resultCopiesTo
     */
    public String getResultCopiesTo() {
        return (resultCopiesTo != null ? resultCopiesTo : "");
    }

    /**
     * Gets the parentNumber
     *
     * @return String parentNumber
     */
    public String getParentNumber() {
        return (parentNumber != null ? parentNumber : "");
    }

    /**
     * Gets the transportationMode
     *
     * @return String transportationMode
     */
    public String getTransportationMode() {
        return (transportationMode != null ? transportationMode : "");
    }

    /**
     * Gets the reasonForStudy
     *
     * @return String reasonForStudy
     */
    public String getReasonForStudy() {
        return (reasonForStudy != null ? reasonForStudy : "");
    }

    /**
     * Gets the principalResultInterpreter
     *
     * @return String principalResultInterpreter
     */
    public String getPrincipalResultInterpreter() {
        return (principalResultInterpreter != null ? principalResultInterpreter :
                "");
    }

    /**
     * Gets the assistantResultInterpreter
     *
     * @return String assistantResultInterpreter
     */
    public String getAssistantResultInterpreter() {
        return (assistantResultInterpreter != null ? assistantResultInterpreter :
                "");
    }

    /**
     * Gets the technician
     *
     * @return String technician
     */
    public String getTechnician() {
        return (technician != null ? technician : "");
    }

    /**
     * Gets the transcriptionist
     *
     * @return String transcriptionist
     */
    public String getTranscriptionist() {
        return (transcriptionist != null ? transcriptionist : "");
    }

    /**
     * Gets the scheduledDateTime
     *
     * @return String scheduledDateTime
     */
    public String getScheduledDateTime() {
        return scheduledDateTime;
    }

    /**
     * Gets the transportArranged
     *
     * @return String transportArranged
     */
    public String getTransportArranged() {
        return (transportArranged != null ? transportArranged : "");
    }

    /**
     * Gets the numberOfSampleContainers
     *
     * @return String numberOfSampleContainers
     */
    public String getNumberOfSampleContainers() {
        return (numberOfSampleContainers != null ? numberOfSampleContainers : "");
    }

    /**
     * Gets the transportLogisticsOfCollectedSample
     *
     * @return String transportLogisticsOfCollectedSample
     */
    public String getTransportLogisticsOfCollectedSample() {
        return (transportLogisticsOfCollectedSample != null ?
                transportLogisticsOfCollectedSample : "");
    }

    /**
     * Gets the collectorComment
     *
     * @return String collectorComment
     */
    public String getCollectorComment() {
        return (collectorComment != null ? collectorComment : "");
    }

    /**
     * Gets the transportArrangementResponsibility
     *
     * @return String transportArrangementResponsibility
     */
    public String getTransportArrangementResponsibility() {
        return (transportArrangementResponsibility != null ?
                transportArrangementResponsibility : "");
    }

    /**
     * Gets the escortRequired
     *
     * @return String escortRequired
     */
    public String getEscortRequired() {
        return (escortRequired != null ? escortRequired : "");
    }

    /**
     * Gets the plannedPatientTransportComment
     *
     * @return String plannedPatientTransportComment
     */
    public String getPlannedPatientTransportComment() {
        return (plannedPatientTransportComment != null ?
                plannedPatientTransportComment : "");
    }

    /**
     * Gets the note
     *
     * @return String note
     */
    public String getNote() {
        return (note != null ? note : "");
    }

    /**
     * Sets the unique OBR record identifier.
     * This is the database primary key for this OBR segment record.
     *
     * @param obrId int the unique OBR record identifier
     */
    public void setObrId(int obrId) {
        this.obrId = obrId;
    }

    /**
     * Sets the patient identifier linking this OBR to the corresponding PID segment.
     * This creates the relationship between the observation request and the patient.
     *
     * @param pidId int the patient identifier from the related PID segment
     */
    public void setPidId(int pidId) {
        this.pidId = pidId;
    }

    /**
     * Sets the setId
     *
     * @param setId String
     */
    public void setSetId(String setId) {
        this.setId = setId;
    }

    /**
     * Sets the placerOrderNumber
     *
     * @param placerOrderNumber String
     */
    public void setPlacerOrderNumber(String placerOrderNumber) {
        this.placerOrderNumber = placerOrderNumber;
    }

    /**
     * Sets the fillerOrderNumber
     *
     * @param fillerOrderNumber String
     */
    public void setFillerOrderNumber(String fillerOrderNumber) {
        this.fillerOrderNumber = fillerOrderNumber;
    }

    /**
     * Sets the universalServiceId
     *
     * @param universalServiceId String
     */
    public void setUniversalServiceId(String universalServiceId) {
        this.universalServiceId = universalServiceId;
    }

    /**
     * Sets the priority
     *
     * @param priority String
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Sets the requestedDateTime
     *
     * @param requestedDateTime String
     */
    public void setRequestedDateTime(String requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
    }

    /**
     * Sets the observationDateTime
     *
     * @param observationDateTime String
     */
    public void setObservationDateTime(String observationDateTime) {
        this.observationDateTime = observationDateTime;
    }

    /**
     * Sets the observationEndDateTime
     *
     * @param observationEndDateTime String
     */
    public void setObservationEndDateTime(String
                                                  observationEndDateTime) {
        this.observationEndDateTime = observationEndDateTime;
    }

    /**
     * Sets the collectionVolume
     *
     * @param collectionVolume String
     */
    public void setCollectionVolume(String collectionVolume) {
        this.collectionVolume = collectionVolume;
    }

    /**
     * Sets the collectorIdentifier
     *
     * @param collectorIdentifier String
     */
    public void setCollectorIdentifier(String collectorIdentifier) {
        this.collectorIdentifier = collectorIdentifier;
    }

    /**
     * Sets the specimenActionCode
     *
     * @param specimenActionCode String
     */
    public void setSpecimenActionCode(String specimenActionCode) {
        this.specimenActionCode = specimenActionCode;
    }

    /**
     * Sets the dangerCode
     *
     * @param dangerCode String
     */
    public void setDangerCode(String dangerCode) {
        this.dangerCode = dangerCode;
    }

    /**
     * Sets the relevantClinicalInfo
     *
     * @param relevantClinicalInfo String
     */
    public void setRelevantClinicalInfo(String relevantClinicalInfo) {
        this.relevantClinicalInfo = relevantClinicalInfo;
    }

    /**
     * Sets the specimenReceivedDateTime
     *
     * @param specimenReceivedDateTime String
     */
    public void setSpecimenReceivedDateTime(String
                                                    specimenReceivedDateTime) {
        this.specimenReceivedDateTime = specimenReceivedDateTime;
    }

    /**
     * Sets the specimenSource
     *
     * @param specimenSource String
     */
    public void setSpecimenSource(String specimenSource) {
        this.specimenSource = specimenSource;
    }

    /**
     * Sets the orderingProvider
     *
     * @param orderingProvider String
     */
    public void setOrderingProvider(String orderingProvider) {
        this.orderingProvider = orderingProvider;
    }

    /**
     * Sets the orderCallbackPhoneNumber
     *
     * @param orderCallbackPhoneNumber String
     */
    public void setOrderCallbackPhoneNumber(String orderCallbackPhoneNumber) {
        this.orderCallbackPhoneNumber = orderCallbackPhoneNumber;
    }

    /**
     * Sets the placersField1
     *
     * @param placersField1 String
     */
    public void setPlacersField1(String placersField1) {
        this.placersField1 = placersField1;
    }

    /**
     * Sets the palcersField2
     *
     * @param palcersField2 String
     */
    public void setPalcersField2(String palcersField2) {
        this.palcersField2 = palcersField2;
    }

    /**
     * Sets the fillerField1
     *
     * @param fillerField1 String
     */
    public void setFillerField1(String fillerField1) {
        this.fillerField1 = fillerField1;
    }

    /**
     * Sets the fillerField2
     *
     * @param fillerField2 String
     */
    public void setFillerField2(String fillerField2) {
        this.fillerField2 = fillerField2;
    }

    /**
     * Sets the resultsReportStatusChange
     *
     * @param resultsReportStatusChange String
     */
    public void setResultsReportStatusChange(String
                                                     resultsReportStatusChange) {
        this.resultsReportStatusChange = resultsReportStatusChange;
    }

    /**
     * Sets the chargeToPractice
     *
     * @param chargeToPractice String
     */
    public void setChargeToPractice(String chargeToPractice) {
        this.chargeToPractice = chargeToPractice;
    }

    /**
     * Sets the diagnosticServiceSectId
     *
     * @param diagnosticServiceSectId String
     */
    public void setDiagnosticServiceSectId(String diagnosticServiceSectId) {
        this.diagnosticServiceSectId = diagnosticServiceSectId;
    }

    /**
     * Sets the resultStatus
     *
     * @param resultStatus String
     */
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    /**
     * Sets the parentResult
     *
     * @param parentResult String
     */
    public void setParentResult(String parentResult) {
        this.parentResult = parentResult;
    }

    /**
     * Sets the quantityTiming
     *
     * @param quantityTiming String
     */
    public void setQuantityTiming(String quantityTiming) {
        this.quantityTiming = quantityTiming;
    }

    /**
     * Sets the resultCopiesTo
     *
     * @param resultCopiesTo String
     */
    public void setResultCopiesTo(String resultCopiesTo) {
        this.resultCopiesTo = resultCopiesTo;
    }

    /**
     * Sets the parentNumber
     *
     * @param parentNumber String
     */
    public void setParentNumber(String parentNumber) {
        this.parentNumber = parentNumber;
    }

    /**
     * Sets the transportationMode
     *
     * @param transportationMode String
     */
    public void setTransportationMode(String transportationMode) {
        this.transportationMode = transportationMode;
    }

    /**
     * Sets the reasonForStudy
     *
     * @param reasonForStudy String
     */
    public void setReasonForStudy(String reasonForStudy) {
        this.reasonForStudy = reasonForStudy;
    }

    /**
     * Sets the principalResultInterpreter
     *
     * @param principalResultInterpreter String
     */
    public void setPrincipalResultInterpreter(String principalResultInterpreter) {
        this.principalResultInterpreter = principalResultInterpreter;
    }

    /**
     * Sets the assistantResultInterpreter
     *
     * @param assistantResultInterpreter String
     */
    public void setAssistantResultInterpreter(String assistantResultInterpreter) {
        this.assistantResultInterpreter = assistantResultInterpreter;
    }

    /**
     * Sets the technician
     *
     * @param technician String
     */
    public void setTechnician(String technician) {
        this.technician = technician;
    }

    /**
     * Sets the transcriptionist
     *
     * @param transcriptionist String
     */
    public void setTranscriptionist(String transcriptionist) {
        this.transcriptionist = transcriptionist;
    }

    /**
     * Sets the scheduledDateTime
     *
     * @param scheduledDateTime String
     */
    public void setScheduledDateTime(String scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }

    /**
     * Sets the transportArranged
     *
     * @param transportArranged String
     */
    public void setTransportArranged(String transportArranged) {
        this.transportArranged = transportArranged;
    }

    /**
     * Sets the numberOfSampleContainers
     *
     * @param numberOfSampleContainers String
     */
    public void setNumberOfSampleContainers(String numberOfSampleContainers) {
        this.numberOfSampleContainers = numberOfSampleContainers;
    }

    /**
     * Sets the transportLogisticsOfCollectedSample
     *
     * @param transportLogisticsOfCollectedSample String
     */
    public void setTransportLogisticsOfCollectedSample(String
                                                               transportLogisticsOfCollectedSample) {
        this.transportLogisticsOfCollectedSample =
                transportLogisticsOfCollectedSample;
    }

    /**
     * Sets the collectorComment
     *
     * @param collectorComment String
     */
    public void setCollectorComment(String collectorComment) {
        this.collectorComment = collectorComment;
    }

    /**
     * Sets the transportArrangementResponsibility
     *
     * @param transportArrangementResponsibility String
     */
    public void setTransportArrangementResponsibility(String
                                                              transportArrangementResponsibility) {
        this.transportArrangementResponsibility =
                transportArrangementResponsibility;
    }

    /**
     * Sets the escortRequired
     *
     * @param escortRequired String
     */
    public void setEscortRequired(String escortRequired) {
        this.escortRequired = escortRequired;
    }

    /**
     * Sets the plannedPatientTransportComment
     *
     * @param plannedPatientTransportComment String
     */
    public void setPlannedPatientTransportComment(String
                                                          plannedPatientTransportComment) {
        this.plannedPatientTransportComment = plannedPatientTransportComment;
    }

    /**
     * Sets the note
     *
     * @param note String
     */
    public void setNote(String note) {
        this.note = note;
    }

}
