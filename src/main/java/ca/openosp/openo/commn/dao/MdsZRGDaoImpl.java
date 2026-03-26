//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MdsZRG;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class MdsZRGDaoImpl extends AbstractDaoImpl<MdsZRG> implements MdsZRGDao {

    public MdsZRGDaoImpl() {
        super(MdsZRG.class);
    }

    @Override
    public List<Object[]> findById(Integer id) {
        String sql = "SELECT zrg.reportGroupDesc, zrg.reportGroupId, count(zrg.reportGroupID), zrg.reportGroupHeading, zrg.reportSequence " +
                "FROM MdsZRG zrg where zrg.id = :id group by zrg.reportGroupDesc, zrg.reportGroupID " +
                "ORDER BY zrg.reportSequence";
        Query query = entityManager.createQuery(sql);
        query.setParameter("id", id);
        return query.getResultList();
    }

    @Override
    public List<Object> findReportGroupHeadingsById(Integer id, String reportGroupId) {
        String sql = "SELECT zrg.reportGroupHeading FROM MdsZRG zrg where zrg.id = :id " +
                "AND zrg.reportGroupId = :reportGroupId " +
                "ORDER BY zrg.reportSequence";
        Query query = entityManager.createQuery(sql);
        query.setParameter("id", id);
        query.setParameter("reportGroupId", reportGroupId);
        return query.getResultList();
    }
}
