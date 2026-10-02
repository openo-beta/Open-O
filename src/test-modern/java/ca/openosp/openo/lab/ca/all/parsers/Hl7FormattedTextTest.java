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
    @DisplayName("decodes \\Xdd\\ hexadecimal escapes to ISO-8859-1 characters")
    void shouldDecodeHexEscapes() {
        assertThat(Hl7FormattedText.toPlainText("\\X61\\")).isEqualTo("a");
        // E9 = é in ISO-8859-1
        assertThat(Hl7FormattedText.toPlainText("caf\\XE9\\")).isEqualTo("café");
        // multi-byte: CR LF
        assertThat(Hl7FormattedText.toPlainText("a\\X0D0A\\b")).isEqualTo("a\r\nb");
        // invalid/odd hex is dropped (spec-robust), like other unknown escapes
        assertThat(Hl7FormattedText.toPlainText("a\\Xizz\\b")).isEqualTo("ab");
        assertThat(Hl7FormattedText.toPlainText("a\\X6\\b")).isEqualTo("ab");
    }

    @Test
    @DisplayName("treats adjacent delimiters \\\\ as a single literal backslash")
    void shouldHandleAdjacentDelimiters() {
        assertThat(Hl7FormattedText.toPlainText("a\\\\b")).isEqualTo("a\\b");
        assertThat(Hl7FormattedText.toPlainText("a\\\\\\\\b")).isEqualTo("a\\\\b");
    }

    @Test
    @DisplayName("renders indent escapes as leading spaces (fixed-width layout)")
    void shouldRenderIndentAsSpaces() {
        // \.in3\ and \.ti2\ emit their operand count of spaces so the indented
        // layout the OLIS reference reports rely on survives in a fixed-width font.
        assertThat(Hl7FormattedText.toPlainText("a\\.in3\\b\\.ti2\\c")).isEqualTo("a   b  c");
    }

    @Test
    @DisplayName("handles absolute, signed and spaced indent operands (.in4 / .in+4 / .in 4 / .in-4)")
    void shouldHandleSignedIndentOperands() {
        assertThat(Hl7FormattedText.toPlainText("x\\.in4\\y")).isEqualTo("x    y");
        assertThat(Hl7FormattedText.toPlainText("x\\.in+4\\y")).isEqualTo("x    y");
        assertThat(Hl7FormattedText.toPlainText("x\\.in 4\\y")).isEqualTo("x    y");
        // A negative indent cannot remove already-emitted characters in one pass.
        assertThat(Hl7FormattedText.toPlainText("x\\.in-4\\y")).isEqualTo("xy");
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

        // Operand wider than Integer.MAX_VALUE: clamped, not thrown. Cover both
        // operand-bearing operators (\.sp\ and \.sk\) since they share the path.
        assertThat(Hl7FormattedText.toPlainText("\\.sp99999999999999999999\\"))
                .hasSize(cap);
        assertThat(Hl7FormattedText.toPlainText("\\.sk99999999999999999999\\"))
                .hasSize(cap);
    }

    // ------------------------------------------------------------------
    // toHtml(): the safe-HTML sibling that preserves highlight (\H\/\N\) and
    // centre (\.ce\) presentation the plain-text decode necessarily drops.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("toHtml: null and empty decode to empty string")
    void shouldReturnEmptyHtmlForNullAndEmpty() {
        assertThat(Hl7FormattedText.toHtml(null)).isEmpty();
        assertThat(Hl7FormattedText.toHtml("")).isEmpty();
    }

    @Test
    @DisplayName("toHtml: HTML-escapes literal text so content cannot inject markup")
    void shouldHtmlEscapeLiteralText() {
        assertThat(Hl7FormattedText.toHtml("a < b & <script>alert(1)</script>"))
                .isEqualTo("a &lt; b &amp; &lt;script&gt;alert(1)&lt;/script&gt;");
    }

    @Test
    @DisplayName("toHtml: \\H\\..\\N\\ wraps the enclosed text in a bold span")
    void shouldWrapHighlightInBoldSpan() {
        assertThat(Hl7FormattedText.toHtml("normal \\H\\bold\\N\\ normal"))
                .isEqualTo("normal <span style=\"font-weight:bold\">bold</span> normal");
    }

    @Test
    @DisplayName("toHtml: highlighted text is itself HTML-escaped")
    void shouldEscapeHighlightedContent() {
        assertThat(Hl7FormattedText.toHtml("\\H\\<b>x</b>\\N\\"))
                .isEqualTo("<span style=\"font-weight:bold\">&lt;b&gt;x&lt;/b&gt;</span>");
    }

    @Test
    @DisplayName("toHtml: an unterminated \\H\\ auto-closes at end of input")
    void shouldAutoCloseUnterminatedHighlight() {
        assertThat(Hl7FormattedText.toHtml("\\H\\still bold"))
                .isEqualTo("<span style=\"font-weight:bold\">still bold</span>");
    }

    @Test
    @DisplayName("toHtml: the dotted \\.H\\/\\.N\\ form is accepted too")
    void shouldAcceptDottedHighlightForm() {
        assertThat(Hl7FormattedText.toHtml("\\.H\\x\\.N\\"))
                .isEqualTo("<span style=\"font-weight:bold\">x</span>");
    }

    @Test
    @DisplayName("toHtml: highlight spanning a line break is balanced on each line")
    void shouldBalanceHighlightAcrossLineBreak() {
        assertThat(Hl7FormattedText.toHtml("\\H\\one\\.br\\two\\N\\"))
                .isEqualTo("<span style=\"font-weight:bold\">one</span>"
                        + "<br/><span style=\"font-weight:bold\">two</span>");
    }

    @Test
    @DisplayName("toHtml: \\.br\\ becomes a line break")
    void shouldRenderBreakAsBr() {
        assertThat(Hl7FormattedText.toHtml("line1\\.br\\line2"))
                .isEqualTo("line1<br/>line2");
    }

    @Test
    @DisplayName("toHtml: \\.sp2\\ becomes two line breaks")
    void shouldRenderVerticalSkipAsBreaks() {
        assertThat(Hl7FormattedText.toHtml("a\\.sp2\\b"))
                .isEqualTo("a<br/><br/>b");
    }

    @Test
    @DisplayName("toHtml: \\.ce\\ ends the current line and centres the next")
    void shouldCentreNextLine() {
        assertThat(Hl7FormattedText.toHtml("header\\.ce\\centred\\.br\\after"))
                .isEqualTo("header"
                        + "<div style=\"text-align:center\">centred</div>"
                        + "after");
    }

    @Test
    @DisplayName("toHtml: \\.in\\/\\.sk\\ indent becomes leading &nbsp; runs")
    void shouldRenderIndentAsNbsp() {
        assertThat(Hl7FormattedText.toHtml("\\.in3\\x"))
                .isEqualTo("&nbsp;&nbsp;&nbsp;x");
    }

    @Test
    @DisplayName("toHtml: delimiter and hex escapes decode then get HTML-escaped")
    void shouldDecodeThenEscapeDelimiterAndHexEscapes() {
        // \S\ -> '^', \T\ -> '&' (which must then be escaped to &amp;)
        assertThat(Hl7FormattedText.toHtml("a\\S\\b\\T\\c")).isEqualTo("a^b&amp;c");
        // \X3C\ -> '<' (0x3C), which must be escaped to &lt; not emitted raw
        assertThat(Hl7FormattedText.toHtml("\\X3C\\b")).isEqualTo("&lt;b");
    }

    // ------------------------------------------------------------------
    // toLines(): the neutral structured model the PDF renderer consumes. Shares
    // the same tokenizer as toHtml, so highlight/centre/break stay in lockstep.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("toLines: a plain run is one non-bold, non-centred line")
    void shouldReturnSinglePlainLine() {
        var lines = Hl7FormattedText.toLines("hello world");
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).isCentered()).isFalse();
        assertThat(lines.get(0).getRuns()).singleElement()
                .satisfies(r -> {
                    assertThat(r.getText()).isEqualTo("hello world");
                    assertThat(r.isBold()).isFalse();
                });
    }

    @Test
    @DisplayName("toLines: \\.H\\..\\.N\\ splits into normal/bold/normal runs")
    void shouldSplitHighlightIntoRuns() {
        var runs = Hl7FormattedText.toLines("a \\.H\\b\\.N\\ c").get(0).getRuns();
        assertThat(runs).hasSize(3);
        assertThat(runs.get(0).getText()).isEqualTo("a ");
        assertThat(runs.get(0).isBold()).isFalse();
        assertThat(runs.get(1).getText()).isEqualTo("b");
        assertThat(runs.get(1).isBold()).isTrue();
        assertThat(runs.get(2).getText()).isEqualTo(" c");
        assertThat(runs.get(2).isBold()).isFalse();
    }

    @Test
    @DisplayName("toLines: \\.ce\\ marks the next line centred; \\.br\\ does not")
    void shouldMarkCentredLine() {
        var lines = Hl7FormattedText.toLines("title\\.ce\\middle\\.br\\foot");
        assertThat(lines).hasSize(3);
        assertThat(lines.get(0).isCentered()).isFalse(); // "title"
        assertThat(lines.get(1).isCentered()).isTrue();  // "middle"
        assertThat(lines.get(2).isCentered()).isFalse(); // "foot"
        assertThat(lines.get(1).getRuns().get(0).getText()).isEqualTo("middle");
    }

    @Test
    @DisplayName("toLines: indent escapes become literal spaces in the run text")
    void shouldRenderIndentAsLiteralSpaces() {
        var runs = Hl7FormattedText.toLines("\\.in3\\x").get(0).getRuns();
        assertThat(runs.get(0).getText()).isEqualTo("   x");
    }

    @Test
    @DisplayName("toLines: delimiter/hex escapes decode to raw characters (no HTML-escaping)")
    void shouldDecodeRawCharactersForLines() {
        // unlike toHtml, the neutral model keeps '<'/'&' raw for the consumer to handle
        var runs = Hl7FormattedText.toLines("a\\T\\b\\X3C\\c").get(0).getRuns();
        assertThat(runs.get(0).getText()).isEqualTo("a&b<c");
    }
}
