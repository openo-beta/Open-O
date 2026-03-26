//CHECKSTYLE:OFF



package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import ca.openosp.openo.billing.CA.model.GstControl;
import ca.openosp.openo.commn.dao.AbstractDao;

/**
 * @author rjonasz
 */

public interface GstControlDao extends AbstractDao<GstControl> {

    public List<GstControl> findAll();
}
