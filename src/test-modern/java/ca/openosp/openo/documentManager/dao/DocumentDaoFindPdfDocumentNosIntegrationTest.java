package ca.openosp.openo.documentManager.dao;

import ca.openosp.openo.commn.model.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@code DocumentDao.findPdfDocumentNos}, the batched lookup the tickler
 * lists use to open attached PDFs in the EMR document viewer instead of as the raw file.
 *
 * @since 2026-11-13
 */
@DisplayName("DocumentDao.findPdfDocumentNos")
@Tag("integration")
@Tag("dao")
@Tag("document")
@Tag("read")
public class DocumentDaoFindPdfDocumentNosIntegrationTest extends DocumentDaoBaseIntegrationTest {

    @Test
    @DisplayName("returns an empty list when the input is null")
    void shouldReturnEmpty_whenInputIsNull() {
        assertThat(documentDao.findPdfDocumentNos(null)).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list when the input is empty")
    void shouldReturnEmpty_whenInputIsEmpty() {
        assertThat(documentDao.findPdfDocumentNos(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("returns only the requested documents whose content type is a PDF")
    void shouldReturnOnlyPdfs_whenMixedContentTypesRequested() {
        Integer pdf = persistDocument("Discharge summary", 0, Document.STATUS_ACTIVE);
        Integer image = persistDocumentWithContentType("Wound photo", "image/jpeg");
        Integer unrequestedPdf = persistDocument("Unrelated PDF", 0, Document.STATUS_ACTIVE);

        List<Integer> result = documentDao.findPdfDocumentNos(Arrays.asList(pdf, image));

        assertThat(result).containsExactly(pdf).doesNotContain(unrequestedPdf);
    }

    @Test
    @DisplayName("uses the eChart's PDF test: a content type containing /pdf, so application/x-pdf is not a PDF")
    void shouldNotReturnDoc_whenContentTypeLacksSlashPdf() {
        Integer xPdf = persistDocumentWithContentType("Legacy upload", "application/x-pdf");
        Integer text = persistDocumentWithContentType("Typed note", "text/plain");

        assertThat(documentDao.findPdfDocumentNos(Arrays.asList(xPdf, text))).isEmpty();
    }

    private Integer persistDocumentWithContentType(String description, String contentType) {
        Integer docNo = persistDocument(description, 0, Document.STATUS_ACTIVE);
        Document document = entityManager.find(Document.class, docNo);
        document.setContenttype(contentType);
        entityManager.flush();
        return docNo;
    }
}
