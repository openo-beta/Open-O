package ca.openosp.openo.documentManager.dao;

import ca.openosp.openo.commn.dao.CtlDocumentDao;
import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CtlDocumentDao#findByDocumentNos(List)} — the batch
 * lookup used by {@code EDocUtil.enrichAttachedWithCtl} to classify many attached
 * docs in a single query.
 */
@DisplayName("CtlDocumentDao.findByDocumentNos")
@Tag("integration")
@Tag("dao")
@Tag("document")
@Tag("read")
public class CtlDocumentDaoFindByDocumentNosIntegrationTest extends DocumentDaoBaseIntegrationTest {

    @Autowired
    private CtlDocumentDao ctlDocumentDao;

    @Test
    @DisplayName("returns an empty list when the input is null")
    void shouldReturnEmpty_whenInputIsNull() {
        assertThat(ctlDocumentDao.findByDocumentNos(null)).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list when the input is empty")
    void shouldReturnEmpty_whenInputIsEmpty() {
        assertThat(ctlDocumentDao.findByDocumentNos(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("returns the single ctl row for a single doc")
    void shouldReturnOneCtl_whenOneDocHasOneCtl() {
        Integer docNo = persistDocument("Single doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(docNo, "provider", 42);

        List<CtlDocument> result = ctlDocumentDao.findByDocumentNos(Collections.singletonList(docNo));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId().getModule()).isEqualTo("provider");
        assertThat(result.get(0).getId().getModuleId()).isEqualTo(42);
    }

    @Test
    @DisplayName("returns one ctl row per doc when multiple docs are requested")
    void shouldReturnOnePerDoc_whenMultipleDocs() {
        Integer providerDocNo = persistDocument("Provider-owned doc", 0, Document.STATUS_ACTIVE);
        Integer patientDocNo = persistDocument("Patient-owned doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(providerDocNo, "providers", 42);
        persistCtlDocument(patientDocNo, "demographic", 2001);

        List<CtlDocument> result = ctlDocumentDao.findByDocumentNos(Arrays.asList(providerDocNo, patientDocNo));

        assertThat(result).hasSize(2);
        Set<String> modules = result.stream()
                .map(c -> c.getId().getModule())
                .collect(Collectors.toSet());
        assertThat(modules).containsExactlyInAnyOrder("providers", "demographic");
    }

    @Test
    @DisplayName("returns every ctl row when one doc has multiple bindings")
    void shouldReturnAllCtls_whenDocHasMultipleBindings() {
        Integer docNo = persistDocument("Multi-ctl doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(docNo, "providers", 42);
        persistCtlDocument(docNo, "demographic", 2001);

        List<CtlDocument> result = ctlDocumentDao.findByDocumentNos(Collections.singletonList(docNo));

        assertThat(result)
                .as("All bindings for the doc should surface so the caller can apply provider-wins")
                .hasSize(2);
        Set<String> modules = result.stream()
                .map(c -> c.getId().getModule())
                .collect(Collectors.toSet());
        assertThat(modules).containsExactlyInAnyOrder("providers", "demographic");
    }

    @Test
    @DisplayName("does not return ctl rows for doc ids that are not in the input list")
    void shouldScopeByDocumentNos() {
        Integer includedDocNo = persistDocument("Included doc", 0, Document.STATUS_ACTIVE);
        Integer excludedDocNo = persistDocument("Excluded doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(includedDocNo, "providers", 42);
        persistCtlDocument(excludedDocNo, "providers", 42);

        List<CtlDocument> result = ctlDocumentDao.findByDocumentNos(Collections.singletonList(includedDocNo));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId().getDocumentNo()).isEqualTo(includedDocNo);
    }
}
