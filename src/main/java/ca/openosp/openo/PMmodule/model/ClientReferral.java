//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.PMmodule.model;

import java.io.Serializable;

/**
 * Entity representing a client referral to a program or service.
 *
 * A ClientReferral tracks the process of referring a client to a specific
 * program, from initial referral through acceptance, rejection, or completion.
 * This is a critical component of care coordination and program admission.
 *
 * Key features:
 * - Tracks referral lifecycle from creation to completion
 * - Supports both internal and remote facility referrals
 * - Links clients to programs with provider involvement
 * - Manages vacancy-based admissions
 * - Records rejection reasons and completion notes
 *
 * Referral workflow:
 * 1. Referral created by provider
 * 2. Status set to PENDING
 * 3. Program reviews referral
 * 4. Status updated to ACTIVE (accepted) or REJECTED
 * 5. If active, may progress to CURRENT (admitted)
 * 6. Completion date set when service ends
 *
 * Status values:
 * - PENDING: Awaiting review
 * - ACTIVE: Accepted, awaiting admission
 * - CURRENT: Currently admitted to program
 * - REJECTED: Not accepted for program
 * - UNKNOWN: Status unclear or legacy data
 *
 * Remote referrals:
 * - Support for inter-facility referrals
 * - Remote facility and program IDs for tracking
 * - Enables care coordination across organizations
 *
 * Vacancy management:
 * - Links to specific vacancies when available
 * - Tracks vacancy template for bed management
 * - Supports temporary admissions
 *
 * Database mapping:
 * - Table: client_referral
 * - Primary key: referral_id
 * - Foreign keys: client_id, program_id, provider_no
 *
 * @since 2005-01-01
 * @see Program
 * @see Vacancy
 * @see ProgramClientStatus
 */
public class ClientReferral implements Serializable {

    /**
     * Status constant for rejected referrals.
     * Indicates the program has declined to accept this client.
     */
    public static String STATUS_REJECTED = "rejected";

    /**
     * Status constant for active/accepted referrals.
     * Client has been accepted but not yet admitted.
     */
    public static String STATUS_ACTIVE = "active";

    /**
     * Status constant for current admissions.
     * Client is currently receiving services in the program.
     */
    public static String STATUS_CURRENT = "current";

    /**
     * Status constant for unknown status.
     * Used for legacy data or unclear referral states.
     */
    public static String STATUS_UNKNOWN = "unknown";

    /**
     * Status constant for pending referrals.
     * Awaiting review and decision by the program.
     */
    public static String STATUS_PENDING = "pending";

    /**
     * Cached hash code for performance optimization.
     * Set to Integer.MIN_VALUE to indicate uncalculated state.
     */
    private int hashCode = Integer.MIN_VALUE;

    /**
     * Primary key from the client_referral table.
     * Uniquely identifies this referral.
     */
    private Long _id;

    /**
     * Client demographic ID being referred.
     * References demographic_no in demographic table.
     */
    private Long _clientId;

    /**
     * Date when the referral was initiated.
     * Tracks when the referral process began.
     */
    private java.util.Date _referralDate;

    /**
     * Provider number who made the referral.
     * References the referring healthcare provider.
     */
    private String _providerNo;

    /**
     * Local facility ID for the referral.
     * Identifies which facility is involved.
     */
    private Integer facilityId;

    /**
     * General notes about the referral.
     * May include reason for referral or special considerations.
     */
    private String _notes;

    /**
     * Client's presenting problems or issues.
     * Documents the primary reasons for referral.
     */
    private String presentProblems;

    /**
     * Reason for rejection if referral was declined.
     * Selected from predefined rejection reasons.
     */
    private String radioRejectionReason;

    /**
     * Notes added when referral is completed.
     * Documents outcome and any follow-up needed.
     */
    private String _completionNotes;

    /**
     * Program ID being referred to.
     * References program_id in program table.
     */
    private Long _programId;

    /**
     * Current status of the referral.
     * Uses STATUS_* constants defined above.
     */
    private String _status;

    /**
     * Flag for temporary admission.
     * Indicates if this is a short-term or emergency placement.
     */
    private boolean _temporaryAdmission;

    /**
     * Date when referral was completed.
     * Set when client finishes program or referral closes.
     */
    private java.util.Date _completionDate;

