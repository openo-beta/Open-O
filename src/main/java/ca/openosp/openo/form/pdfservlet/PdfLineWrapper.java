package ca.openosp.openo.form.pdfservlet;

import com.itextpdf.text.pdf.BaseFont;

import java.util.ArrayList;
import java.util.List;

/**
 * Breaks text drawn straight onto a PDF page into lines that fit a width, for text that iText
 * doesn't wrap by itself, such as the prescriber name under the signature line of the Rx PDF.
 *
 * @since 2026-10-26
 */
final class PdfLineWrapper {

    private PdfLineWrapper() {
    }

    /**
     * Breaks text into lines no wider than a width: at spaces where it can, and inside a word only
     * when the word alone is too wide.
     *
     * @param text String the text to break
     * @param font BaseFont the font it is drawn in
     * @param fontSize float the font size
     * @param width float the widest a line can be, in points
     * @return List&lt;String&gt; the lines, at least one
     */
    static List<String> wrap(String text, BaseFont font, float fontSize, float width) {
        List<String> lines = new ArrayList<>();
        String line = "";
        for (String word : text.trim().split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.getWidthPoint(candidate, fontSize) <= width) {
                line = candidate;
                continue;
            }
            if (!line.isEmpty()) {
                lines.add(line);
            }
            // A word wider than the line is split where it runs out of room
            while (word.length() > 1 && font.getWidthPoint(word, fontSize) > width) {
                int end = word.length() - 1;
                while (end > 1 && font.getWidthPoint(word.substring(0, end), fontSize) > width) {
                    end--;
                }
                lines.add(word.substring(0, end));
                word = word.substring(end);
            }
            line = word;
        }
        lines.add(line);
        return lines;
    }
}
