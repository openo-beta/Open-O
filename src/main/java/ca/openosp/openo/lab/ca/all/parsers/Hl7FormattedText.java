package ca.openosp.openo.lab.ca.all.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decoder for HL7 v2 "Formatted Text" (FT) data-type escape sequences,
 * producing neutral <em>plain text</em> (with real {@code \n} newlines) rather
 * than HTML.
 *
 * <p>OLIS sends comment and (occasionally) result fields as the HL7 FT data
 * type, whose presentation is expressed through in-band escape sequences
 * (HL7 v2.x Ch.&nbsp;2, <i>Use of escape sequences in text fields</i>):
 * {@code \.br\} (line break), {@code \.sp\} (vertical skip), {@code \.sk\}
 * (horizontal skip), {@code \.ce\} (centre), highlight on/off, indent, and the
 * {@code \F\ \S\ \T\ \R\ \E\} delimiter escapes. The OLIS upload handler runs a
 * small pre-pass that normalises {@code \H\}/{@code \N\} to the dotted
 * {@code \.H\}/{@code \.N\} form and substitutes {@code \SLASHHACK\} for an
 * escaped backslash and {@code \MUHACK\} for the micro sign, so those tokens
 * appear here too.
 *
 * <p>Historically {@code OLISHL7Handler.formatString} translated these escapes
 * straight to HTML ({@code <br/>}, {@code <span>}, {@code &nbsp;}), baking
 * presentation into model data that every non-HTML consumer (PDF, stored
 * measurement comments, export) then had to strip back out. This class
 * is the neutral seam: it decodes the FT escapes to plain text once, leaving
 * each consumer free to render that text however it needs — a PDF cell as-is,
 * an HTML view by escaping and turning {@code \n} into {@code <br>}.
 *
 * <p>Plain text cannot carry highlight, centring, or indentation, so those
 * formatting-only escapes are dropped (centring is flattened to a line break);
 * the surrounding text content is always preserved. Original spacing is kept
 * verbatim (it is not collapsed), so callers that need monospace alignment can
 * preserve it and callers that do not can normalise whitespace themselves.
 *
 * @since 2026-06-01
 */
public final class Hl7FormattedText {

    /** A {@code \...\} escape: two delimiters wrapping a backslash-free token. */
    private static final Pattern ESCAPE = Pattern.compile("\\\\([^\\\\]*)\\\\");

    /**
     * Escapes that carry an optional integer operand, e.g. {@code \.sp3\},
     * {@code \.sk 5\}. Matched against the upper-cased operator token.
     */
    private static final Pattern PARAM_OP =
            Pattern.compile("\\.(SP|SK|IN|TI)\\s*([+-]?)\\s*(\\d*)\\s*");

    /**
     * Upper bound on a single {@code \.sp\}/{@code \.sk\} repeat operand. The
     * operand is in-band data from the HL7 message; a malformed or hostile token
     * like {@code \.sp999999999\} would otherwise allocate an enormous string (or
     * overflow {@code int}) and break rendering of the whole message. 1000 is far
     * above any real formatting — the widest classic reports are ~132 columns and
     * vertical skips are only ever a handful of lines — so real OLIS comments are
     * never clamped; the cap exists purely to bound the pathological case.
     * <p>Package-private so the unit test can assert against it rather than a
     * hard-coded copy of the value.
     */
    static final int MAX_REPEAT = 1000;

    private Hl7FormattedText() {
        // utility class — no instances
    }

