//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MdsZMC;

public interface MdsZMCDao extends AbstractDao<MdsZMC> {
    MdsZMC findByIdAndSetId(Integer id, String setId);
}
