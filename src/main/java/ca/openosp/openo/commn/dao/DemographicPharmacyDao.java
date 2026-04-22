//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.DemographicPharmacy;

public interface DemographicPharmacyDao extends AbstractDao<DemographicPharmacy> {

    public DemographicPharmacy addPharmacyToDemographic(Integer pharmacyId, Integer demographicNo,
                                                        Integer preferredOrder);

    public void unlinkPharmacy(Integer pharmacyId, Integer demographicNo);

    public List<DemographicPharmacy> findByDemographicId(Integer demographicNo);

    public List<DemographicPharmacy> findAllByDemographicId(Integer demographicNo);

    public List<DemographicPharmacy> findAllByPharmacyId(Integer pharmacyId);

    public Long getTotalDemographicsPreferedToPharmacyByPharmacyId(Integer pharmacyId);
}
