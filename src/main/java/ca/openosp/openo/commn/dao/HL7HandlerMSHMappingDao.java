//CHECKSTYLE:OFF
/**

 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 * <p>
 * Modifications made by Magenta Health in collaboration with Open OSP in 2024.
 */

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.HL7HandlerMSHMapping;

public interface HL7HandlerMSHMappingDao extends AbstractDao<HL7HandlerMSHMapping> {
    HL7HandlerMSHMapping findByFacility(String facility);
}
