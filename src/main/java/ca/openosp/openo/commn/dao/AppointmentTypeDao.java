//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AppointmentType;

public interface AppointmentTypeDao extends AbstractDao<AppointmentType> {


    public List<AppointmentType> listAll();

    public AppointmentType findByAppointmentTypeByName(String name);

}
