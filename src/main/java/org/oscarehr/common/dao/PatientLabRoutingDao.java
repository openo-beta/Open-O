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

package org.oscarehr.common.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringEscapeUtils;
import org.oscarehr.common.model.LabPatientPhysicianInfo;
import org.oscarehr.common.model.LabTestResults;
import org.oscarehr.common.model.MdsMSH;
import org.oscarehr.common.model.MdsOBX;
import org.oscarehr.common.model.MdsZRG;
import org.oscarehr.common.model.PatientLabRouting;
import org.springframework.stereotype.Repository;

public interface PatientLabRoutingDao extends AbstractDao<PatientLabRouting> {

    Integer UNMATCHED = 0;
    String HL7 = "HL7";

    PatientLabRouting findDemographicByLabId(Integer labId);

    PatientLabRouting findDemographics(String labType, Integer labNo);

    List<PatientLabRouting> findDocByDemographic(Integer docNum);

    PatientLabRouting findByLabNo(int labNo);

    List<PatientLabRouting> findByLabNoAndLabType(int labNo, String labType);

    List<Object[]> findUniqueTestNames(Integer demoId, String labType);

    List<Object[]> findTests(Integer demoId, String labType);

    List<Object[]> findUniqueTestNamesForPatientExcelleris(Integer demoNo, String labType);

    List<PatientLabRouting> findByDemographicAndLabType(Integer demoNo, String labType);

    List<Object[]> findRoutingsAndTests(Integer demoNo, String labType, String testName);

    List<Object[]> findRoutingsAndTests(Integer demoNo, String labType);

    List<Object[]> findMdsRoutings(Integer demoNo, String testName, String labType);

    List<Object[]> findHl7InfoForRoutingsAndTests(Integer demoNo, String labType, String testName);

    List<Object[]> findRoutingsAndConsultDocsByRequestId(Integer reqId, String docType);

    List<Object[]> findResultsByDemographicAndLabType(Integer demographicNo, String labType);

    List<Object[]> findRoutingAndPhysicianInfoByTypeAndDemoNo(String labType, Integer demographicNo);

    List<Object[]> findRoutingsAndMdsMshByDemoNo(Integer demographicNo);

    List<PatientLabRouting> findLabNosByDemographic(Integer demographicNo, String[] labTypes);

    List<Integer> findDemographicIdsSince(Date date);

}
