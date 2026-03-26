//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Relationships;

public interface RelationshipsDao extends AbstractDao<Relationships> {
    List<Relationships> findAll();

    Relationships findActive(Integer id);

    List<Relationships> findByDemographicNumber(Integer demographicNumber);

    List<Relationships> findActiveSubDecisionMaker(Integer demographicNumber);

    List<Relationships> findActiveByDemographicNumberAndFacility(Integer demographicNumber, Integer facilityId);
}
