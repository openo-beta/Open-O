//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;

import ca.openosp.openo.eform.data.EForm;
import ca.openosp.openo.encounter.data.EctFormData;

public interface EformDataManager {

    public Integer saveEformData(LoggedInInfo loggedInInfo, EForm eform);

    /**
     * Saves an form as PDF EDoc.
     * Returns the Eform id that was saved.
     */
    public Integer saveEformDataAsEDoc(LoggedInInfo loggedInInfo, String fdid);

    public Integer saveEFormWithAttachmentsAsEDoc(LoggedInInfo loggedInInfo, String fdid, String demographicId, Path eFormPDFPath) throws PDFGenerationException;

    public EFormData findByFdid(LoggedInInfo loggedInInfo, Integer fdid);

    /**
     * Saves an form as PDF in a temp directory.
     * <p>
     * Path to a temp file is returned. Remember to change the .tmp filetype and to delete the tmp file when finished.
     */
    public Path createEformPDF(LoggedInInfo loggedInInfo, int fdid) throws PDFGenerationException;


    /**
     * Get all current eForms by demographic number but do not include the HTML data.
     * This is a good method for getting just the list and status of eForms. It's a little lighter on the database.
     * <p>
     * Returns a map - not an entity
     */
    public List<Map<String, Object>> findCurrentByDemographicIdNoData(LoggedInInfo loggedInInfo, Integer demographicId);

    public ArrayList<HashMap<String, ? extends Object>> getHRMDocumentsAttachedToEForm(LoggedInInfo loggedInInfo, String fdid, String demographicId);

    public List<EctFormData.PatientForm> getFormsAttachedToEForm(LoggedInInfo loggedInInfo, String fdid, String demographicId);

    public void removeEFormData(LoggedInInfo loggedInInfo, String fdid);

}
