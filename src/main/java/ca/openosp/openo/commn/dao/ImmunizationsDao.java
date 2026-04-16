//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Immunizations;

public interface ImmunizationsDao extends AbstractDao<Immunizations> {
    List<Immunizations> findCurrentByDemographicNo(Integer demographicNo);
}
