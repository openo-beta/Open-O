//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ServiceSpecialists;

public interface ServiceSpecialistsDao extends AbstractDao<ServiceSpecialists> {
    List<ServiceSpecialists> findByServiceId(int serviceId);

    List<Object[]> findSpecialists(Integer servId);
}
