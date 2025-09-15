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
package ca.openosp.openo.PMmodule.caisi_integrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.openosp.openo.caisi_integrator.ws.CachedAdmission;
import ca.openosp.openo.caisi_integrator.ws.CachedAppointment;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicAllergy;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicDocument;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicDocumentContents;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicDrug;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicForm;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicIssue;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicLabResult;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicNote;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicPrevention;
import ca.openosp.openo.caisi_integrator.ws.DemographicWs;
import ca.openosp.openo.caisi_integrator.ws.FacilityIdIntegerCompositePk;
import ca.openosp.openo.commn.dao.DemographicExtDao;
import ca.openosp.openo.commn.dao.RemoteIntegratedDataCopyDao;
import ca.openosp.openo.commn.model.RemoteIntegratedDataCopy;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.OscarProperties;

/**
 * CAISI Integrator fallback data manager for healthcare data caching and retrieval.
 *
 * <p>This class provides a comprehensive fallback mechanism for the CAISI (Client Access to Integrated Services Information)
 * Integrator system when primary connections to remote healthcare facilities are unavailable. It implements local caching
 * and retrieval of patient healthcare data across multiple data types including clinical notes, medications, allergies,
 * lab results, forms, and other critical patient information.</p>
 *
 * <p>The fallback manager ensures continuity of care by maintaining local copies of integrated healthcare data from
 * multiple facilities, allowing healthcare providers to access patient information even when network connectivity
 * to remote systems is interrupted. This is crucial for maintaining patient safety and care quality in distributed
 * healthcare environments.</p>
 *
 * <p>Key healthcare data types managed:
 * <ul>
 * <li>Clinical notes and documentation</li>
 * <li>Medications and prescriptions</li>
 * <li>Allergies and adverse reactions</li>
 * <li>Laboratory results and diagnostic reports</li>
 * <li>Medical forms and assessments</li>
 * <li>Preventive care and immunizations</li>
 * <li>Hospital admissions and appointments</li>
 * <li>Medical documents and attachments</li>
 * </ul></p>
 *
 * <p>All data operations respect HIPAA/PIPEDA privacy requirements and maintain audit trails for patient data access.
 * The local storage mechanism is controlled by the INTEGRATOR_LOCAL_STORE property.</p>
 *
 * @see CaisiIntegratorManager
 * @see CaisiIntegratorUpdateTask
 * @see RemoteIntegratedDataCopyDao
 * @since 2012-03-19
 */
public class IntegratorFallBackManager {
    static RemoteIntegratedDataCopyDao remoteIntegratedDataCopyDao = SpringUtils.getBean(RemoteIntegratedDataCopyDao.class);
    static DemographicExtDao demographicExtDao = SpringUtils.getBean(DemographicExtDao.class);

