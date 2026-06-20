package ca.openosp.openo.lab.ca.all.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.owasp.encoder.Encode;

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

    /**
     * HTML fragment emitted for {@code \H\} (start highlighting). Rendered as
     * bold rather than a background colour so it stays legible against the lab
     * table's alternating row striping and in printed/greyscale output, and so
     * it mirrors cleanly to a bold font in the PDF renderer. Contains no message
     * data, so it is safe to emit verbatim around HTML-escaped content.
     */
    private static final String HIGHLIGHT_OPEN = "<span style=\"font-weight:bold\">";

    /** Closing tag for {@link #HIGHLIGHT_OPEN}. */
    private static final String HIGHLIGHT_CLOSE = "</span>";

    /**
     * A neutral run of decoded text carrying the highlight (bold) flag and no
     * presentation markup. Building block of {@link FtLine}, for renderers that
     * cannot consume HTML — e.g. the PDF creator turns each run into a bold-or-
     * normal iText chunk.
     */
    public static final class FtRun {
        private final String text;
        private final boolean bold;
        FtRun(String text, boolean bold) {
            this.text = text;
            this.bold = bold;
        }
        /** @return String the run's decoded text, markup-free, never {@code null}. */
        public String getText() { return text; }
        /** @return boolean whether this run is highlighted (rendered bold). */
        public boolean isBold() { return bold; }
    }

    /**
     * A neutral output line from {@link #toLines(String)}: the ordered
     * {@link FtRun}s plus whether the line is centre-aligned ({@code \.ce\}).
     */
    public static final class FtLine {
        private final List<FtRun> runs;
        private final boolean centered;
        FtLine(List<FtRun> runs, boolean centered) {
            this.runs = runs;
            this.centered = centered;
        }
        /** @return List&lt;FtRun&gt; the runs composing this line, in order, never {@code null}. */
        public List<FtRun> getRuns() { return runs; }
        /** @return boolean whether the line is centre-aligned. */
        public boolean isCentered() { return centered; }
    }

    /**
     * Decode an HL7 Formatted-Text string to a <em>safe HTML</em> fragment that
     * preserves the highlight ({@code \H\}/{@code \N\}) and centre
     * ({@code \.ce\}) presentation that {@link #toPlainText(String)} necessarily
     * drops, while remaining injection-proof.
     *
     * <p>Every run of literal message text is {@link Encode#forHtml(String)
     * HTML-escaped} <em>before</em> any markup is emitted, and the only tags in
     * the output come from a fixed internal whitelist ({@code <br/>}, a bold
     * {@code <span>}, a centred {@code <div>}, {@code &nbsp;}) that never carries
     * message data in a tag or attribute — so hostile FT content cannot inject
     * markup. This is the HTML counterpart to {@link #toPlainText(String)}: use
     * it for on-screen rendering of comment/notes and FT-typed result values
     * (CT&nbsp;Tracker 5.5.4 / 9.9.4 / 10.3.4 / 11.10.4 / 12.10.4 / 13.3.x), and
     * keep {@link #toPlainText(String)} for every non-HTML consumer.
     *
     * <p>Mapping: {@code \H\}/{@code \.H\} open and {@code \N\}/{@code \.N\}
     * close a bold span (an unterminated highlight auto-closes at end of input,
     * and each line is independently balanced); {@code \.ce\} ends the current
     * line and centres the next (centring is line-scoped, ending at the next
     * line break, per the OLIS interface spec); {@code \.br\} is a line break,
     * {@code \.sp n\} is {@code n} breaks, and {@code \.sk\}/{@code \.in\}/{@code
     * \.ti\} become leading {@code &nbsp;} runs (mirroring the spacing
     * {@link #toPlainText(String)} preserves). Delimiter ({@code \F\ \S\ \T\ \R\
     * \E\}) and {@code \Xdd\} hex escapes decode to their characters and are then
     * HTML-escaped like any other text.
     *
     * @param ft String the FT-encoded field value, may contain HL7 escape
     *           sequences; {@code null} is treated as empty.
     * @return String a safe HTML fragment, never {@code null}.
     */
    public static String toHtml(String ft) {
        HtmlSink sink = new HtmlSink();
        feed(ft, sink);
        return sink.build();
    }

    /**
     * Decode an HL7 Formatted-Text string to a neutral list of {@link FtLine}s —
     * the structured counterpart to {@link #toHtml(String)} for consumers that
     * render without HTML (the PDF creator turns each {@link FtRun} into a
     * bold-or-normal iText chunk and centres a line via paragraph alignment).
     * Both methods share one tokenizer ({@link #feed}), so highlight, centre, and
     * line-break handling stay identical between the on-screen and printed report.
     *
     * @param ft String the FT-encoded field value; {@code null} is treated as empty.
     * @return List&lt;FtLine&gt; the decoded lines, never {@code null}.
     */
    public static List<FtLine> toLines(String ft) {
        LineSink sink = new LineSink();
        feed(ft, sink);
        return sink.build();
    }

    /**
     * Tokenize an FT string, driving a {@link FtSink}. Literal text between
     * escapes (with any raw newlines split into line breaks) and each {@code \..\}
     * operator are pushed to the sink in order; the sink decides how to render.
     *
     * @param ft   String the FT-encoded value, may be {@code null}/empty
     * @param sink FtSink the render target receiving the decoded stream
     */
    private static void feed(String ft, FtSink sink) {
        if (ft == null || ft.isEmpty()) {
            return;
        }
        Matcher m = ESCAPE.matcher(ft);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                feedText(ft.substring(last, m.start()), sink);
            }
            dispatchOperator(m.group(1), sink);
            last = m.end();
        }
        if (last < ft.length()) {
            feedText(ft.substring(last), sink);
        }
    }

    /**
     * Push literal text to the sink, splitting on raw newlines. Raw CR/LF should
     * not occur in FT (the spec mandates {@code \.br\}), but if a lab sends them
     * they become line breaks rather than silently collapsing.
     */
    private static void feedText(String s, FtSink sink) {
        String norm = s.replace("\r\n", "\n").replace('\r', '\n');
        int start = 0;
        for (int i = 0; i < norm.length(); i++) {
            if (norm.charAt(i) == '\n') {
                sink.text(norm.substring(start, i));
                sink.lineBreak(false);
                start = i + 1;
            }
        }
        sink.text(norm.substring(start));
    }

    /** Dispatch one FT operator token (the text between two {@code \}) to the sink. */
    private static void dispatchOperator(String op, FtSink sink) {
        if (op.isEmpty()) {
            sink.text("\\");
            return;
        }
        if ((op.charAt(0) == 'X' || op.charAt(0) == 'x') && op.length() > 1) {
            String hex = op.substring(1);
            if (hex.length() % 2 == 0 && hex.matches("[0-9A-Fa-f]+")) {
                StringBuilder out = new StringBuilder(hex.length() / 2);
                for (int k = 0; k < hex.length(); k += 2) {
                    out.append((char) Integer.parseInt(hex.substring(k, k + 2), 16));
                }
                sink.text(out.toString());
                return;
            }
        }
        switch (op.toUpperCase()) {
            case ".BR": sink.lineBreak(false); return;
            case ".CE": sink.lineBreak(true); return; // end line, centre the next
            case ".H": case "H": sink.highlightOn(); return;
            case ".N": case "N": sink.highlightOff(); return;
            case ".FE": case ".NF": return; // fill-mode toggles: no textual content
            case "F": sink.text("|"); return;
            case "S": sink.text("^"); return;
            case "T": sink.text("&"); return;
            case "R": sink.text("~"); return;
            case "E": sink.text("\\"); return;
            case "SLASHHACK": sink.text("\\"); return;
            case "MUHACK": sink.text("µ"); return;
            default: dispatchParamOperator(op.toUpperCase(), sink);
        }
    }

    /** Dispatch the operand-bearing escapes ({@code \.sp\}, {@code \.sk\}, etc.) to the sink. */
    private static void dispatchParamOperator(String key, FtSink sink) {
        Matcher m = PARAM_OP.matcher(key);
        if (!m.matches()) {
            return; // unknown escape: dropped, matching toPlainText
        }
        String cmd = m.group(1);
        boolean negative = "-".equals(m.group(2));
        int count = 1;
        if (!m.group(3).isEmpty()) {
            try {
                count = Math.min(Integer.parseInt(m.group(3)), MAX_REPEAT);
            } catch (NumberFormatException e) {
                count = MAX_REPEAT;
            }
        }
        if ("SP".equals(cmd)) {
            for (int i = 0; i < Math.max(0, count); i++) {
                sink.lineBreak(false); // vertical skip -> blank lines
            }
            return;
        }
        if (negative) {
            return; // negative indent cannot un-emit characters in one pass
        }
        sink.indent(Math.max(0, count)); // SK/IN/TI -> horizontal indent
    }

    /**
     * Render target driven by {@link #feed}. One implementation per output format
     * keeps a single FT tokenizer behind both {@link #toHtml} and {@link #toLines}.
     */
    private interface FtSink {
        /** Append literal decoded text (the sink escapes/stores as appropriate). */
        void text(String s);
        /** Append {@code spaces} of horizontal indent ({@code \.sk\}/{@code \.in\}/{@code \.ti\}). */
        void indent(int spaces);
        /** End the current line; {@code centerNext} centres the line that follows. */
        void lineBreak(boolean centerNext);
        /** Start highlighting (bold). */
        void highlightOn();
        /** Stop highlighting (normal text). */
        void highlightOff();
    }

    /**
     * {@link FtSink} that assembles the safe-HTML fragment for {@link #toHtml}.
     * Accumulates the current line (with inline highlight spans), flushing on each
     * line break so every emitted line is independently HTML-balanced, then joins
     * the lines — centred lines as block {@code <div>}s, the rest separated by
     * {@code <br/>}.
     */
    private static final class HtmlSink implements FtSink {
        private final List<String> lines = new ArrayList<>();
        private final List<Boolean> centeredFlags = new ArrayList<>();
        private final StringBuilder line = new StringBuilder();
        private boolean highlightOpen = false;
        /** Whether the line currently being accumulated is centre-aligned. */
        private boolean currentCentered = false;

        @Override
        public void text(String s) {
            line.append(Encode.forHtml(s));
        }

        @Override
        public void indent(int spaces) {
            // &nbsp; survives HTML whitespace collapse, guaranteeing the indent.
            line.append("&nbsp;".repeat(spaces));
        }

        @Override
        public void highlightOn() {
            if (!highlightOpen) {
                line.append(HIGHLIGHT_OPEN);
                highlightOpen = true;
            }
        }

        @Override
        public void highlightOff() {
            if (highlightOpen) {
                line.append(HIGHLIGHT_CLOSE);
                highlightOpen = false;
            }
        }

        @Override
        public void lineBreak(boolean centerNext) {
            // A highlight open across the break is closed on this line and re-opened
            // on the next so each line's markup is self-balanced.
            if (highlightOpen) {
                line.append(HIGHLIGHT_CLOSE);
            }
            lines.add(line.toString());
            centeredFlags.add(currentCentered);
            line.setLength(0);
            currentCentered = centerNext;
            if (highlightOpen) {
                line.append(HIGHLIGHT_OPEN);
            }
        }

        /** Assemble the flushed lines into the final HTML fragment. */
        String build() {
            lineBreak(false); // flush the trailing line
            StringBuilder out = new StringBuilder();
            boolean first = true;
            boolean prevBlock = false;
            for (int i = 0; i < lines.size(); i++) {
                boolean block = centeredFlags.get(i);
                if (!first && !block && !prevBlock) {
                    out.append("<br/>");
                }
                if (block) {
                    out.append("<div style=\"text-align:center\">").append(lines.get(i)).append("</div>");
                } else {
                    out.append(lines.get(i));
                }
                first = false;
                prevBlock = block;
            }
            return out.toString();
        }
    }

    /**
     * {@link FtSink} that assembles the neutral {@link FtLine} list for
     * {@link #toLines}. Accumulates a run until the highlight state changes or the
     * line breaks, so each {@link FtRun} is uniformly bold or normal.
     */
    private static final class LineSink implements FtSink {
        private final List<FtLine> lines = new ArrayList<>();
        private List<FtRun> runs = new ArrayList<>();
        private final StringBuilder run = new StringBuilder();
        private boolean bold = false;
        private boolean currentCentered = false;

        @Override
        public void text(String s) {
            run.append(s);
        }

        @Override
        public void indent(int spaces) {
            // Fixed-width consumers (PDF Courier) preserve literal spaces verbatim.
            run.append(" ".repeat(spaces));
        }

        @Override
        public void highlightOn() {
            if (!bold) {
                flushRun();
                bold = true;
            }
        }

        @Override
        public void highlightOff() {
            if (bold) {
                flushRun();
                bold = false;
            }
        }

        @Override
        public void lineBreak(boolean centerNext) {
            flushRun();
            lines.add(new FtLine(runs, currentCentered));
            runs = new ArrayList<>();
            currentCentered = centerNext;
        }

        private void flushRun() {
            if (run.length() > 0) {
                runs.add(new FtRun(run.toString(), bold));
                run.setLength(0);
            }
        }

        List<FtLine> build() {
            lineBreak(false); // flush the trailing line
            return lines;
        }
    }
}
