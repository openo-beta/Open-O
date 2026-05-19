package ca.openosp.openo.webserv.rest;

//import ca.openosp.openo.PMmodule.model.SecUserRole;
import ca.openosp.openo.casemgmt.model.CaseManagementNote;
import ca.openosp.openo.casemgmt.service.NoteSelectionCriteria;
import ca.openosp.openo.casemgmt.service.NoteSelectionResult;
import ca.openosp.openo.casemgmt.service.NoteService;
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.commn.model.Measurement;
import ca.openosp.openo.commn.model.Prescription;
import ca.openosp.openo.managers.AllergyManager;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.managers.DocumentManager;
import ca.openosp.openo.managers.MeasurementManager;
import ca.openosp.openo.managers.PrescriptionManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ca.openosp.openo.casemgmt.model.CaseManagementNote;
import ca.openosp.openo.casemgmt.service.NoteSelectionCriteria;
import ca.openosp.openo.casemgmt.service.NoteSelectionResult;
import ca.openosp.openo.casemgmt.service.NoteService;
import ca.openosp.openo.PMmodule.dao.SecUserRoleDao;
import ca.openosp.openo.PMmodule.model.SecUserRole;
import java.util.stream.Collectors;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Builds an AI-ready patient export string using OSCAR managers.
 * This class is a plain Spring component and does NOT depend on AbstractWs.
 */
@Component
public class LLMExtractorService {

    private static final Logger logger = MiscUtils.getLogger();
    private static final Date EARLIEST_DATE = new Date(0L);

    @Autowired
    private DemographicManager demographicManager;

    @Autowired
    private AllergyManager allergyManager;

    @Autowired
    private PrescriptionManager prescriptionManager;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private MeasurementManager measurementManager;

    @Autowired
    private NoteService noteService;

    @Autowired
    private SecUserRoleDao secUserRoleDao;

    /**
     * Main export method.
     *
     * @param loggedInInfo current OSCAR logged-in context
     * @param demographicId target patient demographic id
     * @return formatted patient export text
     */
    public String generatePatientExport(LoggedInInfo loggedInInfo, Integer demographicId) {
        StringBuilder out = new StringBuilder(4096);

        out.append("PATIENT EXPORT").append('\n');
        out.append("==============").append('\n');

        if (demographicId == null) {
            out.append("ERROR: demographicId is required.");
            return out.toString();
        }

        if (loggedInInfo == null) {
            out.append("ERROR: No logged-in context available.");
            return out.toString();
        }

        Demographic demographic = null;
        try {
            demographic = demographicManager.getDemographic(loggedInInfo, demographicId);
        } catch (Exception e) {
            logger.error("Failed to load demographic {}", demographicId, e);
        }

        appendDemographicsSection(out, demographic);

        List<Allergy> allergies = getAllergies(loggedInInfo, demographicId);
        appendAllergiesSection(out, allergies);

        List<Prescription> prescriptions = getPrescriptions(loggedInInfo, demographicId);
        List<Drug> medications = getMedications(loggedInInfo, demographicId);
        appendMedicationsSection(out, prescriptions, medications);

        List<Document> documents = getDocuments(loggedInInfo, demographicId);
        appendDocumentsSection(out, documents);

        List<Measurement> measurements = getMeasurements(loggedInInfo, demographicId);
        appendMeasurementsSection(out, measurements);

        // notes are handled a bit differently since they come from the case management module and have rich text
        // that may contain newlines, so we will append them in a more free-form way
        List<CaseManagementNote> notes = getNotes(loggedInInfo, demographicId);
        appendNotesSection(out, notes);

        return out.toString();
    }

    private void appendDemographicsSection(StringBuilder out, Demographic d) {
        out.append('\n').append("DEMOGRAPHICS").append('\n');
        out.append("------------").append('\n');

        if (d == null) {
            out.append("- No demographic record found or accessible.").append('\n');
            return;
        }

        out.append("- Demographic No: ").append(nvl(d.getDemographicNo())).append('\n');
        out.append("- Name: ").append(nvl(d.getDisplayName())).append('\n');
        out.append("- Chart No: ").append(nvl(d.getChartNo())).append('\n');
        out.append("- DOB: ").append(buildDob(d)).append('\n');
        out.append("- Sex: ").append(nvl(d.getSex())).append('\n');
        out.append("- HIN: ").append(nvl(d.getHin())).append('\n');
        out.append("- Phone (Home): ").append(nvl(d.getPhone())).append('\n');
        out.append("- Phone (Work): ").append(nvl(d.getPhone2())).append('\n');
        out.append("- Email: ").append(nvl(d.getEmail())).append('\n');
        out.append("- Address: ").append(nvl(d.getAddress())).append('\n');
        out.append("- City: ").append(nvl(d.getCity())).append('\n');
        out.append("- Province: ").append(nvl(d.getProvince())).append('\n');
        out.append("- Postal: ").append(nvl(d.getPostal())).append('\n');
    }

