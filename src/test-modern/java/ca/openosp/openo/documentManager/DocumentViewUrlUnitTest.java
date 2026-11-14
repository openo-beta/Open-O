package ca.openosp.openo.documentManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DocumentViewUrl}, which picks the EMR document viewer for PDFs and the raw
 * file for anything else, matching the eChart's document links.
 *
 * @since 2026-11-13
 */
@DisplayName("DocumentViewUrl")
@Tag("unit")
@Tag("fast")
@Tag("document")
class DocumentViewUrlUnitTest {

    @Test
    @DisplayName("should open a PDF in the EMR document viewer")
    void shouldBuildViewerUrl_whenDocumentIsPdf() {
        assertThat(DocumentViewUrl.forDocument("12", true, "999998"))
                .isEqualTo("/documentManager/showDocument.jsp?inWindow=true&segmentID=12");
    }

    @Test
    @DisplayName("should open a non-PDF as the raw file")
    void shouldBuildRawFileUrl_whenDocumentIsNotPdf() {
        assertThat(DocumentViewUrl.forDocument("12", false, "999998")).isEqualTo(
                "/documentManager/ManageDocument.do?method=display&doc_no=12&providerNo=999998&searchProviderNo=999998&status=");
    }

    @Test
    @DisplayName("should encode the document number so it cannot add URL parameters")
    void shouldEncodeDocumentNo_whenItContainsUrlCharacters() {
        assertThat(DocumentViewUrl.forDocument("12&inWindow=false", true, "999998"))
                .isEqualTo("/documentManager/showDocument.jsp?inWindow=true&segmentID=12%26inWindow%3Dfalse");
    }

    @ParameterizedTest(name = "\"{0}\" is a PDF: {1}")
    @CsvSource(value = {"application/pdf,true", "application/x-pdf,false", "image/jpeg,false",
            "text/plain,false", "NULL,false"}, nullValues = "NULL")
    @DisplayName("should treat a content type as a PDF when it contains /pdf, as EDoc.isPDF does")
    void shouldDetectPdf_whenContentTypeContainsPdf(String contentType, boolean expected) {
        assertThat(DocumentViewUrl.isPdfContentType(contentType)).isEqualTo(expected);
    }
}
