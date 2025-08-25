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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.oscarehr.common.dao.MeasurementDao;
import org.oscarehr.common.dao.MeasurementGroupStyleDao;
import org.oscarehr.common.dao.MeasurementMapDao;
import org.oscarehr.common.dao.PropertyDao;
import org.oscarehr.common.model.ConsentType;
import org.oscarehr.common.model.Measurement;
import org.oscarehr.common.model.MeasurementGroupStyle;
import org.oscarehr.common.model.MeasurementMap;
import org.oscarehr.common.model.Property;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oscar.OscarProperties;
import oscar.log.LogAction;
import oscar.oscarEncounter.oscarMeasurements.MeasurementFlowSheet;

public interface MeasurementManager {

    List<Measurement> getCreatedAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateExclusive,
                                          int itemsToReturn);

    Measurement getMeasurement(LoggedInInfo loggedInInfo, Integer id);

    List<Measurement> getMeasurementByType(LoggedInInfo loggedInInfo, Integer id, List<String> types);

    List<Measurement> getMeasurementByDemographicIdAfter(LoggedInInfo loggedInInfo, Integer demographicId,
                                                         Date updateAfter);

    List<MeasurementMap> getMeasurementMaps();

    Measurement addMeasurement(LoggedInInfo loggedInInfo, Measurement measurement);

    List<Measurement> getMeasurementsByProgramProviderDemographicDate(LoggedInInfo loggedInInfo,
                                                                      Integer programId, String providerNo, Integer demographicId, Calendar updatedAfterThisDateExclusive,
                                                                      int itemsToReturn);

    String getDShtml(String groupName);

    boolean isProperty(String prop);

    String findGroupId(String groupName);

    void addMeasurementGroupDS(String groupName, String dsHTML);

    void removeMeasurementGroupDS(String propKey);

    List<Measurement> getLatestMeasurementsByDemographicIdObservedAfter(LoggedInInfo loggedInInfo, Integer demographicId, Date observedDate);

    static List<String> getFlowsheetDsHTML() {
        List<String> dsHtml = new ArrayList<String>();
        String path_set_by_property = OscarProperties.getInstance().getProperty("MEASUREMENT_DS_HTML_DIRECTORY");

        if (path_set_by_property != null) {
            File[] files1 = new File(path_set_by_property).listFiles();

            for (File file1 : files1) {
                if (file1.isFile()) {
                    dsHtml.add(file1.getName());
                }
            }
        }

        URL path_of_resource = MeasurementFlowSheet.class.getClassLoader()
                .getResource("/oscar/oscarEncounter/oscarMeasurements/flowsheets/html/");
        File[] files2 = new File(path_of_resource.getPath()).listFiles();

        for (File file2 : files2) {
            if (file2.isFile()) {
                dsHtml.add(file2.getName());
            }
        }

        return dsHtml;
    }

    static String getPropertyValue(String prop) {
        PropertyDao propertyDao = (PropertyDao) SpringUtils.getBean(PropertyDao.class);
        Property p = propertyDao.checkByName(prop);
        String value = p.getValue();

        return value;
    }
}