    private void appendAllergiesSection(StringBuilder out, List<Allergy> allergies) {
        out.append('\n').append("ALLERGIES").append('\n');
        out.append("---------").append('\n');

        if (allergies.isEmpty()) {
            out.append("- None found.").append('\n');
            return;
        }

        for (Allergy a : allergies) {
            String typeDesc = a.getTypeCode() != null ? Allergy.getTypeDesc(a.getTypeCode()) : "Unknown";
            out.append("- [")
                    .append(nvl(typeDesc))
                    .append("] ")
                    .append(nvl(a.getDescription()))
                    .append(" | Reaction: ")
                    .append(nvl(a.getReaction()))
                    .append(" | Start: ")
                    .append(fmt(a.getStartDate()))
                    .append(" | Updated: ")
                    .append(fmt(a.getLastUpdateDate()))
                    .append('\n');
        }
    }

    private void appendMedicationsSection(StringBuilder out, List<Prescription> prescriptions, List<Drug> medications) {
        out.append('\n').append("MEDICATIONS").append('\n');
        out.append("-----------").append('\n');

        if (prescriptions.isEmpty() && medications.isEmpty()) {
            out.append("- None found.").append('\n');
            return;
        }

        if (!medications.isEmpty()) {
            out.append("Medications:").append('\n');
            for (Drug d : medications) {
                out.append("- ")
                        .append(resolveDrugName(d))
                        .append(" | Dosage: ").append(nvl(d.getDosage()))
                        .append(" | Freq: ").append(nvl(d.getFreqCode()))
                        .append(" | Route: ").append(nvl(d.getRoute()))
                        .append(" | Special: ").append(nvl(d.getSpecial()))
                        .append(" | ScriptNo: ").append(nvl(d.getScriptNo()))
                        .append(" | RxDate: ").append(fmt(d.getRxDate()))
                        .append(" | EndDate: ").append(fmt(d.getEndDate()))
                        .append(" | Archived: ").append(d.isArchived())
                        .append('\n');
            }
        }

        if (!prescriptions.isEmpty()) {
            out.append("Prescriptions:").append('\n');
            for (Prescription p : prescriptions) {
                out.append("- RxId: ").append(nvl(p.getId()))
                        .append(" | Date Prescribed: ").append(fmt(p.getDatePrescribed()))
                        .append(" | Last Updated: ").append(fmt(p.getLastUpdateDate()))
                        .append(" | Comments: ").append(nvl(p.getComments()))
                        .append(" | Text: ").append(nvl(p.getTextView()))
                        .append('\n');
            }
        }
    }

    private void appendDocumentsSection(StringBuilder out, List<Document> documents) {
        out.append('\n').append("DOCUMENTS / NOTES").append('\n');
        out.append("-----------------").append('\n');

        if (documents.isEmpty()) {
            out.append("- None found.").append('\n');
            return;
        }

        for (Document d : documents) {
            out.append("- DocNo: ").append(nvl(d.getDocumentNo()))
                    .append(" | Title: ").append(firstNonBlank(d.getDocdesc(), d.getDocfilename(), "Untitled"))
                    .append(" | ContentType: ").append(nvl(d.getContenttype()))
                    .append(" | Observation Date: ").append(fmt(d.getObservationdate()))
                    .append(" | Updated: ").append(fmt(d.getUpdatedatetime()))
                    .append(" | Creator: ").append(nvl(d.getDoccreator()))
                    .append(" | Source: ").append(nvl(d.getSource()))
                    .append('\n');
        }
    }

    private void appendMeasurementsSection(StringBuilder out, List<Measurement> measurements) {
        out.append('\n').append("MEASUREMENTS / LABS").append('\n');
        out.append("-------------------").append('\n');

        if (measurements.isEmpty()) {
            out.append("- None found.").append('\n');
            return;
        }

        for (Measurement m : measurements) {
            out.append("- Type: ").append(nvl(m.getType()))
                    .append(" | Value: ").append(nvl(m.getDataField()))
                    .append(" | Instruction: ").append(nvl(m.getMeasuringInstruction()))
                    .append(" | Observed: ").append(fmt(m.getDateObserved()))
                    .append(" | Entered: ").append(fmt(m.getCreateDate()))
                    .append(" | Comments: ").append(nvl(m.getComments()))
                    .append(" | Provider: ").append(nvl(m.getProviderNo()))
                    .append('\n');
        }
    }
    
