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
package ca.openosp.openo.integration.fhir.dstu3.builder;

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.dao.ClinicDAO;
import ca.openosp.openo.integration.fhir.dstu3.model.Sender;
import ca.openosp.openo.integration.fhir.dstu3.resources.Settings;
import ca.openosp.openo.utility.SpringUtils;

public final class SenderFactory {

  private static final String buildName = OscarProperties.getInstance()
      .getProperty("buildtag", "UNKNOWN");
  private static final String senderEndpoint = OscarProperties.getInstance()
      .getProperty("ws_endpoint_url_base", "UNKNOWN");
  private static final ClinicDAO clinicDao = SpringUtils.getBean(ClinicDAO.class);

  public static Sender getSender() {
    return init(null);
  }

  public static Sender getSender(Settings settings) {
    return init(settings);
  }

  private static Sender init(Settings settings) {
    Sender sender = null;
    String vendorName = "Oscar EMR";
    String softwareName = "Oscar";
    if (settings != null && !settings.isIncludeSenderEndpoint()) {
      sender = new Sender(vendorName, softwareName, buildName);
    } else {
      sender = new Sender(vendorName, softwareName, buildName, senderEndpoint);
    }
    if (clinicDao != null) {
      org.oscarehr.common.model.Clinic clinic = clinicDao.getClinic();
      sender.setClinic(clinic);
    }
    return sender;
  }
}
