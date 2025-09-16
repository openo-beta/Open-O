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
 * Healthcare Laboratory Observation Result (HL7 OBX) segment entity.
 *
 * This entity represents the HL7 v2.x OBX (Observation/Result) segment, which contains actual
 * laboratory test results, observations, and measurements. The OBX segment is paired with OBR
 * segments to provide complete laboratory information in healthcare messaging.
 *
 * Key clinical information stored includes:
 * - Test results and values with units of measurement
 * - Reference ranges and abnormal flags
 * - LOINC codes for standardized test identification
 * - Result status and observation methods
 * - Responsible observer and producer information
 * - Date/time stamps for observations
 *
 * This entity follows HL7 v2.x messaging standards and supports laboratory information
 * system integration for both inbound and outbound result processing.
 *
 * @see <a href="http://www.hl7.org/implement/standards/product_brief.cfm?product_id=185">HL7 v2.x Standard</a>
 * @see Hl7Obr for related observation requests
 * @see LoincCodes for LOINC code definitions
 * @since November 1, 2004
 */
public class Hl7Obx {
    /**
     * auto_increment
     */
    private int obxId;
    private int obrId;
    private String setId;
    private String valueType;
    private String observationIdentifier;
    private String loincNum;
    private String observationSubId;
    private String observationResults;
    private String units;
    private String referenceRange;
    private String abnormalFlags;
    private String probability;
    private String natureOfAbnormalTest;
    private String observationResultStatus;
    private String dateLastNormalValue;
    private String userDefinedAccessChecks;
    private String observationDateTime;
    private String producerId;
    private String responsibleObserver;
    private String observationMethod;
    private String note;

    /**
     * Default constructor for HL7 OBX (Observation/Result) segment entity.
     * Initializes all fields to their default values.
     */
    public Hl7Obx() {
    }

    /**
     * Complete constructor for HL7 OBX (Observation/Result) segment entity.
     * Creates a fully initialized OBX segment with all HL7 standard fields.
     *
     * @param obxId                   int unique identifier for this OBX record
     * @param obrId                   int related OBR record identifier
     * @param setId                   String set ID for OBX segment (OBX.1)
     * @param valueType               String value type (NM=Numeric, ST=String, etc.) (OBX.2)
     * @param observationIdentifier   String observation identifier (OBX.3)
     * @param loincNum                String LOINC code for standardized test identification
     * @param observationSubId        String observation sub-ID for repeated values (OBX.4)
     * @param observationResults      String actual test result or observation value (OBX.5)
     * @param units                   String units of measurement (OBX.6)
     * @param referenceRange          String normal reference range (OBX.7)
     * @param abnormalFlags           String abnormal flags (H=High, L=Low, N=Normal) (OBX.8)
     * @param probability             String probability of abnormality (OBX.9)
     * @param natureOfAbnormalTest    String nature of abnormal test (OBX.10)
     * @param observationResultStatus String result status (F=Final, P=Preliminary) (OBX.11)
     * @param dateLastNormalValue     String date of last normal value (OBX.12)
     * @param userDefinedAccessChecks String user-defined access checks (OBX.13)
     * @param observationDateTime     String date/time of observation (OBX.14)
     * @param producerId              String producer ID (OBX.15)
     * @param responsibleObserver     String responsible observer (OBX.16)
     * @param observationMethod       String observation method (OBX.17)
     * @param note                    String additional notes or comments
     */
    public Hl7Obx(int obxId, int obrId, String setId, String valueType,
                  String observationIdentifier, String loincNum,
                  String observationSubId, String observationResults,
                  String units, String referenceRange, String abnormalFlags,
                  String probability, String natureOfAbnormalTest,
                  String observationResultStatus, String dateLastNormalValue,
                  String userDefinedAccessChecks, String observationDateTime,
                  String producerId, String responsibleObserver,
                  String observationMethod, String note) {
        this.obxId = obxId;
        this.obrId = obrId;
        this.setId = setId;
        this.valueType = valueType;
        this.observationIdentifier = observationIdentifier;
        this.loincNum = loincNum;
        this.observationSubId = observationSubId;
        this.observationResults = observationResults;
        this.units = units;
        this.referenceRange = referenceRange;
        this.abnormalFlags = abnormalFlags;
        this.probability = probability;
        this.natureOfAbnormalTest = natureOfAbnormalTest;
        this.observationResultStatus = observationResultStatus;
        this.dateLastNormalValue = dateLastNormalValue;
        this.userDefinedAccessChecks = userDefinedAccessChecks;
        this.observationDateTime = observationDateTime;
        this.producerId = producerId;
        this.responsibleObserver = responsibleObserver;
        this.observationMethod = observationMethod;
        this.note = note;
    }

