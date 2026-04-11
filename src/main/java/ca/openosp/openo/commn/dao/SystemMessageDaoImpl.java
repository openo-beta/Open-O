//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.SystemMessage;
import org.springframework.stereotype.Repository;

@Repository
public class SystemMessageDaoImpl extends AbstractDaoImpl<SystemMessage> implements SystemMessageDao {

    public SystemMessageDaoImpl() {
        super(SystemMessage.class);
    }

    @Override
    public List<SystemMessage> findAll() {
        Query query = entityManager.createQuery("select x from SystemMessage x order by x.expiryDate desc");

        @SuppressWarnings("unchecked")
        List<SystemMessage> results = query.getResultList();

        return (results);
    }
}
