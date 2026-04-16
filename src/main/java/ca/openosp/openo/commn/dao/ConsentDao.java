//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Consent;

public interface ConsentDao extends AbstractDao<Consent> {

    public Consent findByDemographicAndConsentTypeId(int demographic_no, int consentTypeId);

    public Consent findByDemographicAndConsentType(int demographic_no, String consentType);

    public List<Consent> findByDemographic(int demographic_no);

    public List<Consent> findLastEditedByConsentTypeId(int consentTypeId, Date lastEditDate);

    public List<Integer> findAllDemoIdsConsentedToType(int consentTypeId);

}
