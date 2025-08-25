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

import java.io.UnsupportedEncodingException;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.persistence.Query;

import org.apache.commons.codec.binary.Base64;
import org.oscarehr.common.NativeSql;
import org.oscarehr.common.model.Hl7TextInfo;
import org.oscarehr.common.model.Hl7TextMessageInfo;
import org.oscarehr.common.model.Hl7TextMessageInfo2;
import org.oscarehr.common.model.SystemPreferences;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;
import org.springframework.stereotype.Repository;
import oscar.OscarProperties;

public interface Hl7TextInfoDao extends AbstractDao<Hl7TextInfo> {

    List<Hl7TextInfo> findAll();

    Hl7TextInfo findLabId(int labId);

    List<Hl7TextInfo> findByHealthCardNo(String hin);

    List<Hl7TextInfo> searchByAccessionNumber(String acc);

    List<Hl7TextInfo> searchByAccessionNumber(String acc1, String acc2);

    List<Hl7TextInfo> searchByAccessionNumberOrderByObrDate(String accessionNumber);

    Hl7TextInfo findLatestVersionByAccessionNumberOrFillerNumber(
        String acc, String fillerNumber);

    List<Hl7TextInfo> searchByFillerOrderNumber(String fon, String sending_facility);

    void updateReportStatusByLabId(String reportStatus, int labNumber);

    List<Hl7TextMessageInfo> getMatchingLabs(String hl7msg);

    List<Hl7TextMessageInfo2> getMatchingLabsByAccessionNo(String accession);

    List<Hl7TextInfo> getAllLabsByLabNumberResultStatus();

    void updateResultStatusByLabId(String resultStatus, int labNumber);

    void createUpdateLabelByLabNumber(String label, int lab_no);

    List<Hl7TextInfo> findByLabId(Integer labNo);

    List<Object[]> findByLabIdViaMagic(Integer labNo);

    List<Object[]> findByDemographicId(Integer demographicNo);

    List<Hl7TextInfo> findByLabIdList(List<Integer> labIds);

    List<Object[]> findLabsViaMagic(String status, String providerNo, String patientFirstName,
                                    String patientLastName, String patientHealthNumber);

    List<Object[]> findLabAndDocsViaMagic(String providerNo, String demographicNo, String patientFirstName,
                                          String patientLastName, String patientHealthNumber, String status, boolean isPaged, Integer page,
                                          Integer pageSize, boolean mixLabsAndDocs, Boolean isAbnormal, boolean searchProvider,
                                          boolean patientSearch);

    List<Object[]> findLabAndDocsViaMagic(String providerNo, String demographicNo, String patientFirstName,
                                          String patientLastName, String patientHealthNumber, String status, boolean isPaged, Integer page,
                                          Integer pageSize, boolean mixLabsAndDocs, Boolean isAbnormal, boolean searchProvider, boolean patientSearch,
                                          Date startDate, Date endDate);

    List<Object> findDisciplines(Integer labid);

    List<Hl7TextInfo> findByFillerOrderNumber(String fillerOrderNum);
}
