//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DigitalSignature;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalSignatureDaoImpl extends AbstractDaoImpl<DigitalSignature> implements DigitalSignatureDao {

    public DigitalSignatureDaoImpl() {
        super(DigitalSignature.class);
    }
}
