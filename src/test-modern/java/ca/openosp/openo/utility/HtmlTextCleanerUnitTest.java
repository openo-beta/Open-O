/**
 *
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package ca.openosp.openo.utility;

import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HtmlTextCleaner}.
 *
 * <p>Pinned regression cases for OLIS lab data shapes that previously leaked
 * raw HTML into rendered PDFs (see ticket A1 in {@code docs/olis/readiness-plan.md}).
 * Each test covers one input shape encountered in production OLIS messages or
 * in OpenO's own synthesized markup.</p>
 *
 * @since 2026-05-13
 * @see HtmlTextCleaner
 */
@DisplayName("HtmlTextCleaner unit tests")
@Tag("unit")
@Tag("fast")
public class HtmlTextCleanerUnitTest extends OpenOUnitTestBase {

    @Test
    @DisplayName("should strip <span> markup synthesized by OLISHL7Handler.getFullDocName")
    void shouldStripSpanMarkupFromDoctorName() {
        // Replicates OLISHL7Handler:2689 — the doctor-name field synthesizes <span>
        // markup so web display can style the registration number small/grey.
        // In the PDF path this leaked as literal text before A1 was fixed.
        String input = "DR. JANE Q TESTDOC <span style=\"margin-left:15px; font-size:8px; color:#333333;\">MD 999999</span>";

        String result = HtmlTextCleaner.toPlainText(input);

        assertThat(result)
                .doesNotContain("<span")
                .doesNotContain("</span>")
                .doesNotContain("style=")
                .contains("DR. JANE Q TESTDOC")
                .contains("MD 999999");
    }

    @Test
    @DisplayName("should decode &nbsp; entities into spaces")
    void shouldDecodeNbspEntities() {
        // Replicates the Specimen Comment shape from the A1 reporter's screenshot —
        // upstream OLIS data uses &nbsp; as word separators in clinical guidance
        // text, which iText would otherwise render as a wall of literal entities.
        String input = "Action&nbsp;required&nbsp;for&nbsp;you:&nbsp;Complete&nbsp;a&nbsp;new&nbsp;FIT";

        String result = HtmlTextCleaner.toPlainText(input);

        assertThat(result)
                .doesNotContain("&nbsp;")
                .doesNotContain("&amp;")
                .contains("Action")
                .contains("Complete a new FIT");
    }

    @Test
    @DisplayName("should preserve <br> as newline")
    void shouldPreserveBrAsNewline() {
        String input = "Line one<br/>Line two<br>Line three<br />Line four";

        String result = HtmlTextCleaner.toPlainText(input);

        assertThat(result).doesNotContain("<br");
        assertThat(result.split("\n")).hasSize(4);
        assertThat(result).contains("Line one");
        assertThat(result).contains("Line four");
    }

    @Test
    @DisplayName("should return empty string for null input")
    void shouldReturnEmptyForNullInput() {
        assertThat(HtmlTextCleaner.toPlainText(null)).isEmpty();
    }

    @Test
    @DisplayName("should return empty string for empty input")
    void shouldReturnEmptyForEmptyInput() {
        assertThat(HtmlTextCleaner.toPlainText("")).isEmpty();
    }

    @Test
    @DisplayName("should pass through plain text unchanged")
    void shouldPassThroughPlainTextUnchanged() {
        String input = "5.4 mmol/L";

        assertThat(HtmlTextCleaner.toPlainText(input)).isEqualTo("5.4 mmol/L");
    }

    @Test
    @DisplayName("should handle mixed HTML markup, entities, and breaks together")
    void shouldHandleMixedHtmlMarkup() {
        // Real-world OLIS field combining all three failure modes in one value.
        String input = "Action&nbsp;required:<br/>Complete a new <strong>FIT</strong> sample";

        String result = HtmlTextCleaner.toPlainText(input);

        assertThat(result)
                .doesNotContain("&nbsp;")
                .doesNotContain("<")
                .doesNotContain(">")
                .doesNotContain("&amp;")
                .contains("Action required")
                .contains("FIT")
                .contains("\n");
    }

    @Test
    @DisplayName("should not convert a literal backslash-n in the input into a newline")
    void shouldNotCorruptLiteralBackslashN() {
        // Regression: the internal <br> placeholder must not collide with real
        // content. A literal "\n" (the two characters backslash + n, e.g. in an
        // escaped Windows path) must survive intact and never become a line break.
        String input = "Path C:\\new\\file is not a line break";

        String result = HtmlTextCleaner.toPlainText(input);

        assertThat(result)
                .isEqualTo("Path C:\\new\\file is not a line break")
                .doesNotContain("\n");
    }

    @Test
    @DisplayName("should decode common HTML entities (&lt; &gt; &amp;)")
    void shouldDecodeCommonHtmlEntities() {
        String input = "Result &lt; 10 &amp; &gt; 5";

        assertThat(HtmlTextCleaner.toPlainText(input)).isEqualTo("Result < 10 & > 5");
    }

    @Test
    @DisplayName("should strip unknown tags without keeping inner text wrapped")
    void shouldStripArbitraryTags() {
        String input = "<div class=\"warn\"><b>Critical:</b> sample <em>rejected</em></div>";

        String result = HtmlTextCleaner.toPlainText(input);

        assertThat(result)
                .doesNotContain("<")
                .doesNotContain(">")
                .doesNotContain("class=")
                .contains("Critical:")
                .contains("sample")
                .contains("rejected");
    }
}
