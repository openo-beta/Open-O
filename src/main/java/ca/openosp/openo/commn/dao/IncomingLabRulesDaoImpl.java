//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.IncomingLabRules;
import ca.openosp.openo.commn.model.Provider;
import org.springframework.stereotype.Repository;

@Repository
public class IncomingLabRulesDaoImpl extends AbstractDaoImpl<IncomingLabRules> implements IncomingLabRulesDao {

    public IncomingLabRulesDaoImpl() {
        super(IncomingLabRules.class);
    }

    public List<IncomingLabRules> findCurrentByProviderNoAndFrwdProvider(String providerNo, String frwdProvider) {
        Query q = entityManager.createQuery("select i from IncomingLabRules i where i.providerNo=?1 and i.frwdProviderNo=?2 and i.archive=?3");
        q.setParameter(1, providerNo);
        q.setParameter(2, frwdProvider);
        q.setParameter(3, "0");

        @SuppressWarnings("unchecked")
        List<IncomingLabRules> results = q.getResultList();

        return results;
    }

    public List<IncomingLabRules> findByProviderNoAndFrwdProvider(String providerNo, String frwdProvider) {
        Query q = entityManager.createQuery("select i from IncomingLabRules i where i.providerNo=?1 and i.frwdProviderNo=?2");
        q.setParameter(1, providerNo);
        q.setParameter(2, frwdProvider);


        @SuppressWarnings("unchecked")
        List<IncomingLabRules> results = q.getResultList();

        return results;
    }

    public List<IncomingLabRules> findCurrentByProviderNo(String providerNo) {
        Query q = entityManager.createQuery("select i from IncomingLabRules i where i.providerNo=?1 and i.archive=?2");
        q.setParameter(1, providerNo);
        q.setParameter(2, "0");

        @SuppressWarnings("unchecked")
        List<IncomingLabRules> results = q.getResultList();

        return results;
    }

    public List<IncomingLabRules> findByProviderNo(String providerNo) {
        Query q = entityManager.createQuery("select i from IncomingLabRules i where i.providerNo=?1");
        q.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<IncomingLabRules> results = q.getResultList();

        return results;
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findRules(String providerNo) {
        // assume archive represents boolean with 0 == false and 1 == true
        Query q = entityManager.createQuery("FROM IncomingLabRules i, " + Provider.class.getSimpleName() + " p " +
                "WHERE i.archive <> '1' " + // non-archived rules
                "AND i.providerNo = :providerNo " +
                "AND p.id = i.frwdProviderNo");
        q.setParameter("providerNo", providerNo);
        return q.getResultList();
    }
}
