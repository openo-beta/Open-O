//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Institution;

public interface InstitutionDao extends AbstractDao<Institution> {
    List<Institution> findAll();
}
