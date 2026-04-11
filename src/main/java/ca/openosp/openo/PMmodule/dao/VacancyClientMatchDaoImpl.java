//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.VacancyClientMatch;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class VacancyClientMatchDaoImpl extends AbstractDaoImpl<VacancyClientMatch> implements VacancyClientMatchDao {

    public VacancyClientMatchDaoImpl() {
        super(VacancyClientMatch.class);
    }

    @Override
    public List<VacancyClientMatch> findByClientIdAndVacancyId(int clientId, int vacancyId) {
        Query q = entityManager
                .createQuery("select x from VacancyClientMatch x where x.client_id = ?1 and x.vacancy_id = ?2");
        q.setParameter(1, clientId);
        q.setParameter(2, vacancyId);

        @SuppressWarnings("unchecked")
        List<VacancyClientMatch> results = q.getResultList();

        return results;
    }

    @Override
    public List<VacancyClientMatch> findByClientId(int clientId) {
        Query q = entityManager.createQuery("select x from VacancyClientMatch x where x.client_id = ?1");
        q.setParameter(1, clientId);

        @SuppressWarnings("unchecked")
        List<VacancyClientMatch> results = q.getResultList();

        return results;
    }

    @Override
    public List<VacancyClientMatch> findBystatus(String status) {
        Query q = entityManager.createQuery("select x from VacancyClientMatch x where x.status = ?1");
        q.setParameter(1, status);

        @SuppressWarnings("unchecked")
        List<VacancyClientMatch> results = q.getResultList();

        return results;
    }

    @Override
    public void updateStatus(String status, int clientId, int vacancyId) {
        for (VacancyClientMatch v : this.findByClientIdAndVacancyId(clientId, vacancyId)) {
            v.setStatus(status);
        }
    }

    @Override
    public void updateStatusAndRejectedReason(String status, String rejectedReason, int clientId, int vacancyId) {
        for (VacancyClientMatch v : this.findByClientIdAndVacancyId(clientId, vacancyId)) {
            v.setStatus(status);
            v.setRejectionReason(rejectedReason);
        }
    }

}
