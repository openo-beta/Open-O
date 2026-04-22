//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MdsZMC;
import org.springframework.stereotype.Repository;

@Repository
public class MdsZMCDaoImpl extends AbstractDaoImpl<MdsZMC> implements MdsZMCDao {

    public MdsZMCDaoImpl() {
        super(MdsZMC.class);
    }

    @Override
    public MdsZMC findByIdAndSetId(Integer id, String setId) {
        String sql = "FROM MdsZMC zmc WHERE zmc.id = ?1 " +
                "AND zmc.setId like ?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, id);
        query.setParameter(2, setId);
        return getSingleResultOrNull(query);
    }
}
