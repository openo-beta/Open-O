//CHECKSTYLE:OFF

package ca.openosp.openo.casemgmt.service;

import ca.openosp.openo.casemgmt.dao.ClientImageDAO;
import ca.openosp.openo.casemgmt.model.ClientImage;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ClientImageManagerImpl implements ClientImageManager {
    protected ClientImageDAO clientImageDAO;

    @Override
    public void setClientImageDAO(ClientImageDAO dao) {
        this.clientImageDAO = dao;
    }

    @Override
    public void saveClientImage(String id, byte[] image_data, String image_type) {
        ClientImage clientImage = new ClientImage();
        clientImage.setDemographic_no(Integer.parseInt(id));
        clientImage.setImage_data(image_data);
        clientImage.setImage_type(image_type);
        clientImageDAO.saveClientImage(clientImage);
    }

    @Override
    public ClientImage getClientImage(Integer clientId) {
        return clientImageDAO.getClientImage(clientId);
    }

    @Override
    public void saveClientImage(ClientImage img) {
        clientImageDAO.saveClientImage(img);
    }
}
