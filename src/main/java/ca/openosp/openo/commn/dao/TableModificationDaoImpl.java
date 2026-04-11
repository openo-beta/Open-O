//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import org.springframework.stereotype.Repository;
import ca.openosp.openo.commn.model.TableModification;

@Repository
public class TableModificationDaoImpl extends AbstractDaoImpl<TableModification> implements TableModificationDao {

    public TableModificationDaoImpl() {
        super(TableModification.class);
    }
}
