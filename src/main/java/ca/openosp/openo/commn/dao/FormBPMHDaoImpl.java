//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.FormBPMH;
import org.springframework.stereotype.Repository;

@Repository
public class FormBPMHDaoImpl extends AbstractDaoImpl<FormBPMH> implements FormBPMHDao {

    public FormBPMHDaoImpl() {
        super(FormBPMH.class);
    }

}
