//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package org.oscarehr.hospitalReportManager.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.oscarehr.common.dao.AbstractDaoImpl;
import org.oscarehr.common.dao.SystemPreferencesDao;
import org.oscarehr.common.model.SystemPreferences;
import org.oscarehr.hospitalReportManager.model.HRMDocumentToProvider;
import org.oscarehr.util.SpringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class HRMDocumentToProviderDao extends AbstractDaoImpl<HRMDocumentToProvider> {

    public HRMDocumentToProviderDao() {
        super(HRMDocumentToProvider.class);
    }

    public List<HRMDocumentToProvider> findAllUnsigned(Integer page, Integer pageSize) {
        String sql = "select x from " + this.modelClass.getName() + " x where (x.signedOff IS NULL or x.signedOff = 0)";
        Query query = entityManager.createQuery(sql);
        query.setMaxResults(pageSize);
        query.setFirstResult(page * pageSize);
        @SuppressWarnings("unchecked")
        List<HRMDocumentToProvider> documentToProviders = query.getResultList();
        return documentToProviders;
    }

    public List<HRMDocumentToProvider> findByProviderNo(String providerNo, Integer page, Integer pageSize) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.providerNo=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, providerNo);
        query.setMaxResults(pageSize);
        query.setFirstResult(page * pageSize);
        @SuppressWarnings("unchecked")
        List<HRMDocumentToProvider> documentToProviders = query.getResultList();
        return documentToProviders;
    }

    public List<HRMDocumentToProvider> findByProviderNoLimit(String providerNo, List<Integer> demographicNumbers, boolean patientSearch,
                                                             Date newestDate, Date oldestDate, Integer viewed, Integer signedOff,
                                                             boolean isPaged, Integer page, Integer pageSize) {

        if (patientSearch && (demographicNumbers == null || demographicNumbers.isEmpty())) {
            return Collections.emptyList();
        }

        // Prepare base query and dynamic filters
        StringBuilder baseQuery = new StringBuilder();
        StringBuilder filterClauses = new StringBuilder();
        boolean hasDemographics = demographicNumbers != null && !demographicNumbers.isEmpty();
        boolean hasOnlyZero = hasDemographics && demographicNumbers.size() == 1 && demographicNumbers.get(0) == 0;

        int paramIndex = 2; // Start at 2 since ?1 is reserved for providerNo

        // SELECT + JOINs
        baseQuery.append("SELECT x FROM ").append(this.modelClass.getName()).append(" x ");
        baseQuery.append("JOIN HRMDocument h ON x.hrmDocumentId = h.id ");

        if (hasDemographics && !hasOnlyZero) {
            baseQuery.append("JOIN HRMDocumentToDemographic d ON x.hrmDocumentId = d.hrmDocumentId ");
        }

        // WHERE x.providerNo LIKE ?1
        filterClauses.append("WHERE x.providerNo LIKE ?1 ");

        // Demographic conditions
        if (hasDemographics) {
            if (hasOnlyZero) {
                filterClauses.append("AND h.id NOT IN (SELECT d.hrmDocumentId FROM HRMDocumentToDemographic d) ");
            } else {
                filterClauses.append("AND d.demographicNo IN (?").append(paramIndex++).append(") ");
            }
        }

        // Determine date search type
        SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(SystemPreferencesDao.class);
        String dateSearchType = "serviceObservation";
        SystemPreferences prefs = systemPreferencesDao.findPreferenceByName(SystemPreferences.LAB_DISPLAY_PREFERENCE_KEYS.inboxDateSearchType);
        if (prefs != null && prefs.getValue() != null && !prefs.getValue().isEmpty()) {
            dateSearchType = prefs.getValue();
        }

        if (newestDate != null) {
            filterClauses.append(dateSearchType.equals("receivedCreated")
                            ? "AND h.timeReceived <= ?" : "AND h.reportDate <= ?")
                    .append(paramIndex++).append(" ");
        }

        if (oldestDate != null) {
            filterClauses.append(dateSearchType.equals("receivedCreated")
                            ? "AND h.timeReceived >= ?" : "AND h.reportDate >= ?")
                    .append(paramIndex++).append(" ");
        }

        if (viewed != 2) {
            filterClauses.append("AND x.viewed = ?").append(paramIndex++).append(" ");
        }

        if (signedOff != 2) {
            filterClauses.append("AND x.signedOff = ?").append(paramIndex++).append(" ");
        }

        // Final query string
        String fullQuery = baseQuery.toString() + filterClauses.toString();
        Query query = entityManager.createQuery(fullQuery);

        // Set parameters
        int counter = 1;
        query.setParameter(counter++, providerNo);

        if (hasDemographics && !hasOnlyZero) {
            query.setParameter(counter++, demographicNumbers);
        }

        if (newestDate != null) {
            query.setParameter(counter++, newestDate);
        }

        if (oldestDate != null) {
            query.setParameter(counter++, oldestDate);
        }

        if (viewed != 2) {
            query.setParameter(counter++, viewed);
        }

        if (signedOff != 2) {
            query.setParameter(counter++, signedOff);
        }

        // Pagination
        if (isPaged) {
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
        }

        @SuppressWarnings("unchecked")
        List<HRMDocumentToProvider> documentToProviders = query.getResultList();
        return documentToProviders;
    }

    public List<HRMDocumentToProvider> findByHrmDocumentId(Integer hrmDocumentId) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.hrmDocumentId=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hrmDocumentId);
        @SuppressWarnings("unchecked")
        List<HRMDocumentToProvider> documentToProviders = query.getResultList();
        return documentToProviders;
    }

    public List<HRMDocumentToProvider> findByHrmDocumentIdNoSystemUser(Integer hrmDocumentId) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.hrmDocumentId=?1 and x.providerNo != '-1'";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hrmDocumentId);
        @SuppressWarnings("unchecked")
        List<HRMDocumentToProvider> documentToProviders = query.getResultList();
        return documentToProviders;
    }

    public HRMDocumentToProvider findByHrmDocumentIdAndProviderNo(Integer hrmDocumentId, String providerNo) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.hrmDocumentId=?1 and x.providerNo=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hrmDocumentId);
        query.setParameter(2, providerNo);
        try {
            List<HRMDocumentToProvider> results = query.getResultList();
            return results.get(results.size() - 1);
        } catch (Exception e) {
            return null;
        }
    }

    public List<HRMDocumentToProvider> findByHrmDocumentIdAndProviderNoList(Integer hrmDocumentId, String providerNo) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.hrmDocumentId=?1 and x.providerNo=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hrmDocumentId);
        query.setParameter(2, providerNo);
        @SuppressWarnings("unchecked")
        List<HRMDocumentToProvider> documentToProviders = query.getResultList();
        return documentToProviders;
    }

    public List<HRMDocumentToProvider> findSignedByHrmDocumentId(Integer hrmDocumentId) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.hrmDocumentId=?1 and x.signedOff=1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hrmDocumentId);
        @SuppressWarnings("unchecked")
        List<HRMDocumentToProvider> documentToProviders = query.getResultList();
        return documentToProviders;
    }

    public Integer getCountByProviderNo(String providerNo) {
        String sql = "select count(*) from " + this.modelClass.getName() + " x where x.providerNo=?1 and x.signedOff=0";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, providerNo);
        @SuppressWarnings("unchecked")
        Long result = (Long) query.getSingleResult();
        return result.intValue();
    }
}
