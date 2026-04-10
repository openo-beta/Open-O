//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.Specialty;
import org.springframework.stereotype.Repository;

@Repository
public class SpecialtyDaoImpl extends AbstractDaoImpl<Specialty> implements SpecialtyDao {

    public SpecialtyDaoImpl() {
        super(Specialty.class);
    }
}
