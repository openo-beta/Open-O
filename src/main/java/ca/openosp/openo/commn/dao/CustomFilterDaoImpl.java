//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CustomFilter;
import org.springframework.stereotype.Repository;

@Repository
public class CustomFilterDaoImpl extends AbstractDaoImpl<CustomFilter> implements CustomFilterDao {

    public CustomFilterDaoImpl() {
        super(CustomFilter.class);
    }

    @Override
    public CustomFilter findByName(String name) {
        Query query = entityManager.createQuery("select c FROM CustomFilter c where c.name = ?1");
        query.setParameter(1, name);

        CustomFilter result = this.getSingleResultOrNull(query);

        return result;
    }

    @Override
    public CustomFilter findByNameAndProviderNo(String name, String providerNo) {
        Query query = entityManager.createQuery("select c FROM CustomFilter c where c.name = ?1 and c.providerNo = ?2");
        query.setParameter(1, name);
        query.setParameter(2, providerNo);

        CustomFilter result = this.getSingleResultOrNull(query);

        return result;
    }

    @Override
    public List<CustomFilter> getCustomFilters() {
        Query query = entityManager.createQuery("select c FROM CustomFilter c");

        @SuppressWarnings("unchecked")
        List<CustomFilter> result = query.getResultList();

        return result;
    }

    @Override
    public List<CustomFilter> findByProviderNo(String providerNo) {
        Query query = entityManager.createQuery("select c FROM CustomFilter c where c.providerNo = ?1");
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<CustomFilter> result = query.getResultList();

        return result;
    }

    @Override
    public List<CustomFilter> getCustomFilterWithShortCut(String providerNo) {
        Query query = entityManager
                .createQuery("select c FROM CustomFilter c where c.providerNo = ?1 and c.shortcut = true");
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<CustomFilter> result = query.getResultList();

        return result;
    }

    @Override
    public void deleteCustomFilter(String name) {
        CustomFilter filter = findByName(name);
        if (filter != null) {
            remove(filter);
        }
    }

}
