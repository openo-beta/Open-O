//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.Vacancy;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class VacancyDaoImpl extends AbstractDaoImpl<Vacancy> implements VacancyDao {

    public VacancyDaoImpl() {
        super(Vacancy.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vacancy> getVacanciesByWlProgramId(Integer wlProgramId) {
        String sqlCommand = "select x from Vacancy x where x.wlProgramId=?1 order by x.name";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, wlProgramId);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vacancy> getVacanciesByWlProgramIdAndStatus(Integer wlProgramId, String status) {
        String sqlCommand = "select x from Vacancy x where x.wlProgramId=?1 and x.status=?2 order by x.name";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, wlProgramId);
        query.setParameter(2, status);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vacancy> getVacanciesByName(String vacancyName) {
        String sqlCommand = "select x from Vacancy x where x.name=?1 order by x.name";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, vacancyName);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vacancy> findByStatusAndVacancyId(String status, int vacancyId) {
        String sqlCommand = "select x from Vacancy x where x.status=?1 and x.id=?2";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, status);
        query.setParameter(2, vacancyId);

        return query.getResultList();
    }

    @Override
    public Vacancy getVacancyById(int vacancyId) {
        String sqlCommand = "select x from Vacancy x where x.id=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, vacancyId);

        List<Vacancy> l = query.getResultList();
        if (l != null && !l.isEmpty())
            return l.get(0);
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vacancy> findCurrent() {
        String sqlCommand = "select x from Vacancy x where x.status=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, "ACTIVE");
        ;

        return query.getResultList();
    }
}