    /**
     * Retrieves cached clinical notes for a patient from local fallback storage.
     *
     * <p>This method provides access to clinical notes from linked healthcare facilities when the primary
     * CAISI Integrator connection is unavailable. Notes are retrieved from the local cache maintained by
     * the fallback system, ensuring healthcare providers can access critical patient documentation even
     * during network outages.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier (demographic number) for whom to retrieve notes
     * @return List of CachedDemographicNote objects containing patient notes from linked facilities,
     *         or null if local storage is disabled or an empty list if no notes are available
     * @since 2012-03-19
     */
    public static List<CachedDemographicNote> getLinkedNotes(LoggedInInfo loggedInInfo, Integer demographicNo) {
        // Check if local storage fallback is enabled in system properties
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicNote> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicNote[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedDemographicNote[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicNote[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedDemographicNote>();
            for (CachedDemographicNote cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }

    /**
     * Saves linked clinical notes to local fallback storage for offline access.
     *
     * <p>This method fetches clinical notes from all linked healthcare facilities via the CAISI Integrator
     * and stores them locally to provide fallback access when remote connections are unavailable. This ensures
     * continuity of care by maintaining local copies of critical patient documentation.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save linked notes
     * @since 2012-03-19
     */
    public static void saveLinkNotes(LoggedInInfo loggedInInfo, Integer demographicNo) {
        try {
            try {
                String providerNo = null;
                if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                    providerNo = loggedInInfo.getLoggedInProviderNo();
                }
                DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
                List<CachedDemographicNote> linkedNotes = Collections.unmodifiableList(demographicWs.getLinkedCachedDemographicNotes(demographicNo));
                MiscUtils.getLogger().info("Saving remote copy for " + demographicNo + "  linkedNotes : " + linkedNotes.size());

                if (linkedNotes.size() == 0) {
                    return;
                }
                CachedDemographicNote[] array = linkedNotes.toArray(new CachedDemographicNote[linkedNotes.size()]);

                remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());
            } catch (Exception e) {
                MiscUtils.getLogger().error("Error saving remote notes for " + demographicNo, e);
            }
            //	}
        } catch (Exception ee) {
            MiscUtils.getLogger().error("Error getting remote notes for " + demographicNo, ee);
        }
    }

    /**
     * Saves remote medical forms to local fallback storage.
     *
     * <p>This method retrieves medical forms (such as lab requisitions) from linked healthcare facilities
     * and caches them locally for offline access. Currently supports specific form types like lab requisitions
     * but the architecture allows for expansion to additional form types.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save remote forms
     * @since 2012-03-19
     */
    public static void saveRemoteForms(LoggedInInfo loggedInInfo, Integer demographicNo) {
        // Array of form table names to cache - currently supports lab requisition forms
        // TODO: Implement more dynamic form type discovery mechanism
        String[] tables = {"formLabReq07"};
        List<CachedDemographicForm> remoteForms = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            for (String table : tables) {
                DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
                remoteForms = demographicWs.getLinkedCachedDemographicForms(demographicNo, table);
                MiscUtils.getLogger().debug("Saving remote forms for " + demographicNo + "  forms : " + remoteForms.size() + " table " + table);
                if (remoteForms.size() == 0) {
                    continue;
                }

                CachedDemographicForm[] array = remoteForms.toArray(new CachedDemographicForm[remoteForms.size()]);
                MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + table + " " + demographicNo);

                remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId(), table);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote forms for " + demographicNo, e);
        }
    }

    /**
     * Retrieves cached medical forms for a patient from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve forms
     * @param table the specific form table name (e.g., "formLabReq07")
     * @return List of CachedDemographicForm objects for the specified form type,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedDemographicForm> getRemoteForms(LoggedInInfo loggedInInfo, Integer demographicNo, String table) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicForm> linkedForms = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicForm[].class.getName() + "+" + table);

        if (remoteIntegratedDataCopy == null) {
            return linkedForms;
        }

        try {
            CachedDemographicForm[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicForm[].class, remoteIntegratedDataCopy);
            linkedForms = new ArrayList<CachedDemographicForm>();
            for (CachedDemographicForm cdn : array) {
                linkedForms.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedForms;
    }


    /**
     * Saves patient medical issues to local fallback storage.
     *
     * <p>Medical issues represent ongoing health conditions, diagnoses, and clinical problems
     * that require tracking across healthcare encounters and facilities.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save medical issues
     * @since 2012-03-19
     */
    public static void saveDemographicIssues(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedDemographicIssue> remoteIssues = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteIssues = demographicWs.getLinkedCachedDemographicIssuesByDemographicId(demographicNo);
            MiscUtils.getLogger().debug("Saving remoteIssues for " + demographicNo + "  issues : " + remoteIssues.size());
            if (remoteIssues.size() == 0) {
                return;
            }

            CachedDemographicIssue[] array = remoteIssues.toArray(new CachedDemographicIssue[remoteIssues.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);

            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote forms for " + demographicNo, e);
        }
    }


    /**
     * Retrieves cached patient medical issues from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve medical issues
     * @return List of CachedDemographicIssue objects containing patient's medical conditions,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedDemographicIssue> getRemoteDemographicIssues(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicIssue> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicIssue[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedDemographicIssue[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicIssue[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedDemographicIssue>();
            for (CachedDemographicIssue cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }


    /**
     * Saves patient prevention and immunization data to local fallback storage.
     *
     * <p>Prevention data includes immunizations, screenings, and preventive care measures
     * critical for maintaining patient health and meeting public health requirements.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save prevention data
     * @since 2012-03-19
     */
    public static void saveDemographicPreventions(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedDemographicPrevention> remoteItems = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteItems = demographicWs.getLinkedCachedDemographicPreventionsByDemographicId(demographicNo);
            MiscUtils.getLogger().debug("Saving remote Preventions for " + demographicNo + "  issues : " + remoteItems.size());
            if (remoteItems.size() == 0) {
                return;
            }

            CachedDemographicPrevention[] array = remoteItems.toArray(new CachedDemographicPrevention[remoteItems.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);
            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote forms for " + demographicNo, e);
        }
    }


