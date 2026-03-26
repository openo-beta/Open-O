//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ClientLink;

public interface ClientLinkDao extends AbstractDao<ClientLink> {
    List<ClientLink> findByFacilityIdClientIdType(Integer facilityId, Integer clientId, Boolean currentlyLinked, ClientLink.Type type);
}
