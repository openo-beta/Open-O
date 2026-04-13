/**
 * Copyright (c) 2021 WELL EMR Group Inc. This software is made available under the terms of the GNU
 * General Public License, Version 2, 1991 (GPLv2). License details are available via
 * "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.utility;

import lombok.NonNull;

public class PathUtils {
  public static String addTrailingSlash(@NonNull final String url) {
    return !url.endsWith("/") ? url + "/" : url;
  }

  public static String addLeadingSlash(@NonNull final String url) {
    return !url.startsWith("/") ? "/" + url : url;
  }

  public static String surroundWithSlashes(@NonNull final String url) {
    return addLeadingSlash(addTrailingSlash(url));
  }
}
