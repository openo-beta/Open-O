//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.model.Program;

public interface ProgramDao {


    public boolean isServiceProgram(Integer programId);

    public boolean isCommunityProgram(Integer programId);

    public boolean isExternalProgram(Integer programId);

    public Program getProgram(Integer programId);

    public Program getProgramForApptView(Integer programId);

    public String getProgramName(Integer programId);

    public Integer getProgramIdByProgramName(String programName);

    public List<Program> findAll();

    public List<Program> getAllPrograms();

    public List<Program> getAllActivePrograms();

    public List<Program> getAllPrograms(String programStatus, String type, int facilityId);

    public List<Program> getPrograms();

    public List<Program> getActivePrograms();

    public List<Program> getProgramsByFacilityId(Integer facilityId);

    public List<Program> getProgramsByFacilityIdAndFunctionalCentreId(Integer facilityId, String functionalCentreId);

    public List<Program> getCommunityProgramsByFacilityId(Integer facilityId);

    public List<Program> getProgramsByType(Integer facilityId, String type, Boolean active);

    public List<Program> getProgramByGenderType(String genderType);

    public void saveProgram(Program program);

    public void removeProgram(Integer programId);

    public List<Program> search(Program program);

    public List<Program> searchByFacility(Program program, Integer facilityId);

    public void resetHoldingTank();

    public Program getHoldingTankProgram();

    public boolean programExists(Integer programId);


    public boolean isInSameFacility(Integer programId1, Integer programId2);

    public Program getProgramBySiteSpecificField(String value);

    public Program getProgramByName(String value);

    public List<Integer> getRecordsAddedAndUpdatedSinceTime(Integer facilityId, Date date);

    public List<Integer> getRecordsByFacilityId(Integer facilityId);

    public List<String> getRecordsAddedAndUpdatedSinceTime(Date date);
}
