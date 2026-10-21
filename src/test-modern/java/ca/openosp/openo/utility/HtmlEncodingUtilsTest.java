/**
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package ca.openosp.openo.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HtmlEncodingUtils")
@Tag("unit")
@Tag("fast")
@Tag("security")
class HtmlEncodingUtilsTest {

    @Test
    @DisplayName("returns empty string for null input")
    void shouldReturnEmptyForNull() {
        assertThat(HtmlEncodingUtils.encodeForHtmlAllowingBreaks(null)).isEmpty();
    }

    @Test
    @DisplayName("encodes < > & \" ' in plain text")
    void shouldEncodeHtmlSpecials() {
        String result = HtmlEncodingUtils.encodeForHtmlAllowingBreaks("<5.0 & >2.0 \"a\" 'b'");
        assertThat(result).doesNotContain("<5", "&2", "\"a\"", "'b'");
        assertThat(result).contains("&lt;5.0", "&amp;");
    }

    @ParameterizedTest
    @ValueSource(strings = {"<br>", "<br/>", "<br />", "<br  />", "<BR>", "<Br/>", "<bR />"})
    @DisplayName("preserves <br> variants as line breaks (case-insensitive)")
    void shouldRestoreBrVariants(String br) {
        String result = HtmlEncodingUtils.encodeForHtmlAllowingBreaks("line1" + br + "line2");
        assertThat(result).isEqualTo("line1<br />line2");
    }

    @Test
    @DisplayName("does not allow other tags through")
    void shouldNotAllowOtherTags() {
        String result = HtmlEncodingUtils.encodeForHtmlAllowingBreaks("<script>alert(1)</script>");
        assertThat(result)
                .doesNotContain("<script>")
                .doesNotContain("</script>")
                .contains("&lt;script&gt;");
    }

    @Test
    @DisplayName("does not allow attributes inside <br>")
    void shouldNotAllowBrAttributes() {
        String result = HtmlEncodingUtils.encodeForHtmlAllowingBreaks("<br onerror=alert(1)>");
        assertThat(result)
                .doesNotContain("<br onerror")
                .contains("&lt;br onerror");
    }

    @Test
    @DisplayName("converts non-string values via toString()")
    void shouldConvertNonStringInput() {
        assertThat(HtmlEncodingUtils.encodeForHtmlAllowingBreaks(42)).isEqualTo("42");
        assertThat(HtmlEncodingUtils.encodeForHtmlAllowingBreaks(true)).isEqualTo("true");
    }

    @Test
    @DisplayName("handles multiple <br> in one string")
    void shouldRestoreMultipleBrs() {
        String result = HtmlEncodingUtils.encodeForHtmlAllowingBreaks("a<br>b<BR/>c<br />d");
        assertThat(result).isEqualTo("a<br />b<br />c<br />d");
    }

    // --- encodeForHtmlWithSemicolonBreaks ---

    @Test
    @DisplayName("semicolon breaks: returns empty for null")
    void semicolonBreaksShouldReturnEmptyForNull() {
        assertThat(HtmlEncodingUtils.encodeForHtmlWithSemicolonBreaks(null)).isEmpty();
    }

    @Test
    @DisplayName("semicolon breaks: replaces ; with <br />")
    void semicolonBreaksShouldReplaceSemicolons() {
        String result = HtmlEncodingUtils.encodeForHtmlWithSemicolonBreaks("Take 1 tab;Once daily;With food");
        assertThat(result).isEqualTo("Take 1 tab<br />Once daily<br />With food");
    }

    @Test
    @DisplayName("semicolon breaks: does not break entities produced by Encode.forHtml")
    void semicolonBreaksShouldNotBreakEntities() {
        // Apostrophe encodes to &#39; which contains ; — must not be split
        String result = HtmlEncodingUtils.encodeForHtmlWithSemicolonBreaks("Take patient's tablet");
        assertThat(result).isEqualTo("Take patient&#39;s tablet");
    }

    @Test
    @DisplayName("semicolon breaks: encodes ampersand and semicolon together")
    void semicolonBreaksShouldHandleAmpersand() {
        String result = HtmlEncodingUtils.encodeForHtmlWithSemicolonBreaks("Mix A & B;Then take");
        assertThat(result).isEqualTo("Mix A &amp; B<br />Then take");
    }

    @Test
    @DisplayName("semicolon breaks: encodes < > inside instructions")
    void semicolonBreaksShouldEncodeAngleBrackets() {
        String result = HtmlEncodingUtils.encodeForHtmlWithSemicolonBreaks("Dose <5mg;Reduce >2 weeks");
        assertThat(result)
                .doesNotContain("<5", ">2")
                .contains("&lt;5mg", "&gt;2 weeks");
    }

    @Test
    @DisplayName("semicolon breaks: preserves trailing empties")
    void semicolonBreaksShouldKeepTrailingEmpties() {
        String result = HtmlEncodingUtils.encodeForHtmlWithSemicolonBreaks("a;b;");
        assertThat(result).isEqualTo("a<br />b<br />");
    }
}
