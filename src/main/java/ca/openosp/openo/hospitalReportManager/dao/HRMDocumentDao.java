//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.hospitalReportManager.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.hospitalReportManager.model.HRMDocument;
import org.springframework.stereotype.Repository;

@Repository
public class HRMDocumentDao extends AbstractDaoImpl<HRMDocument> {

    /**
     * Maps allowed order column names to their safe HQL ORDER BY fragments (ascending).
     * Only values from this map are used in query construction, preventing HQL injection.
     */
    private static final Map<String, String> ORDER_ASC_FRAGMENTS = new HashMap<>();
    private static final Map<String, String> ORDER_DESC_FRAGMENTS = new HashMap<>();
    static {
        ORDER_ASC_FRAGMENTS.put("formattedName", " ORDER BY x.formattedName ASC");
        ORDER_ASC_FRAGMENTS.put("dob",           " ORDER BY x.dob ASC");
        ORDER_ASC_FRAGMENTS.put("reportDate",    " ORDER BY x.reportDate ASC");
        ORDER_ASC_FRAGMENTS.put("timeReceived",  " ORDER BY x.timeReceived ASC");
        ORDER_ASC_FRAGMENTS.put("sourceFacility"," ORDER BY x.sourceFacility ASC");

        ORDER_DESC_FRAGMENTS.put("formattedName", " ORDER BY x.formattedName DESC");
        ORDER_DESC_FRAGMENTS.put("dob",           " ORDER BY x.dob DESC");
        ORDER_DESC_FRAGMENTS.put("reportDate",    " ORDER BY x.reportDate DESC");
        ORDER_DESC_FRAGMENTS.put("timeReceived",  " ORDER BY x.timeReceived DESC");
        ORDER_DESC_FRAGMENTS.put("sourceFacility"," ORDER BY x.sourceFacility DESC");
    }

    /**
     * Returns a safe, pre-built ORDER BY HQL fragment for the given column and direction,
     * or an empty string if the column or direction is not in the allowlist.
     */
    private static String getSafeOrderByFragment(String orderColumn, String orderDirection) {
        if (StringUtils.isEmpty(orderColumn) || StringUtils.isEmpty(orderDirection)) {
            return "";
        }
        Map<String, String> fragments = "DESC".equalsIgnoreCase(orderDirection) ? ORDER_DESC_FRAGMENTS : ORDER_ASC_FRAGMENTS;
        String fragment = fragments.get(orderColumn);
        return fragment != null ? fragment : "";
    }

    public HRMDocumentDao() {
        super(HRMDocument.class);
    }

