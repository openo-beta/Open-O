//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportTableFieldCaption;
import org.springframework.stereotype.Repository;

@Repository
public class ReportTableFieldCaptionDaoImpl extends AbstractDaoImpl<ReportTableFieldCaption> implements ReportTableFieldCaptionDao {

    public ReportTableFieldCaptionDaoImpl() {
        super(ReportTableFieldCaption.class);
    }

    @Override
    public List<ReportTableFieldCaption> findByTableNameAndName(String tableName, String name) {
        Query q = entityManager.createQuery("SELECT x FROM ReportTableFieldCaption x WHERE x.tableName=?1 AND x.name=?2");
        q.setParameter(1, tableName);
        q.setParameter(2, name);

        @SuppressWarnings("unchecked")
        List<ReportTableFieldCaption> results = q.getResultList();

        return results;
    }
}
