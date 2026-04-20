//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DrugProductTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DrugProductTemplateDaoImpl extends AbstractDaoImpl<DrugProductTemplate> implements DrugProductTemplateDao {

    public DrugProductTemplateDaoImpl() {
        super(DrugProductTemplate.class);
    }
}
