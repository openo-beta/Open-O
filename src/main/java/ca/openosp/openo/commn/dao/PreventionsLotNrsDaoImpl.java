//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.PreventionsLotNrs;
import org.springframework.stereotype.Repository;

@Repository
public class PreventionsLotNrsDaoImpl extends AbstractDaoImpl<PreventionsLotNrs> implements PreventionsLotNrsDao {

    public PreventionsLotNrsDaoImpl() {
        super(PreventionsLotNrs.class);
    }

    @Override
    public List<PreventionsLotNrs> findLotNrData(Boolean bDeleted) {
        StringBuilder sb = new StringBuilder();
        sb.append("select x from PreventionsLotNrs x");
        if (bDeleted != null) sb.append(" where x.deleted=?1");
        sb.append(" order by x.preventionType, x.lotNr");
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter(1, bDeleted);

        @SuppressWarnings("unchecked")
        List<PreventionsLotNrs> pList = query.getResultList();

        return (pList);
    }

    @Override
    public PreventionsLotNrs findByName(String prevention, String lotNr, Boolean bDeleted) {
        StringBuilder sb = new StringBuilder();
        sb.append("select x from PreventionsLotNrs x where x.preventionType=?1 and x.lotNr=?2");
        if (bDeleted != null) sb.append(" and x.deleted=?3");
        sb.append(" order by x.lotNr");
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter(1, prevention);
        query.setParameter(2, lotNr);
        if (bDeleted != null) query.setParameter(3, bDeleted);

        return this.getSingleResultOrNull(query);
    }

    @Override
    public List<String> findLotNrs(String prevention, Boolean bDeleted) {
        StringBuilder sb = new StringBuilder();
        sb.append("select x.lotNr from PreventionsLotNrs x where x.preventionType=?1");
        if (bDeleted != null) sb.append(" and x.deleted=?2");
        sb.append(" order by x.lotNr");
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter(1, prevention);
        if (bDeleted != null) query.setParameter(2, bDeleted);

        @SuppressWarnings("unchecked")
        List<String> pList = query.getResultList();

        return (pList);
    }

    @Override
    public List<PreventionsLotNrs> findPagedData(String prevention, Boolean bDeleted, Integer offset, Integer limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("select x from PreventionsLotNrs x where lower(x.preventionType) like ?1");
        if (bDeleted != null) sb.append(" and x.deleted=?2");
        sb.append(" order by x.preventionType, x.lotNr");
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter(1, prevention);
        if (bDeleted != null) query.setParameter(2, bDeleted);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<PreventionsLotNrs> pList = query.getResultList();

        return (pList);
    }
}
