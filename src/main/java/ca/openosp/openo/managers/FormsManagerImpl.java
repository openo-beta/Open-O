//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.EFormDao;
import ca.openosp.openo.commn.dao.EFormDao.EFormSortOrder;
import ca.openosp.openo.commn.dao.EFormDataDao;
import ca.openosp.openo.commn.dao.EFormGroupDao;
import ca.openosp.openo.commn.dao.EncounterFormDao;
import ca.openosp.openo.commn.model.EForm;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.commn.model.EncounterForm;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PDFGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.openosp.openo.documentManager.ConvertToEdoc;
import ca.openosp.openo.documentManager.EDoc;
import ca.openosp.openo.form.util.FormTransportContainer;
import ca.openosp.openo.form.pdfservlet.GrowthChartConsultPdfRenderer;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.encounter.data.EctFormData;
import ca.openosp.openo.encounter.data.EctFormData.PatientForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class will change soon to incorporate dealing with forms
 */
@Service
public class FormsManagerImpl implements FormsManager {
    private static final Set<String> CONSULT_ATTACHABLE_FORM_TABLES =
            Set.of("formAnnual", "formGrowthChart", "formGrowth0_36");
    private final Logger logger = MiscUtils.getLogger();

    @Autowired
    private EFormDao eformDao;

    @Autowired
    private EFormGroupDao eFormGroupDao;

    @Autowired
    private EFormDataDao eFormDataDao;

    @Autowired
    private EncounterFormDao encounterFormDao;

    @Autowired
    DocumentManager documentManager;

    @Autowired
    private SecurityInfoManager securityInfoManager;

    @Autowired
    private GrowthChartConsultPdfRenderer growthChartConsultPdfRenderer;


    /**
     * Finds all eforms based on the status.
     *
     * @param status    Status to be used when looking up forms.
     * @param sortOrder Order how records should be sorted. Providing no sort order
     *                  delegates to the default sorting order of the persistence
     *                  providers
     * @return Returns the list of all forms with the specified status.
     */
    @Override
    public List<EForm> findByStatus(LoggedInInfo loggedInInfo, boolean status, EFormSortOrder sortOrder) {
        List<EForm> results = eformDao.findByStatus(status, sortOrder);

        if (results.size() > 0) {
            String resultIds = EForm.getIdsAsStringList(results);
            LogAction.addLogSynchronous(loggedInInfo, "FormsManager.findByStatus", "ids returned=" + resultIds);
        }

        return (results);
    }

    /**
     * get eform in group by group name
     *
     * @param groupName
     * @return list of EForms
     */
    @Override
    public List<EForm> getEfromInGroupByGroupName(LoggedInInfo loggedInInfo, String groupName) {
        List<EForm> results = eformDao.getEfromInGroupByGroupName(groupName);
        if (results.size() > 0) {
            String resultIds = EForm.getIdsAsStringList(results);
            LogAction.addLogSynchronous(loggedInInfo, "FormsManager.getEfromInGroupByGroupName",
                    "ids returned=" + resultIds);
        }

        return (results);
    }

    @Override
    public List<String> getGroupNames() {
        return eFormGroupDao.getGroupNames();
    }

    @Override
    public List<EFormData> findByDemographicId(LoggedInInfo loggedInInfo, Integer demographicId) {
        List<EFormData> results = eFormDataDao.findByDemographicId(demographicId);
        if (results.size() > 0) {
            String resultIds = EForm.getIdsAsStringList(results);
            LogAction.addLogSynchronous(loggedInInfo, "FormsManager.findByDemographicId", "ids returned=" + resultIds);
        }

        return (results);

    }

    @Override
    public List<EncounterForm> getAllEncounterForms() {
        List<EncounterForm> results = encounterFormDao.findAll();
        Collections.sort(results, EncounterForm.FORM_NAME_COMPARATOR);
        return (results);
    }

    @Override
    public List<EncounterForm> getSelectedEncounterForms() {
        List<EncounterForm> results = encounterFormDao.findAllNotHidden();
        Collections.sort(results, EncounterForm.FORM_NAME_COMPARATOR);
        return (results);
    }

