//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.dao.ClientReferralDAO;
import ca.openosp.openo.PMmodule.dao.ProgramQueueDao;
import ca.openosp.openo.PMmodule.dao.VacancyDao;
import ca.openosp.openo.PMmodule.dao.VacancyTemplateDao;
import ca.openosp.openo.PMmodule.model.ClientReferral;
import ca.openosp.openo.PMmodule.model.ProgramQueue;
import ca.openosp.openo.PMmodule.model.Vacancy;
import ca.openosp.openo.PMmodule.model.VacancyTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ProgramQueueManagerImpl implements ProgramQueueManager {
    private ProgramQueueDao dao;
    private ClientReferralDAO referralDAO;
    private VacancyTemplateDao vacancyTemplateDao;
    private VacancyDao vacancyDao;

    public void setVacancyDao(VacancyDao vacancyDao) {
        this.vacancyDao = vacancyDao;
    }

    public void setVacancyTemplateDao(VacancyTemplateDao vacancyTemplateDao) {
        this.vacancyTemplateDao = vacancyTemplateDao;
    }

    public void setProgramQueueDao(ProgramQueueDao dao) {
        this.dao = dao;
    }

    public void setClientReferralDAO(ClientReferralDAO dao) {
        this.referralDAO = dao;
    }

    public ProgramQueue getProgramQueue(String queueId) {
        ProgramQueue pq = dao.getProgramQueue(Long.valueOf(queueId));
        return pq;
    }

    public List<ProgramQueue> getProgramQueuesByProgramId(Long programId) {
        return dao.getProgramQueuesByProgramId(programId);
    }

    public void saveProgramQueue(ProgramQueue programQueue) {
        dao.saveProgramQueue(programQueue);
    }

    public List<ProgramQueue> getActiveProgramQueuesByProgramId(Long programId) {
        List<ProgramQueue> queue = dao.getActiveProgramQueuesByProgramId(programId);
        List<ProgramQueue> q = new ArrayList<ProgramQueue>();
        //Get vacancy name and vacancy template name.
        for (ProgramQueue pq : queue) {
            if (pq.getReferralId() != null) {
                ClientReferral ref = referralDAO.getClientReferral(pq.getReferralId());
                if (ref.getSelectVacancy() != null) {
                    pq.setVacancyName(ref.getSelectVacancy() == null ? "" : ref.getSelectVacancy());
                    Vacancy vacancy = vacancyDao.getVacancyById(ref.getVacancyId() == null ? 0 : ref.getVacancyId());
                    if (vacancy != null) {
                        VacancyTemplate vt = vacancyTemplateDao.getVacancyTemplate(vacancy.getTemplateId() == null ? 0 : vacancy.getTemplateId());
                        pq.setVacancyTemplateName(vt != null ? vt.getName() : "");
                    }

                }
            }
            q.add(pq);
        }
        return q;
    }

    public ProgramQueue getActiveProgramQueue(String programId, String demographicNo) {
        return dao.getActiveProgramQueue(Long.valueOf(programId), Long.valueOf(demographicNo));
    }

    public void rejectQueue(String programId, String clientId, String notes, String rejectionReason) {
        ProgramQueue queue = getActiveProgramQueue(programId, clientId);
        if (queue == null) {
            return;
        }
        ClientReferral referral = this.referralDAO.getClientReferral(queue.getReferralId());
        if (referral != null) {
            referral.setStatus(ClientReferral.STATUS_REJECTED);
            referral.setCompletionDate(new Date());
            referral.setCompletionNotes(notes);
            referral.setRadioRejectionReason(rejectionReason);
            this.referralDAO.saveClientReferral(referral);
        }
        queue.setStatus(ProgramQueue.STATUS_REJECTED);

        this.saveProgramQueue(queue);
    }
}
