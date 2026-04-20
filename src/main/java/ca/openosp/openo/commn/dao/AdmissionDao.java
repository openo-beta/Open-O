//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import ca.openosp.openo.PMmodule.dao.ProgramDao;
import ca.openosp.openo.PMmodule.model.AdmissionSearchBean;
import ca.openosp.openo.commn.model.Admission;

public interface AdmissionDao extends AbstractDao<Admission> {

    public List<Admission> getAdmissions_archiveView(Integer programId, Integer demographicNo);

    public Admission getAdmission(Integer programId, Integer demographicNo);

    public Admission getCurrentAdmission(Integer programId, Integer demographicNo);

    public List<Admission> getAdmissions();

    public List<Admission> getAdmissions(Integer demographicNo);

    public List<Admission> getAdmissionsASC(Integer demographicNo);

    public List<Admission> getAdmissionsByFacility(Integer demographicNo, Integer facilityId);


    public List<Admission> getAdmissionsByProgramAndClient(Integer demographicNo, Integer programId);

    public List<Admission> getAdmissionsByProgramId(Integer programId, Boolean automaticDischarge, Integer days);

    public List<Integer> getAdmittedDemographicIdByProgramAndProvider(Integer programId, String providerNo);

    public List<Admission> getCurrentAdmissions(Integer demographicNo);

    public List<Admission> getDischargedAdmissions(Integer demographicNo);

    public List<Admission> getCurrentAdmissionsByFacility(Integer demographicNo, Integer facilityId);

    public Admission getCurrentExternalProgramAdmission(ProgramDao programDAO, Integer demographicNo);


    public List<Admission> getCurrentServiceProgramAdmission(ProgramDao programDAO, Integer demographicNo);

    public Admission getCurrentCommunityProgramAdmission(ProgramDao programDAO, Integer demographicNo);

    public List<Admission> getCurrentAdmissionsByProgramId(Integer programId);

    public Admission getAdmission(int id);

    public Admission getAdmission(Long id);

    public void saveAdmission(Admission admission);

    public List<Admission> getAdmissionsInTeam(Integer programId, Integer teamId);

    public Admission getTemporaryAdmission(Integer demographicNo);

    public List search(AdmissionSearchBean searchBean);

    public List<Admission> getClientIdByProgramDate(int programId, Date dt);

    public Integer getLastClientStatusFromAdmissionByProgramIdAndClientId(Integer programId, Integer demographicId);

    public List<Admission> getAdmissionsByProgramAndAdmittedDate(int programId, Date startDate, Date endDate);

    public List<Admission> getAdmissionsByProgramAndDate(int programId, Date startDate, Date endDate);

    public boolean wasInProgram(Integer programId, Integer clientId);

    public List getActiveAnonymousAdmissions();

    public List<Admission> getAdmissionsByFacilitySince(Integer demographicNo, Integer facilityId, Date lastUpdateDate);

    public List<Integer> getAdmissionsByFacilitySince(Integer facilityId, Date lastUpdateDate);

    public List<Admission> findAdmissionsByProgramAndDate(Integer programNo, Date day, int startIndex, int numToReturn);

    public Integer findAdmissionsByProgramAndDateAsCount(Integer programNo, Date day);
}
