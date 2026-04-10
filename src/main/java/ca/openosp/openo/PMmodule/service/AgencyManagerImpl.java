//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import ca.openosp.openo.PMmodule.dao.AgencyDao;
import ca.openosp.openo.PMmodule.model.Agency;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AgencyManagerImpl implements AgencyManager {

    private AgencyDao dao;

    public void setAgencyDao(AgencyDao dao) {
        this.dao = dao;
    }

    @Override
    public Agency getLocalAgency() {
        Agency agency = dao.getLocalAgency();
        return agency;
    }

    @Override
    public void saveAgency(Agency agency) {
        dao.saveAgency(agency);
    }
}
