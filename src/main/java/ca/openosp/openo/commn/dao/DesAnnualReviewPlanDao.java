//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DesAnnualReviewPlan;

public interface DesAnnualReviewPlanDao extends AbstractDao<DesAnnualReviewPlan> {
    DesAnnualReviewPlan search(Integer formNo, Integer demographicNo);
}
