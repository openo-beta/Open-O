//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DocumentStorage;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentStorageDaoImpl extends AbstractDaoImpl<DocumentStorage> implements DocumentStorageDao {

    public DocumentStorageDaoImpl() {
        super(DocumentStorage.class);
    }
}
