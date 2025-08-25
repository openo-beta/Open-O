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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.oscarehr.PMmodule.dao.ProgramDao;
import org.oscarehr.PMmodule.model.AdmissionSearchBean;
import org.oscarehr.common.model.Admission;
import org.oscarehr.util.MiscUtils;
import org.springframework.stereotype.Repository;

public interface AdmissionDao extends AbstractDao<Admission> {

    List<Admission> getAdmissions_archiveView(Integer programId, Integer demographicNo);

    Admission getAdmission(Integer programId, Integer demographicNo);

    Admission getCurrentAdmission(Integer programId, Integer demographicNo);

    List<Admission> getAdmissions();

    List<Admission> getAdmissions(Integer demographicNo);

    List<Admission> getAdmissionsASC(Integer demographicNo);

    List<Admission> getAdmissionsByFacility(Integer demographicNo, Integer facilityId);


    List<Admission> getAdmissionsByProgramAndClient(Integer demographicNo, Integer programId);

    List<Admission> getAdmissionsByProgramId(Integer programId, Boolean automaticDischarge, Integer days);

    List<Integer> getAdmittedDemographicIdByProgramAndProvider(Integer programId, String providerNo);

    List<Admission> getCurrentAdmissions(Integer demographicNo);

    List<Admission> getDischargedAdmissions(Integer demographicNo);

    List<Admission> getCurrentAdmissionsByFacility(Integer demographicNo, Integer facilityId);

    Admission getCurrentExternalProgramAdmission(ProgramDao programDAO, Integer demographicNo);


    List<Admission> getCurrentServiceProgramAdmission(ProgramDao programDAO, Integer demographicNo);

    Admission getCurrentCommunityProgramAdmission(ProgramDao programDAO, Integer demographicNo);

    List<Admission> getCurrentAdmissionsByProgramId(Integer programId);

    Admission getAdmission(int id);

    Admission getAdmission(Long id);

    void saveAdmission(Admission admission);

    List<Admission> getAdmissionsInTeam(Integer programId, Integer teamId);

    Admission getTemporaryAdmission(Integer demographicNo);

    List search(AdmissionSearchBean searchBean);

    List<Admission> getClientIdByProgramDate(int programId, Date dt);

    Integer getLastClientStatusFromAdmissionByProgramIdAndClientId(Integer programId, Integer demographicId);

    List<Admission> getAdmissionsByProgramAndAdmittedDate(int programId, Date startDate, Date endDate);

    List<Admission> getAdmissionsByProgramAndDate(int programId, Date startDate, Date endDate);

    boolean wasInProgram(Integer programId, Integer clientId);

    List getActiveAnonymousAdmissions();

    List<Admission> getAdmissionsByFacilitySince(Integer demographicNo, Integer facilityId, Date lastUpdateDate);

    List<Integer> getAdmissionsByFacilitySince(Integer facilityId, Date lastUpdateDate);

    List<Admission> findAdmissionsByProgramAndDate(Integer programNo, Date day, int startIndex, int numToReturn);

    Integer findAdmissionsByProgramAndDateAsCount(Integer programNo, Date day);
}
