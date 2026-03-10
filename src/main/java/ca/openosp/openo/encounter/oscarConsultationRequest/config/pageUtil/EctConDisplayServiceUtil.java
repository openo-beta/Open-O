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


package ca.openosp.openo.encounter.oscarConsultationRequest.config.pageUtil;

import java.util.List;
import java.util.Vector;

import ca.openosp.openo.commn.dao.ConsultationServiceDao;
import ca.openosp.openo.commn.model.ConsultationServices;
import ca.openosp.openo.utility.SpringUtils;

public class EctConDisplayServiceUtil {
    private ConsultationServiceDao consultationServiceDao = (ConsultationServiceDao) SpringUtils.getBean(ConsultationServiceDao.class);

    public Vector<String> serviceName;
    public Vector<String> serviceId;

    public String getServiceDesc(String serId) {
        String retval = new String();

        ConsultationServices cs = consultationServiceDao.find(Integer.parseInt(serId));
        if (cs != null) {
            retval = cs.getServiceDesc();
        }

        return retval;
    }

    public void estServicesVectors() {
        serviceId = new Vector<String>();
        serviceName = new Vector<String>();

        List<ConsultationServices> services = consultationServiceDao.findActive();
        for (ConsultationServices service : services) {
            serviceId.add(String.valueOf(service.getServiceId()));
            serviceName.add(service.getServiceDesc());
        }

    }
}
