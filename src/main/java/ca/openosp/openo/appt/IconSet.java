package ca.openosp.openo.appt;

import java.util.List;
import java.util.stream.Stream;

/**
 * The Icon Set: the one collection of icons an administrator picks from, for appointment statuses
 * and Location List items alike. The status images come first, then the glyph icons.
 *
 * <p>An icon is stored by name. A glyph icon's name is its Glyphicons Halflings class, such as
 * {@code glyphicon-home}; any other name is an image file under {@code /images}. Pages decide how to
 * draw a stored icon by that rule, so one outside the set still draws.</p>
 *
 * @since 2026-10-07
 */
public final class IconSet {

    /**
     * The status images, under /images. The schedule shows a signed or verified appointment with the
     * image's S- or V-prefixed copy, so only images that have both are listed.
     */
    public static final List<String> IMAGES = List.of(
            "starbill.gif", "todo.gif", "here.gif", "picked.gif", "empty.gif", "noshow.gif", "cancel.gif", "billed.gif",
            "thumb.png",
            "1.gif", "2.gif", "3.gif", "4.gif", "5.gif", "6.gif", "7.gif", "8.gif",
            "9.gif", "10.gif", "11.gif", "12.gif", "13.gif", "14.gif", "15.gif", "16.gif");

    /** Every Glyphicons Halflings class in css/glyphicons-standalone.css, in stylesheet order. */
    public static final List<String> GLYPHS = List.of(
            "glyphicon-asterisk", "glyphicon-plus", "glyphicon-euro", "glyphicon-minus", "glyphicon-cloud",
            "glyphicon-envelope", "glyphicon-pencil", "glyphicon-glass", "glyphicon-music", "glyphicon-search",
            "glyphicon-heart", "glyphicon-star", "glyphicon-star-empty", "glyphicon-user", "glyphicon-film",
            "glyphicon-th-large", "glyphicon-th", "glyphicon-th-list", "glyphicon-ok", "glyphicon-remove",
            "glyphicon-zoom-in", "glyphicon-zoom-out", "glyphicon-off", "glyphicon-signal", "glyphicon-cog",
            "glyphicon-trash", "glyphicon-home", "glyphicon-file", "glyphicon-time", "glyphicon-road",
            "glyphicon-download-alt", "glyphicon-download", "glyphicon-upload", "glyphicon-inbox",
            "glyphicon-play-circle", "glyphicon-repeat", "glyphicon-refresh", "glyphicon-list-alt", "glyphicon-flag",
            "glyphicon-headphones", "glyphicon-volume-off", "glyphicon-volume-down", "glyphicon-volume-up",
            "glyphicon-qrcode", "glyphicon-barcode", "glyphicon-tag", "glyphicon-tags", "glyphicon-book",
            "glyphicon-print", "glyphicon-font", "glyphicon-bold", "glyphicon-italic", "glyphicon-text-height",
            "glyphicon-text-width", "glyphicon-align-left", "glyphicon-align-center", "glyphicon-align-right",
            "glyphicon-align-justify", "glyphicon-list", "glyphicon-indent-left", "glyphicon-indent-right",
            "glyphicon-facetime-video", "glyphicon-picture", "glyphicon-map-marker", "glyphicon-adjust",
            "glyphicon-tint", "glyphicon-edit", "glyphicon-share", "glyphicon-check", "glyphicon-move",
            "glyphicon-step-backward", "glyphicon-fast-backward", "glyphicon-backward", "glyphicon-play",
            "glyphicon-pause", "glyphicon-stop", "glyphicon-forward", "glyphicon-fast-forward",
            "glyphicon-step-forward", "glyphicon-eject", "glyphicon-chevron-left", "glyphicon-chevron-right",
            "glyphicon-plus-sign", "glyphicon-minus-sign", "glyphicon-remove-sign", "glyphicon-ok-sign",
            "glyphicon-question-sign", "glyphicon-info-sign", "glyphicon-screenshot", "glyphicon-remove-circle",
            "glyphicon-ok-circle", "glyphicon-ban-circle", "glyphicon-arrow-left", "glyphicon-arrow-right",
            "glyphicon-arrow-up", "glyphicon-arrow-down", "glyphicon-share-alt", "glyphicon-resize-full",
            "glyphicon-resize-small", "glyphicon-exclamation-sign", "glyphicon-gift", "glyphicon-leaf",
            "glyphicon-eye-open", "glyphicon-eye-close", "glyphicon-warning-sign", "glyphicon-plane",
            "glyphicon-random", "glyphicon-comment", "glyphicon-magnet", "glyphicon-chevron-up",
            "glyphicon-chevron-down", "glyphicon-retweet", "glyphicon-shopping-cart", "glyphicon-folder-close",
            "glyphicon-folder-open", "glyphicon-resize-vertical", "glyphicon-resize-horizontal", "glyphicon-hdd",
            "glyphicon-bullhorn", "glyphicon-certificate", "glyphicon-thumbs-up", "glyphicon-thumbs-down",
            "glyphicon-hand-right", "glyphicon-hand-left", "glyphicon-hand-up", "glyphicon-hand-down",
            "glyphicon-circle-arrow-right", "glyphicon-circle-arrow-left", "glyphicon-circle-arrow-up",
            "glyphicon-circle-arrow-down", "glyphicon-globe", "glyphicon-tasks", "glyphicon-filter",
            "glyphicon-fullscreen", "glyphicon-dashboard", "glyphicon-heart-empty", "glyphicon-link", "glyphicon-phone",
            "glyphicon-usd", "glyphicon-gbp", "glyphicon-sort", "glyphicon-sort-by-alphabet",
            "glyphicon-sort-by-alphabet-alt", "glyphicon-sort-by-order", "glyphicon-sort-by-order-alt",
            "glyphicon-sort-by-attributes", "glyphicon-sort-by-attributes-alt", "glyphicon-unchecked",
            "glyphicon-expand", "glyphicon-collapse-down", "glyphicon-collapse-up", "glyphicon-log-in",
            "glyphicon-flash", "glyphicon-log-out", "glyphicon-new-window", "glyphicon-record", "glyphicon-save",
            "glyphicon-open", "glyphicon-saved", "glyphicon-import", "glyphicon-export", "glyphicon-send",
            "glyphicon-floppy-disk", "glyphicon-floppy-saved", "glyphicon-floppy-remove", "glyphicon-floppy-save",
            "glyphicon-floppy-open", "glyphicon-credit-card", "glyphicon-transfer", "glyphicon-cutlery",
            "glyphicon-header", "glyphicon-compressed", "glyphicon-earphone", "glyphicon-phone-alt", "glyphicon-tower",
            "glyphicon-stats", "glyphicon-sd-video", "glyphicon-hd-video", "glyphicon-subtitles",
            "glyphicon-sound-stereo", "glyphicon-sound-dolby", "glyphicon-sound-5-1", "glyphicon-sound-6-1",
            "glyphicon-sound-7-1", "glyphicon-copyright-mark", "glyphicon-registration-mark",
            "glyphicon-cloud-download", "glyphicon-cloud-upload", "glyphicon-tree-conifer", "glyphicon-tree-deciduous",
            "glyphicon-briefcase", "glyphicon-calendar", "glyphicon-pushpin", "glyphicon-paperclip", "glyphicon-camera",
            "glyphicon-lock", "glyphicon-bell", "glyphicon-bookmark", "glyphicon-fire", "glyphicon-wrench");

    /** The whole set, in the order the pickers offer it: the images, then the glyphs. */
    public static final List<String> ICONS = Stream.concat(IMAGES.stream(), GLYPHS.stream()).toList();

    private static final String GLYPH_PREFIX = "glyphicon-";

    private IconSet() {
    }

    /**
     * Whether an icon may be saved: it is in the set.
     *
     * @param icon String the icon name, or null
     * @return boolean true when the icon is in the set
     */
    public static boolean contains(String icon) {
        return icon != null && ICONS.contains(icon);
    }

    /**
     * Whether an icon is a glyph, drawn with its class, rather than an image file.
     *
     * @param icon String the icon name, or null
     * @return boolean true when the name is a Glyphicons Halflings class
     */
    public static boolean isGlyph(String icon) {
        return icon != null && icon.startsWith(GLYPH_PREFIX);
    }
}
