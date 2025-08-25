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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.oscarehr.common.model.ConsultDocs;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Document;
import org.oscarehr.common.model.EFormDocs;
import org.springframework.stereotype.Repository;

import org.oscarehr.documentManager.EDocUtil.EDocSort;
import oscar.util.ConversionUtils;

public interface DocumentDao extends AbstractDao<Document> {

    enum Module {
        DEMOGRAPHIC;

        public String getName() {
            return this.name().toLowerCase();
        }
    }

    enum DocumentType {
        CONSULT, LAB, ECONSULT;

        public String getName() {
            return this.name().toLowerCase();
        }
    }

    List<Object[]> getCtlDocsAndDocsByDemoId(Integer demoId, Module moduleName, DocumentType docType);

    List<Document> findActiveByDocumentNo(Integer demoId);

    List<Object[]> findCtlDocsAndDocsByModuleDocTypeAndModuleId(Module module, DocumentType docType,
                                                                Integer moduleId);

    List<Object[]> findCtlDocsAndDocsByModuleAndModuleId(Module module, Integer moduleId);

    List<Object[]> findDocsAndConsultDocsByConsultId(Integer consultationId);

    List<Object[]> findDocsAndEFormDocsByFdid(Integer fdid);

    List<Object[]> findDocsAndConsultResponseDocsByConsultId(Integer consultationId);

    List<Object[]> findCtlDocsAndDocsByDocNo(Integer documentNo);

    List<Object[]> findCtlDocsAndDocsByModuleCreatorResponsibleAndDates(Module module, String providerNo,
                                                                        String responsible, Date from, Date to, boolean unmatchedDemographics);

    List<Object[]> findConstultDocsDocsAndProvidersByModule(Module module, Integer moduleId);

    Integer findMaxDocNo();

    Document getDocument(String documentNo);

    Demographic getDemoFromDocNo(String docNo);

    int getNumberOfDocumentsAttachedToAProviderDemographics(String providerNo, Date startDate, Date endDate);

    void subtractPages(String documentNo, Integer i);

    List<Document> findByDemographicId(String demoNo);

    List<Object[]> findDocuments(String module, String moduleid, String docType, boolean includePublic,
                                 boolean includeDeleted, boolean includeActive, EDocSort sort, Date since);

    List<Document> findByUpdateDate(Date updatedAfterThisDateExclusive, int itemsToReturn);

    List<Document> findByDemographicUpdateDate(Integer demographicId, Date updatedAfterThisDateInclusive);

    List<Document> findByDemographicUpdateAfterDate(Integer demographicId, Date updatedAfterThisDate);

    List<Document> findByProgramProviderDemographicUpdateDate(Integer programId, String providerNo,
                                                              Integer demographicId, Date updatedAfterThisDateExclusive, int itemsToReturn);

    List<Integer> findDemographicIdsSince(Date since);

    List<Document> findByDoctype(String docType);

    List<Document> findByDoctypeAndProviderNo(String docType, String provider_no, Integer isPublic);

    List<Document> findByDemographicAndDoctype(int demographicId, DocumentType documentType);

    Document findByDemographicAndFilename(int demographicId, String fileName);
}
