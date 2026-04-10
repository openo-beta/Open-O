//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.SystemMessage;

public interface SystemMessageDao extends AbstractDao<SystemMessage> {
    List<SystemMessage> findAll();
}
