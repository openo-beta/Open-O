/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.utility;

import java.util.regex.Pattern;

import org.owasp.encoder.Encode;

/**
 * Helpers for HTML-encoding values that need to render in JSP output.
 *
 * <p>Use {@link #encodeForHtmlAllowingBreaks(Object)} for fields that may
 * contain literal {@code <br>} tags inserted by an upstream source (most
 * commonly HL7 lab NTE/OBX comments) — these need to render as line breaks
 * rather than as visible escaped text, while every other tag, attribute, and
 * entity stays escaped.
 */
public final class HtmlEncodingUtils {

    private static final Pattern ENCODED_BR =
            Pattern.compile("(?i)&lt;br\\s*/?&gt;");

    private HtmlEncodingUtils() {}

    /**
     * HTML-encode {@code value} for safe rendering, but leave literal
     * {@code <br>} tags (and the {@code <br/>} / {@code <br />} variants,
     * case-insensitive) intact so that line breaks embedded by upstream
     * sources render as line breaks.
     *
     * <p>All other tags, attributes, and entities remain escaped — this
     * does not widen the XSS surface beyond {@code <br>}, which cannot
     * carry script.
     *
     * @param value any value; {@code null} produces an empty string
     * @return the encoded string with {@code <br>} variants restored
     */
    public static String encodeForHtmlAllowingBreaks(Object value) {
        if (value == null) {
            return "";
        }
        String encoded = Encode.forHtml(value.toString());
        return ENCODED_BR.matcher(encoded).replaceAll("<br />");
    }

    /**
     * HTML-encode {@code value}, replacing literal {@code ;} delimiters in
     * the source string with {@code <br />} line breaks. Used for Rx
     * instruction fields ({@code fullOutLine}) where {@code ;} is the
     * conventional in-band line-break marker.
     *
     * <p>Splits on {@code ;} <i>before</i> encoding so that HTML entities
     * produced by the encoder (e.g. {@code &#39;} for an apostrophe) are
     * never mistaken for delimiters.
     *
     * @param value the raw instruction string; {@code null} produces an
     *              empty string
     * @return the encoded string with {@code ;} replaced by {@code <br />}
     */
    public static String encodeForHtmlWithSemicolonBreaks(String value) {
        if (value == null) {
            return "";
        }
        String[] pieces = value.split(";", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pieces.length; i++) {
            if (i > 0) {
                sb.append("<br />");
            }
            sb.append(Encode.forHtml(pieces[i]));
        }
        return sb.toString();
    }

    /**
     * Strip any upstream HTML markup/entities from {@code value}, HTML-encode
     * the resulting plain text, then re-inject {@code <br />} for the line
     * breaks the markup carried.
     *
     * <p>Use this for OLIS lab comment fields, where
     * {@link ca.openosp.openo.lab.ca.all.parsers.OLISHL7Handler} pre-bakes
     * markup ({@code <br/>}, {@code <span style=...>}, {@code &nbsp;}) into the
     * text. Routing through {@link HtmlTextCleaner#toPlainText(String)} first
     * removes that markup (so it does not render as a literal {@code &nbsp;}
     * wall or visible {@code <span>} tags), {@link Encode#forHtml(String)} then
     * escapes the clean text, and finally each newline becomes a <i>trusted</i>
     * {@code <br />} so multi-line comments still render across lines.
     *
     * <p>Only the literal {@code <br />} we insert is unescaped — it carries no
     * user data and cannot host script — so this is XSS-safe.
     *
     * @param value the raw (possibly markup-laced) comment text; {@code null}
     *              produces an empty string
     * @return the cleaned, encoded string with line breaks rendered as
     *         {@code <br />}
     */
    public static String encodeCleanTextWithBreaks(String value) {
        return Encode.forHtml(HtmlTextCleaner.toPlainText(value)).replace("\n", "<br />");
    }

    /**
     * HTML-encode already-plain {@code value} and render its {@code \n}
     * newlines as {@code <br />}.
     *
     * <p>Use this for text that is <em>already plain</em> — no HTML markup or
     * entities to strip — such as OLIS comment fields once they have been
     * decoded by {@link ca.openosp.openo.lab.ca.all.parsers.Hl7FormattedText}.
     * Unlike {@link #encodeCleanTextWithBreaks(String)} it does not run the
     * jsoup {@link HtmlTextCleaner} pass, so genuine {@code \n} line breaks
     * survive (jsoup would otherwise collapse them as whitespace).
     *
     * <p>Only the literal {@code <br />} we insert is unescaped — it carries no
     * user data and cannot host script — so this is XSS-safe.
     *
     * @param value the plain text; {@code null} produces an empty string
     * @return the encoded string with {@code \n} rendered as {@code <br />}
     */
    public static String encodeTextWithNewlineBreaks(String value) {
        if (value == null) {
            return "";
        }
        return Encode.forHtml(value).replace("\n", "<br />");
    }
}
