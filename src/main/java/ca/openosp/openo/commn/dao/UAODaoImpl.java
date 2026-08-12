//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.UAO;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

/**
 * JPA implementation of {@link UAODao}.
 *
 * <p>Method contracts are on the interface. Both queries are scoped to one provider, and only
 * active values are returned, so a value taken out of use cannot be listed or made default.
 *
 * @since 2026-07-02
 */
@Repository
public class UAODaoImpl extends AbstractDaoImpl<UAO> implements UAODao {

    public UAODaoImpl() {
        super(UAO.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UAO> findByProvider(String providerNo) {
        Query query = entityManager.createQuery(
                "select u from UAO u where u.providerNo = :providerNo and u.active = true order by u.defaultUAO desc");
        query.setParameter("providerNo", providerNo);
        return (List<UAO>) query.getResultList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Only the rows whose flag actually changes are written, so making an already-default value
     * default again writes nothing. A value belonging to another provider matches no row in the
     * list, which clears the provider's existing default without setting a new one, so callers are
     * expected to have resolved the value against this provider first.
     */
    @Override
    public void setAsDefault(UAO uao, String providerNo) {
        for (UAO current : findByProvider(providerNo)) {
            boolean isDefault = Boolean.TRUE.equals(current.getDefaultUAO());
            if (current.getId().equals(uao.getId())) {
                if (!isDefault) {
                    current.setDefaultUAO(true);
                    merge(current);
                }
            } else if (isDefault) {
                current.setDefaultUAO(false);
                merge(current);
            }
        }
    }
}
