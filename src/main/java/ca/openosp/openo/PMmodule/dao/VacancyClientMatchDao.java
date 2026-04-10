//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.VacancyClientMatch;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface VacancyClientMatchDao extends AbstractDao<VacancyClientMatch> {

    public List<VacancyClientMatch> findByClientIdAndVacancyId(int clientId, int vacancyId);

    public List<VacancyClientMatch> findByClientId(int clientId);

    public List<VacancyClientMatch> findBystatus(String status);

    public void updateStatus(String status, int clientId, int vacancyId);

    public void updateStatusAndRejectedReason(String status, String rejectedReason, int clientId, int vacancyId);

}
