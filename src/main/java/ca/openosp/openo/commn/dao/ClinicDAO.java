//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Clinic;

/**
 * @author Jason Gallagher
 */
public interface ClinicDAO extends AbstractDao<Clinic> {

    public List<Clinic> findAll();

    public Clinic getClinic();

    public void save(Clinic clinic);

}
