//CHECKSTYLE:OFF

package ca.openosp.openo.casemgmt.service;

import ca.openosp.openo.casemgmt.dao.ClientImageDAO;
import ca.openosp.openo.casemgmt.model.ClientImage;

public interface ClientImageManager {
    void setClientImageDAO(ClientImageDAO dao);

    void saveClientImage(String id, byte[] image_data, String image_type);

    ClientImage getClientImage(Integer clientId);

    void saveClientImage(ClientImage img);
}