    @Override
    public List<PatientForm> getEncounterFormsbyDemographicNumber(LoggedInInfo loggedInInfo, Integer demographicId,
                                                                  boolean getAllVersions, boolean getOnlyPDFReadyForms) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_form", SecurityInfoManager.READ, null)) {
            throw new RuntimeException("missing required sec object (_form)");
        }

        return processEncounterForms(loggedInInfo, demographicId, getAllVersions, getOnlyPDFReadyForms);
    }

    private List<PatientForm> processEncounterForms(LoggedInInfo loggedInInfo, Integer demographicId,
                                                    boolean getAllVersions, boolean getOnlyPDFReadyForms) {
        List<PatientForm> patientFormList = new ArrayList<PatientForm>();
        List<EncounterForm> encounterFormList = getAllEncounterForms();
        String[] pdfReadyFormNames = {"Annual"};

        for (EncounterForm encounterForm : encounterFormList) {
            String formName = encounterForm.getFormName();
            if (getOnlyPDFReadyForms && !Arrays.asList(pdfReadyFormNames).contains(formName)) {
                continue;
            }

            String table = encounterForm.getFormTable();
            PatientForm[] patientFormArray = EctFormData.getPatientForms(demographicId + "", table);
            int maxFormsToProcess = getAllVersions ? patientFormArray.length : Math.min(1, patientFormArray.length);
            for (int i = 0; i < maxFormsToProcess; i++) {
                PatientForm patientForm = patientFormArray[i];
                patientForm.setTable(table);
                patientForm.setFormName(formName);
                patientFormList.add(patientForm);
            }
        }

        return patientFormList;
    }

    @Override
    public List<PatientForm> getConsultAttachableForms(LoggedInInfo loggedInInfo, Integer demographicId,
                                                        boolean getAllVersions) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_form", SecurityInfoManager.READ, null)) {
            throw new RuntimeException("missing required sec object (_form)");
        }

        List<PatientForm> forms = new ArrayList<>();
        for (EncounterForm encounterForm : getAllEncounterForms()) {
            if (!CONSULT_ATTACHABLE_FORM_TABLES.contains(encounterForm.getFormTable())) {
                continue;
            }
            PatientForm[] patientForms = EctFormData.getPatientForms(
                    String.valueOf(demographicId), encounterForm.getFormTable());
            int count = getAllVersions ? patientForms.length : Math.min(1, patientForms.length);
            for (int i = 0; i < count; i++) {
                PatientForm form = patientForms[i];
                form.setTable(encounterForm.getFormTable());
                form.setFormName(encounterForm.getFormName());
                forms.add(form);
            }
        }
        return forms;
    }

    @Override
    public PatientForm getConsultForm(LoggedInInfo loggedInInfo, String formTable, Integer formId,
                                      Integer demographicNo) {
        if (!CONSULT_ATTACHABLE_FORM_TABLES.contains(formTable) || formId == null || formId <= 0) {
            return null;
        }
        for (PatientForm form : getConsultAttachableForms(loggedInInfo, demographicNo, true)) {
            if (formTable.equals(form.getTable()) && String.valueOf(formId).equals(form.getFormId())) {
                return form;
            }
        }
        return null;
    }


    private List<String> getPDFReadyFormNames() {
        List<String> pdfReadyFormList = new ArrayList<>();
        pdfReadyFormList.add("Annual");
        return pdfReadyFormList;
    }

    /**
     * Saves a form as PDF EDoc.
     * Returns the id of the converted document.
     */
    @Override
    public Integer saveFormDataAsEDoc(LoggedInInfo loggedInInfo, FormTransportContainer formTransportContainer) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", SecurityInfoManager.WRITE, null)) {
            throw new RuntimeException("missing required sec object (_eform)");
        }

        EDoc edoc = ConvertToEdoc.from(formTransportContainer);
        documentManager.moveDocument(loggedInInfo, edoc.getDocument(), edoc.getFilePath(), null);
        edoc.setFilePath(null);
        Integer documentId = documentManager.saveDocument(loggedInInfo, edoc);

        if (documentId != null) {
            LogAction.addLogSynchronous(loggedInInfo, "FormsManager.saveFormDataAsEDoc",
                    "Document ID saved: " + documentId);
        } else {
            LogAction.addLogSynchronous(loggedInInfo, "FormsManager.saveFormDataAsEDoc",
                    "Document conversion for Form " + edoc.getFileName() + " failed.");
        }

        return documentId;
    }


    /**
     * Please refrain from using this method unless your form ID is sourced from PDF-ready forms, as the form ID alone is not guaranteed to be unique.
     * To generate a PDF of a specific form, provide both the form ID and name, as they collectively ensure accurate identification.
     */
    @Override
    public Path renderForm(HttpServletRequest request, HttpServletResponse response, Integer formId, Integer demographicNo) throws PDFGenerationException {
        EctFormData.PatientForm patientForm = null;
        List<EncounterForm> encounterFormList = getAllEncounterForms();
        List<String> pdfReadyFormList = getPDFReadyFormNames();

        for (EncounterForm encounterForm : encounterFormList) {
            String formName = encounterForm.getFormName();
            String table = encounterForm.getFormTable();
            if (!pdfReadyFormList.contains(formName)) {
                continue;
            }
            patientForm = new PatientForm(table, formName, formId, demographicNo);
        }

        if (patientForm == null) {
            throw new PDFGenerationException("Error Details: Form with id: " + formId + " is not a PDF-ready form");
        }

        return renderForm(request, response, patientForm);
    }

    @Override
    public Path renderForm(LoggedInInfo loggedInInfo, FormTransportContainer formTransportContainer) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_form", SecurityInfoManager.READ, null)) {
            throw new RuntimeException("missing required sec object (_form)");
        }

        LogAction.addLogSynchronous(loggedInInfo, "FormsManager.saveFormAsTempPdf", "");

        return ConvertToEdoc.saveAsTempPDF(formTransportContainer);
    }

    /**
     * This method processes a PatientForm, which can be null, and retrieves data using the 'formId', 'formName',
     * and 'demographicNo' parameters from the HttpServletRequest request.
     *
     * @param form    The PatientForm to process (can be null).
     * @param request The HttpServletRequest containing the parameters.
     */
    @Override
    public Path renderForm(HttpServletRequest request, HttpServletResponse response, EctFormData.PatientForm form) throws PDFGenerationException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() == null) {
            loggedInInfo = LoggedInInfo.getLoggedInInfoFromRequest(request);
        }
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_form", SecurityInfoManager.READ, null)) {
            throw new RuntimeException("missing required sec object (_form)");
        }

        FormTransportContainer formTransportContainer = getFormTransportContainer(request, response, form);
        Path path = null;
        try {
            path = ConvertToEdoc.saveAsTempPDF(formTransportContainer);
        } catch (Exception e) {
            throw new PDFGenerationException("Error Details: Form [" + formTransportContainer.getFormName() + "] could not be converted into a PDF", e);
        }
        return path;
    }

    @Override
    public Path renderConsultForm(HttpServletRequest request, HttpServletResponse response,
                                  EctFormData.PatientForm form) throws PDFGenerationException {
        if (form == null) {
            throw new PDFGenerationException("The encounter form attachment could not be found");
        }
        if ("formGrowthChart".equals(form.getTable()) || "formGrowth0_36".equals(form.getTable())) {
            LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
            if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() == null) {
                loggedInInfo = LoggedInInfo.getLoggedInInfoFromRequest(request);
            }
            if (!securityInfoManager.hasPrivilege(loggedInInfo, "_form", SecurityInfoManager.READ, null)) {
                throw new SecurityException("missing required sec object (_form)");
            }
            LogAction.addLogSynchronous(loggedInInfo, "FormsManager.saveFormAsTempPdf",
                    form.getTable() + "|" + form.getFormId());
            return growthChartConsultPdfRenderer.render(request, form);
        }
        return renderForm(request, response, form);
    }

    @Override
    public Path renderConsultForm(HttpServletRequest request, HttpServletResponse response,
                                  String formTable, Integer formId, Integer demographicNo) throws PDFGenerationException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() == null) {
            loggedInInfo = LoggedInInfo.getLoggedInInfoFromRequest(request);
        }
        PatientForm form = getConsultForm(loggedInInfo, formTable, formId, demographicNo);
        if (form == null) {
            throw new SecurityException("Encounter form does not belong to the patient or is not attachable");
        }
        return renderConsultForm(request, response, form);
    }

    private FormTransportContainer getFormTransportContainer(HttpServletRequest request, HttpServletResponse response,
                                                             EctFormData.PatientForm form) throws PDFGenerationException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String formId = form != null ? form.getFormId() : request.getParameter("formId");
        String formName = form != null ? form.getFormName() : request.getParameter("formName");
        String demographicNo = form != null ? form.getDemoNo() : request.getParameter("demographicNo");
        if (formId == null || formName == null || demographicNo == null) {
            throw new PDFGenerationException("The encounter form rendering identity is incomplete");
        }
        String formPath = "/form/forwardshortcutname.jsp?method=fetch&formname="
                + URLEncoder.encode(formName, StandardCharsets.UTF_8) + "&demographic_no="
                + URLEncoder.encode(demographicNo, StandardCharsets.UTF_8) + "&formId="
                + URLEncoder.encode(formId, StandardCharsets.UTF_8);
        FormTransportContainer formTransportContainer = null;
        try {
            formTransportContainer = new FormTransportContainer(response, request, formPath);
            formTransportContainer.setDemographicNo(demographicNo);
            formTransportContainer.setProviderNo(loggedInInfo.getLoggedInProviderNo());
            formTransportContainer.setSubject(formName + " Form ID " + formId);
            formTransportContainer.setFormName(formName);
            formTransportContainer.setRealPath(request.getServletContext().getRealPath(File.separator));
        } catch (ServletException | IOException e) {
            throw new PDFGenerationException("An error occurred while processing the form. " + "Form name: " + formName,
                    e);
        }
        return formTransportContainer;
    }

    /**
     * Please refrain from using this method unless your form ID is sourced from PDF-ready forms, as the form ID alone is not guaranteed to be unique.
     * Fetch a specific form by providing both the form ID and name, as they collectively ensure accurate identification.
     */
    public PatientForm getFormById(LoggedInInfo loggedInInfo, Integer formId, Integer demographicNo) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_form", SecurityInfoManager.READ, null)) {
            throw new RuntimeException("missing required sec object (_form)");
        }

        PatientForm patientForm = null;
        List<EncounterForm> encounterFormList = getAllEncounterForms();
        List<String> pdfReadyFormList = getPDFReadyFormNames();

        for (EncounterForm encounterForm : encounterFormList) {
            String formName = encounterForm.getFormName();
            String table = encounterForm.getFormTable();
            if (!pdfReadyFormList.contains(formName)) {
                continue;
            }
            patientForm = new PatientForm(table, formName, formId, demographicNo);
            if (patientForm != null) {
                break;
            }
        }

        return patientForm;
    }

}
