//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.Vacancy;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface VacancyDao extends AbstractDao<Vacancy> {

    public List<Vacancy> getVacanciesByWlProgramId(Integer wlProgramId);

    public List<Vacancy> getVacanciesByWlProgramIdAndStatus(Integer wlProgramId, String status);

    public List<Vacancy> getVacanciesByName(String vacancyName);

    public List<Vacancy> findByStatusAndVacancyId(String status, int vacancyId);

    public Vacancy getVacancyById(int vacancyId);

    public List<Vacancy> findCurrent();
}
