//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;

import ca.openosp.openo.commn.model.Episode;

public interface EpisodeDao extends AbstractDao<Episode> {
    List<Episode> findAll(Integer demographicNo);

    List<Episode> findAllCurrent(Integer demographicNo);

    List<Episode> findCurrentByCodeTypeAndCodes(Integer demographicNo, String codeType, Collection<String> codes);

    List<Episode> findCompletedByCodeTypeAndCodes(Integer demographicNo, String codeType, Collection<String> codes);
}