    public List<HRMDocument> findById(int id) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.id=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, id);
        @SuppressWarnings("unchecked")
        List<HRMDocument> documents = query.getResultList();
        return documents;
    }

    public List<HRMDocument> findAll(int offset, int limit) {
        String sql = "select x from " + this.modelClass.getName() + " x order by x.id";
        Query query = entityManager.createQuery(sql);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<HRMDocument> documents = query.getResultList();
        return documents;
    }

    public List<HRMDocument> findAll() {
        String sql = "select x from " + this.modelClass.getName() + " x order by x.id";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<HRMDocument> documents = query.getResultList();
        return documents;
    }


    public List<Integer> findByHash(String hash) {
        String sql = "select distinct id from " + this.modelClass.getName() + " x where x.reportHash=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hash);
        @SuppressWarnings("unchecked")
        List<Integer> matches = query.getResultList();
        return matches;
    }

    public List<HRMDocument> findByNoTransactionInfoHash(String hash) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.reportLessTransactionInfoHash=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hash);
        @SuppressWarnings("unchecked")
        List<HRMDocument> matches = query.getResultList();
        return matches;
    }

    @SuppressWarnings("unchecked")
    public List<Integer> findAllWithSameNoDemographicInfoHash(String hash) {
        String sql = "select distinct parentReport from " + this.modelClass.getName() + " x where x.reportLessDemographicInfoHash=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, hash);
        List<Integer> matches = query.getResultList();

        if (matches != null && matches.size() == 1 && matches.get(0) == null) {
            sql = "select distinct id from " + this.modelClass.getName() + " x where x.reportLessDemographicInfoHash=?1";
            query = entityManager.createQuery(sql);
            query.setParameter(1, hash);
            matches = query.getResultList();
        }
        return matches;
    }

    @SuppressWarnings("unchecked")
    public List<HRMDocument> findAllDocumentsWithRelationship(Integer docId) {
        List<HRMDocument> documentsWithRelationship = new LinkedList<HRMDocument>();
        // Get the document that was specified first
        HRMDocument firstDocument = this.find(docId);
        if (firstDocument != null) {
            String sql = null;
            Query query = null;
            if (firstDocument.getParentReport() != null && !firstDocument.getParentReport().equals(docId)) {
                // This is a child report; get the parent and all siblings of this report (which includes itself)
                sql = "select x from " + this.modelClass.getName() + " x where x.id = ?1 order by x.id asc";
                query = entityManager.createQuery(sql);
                query.setParameter(1, firstDocument.getParentReport());
                documentsWithRelationship.addAll(query.getResultList());

                sql = "select x from " + this.modelClass.getName() + " x where x.parentReport = ?1 order by x.id asc";
                query = entityManager.createQuery(sql);
                query.setParameter(1, firstDocument.getParentReport());
                documentsWithRelationship.addAll(query.getResultList());


            } else {
                // This is a parent report; get all the children of this report as well as itself
                sql = "select x from " + this.modelClass.getName() + " x where x.parentReport = ?1 or x.id = ?2  order by x.id asc";
                query = entityManager.createQuery(sql);
                query.setParameter(1, firstDocument.getId());
                query.setParameter(2, firstDocument.getId());
                documentsWithRelationship = query.getResultList();
            }

        }

        return documentsWithRelationship;

    }

    public List<HRMDocument> getAllChildrenOf(Integer docId) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.parentReport=?1 and x.id != ?2 order by id asc";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, docId);
        query.setParameter(2, docId);
        @SuppressWarnings("unchecked")
        List<HRMDocument> documents = query.getResultList();
        return documents;
    }

    public List<HRMDocument> findByKey(String sourceFacility, String sourceFacilityReportNo, String deliverToId) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.sourceFacility=?1 AND x.sourceFacilityReportNo = ?2 AND x.recipientId = ?3";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, sourceFacility);
        query.setParameter(2, sourceFacilityReportNo);
        query.setParameter(3, deliverToId);

        @SuppressWarnings("unchecked")
        List<HRMDocument> documents = query.getResultList();
        return documents;
    }


    /**
     * Builds the base HQL string for querying HRM documents.
     * All parts are string literals; no user input is concatenated into the HQL.
     *
     * @param demographicUnmatched filter for unmatched demographics
     * @param providerUnmatched filter for unmatched providers
     * @param providerNo provider number filter (null if not filtering)
     * @param noSignOff filter for unsigned documents
     * @param isCount true to build a COUNT query, false for entity selection
     * @return the base HQL string with named parameters
     */
    private String buildQueryHql(boolean demographicUnmatched, boolean providerUnmatched,
                                  String providerNo, boolean noSignOff, boolean categoryUnmatched, boolean isCount) {
        String selectPart = isCount
                ? "select count(x) from HRMDocument x"
                : "select x from HRMDocument x";

        String joinPart = " inner JOIN x.matchedProviders p";
        String wherePart = " WHERE x.parentReport IS NULL";

        String demoFilter = demographicUnmatched ? " AND SIZE(x.matchedDemographics) = 0" : "";

        // A null hrmCategoryId means the report did not match any configured category mapping at import.
        String categoryFilter = categoryUnmatched ? " AND x.hrmCategoryId IS NULL" : "";

        String providerFilter;
        if (providerUnmatched) {
            providerFilter = " AND p.providerNo = :pNo";
        } else {
            String pNoFilter = (providerNo != null) ? " AND p.providerNo = :pNo" : "";
            String signOffFilter = noSignOff ? " AND p.signedOff = 0" : "";
            providerFilter = pNoFilter + signOffFilter;
        }

        return selectPart + joinPart + wherePart + demoFilter + categoryFilter + providerFilter;
    }

    public List<HRMDocument> query(String providerNo, boolean providerUnmatched, boolean noSignOff, boolean demographicUnmatched, boolean categoryUnmatched, int start, int length, String orderColumn, String orderDirection) {

        // Build HQL using pre-built safe fragments to prevent HQL injection.
        // orderColumn and orderDirection are validated via the allowlisted ORDER_*_FRAGMENTS maps.
        String hql = buildQueryHql(demographicUnmatched, providerUnmatched, providerNo, noSignOff, categoryUnmatched, false);
        hql = hql + getSafeOrderByFragment(orderColumn, orderDirection);

        System.out.println("[HRM-INBOX-DEBUG] DAO query hql=" + hql);
        System.out.println("[HRM-INBOX-DEBUG] DAO query params providerNo=" + providerNo + " providerUnmatched=" + providerUnmatched
                + " noSignOff=" + noSignOff + " demographicUnmatched=" + demographicUnmatched + " categoryUnmatched=" + categoryUnmatched
                + " start=" + start + " length=" + length + " orderColumn=" + orderColumn + " orderDirection=" + orderDirection);

        Query query = entityManager.createQuery(hql);

        if (providerUnmatched) {
            query.setParameter("pNo", "-1");
            System.out.println("[HRM-INBOX-DEBUG] DAO query bound pNo=-1 (unmatched)");
        } else {
            if (providerNo != null) {
                query.setParameter("pNo", providerNo);
                System.out.println("[HRM-INBOX-DEBUG] DAO query bound pNo=" + providerNo);
            } else {
                System.out.println("[HRM-INBOX-DEBUG] DAO query no pNo bound (all providers)");
            }
        }

        query.setFirstResult(start);
        query.setMaxResults(length);

        @SuppressWarnings("unchecked")
        List<HRMDocument> documents = query.getResultList();
        System.out.println("[HRM-INBOX-DEBUG] DAO query returned " + documents.size() + " documents");
        return documents;
    }

    public long queryForCount(String providerNo, boolean providerUnmatched, boolean noSignOff, boolean demographicUnmatched, boolean categoryUnmatched, String orderColumn, String orderDirection) {

        // Build HQL using pre-built safe fragments to prevent HQL injection.
        // orderColumn and orderDirection are validated via the allowlisted ORDER_*_FRAGMENTS maps.
        String hql = buildQueryHql(demographicUnmatched, providerUnmatched, providerNo, noSignOff, categoryUnmatched, true);
        hql = hql + getSafeOrderByFragment(orderColumn, orderDirection);

        Query query = entityManager.createQuery(hql);

        if (providerUnmatched) {
            query.setParameter("pNo", "-1");
        } else {
            if (providerNo != null) {
                query.setParameter("pNo", providerNo);
            }
        }

        Long count = (Long) query.getSingleResult();

        System.out.println("[HRM-INBOX-DEBUG] DAO queryForCount hql=" + hql + " -> count=" + count);
        return count;
    }

    /**
     * Debug only: prints raw table counts so the inbox filter result can be compared
     * against what is actually in the database.
     */
    public void debugPrintCounts() {
        try {
            Object totalDocs = entityManager.createQuery("select count(x) from HRMDocument x").getSingleResult();
            Object parentDocs = entityManager.createQuery("select count(x) from HRMDocument x where x.parentReport IS NULL").getSingleResult();
            Object childDocs = entityManager.createQuery("select count(x) from HRMDocument x where x.parentReport IS NOT NULL").getSingleResult();
            Object docsNoProvider = entityManager.createQuery("select count(x) from HRMDocument x where SIZE(x.matchedProviders) = 0").getSingleResult();
            Object docsNoDemo = entityManager.createQuery("select count(x) from HRMDocument x where SIZE(x.matchedDemographics) = 0").getSingleResult();
            Object docsNoCategory = entityManager.createQuery("select count(x) from HRMDocument x where x.hrmCategoryId IS NULL").getSingleResult();
            Object providerRows = entityManager.createQuery("select count(p) from HRMDocumentToProvider p").getSingleResult();
            Object providerRowsUnsigned = entityManager.createQuery("select count(p) from HRMDocumentToProvider p where p.signedOff = 0").getSingleResult();
            Object providerRowsSigned = entityManager.createQuery("select count(p) from HRMDocumentToProvider p where p.signedOff <> 0").getSingleResult();
            Object providerRowsNullSigned = entityManager.createQuery("select count(p) from HRMDocumentToProvider p where p.signedOff IS NULL").getSingleResult();
            Object orphanProviderRows = entityManager.createQuery("select count(p) from HRMDocumentToProvider p where p.hrmDocumentId NOT IN (select x.id from HRMDocument x)").getSingleResult();
            @SuppressWarnings("unchecked")
            List<Object[]> perProvider = entityManager.createQuery("select p.providerNo, p.signedOff, count(p) from HRMDocumentToProvider p group by p.providerNo, p.signedOff").getResultList();
            @SuppressWarnings("unchecked")
            List<Object[]> docIds = entityManager.createQuery("select x.id, x.recipientProviderNo, x.parentReport, x.timeReceived, SIZE(x.matchedProviders) from HRMDocument x order by x.id desc").setMaxResults(50).getResultList();

            System.out.println("[HRM-INBOX-DEBUG] DB counts: HRMDocument total=" + totalDocs + " parentReport null=" + parentDocs + " parentReport set(children, hidden)=" + childDocs);
            System.out.println("[HRM-INBOX-DEBUG] DB counts: docs with NO HRMDocumentToProvider row (hidden by inner join)=" + docsNoProvider
                    + " docs with no demographic=" + docsNoDemo + " docs with null category=" + docsNoCategory);
            System.out.println("[HRM-INBOX-DEBUG] DB counts: HRMDocumentToProvider total=" + providerRows + " signedOff=0 -> " + providerRowsUnsigned
                    + " signedOff<>0 -> " + providerRowsSigned + " signedOff null -> " + providerRowsNullSigned + " orphan rows (no document)=" + orphanProviderRows);
            for (Object[] row : perProvider) {
                System.out.println("[HRM-INBOX-DEBUG] DB HRMDocumentToProvider providerNo=" + row[0] + " signedOff=" + row[1] + " rows=" + row[2]);
            }
            for (Object[] row : docIds) {
                System.out.println("[HRM-INBOX-DEBUG] DB HRMDocument id=" + row[0] + " recipientProviderNo=" + row[1]
                        + " parentReport=" + row[2] + " timeReceived=" + row[3] + " providerRows=" + row[4]);
            }
        } catch (Exception e) {
            System.out.println("[HRM-INBOX-DEBUG] debugPrintCounts failed: " + e);
            e.printStackTrace(System.out);
        }
    }
}