    /**
     * Referring provider's last name.
     * Cached for display without provider table join.
     */
    private String _providerLastName;

    /**
     * Referring provider's first name.
     * Cached for display without provider table join.
     */
    private String _providerFirstName;

    /**
     * Name of the program being referred to.
     * Cached for display without program table join.
     */
    private String _programName;

    /**
     * Type of program being referred to.
     * Cached program classification.
     */
    private String _programType;

    /**
     * Remote facility ID for inter-facility referrals.
     * Identifies external facility in integrated systems.
     */
    private String remoteFacilityId;

    /**
     * Remote program ID for inter-facility referrals.
     * Identifies specific program at remote facility.
     */
    private String remoteProgramId;

    /**
     * Transient field for selected vacancy name.
     * Used in UI for vacancy selection, not persisted.
     */
    private String selectVacancy;

    /**
     * Transient field for vacancy template name.
     * Used in UI for template display, not persisted.
     */
    private String vacancyTemplateName;

    /**
     * Gets the vacancy ID associated with this referral.
     *
     * Returns the specific vacancy (bed/slot) assigned
     * for this referral if vacancy-based admission is used.
     *
     * @return Integer the vacancy ID, null if not assigned
     * @since 2005-01-01
     */
    public Integer getVacancyId() {
        return vacancyId;
    }

    /**
     * Sets the vacancy ID associated with this referral.
     *
     * Links this referral to a specific vacancy for
     * bed/slot management in residential programs.
     *
     * @param vacancyId Integer the vacancy ID to assign
     * @since 2005-01-01
     */
    public void setVacancyId(Integer vacancyId) {
        this.vacancyId = vacancyId;
    }

    /**
     * Vacancy ID for bed/slot assignment.
     * Links to specific vacancy in residential programs.
     */
    private Integer vacancyId;

    /**
     * Default constructor for creating a new ClientReferral.
     *
     * Creates an empty referral instance and calls
     * initialize() for any setup operations.
     *
     * @since 2005-01-01
     */
    public ClientReferral() {
        initialize();
    }

    /**
     * Constructor for creating a ClientReferral with specific ID.
     *
     * Used when loading existing referrals from the database
     * or when ID is known in advance.
     *
     * @param _id Long the unique referral identifier
     * @since 2005-01-01
     */
    public ClientReferral(Long _id) {
        this.setId(_id);
        initialize();
    }

    /**
     * Constructor for creating a ClientReferral with required fields.
     *
     * Creates a complete referral with all mandatory information:
     * client being referred, referring provider, and target program.
     *
     * @param _id Long the unique referral identifier
     * @param _clientId Long the client demographic ID
     * @param _providerNo String the referring provider number
     * @param _programId Long the target program ID
     * @since 2005-01-01
     */
    public ClientReferral(Long _id, Long _clientId, String _providerNo, Long _programId) {
        this.setId(_id);
        this.setClientId(_clientId);
        this.setProviderNo(_providerNo);
        this.setProgramId(_programId);
        initialize();
    }

    /**
     * Gets the provider's name in formatted display format.
     *
     * Returns the provider name as "LastName,FirstName"
     * for consistent display in user interfaces.
     *
     * @return String the formatted provider name
     * @since 2005-01-01
     */
    public String getProviderFormattedName() {
        return getProviderLastName() + "," + getProviderFirstName();
    }

    /**
     * Initializes the ClientReferral instance.
     *
     * Protected method called by constructors for any
     * necessary initialization. Currently empty but
     * preserved for future enhancements.
     *
     * @since 2005-01-01
     */
    protected void initialize() {
        // Reserved for future initialization logic
    }

    /**
     * Gets the unique identifier of this referral.
     *
     * Returns the primary key from the client_referral table.
     * Used to uniquely identify and reference this referral.
     *
     * Database mapping:
     * - Column: referral_id
     * - Generator: native (auto-increment)
     *
     * @return Long the unique referral ID, null if not persisted
     * @since 2005-01-01
     */
    public Long getId() {
        return _id;
    }

    /**
     * Sets the unique identifier of this referral.
     *
     * Updates the primary key and invalidates cached hash code.
     * Typically called by persistence frameworks.
     *
     * @param _id Long the new referral ID
     * @since 2005-01-01
     */
    public void setId(Long _id) {
        this._id = _id;
        this.hashCode = Integer.MIN_VALUE;
    }

