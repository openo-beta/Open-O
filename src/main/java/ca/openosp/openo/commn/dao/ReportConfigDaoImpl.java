//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportConfig;
import org.springframework.stereotype.Repository;

@Repository
public class ReportConfigDaoImpl extends AbstractDaoImpl<ReportConfig> implements ReportConfigDao {

    public ReportConfigDaoImpl() {
        super(ReportConfig.class);
    }

    @Override
    public List<ReportConfig> findByReportIdAndNameAndCaptionAndTableNameAndSave(int reportId, String name, String caption, String tableName, String save) {
        Query q = entityManager.createQuery("select x from ReportConfig x where x.reportId=?1 and x.name=?2 and x.caption=?3 and x.tableName=?4 and x.save=?5");
        q.setParameter(1, reportId);
        q.setParameter(2, name);
        q.setParameter(3, caption);
        q.setParameter(4, tableName);
        q.setParameter(5, save);

        @SuppressWarnings("unchecked")
        List<ReportConfig> results = q.getResultList();

        return results;
    }

    @Override
    public List<ReportConfig> findByReportIdAndSaveAndGtOrderNo(int reportId, String save, int orderNo) {
        Query q = entityManager.createQuery("select x from ReportConfig x where x.reportId=?1 and x.save=?2 and x.orderNo >= ?3 order by x.orderNo DESC");
        q.setParameter(1, reportId);
        q.setParameter(2, save);
        q.setParameter(3, orderNo);


        @SuppressWarnings("unchecked")
        List<ReportConfig> results = q.getResultList();

        return results;
    }
}
