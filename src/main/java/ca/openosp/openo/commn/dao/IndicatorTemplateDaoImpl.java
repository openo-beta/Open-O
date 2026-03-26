//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.IndicatorTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorTemplateDaoImpl extends AbstractDaoImpl<IndicatorTemplate> implements IndicatorTemplateDao {

    public IndicatorTemplateDaoImpl() {
        super(IndicatorTemplate.class);
    }

    @Override
    public List<IndicatorTemplate> getActiveIndicatorTemplates() {
        return getIndicatorTemplatesByStatus(Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IndicatorTemplate> getIndicatorTemplatesByStatus(boolean status) {
        Query query = entityManager.createQuery("SELECT x FROM IndicatorTemplate x WHERE x.active = ?1");
        query.setParameter(1, status);
        List<IndicatorTemplate> result = query.getResultList();
        return result;
    }

    /**
     * This is a safe operation because the database is not expected to grow
     * large enough to cause performance issues.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<IndicatorTemplate> getIndicatorTemplates() {
        Query query = entityManager.createQuery("SELECT x FROM IndicatorTemplate x");
        List<IndicatorTemplate> result = query.getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IndicatorTemplate> getNotSharedIndicatorTemplates() {
        Query query = entityManager.createQuery("SELECT x FROM IndicatorTemplate x where x.shared = ?1");
        query.setParameter(1, false);
        List<IndicatorTemplate> result = query.getResultList();
        return result;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<IndicatorTemplate> getSharedIndicatorTemplates() {
        Query query = entityManager.createQuery("SELECT x FROM IndicatorTemplate x where x.shared = ?1");
        query.setParameter(1, true);
        List<IndicatorTemplate> result = query.getResultList();
        return result;
    }

    /**
     * Gets all ACTIVE Indicators by the specified Dashboard Id.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<IndicatorTemplate> getIndicatorTemplatesByDashboardId(int id) {
        Query query = entityManager
                .createQuery("SELECT x FROM IndicatorTemplate x WHERE x.dashboardId = ?1 AND x.active = ?2");
        query.setParameter(1, id);
        query.setParameter(2, Boolean.TRUE);
        List<IndicatorTemplate> result = query.getResultList();
        return result;
    }

    /**
     * Get a list of all the Indicator categories
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getIndicatorCategories() {
        Query query = entityManager.createQuery("SELECT x.category FROM IndicatorTemplate x "
                + "WHERE x.active IS TRUE "
                + "GROUP BY x.category HAVING COUNT(x.category) > -1");
        List<String> result = query.getResultList();
        return result;
    }

    /**
     * Get a list of all the Indicator sub-categories
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getIndicatorSubCategories() {
        Query query = entityManager.createQuery("SELECT x.subCategory FROM IndicatorTemplate x "
                + "WHERE x.active IS TRUE "
                + "GROUP BY x.subCategory HAVING COUNT(x.subCategory) > -1");
        List<String> result = query.getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IndicatorTemplate> getIndicatorTemplatesByCategory(String category) {
        Query query = entityManager
                .createQuery("SELECT x FROM IndicatorTemplate x WHERE x.category = ?1 AND x.active = ?2");
        query.setParameter(1, category);
        query.setParameter(2, Boolean.TRUE);
        List<IndicatorTemplate> result = query.getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IndicatorTemplate> getIndicatorTemplatesBySubcategory(String subCategory) {
        Query query = entityManager.createQuery(
                "SELECT x FROM IndicatorTemplate x WHERE x.subCategory = ?1 AND x.active = ?2");
        query.setParameter(1, subCategory);
        query.setParameter(2, Boolean.TRUE);
        List<IndicatorTemplate> result = query.getResultList();
        return result;
    }

}
