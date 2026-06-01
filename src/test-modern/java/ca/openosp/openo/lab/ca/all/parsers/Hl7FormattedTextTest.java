package ca.openosp.openo.lab.ca.all.parsers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Hl7FormattedText}, the HL7 Formatted-Text (FT) escape
 * decoder. Pins the plain-text decode contract that will replace the legacy
 * HTML-emitting {@code OLISHL7Handler.formatString}.
 */
@DisplayName("Hl7FormattedText")
@Tag("unit")
@Tag("fast")
class Hl7FormattedTextTest {

    @Test
    @DisplayName("returns empty string for null")
    void shouldReturnEmptyForNull() {
        assertThat(Hl7FormattedText.toPlainText(null)).isEmpty();
    }

    @Test
    @DisplayName("returns empty string for empty input")
    void shouldReturnEmptyForEmpty() {
        assertThat(Hl7FormattedText.toPlainText("")).isEmpty();
    }

    @Test
    @DisplayName("leaves plain text without escapes unchanged")
    void shouldLeavePlainTextUnchanged() {
        assertThat(Hl7FormattedText.toPlainText("Specimen slightly hemolyzed"))
                .isEqualTo("Specimen slightly hemolyzed");
    }

    @Test
    @DisplayName("preserves original spacing (does not collapse runs)")
    void shouldPreserveSpacing() {
        // monospace alignment relies on multiple spaces surviving
        assertThat(Hl7FormattedText.toPlainText("Col1     Col2     Col3"))
                .isEqualTo("Col1     Col2     Col3");
    }

    @Test
    @DisplayName("converts \\.br\\ to a newline")
    void shouldConvertBreakToNewline() {
        assertThat(Hl7FormattedText.toPlainText("Line one\\.br\\Line two"))
                .isEqualTo("Line one\nLine two");
    }

    @Test
    @DisplayName("converts multiple \\.br\\ to multiple newlines")
    void shouldConvertMultipleBreaks() {
        assertThat(Hl7FormattedText.toPlainText("a\\.br\\b\\.br\\c"))
                .isEqualTo("a\nb\nc");
    }

    @Test
    @DisplayName("\\.sp\\ skips one line, \\.sp n\\ skips n lines")
    void shouldHandleVerticalSkip() {
        assertThat(Hl7FormattedText.toPlainText("a\\.sp\\b")).isEqualTo("a\nb");
        assertThat(Hl7FormattedText.toPlainText("a\\.sp3\\b")).isEqualTo("a\n\n\nb");
    }

    @Test
    @DisplayName("\\.sk\\ skips one space, \\.sk n\\ skips n spaces")
    void shouldHandleHorizontalSkip() {
        assertThat(Hl7FormattedText.toPlainText("a\\.sk\\b")).isEqualTo("a b");
        assertThat(Hl7FormattedText.toPlainText("a\\.sk5\\b")).isEqualTo("a     b");
    }

    @Test
    @DisplayName("drops highlight escapes but keeps the highlighted text")
    void shouldDropHighlightKeepText() {
        assertThat(Hl7FormattedText.toPlainText("Call if \\.H\\STAT\\.N\\ required"))
                .isEqualTo("Call if STAT required");
    }

    @Test
    @DisplayName("drops raw (un-normalised) \\H\\ / \\N\\ highlight escapes too")
    void shouldDropRawHighlight() {
        assertThat(Hl7FormattedText.toPlainText("Call if \\H\\STAT\\N\\ required"))
                .isEqualTo("Call if STAT required");
    }

    @Test
    @DisplayName("flattens \\.ce\\ centring to a line break")
    void shouldFlattenCentring() {
        assertThat(Hl7FormattedText.toPlainText("title\\.ce\\centered body"))
                .isEqualTo("title\ncentered body");
    }

    @Test
    @DisplayName("decodes HL7 delimiter escapes \\F\\ \\S\\ \\T\\ \\R\\ \\E\\")
    void shouldDecodeDelimiterEscapes() {
        assertThat(Hl7FormattedText.toPlainText("a\\F\\b")).isEqualTo("a|b");
        assertThat(Hl7FormattedText.toPlainText("a\\S\\b")).isEqualTo("a^b");
        assertThat(Hl7FormattedText.toPlainText("a\\T\\b")).isEqualTo("a&b");
        assertThat(Hl7FormattedText.toPlainText("a\\R\\b")).isEqualTo("a~b");
        assertThat(Hl7FormattedText.toPlainText("a\\E\\b")).isEqualTo("a\\b");
    }

