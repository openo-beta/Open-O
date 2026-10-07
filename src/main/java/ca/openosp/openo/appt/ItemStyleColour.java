package ca.openosp.openo.appt;

import java.util.regex.Pattern;

/**
 * The colours appointment statuses and Location List items are drawn in.
 *
 * <p>A colour is {@code #rrggbb}, or {@code #rrggbbaa} when it is partly see-through. The schedule
 * paints a see-through status colour over white, so it shows paler; a location chip draws its
 * colour as it is, over whatever the appointment cell shows.</p>
 *
 * @since 2026-10-07
 */
public final class ItemStyleColour {

    /** A colour as the settings pages save it: {@code #rrggbb}, or {@code #rrggbbaa} when see-through. */
    public static final Pattern PATTERN = Pattern.compile("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?");

    /**
     * The schedule's appointment text colour, rgb(0,40,60): the icon colour on light or see-through
     * chips, and a glyph status icon's colour.
     */
    public static final String DARK = "#00283c";

    /** The icon colour on dark chips. */
    public static final String WHITE = "#ffffff";

    private static final Pattern SEE_THROUGH = Pattern.compile("#[0-9a-fA-F]{8}");

    /* Under this opacity, in whole percent, a chip mostly shows the appointment cell behind it. */
    private static final int HALF_OPAQUE_PERCENT = 50;

    /* WCAG 2.1's minimum contrast for icons and other graphics (success criterion 1.4.11). */
    private static final double ICON_MIN_CONTRAST = 3.0;

    private ItemStyleColour() {
    }

    /**
     * Returns the solid colour a see-through colour shows when painted over white, so a page can
     * keep drawing it with the legacy bgcolor attribute, which misreads {@code #rrggbbaa}.
     *
     * @param colour String the colour, or null
     * @return String the mixed {@code #rrggbb} for a {@code #rrggbbaa} colour; any other value,
     *     null included, unchanged
     */
    public static String overWhite(String colour) {
        if (colour == null || !SEE_THROUGH.matcher(colour).matches()) {
            return colour;
        }
        int alpha = alpha(colour);
        StringBuilder mixed = new StringBuilder("#");
        for (int start = 1; start < 7; start += 2) {
            int value = (int) Math.round((channel(colour, start) * alpha + 255 * (255 - alpha)) / 255.0);
            mixed.append(String.format("%02x", value));
        }
        return mixed.toString();
    }

    /**
     * Returns the colour to draw an icon in on a location chip of the given colour: white whenever it
     * meets WCAG's minimum contrast for icons, otherwise dark. White comes first because WCAG 2's
     * contrast ratio overrates dark on mid-tones, such as a medium blue, where white reads better.
     * A chip with no colour, or one under 50% opaque, mostly shows the appointment cell behind it,
     * so its icon is dark, like the appointment's text.
     *
     * @param colour String the chip's colour, or null for none
     * @return String {@link #DARK} or {@link #WHITE}
     */
    public static String iconColour(String colour) {
        if (colour == null || !PATTERN.matcher(colour).matches()) {
            return DARK;
        }
        // Rounded to whole percent, as the picker shows it, so its 50% (#..7f) counts as 50%.
        if (colour.length() == 9 && Math.round(alpha(colour) * 100 / 255.0) < HALF_OPAQUE_PERCENT) {
            return DARK;
        }
        return contrast(luminance(colour), luminance(WHITE)) >= ICON_MIN_CONTRAST ? WHITE : DARK;
    }

    private static int alpha(String colour) {
        return channel(colour, 7);
    }

    /* Only ever given a colour PATTERN or SEE_THROUGH has matched, so these are always two hex digits. */
    private static int channel(String colour, int start) {
        return Integer.parseInt(colour.substring(start, start + 2), 16);
    }

    /* WCAG 2 contrast ratio between two relative luminances. */
    private static double contrast(double one, double other) {
        return (Math.max(one, other) + 0.05) / (Math.min(one, other) + 0.05);
    }

    /* WCAG 2 relative luminance of the colour's #rrggbb part. */
    private static double luminance(String colour) {
        return 0.2126 * linear(channel(colour, 1)) + 0.7152 * linear(channel(colour, 3)) + 0.0722 * linear(channel(colour, 5));
    }

    private static double linear(int channel) {
        double value = channel / 255.0;
        return value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }
}