    /**
     * Gets the client ID being referred.
     *
     * Returns the demographic number of the client
     * who is the subject of this referral.
     *
     * @return Long the client demographic ID
     * @since 2005-01-01
     */
    public Long getClientId() {
        return _clientId;
    }

    /**
     * Sets the client ID being referred.
     *
     * Specifies which client this referral is for.
     * Must reference a valid demographic_no.
     *
     * @param _clientId Long the client demographic ID
     * @since 2005-01-01
     */
    public void setClientId(Long _clientId) {
        this._clientId = _clientId;
    }

    /**
     * Gets the date when referral was initiated.
     *
     * Returns the timestamp of when this referral
     * was created in the system.
     *
     * @return Date the referral initiation date
     * @since 2005-01-01
     */
    public java.util.Date getReferralDate() {
        return _referralDate;
    }

    /**
     * Sets the date when referral was initiated.
     *
     * Records when the referral process began.
     * Typically set to current date on creation.
     *
     * @param _referralDate Date the referral initiation date
     * @since 2005-01-01
     */
    public void setReferralDate(java.util.Date _referralDate) {
        this._referralDate = _referralDate;
    }

    /**
     * Gets the referring provider number.
     *
     * Returns the provider number of the healthcare
     * professional who initiated this referral.
     *
     * @return String the provider number
     * @since 2005-01-01
     */
    public String getProviderNo() {
        return _providerNo;
    }

    /**
     * Sets the referring provider number.
     *
     * Specifies which provider made this referral.
     * Must reference a valid provider_no.
     *
     * @param _providerNo String the provider number
     * @since 2005-01-01
     */
    public void setProviderNo(String _providerNo) {
        this._providerNo = _providerNo;
    }

    /**
     * Gets the general notes about this referral.
     *
     * Returns any additional information or special
     * considerations documented for this referral.
     *
     * @return String the referral notes
     * @since 2005-01-01
     */
    public String getNotes() {
        return _notes;
    }

    /**
     * Sets the general notes about this referral.
     *
     * Records additional information such as special
     * needs, preferences, or referral context.
     *
     * @param _notes String the referral notes
     * @since 2005-01-01
     */
    public void setNotes(String _notes) {
        this._notes = _notes;
    }

    /**
     * Gets the client's presenting problems.
     *
     * Returns the documented issues or conditions
     * that prompted this referral.
     *
     * @return String the presenting problems description
     * @since 2005-01-01
     */
    public String getPresentProblems() {
        return presentProblems;
    }

    /**
     * Sets the client's presenting problems.
     *
     * Documents the primary issues, symptoms, or needs
     * that are the reason for this referral.
     *
     * @param presentProblems String the presenting problems description
     * @since 2005-01-01
     */
    public void setPresentProblems(String presentProblems) {
        this.presentProblems = presentProblems;
    }

    /**
     * Gets the rejection reason code.
     *
     * Returns the selected reason if this referral
     * was rejected, chosen from predefined options.
     *
     * @return String the rejection reason code
     * @since 2005-01-01
     */
    public String getRadioRejectionReason() {
        return radioRejectionReason;
    }

    /**
     * Sets the rejection reason code.
     *
     * Records why the referral was rejected using
     * a predefined reason code from radio button options.
     *
     * @param radioRejectionReason String the rejection reason code
     * @since 2005-01-01
     */
    public void setRadioRejectionReason(String radioRejectionReason) {
        this.radioRejectionReason = radioRejectionReason;
    }

    /**
     * Gets the completion notes for this referral.
     *
     * Returns notes added when the referral was completed,
     * including outcome information and follow-up plans.
     *
     * @return String the completion notes
     * @since 2005-01-01
     */
    public String getCompletionNotes() {
        return _completionNotes;
    }

    /**
     * Sets the completion notes for this referral.
     *
     * Documents the outcome, discharge information,
     * and any follow-up requirements.
     *
     * @param _completionNotes String the completion notes
     * @since 2005-01-01
     */
    public void setCompletionNotes(String _completionNotes) {
        this._completionNotes = _completionNotes;
    }

