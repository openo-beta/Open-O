//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ClientLink;
import org.springframework.stereotype.Repository;

@Repository
public class ClientLinkDaoImpl extends AbstractDaoImpl<ClientLink> implements ClientLinkDao {

    public ClientLinkDaoImpl() {
        super(ClientLink.class);
    }

    public List<ClientLink> findByFacilityIdClientIdType(Integer facilityId, Integer clientId, Boolean currentlyLinked, ClientLink.Type type) {
        // build sql string
        StringBuilder sqlCommand = new StringBuilder();
        sqlCommand.append("select x from ClientLink x where x.facilityId=?1 and x.clientId=?2");
        if (type != null) sqlCommand.append(" and x.linkType=?3");
        if (currentlyLinked != null) {
            if (currentlyLinked) sqlCommand.append(" and x.unlinkProviderNo is null");
            else sqlCommand.append(" and x.unlinkProviderNo is not null");
        }

        // set parameters
        Query query = entityManager.createQuery(sqlCommand.toString());
        query.setParameter(1, facilityId);
        query.setParameter(2, clientId);
        if (type != null) query.setParameter(3, type);

        // run query
        @SuppressWarnings("unchecked")
        List<ClientLink> results = query.getResultList();

        return (results);
    }
}
