//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.PublicKey;

public interface PublicKeyDao extends AbstractDao<PublicKey> {
    List<PublicKey> findAll();
}
