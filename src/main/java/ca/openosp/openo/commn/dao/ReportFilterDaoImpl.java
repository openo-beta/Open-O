//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ReportFilter;
import org.springframework.stereotype.Repository;

@Repository
public class ReportFilterDaoImpl extends AbstractDaoImpl<ReportFilter> implements ReportFilterDao {

    public ReportFilterDaoImpl() {
        super(ReportFilter.class);
    }
}
