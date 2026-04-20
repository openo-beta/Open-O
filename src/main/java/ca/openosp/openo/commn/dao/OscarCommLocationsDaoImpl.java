//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.NativeSql;
import ca.openosp.openo.commn.model.OscarCommLocations;
import org.springframework.stereotype.Repository;

@Repository
public class OscarCommLocationsDaoImpl extends AbstractDaoImpl<OscarCommLocations> implements OscarCommLocationsDao {

    public OscarCommLocationsDaoImpl() {
        super(OscarCommLocations.class);
    }

    @Override
    public List<OscarCommLocations> findByCurrent1(int current1) {
        Query q = entityManager.createQuery("SELECT x FROM OscarCommLocations x WHERE x.current1=?1");
        q.setParameter(1, current1);

        @SuppressWarnings("unchecked")
        List<OscarCommLocations> results = q.getResultList();

        return results;

    }

    @NativeSql({"messagetbl", "oscarcommlocations"})
    @Override
    public List<Object[]> findFormLocationByMesssageId(String messId) {
        String sql = "select ocl.locationDesc, mess.thesubject from messagetbl mess, oscarcommlocations ocl where"
                + "mess.sentByLocation = ocl.locationId and mess.messageid = ?1 ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, messId);
        return query.getResultList();
    }

    @NativeSql({"messagetbl", "oscarcommlocations"})
    @Override
    public List<Object[]> findAttachmentsByMessageId(String messageId) {
        String sql = "SELECT m.thesubject, m.theime, m.thedate, m.attachment, m.themessage, m.sentBy, ocl.locationDesc  "
                + "FROM messagetbl m, oscarcommlocations ocl where m.sentByLocation = ocl.locationId and "
                + " messageid = ?1";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, messageId);
        return query.getResultList();
    }
}
