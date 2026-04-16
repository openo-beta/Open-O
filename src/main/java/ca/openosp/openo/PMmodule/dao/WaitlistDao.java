//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.dao;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ca.openosp.openo.PMmodule.wlmatch.CriteriasBO;
import ca.openosp.openo.PMmodule.wlmatch.MatchBO;
import ca.openosp.openo.PMmodule.wlmatch.VacancyDisplayBO;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.match.client.ClientData;
import ca.openosp.openo.match.vacancy.VacancyData;

public interface WaitlistDao {

    public List<MatchBO> getClientMatches(int vacancyId);

    public List<MatchBO> getClientMatchesWithMinPercentage(int vacancyId, double percentage);

    public Collection<EFormData> searchForMatchingEforms(CriteriasBO crits);

    public List<VacancyDisplayBO> listDisplayVacanciesForWaitListProgram(int programID);

    public List<VacancyDisplayBO> listDisplayVacanciesForAllWaitListPrograms();

    public List<VacancyDisplayBO> getDisplayVacanciesForAgencyProgram(int programID);

    public VacancyDisplayBO getDisplayVacancy(int vacancyID);

    public void loadStats(VacancyDisplayBO bo);

    public Integer getProgramIdByVacancyId(int vacancyId);

    public List<VacancyDisplayBO> listNoOfVacanciesForWaitListProgram();

    public List<VacancyDisplayBO> listVacanciesForWaitListProgram();

    public List<ClientData> getAllClientsData();

    public List<ClientData> getAllClientsDataByProgramId(int wlProgramId);

    public ClientData getClientData(int clientId);

    // private static final String field_type_one = "select_one";
    // private static final String field_type_number = "number";

    public VacancyData loadVacancyData(final int vacancyId);

    public VacancyData loadVacancyData(final int vacancyId, final int wlProgramId);

}
