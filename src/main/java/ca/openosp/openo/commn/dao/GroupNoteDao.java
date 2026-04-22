//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.GroupNoteLink;

public interface GroupNoteDao extends AbstractDao<GroupNoteLink> {

    public List<GroupNoteLink> findLinksByDemographic(Integer demographicNo);

    public List<GroupNoteLink> findLinksByDemographicSince(Integer demographicNo, Date lastDateUpdated);

    public List<GroupNoteLink> findLinksByNoteId(Integer noteId);

    public int getNumberOfLinksByNoteId(Integer noteId);
}
