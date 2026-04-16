//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.webserv.rest.conversion;

import ca.openosp.openo.commn.model.DemographicMerge;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.webserv.rest.to.model.DemographicMergedTo1;

/**
 * Converts between {@link DemographicMerge} domain objects and {@link DemographicMergedTo1} transfer objects.
 *
 * @since 2026-04-13
 */
public class DemographicMergedConverter extends AbstractConverter<DemographicMerge, DemographicMergedTo1> {

    @Override
    public DemographicMerge getAsDomainObject(LoggedInInfo loggedInInfo, DemographicMergedTo1 t) throws ConversionException {
        DemographicMerge d = new DemographicMerge();

        d.setId(t.getId());
        d.setPrimaryDemographicNo(t.getPrimaryDemographicNo());
        d.setSecondaryDemographicNo(t.getSecondaryDemographicNo());
        d.setMergedDemographicNo(t.getMergedDemographicNo());
        d.setProviderNo(t.getProviderNo());
        d.setEventDate(t.getEventDate());
        if (t.getEventType() != null) {
            try {
                d.setEventType(DemographicMerge.EventType.valueOf(t.getEventType()));
            } catch (IllegalArgumentException e) {
                throw new ConversionException("Unknown eventType value: " + t.getEventType());
            }
        }
        return d;
    }

    @Override
    public DemographicMergedTo1 getAsTransferObject(LoggedInInfo loggedInInfo, DemographicMerge d) throws ConversionException {
        DemographicMergedTo1 t = new DemographicMergedTo1();

        t.setId(d.getId());
        t.setPrimaryDemographicNo(d.getPrimaryDemographicNo());
        t.setSecondaryDemographicNo(d.getSecondaryDemographicNo());
        t.setMergedDemographicNo(d.getMergedDemographicNo());
        t.setProviderNo(d.getProviderNo());
        t.setEventDate(d.getEventDate());
        if (d.getEventType() != null) {
            t.setEventType(d.getEventType().name());
        }
        return t;
    }

}
