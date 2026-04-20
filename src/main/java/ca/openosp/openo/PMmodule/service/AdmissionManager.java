//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.exception.AdmissionException;
import ca.openosp.openo.PMmodule.exception.AlreadyAdmittedException;
import ca.openosp.openo.PMmodule.exception.ProgramFullException;
import ca.openosp.openo.PMmodule.exception.ServiceRestrictionException;
import ca.openosp.openo.PMmodule.model.AdmissionSearchBean;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.commn.model.Admission;
import ca.openosp.openo.commn.model.JointAdmission;
import ca.openosp.openo.utility.LoggedInInfo;

public interface AdmissionManager {

    List<Admission> getAdmissions_archiveView(String programId, Integer demographicNo);

    Admission getAdmission(String programId, Integer demographicNo);

    Admission getCurrentAdmission(String programId, Integer demographicNo);

    List<Admission> getAdmissionsByFacility(Integer demographicNo, Integer facilityId);

    List<Admission> getCurrentAdmissionsByFacility(Integer demographicNo, Integer facilityId);

    List<Admission> getAdmissions();

    List<Admission> getAdmissions(Integer demographicNo);

    List<Admission> getCurrentAdmissions(Integer demographicNo);


    List<Admission> getCurrentServiceProgramAdmission(Integer demographicNo);

    Admission getCurrentExternalProgramAdmission(Integer demographicNo);

    Admission getCurrentCommunityProgramAdmission(Integer demographicNo);

    List<Admission> getCurrentAdmissionsByProgramId(String programId);

    Admission getAdmission(Long id);

    Admission getAdmission(Integer id);

    void saveAdmission(Admission admission);

    void processAdmission(Integer demographicNo, String providerNo, Program program, String dischargeNotes, String admissionNotes) throws ProgramFullException, AdmissionException, ServiceRestrictionException;

    void processAdmission(Integer demographicNo, String providerNo, Program program, String dischargeNotes, String admissionNotes, boolean tempAdmission) throws ProgramFullException, AdmissionException, ServiceRestrictionException;

    void processAdmission(Integer demographicNo, String providerNo, Program program, String dischargeNotes, String admissionNotes, boolean tempAdmission, List<Integer> dependents) throws ProgramFullException, AdmissionException, ServiceRestrictionException;

    void processAdmission(Integer demographicNo, String providerNo, Program program, String dischargeNotes, String admissionNotes, boolean tempAdmission, boolean overrideRestriction) throws ProgramFullException, AdmissionException, ServiceRestrictionException;

    void processAdmission(Integer demographicNo, String providerNo, Program program, String dischargeNotes, String admissionNotes, Date admissionDate) throws ProgramFullException, AdmissionException, ServiceRestrictionException;

    void processAdmission(Integer demographicNo, String providerNo, Program program, String dischargeNotes, String admissionNotes, boolean tempAdmission, List<Integer> dependents, Date admissionDate) throws ProgramFullException, AdmissionException, ServiceRestrictionException;

    void processAdmission(Integer demographicNo, String providerNo, Program program, String dischargeNotes, String admissionNotes, boolean tempAdmission, Date admissionDate, boolean overrideRestriction, List<Integer> dependents) throws ProgramFullException, AdmissionException, ServiceRestrictionException;

    void processInitialAdmission(Integer demographicNo, String providerNo, Program program, String admissionNotes, Date admissionDate) throws ProgramFullException, AlreadyAdmittedException, ServiceRestrictionException;

    Admission getTemporaryAdmission(Integer demographicNo);

    List<Admission> getCurrentTemporaryProgramAdmission(Integer demographicNo);

    boolean isDependentInDifferentProgramFromHead(Integer demographicNo, List<JointAdmission> dependentList);

    List search(AdmissionSearchBean searchBean);

    void processDischarge(Integer programId, Integer demographicNo, String dischargeNotes, String radioDischargeReason) throws AdmissionException;

    void processDischarge(Integer programId, Integer demographicNo, String dischargeNotes, String radioDischargeReason, Date dischargeDate) throws AdmissionException;

    void processDischarge(Integer programId, Integer demographicNo, String dischargeNotes, String radioDischargeReason, Date dischargeDate, List<Integer> dependents, boolean fromTransfer, boolean automaticDischarge) throws AdmissionException;

    void processDischargeToCommunity(Integer communityProgramId, Integer demographicNo, String providerNo, String notes, String radioDischargeReason, Date dischargeDate) throws AdmissionException;

    void processDischargeToCommunity(Integer communityProgramId, Integer demographicNo, String providerNo, String notes, String radioDischargeReason, List<Integer> dependents, Date dischargeDate) throws AdmissionException;

    boolean isActiveInCurrentFacility(LoggedInInfo loggedInInfo, int demographicId);

    List getActiveAnonymousAdmissions();

    boolean wasInProgram(Integer programId, Integer clientId);

    List<Admission> findAdmissionsByProgramAndDate(LoggedInInfo loggedInInfo, Integer programNo, Date day, int startIndex, int numToReturn);

    Integer findAdmissionsByProgramAndDateAsCount(LoggedInInfo loggedInInfo, Integer programNo, Date day);
}
