//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TemporalType;

import ca.openosp.openo.commn.model.Consent;
import org.springframework.stereotype.Repository;

@Repository
public class ConsentDaoImpl extends AbstractDaoImpl<Consent> implements ConsentDao {

    protected ConsentDaoImpl() {
        super(Consent.class);
    }

    /**
     * This query should never return more than one consentType. Returns consents
     * that are not deleted only.
     *
     * @param demographic_no the demographic ID
     * @param consentTypeId the consent type ID
     * @return the consent record, or null if not found
     */
    @Override
    public Consent findByDemographicAndConsentTypeId(int demographic_no, int consentTypeId) {
        String sql = "select x from " + modelClass.getSimpleName()
                + " x where x.demographicNo=?1 and x.consentTypeId=?2 AND x.deleted=0";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographic_no);
        query.setParameter(2, consentTypeId);

        Consent consent = getSingleResultOrNull(query);
        return consent;
    }

    @Override
    public Consent findByDemographicAndConsentType(int demographic_no, String consentType) {
        String sql = "select x from " + modelClass.getSimpleName()
                + " x where x.demographicNo=?1 and x.consentType.type=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographic_no);
        query.setParameter(2, consentType);

        Consent consent = getSingleResultOrNull(query);
        return consent;
    }

    @Override
    public List<Consent> findByDemographic(int demographic_no) {
        String sql = "select x from " + modelClass.getSimpleName() + " x where x.demographicNo=?1 AND x.deleted=0";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographic_no);

        @SuppressWarnings("unchecked")
        List<Consent> consent = query.getResultList();
        return consent;
    }

    @Override
    public List<Consent> findLastEditedByConsentTypeId(int consentTypeId, Date lastEditDate) {
        String sql = "SELECT x FROM "
                + modelClass.getSimpleName()
                + " x WHERE x.consentTypeId = ?1"
                + " AND x.editDate  > ?2 AND x.deleted=0";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, consentTypeId);
        query.setParameter(2, lastEditDate, TemporalType.TIMESTAMP);

        @SuppressWarnings("unchecked")
        List<Consent> consents = query.getResultList();
        return consents;
    }

    /**
     * Returns all demographic ids that have consented (opt-in) to the given consent
     * type id.
     *
     * @param consentTypeId
     * @return
     */
    @Override
    public List<Integer> findAllDemoIdsConsentedToType(int consentTypeId) {
        String sql = "SELECT x.demographicNo FROM "
                + modelClass.getSimpleName()
                + " x WHERE x.consentTypeId = ?1"
                + " AND x.optout = 0 "
                + " AND x.deleted = 0";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, consentTypeId);

        @SuppressWarnings("unchecked")
        List<Integer> consents = query.getResultList();
        if (consents == null) {
            consents = Collections.emptyList();
        }
        return consents;
    }

}
