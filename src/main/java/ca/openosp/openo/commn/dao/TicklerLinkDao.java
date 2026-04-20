//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.TicklerLink;

public interface TicklerLinkDao extends AbstractDao<TicklerLink> {

    public TicklerLink getTicklerLink(Integer id);

    public List<TicklerLink> getLinkByTableId(String tableName, Long tableId);

    public List<TicklerLink> getLinkByTickler(Integer ticklerNo);

    public void save(TicklerLink cLink);

    public void update(TicklerLink cLink);
}
