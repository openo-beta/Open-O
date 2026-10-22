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
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.hospitalReportManager.model.HRMDocument;
import org.springframework.stereotype.Repository;

/**
 * Data access for HRM (Hospital Report Manager) reports: lookups by id, hash and
 * sending-facility key, parent/child report relationships, and the filtered,
 * paged listing behind the HRM Inbox page.
 *
 * @since 2011-06-18
 */
@Repository
public class HRMDocumentDao extends AbstractDaoImpl<HRMDocument> {

    /**
     * Columns the HRM Inbox can be sorted by. Each value pairs the DataTables column name
     * sent by inbox.js with the HRMDocument field it sorts on, so only these fields ever
     * reach the HQL and no request text is concatenated into it.
     */
    public enum InboxSortColumn {
        PATIENT_NAME("patient_name", "x.formattedName"),
        PATIENT_DOB("patient_dob", "x.dob"),
        REPORT_DATE("report_date", "x.reportDate"),
        RECEIVED_DATE("received_date", "x.timeReceived"),
        SENDING_FACILITY("sending_facility", "x.sourceFacility");

        private final String requestName;
        private final String hqlField;

        InboxSortColumn(String requestName, String hqlField) {
            this.requestName = requestName;
            this.hqlField = hqlField;
        }

        /**
         * Finds the sort column for a DataTables column name.
         *
         * @param requestName String the "columns[n][data]" value from the request, may be null
         * @return InboxSortColumn the matching column, or null when that column is not sortable
         */
        public static InboxSortColumn fromRequestName(String requestName) {
            for (InboxSortColumn column : values()) {
                if (column.requestName.equals(requestName)) {
                    return column;
                }
            }
            return null;
        }
    }

    /**
     * Sort direction of the HRM Inbox listing.
     */
    public enum SortDirection {
        ASC, DESC;

        /**
         * Reads a DataTables "order[0][dir]" value. Anything other than "desc" sorts ascending.
         *
         * @param requestValue String "asc" or "desc" in any case, may be null
         * @return SortDirection never null
         */
        public static SortDirection fromRequestValue(String requestValue) {
            return "DESC".equalsIgnoreCase(requestValue) ? DESC : ASC;
        }
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
     * Lists one page of HRM Inbox reports. A report is listed once, no matter how many
     * recipients it has, and only when it has at least one HRMDocumentToProvider row that
     * passes the provider filters. Child reports (parentReport set) are never listed.
     *
     * @param providerNo String provider the report must be addressed to, null for any provider
     * @param providerUnmatched boolean true lists only reports whose recipient could not be matched (provider "-1"); noSignOff is then ignored
     * @param noSignOff boolean true lists only reports the provider has not signed off
     * @param demographicUnmatched boolean true lists only reports with no linked patient
     * @param categoryUnmatched boolean true lists only reports with no category
     * @param start int first row of the page
     * @param length int page size
     * @param sortColumn InboxSortColumn column to sort by, null for no ordering
     * @param sortDirection SortDirection ascending or descending
     * @return List<HRMDocument> the page of matching reports
     */
    public List<HRMDocument> query(String providerNo, boolean providerUnmatched, boolean noSignOff, boolean demographicUnmatched, boolean categoryUnmatched, int start, int length, InboxSortColumn sortColumn, SortDirection sortDirection) {
        String orderBy = sortColumn == null ? "" : " order by " + sortColumn.hqlField + " " + sortDirection;
        TypedQuery<HRMDocument> query = createInboxQuery("select x", HRMDocument.class, providerNo, providerUnmatched, noSignOff, demographicUnmatched, categoryUnmatched, orderBy);
        query.setFirstResult(start);
        query.setMaxResults(length);
        return query.getResultList();
    }

    /**
     * Counts the HRM Inbox reports that {@link #query} would list for the same filters.
     *
     * @param providerNo String provider the report must be addressed to, null for any provider
     * @param providerUnmatched boolean true counts only reports whose recipient could not be matched (provider "-1")
     * @param noSignOff boolean true counts only reports the provider has not signed off
     * @param demographicUnmatched boolean true counts only reports with no linked patient
     * @param categoryUnmatched boolean true counts only reports with no category
     * @return long number of matching reports
     */
    public long queryForCount(String providerNo, boolean providerUnmatched, boolean noSignOff, boolean demographicUnmatched, boolean categoryUnmatched) {
        return createInboxQuery("select count(x)", Long.class, providerNo, providerUnmatched, noSignOff, demographicUnmatched, categoryUnmatched, "").getSingleResult();
    }

    /**
     * Builds and binds the HRM Inbox query shared by {@link #query} and {@link #queryForCount}.
     * The provider filters sit in an EXISTS subquery on HRMDocumentToProvider, so a report with
     * several recipients comes back once and the count needs no DISTINCT.
     *
     * @param select String "select x" or "select count(x)"
     * @param resultType Class<R> HRMDocument.class or Long.class, matching the select
     * @param orderBy String ORDER BY clause built by {@link #query}, "" for none
     * @return TypedQuery<R> the query with its parameter bound
     */
    private <R> TypedQuery<R> createInboxQuery(String select, Class<R> resultType, String providerNo, boolean providerUnmatched, boolean noSignOff, boolean demographicUnmatched, boolean categoryUnmatched, String orderBy) {
        String pNo = providerUnmatched ? "-1" : providerNo;

        StringBuilder hql = new StringBuilder(select).append(" from HRMDocument x where x.parentReport is null");
        if (demographicUnmatched) {
            hql.append(" and size(x.matchedDemographics) = 0");
        }
        if (categoryUnmatched) {
            // A null hrmCategoryId means the report did not match any configured category mapping at import.
            hql.append(" and x.hrmCategoryId is null");
        }
        hql.append(" and exists (select p.id from HRMDocumentToProvider p where p.hrmDocumentId = x.id");
        if (pNo != null) {
            hql.append(" and p.providerNo = :pNo");
        }
        if (!providerUnmatched && noSignOff) {
            hql.append(" and p.signedOff = 0");
        }
        hql.append(")").append(orderBy);


        TypedQuery<R> query = entityManager.createQuery(hql.toString(), resultType);
        if (pNo != null) {
            query.setParameter("pNo", pNo);
        }
        return query;
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
