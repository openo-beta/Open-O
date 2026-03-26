//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DemographicContact;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicContactDaoImpl extends AbstractDaoImpl<DemographicContact> implements DemographicContactDao {

    public DemographicContactDaoImpl() {
        super(DemographicContact.class);
    }

    @Override
    public List<DemographicContact> findByDemographicNo(int demographicNo) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.demographicNo=?1 and x.deleted=false";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);
        @SuppressWarnings("unchecked")
        List<DemographicContact> dContacts = query.getResultList();
        return dContacts;
    }

    @Override
    public List<DemographicContact> findActiveByDemographicNo(int demographicNo) {
        String sql = "select x from " + this.modelClass.getName()
                + " x where x.demographicNo=?1 and x.deleted=false and x.active=1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);
        @SuppressWarnings("unchecked")
        List<DemographicContact> dContacts = query.getResultList();
        return dContacts;
    }

    @Override
    public List<DemographicContact> findByDemographicNoAndCategory(int demographicNo, String category) {
        String sql = "select x from " + this.modelClass.getName()
                + " x where x.demographicNo=?1 and x.category=?2 and x.deleted=false";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);
        query.setParameter(2, category);
        @SuppressWarnings("unchecked")
        List<DemographicContact> dContacts = query.getResultList();
        return dContacts;
    }

    @Override
    public List<DemographicContact> find(int demographicNo, int contactId) {
        String sql = "select x from " + this.modelClass.getName()
                + " x where x.demographicNo=?1 and x.contactId = ?2 and x.deleted=false";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);
        query.setParameter(2, Integer.valueOf(contactId).toString());
        @SuppressWarnings("unchecked")
        List<DemographicContact> dContacts = query.getResultList();
        return dContacts;
    }

    @Override
    public List<DemographicContact> findAllByContactIdAndCategoryAndType(int contactId, String category, int type) {
        String sql = "select x from " + this.modelClass.getName()
                + " x where x.contactId = ?1 and x.category = ?2 and x.type = ?3";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, Integer.valueOf(contactId).toString());
        query.setParameter(2, category);
        query.setParameter(3, type);

        @SuppressWarnings("unchecked")
        List<DemographicContact> dContacts = query.getResultList();
        return dContacts;
    }

    @Override
    public List<DemographicContact> findAllByDemographicNoAndCategoryAndType(int demographicNo, String category,
                                                                             int type) {
        String sql = "select x from " + this.modelClass.getName()
                + " x where x.demographicNo = ?1 and x.category = ?2 and x.type = ?3 and x.active=1 and deleted=false";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);
        query.setParameter(2, category);
        query.setParameter(3, type);

        @SuppressWarnings("unchecked")
        List<DemographicContact> dContacts = query.getResultList();
        if (dContacts == null) {
            dContacts = Collections.emptyList();
        }
        return dContacts;
    }

    @Override
    public List<DemographicContact> findSDMByDemographicNo(int demographicNo) {
        String sql = "select x from " + this.modelClass.getName()
                + " x where x.demographicNo = ?1 and x.sdm = 'true'  and x.active=1 and deleted=false";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicContact> dContacts = query.getResultList();
        if (dContacts == null) {
            dContacts = Collections.emptyList();
        }
        return dContacts;
    }
}
