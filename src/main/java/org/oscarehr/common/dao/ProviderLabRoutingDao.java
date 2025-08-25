//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package org.oscarehr.common.dao;

import java.util.List;

import javax.persistence.Query;

import org.oscarehr.common.NativeSql;
import org.oscarehr.common.model.Provider;
import org.oscarehr.common.model.ProviderLabRoutingModel;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

public interface ProviderLabRoutingDao extends AbstractDao<ProviderLabRoutingModel> {
    String UNCLAIMED_PROVIDER = "0";

    enum LAB_TYPE {
        DOC, HL7
    }

    enum STATUS {
        X, N, A, D
    }

    List<ProviderLabRoutingModel> findByLabNoAndLabTypeAndProviderNo(int labNo, String labType,
                                                                     String providerNo);

    List<ProviderLabRoutingModel> getProviderLabRoutingDocuments(Integer labNo);

    List<ProviderLabRoutingModel> getProviderLabRoutingForLabProviderType(Integer labNo, String providerNo,
                                                                          String labType);

    List<ProviderLabRoutingModel> getProviderLabRoutingForLabAndType(Integer labNo, String labType);

    List<ProviderLabRoutingModel> findAllLabRoutingByIdandType(Integer labNo, String labType);

    void updateStatus(Integer labNo, String labType);

    ProviderLabRoutingModel findByLabNo(int labNo);

    List<ProviderLabRoutingModel> findByLabNoIncludingPotentialDuplicates(int labNo);

    ProviderLabRoutingModel findByLabNoAndLabType(int labNo, String labType);

    List<Object[]> getProviderLabRoutings(Integer labNo, String labType);

    List<ProviderLabRoutingModel> findByStatusANDLabNoType(Integer labNo, String labType, String status);

    List<ProviderLabRoutingModel> findByProviderNo(String providerNo, String status);

    List<ProviderLabRoutingModel> findByLabNoTypeAndStatus(int labId, String labType, String status);

    List<Integer> findLastRoutingIdGroupedByProviderAndCreatedByDocCreator(String docCreator);

    List<Object[]> findProviderAndLabRoutingById(Integer id);

    List<Object[]> findMdsResultResultDataByManyThings(String status, String providerNo, String patientLastName,
                                                       String patientFirstName, String patientHealthNumber);

    List<Object[]> findMdsResultResultDataByDemographicNoAndLabNo(Integer demographicNo, Integer labNo);

    List<Object[]> findMdsResultResultDataByDemoId(String demographicNo);

    List<Object[]> findProviderAndLabRoutingByIdAndLabType(Integer id, String labType);
}
