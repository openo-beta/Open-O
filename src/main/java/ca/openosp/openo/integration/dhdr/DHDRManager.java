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
package ca.openosp.openo.integration.dhdr;

import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DHDRManager extends OmdGateway {

  private static final Logger logger = MiscUtils.getLogger();
  private final SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(
      SystemPreferencesDao.class);

  public String search2(LoggedInInfo loggedInInfo, Demographic demographic, Date startDate,
                        Date endDate, String searchId, Integer pageId) throws Exception {

    String dhdrEndpoint = systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.dhdr_endpoint).getName();
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    WebClient wc = getWebClient(loggedInInfo, dhdrEndpoint);

    wc.query("patient.identifier",
        "https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-patient-hcn|"
            + demographic.getHin());//"5365837912");

    wc.query("patient.birthdate", demographic.getBirthDayAsString());

    if ("M".equalsIgnoreCase(demographic.getSex())) {
      wc.query("patient.gender", "male");
    } else if ("F".equalsIgnoreCase(demographic.getSex())) {
      wc.query("patient.gender", "female");
    } else if ("O".equalsIgnoreCase(demographic.getSex())) {
      wc.query("patient.gender", "other");
    } else if ("U".equalsIgnoreCase(demographic.getSex())) {
      wc.query("patient.gender", "unknown");
    }

    wc.query("_count", "1000");

    if (endDate == null) {
      wc.query("whenprepared", "lt" + fmt.format(endDate));
    } else {
      wc.query("whenprepared", "lt" + fmt.format(new Date()));
    }
    if (startDate != null) {
      wc.query("whenprepared", "gt" + fmt.format(startDate));
    }
    wc.query("_format", "application/fhir+json");

    if (searchId != null) {
      wc.query("search-id", searchId);
    }

    if (pageId != null) {
      wc.query("page", pageId);
    }

    AuditInfo auditInfo = new AuditInfo(AuditInfo.DHDR, AuditInfo.SEARCH,
        demographic.getDemographicNo());
    Response response2 = doGet(loggedInInfo, wc, auditInfo);
    String body = response2.readEntity(String.class);

    logger.debug("body:" + body);

    return body;
  }
}
