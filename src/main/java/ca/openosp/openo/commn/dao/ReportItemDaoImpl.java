//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ReportItem;
import org.springframework.stereotype.Repository;

@Repository
public class ReportItemDaoImpl extends AbstractDaoImpl<ReportItem> implements ReportItemDao {

    public ReportItemDaoImpl() {
        super(ReportItem.class);
    }
}
