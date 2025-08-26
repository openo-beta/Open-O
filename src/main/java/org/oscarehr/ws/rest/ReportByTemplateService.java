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
package org.oscarehr.ws.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jettison.json.JSONObject;
import org.oscarehr.app.AppOAuth1Config;
import org.oscarehr.app.OAuth1Utils;
import org.oscarehr.common.dao.AppDefinitionDao;
import org.oscarehr.common.dao.AppUserDao;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.common.model.AppUser;
import org.oscarehr.managers.AppManager;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.ws.rest.to.GenericRESTResponse;
import org.springframework.beans.factory.annotation.Autowired;

import oscar.oscarReport.reportByTemplate.ReportManager;

@Path("/reportByTemplate")
public class ReportByTemplateService extends AbstractServiceImpl {
    @Autowired
    private SecurityInfoManager securityInfoManager;

    @Autowired
    private AppDefinitionDao appDefinitionDao;

    @Autowired
    AppManager appManager;

    @Autowired
    private AppUserDao appUserDao;

}
