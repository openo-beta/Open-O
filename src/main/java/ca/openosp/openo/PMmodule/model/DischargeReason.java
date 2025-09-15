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

/**
 * Enumeration of standardized reasons for client discharge from programs.
 *
 * DischargeReason provides a comprehensive list of reasons why a client
 * may be discharged or terminated from a healthcare or social service program.
 * These standardized codes ensure consistent tracking and reporting across
 * the system.
 *
 * Key categories of discharge reasons:
 * - Clinical needs exceeding capacity
 * - Client choice and preferences
 * - Program completion outcomes
 * - Administrative and eligibility issues
 * - External circumstances (death, relocation)
 *
 * Reason classifications:
 * - Positive outcomes: Completion with/without referral, service plan completed
 * - Capacity limitations: Medical/social/mental health needs exceed provision
 * - Client initiated: Not interested, withdrawal by preference
 * - Administrative: Does not fit criteria, no space available
 * - Critical events: Death, suicide, admission to LTC
 *
 * The discharge reason is critical for:
 * - Program evaluation and quality improvement
 * - Resource planning and capacity management
 * - Outcome tracking and reporting
 * - Referral pathway optimization
 * - Compliance with healthcare regulations
 *
 * Integration points:
 * - Recorded in admission discharge records
 * - Used in statistical reports
 * - Influences referral decisions
 * - Tracked for program effectiveness metrics
 *
 * Display strings for each reason are defined in:
 * MessageResources_program.properties
 *
 * @since 2005-01-01
 * @see Admission
 * @see Program
 * @see ClientReferral
 */
public enum DischargeReason {

    /**
     * Unknown or unspecified discharge reason.
     * Default value when reason is not recorded.
     */
    UNKNOWN,

    /**
     * Client requires acute medical care.
     * Transferred to hospital or emergency services.
     */
    REQUIRES_ACUTE_CARE,

    /**
     * Client is not interested in the program.
     * Voluntary withdrawal due to lack of interest.
     */
    NOT_INTERESTED,

    /**
     * Client does not meet program eligibility criteria.
     * Administrative discharge due to ineligibility.
     */
    DOES_NOT_FIT_CRITERIA,

    /**
     * No space available in the program.
     * Capacity-related discharge or waitlist removal.
     */
    NO_SPACE_AVAILABLE,

    /**
     * Other discharge reason not listed.
     * Requires additional documentation.
     */
    OTHER,

    /**
     * Reserved placeholder for future use.
     */
    STUB_6,

    /**
     * Reserved placeholder for future use.
     */
    STUB_7,

    /**
     * Reserved placeholder for future use.
     */
    STUB_8,

    /**
     * Reserved placeholder for future use.
     */
    STUB_9,

    /**
     * Client's medical needs exceed program capacity.
     * Requires higher level of medical care.
     */
    MEDICAL_NEEDS_EXCEED_PROVISION,

    /**
     * Client's social or behavioral needs exceed capacity.
     * Requires specialized behavioral intervention.
     */
    SOCIAL_BEHAVIOUR_NEEDS_EXCEED_PROVISION,

    /**
     * Client's withdrawal/detox needs exceed capacity.
     * Requires specialized addiction treatment.
     */
    WITHDRAWAL_NEEDS_EXCEED_PROVISION,

    /**
     * Client's mental health needs exceed capacity.
     * Requires specialized psychiatric care.
     */
    MENTAL_HEALTH_NEEDS_EXCEED_PROVISION,

    /**
     * Other client needs exceed program capacity.
     * Generic category for unspecified exceeded needs.
     */
    OTHER_NEEDS_EXCEED_PROVISION,

    /**
     * Client admitted to long-term care facility.
     * Permanent placement in nursing home or LTC.
     */
    ADMITTED_TO_LTC_FACILITY,

    /**
     * Program completed without referral to other services.
     * Successful completion, no follow-up needed.
     */
    COMPLETION_WITHOUT_REFERRAL,

    /**
     * Program completed with referral to other services.
     * Successful completion with continuity of care.
     */
    COMPLETION_WITH_REFERRAL,

    /**
     * Client deceased.
     * Discharge due to client death.
     */
    DEATH,

    /**
     * Client relocated outside service area.
     * Geographic move preventing continued service.
     */
    RELOCATION,

    /**
     * Service plan successfully completed.
     * All treatment goals achieved.
     */
    SERVICE_PLAN_COMPLETED,

    /**
     * Client death by suicide.
     * Critical incident requiring special reporting.
     */
    SUICIDE,

    /**
     * Withdrawal at client's preference.
     * Voluntary discharge requested by client.
     * Note: Spelling retained for backward compatibility.
     */
    WITHDRAWL_CLIENT_PREFERENCE

    /**
     * Display strings for each discharge reason are defined in
     * MessageResources_program.properties for internationalization
     * and user-friendly presentation.
     */
}
