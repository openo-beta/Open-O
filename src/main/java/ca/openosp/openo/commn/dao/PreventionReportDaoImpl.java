//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.PreventionReport;
import org.springframework.stereotype.Repository;

@Repository
public class PreventionReportDaoImpl extends AbstractDaoImpl<PreventionReport> implements PreventionReportDao {

    public PreventionReportDaoImpl() {
        super(PreventionReport.class);
    }

    @Override
    public List<PreventionReport> getPreventionReports() {
        String sql = "select x from " + modelClass.getSimpleName() + " x where x.active = true order by x.createDate desc";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<PreventionReport> allergies = query.getResultList();
        return allergies;
    }
}
