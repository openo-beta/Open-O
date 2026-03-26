//CHECKSTYLE:OFF

package ca.openosp.openo.managers;


import java.nio.file.Path;
import java.util.List;

import ca.openosp.openo.commn.dao.EFormDao.EFormSortOrder;
import ca.openosp.openo.commn.model.EForm;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.commn.model.EncounterForm;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;

import ca.openosp.openo.form.util.FormTransportContainer;
import ca.openosp.openo.encounter.data.EctFormData;
import ca.openosp.openo.encounter.data.EctFormData.PatientForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FormsManager {


    public static final String EFORM = "eform";
    public static final String FORM = "form";

    public List<EForm> findByStatus(LoggedInInfo loggedInInfo, boolean status, EFormSortOrder sortOrder);

    public List<EForm> getEfromInGroupByGroupName(LoggedInInfo loggedInInfo, String groupName);


    public List<String> getGroupNames();


    public List<EFormData> findByDemographicId(LoggedInInfo loggedInInfo, Integer demographicId);

    public List<EncounterForm> getAllEncounterForms();

    public List<EncounterForm> getSelectedEncounterForms();

    public List<PatientForm> getEncounterFormsbyDemographicNumber(LoggedInInfo loggedInInfo, Integer demographicId, boolean getAllVersions, boolean getOnlyPDFReadyForms);

    public Integer saveFormDataAsEDoc(LoggedInInfo loggedInInfo, FormTransportContainer formTransportContainer);

    public Path renderForm(HttpServletRequest request, HttpServletResponse response, Integer formId, Integer demographicNo) throws PDFGenerationException;

    public Path renderForm(LoggedInInfo loggedInInfo, FormTransportContainer formTransportContainer);

    public Path renderForm(HttpServletRequest request, HttpServletResponse response, EctFormData.PatientForm form) throws PDFGenerationException;

    public PatientForm getFormById(LoggedInInfo loggedInInfo, Integer formId, Integer demographicNo);
}

