package ca.openosp.openo.form.pdfservlet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DrawnSignatureFile}: which posted path the Rx PDF takes its signature image from.
 *
 * <p>The contract: only the file name counts, and only the logged-in provider's own drawing in the
 * temp folder (signature_&lt;provider number&gt;&lt;13-digit time&gt;.jpg) that still exists is used.
 * Anything else means no image.</p>
 *
 * @since 2026-10-26
 */
@DisplayName("DrawnSignatureFile")
@Tag("unit")
@Tag("fast")
class DrawnSignatureFileUnitTest {

    private static final String PROVIDER_NO = "999998";
    private static final String TIME = "1761500000000";

    private File drawing;

    @BeforeEach
    void drawSignature() throws IOException {
        drawing = new File(System.getProperty("java.io.tmpdir"), "signature_" + PROVIDER_NO + TIME + ".jpg");
        Files.write(drawing.toPath(), new byte[]{1});
    }

    @AfterEach
    void removeDrawing() throws IOException {
        Files.deleteIfExists(drawing.toPath());
    }

    @Test
    @DisplayName("should find the provider's own drawing from the path the window posts")
    void shouldFindOwnDrawing_whenWindowPostsItsPath() throws IOException {
        File found = DrawnSignatureFile.find(drawing.getPath(), PROVIDER_NO);

        assertThat(found).isNotNull();
        assertThat(found.getCanonicalPath()).isEqualTo(drawing.getCanonicalPath());
    }

    @Test
    @DisplayName("should look in the temp folder whatever folder the posted path names")
    void shouldLookInTempFolder_whenPostedPathNamesAnotherFolder() throws IOException {
        File found = DrawnSignatureFile.find(new File("/var/lib/other", drawing.getName()).getPath(), PROVIDER_NO);

        assertThat(found).isNotNull();
        assertThat(found.getCanonicalPath()).isEqualTo(drawing.getCanonicalPath());
    }

    @Test
    @DisplayName("should not use another provider's drawing")
    void shouldNotUseDrawing_whenItIsAnotherProvidersDrawing() {
        assertThat(DrawnSignatureFile.find(drawing.getPath(), "999997")).isNull();
    }

    @Test
    @DisplayName("should not let a provider number match the start of a longer one")
    void shouldNotMatchLongerProviderNumber_whenNumberIsItsPrefix() {
        // To provider 99999 the request id reads as their number, then "8" and the time: 14 digits
        assertThat(DrawnSignatureFile.find(drawing.getPath(), "99999")).isNull();
    }

    @Test
    @DisplayName("should keep the time's leading zeros, so its digits can't shift into another provider's number")
    void shouldKeepLeadingZeros_whenTimeStartsWithZero() throws IOException {
        // Provider 10's drawing, which provider 101's time 0799999999999 would name without its zero
        File othersDrawing = new File(System.getProperty("java.io.tmpdir"), "signature_101799999999999.jpg");
        Files.write(othersDrawing.toPath(), new byte[]{1});
        try {
            assertThat(DrawnSignatureFile.find("signature_1010799999999999.jpg", "101")).isNull();
        } finally {
            Files.deleteIfExists(othersDrawing.toPath());
        }
    }

    @Test
    @DisplayName("should find the drawing when the server's locale writes numbers in other digits")
    void shouldFindOwnDrawing_whenDefaultLocaleUsesOtherDigits() throws IOException {
        Locale serverLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));

            File found = DrawnSignatureFile.find(drawing.getPath(), PROVIDER_NO);

            assertThat(found).isNotNull();
            assertThat(found.getCanonicalPath()).isEqualTo(drawing.getCanonicalPath());
        } finally {
            Locale.setDefault(serverLocale);
        }
    }

    @Test
    @DisplayName("should not use a drawing whose file is gone")
    void shouldNotUseDrawing_whenFileIsGone() throws IOException {
        Files.delete(drawing.toPath());

        assertThat(DrawnSignatureFile.find(drawing.getPath(), PROVIDER_NO)).isNull();
    }

    @ParameterizedTest(name = "posted path [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {
            "/var/lib/OscarDocument/oscar/document/scan.jpg",
            "http://localhost/oscar/images/logo.jpg",
            "/oscar/imageRenderingServlet?source=signature_preview",
            "signature_999998.jpg",
            "signature_999998abc0000000000.jpg",
            "signature_9999981761500000000.png"
    })
    @DisplayName("should use no image when the posted path isn't one of the provider's drawings")
    void shouldUseNoImage_whenPathIsNotOwnDrawing(String postedPath) {
        assertThat(DrawnSignatureFile.find(postedPath, PROVIDER_NO)).isNull();
    }
}