    @Test
    @DisplayName("decodes the OLIS upload pre-pass placeholders")
    void shouldDecodePrepassPlaceholders() {
        assertThat(Hl7FormattedText.toPlainText("path\\SLASHHACK\\file")).isEqualTo("path\\file");
        assertThat(Hl7FormattedText.toPlainText("10 \\MUHACK\\g")).isEqualTo("10 µg");
    }

    @Test
    @DisplayName("treats adjacent delimiters \\\\ as a single literal backslash")
    void shouldHandleAdjacentDelimiters() {
        assertThat(Hl7FormattedText.toPlainText("a\\\\b")).isEqualTo("a\\b");
        assertThat(Hl7FormattedText.toPlainText("a\\\\\\\\b")).isEqualTo("a\\\\b");
    }

    @Test
    @DisplayName("ignores indent escapes (no plain-text content)")
    void shouldIgnoreIndentEscapes() {
        assertThat(Hl7FormattedText.toPlainText("a\\.in3\\b\\.ti2\\c")).isEqualTo("abc");
    }

    @ParameterizedTest
    @ValueSource(strings = {".fe", ".nf"})
    @DisplayName("ignores fill-mode toggles")
    void shouldIgnoreFillModeToggles(String op) {
        assertThat(Hl7FormattedText.toPlainText("a\\" + op + "\\b")).isEqualTo("ab");
    }

    @Test
    @DisplayName("drops unknown escapes (spec-robust)")
    void shouldDropUnknownEscapes() {
        assertThat(Hl7FormattedText.toPlainText("before\\.xyz\\after")).isEqualTo("beforeafter");
    }

    @Test
    @DisplayName("treats an unterminated escape as literal text")
    void shouldTreatUnterminatedEscapeAsLiteral() {
        assertThat(Hl7FormattedText.toPlainText("abc\\def")).isEqualTo("abc\\def");
    }

    @Test
    @DisplayName("decodes a realistic multi-line OLIS comment")
    void shouldDecodeRealisticOlisComment() {
        String ft = "Line one of interpretation.\\.br\\Line two should appear below it."
                + "\\.br\\Call lab if \\.H\\STAT\\.N\\ required.";
        assertThat(Hl7FormattedText.toPlainText(ft))
                .isEqualTo("Line one of interpretation.\n"
                        + "Line two should appear below it.\n"
                        + "Call lab if STAT required.");
    }

    @Test
    @DisplayName("produces no HTML markup or entities")
    void shouldProduceNoMarkup() {
        String ft = "x\\.br\\y \\.H\\z\\.N\\ a\\.sk3\\b";
        String out = Hl7FormattedText.toPlainText(ft);
        assertThat(out)
                .doesNotContain("<br")
                .doesNotContain("<span")
                .doesNotContain("&nbsp;")
                .doesNotContain("</");
    }

    @Test
    @DisplayName("clamps an oversized repeat operand instead of exhausting memory")
    void shouldClampOversizedRepeatOperand() {
        // A huge \.sp\ operand must not allocate an enormous string. It is clamped
        // to MAX_REPEAT, and an operand beyond int range is clamped too rather than
        // throwing NumberFormatException. Reference the constant so this test stays
        // correct if the cap is retuned.
        int cap = Hl7FormattedText.MAX_REPEAT;

        assertThat(Hl7FormattedText.toPlainText("a\\.sp999999999\\b"))
                .hasSize(1 + cap + 1)
                .startsWith("a\n")
                .endsWith("\nb");

        assertThat(Hl7FormattedText.toPlainText("a\\.sk999999999\\b"))
                .hasSize(1 + cap + 1);

        // Operand wider than Integer.MAX_VALUE: clamped, not thrown.
        assertThat(Hl7FormattedText.toPlainText("\\.sp99999999999999999999\\"))
                .hasSize(cap);
    }
}
