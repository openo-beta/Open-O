/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.utility;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

public class PKCEUtils {

  public static String encodeBase64NoPadding(String data) {
    return encodeBase64NoPadding(data.getBytes());
  }

  public static String encodeBase64NoPadding(byte[] data) {
    String b = Base64.encodeBase64String(data);
    if (b.contains("=")) {
      b = b.substring(0, b.indexOf("="));
    }
    b = b.replaceAll("\\+", "-").replaceAll("/", "_");
    return b;
  }

  public static String generateChallengeS256(String verifier)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(bytes, 0, bytes.length);
    byte[] digest = md.digest();

    return encodeBase64NoPadding(digest);
  }

  public static Date getDateInFuture(int minutes) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, minutes);
    return cal.getTime();
  }
}
