package ca.openosp.openo.commn.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.openosp.openo.commn.model.ProviderSignature;
import ca.openosp.openo.test.base.OpenOTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProviderSignatureDao#findByProviderNo}: one provider's signatures,
 * in the order they were added, and never another provider's.
 *
 * @since 2026-10-26
 */
@DisplayName("ProviderSignatureDao integration tests")
@Tag("integration")
@Tag("database")
@Tag("dao")
@Tag("read")
public class ProviderSignatureDaoIntegrationTest extends OpenOTestBase {

    @Autowired
    private ProviderSignatureDao providerSignatureDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    private ProviderSignature signature(String providerNo, String text) {
        ProviderSignature signature = new ProviderSignature();
        signature.setProviderNo(providerNo);
        signature.setSignature(text);
        signature.setLastUpdateUser(providerNo);
        providerSignatureDao.persist(signature);
        return signature;
    }

    @Test
    @DisplayName("should return the provider's signatures in the order they were added")
    void shouldReturnSignaturesInOrderAdded_whenProviderHasSeveral() {
        signature("999998", "Alex Openodoc, PA. Prescribed under the authority of Dr. Ty Armstrong");
        signature("999997", "Someone else's signature");
        signature("999998", "Alex Openodoc, PA. Prescribed under the authority of Dr. Kirsten Welch");
        entityManager.flush();
        entityManager.clear();

        assertThat(providerSignatureDao.findByProviderNo("999998"))
                .extracting(ProviderSignature::getSignature)
                .containsExactly(
                        "Alex Openodoc, PA. Prescribed under the authority of Dr. Ty Armstrong",
                        "Alex Openodoc, PA. Prescribed under the authority of Dr. Kirsten Welch");
    }

    @Test
    @DisplayName("should return no signatures when the provider has none")
    void shouldReturnEmptyList_whenProviderHasNone() {
        signature("999997", "Someone else's signature");

        assertThat(providerSignatureDao.findByProviderNo("999998")).isEmpty();
    }

    @Test
    @DisplayName("should stamp the update date when a signature is saved")
    void shouldStampUpdateDate_whenSignatureIsSaved() {
        ProviderSignature saved = signature("999998", "Alex Openodoc, PA");
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLastUpdateDate()).isNotNull();
    }
}
