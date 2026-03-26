//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AppointmentSearch;

public interface AppointmentSearchDao extends AbstractDao<AppointmentSearch> {

    public List<AppointmentSearch> findAll();

    public List<AppointmentSearch> findActive();

    public List<AppointmentSearch> findByUUID(String uuid, Boolean active);

    public AppointmentSearch findForProvider(String providerNo);

}
