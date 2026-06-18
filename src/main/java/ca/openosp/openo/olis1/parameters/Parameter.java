//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1.parameters;

public interface Parameter {

    public String toOlisString();

    public void setValue(Object value);

    public void setValue(Integer part, Object value);

    public void setValue(Integer part, Integer part2, Object value);

    public String getQueryCode();

    /**
     * Escapes the HL7 v2 delimiter characters in a free-text parameter value so a
     * value containing {@code ~ ^ \ &} cannot corrupt the {@code @CODE^value~...}
     * OLIS query-segment structure. The escape character is substituted first so the
     * escape sequences introduced for the other delimiters are not re-escaped.
     *
     * @param value String the raw parameter value; may be null
     * @return String the delimiter-escaped value, or an empty string when {@code value} is null
     * @since 2026-06-18
     */
    static String escapeHl7(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\E\\")
                .replace("~", "\\R\\")
                .replace("^", "\\S\\")
                .replace("&", "\\T\\");
    }
}
