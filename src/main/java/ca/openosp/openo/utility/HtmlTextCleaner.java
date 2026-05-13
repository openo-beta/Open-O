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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Cleans HTML-laced text for non-HTML rendering targets (PDF, plain text).
 *
 * <p>Source data from external systems (especially OLIS lab results) frequently
 * contains HTML markup like {@code <span style="...">} for styling and entities
 * like {@code &nbsp;} for whitespace. When that data is rendered to non-HTML
 * targets such as iText PDFs, the markup leaks through as literal characters,
 * making the output unreadable. In clinical contexts this is a patient-safety
 * concern when actionable guidance becomes a wall of {@code &nbsp;} entities.</p>
 *
 * <p>This utility uses Jsoup to parse the input, strip all tags, decode HTML
 * entities, and normalise whitespace. {@code <br>} tags are preserved as
 * newlines (the only block-level structure most plain-text callers need).</p>
 *
 * <p><b>Use this for plain-text output paths only.</b> For HTML output paths
 * (JSP, web rendering) prefer the encode-then-selective-unescape approach,
 * which keeps the output HTML-safe rather than flattening structure.</p>
 *
 * @since 2026-05-13
 */
public final class HtmlTextCleaner {

    private static final String BR_PLACEHOLDER = "\\n";

    private HtmlTextCleaner() {
        // utility class — no instances
    }

    /**
     * Returns a plain-text rendering of input that may contain HTML markup
     * and/or HTML entities.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "DR. SMITH <span style=\"...\">MD 12345</span>"} &rarr;
     *       {@code "DR. SMITH MD 12345"}</li>
     *   <li>{@code "Action&nbsp;required&nbsp;today"} &rarr;
     *       {@code "Action required today"}</li>
     *   <li>{@code "Line one<br/>Line two"} &rarr; {@code "Line one\nLine two"}</li>
     * </ul>
     *
     * @param input String the input text, may contain HTML markup or entities.
     *              {@code null} is treated as empty.
     * @return String the input with tags stripped, entities decoded, whitespace
     *         normalised, and {@code <br>} preserved as newlines. Never {@code null}.
     */
    public static String toPlainText(String input) {
        if (input == null) {
            return "";
        }
        Document doc = Jsoup.parse(input);
        doc.outputSettings().prettyPrint(false);
        doc.select("br").append(BR_PLACEHOLDER);
        return doc.text().replace(BR_PLACEHOLDER, "\n");
    }
}
