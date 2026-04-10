//CHECKSTYLE:OFF

package ca.openosp.openo.ticklers.service;

import java.util.List;

import ca.openosp.openo.commn.PaginationQuery;
import ca.openosp.openo.commn.dao.AbstractDao;
import ca.openosp.openo.commn.model.Tickler;
import ca.openosp.openo.ticklers.web.TicklerQuery;

public interface TicklersDao extends AbstractDao<Tickler> {

    public int getTicklersCount(PaginationQuery paginationQuery);

    public List<Tickler> getTicklers(TicklerQuery ticklerQuery);
}
