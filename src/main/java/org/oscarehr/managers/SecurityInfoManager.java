//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */
package org.oscarehr.managers;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.oscarehr.common.exception.PatientDirectiveException;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oscar.util.OscarRoleObjectPrivilege;

import com.quatro.dao.security.SecobjprivilegeDao;
import com.quatro.dao.security.SecuserroleDao;
import com.quatro.model.security.Secobjprivilege;
import com.quatro.model.security.Secuserrole;

import javax.servlet.http.HttpSession;

public interface SecurityInfoManager {
    String READ = "r";
    String WRITE = "w";
    String UPDATE = "u";
    String DELETE = "d";
    String NORIGHTS = "o";

    List<Secuserrole> getRoles(LoggedInInfo loggedInInfo);

    List<Secobjprivilege> getSecurityObjects(LoggedInInfo loggedInInfo);

    boolean hasPrivilege(LoggedInInfo loggedInInfo, String objectName, String privilege, int demographicNo);

    boolean hasPrivilege(LoggedInInfo loggedInInfo, String objectName, String privilege, String demographicNo);

    boolean isAllowedAccessToPatientRecord(LoggedInInfo loggedInInfo, Integer demographicNo);
}
