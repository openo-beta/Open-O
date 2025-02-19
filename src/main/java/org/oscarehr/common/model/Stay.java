//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package org.oscarehr.common.model;

import java.util.Date;
import java.time.Instant;
import java.time.Duration;

import org.apache.logging.log4j.Logger;
import org.oscarehr.util.MiscUtils;

public class Stay {

    private static final Logger logger = MiscUtils.getLogger();

    private Instant admissionInstant;
    private Instant dischargeInstant;
    private Duration stayDuration;

    public Stay(Date admission, Date discharge, Date start, Date end) {
        this.admissionInstant = (admission != null && admission.after(start)) ? admission.toInstant() : start.toInstant();
        this.dischargeInstant = (discharge != null) ? discharge.toInstant() : end.toInstant();

        try {
            if (admissionInstant.isAfter(dischargeInstant)) {
                throw new IllegalArgumentException("Admission date cannot be after discharge date.");
            }
            this.stayDuration = Duration.between(admissionInstant, dischargeInstant);
        } catch (IllegalArgumentException e) {
            logger.error("admission: " + admission + " discharge: " + discharge, e);
            logger.error("admission instant: " + admissionInstant + " discharge instant: " + dischargeInstant);
            throw e;
        }
    }

    public Duration getStayDuration() {
        return stayDuration;
    }

    public Instant getAdmissionInstant() {
        return admissionInstant;
    }

    public Instant getDischargeInstant() {
        return dischargeInstant;
    }

}
