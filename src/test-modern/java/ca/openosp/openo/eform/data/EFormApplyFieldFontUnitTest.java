package ca.openosp.openo.eform.data;

import ca.openosp.openo.commn.dao.EFormDataDao;
import ca.openosp.openo.managers.NioFileManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link EForm#applyFieldFont()}, which runs when an eForm is saved.
 *
 * <p>The method forces the DejaVu Sans fonts onto the form's fields so the browser and the PDF
 * draw the same text: it removes the form's own @font-face rules, which the PDF converter cannot
 * load, and writes one style that maps the field font to the DejaVu Sans installed on the server.
 * These tests pin down that contract: the form's font declarations go, the rest of its CSS stays,
 * the written style carries no file paths, and a re-save replaces the style instead of stacking
 * copies.</p>
 */
@DisplayName("EForm.applyFieldFont")
@Tag("unit")
@Tag("fast")
@Tag("eform")
class EFormApplyFieldFontUnitTest extends OpenOUnitTestBase {

    private static final String SAVED_STYLE_ID = "eform-field-font-saved";
    private static final String FORCED_FAMILY = "OpenO eForm Sans";

    @BeforeEach
    void registerStaticDependencies() {
        // EForm and ConvertToEdoc resolve these once at class load
        registerMock(EFormDataDao.class, mock(EFormDataDao.class));
        registerMock(NioFileManager.class, mock(NioFileManager.class));
    }

    private String applyTo(String html) {
        EForm eForm = new EForm();
        eForm.setFormHtml(html);
        eForm.applyFieldFont();
        return eForm.getFormHtml();
    }

    @Test
    @DisplayName("should remove the form's @font-face rules when the form is saved")
    void shouldRemoveFontFaceRules_whenFormIsSaved() {
        String html = "<!doctype html><html><head><style>"
                + "@font-face { font-family: 'DejaVu Sans'; src: url('../library/fonts/DejaVuSans.ttf'); }"
                + "</style></head><body><div style=\"font-family:'DejaVu Sans';\">text</div></body></html>";

        String saved = applyTo(html);

        assertThat(saved).doesNotContain("url('../library/fonts/DejaVuSans.ttf')");
    }

    @Test
    @DisplayName("should remove every @font-face rule regardless of case")
    void shouldRemoveAllFontFaceRules_whenFormHasSeveral() {
        String html = "<!doctype html><html><head><style>"
                + "@font-face { font-family: A; src: url('a.ttf'); }"
                + "@FONT-FACE { font-family: B; src: url('b.woff'); }"
                + "</style><style>"
                + "@font-face{font-family:C;src:url('c.otf')}"
                + "</style></head><body>text</body></html>";

        String saved = applyTo(html);

        assertThat(saved).doesNotContain("a.ttf").doesNotContain("b.woff").doesNotContain("c.otf");
    }

    @Test
    @DisplayName("should keep the form's other CSS rules when removing @font-face")
    void shouldKeepOtherCssRules_whenFormIsSaved() {
        String html = "<!doctype html><html><head><style>"
                + ".noborder { border: 0; overflow: hidden; }"
                + "@font-face { font-family: X; src: url('x.ttf'); }"
                + "body { background: #fff; }"
                + "</style></head><body>text</body></html>";

        String saved = applyTo(html);

        assertThat(saved).contains(".noborder { border: 0; overflow: hidden; }");
        assertThat(saved).contains("body { background: #fff; }");
        assertThat(saved).doesNotContain("x.ttf");
    }

    @Test
    @DisplayName("should leave @font-face alone when it is visible text, not a stylesheet")
    void shouldKeepFontFaceText_whenItIsVisibleContent() {
        String html = "<!doctype html><html><head></head><body>"
                + "<div>@font-face { font-family: shown; src: url('shown.ttf'); }</div>"
                + "</body></html>";

        String saved = applyTo(html);

        assertThat(saved).contains("shown.ttf");
    }

    @Test
    @DisplayName("should write the field font style when the form is saved")
    void shouldWriteFieldFontStyle_whenFormIsSaved() {
        String saved = applyTo("<!doctype html><html><head></head><body>text</body></html>");

        assertThat(saved).contains("id=\"" + SAVED_STYLE_ID + "\"");
        assertThat(saved).contains("font-family:'" + FORCED_FAMILY + "',sans-serif !important");
        assertThat(saved).contains("src:local('DejaVu Sans')");
        assertThat(saved).contains("text-rendering:optimizeLegibility !important");
        assertThat(saved).contains("font-synthesis:none");
    }

    @Test
    @DisplayName("should write no file paths, so the saved form works on any server")
    void shouldWriteNoFilePaths_whenFormIsSaved() {
        String saved = applyTo("<!doctype html><html><head></head><body>text</body></html>");

        int styleStart = saved.indexOf(SAVED_STYLE_ID);
        int styleEnd = saved.indexOf("</style>", styleStart);
        assertThat(saved.substring(styleStart, styleEnd)).doesNotContain("url(");
    }

    @Test
    @DisplayName("should replace its style when the form is saved again")
    void shouldReplaceOwnStyle_whenFormIsSavedAgain() {
        String once = applyTo("<!doctype html><html><head><style>"
                + "@font-face { font-family: X; src: url('x.ttf'); }"
                + "</style></head><body>text</body></html>");

        String twice = applyTo(once);

        assertThat(countOf(twice, SAVED_STYLE_ID)).isEqualTo(countOf(once, SAVED_STYLE_ID)).isEqualTo(1);
        assertThat(twice).isEqualTo(once);
    }

    private static int countOf(String html, String token) {
        int count = 0;
        for (int at = html.indexOf(token); at > -1; at = html.indexOf(token, at + 1)) {
            count++;
        }
        return count;
    }
}
