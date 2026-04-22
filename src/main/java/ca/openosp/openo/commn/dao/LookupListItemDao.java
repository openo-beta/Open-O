//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.LookupListItem;

public interface LookupListItemDao extends AbstractDao<LookupListItem> {

    public List<LookupListItem> findActiveByLookupListId(int lookupListId);

    public List<LookupListItem> findByLookupListId(int lookupListId, boolean active);

    public LookupListItem findByLookupListIdAndValue(int lookupListId, String value);

}
