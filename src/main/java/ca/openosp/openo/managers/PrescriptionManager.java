//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.commn.model.Prescription;
import ca.openosp.openo.utility.LoggedInInfo;

import java.util.Date;
import java.util.List;
import java.util.Calendar;

public interface PrescriptionManager {

    public Prescription getPrescription(LoggedInInfo loggedInInfo, Integer prescriptionId);

    public List<Prescription> getPrescriptionUpdatedAfterDate(LoggedInInfo loggedInInfo,
                                                              Date updatedAfterThisDateExclusive, int itemsToReturn);

    public List<Prescription> getPrescriptionByDemographicIdUpdatedAfterDate(LoggedInInfo loggedInInfo,
                                                                             Integer demographicId, Date updatedAfterThisDateExclusive);

    public List<Drug> getDrugsByScriptNo(LoggedInInfo loggedInInfo, Integer scriptNo, Boolean archived);

    public List<Drug> getUniqueDrugsByPatient(LoggedInInfo loggedInInfo, Integer demographicNo);

    public List<Prescription> getPrescriptionsByProgramProviderDemographicDate(LoggedInInfo loggedInInfo,
                                                                               Integer programId, String providerNo, Integer demographicId, Calendar updatedAfterThisDateExclusive,
                                                                               int itemsToReturn);

    public Prescription createNewPrescription(LoggedInInfo info, List<Drug> drugs, Integer demographicNo);

    public List<Drug> getMedicationsByDemographicNo(LoggedInInfo loggedInInfo, Integer demographicNo, Boolean archived);

    public List<Drug> getActiveMedications(LoggedInInfo loggedInInfo, String demographicNo);

    public List<Drug> getActiveMedications(LoggedInInfo loggedInInfo, Integer demographicNo);

    public Drug findDrugById(LoggedInInfo loggedInInfo, Integer drugId);

    public List<Drug> getLongTermDrugs(LoggedInInfo loggedInInfo, Integer demographicId);

    public List<Prescription> getPrescriptions(LoggedInInfo loggedInInfo, Integer demographicId);

    public boolean print(LoggedInInfo loggedInInfo, int scriptNo);

    boolean setPrescriptionSignature(LoggedInInfo loggedInInfo, int scriptNo, Integer digitalSignatureId);
}
