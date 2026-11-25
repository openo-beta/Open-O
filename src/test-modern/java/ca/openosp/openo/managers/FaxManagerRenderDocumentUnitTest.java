package ca.openosp.openo.managers;

import ca.openosp.openo.commn.exception.DocumentNotSendableException;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.documentManager.DocumentAttachmentManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FaxManagerImpl#renderDocument(LoggedInInfo, int, int)}: faxing a patient document
 * from the document view.
 *
 * <p>The contract: {@link DocumentManager#getSendableDemographicNo(LoggedInInfo, Document)} decides whether the
 * document can be sent at all, and its refusals pass through unchanged. The document must also be filed to the
 * patient the document view showed. The document manager renders an image into a new temp PDF but hands back the
 * stored file for a PDF, so a PDF is copied through {@link DocumentAttachmentManager#concatPDF(List)} and the
 * stored file never reaches the fax job. Fax rights are checked when the fax is sent.</p>
 *
 * @since 2026-11-24
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FaxManager renderDocument")
@Tag("unit")
@Tag("fast")
@Tag("manager")
@Tag("fax")
class FaxManagerRenderDocumentUnitTest extends OpenOUnitTestBase {

    private static final int DOCUMENT_NO = 15;
    private static final int DEMOGRAPHIC_NO = 3001;
    private static final int OTHER_DEMOGRAPHIC_NO = 3002;
    private static final Path STORED_PATH = Paths.get("/var/lib/OscarDocument/oscar/document/fake_mri_head.pdf");
    private static final Path IMAGE_PDF = Paths.get("/usr/local/tomcat/temp/tempPDF1/temporaryPDF1.pdf");
    private static final Path TEMP_COPY = Paths.get("/usr/local/tomcat/temp/tempPDF2/combinedPDF_2.pdf");

    @Mock
    private DocumentManager documentManager;

    @Mock
    private DocumentAttachmentManager documentAttachmentManager;

    @InjectMocks
    private FaxManagerImpl faxManager;

    private LoggedInInfo loggedInInfo;

    @BeforeEach
    void setUp() throws Exception {
        loggedInInfo = new LoggedInInfo();
        loggedInInfo.setLoggedInProvider(new Provider("999998"));

        givenDocument("application/pdf", STORED_PATH);
        when(documentManager.getSendableDemographicNo(any(LoggedInInfo.class), any(Document.class))).thenReturn(DEMOGRAPHIC_NO);
        when(documentAttachmentManager.concatPDF(anyList())).thenReturn(TEMP_COPY);
    }

    private void givenDocument(String contentType, Path rendered) throws PDFGenerationException {
        Document document = new Document(DOCUMENT_NO);
        document.setStatus('A');
        document.setContenttype(contentType);
        when(documentManager.getDocument(any(LoggedInInfo.class), eq(DOCUMENT_NO))).thenReturn(document);
        when(documentManager.renderDocument(any(LoggedInInfo.class), eq(String.valueOf(DOCUMENT_NO)))).thenReturn(rendered);
    }

    @Nested
    @DisplayName("when the document can be faxed")
    class WhenFaxable {

        @Test
        @DisplayName("should return a temporary copy, not the stored file, when the document is a PDF")
        void shouldReturnTempCopy_whenDocumentIsPdf() throws Exception {
            Path result = faxManager.renderDocument(loggedInInfo, DOCUMENT_NO, DEMOGRAPHIC_NO);

            assertThat(result).isEqualTo(TEMP_COPY).isNotEqualTo(STORED_PATH);
            verify(documentAttachmentManager).concatPDF(List.of(STORED_PATH));
        }

        @Test
        @DisplayName("should return the rendered temp PDF without a second copy when the document is an image")
        void shouldReturnRenderedPdf_whenDocumentIsImage() throws Exception {
            givenDocument("image/jpeg", IMAGE_PDF);

            assertThat(faxManager.renderDocument(loggedInInfo, DOCUMENT_NO, DEMOGRAPHIC_NO)).isEqualTo(IMAGE_PDF);
            verify(documentAttachmentManager, never()).concatPDF(anyList());
        }

        @Test
        @DisplayName("should pass on the render error when the document isn't a PDF or an image")
        void shouldThrowPdfGenerationException_whenDocumentCannotBeRendered() throws Exception {
            givenDocument("text/html", null);
            when(documentManager.renderDocument(any(LoggedInInfo.class), eq(String.valueOf(DOCUMENT_NO))))
                    .thenThrow(new PDFGenerationException("could not be converted into a PDF"));

            assertThatThrownBy(() -> faxManager.renderDocument(loggedInInfo, DOCUMENT_NO, DEMOGRAPHIC_NO))
                    .isInstanceOf(PDFGenerationException.class)
                    .hasMessageContaining("could not be converted");
            verify(documentAttachmentManager, never()).concatPDF(anyList());
        }
    }

    @Nested
    @DisplayName("when the document can't be faxed")
    class WhenRefused {

        @Test
        @DisplayName("should refuse when the document is filed to a different patient than the document view showed")
        void shouldRefuse_whenDocumentIsLinkedToAnotherPatient() throws Exception {
            when(documentManager.getSendableDemographicNo(any(LoggedInInfo.class), any(Document.class))).thenReturn(OTHER_DEMOGRAPHIC_NO);

            assertThatThrownBy(() -> faxManager.renderDocument(loggedInInfo, DOCUMENT_NO, DEMOGRAPHIC_NO))
                    .isInstanceOf(DocumentNotSendableException.class)
                    .hasMessage("It isn't linked to this patient.");
            verify(documentManager, never()).renderDocument(any(LoggedInInfo.class), anyString());
        }

        @Test
        @DisplayName("should pass on the document manager's refusal unchanged")
        void shouldPassOnRefusal_whenDocumentIsNotSendable() throws Exception {
            DocumentNotSendableException deleted = new DocumentNotSendableException("It has been deleted.");
            when(documentManager.getSendableDemographicNo(any(LoggedInInfo.class), any(Document.class))).thenThrow(deleted);

            assertThatThrownBy(() -> faxManager.renderDocument(loggedInInfo, DOCUMENT_NO, DEMOGRAPHIC_NO)).isSameAs(deleted);
            verify(documentManager, never()).renderDocument(any(LoggedInInfo.class), anyString());
        }

        @Test
        @DisplayName("should pass on a security refusal when the user can't open the patient")
        void shouldThrowSecurityException_whenUserCannotOpenPatient() throws Exception {
            when(documentManager.getSendableDemographicNo(any(LoggedInInfo.class), any(Document.class)))
                    .thenThrow(new SecurityException("Unauthorized access to patient record"));

            assertThatThrownBy(() -> faxManager.renderDocument(loggedInInfo, DOCUMENT_NO, DEMOGRAPHIC_NO))
                    .isInstanceOf(SecurityException.class);
            verify(documentManager, never()).renderDocument(any(LoggedInInfo.class), anyString());
        }
    }
}