    /**
     * Gets the unique OBX record identifier.
     * This is the database primary key for this OBX segment record.
     *
     * @return int the unique OBX record identifier
     */
    public int getObxId() {
        return obxId;
    }

    /**
     * Gets the related OBR record identifier.
     * This links the observation result to its corresponding request.
     *
     * @return int the OBR record identifier this result belongs to
     */
    public int getObrId() {
        return obrId;
    }

    /**
     * Gets the set ID for this OBX segment (HL7 field OBX.1).
     * Used to identify different instances of OBX segments within a message.
     *
     * @return String the set ID, empty string if null
     */
    public String getSetId() {
        return (setId != null ? setId : "");
    }

    /**
     * Gets the value type (HL7 field OBX.2).
     * Specifies the data type of the observation value (NM=Numeric, ST=String, etc.).
     *
     * @return String the value type code, empty string if null
     */
    public String getValueType() {
        return (valueType != null ? valueType : "");
    }

    /**
     * Gets the observation identifier (HL7 field OBX.3).
     * Identifies the specific observation or test being reported.
     *
     * @return String the observation identifier, empty string if null
     */
    public String getObservationIdentifier() {
        return (observationIdentifier != null ? observationIdentifier : "");
    }

    /**
     * Gets the LOINC number for standardized test identification.
     * LOINC (Logical Observation Identifiers Names and Codes) provides universal
     * identifiers for laboratory and clinical observations.
     *
     * @return String the LOINC code, empty string if null
     * @see LoincCodes for complete LOINC code definitions
     */
    public String getLoincNum() {
        return (loincNum != null ? loincNum : "");
    }

    /**
     * Gets the observationSubId
     *
     * @return String observationSubId
     */
    public String getObservationSubId() {
        return (observationSubId != null ? observationSubId : "");
    }

    /**
     * Gets the observation results (HL7 field OBX.5).
     * The actual test result, measurement value, or clinical observation.
     *
     * @return String the observation result value, empty string if null
     */
    public String getObservationResults() {
        return (observationResults != null ? observationResults : "");
    }

    /**
     * Gets the units
     *
     * @return String units
     */
    public String getUnits() {
        return (units != null ? units : "");
    }

    /**
     * Gets the referenceRange
     *
     * @return String referenceRange
     */
    public String getReferenceRange() {
        return (referenceRange != null ? referenceRange : "");
    }

    /**
     * Gets the abnormalFlags
     *
     * @return String abnormalFlags
     */
    public String getAbnormalFlags() {
        return (abnormalFlags != null ? abnormalFlags : "");
    }

    /**
     * Gets the probability
     *
     * @return String probability
     */
    public String getProbability() {
        return (probability != null ? probability : "");
    }

    /**
     * Gets the natureOfAbnormalTest
     *
     * @return String natureOfAbnormalTest
     */
    public String getNatureOfAbnormalTest() {
        return (natureOfAbnormalTest != null ? natureOfAbnormalTest : "");
    }

    /**
     * Gets the observationResultStatus
     *
     * @return String observationResultStatus
     */
    public String getObservationResultStatus() {
        return (observationResultStatus != null ? observationResultStatus : "");
    }

    /**
     * Gets the dateLastNormalValue
     *
     * @return String dateLastNormalValue
     */
    public String getDateLastNormalValue() {
        return dateLastNormalValue;
    }

