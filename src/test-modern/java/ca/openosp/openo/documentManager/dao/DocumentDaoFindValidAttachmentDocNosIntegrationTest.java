package ca.openosp.openo.documentManager.dao;

import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.CtlDocumentPK;
import ca.openosp.openo.commn.model.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@code DocumentDao.findValidAttachmentDocNos} — the
 * lookup the Ocean eReferral validator uses to confirm that each attached doc
 * either belongs to the patient's chart or lives in some provider's library
 * (public or private eDoc). Pins the DB-level contract for both branches of
 * the query plus the shared {@code c.status != 'D'} filter.
 */
@DisplayName("DocumentDao.findValidAttachmentDocNos")
@Tag("integration")
@Tag("dao")
@Tag("document")
@Tag("read")
public class DocumentDaoFindValidAttachmentDocNosIntegrationTest extends DocumentDaoBaseIntegrationTest {

    private static final Integer PROVIDER_A = 42;
    private static final Integer PROVIDER_B = 99;
    private static final Integer PATIENT = 2001;
    private static final Integer OTHER_PATIENT = 2002;

    @Test
    @DisplayName("returns an empty list when the input is null")
    void shouldReturnEmpty_whenInputIsNull() {
        assertThat(documentDao.findValidAttachmentDocNos(PATIENT, null)).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list when the input is empty")
    void shouldReturnEmpty_whenInputIsEmpty() {
        assertThat(documentDao.findValidAttachmentDocNos(PATIENT, Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("returns docs demographic-scoped to this patient")
    void shouldReturnDoc_whenCtlModuleIsDemographicForThisPatient() {
        Integer docNo = persistDocument("Patient doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(docNo, "demographic", PATIENT);

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(docNo));

        assertThat(result).containsExactly(docNo);
    }

    @Test
    @DisplayName("does not return docs demographic-scoped to a different patient")
    void shouldNotReturnDoc_whenCtlModuleIsDemographicForAnotherPatient() {
        Integer docNo = persistDocument("Other patient's doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(docNo, "demographic", OTHER_PATIENT);

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(docNo));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns docs with a 'providers' module ctl row regardless of owning provider")
    void shouldReturnDoc_whenCtlModuleIsProviders() {
        Integer docNo = persistDocument("Public eDoc", 1, Document.STATUS_ACTIVE);
        persistCtlDocument(docNo, "providers", PROVIDER_A);

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(docNo));

        assertThat(result).containsExactly(docNo);
    }

    @Test
    @DisplayName("returns docs with a legacy 'provider' module ctl row")
    void shouldReturnDoc_whenCtlModuleIsProviderLegacy() {
        Integer docNo = persistDocument("Legacy provider doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(docNo, "provider", PROVIDER_B);

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(docNo));

        assertThat(result).containsExactly(docNo);
    }

    @Test
    @DisplayName("does not return a provider-library doc whose ctl row is soft-deleted (status='D')")
    void shouldNotReturnDoc_whenProviderCtlRowIsSoftDeleted() {
        // Matches the demographic branch's behaviour: a cleaned-up binding
        // (ctl row marked 'D') no longer counts as attachable membership,
        // even for provider-library docs. Keeps the filter symmetric.
        Integer docNo = persistDocument("Library-deleted provider doc", 0, Document.STATUS_ACTIVE);
        CtlDocument ctl = new CtlDocument();
        ctl.setId(new CtlDocumentPK("provider", PROVIDER_A, docNo));
        ctl.setStatus("D");
        entityManager.persist(ctl);
        entityManager.flush();

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(docNo));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("does not return a patient doc whose demographic ctl row is soft-deleted")
    void shouldNotReturnDoc_whenDemographicCtlRowIsSoftDeleted() {
        Integer docNo = persistDocument("Detached patient doc", 0, Document.STATUS_ACTIVE);
        CtlDocument ctl = new CtlDocument();
        ctl.setId(new CtlDocumentPK("demographic", PATIENT, docNo));
        ctl.setStatus("D");
        entityManager.persist(ctl);
        entityManager.flush();

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(docNo));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("returns a mix of demographic-scoped and provider-library docs in one call")
    void shouldReturnBoth_whenMixedDemographicAndProviderDocs() {
        Integer patientDoc = persistDocument("Patient doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(patientDoc, "demographic", PATIENT);
        Integer providerDoc = persistDocument("Shared provider doc", 1, Document.STATUS_ACTIVE);
        persistCtlDocument(providerDoc, "providers", PROVIDER_A);

        List<Integer> result = documentDao.findValidAttachmentDocNos(
                PATIENT, Arrays.asList(patientDoc, providerDoc));

        assertThat(result).containsExactlyInAnyOrder(patientDoc, providerDoc);
    }

    @Test
    @DisplayName("does not return docs outside the requested id list")
    void shouldScopeByDocIds() {
        Integer requestedDoc = persistDocument("Requested", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(requestedDoc, "providers", PROVIDER_A);
        Integer otherDoc = persistDocument("Other provider doc", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(otherDoc, "providers", PROVIDER_B);

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(requestedDoc));

        assertThat(result).containsExactly(requestedDoc);
    }

    @Test
    @DisplayName("returns a doc that has multiple qualifying ctl rows")
    void shouldReturnDoc_whenDocHasMultipleQualifyingCtlRows() {
        // A doc can legitimately have rows under both 'provider' and 'providers',
        // or under multiple providers, or under demographic + provider. The DAO
        // may return the id more than once; the caller wraps the result in a
        // HashSet before its containsAll check, so cardinality is its problem.
        // What matters here is that the doc is present in the result.
        Integer docNo = persistDocument("Doc with two qualifying ctl rows", 0, Document.STATUS_ACTIVE);
        persistCtlDocument(docNo, "provider", PROVIDER_A);
        persistCtlDocument(docNo, "providers", PROVIDER_B);

        List<Integer> result = documentDao.findValidAttachmentDocNos(PATIENT, Arrays.asList(docNo));

        assertThat(result).contains(docNo);
    }
}
