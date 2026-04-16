//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProviderArchive;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderArchiveDaoImpl extends AbstractDaoImpl<ProviderArchive> implements ProviderArchiveDao {

    public ProviderArchiveDaoImpl() {
        super(ProviderArchive.class);
    }
}
