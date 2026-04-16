//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DemographicQueryFavourite;

public interface DemographicQueryFavouritesDao extends AbstractDao<DemographicQueryFavourite> {
    List<DemographicQueryFavourite> findByArchived(String archived);
}
