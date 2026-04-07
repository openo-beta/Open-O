//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DesAnnualReviewPlan;
import org.springframework.stereotype.Repository;

@Repository
public class DesAnnualReviewPlanDaoImpl extends AbstractDaoImpl<DesAnnualReviewPlan> implements DesAnnualReviewPlanDao {

    public DesAnnualReviewPlanDaoImpl() {
        super(DesAnnualReviewPlan.class);
    }

    @Override
    public DesAnnualReviewPlan search(Integer formNo, Integer demographicNo) {

        String sqlCommand = "select x from DesAnnualReviewPlan x where x.formNo <= ?1 and x.demographicNo=?2 order by x.formNo DESC, x.desDate DESC, x.desTime DESC";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, formNo);
        query.setParameter(2, demographicNo);

        @SuppressWarnings("unchecked")
        List<DesAnnualReviewPlan> results = query.getResultList();

        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }
}
