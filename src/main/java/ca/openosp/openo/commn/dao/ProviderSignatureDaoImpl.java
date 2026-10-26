package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProviderSignature;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA implementation of {@link ProviderSignatureDao}.
 *
 * @since 2026-10-26
 */
@Repository
public class ProviderSignatureDaoImpl extends AbstractDaoImpl<ProviderSignature> implements ProviderSignatureDao {

    public ProviderSignatureDaoImpl() {
        super(ProviderSignature.class);
    }

    @Override
    public List<ProviderSignature> findByProviderNo(String providerNo) {
        return entityManager.createQuery(
                        "SELECT s FROM ProviderSignature s WHERE s.providerNo = ?1 ORDER BY s.id", ProviderSignature.class)
                .setParameter(1, providerNo)
                .getResultList();
    }
}
