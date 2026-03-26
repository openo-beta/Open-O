//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ConfigImmunization;

public interface ConfigImmunizationDao extends AbstractDao<ConfigImmunization> {
    List<ConfigImmunization> findAll();

    List<ConfigImmunization> findByArchived(Integer archived, boolean orderByName);
}
