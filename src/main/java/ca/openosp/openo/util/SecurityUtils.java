/**
 * Copyright (c) 2023 WELL EMR Group Inc. This software is made available under the terms of the GNU
 * General Public License, Version 2, 1991 (GPLv2). License details are available via
 * "gnu.org/licenses/gpl-2.0.html".
 */
package ca.kai.util;

import javax.servlet.http.Cookie;

public class SecurityUtils {

  /**
   * Create a kai-sso cookie with the given jws. The cookie expiry is set to 72 hours.
   * @param jws the jws to be stored in the cookie
   * @return A cookie with the given jws that expires after 72 hours
   */
  public static Cookie createKaiSsoCookie(final String jws) {
    Cookie cookie = new Cookie("kai-sso", jws);
    cookie.setMaxAge(259200);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    return cookie;
  }

}
