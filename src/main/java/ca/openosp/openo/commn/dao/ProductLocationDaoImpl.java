//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProductLocation;
import org.springframework.stereotype.Repository;

@Repository
public class ProductLocationDaoImpl extends AbstractDaoImpl<ProductLocation> implements ProductLocationDao {

    public ProductLocationDaoImpl() {
        super(ProductLocation.class);
    }
}