    /**
     * Retrieves cached patient prevention and immunization data from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve prevention data
     * @return List of CachedDemographicPrevention objects containing immunization and screening records,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedDemographicPrevention> getRemotePreventions(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicPrevention> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicPrevention[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedDemographicPrevention[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicPrevention[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedDemographicPrevention>();
            for (CachedDemographicPrevention cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }


    /**
     * Saves patient medication and prescription data to local fallback storage.
     *
     * <p>Drug information includes current medications, dosages, prescribing information,
     * and medication history essential for safe prescribing and drug interaction checking.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save medication data
     * @since 2012-03-19
     */
    public static void saveDemographicDrugs(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedDemographicDrug> remoteItems = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteItems = demographicWs.getLinkedCachedDemographicDrugsByDemographicId(demographicNo);
            MiscUtils.getLogger().debug("Saving remote Drugs for " + demographicNo + "  issues : " + remoteItems.size());
            if (remoteItems.size() == 0) {
                return;
            }

            CachedDemographicDrug[] array = remoteItems.toArray(new CachedDemographicDrug[remoteItems.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);
            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote forms for " + demographicNo, e);
        }
    }

    /**
     * Retrieves cached patient medication data from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve medication data
     * @return List of CachedDemographicDrug objects containing patient's medication records,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedDemographicDrug> getRemoteDrugs(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicDrug> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicDrug[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedDemographicDrug[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicDrug[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedDemographicDrug>();
            for (CachedDemographicDrug cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }

    /**
     * Saves patient hospital admission data to local fallback storage.
     *
     * <p>Admission data includes hospital stays, discharge information, and institutional care
     * records important for care coordination and medical history.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save admission data
     * @since 2012-03-19
     */
    public static void saveAdmissions(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedAdmission> remoteItems = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteItems = demographicWs.getLinkedCachedAdmissionsByDemographicId(demographicNo);
            MiscUtils.getLogger().debug("Saving remote Admissions for " + demographicNo + "  issues : " + remoteItems.size());
            if (remoteItems.size() == 0) {
                return;
            }

            CachedAdmission[] array = remoteItems.toArray(new CachedAdmission[remoteItems.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);
            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote forms for " + demographicNo, e);
        }
    }

    /**
     * Retrieves cached patient hospital admission data from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve admission data
     * @return List of CachedAdmission objects containing patient's hospital admission records,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedAdmission> getRemoteAdmissions(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedAdmission> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedAdmission[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedAdmission[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedAdmission[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedAdmission>();
            for (CachedAdmission cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }


    /**
     * Saves patient appointment data to local fallback storage.
     *
     * <p>Appointment data includes scheduled visits, appointment history, and scheduling
     * information across multiple healthcare facilities.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save appointment data
     * @since 2012-03-19
     */
    public static void saveAppointments(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedAppointment> remoteItems = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteItems = demographicWs.getLinkedCachedAppointments(demographicNo);
            MiscUtils.getLogger().debug("Saving remote CachedAppointment for " + demographicNo + "  issues : " + remoteItems.size());
            if (remoteItems.size() == 0) {
                return;
            }

            CachedAppointment[] array = remoteItems.toArray(new CachedAppointment[remoteItems.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);
            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote CachedAppointment for " + demographicNo, e);
        }
    }


