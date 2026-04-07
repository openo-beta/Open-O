//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.FacilityMessage;
import org.springframework.stereotype.Repository;

@Repository
public class FacilityMessageDaoImpl extends AbstractDaoImpl<FacilityMessage> implements FacilityMessageDao {

    public FacilityMessageDaoImpl() {
        super(FacilityMessage.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FacilityMessage> getMessages() {
        String sql = "select fm from FacilityMessage fm order by fm.expiryDate desc";
        Query query = entityManager.createQuery(sql);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FacilityMessage> getMessagesByFacilityId(Integer facilityId) {
        String sql = "select fm from FacilityMessage fm where fm.facilityId=?1 order by fm.expiryDate desc";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, facilityId);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FacilityMessage> getMessagesByFacilityIdOrNull(Integer facilityId) {
        String sql = "select fm from FacilityMessage fm where (fm.facilityId=?1 or fm.facilityId IS NULL or fm.facilityId=0) order by fm.expiryDate desc";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, facilityId);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FacilityMessage> getMessagesByFacilityIdAndProgramId(Integer facilityId, Integer programId) {
        String sql = "select fm from FacilityMessage fm where fm.facilityId=?1 and fm.programId = ?2 order by fm.expiryDate desc";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, facilityId);
        query.setParameter(2, programId);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FacilityMessage> getMessagesByFacilityIdOrNullAndProgramIdOrNull(Integer facilityId,
                                                                                 Integer programId) {
        String sql = "select fm from FacilityMessage fm where (fm.facilityId=?1 or fm.facilityId IS NULL or fm.facilityId=0) and (fm.programId = ?2 or fm.programId IS NULL) order by fm.expiryDate desc";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, facilityId);
        query.setParameter(2, programId);
        return query.getResultList();
    }

}
