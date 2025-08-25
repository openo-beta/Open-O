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

package org.oscarehr.common.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscarehr.common.dao.MyGroupDao;
import org.oscarehr.common.model.MyGroup;
import org.oscarehr.util.SpringUtils;

/**
 * @author mweston4
 */
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class GroupPreference2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();


    public String execute() {
        if ("save".equals(request.getParameter("method"))) {
            return save();
        }
        return setDefaultBillingForm();
    }

    public String save() {

        MyGroupDao myGroupDao = (MyGroupDao) SpringUtils.getBean(MyGroupDao.class);

        String billingForm = request.getParameter("chosenForm");

        if (billingForm != null && !billingForm.isEmpty()) {

            String[] selectedGroups = request.getParameterValues("data");

            if (selectedGroups != null) {
                for (int i = 0; i < selectedGroups.length; i++) {

                    List<MyGroup> myGroups = myGroupDao.getGroupByGroupNo(selectedGroups[i]);
                    for (MyGroup myGroup : myGroups) {

                        if (selectedGroups[i].isEmpty())
                            myGroup.setDefaultBillingForm(null);
                        else
                            myGroup.setDefaultBillingForm(billingForm);

                        myGroupDao.merge(myGroup);
                    }
                }
            }
        }
        request.setAttribute("saved", true);
        return "saved";
    }

    public String setDefaultBillingForm() {

        return "changedForm";
    }

}
