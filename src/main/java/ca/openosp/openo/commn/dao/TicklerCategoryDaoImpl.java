//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.TicklerCategory;
import org.springframework.stereotype.Repository;

@Repository
public class TicklerCategoryDaoImpl extends AbstractDaoImpl<TicklerCategory> implements TicklerCategoryDao {

    protected TicklerCategoryDaoImpl() {
        super(TicklerCategory.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TicklerCategory> getActiveCategories() {
        String sql = "SELECT x FROM TicklerCategory x WHERE x.active = true ORDER BY x.category";
        Query query = entityManager.createQuery(sql);
        List<TicklerCategory> results = query.getResultList();
        return results;
    }

}
