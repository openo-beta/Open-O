//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MsgDemoMap;
import org.springframework.stereotype.Repository;

@Repository
public class MsgDemoMapDaoImpl extends AbstractDaoImpl<MsgDemoMap> implements MsgDemoMapDao {

    public MsgDemoMapDaoImpl() {
        super(MsgDemoMap.class);
    }

    @Override
    public List<MsgDemoMap> findByDemographicNo(Integer demographicNo) {
        String sql = "select x from MsgDemoMap x where x.demographic_no=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<MsgDemoMap> results = query.getResultList();
        return results;
    }

    @Override
    public List<MsgDemoMap> findByMessageId(Integer messageId) {
        String sql = "select x from MsgDemoMap x where x.messageID=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, messageId);

        @SuppressWarnings("unchecked")
        List<MsgDemoMap> results = query.getResultList();
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object[]> getMessagesAndDemographicsByMessageId(Integer messageId) {
        String sql = "FROM MsgDemoMap m, Demographic d " +
                "WHERE m.messageID = :msgId " +
                "AND d.DemographicNo = m.demographic_no " +
                "ORDER BY d.LastName, d.FirstName";
        Query query = entityManager.createQuery(sql);
        query.setParameter("msgId", messageId);
        return query.getResultList();
    }

    @Override
    public List<Object[]> getMapAndMessagesByDemographicNo(Integer demoNo) {
        // TODO Auto-generated method stub
        String sql = "FROM MsgDemoMap map, MessageTbl m " +
                "WHERE m.id = map.messageID " +
                "AND map.demographic_no = :demoNo " +
                "ORDER BY m.date DESC, m.id DESC";
        Query query = entityManager.createQuery(sql);
        query.setParameter("demoNo", demoNo);
        return query.getResultList();
    }

    @Override
    public List<Object[]> getMapAndMessagesByDemographicNoAndType(Integer demoNo, Integer type) {
        String sql = "FROM MsgDemoMap map, MessageTbl m " +
                "WHERE m.id = map.messageID " +
                "AND map.demographic_no = :demoNo " +
                "AND m.type = :type ORDER BY m.date DESC, m.id DESC";
        Query query = entityManager.createQuery(sql);
        query.setParameter("demoNo", demoNo);
        query.setParameter("type", type);
        return query.getResultList();
    }

    @Override
    public void remove(Integer messageID, Integer demographicNo) {
        String sql = "select x from MsgDemoMap x where x.messageID = :id and x.demographic_no = :demoNo";
        Query query = entityManager.createQuery(sql);
        query.setParameter("id", messageID);
        query.setParameter("demoNo", demographicNo);

        List<MsgDemoMap> list = query.getResultList();
        for (MsgDemoMap demoMap : list) {
            this.remove(demoMap.getId());
        }

    }
}