    /**
     * Decode an HL7 Formatted-Text string to plain text. Every {@code \...\}
     * escape is replaced by its plain-text equivalent; text outside escapes
     * (and any unterminated trailing backslash) is left verbatim.
     *
     * @param ft String the FT-encoded field value, may contain HL7 escape
     *           sequences; {@code null} is treated as empty.
     * @return String the decoded plain text with {@code \n} for line breaks,
     *         markup-free and never {@code null}.
     */
    public static String toPlainText(String ft) {
        if (ft == null || ft.isEmpty()) {
            return "";
        }
        Matcher m = ESCAPE.matcher(ft);
        StringBuilder sb = new StringBuilder(ft.length());
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(decodeOperator(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Map a single FT operator token to its plain-text equivalent.
     *
     * @param op String the operator text found between two {@code \} delimiters
     * @return String the decoded fragment (possibly empty)
     */
    private static String decodeOperator(String op) {
        if (op.isEmpty()) {
            // Adjacent delimiters "\\" denote a single literal backslash.
            return "\\";
        }
        // Hexadecimal escape \Xdd..\ — each pair of hex digits is one ISO-8859-1
        // byte/char (e.g. \X0D\ = CR, \X61\ = "a"). HL7 v2 Ch.2 / CT 13.3 special chars.
        if ((op.charAt(0) == 'X' || op.charAt(0) == 'x') && op.length() > 1) {
            String hex = op.substring(1);
            if (hex.length() % 2 == 0 && hex.matches("[0-9A-Fa-f]+")) {
                StringBuilder out = new StringBuilder(hex.length() / 2);
                for (int k = 0; k < hex.length(); k += 2) {
                    out.append((char) Integer.parseInt(hex.substring(k, k + 2), 16));
                }
                return out.toString();
            }
        }
        switch (op.toUpperCase()) {
            // Vertical movement -> newline. Centring cannot survive in plain
            // text, so it is flattened to a line break.
            case ".BR":
            case ".CE":
                return "\n";
            // Formatting-only toggles with no textual content.
            case ".H": case "H":   // highlight on
            case ".N": case "N":   // highlight off / normal
            case ".FE":            // fill mode
            case ".NF":            // no-fill mode
                return "";
            // HL7 delimiter escapes.
            case "F": return "|";
            case "S": return "^";
            case "T": return "&";
            case "R": return "~";
            case "E": return "\\"; // escape char (literal backslash)
            // Placeholders injected by the OLIS upload pre-pass.
            case "SLASHHACK": return "\\";
            case "MUHACK":    return "µ"; // micro sign
            default:
                return decodeParamOperator(op.toUpperCase());
        }
    }

    /**
     * Decode the operand-bearing escapes ({@code \.sp\}, {@code \.sk\},
     * {@code \.in\}, {@code \.ti\}). Unknown escapes are dropped (spec-robust);
     * this deliberately differs from the legacy {@code formatString}, which
     * appended the unrecognised operator text verbatim.
     *
     * @param key String the upper-cased operator token
     * @return String the decoded fragment (possibly empty)
     */
    private static String decodeParamOperator(String key) {
        Matcher m = PARAM_OP.matcher(key);
        if (!m.matches()) {
            return "";
        }
        String cmd = m.group(1);
        boolean negative = "-".equals(m.group(2));
        int count = 1;
        if (!m.group(3).isEmpty()) {
            try {
                count = Math.min(Integer.parseInt(m.group(3)), MAX_REPEAT);
            } catch (NumberFormatException e) {
                // Operand exceeds int range — treat as the (clamped) maximum
                // rather than letting parsing fail on a single bad message.
                count = MAX_REPEAT;
            }
        }
        if ("SP".equals(cmd)) {
            return "\n".repeat(Math.max(0, count));   // vertical skip -> blank lines
        }
        // SK (horizontal skip) and IN/TI (indent) all render as leading spaces in
        // the fixed-width font OLIS reports use, so the indented layout survives
        // (CT Tracker reqs 5.5.x/9.9.x/10.3.x/11.10.x/12.10.x/13.3.x; CV05). A
        // negative indent (\.in-N\) cannot remove already-emitted characters in this
        // single-pass decoder, so it clamps to no indent; persistent \.in\ across
        // following lines is approximated as a per-tag indent, matching the OLIS
        // sample reports where each wrapped line carries its own tag.
        if (negative) {
            return "";
        }
        return " ".repeat(Math.max(0, count));
    }
}
