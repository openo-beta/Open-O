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


package ca.openosp.openo.log;

/**
 * Healthcare audit logging constants for OpenO EMR system.
 * This class defines standardized action types and content identifiers used
 * throughout the EMR system for regulatory compliance audit trails.
 *
 * <p>These constants ensure consistent audit logging across all clinical and
 * administrative modules, supporting HIPAA, PIPEDA, and provincial healthcare
 * information protection requirements. The standardized values enable:</p>
 * <ul>
 *   <li>Consistent audit trail reporting for regulatory compliance</li>
 *   <li>Automated security monitoring and breach detection</li>
 *   <li>Quality assurance tracking of clinical workflows</li>
 *   <li>Performance analysis of healthcare operations</li>
 * </ul>
 *
 * <p>The constants are organized into two categories:</p>
 * <ul>
 *   <li><strong>Action Constants:</strong> Describe the type of action performed
 *       (LOGIN, READ, ADD, UPDATE, DELETE, etc.)</li>
 *   <li><strong>Content Constants (CON_*):</strong> Identify the type of clinical
 *       or administrative content being accessed or modified</li>
 * </ul>
 *
 * @see LogAction
 * @see OscarLog
 * @since 2005-06-08
 */
public class LogConst {

    // Authentication and session management actions
    /** Provider authentication - user login to EMR system */
    public static final String LOGIN = "log in";
    /** Provider logout - user logout from EMR system */
    public static final String LOGOUT = "log out";

    // Basic data access and modification actions
    /** Data access - viewing patient information or clinical records */
    public static final String READ = "read";
    /** Data creation - adding new patient information or clinical records */
    public static final String ADD = "add";
    /** Data modification - updating existing patient or clinical information */
    public static final String UPDATE = "update";
    /** Data modification - editing patient or clinical information */
    public static final String EDIT = "edit";
    /** Data removal - deleting patient information or clinical records */
    public static final String DELETE = "delete";

    // Clinical workflow actions
    /** Clinical review - provider has reviewed clinical information */
    public static final String REVIEWED = "reviewed";
    /** Clinical acknowledgment - provider acknowledges receipt/review */
    public static final String ACK = "acknowledge";
    /** Clinical verification - provider verifies accuracy of information */
    public static final String VERIFY = "verify";
    /** Clinical annotation - provider adds notes or comments */
    public static final String ANNOTATE = "annotate";

    // Healthcare-specific actions
    /** Medication management - discontinuing prescriptions or treatments */
    public static final String DISCONTINUE = "discontinue";
    /** Prescription management - reissuing existing prescriptions */
    public static final String REPRESCRIBE = "represcribe";
    /** Document management - archiving clinical documents or records */
    public static final String ARCHIVE = "archive";
    /** Document management - reprinting forms, prescriptions, or reports */
    public static final String REPRINT = "reprint";

    // System and security actions
    /** Access control violation - insufficient privileges for requested action */
    public static final String NORIGHT = "no right";
    /** Data relationship - removing links between clinical entities */
    public static final String UNLINK = "unlink";
    /** Communication - sending messages, referrals, or documents */
    public static final String SENT = "sent";
    /** Access denial - user or system refused requested action */
    public static final String REFUSED = "refused";

    // Authentication and system access content types
    /** Legal agreements - login terms and conditions acceptance */
    public static final String CON_LOGIN_AGREEMENT = "login agreement";
    /** Authentication - user login and session management */
    public static final String CON_LOGIN = "login";

    // Patient management and clinical workflow content types
    /** Patient appointments - scheduling, booking, and calendar management */
    public static final String CON_APPT = "appointment";
    /** Electronic patient chart - comprehensive clinical record access */
    public static final String CON_ECHART = "eChart";
    /** Patient demographics - personal and contact information */
    public static final String CON_DEMOGRAPHIC = "demographic";
    /** Patient relationships - family, emergency contacts, and care relationships */
    public static final String CON_DEMOGRAPHIC_RELATION = "demographic_relations";

    // System administration and security content types
    /** User roles - provider role assignments and permissions */
    public static final String CON_ROLE = "role";
    /** System privileges - access control and permission management */
    public static final String CON_PRIVILEGE = "privilege";
    /** Security records - audit logs and security event tracking */
    public static final String CON_SECURITY = "securityRecord";

    // Clinical forms and documentation content types
    /** Medical forms - clinical assessment forms and templates */
    public static final String CON_FORM = "form";
    /** Clinical documents - reports, letters, and medical records */
    public static final String CON_DOCUMENT = "document";
    /** Clinical annotations - provider notes and comments on records */
    public static final String CON_ANNOTATION = "annotation";
    /** Document templates - standardized document format definitions */
    public static final String CON_DOCUMENTDESCRIPTIONTEMPLATE = "documentDescriptionTemplate";
    /** Document preferences - user-specific template and format settings */
    public static final String CON_DOCUMENTDESCRIPTIONTEMPLATEPREFERENCE = "documentDescriptionTemplatePreference";
    /** Report generation - Jasper report letters and clinical summaries */
    public static final String CON_JASPERREPORTLETER = "jr_letter";

    // Medication management content types
    /** Prescriptions - medication orders and prescription management */
    public static final String CON_PRESCRIPTION = "prescription";
    /** Medication records - drug therapy and medication history */
    public static final String CON_MEDICATION = "medication";
    /** Drug database - pharmaceutical product information */
    public static final String CON_DRUGS = "drugs";
    /** Drug reasoning - clinical justification for medication choices */
    public static final String CON_DRUGREASON = "drugReason";
    /** Patient allergies - drug allergies and adverse reactions */
    public static final String CON_ALLERGY = "allergy";
    /** Pharmacy information - pharmacy contacts and prescription routing */
    public static final String CON_PHARMACY = "pharmacy";

    // Laboratory and diagnostic content types
    /** HL7 laboratory results - standardized lab result messages */
    public static final String CON_HL7_LAB = "lab";
    /** Hospital Report Manager - external hospital communications */
    public static final String CON_HRM = "hrm";
    /** CML laboratory results - Calgary Medical Laboratory reports */
    public static final String CON_CML_LAB = "cml lab";
    /** MDS laboratory results - Medical Data Solutions lab reports */
    public static final String CON_MDS_LAB = "mds lab";
    /** Pathnet laboratory results - pathology network lab reports */
    public static final String CON_PATHNET_LAB = "pathnet lab";

    // Clinical monitoring and workflow content types
    /** Clinical flowsheets - vital signs and measurement tracking */
    public static final String CON_FLOWSHEET = "FLWST_";
    /** Tickler reminders - clinical tasks and follow-up reminders */
    public static final String CON_TICKLER = "tickler";
    /** CME notes - continuing medical education documentation */
    public static final String CON_CME_NOTE = "CME note";
    /** Fax communications - medical document transmission */
    public static final String CON_FAX = "fax";

}
