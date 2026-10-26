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

    /**
     * Saves the signature printed on a prescription, and its text copy, unless one is saved already.
     * The first save wins, even when two windows print the same prescription at once.
     *
     * @param scriptNo Integer the prescription's number
     * @param prescriberSignature String the signature printed on it
     * @param textView String the prescription's text copy with that signature on its first line
     * @return int 1 when the signature was saved, 0 when the prescription already had one
     */
    public int lockPrescriberSignature(Integer scriptNo, String prescriberSignature, String textView);

    public List<Prescription> findByUpdateDate(Date updatedAfterThisDateExclusive, int itemsToReturn);

    public List<Prescription> findByProviderDemographicLastUpdateDate(String providerNo, Integer demographicId,
                                                                      Date updatedAfterThisDateExclusive, int itemsToReturn);
}