    /**
     * Retrieves cached patient appointment data from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve appointment data
     * @return List of CachedAppointment objects containing patient's appointment records,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedAppointment> getRemoteAppointments(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedAppointment> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedAppointment[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedAppointment[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedAppointment[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedAppointment>();
            for (CachedAppointment cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }


    /**
     * Saves patient allergy and adverse reaction data to local fallback storage.
     *
     * <p>Allergy information is critical for patient safety, drug prescribing decisions,
     * and treatment planning across all healthcare encounters.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save allergy data
     * @since 2012-03-19
     */
    public static void saveAllergies(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedDemographicAllergy> remoteItems = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteItems = demographicWs.getLinkedCachedDemographicAllergies(demographicNo);
            MiscUtils.getLogger().debug("Saving remote CachedDemographicAllergy for " + demographicNo + "  issues : " + remoteItems.size());
            if (remoteItems.size() == 0) {
                return;
            }

            CachedDemographicAllergy[] array = remoteItems.toArray(new CachedDemographicAllergy[remoteItems.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);
            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote CachedDemographicAllergy for " + demographicNo, e);
        }
    }


    /**
     * Retrieves cached patient allergy data from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve allergy data
     * @return List of CachedDemographicAllergy objects containing patient's allergy and adverse reaction records,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedDemographicAllergy> getRemoteAllergies(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicAllergy> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicAllergy[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedDemographicAllergy[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicAllergy[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedDemographicAllergy>();
            for (CachedDemographicAllergy cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }


    /**
     * Saves patient medical documents to local fallback storage.
     *
     * <p>This method caches both document metadata and content from linked healthcare facilities.
     * Document content is stored separately with composite keys for efficient retrieval.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save medical documents
     * @since 2012-03-19
     */
    public static void saveDocuments(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedDemographicDocument> remoteItems = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteItems = demographicWs.getLinkedCachedDemographicDocuments(demographicNo);
            MiscUtils.getLogger().debug("Saving remote CachedDemographicAllergy for " + demographicNo + "  issues : " + remoteItems.size());
            if (remoteItems.size() == 0) {
                return;
            }

            CachedDemographicDocument[] array = remoteItems.toArray(new CachedDemographicDocument[remoteItems.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);
            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

            for (CachedDemographicDocument document : array) {
                FacilityIdIntegerCompositePk remotePk = document.getFacilityIntegerPk();
                CachedDemographicDocumentContents remoteDocumentContents = demographicWs.getCachedDemographicDocumentContents(remotePk);
                // Cache document content separately with composite key
                MiscUtils.getLogger().debug("what is remotePK " + getPK(remotePk));
                remoteIntegratedDataCopyDao.save(demographicNo, remoteDocumentContents, providerNo, loggedInInfo.getCurrentFacility().getId(), getPK(remotePk));

            }


        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote CachedDemographicDocument for " + demographicNo, e);
        }
    }

    /**
     * Generates a composite primary key string for remote facility documents.
     *
     * @param remotePk the facility and item composite primary key
     * @return String representation combining facility ID and item ID with colon separator
     * @since 2012-03-19
     */
    private static String getPK(FacilityIdIntegerCompositePk remotePk) {
        return (String.valueOf(remotePk.getIntegratorFacilityId()) + ":" + remotePk.getCaisiItemId());
    }

    /**
     * Retrieves the patient demographic number associated with a remote document.
     *
     * @param loggedInInfo the current user's login context containing facility information
     * @param remotePk the remote facility and document composite primary key
     * @return Integer demographic number of the patient associated with the document
     * @since 2012-03-19
     */
    public static Integer getDemographicNoFromRemoteDocument(LoggedInInfo loggedInInfo, FacilityIdIntegerCompositePk remotePk) {
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByType(loggedInInfo.getCurrentFacility().getId(), CachedDemographicDocumentContents.class.getName() + "+" + getPK(remotePk));
        return remoteIntegratedDataCopy.getDemographicNo();
    }


    /**
     * Retrieves cached document content by remote composite key.
     *
     * @param loggedInInfo the current user's login context containing facility information
     * @param remotePk the remote facility and document composite primary key
     * @return CachedDemographicDocumentContents object containing the document content,
     *         or null if not found in local storage
     * @since 2012-03-19
     */
    public static CachedDemographicDocumentContents getRemoteDocument(LoggedInInfo loggedInInfo, FacilityIdIntegerCompositePk remotePk) {
        CachedDemographicDocumentContents documentContents = null;

        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByType(loggedInInfo.getCurrentFacility().getId(), CachedDemographicDocumentContents.class.getName() + "+" + getPK(remotePk));

        try {
            documentContents = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicDocumentContents.class, remoteIntegratedDataCopy);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for remotePK : " + remotePk + " from local store ", e);
        }

