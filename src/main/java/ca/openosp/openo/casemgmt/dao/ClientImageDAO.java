//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.casemgmt.model.ClientImage;

/**
 * Anyone modifying get and set methods should take note of the dataCache and
 * add/remove items as appropriate.
 */
public interface ClientImageDAO {

    public void saveClientImage(ClientImage clientImage);

    public ClientImage getClientImage(Integer clientId);
}
