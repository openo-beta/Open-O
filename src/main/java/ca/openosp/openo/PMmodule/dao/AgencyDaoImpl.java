//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.model.Agency;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class AgencyDaoImpl extends HibernateDaoSupport implements AgencyDao {

    private Logger log = MiscUtils.getLogger();

    public Agency getLocalAgency() {
        Agency agency = null;

        List results = getHibernateTemplate().find("from Agency a");

        if (!results.isEmpty()) {
            agency = (Agency) results.get(0);
        }

        return agency;
    }

    public void saveAgency(Agency agency) {
        if (agency == null) {
            throw new IllegalArgumentException();
        }

        getHibernateTemplate().saveOrUpdate(agency);

        if (log.isDebugEnabled()) {
            log.debug("saveAgency : id = " + agency.getId());
        }

    }

}
