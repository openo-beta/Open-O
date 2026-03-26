//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.RecycleBin;
import org.springframework.stereotype.Repository;

@Repository
public class RecycleBinDaoImpl extends AbstractDaoImpl<RecycleBin> implements RecycleBinDao {

    public RecycleBinDaoImpl() {
        super(RecycleBin.class);
    }
}
