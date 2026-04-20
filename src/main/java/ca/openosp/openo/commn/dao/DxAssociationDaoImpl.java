//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DxAssociation;
import org.springframework.stereotype.Repository;

@Repository
public class DxAssociationDaoImpl extends AbstractDaoImpl<DxAssociation> implements DxAssociationDao {

    public DxAssociationDaoImpl() {
        super(DxAssociation.class);
    }
}
