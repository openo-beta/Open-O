//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Prescription;

public interface PrescriptionDao extends AbstractDao<Prescription> {

    public List<Prescription> findByDemographicId(Integer demographicId);

    public List<Prescription> findByDemographicIdUpdatedAfterDate(Integer demographicId, Date afterThisDate);

    public List<Prescription> findByDemographicIdUpdatedAfterDateExclusive(Integer demographicId, Date afterThisDate);

    public int updatePrescriptionsByScriptNo(Integer scriptNo, String comment);

    public List<Prescription> findByUpdateDate(Date updatedAfterThisDateExclusive, int itemsToReturn);

    public List<Prescription> findByProviderDemographicLastUpdateDate(String providerNo, Integer demographicId,
                                                                      Date updatedAfterThisDateExclusive, int itemsToReturn);
}
