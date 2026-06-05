//CHECKSTYLE:OFF
package ca.openosp.openo.commn.model.enumerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Per OntarioMD HRM data dictionary: supported file formats for Binary report format.
 * Each constant validates that binary content actually matches the declared format,
 * using a format-specific library wherever one is available:
 * <ul>
 *   <li>PDF — Apache PDFBox ({@link PDDocument})</li>
 *   <li>JPEG/GIF/PNG/TIFF — {@link ImageIO} (TIFF support is built in since Java 9)</li>
 *   <li>HTML — content markup pre-check (no magic bytes; HTML is not well-formed XML in general)</li>
 *   <li>RTF — magic bytes ({\rtf), no lightweight parser exists in the project</li>
 * </ul>
 */
public enum BinaryFileExtension {

    PDF(".pdf") {
        @Override protected boolean detect(byte[] b) {
            try (PDDocument ignored = PDDocument.load(b)) {
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    },
    TIFF(".tiff") {
        @Override protected boolean detect(byte[] b) { return isImageOfFormat(b, "tif") || isImageOfFormat(b, "tiff"); }
    },
    RTF(".rtf") {
        // {\rtf is the RTF signature per spec; the version digit that follows is not hardcoded to '1'
        @Override protected boolean detect(byte[] b) { return startsWithMagic(b, '{','\\','r','t','f'); }
    },
    JPEG(".jpeg") {
        @Override protected boolean detect(byte[] b) { return isImageOfFormat(b, "jpeg"); }
    },
    GIF(".gif") {
        @Override protected boolean detect(byte[] b) { return isImageOfFormat(b, "gif"); }
    },
    PNG(".png") {
        @Override protected boolean detect(byte[] b) { return isImageOfFormat(b, "png"); }
    },
    HTML(".html") {
        @Override protected boolean detect(byte[] b) {
            String content = new String(b, StandardCharsets.UTF_8);
            if (content.charAt(0) == '\uFEFF') content = content.substring(1);
            content = content.trim();
            if (content.isEmpty()) return false;
            if (content.regionMatches(true, 0, "<!DOCTYPE html", 0, 14)
                || content.regionMatches(true, 0, "<html", 0, 5)) {
                return true;
            }
            // Look for common HTML tags (not generic tags)
            return HTML_TAG_PATTERN.matcher(content).find();
        }
    };

    // Matches an opening tag for a known HTML element. \b prevents partials like <pre> via <p> or <header> via <head>.
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
        "<(html|head|body|div|span|p|table|ul|li|br|h[1-6])\\b[^>]*>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final String extension;

    BinaryFileExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Returns true if {@code bytes} is a non-empty payload whose content matches this extension's format.
     * Null or empty input returns false.
     */
    public final boolean matchesContent(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return false;
        return detect(bytes);
    }

    protected abstract boolean detect(byte[] bytes);

    public boolean matches(String ext) {
        return this.extension.equalsIgnoreCase(ext);
    }

    public static BinaryFileExtension fromExtension(String ext) {
        if (ext == null) return null;
        for (BinaryFileExtension e : values()) {
            if (e.matches(ext)) return e;
        }
        return null;
    }

    public static String allValues() {
        StringBuilder sb = new StringBuilder();
        for (BinaryFileExtension e : values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.extension);
        }
        return sb.toString();
    }

    private static boolean isImageOfFormat(byte[] bytes, String expectedFormat) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (iis == null) return false;
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            while (readers.hasNext()) {
                ImageReaderSpi spi = readers.next().getOriginatingProvider();
                if (spi == null) continue;
                for (String name : spi.getFormatNames()) {
                    if (name.equalsIgnoreCase(expectedFormat)) return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean startsWithMagic(byte[] bytes, int... magic) {
        if (bytes.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if ((bytes[i] & 0xFF) != magic[i]) return false;
        }
        return true;
    }
}
