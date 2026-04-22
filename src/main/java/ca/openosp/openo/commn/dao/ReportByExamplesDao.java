//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.ReportByExamples;

public interface ReportByExamplesDao extends AbstractDao<ReportByExamples> {
    List<Object[]> findReportsAndProviders();

    List<Object[]> findReportsAndProviders(Date startDate, Date endDate);
}
