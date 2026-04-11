//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.casemgmt.model.CaseManagementCPP;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/*
 * Updated by Eugene Petruhin on 09 jan 2009 while fixing #2482832 & #2494061
 */
public class CaseManagementCPPDAOImpl extends HibernateDaoSupport implements CaseManagementCPPDAO {

    private Logger log = MiscUtils.getLogger();

    @Override
    public CaseManagementCPP getCPP(String demographic_no) {
        List results = this.getHibernateTemplate().find(
                "from CaseManagementCPP cpp where cpp.demographic_no = ?0 order by update_date desc",
                new Object[]{demographic_no});
        return (results.size() != 0) ? (CaseManagementCPP) results.get(0) : null;
    }

    @Override
    public void saveCPP(CaseManagementCPP cpp) {

        String fhist = cpp.getFamilyHistory() == null ? "" : cpp.getFamilyHistory();
        String mhist = cpp.getMedicalHistory() == null ? "" : cpp.getMedicalHistory();
        String ongoing = cpp.getOngoingConcerns() == null ? "" : cpp.getOngoingConcerns();
        String rem = cpp.getReminders() == null ? "" : cpp.getReminders();
        String shist = cpp.getSocialHistory() == null ? "" : cpp.getSocialHistory();
        String ofnum = cpp.getOtherFileNumber() == null ? "" : cpp.getOtherFileNumber();
        String ossystem = cpp.getOtherSupportSystems() == null ? "" : cpp.getOtherSupportSystems();
        String pm = cpp.getPastMedications() == null ? "" : cpp.getPastMedications();

        cpp.setFamilyHistory(fhist);
        cpp.setMedicalHistory(mhist);
        cpp.setOngoingConcerns(ongoing);
        cpp.setReminders(rem);
        cpp.setSocialHistory(shist);
        cpp.setUpdate_date(new Date());
        cpp.setOtherFileNumber(ofnum);
        cpp.setOtherSupportSystems(ossystem);
        cpp.setPastMedications(pm);

        if (log.isDebugEnabled()) {
            log.debug("Saving or updating a CPP: " + cpp);
        }

        this.getHibernateTemplate().saveOrUpdate(cpp);

    }
}
