//CHECKSTYLE:OFF



package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.billing.CA.model.GstControl;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

/**
 * @author rjonasz
 */
@Repository
public class GstControlDaoImpl extends AbstractDaoImpl<GstControl> implements GstControlDao {

    public GstControlDaoImpl() {
        super(GstControl.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<GstControl> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }
}
 