    private void appendNotesSection(StringBuilder out, List<CaseManagementNote> notes) {
        out.append('\n').append("ENCOUNTER NOTES").append('\n');
        out.append("---------------").append('\n');

        if (notes.isEmpty()) {
            out.append("- None found.").append('\n');
            return;
        }

        for (CaseManagementNote n : notes) {
            out.append("- Date: ").append(fmt(n.getObservation_date()))
            .append(" | Provider: ").append(nvl(n.getProviderNo()))
            .append(" | Type: ").append(nvl(n.getEncounter_type()))
            .append(" | Signed: ").append(n.isSigned())
            .append('\n');

            // Append the actual note text indented
            if (n.getNote() != null && !n.getNote().trim().isEmpty()) {
                String[] lines = n.getNote().split("\n");
                for (String line : lines) {
                    out.append("  ").append(line.trim()).append('\n');
                }
            }
            out.append('\n');
        }
    }

    private List<Allergy> getAllergies(LoggedInInfo loggedInInfo, Integer demographicId) {
        try {
            List<Allergy> list = allergyManager.getByDemographicIdUpdatedAfterDate(loggedInInfo, demographicId, EARLIEST_DATE);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to load allergies for demographic {}", demographicId, e);
            return Collections.emptyList();
        }
    }

    private List<Prescription> getPrescriptions(LoggedInInfo loggedInInfo, Integer demographicId) {
        try {
            List<Prescription> list = prescriptionManager.getPrescriptionByDemographicIdUpdatedAfterDate(loggedInInfo, demographicId, EARLIEST_DATE);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to load prescriptions for demographic {}", demographicId, e);
            return Collections.emptyList();
        }
    }

    private List<Drug> getMedications(LoggedInInfo loggedInInfo, Integer demographicId) {
        try {
            List<Drug> list = prescriptionManager.getMedicationsByDemographicNo(loggedInInfo, demographicId, false);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to load medications for demographic {}", demographicId, e);
            return Collections.emptyList();
        }
    }

    private List<Document> getDocuments(LoggedInInfo loggedInInfo, Integer demographicId) {
        try {
            List<Document> list = documentManager.getDocumentsByDemographicIdUpdateAfterDate(loggedInInfo, demographicId, EARLIEST_DATE);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to load documents for demographic {}", demographicId, e);
            return Collections.emptyList();
        }
    }

    private List<Measurement> getMeasurements(LoggedInInfo loggedInInfo, Integer demographicId) {
        try {
            List<Measurement> list = measurementManager.getMeasurementByDemographicIdAfter(loggedInInfo, demographicId, EARLIEST_DATE);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to load measurements for demographic {}", demographicId, e);
            return Collections.emptyList();
        }
    }

    private List<CaseManagementNote> getNotes(LoggedInInfo loggedInInfo, Integer demographicId) {
    try {
        NoteSelectionCriteria criteria = new NoteSelectionCriteria();
        criteria.setDemographicId(demographicId);
        criteria.setMaxResults(200);       // fetch up to 200 notes
        criteria.setFirstResult(0);
        criteria.setNoteSort("observation_date_desc");
        criteria.setSliceFromEndOfList(false);

        // Get user role for note access control
        List<SecUserRole> userRoles = secUserRoleDao.getUserRoles(
            loggedInInfo.getLoggedInProviderNo()
        );
        if (userRoles != null && !userRoles.isEmpty()) {
            criteria.setUserRole(SecUserRole.getRoleNameAsCsv(userRoles));
        }
        criteria.setUserName(loggedInInfo.getLoggedInProviderNo());

        NoteSelectionResult result = noteService.findNotes(loggedInInfo, criteria);
        if (result == null) return Collections.emptyList();

        // Convert NoteDisplay to CaseManagementNote-like text
        return result.getNotes().stream()
            .map(nd -> {
                CaseManagementNote n = new CaseManagementNote();
                n.setNote(nd.getNote());
                n.setObservation_date(nd.getObservationDate());
                n.setUpdate_date(nd.getUpdateDate());
                n.setProviderNo(nd.getProviderNo());
                n.setEncounter_type(nd.getEncounterType());
                n.setSigned(nd.isSigned());
                return n;
            })
            .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to load notes for demographic {}", demographicId, e);
            return Collections.emptyList();
        }
    }

    private String resolveDrugName(Drug d) {
        return firstNonBlank(d.getBrandName(), d.getGenericName(), d.getCustomName(), "Unnamed medication");
    }

    private String buildDob(Demographic d) {
        String direct = d.getDateOfBirth();
        if (!isBlank(direct) && !isBlank(d.getMonthOfBirth()) && !isBlank(d.getYearOfBirth())) {
            return d.getYearOfBirth() + "-" + d.getMonthOfBirth() + "-" + direct;
        }
        return firstNonBlank(direct, "N/A");
    }

    private String fmt(Date date) {
        return date == null ? "N/A" : date.toString();
    }

    private String nvl(Object value) {
        if (value == null) return "N/A";
        String s = String.valueOf(value);
        return isBlank(s) ? "N/A" : s;
    }

    private String firstNonBlank(String... values) {
        if (values == null) return "N/A";
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return "N/A";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}