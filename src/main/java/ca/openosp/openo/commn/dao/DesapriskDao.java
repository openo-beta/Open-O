//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.Desaprisk;

public interface DesapriskDao extends AbstractDao<Desaprisk> {
    Desaprisk search(Integer formNo, Integer demographicNo);
}
