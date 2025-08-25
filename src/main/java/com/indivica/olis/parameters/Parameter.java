//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package com.indivica.olis.parameters;

public interface Parameter {

    String toOlisString();

    void setValue(Object value);

    void setValue(Integer part, Object value);

    void setValue(Integer part, Integer part2, Object value);

    String getQueryCode();
}
