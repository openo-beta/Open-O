/**
 * Copyright (c) 2021 WELL EMR Group Inc. This software is made available under the terms of the GNU
 * General Public License, Version 2, 1991 (GPLv2). License details are available via
 * "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.utility;

import java.util.Objects;

public class PathUtils {

    public static String addTrailingSlash(final String url) {
        Objects.requireNonNull(url, "url must not be null");
        return !url.endsWith("/") ? url + "/" : url;
    }

    public static String addLeadingSlash(final String url) {
        Objects.requireNonNull(url, "url must not be null");
        return !url.startsWith("/") ? "/" + url : url;
    }

    public static String surroundWithSlashes(final String url) {
        Objects.requireNonNull(url, "url must not be null");
        return addLeadingSlash(addTrailingSlash(url));
    }
}
