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
}
