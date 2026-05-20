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
package ca.openosp.openo.commn;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.tools.ant.util.DateUtils;
import org.hibernate.transform.ResultTransformer;
import ca.openosp.openo.webserv.rest.to.model.DemographicSearchResult;

public class DemographicSearchResultTransformer implements ResultTransformer {

    private SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.ISO8601_DATE_PATTERN);

    public DemographicSearchResultTransformer() {
    }


    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        Integer demographicNo = (Integer) tuple[0];
        String lastName = (String) tuple[1];
        String firstName = (String) tuple[2];
        String chartNo = (String) tuple[3];
        String sex = String.valueOf(tuple[4]);
        String providerNo = (String) tuple[5];
        String rosterStatus = (String) tuple[6];
        String patientStatus = (String) tuple[7];
        String phone = (String) tuple[8];
        String year = (String) tuple[9];
        String month = "" + tuple[10];
        String day = "" + tuple[11];
        String providerLastName = (String) tuple[12];
        String providerFirstName = (String) tuple[13];
        String hin = (String) tuple[14];

        Date dob = null;
        try {
            dob = sdf.parse(year + "-" + month + "-" + day);
        } catch (ParseException e) {
            //logger.warn("Demographic " + demographicNo + " has a bad DOB ",e);
        }


        DemographicSearchResult result =
                new DemographicSearchResult(demographicNo, lastName, firstName, chartNo, sex, providerNo, rosterStatus,
                        patientStatus, phone, dob, providerLastName, providerFirstName, hin);
        return result;
    }

    @Override
    public List transformList(List collection) {

        return collection;
    }

}
