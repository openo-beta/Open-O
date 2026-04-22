//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.OscarCommLocations;

public interface OscarCommLocationsDao extends AbstractDao<OscarCommLocations> {

    public List<OscarCommLocations> findByCurrent1(int current1);

    public List<Object[]> findFormLocationByMesssageId(String messId);

    public List<Object[]> findAttachmentsByMessageId(String messageId);
}
