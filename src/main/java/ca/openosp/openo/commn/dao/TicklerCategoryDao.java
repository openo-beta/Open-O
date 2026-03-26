//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.TicklerCategory;

public interface TicklerCategoryDao extends AbstractDao<TicklerCategory> {

    public List<TicklerCategory> getActiveCategories();
}
