//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.casemgmt.model.ClientImage;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.QueueCache;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * Anyone modifying get and set methods should take note of the dataCache and
 * add/remove items as appropriate.
 */
public class ClientImageDAOImpl extends HibernateDaoSupport implements ClientImageDAO {

    private static final Logger logger = MiscUtils.getLogger();

    /**
     * This is a simple cache for image data because the images are excessively
     * large (relatively speaking). The Integer key is the demographic_no.
     */
    private static final QueueCache<Integer, ClientImage> dataCache = new QueueCache<Integer, ClientImage>(4, 40,
            DateUtils.MILLIS_PER_HOUR, null);

    @Override
    public void saveClientImage(ClientImage clientImage) {
        ClientImage existing = getClientImage(clientImage.getDemographic_no());
        if (existing != null) {
            existing.setImage_data(clientImage.getImage_data());
            existing.setImage_type(clientImage.getImage_type());
            existing.setUpdate_date(new Date());
        }
        getHibernateTemplate().saveOrUpdate(clientImage);

        // update cache
        dataCache.remove(clientImage.getDemographic_no());
    }

    @Override
    public ClientImage getClientImage(Integer clientId) {

        // check cache
        ClientImage clientImage = dataCache.get(clientId);
        if (clientImage == null) {
            logger.debug("dataCache miss : clientId=" + clientId);

            // get from database
            @SuppressWarnings("unchecked")
            List<ClientImage> results = (List<ClientImage>) getHibernateTemplate()
                    .find("from ClientImage i where i.demographic_no=?0 order by update_date desc", clientId);
            if (results.size() > 0) {
                clientImage = results.get(0);

                // add to cache if it's less than ... say 1 megs
                if (clientImage.getImage_data() != null && clientImage.getImage_data().length < 1024000) {
                    dataCache.put(clientId, clientImage);
                    logger.debug("entry found in db, adding to dataCache : clientId=" + clientId);
                }
            }
        } else {
            logger.debug("dataCache hit : clientId=" + clientId);
        }

        return (clientImage);
    }
}
