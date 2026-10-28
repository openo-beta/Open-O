package ca.openosp.openo.form.pdfservlet;

import ca.openosp.openo.utility.DigitalSignatureUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Finds the signature a provider drew in the prescription window, which the Rx PDF puts above the
 * signature line. The window posts back the path of the drawing's temp file.
 *
 * @since 2026-10-26
 */
final class DrawnSignatureFile {

    private static final String PREFIX = "signature_";
    private static final String SUFFIX = ".jpg";

    /** Digits of the {@code System.currentTimeMillis()} that ends each drawing's request id (true until 2286). */
    private static final int TIME_DIGITS = 13;

    private DrawnSignatureFile() {
    }

    /**
     * Finds a drawn signature from the path the window posted. Only the file name is used, and only
     * one of the provider's own drawings in the temp folder is accepted, named as
     * {@link DigitalSignatureUtils#getTempFilePath} names it: the provider's number, then the time.
     *
     * @param postedPath String the path the window posted, which may be null or empty
     * @param providerNo String the logged-in provider's number
     * @return File the drawing, or null when the path isn't one of the provider's drawings or the file is gone
     */
    static File find(String postedPath, String providerNo) {
        if (StringUtils.isEmpty(postedPath) || StringUtils.isEmpty(providerNo)) {
            return null;
        }
        String name = new File(postedPath).getName();
        if (!name.startsWith(PREFIX) || !name.endsWith(SUFFIX)) {
            return null;
        }
        String requestId = name.substring(PREFIX.length(), name.length() - SUFFIX.length());
        String time = requestId.startsWith(providerNo) ? requestId.substring(providerNo.length()) : "";
        // The exact length keeps provider 1 from matching provider 10's drawings
        if (time.length() != TIME_DIGITS || !StringUtils.isNumeric(time)) {
            return null;
        }
        // Rebuilt from the provider's number and the time read as a number, zeros kept, so the path holds no posted text
        String ownRequestId = providerNo + StringUtils.leftPad(Long.toString(Long.parseLong(time)), TIME_DIGITS, '0');
        File drawing = new File(DigitalSignatureUtils.getTempFilePath(ownRequestId));
        return drawing.isFile() ? drawing : null;
    }
}