        return documentContents;
    }

    /**
     * Retrieves cached document content by patient and remote composite key.
     *
     * @param loggedInInfo the current user's login context containing facility information
     * @param demographicNo the unique patient identifier
     * @param remotePk the remote facility and document composite primary key
     * @return CachedDemographicDocumentContents object containing the document content,
     *         or null if not found in local storage
     * @since 2012-03-19
     */
    public static CachedDemographicDocumentContents getRemoteDocument(LoggedInInfo loggedInInfo, Integer demographicNo, FacilityIdIntegerCompositePk remotePk) {
        CachedDemographicDocumentContents documentContents = null;

        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicDocumentContents.class.getName() + "+" + getPK(remotePk));

        try {
            documentContents = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicDocumentContents.class, remoteIntegratedDataCopy);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }

        return documentContents;
    }


    /**
     * Retrieves cached patient document headers from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve document headers
     * @return List of CachedDemographicDocument objects containing patient's document metadata,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedDemographicDocument> getRemoteDocuments(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicDocument> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicDocument[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            MiscUtils.getLogger().debug("remoteIntegratedDataCopy was null");
            return linkedNotes;
        }

        try {
            CachedDemographicDocument[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicDocument[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedDemographicDocument>();
            MiscUtils.getLogger().debug("linked Doc header size " + linkedNotes);
            for (CachedDemographicDocument cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Document headers for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }


    /**
     * Saves patient laboratory results to local fallback storage.
     *
     * <p>Laboratory results include diagnostic test results, pathology reports,
     * and other clinical laboratory data essential for diagnosis and treatment decisions.</p>
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to save laboratory results
     * @since 2012-03-19
     */
    public static void saveLabResults(LoggedInInfo loggedInInfo, int demographicNo) {
        List<CachedDemographicLabResult> remoteItems = null;

        try {
            String providerNo = null;

            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
                providerNo = loggedInInfo.getLoggedInProviderNo();
            }

            DemographicWs demographicWs = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
            remoteItems = demographicWs.getLinkedCachedDemographicLabResults(demographicNo);
            MiscUtils.getLogger().debug("Saving remote CachedDemographicLabResult for " + demographicNo + "  issues : " + remoteItems.size());
            if (remoteItems.size() == 0) {
                return;
            }

            CachedDemographicLabResult[] array = remoteItems.toArray(new CachedDemographicLabResult[remoteItems.size()]);
            MiscUtils.getLogger().info("logged in " + loggedInInfo + " " + demographicNo);
            remoteIntegratedDataCopyDao.save(demographicNo, array, providerNo, loggedInInfo.getCurrentFacility().getId());

        } catch (Exception e) {
            MiscUtils.getLogger().error("Error saving remote CachedDemographicLabResult for " + demographicNo, e);
        }
    }

    /**
     * Retrieves cached patient laboratory results from local fallback storage.
     *
     * @param loggedInInfo the current user's login context containing facility and provider information
     * @param demographicNo the unique patient identifier for whom to retrieve laboratory results
     * @return List of CachedDemographicLabResult objects containing patient's lab results,
     *         or null if local storage is disabled
     * @since 2012-03-19
     */
    public static List<CachedDemographicLabResult> getLabResults(LoggedInInfo loggedInInfo, Integer demographicNo) {
        if (!OscarProperties.getInstance().getBooleanProperty("INTEGRATOR_LOCAL_STORE", "yes")) return null;

        List<CachedDemographicLabResult> linkedNotes = null;
        RemoteIntegratedDataCopy remoteIntegratedDataCopy = remoteIntegratedDataCopyDao.findByDemoType(loggedInInfo.getCurrentFacility().getId(), demographicNo, CachedDemographicLabResult[].class.getName());

        if (remoteIntegratedDataCopy == null) {
            return linkedNotes;
        }

        try {
            CachedDemographicLabResult[] array = remoteIntegratedDataCopyDao.getObjectFrom(CachedDemographicLabResult[].class, remoteIntegratedDataCopy);
            linkedNotes = new ArrayList<CachedDemographicLabResult>();
            for (CachedDemographicLabResult cdn : array) {
                linkedNotes.add(cdn);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error Loading Notes for : " + demographicNo + " from local store ", e);
        }
        return linkedNotes;
    }

}