    /**
     * Gets the target program ID for this referral.
     *
     * Returns the ID of the program the client is
     * being referred to.
     *
     * @return Long the program ID
     * @since 2005-01-01
     */
    public Long getProgramId() {
        return _programId;
    }

    /**
     * Sets the target program ID for this referral.
     *
     * Specifies which program the client is being
     * referred to. Must reference a valid program_id.
     *
     * @param _programId Long the program ID
     * @since 2005-01-01
     */
    public void setProgramId(Long _programId) {
        this._programId = _programId;
    }

    /**
     * Gets the current status of this referral.
     *
     * Returns the referral status using one of the
     * STATUS_* constants defined in this class.
     *
     * @return String the status (PENDING, ACTIVE, CURRENT, REJECTED, UNKNOWN)
     * @since 2005-01-01
     */
    public String getStatus() {
        return _status;
    }

    /**
     * Sets the current status of this referral.
     *
     * Updates the referral status. Should use one of
     * the STATUS_* constants defined in this class.
     *
     * @param _status String the new status
     * @since 2005-01-01
     */
    public void setStatus(String _status) {
        this._status = _status;
    }

    /**
     * Checks if this is a temporary admission.
     *
     * Returns true if this referral is for temporary
     * or emergency placement rather than long-term.
     *
     * @return boolean true if temporary admission
     * @since 2005-01-01
     */
    public boolean isTemporaryAdmission() {
        return _temporaryAdmission;
    }

    /**
     * Sets whether this is a temporary admission.
     *
     * Marks the referral as temporary/emergency placement
     * versus standard long-term admission.
     *
     * @param _temporaryAdmission boolean true for temporary admission
     * @since 2005-01-01
     */
    public void setTemporaryAdmission(boolean _temporaryAdmission) {
        this._temporaryAdmission = _temporaryAdmission;
    }

    /**
     * Gets the date when referral was completed.
     *
     * Returns the date when services ended or the
     * referral was closed for any reason.
     *
     * @return Date the completion date, null if still active
     * @since 2005-01-01
     */
    public java.util.Date getCompletionDate() {
        return _completionDate;
    }

    /**
     * Sets the date when referral was completed.
     *
     * Records when the client finished the program
     * or the referral was otherwise closed.
     *
     * @param _completionDate Date the completion date
     * @since 2005-01-01
     */
    public void setCompletionDate(java.util.Date _completionDate) {
        this._completionDate = _completionDate;
    }

    /**
     * Gets the referring provider's last name.
     *
     * Returns the cached last name of the provider
     * who made this referral.
     *
     * @return String the provider's last name
     * @since 2005-01-01
     */
    public String getProviderLastName() {
        return _providerLastName;
    }

    /**
     * Sets the referring provider's last name.
     *
     * Caches the provider's last name for display
     * without requiring a join to provider table.
     *
     * @param _providerLastName String the provider's last name
     * @since 2005-01-01
     */
    public void setProviderLastName(String _providerLastName) {
        this._providerLastName = _providerLastName;
    }

    /**
     * Gets the referring provider's first name.
     *
     * Returns the cached first name of the provider
     * who made this referral.
     *
     * @return String the provider's first name
     * @since 2005-01-01
     */
    public String getProviderFirstName() {
        return _providerFirstName;
    }

    /**
     * Sets the referring provider's first name.
     *
     * Caches the provider's first name for display
     * without requiring a join to provider table.
     *
     * @param _providerFirstName String the provider's first name
     * @since 2005-01-01
     */
    public void setProviderFirstName(String _providerFirstName) {
        this._providerFirstName = _providerFirstName;
    }

    /**
     * Gets the target program name.
     *
     * Returns the cached name of the program
     * this client is being referred to.
     *
     * @return String the program name
     * @since 2005-01-01
     */
    public String getProgramName() {
        return _programName;
    }

    /**
     * Sets the target program name.
     *
     * Caches the program name for display without
     * requiring a join to program table.
     *
     * @param _programName String the program name
     * @since 2005-01-01
     */
    public void setProgramName(String _programName) {
        this._programName = _programName;
    }

    /**
     * Gets the target program type.
     *
     * Returns the cached type/category of the program
     * this client is being referred to.
     *
     * @return String the program type
     * @since 2005-01-01
     */
    public String getProgramType() {
        return _programType;
    }

