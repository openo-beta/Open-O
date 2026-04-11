//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.HnrDataValidation;
import org.springframework.stereotype.Repository;

@Repository
public class HnrDataValidationDaoImpl extends AbstractDaoImpl<HnrDataValidation> implements HnrDataValidationDao {

    public HnrDataValidationDaoImpl() {
        super(HnrDataValidation.class);
    }

    @Override
    public HnrDataValidation findMostCurrentByFacilityIdClientIdType(Integer facilityId, Integer clientId, HnrDataValidation.Type type) {
        // build sql string
        String sqlCommand = "select * from HnrDataValidation where facilityId=?1 and clientId=?2 and validationType=?3 order by created desc";

        // set parameters
        Query query = entityManager.createNativeQuery(sqlCommand, modelClass);
        query.setParameter(1, facilityId);
        query.setParameter(2, clientId);
        query.setParameter(3, type.name());

        // run query
        return (getSingleResultOrNull(query));
    }
}
