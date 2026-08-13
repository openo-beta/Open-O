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
     * <p>Written as two statements rather than a row at a time from a list read beforehand. An
     * update reads the rows as they are now, so two changes to one provider's default queue behind
     * each other and the last one wins; working from a list read earlier let both proceed from the
     * same starting point and leave two rows flagged default.
     *
     * <p>A value belonging to another provider, or one taken out of use, matches neither statement's
     * conditions, so it clears the provider's existing default without setting a new one. Callers
     * are expected to have resolved the value against this provider first.
     */
    @Override
    public void setAsDefault(UAO uao, String providerNo) {
        Query clearOthers = entityManager.createQuery(
                "update UAO u set u.defaultUAO = false"
                        + " where u.providerNo = :providerNo and u.active = true and u.id <> :id");
        clearOthers.setParameter("providerNo", providerNo);
        clearOthers.setParameter("id", uao.getId());
        clearOthers.executeUpdate();

        Query setChosen = entityManager.createQuery(
                "update UAO u set u.defaultUAO = true"
                        + " where u.id = :id and u.providerNo = :providerNo and u.active = true");
        setChosen.setParameter("id", uao.getId());
        setChosen.setParameter("providerNo", providerNo);
        setChosen.executeUpdate();
    }
}