    /**
     * Sets the target program type.
     *
     * Caches the program type/category for display
     * and filtering purposes.
     *
     * @param _programType String the program type
     * @since 2005-01-01
     */
    public void setProgramType(String _programType) {
        this._programType = _programType;
    }

    /**
     * Determines equality based on the unique identifier.
     *
     * Two ClientReferral objects are equal if they have
     * the same non-null referral ID.
     *
     * @param obj Object the object to compare
     * @return boolean true if objects have same ID
     * @since 2005-01-01
     */
    @Override
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof ClientReferral)) return false;
        else {
            ClientReferral mObj = (ClientReferral) obj;
            if (null == this.getId() || null == mObj.getId()) return false;
            else return (this.getId().equals(mObj.getId()));
        }
    }

    /**
     * Generates hash code based on the referral ID.
     *
     * Hash code is cached for performance and
     * recalculated when ID changes.
     *
     * @return int the hash code
     * @since 2005-01-01
     */
    @Override
    public int hashCode() {
        if (Integer.MIN_VALUE == this.hashCode) {
            if (null == this.getId()) return super.hashCode();
            else {
                String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
                this.hashCode = hashStr.hashCode();
            }
        }
        return this.hashCode;
    }

    /**
     * Returns string representation of this referral.
     *
     * Currently delegates to superclass. Could be
     * enhanced to show referral details.
     *
     * @return String the string representation
     * @since 2005-01-01
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Gets the facility ID for this referral.
     *
     * Returns the local facility identifier where
     * this referral is being processed.
     *
     * @return Integer the facility ID
     * @since 2005-01-01
     */
    public Integer getFacilityId() {
        return facilityId;
    }

    /**
     * Sets the facility ID for this referral.
     *
     * Specifies which facility is handling
     * this referral.
     *
     * @param facilityId Integer the facility ID
     * @since 2005-01-01
     */
    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    /**
     * Gets the remote facility ID.
     *
     * Returns the identifier of an external facility
     * for inter-facility referrals.
     *
     * @return String the remote facility ID
     * @since 2005-01-01
     */
    public String getRemoteFacilityId() {
        return remoteFacilityId;
    }

    /**
     * Sets the remote facility ID.
     *
     * Specifies the external facility for
     * inter-facility referral coordination.
     *
     * @param remoteFacilityId String the remote facility ID
     * @since 2005-01-01
     */
    public void setRemoteFacilityId(String remoteFacilityId) {
        this.remoteFacilityId = remoteFacilityId;
    }

    /**
     * Gets the remote program ID.
     *
     * Returns the program identifier at the remote
     * facility for inter-facility referrals.
     *
     * @return String the remote program ID
     * @since 2005-01-01
     */
    public String getRemoteProgramId() {
        return remoteProgramId;
    }

    /**
     * Sets the remote program ID.
     *
     * Specifies the program at the remote facility
     * that will receive this referral.
     *
     * @param remoteProgramId String the remote program ID
     * @since 2005-01-01
     */
    public void setRemoteProgramId(String remoteProgramId) {
        this.remoteProgramId = remoteProgramId;
    }

    /**
     * Gets the selected vacancy name.
     *
     * Returns the transient vacancy name used for
     * UI display during vacancy selection.
     *
     * @return String the vacancy name
     * @since 2005-01-01
     */
    public String getSelectVacancy() {
        return selectVacancy;
    }

    /**
     * Sets the selected vacancy name.
     *
     * Stores the vacancy name for UI purposes.
     * This is transient and not persisted.
     *
     * @param selectVacancy String the vacancy name
     * @since 2005-01-01
     */
    public void setSelectVacancy(String selectVacancy) {
        this.selectVacancy = selectVacancy;
    }

    /**
     * Gets the vacancy template name.
     *
     * Returns the transient template name used
     * for bed/slot categorization in UI.
     *
     * @return String the vacancy template name
     * @since 2005-01-01
     */
    public String getVacancyTemplateName() {
        return vacancyTemplateName;
    }

    /**
     * Sets the vacancy template name.
     *
     * Stores the template name for UI display.
     * This is transient and not persisted.
     *
     * @param vacancyTemplateName String the template name
     * @since 2005-01-01
     */
    public void setVacancyTemplateName(String vacancyTemplateName) {
        this.vacancyTemplateName = vacancyTemplateName;
    }


}
