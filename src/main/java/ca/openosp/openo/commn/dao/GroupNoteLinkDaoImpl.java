//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.GroupNoteLink;
import org.springframework.stereotype.Repository;

@Repository
public class GroupNoteLinkDaoImpl extends AbstractDaoImpl<GroupNoteLink> implements GroupNoteLinkDao {

    public GroupNoteLinkDaoImpl() {
        super(GroupNoteLink.class);
    }
}
