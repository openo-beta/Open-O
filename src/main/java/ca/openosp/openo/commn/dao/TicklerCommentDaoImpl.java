//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.TicklerComment;
import org.springframework.stereotype.Repository;

@Repository
public class TicklerCommentDaoImpl extends AbstractDaoImpl<TicklerComment> implements TicklerCommentDao {

    public TicklerCommentDaoImpl() {
        super(TicklerComment.class);
    }

}
