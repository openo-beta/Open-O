package ca.openosp.openo.documentManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

import io.woo.htmltopdf.HtmlToPdf;
import io.woo.htmltopdf.HtmlToPdfObject;
import io.woo.htmltopdf.PdfPageSize;

public class InternalEDocConverter implements EDocConverterInterface {
    /**
     * Resolution the page is laid out at.
     * <p>
     * The renderer rounds a font size, and the height of a line, to a whole device pixel before it
     * measures anything. At the default 96 the rounding is large enough to change where text wraps
     * and to shorten every line, so the page does not match what the browser drew. Ten device
     * pixels to the CSS pixel puts every size the forms use on a whole number, so none of them is
     * rounded.
     */
    private static final int LAYOUT_DPI = 960;

    /**
     * Converts HTML to PDF using the internal io.woo.htmltopdf library.
     * Requires the required native .so file to be bundled (e.g.,
     * libwkhtmltox.ubuntu.noble.amd64.so).
     * This is now the only converter used; no configuration property is required.
     * 
     * @param document the complete HTML string to convert to PDF
     * @param os       the {@link ByteArrayOutputStream} where the generated PDF
     *                 content will be written
     * @throws Exception if the external process fails or PDF conversion is
     *                   unsuccessful
     */
    @Override
    public void convert(String document, OutputStream os) throws IOException {
        HashMap<String, String> htmlToPdfSettings = new HashMap<String, String>() {{
            put("load.blockLocalFileAccess", "false");
            put("web.enableIntelligentShrinking", "true");
            put("web.minimumFontSize", "10");
            // put("load.zoomFactor", "0.92");
            put("web.printMediaType", "true");
            put("web.defaultEncoding", "utf-8");
            put("T", "10mm");
            put("L", "8mm");
            put("R", "8mm");
            put("web.enableJavascript", "false");
        }};
        try (InputStream in = HtmlToPdf.create()
                .object(HtmlToPdfObject.forHtml(document, htmlToPdfSettings))
                .pageSize(PdfPageSize.Letter)
                .dpi(LAYOUT_DPI)
                .convert()) {
            IOUtils.copy(in, os);
        }
    }
}