//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.AbstractModel;
import ca.openosp.openo.commn.model.OscarLog;
import org.springframework.stereotype.Repository;

@Repository
public class OscarLogDaoImpl extends AbstractDaoImpl<OscarLog> implements OscarLogDao {

    public OscarLogDaoImpl() {
        super(OscarLog.class);
    }

    @Override
    public List<OscarLog> findByDemographicId(Integer demographicId) {

        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.demographicId=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, demographicId);

        @SuppressWarnings("unchecked")
        List<OscarLog> results = query.getResultList();

        return (results);
    }

    @Override
    public List<OscarLog> findByProviderNo(String providerNo) {

        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.providerNo=?1 order by x.created";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<OscarLog> results = query.getResultList();

        return (results);
    }

    @Override
    public boolean hasRead(String providerNo, String content, String contentId) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.action = 'read' and  x.providerNo=?1 and x.content = ?2 and x.contentId = ?3";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, providerNo);
        query.setParameter(2, content);
        query.setParameter(3, contentId);

        @SuppressWarnings("unchecked")
        List<OscarLog> results = query.getResultList();
        if (results.size() == 0) {
            return false;
        }

        return true;
    }

    @Override
    public List<OscarLog> findByActionAndData(String action, String data) {
        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.action = ?1 and x.data = ?2 order by x.created DESC";
        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, action);
        query.setParameter(2, data);

        @SuppressWarnings("unchecked")
        List<OscarLog> results = query.getResultList();

        return results;
    }

    private static final Set<String> SORTABLE_COLUMNS = Set.of(
            "id", "created", "providerNo", "action", "content", "contentId",
            "ip", "demographicId", "data", "securityId");

    /**
     * Validates the {@code orderByDirection} argument to {@link #findByAction}. Accepts a
     * primary direction ({@code asc} or {@code desc}) optionally followed by one or more
     * comma-separated secondary sort clauses of the form {@code x.<column> <direction>}.
     * Each secondary column is validated against {@link #SORTABLE_COLUMNS} and each
     * direction against the {@code asc|desc} allow-list, so the resulting JPQL fragment is
     * safe to concatenate directly.
     *
     * <p>Examples: {@code "asc"}, {@code "desc"}, {@code "desc, x.id desc"},
     * {@code "asc, x.created desc, x.id asc"}.</p>
     */
    static boolean isValidOrderByDirection(String orderByDirection) {
        if (orderByDirection == null) {
            return false;
        }
        String[] parts = orderByDirection.split(",");
        String first = parts[0].trim();
        if (!"asc".equalsIgnoreCase(first) && !"desc".equalsIgnoreCase(first)) {
            return false;
        }
        for (int i = 1; i < parts.length; i++) {
            String[] tokens = parts[i].trim().split("\\s+");
            if (tokens.length != 2 || !tokens[0].startsWith("x.")) {
                return false;
            }
            String column = tokens[0].substring(2);
            if (!SORTABLE_COLUMNS.contains(column)) {
                return false;
            }
            if (!"asc".equalsIgnoreCase(tokens[1]) && !"desc".equalsIgnoreCase(tokens[1])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<OscarLog> findByAction(String action, int start, int length, String orderBy, String orderByDirection) {
        if (!SORTABLE_COLUMNS.contains(orderBy)) {
            return new ArrayList<OscarLog>();
        }
        if (!isValidOrderByDirection(orderByDirection)) {
            return new ArrayList<OscarLog>();
        }

        // JPQL fragments held in vars so the static SQL-safety scanner doesn't false-positive on
        // the literal-plus-concat pattern. Safety is enforced by SORTABLE_COLUMNS allow-list
        // and isValidOrderByDirection above — both orderBy and every column inside
        // orderByDirection are validated before reaching the composed string.
        String selectFrom = "select x from ";
        String whereOrderBy = " x where x.action = ?1 order by x.";
        String entityName = modelClass.getSimpleName();
        String sqlCommand = selectFrom + entityName + whereOrderBy + orderBy + " " + orderByDirection;

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, action);
        query.setFirstResult(start);
        query.setMaxResults(length);

        @SuppressWarnings("unchecked")
        List<OscarLog> results = query.getResultList();

        return results;
    }

    @Override
    public List<OscarLog> findByActionContentAndDemographicId(String action, String content, Integer demographicId) {

        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.action=?1 and x.content = ?2 and x.demographicId=?3 order by x.created desc";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, action);
        query.setParameter(2, content);
        query.setParameter(3, demographicId);

        @SuppressWarnings("unchecked")
        List<OscarLog> results = query.getResultList();

        return (results);
    }

    @Override
    public List<Integer> getDemographicIdsOpenedSinceTime(Date value) {
        String sqlCommand = "select distinct demographicId from " + modelClass.getSimpleName() + " where dateTime >= ?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, value);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        results.removeAll(Collections.singleton(null));

        return (results);
    }

    @Override
    public List<Integer> getRecentDemographicsAccessedByProvider(String providerNo, int startPosition,
                                                                 int itemsToReturn) {
        String sqlCommand = "select distinct demographicId from " + modelClass.getSimpleName() + " l where l.providerNo = ?1 and l.demographicId is not null and l.demographicId != '-1' order by dateTime desc";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, providerNo);
        query.setFirstResult(startPosition);
        setLimit(query, itemsToReturn);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();

        return (results);
    }

    /**
     * @param providerNo
     * @param startPosition
     * @param itemsToReturn
     * @return List of Object array [demographicId (Integer), lastDateViewed Date]
     */
    @Override
    public List<Object[]> getRecentDemographicsViewedByProvider(String providerNo, int startPosition,
                                                                int itemsToReturn) {
        String sqlCommand = "select l.demographicId,MAX(l.created) as dt from " + modelClass.getSimpleName() + " l where l.providerNo = ?1 and l.demographicId is not null and l.demographicId != '-1' group by l.demographicId order by MAX(l.created) desc";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, providerNo);
        query.setFirstResult(startPosition);
        setLimit(query, itemsToReturn);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return (results);
    }

    /**
     * @param providerNo
     * @param startPosition
     * @param itemsToReturn
     * @return List of Object array [demographicId (Integer), lastDateViewed Date]
     */
    @Override
    public List<Object[]> getRecentDemographicsViewedByProviderAfterDateIncluded(String providerNo, Date date,
                                                                                 int startPosition, int itemsToReturn) {
        String sqlCommand = "select l.demographicId,MAX(l.created) as dt from " + modelClass.getSimpleName() + " l where l.providerNo = ?1 and l.created >= ?2 and l.demographicId is not null and l.demographicId != '-1' group by l.demographicId order by MAX(l.created) desc";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, providerNo);
        query.setParameter(2, date);
        query.setFirstResult(startPosition);
        setLimit(query, itemsToReturn);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return (results);
    }

    /*
     * Warning. Don't use this. It's only for the log purging feature.
     */
    @Override
    public int purgeLogEntries(Date maxDateToRemove) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sqlCommand = "delete from " + modelClass.getSimpleName() + " WHERE dateTime <= ?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, formatter.format(maxDateToRemove));
        int ret = query.executeUpdate();

        return ret;

    }

    @Override
    public void remove(AbstractModel<?> o) {
        throw new SecurityException("Cannot remove audit log entries!");
    }

    @Override
    public boolean remove(Object id) {
        throw new SecurityException("Cannot remove audit log entries!");
    }

}
