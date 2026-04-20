//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.dao.ProgramClientRestrictionDAO;
import ca.openosp.openo.PMmodule.exception.ClientAlreadyRestrictedException;
import ca.openosp.openo.PMmodule.model.ProgramClientRestriction;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ClientRestrictionManagerImpl implements ClientRestrictionManager {

    private ProgramClientRestrictionDAO programClientRestrictionDAO;

    @Override
    public List<ProgramClientRestriction> getActiveRestrictionsForProgram(int programId, Date asOfDate) {
        // check daos for restriction
        Collection<ProgramClientRestriction> pcrs = programClientRestrictionDAO.findForProgram(programId);
        List<ProgramClientRestriction> returnPcrs = new ArrayList<ProgramClientRestriction>();
        if (pcrs != null && !pcrs.isEmpty()) {
            for (ProgramClientRestriction pcr : pcrs) {
                if (pcr.getStartDate().getTime() <= asOfDate.getTime() && pcr.getEndDate().getTime() <= pcr.getEndDate().getTime())
                    returnPcrs.add(pcr);
            }
        }
        return returnPcrs;
    }

    public List<ProgramClientRestriction> getDisabledRestrictionsForProgram(Integer programId, Date asOfDate) {
        // check daos for restriction
        Collection<ProgramClientRestriction> pcrs = programClientRestrictionDAO.findDisabledForProgram(programId);
        List<ProgramClientRestriction> returnPcrs = new ArrayList<ProgramClientRestriction>();
        if (pcrs != null && !pcrs.isEmpty()) {
            for (ProgramClientRestriction pcr : pcrs) {
                if (pcr.getStartDate().getTime() <= asOfDate.getTime() && pcr.getEndDate().getTime() <= pcr.getEndDate().getTime())
                    returnPcrs.add(pcr);
            }
        }
        return returnPcrs;
    }

    public List<ProgramClientRestriction> getActiveRestrictionsForClient(int demographicNo, Date asOfDate) {
        // check daos for restriction
        Collection<ProgramClientRestriction> pcrs = programClientRestrictionDAO.findForClient(demographicNo);
        List<ProgramClientRestriction> returnPcrs = new ArrayList<ProgramClientRestriction>();
        if (pcrs != null && !pcrs.isEmpty()) {
            for (ProgramClientRestriction pcr : pcrs) {
                if (pcr.getStartDate().getTime() <= asOfDate.getTime() && pcr.getEndDate().getTime() <= pcr.getEndDate().getTime())
                    returnPcrs.add(pcr);
            }
        }
        return returnPcrs;
    }

    public List<ProgramClientRestriction> getActiveRestrictionsForClient(int demographicNo, int facilityId, Date asOfDate) {
        // check daos for restriction
        Collection<ProgramClientRestriction> pcrs = programClientRestrictionDAO.findForClient(demographicNo, facilityId);
        List<ProgramClientRestriction> returnPcrs = new ArrayList<ProgramClientRestriction>();
        if (pcrs != null && !pcrs.isEmpty()) {
            for (ProgramClientRestriction pcr : pcrs) {
                if (pcr.getStartDate().getTime() <= asOfDate.getTime() && pcr.getEndDate().getTime() <= pcr.getEndDate().getTime())
                    returnPcrs.add(pcr);
            }
        }
        return returnPcrs;
    }

    public List<ProgramClientRestriction> getDisabledRestrictionsForClient(int demographicNo, Date asOfDate) {
        // check daos for restriction
        Collection<ProgramClientRestriction> pcrs = programClientRestrictionDAO.findDisabledForClient(demographicNo);
        List<ProgramClientRestriction> returnPcrs = new ArrayList<ProgramClientRestriction>();
        if (pcrs != null && !pcrs.isEmpty()) {
            for (ProgramClientRestriction pcr : pcrs) {
                if (pcr.getStartDate().getTime() <= asOfDate.getTime() && pcr.getEndDate().getTime() <= pcr.getEndDate().getTime())
                    returnPcrs.add(pcr);
            }
        }
        return returnPcrs;
    }

    public ProgramClientRestriction checkClientRestriction(int programId, int demographicNo, Date asOfDate) {
        // check daos for restriction
        Collection<ProgramClientRestriction> pcrs = programClientRestrictionDAO.find(programId, demographicNo);
        if (pcrs != null && !pcrs.isEmpty()) {
            for (ProgramClientRestriction pcr : pcrs) {
                if (pcr.getStartDate().getTime() <= asOfDate.getTime() && asOfDate.getTime() <= pcr.getEndDate().getTime())
                    return pcr;
            }
        }
        return null;
    }

    public void saveClientRestriction(ProgramClientRestriction restriction) throws ClientAlreadyRestrictedException {
        if (restriction.getId() == null) {
            ProgramClientRestriction result = checkClientRestriction(restriction.getProgramId(), restriction.getDemographicNo(), new Date());
            if (result != null)
                throw new ClientAlreadyRestrictedException("the client has already been service restricted in this program");
        }

        programClientRestrictionDAO.save(restriction);
    }

    public void terminateEarly(int programClientRestrictionId, String providerNo) {
        ProgramClientRestriction x = programClientRestrictionDAO.find(programClientRestrictionId);

        if (x != null) {
            x.setEarlyTerminationProvider(providerNo);
            x.setEndDate(new Date());
            programClientRestrictionDAO.save(x);
        }
    }

    public void disableClientRestriction(int restrictionId) {
        ProgramClientRestriction pcr = programClientRestrictionDAO.find(restrictionId);
        pcr.setEnabled(false);
        try {
            saveClientRestriction(pcr);
        } catch (ClientAlreadyRestrictedException e) {
            // this exception should not happen here, so toss it up as a runtime exception to be caught higher up
            throw new RuntimeException(e);
        }
    }

    public void enableClientRestriction(Integer restrictionId) {
        ProgramClientRestriction pcr = programClientRestrictionDAO.find(restrictionId);
        pcr.setEnabled(true);
        try {
            saveClientRestriction(pcr);
        } catch (ClientAlreadyRestrictedException e) {
            // this exception should not happen here, so toss it up as a runtime exception to be caught higher up
            throw new RuntimeException(e);
        }
    }

    public ProgramClientRestrictionDAO getProgramClientRestrictionDAO() {
        return programClientRestrictionDAO;
    }

    @Required
    public void setProgramClientRestrictionDAO(ProgramClientRestrictionDAO programClientRestrictionDAO) {
        this.programClientRestrictionDAO = programClientRestrictionDAO;
    }
}
