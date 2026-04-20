//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.PublicKey;
import org.springframework.stereotype.Repository;

@Repository
public class PublicKeyDaoImpl extends AbstractDaoImpl<PublicKey> implements PublicKeyDao {

    public PublicKeyDaoImpl() {
        super(PublicKey.class);
    }

    @Override
    public List<PublicKey> findAll() {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x");
        @SuppressWarnings("unchecked")
        List<PublicKey> results = query.getResultList();

        return (results);
    }

}