    /**
     * Gets the userDefinedAccessChecks
     *
     * @return String userDefinedAccessChecks
     */
    public String getUserDefinedAccessChecks() {
        return (userDefinedAccessChecks != null ? userDefinedAccessChecks : "");
    }

    /**
     * Gets the observationDateTime
     *
     * @return String observationDateTime
     */
    public String getObservationDateTime() {
        return observationDateTime;
    }

    /**
     * Gets the producerId
     *
     * @return String producerId
     */
    public String getProducerId() {
        return (producerId != null ? producerId : "");
    }

    /**
     * Gets the responsibleObserver
     *
     * @return String responsibleObserver
     */
    public String getResponsibleObserver() {
        return (responsibleObserver != null ? responsibleObserver : "");
    }

    /**
     * Gets the observationMethod
     *
     * @return String observationMethod
     */
    public String getObservationMethod() {
        return (observationMethod != null ? observationMethod : "");
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
     * Sets the obxId
     *
     * @param obxId int
     */
    public void setObxId(int obxId) {
        this.obxId = obxId;
    }

    /**
     * Sets the obrId
     *
     * @param obrId int
     */
    public void setObrId(int obrId) {
        this.obrId = obrId;
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
     * Sets the valueType
     *
     * @param valueType String
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    /**
     * Sets the observationIdentifier
     *
     * @param observationIdentifier String
     */
    public void setObservationIdentifier(String observationIdentifier) {
        this.observationIdentifier = observationIdentifier;
    }

    /**
     * Sets the loincNum
     *
     * @param loincNum String
     */
    public void setLoincNum(String loincNum) {
        this.loincNum = loincNum;
    }

    /**
     * Sets the observationSubId
     *
     * @param observationSubId String
     */
    public void setObservationSubId(String observationSubId) {
        this.observationSubId = observationSubId;
    }

    /**
     * Sets the observationResults
     *
     * @param observationResults String
     */
    public void setObservationResults(String observationResults) {
        this.observationResults = observationResults;
    }

    /**
     * Sets the units
     *
     * @param units String
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * Sets the referenceRange
     *
     * @param referenceRange String
     */
    public void setReferenceRange(String referenceRange) {
        this.referenceRange = referenceRange;
    }

    /**
     * Sets the abnormalFlags
     *
     * @param abnormalFlags String
     */
    public void setAbnormalFlags(String abnormalFlags) {
        this.abnormalFlags = abnormalFlags;
    }

    /**
     * Sets the probability
     *
     * @param probability String
     */
    public void setProbability(String probability) {
        this.probability = probability;
    }

    /**
     * Sets the natureOfAbnormalTest
     *
     * @param natureOfAbnormalTest String
     */
    public void setNatureOfAbnormalTest(String natureOfAbnormalTest) {
        this.natureOfAbnormalTest = natureOfAbnormalTest;
    }

    /**
     * Sets the observationResultStatus
     *
     * @param observationResultStatus String
     */
    public void setObservationResultStatus(String observationResultStatus) {
        this.observationResultStatus = observationResultStatus;
    }

    /**
     * Sets the dateLastNormalValue
     *
     * @param dateLastNormalValue String
     */
    public void setDateLastNormalValue(String dateLastNormalValue) {
        this.dateLastNormalValue = dateLastNormalValue;
    }

    /**
     * Sets the userDefinedAccessChecks
     *
     * @param userDefinedAccessChecks String
     */
    public void setUserDefinedAccessChecks(String userDefinedAccessChecks) {
        this.userDefinedAccessChecks = userDefinedAccessChecks;
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
     * Sets the producerId
     *
     * @param producerId String
     */
    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    /**
     * Sets the responsibleObserver
     *
     * @param responsibleObserver String
     */
    public void setResponsibleObserver(String responsibleObserver) {
        this.responsibleObserver = responsibleObserver;
    }

    /**
     * Sets the observationMethod
     *
     * @param observationMethod String
     */
    public void setObservationMethod(String observationMethod) {
        this.observationMethod = observationMethod;
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
