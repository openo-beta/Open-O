//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.Report;
import org.springframework.stereotype.Repository;

@Repository
public class ReportDaoImpl extends AbstractDaoImpl<Report> implements ReportDao {

    public ReportDaoImpl() {
        super(Report.class);
    }
}
