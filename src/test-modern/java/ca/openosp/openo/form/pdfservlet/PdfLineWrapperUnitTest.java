package ca.openosp.openo.form.pdfservlet;

import com.itextpdf.text.pdf.BaseFont;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PdfLineWrapper}: breaking the prescriber name under the Rx PDF's signature line.
 *
 * <p>The contract: every line fits the width, lines break at spaces, a word wider than the width is
 * split inside, and no text is lost.</p>
 *
 * @since 2026-10-26
 */
@DisplayName("PdfLineWrapper")
@Tag("unit")
@Tag("fast")
class PdfLineWrapperUnitTest {

    private static final float WIDTH = 190f;
    private static final float FONT_SIZE = 10f;

    private static BaseFont font;

    @BeforeAll
    static void loadFont() throws Exception {
        font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }

    private void assertEveryLineFits(List<String> lines) {
        assertThat(lines).allSatisfy(line -> assertThat(font.getWidthPoint(line, FONT_SIZE)).isLessThanOrEqualTo(WIDTH));
    }

    @Test
    @DisplayName("should keep a short name on one line")
    void shouldKeepOneLine_whenNameFits() {
        assertThat(PdfLineWrapper.wrap("Dr. doctor openodoc", font, FONT_SIZE, WIDTH)).containsExactly("Dr. doctor openodoc");
    }

    @Test
    @DisplayName("should break a long signature at spaces into lines that fit")
    void shouldBreakAtSpaces_whenSignatureIsTooWide() {
        String signature = "Alex Openodoc, PA. Prescribed under the authority of Dr. Kirsten Welch";

        List<String> lines = PdfLineWrapper.wrap(signature, font, FONT_SIZE, WIDTH);

        assertThat(lines).hasSize(2);
        assertEveryLineFits(lines);
        assertThat(String.join(" ", lines)).isEqualTo(signature);
    }

    @Test
    @DisplayName("should split a word wider than the line inside the word")
    void shouldSplitInsideWord_whenWordIsWiderThanLine() {
        String word = "W".repeat(40);

        List<String> lines = PdfLineWrapper.wrap("By " + word, font, FONT_SIZE, WIDTH);

        assertThat(lines.get(0)).isEqualTo("By");
        assertEveryLineFits(lines);
        assertThat(String.join("", lines.subList(1, lines.size()))).isEqualTo(word);
    }

    @Test
    @DisplayName("should return one empty line for an empty name")
    void shouldReturnOneEmptyLine_whenNameIsEmpty() {
        assertThat(PdfLineWrapper.wrap("", font, FONT_SIZE, WIDTH)).containsExactly("");
    }
}
