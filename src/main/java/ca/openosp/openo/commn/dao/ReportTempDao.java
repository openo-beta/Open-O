//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.ReportTemp;

public interface ReportTempDao extends AbstractDao<ReportTemp> {
    List<ReportTemp> findAll();

    List<ReportTemp> findGreateThanEdb(Date edb, int offset, int limit);
}
