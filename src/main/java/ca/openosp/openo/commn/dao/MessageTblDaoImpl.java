//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MessageTbl;
import ca.openosp.openo.commn.model.MsgDemoMap;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class MessageTblDaoImpl extends AbstractDaoImpl<MessageTbl> implements MessageTblDao {

    public MessageTblDaoImpl() {
        super(MessageTbl.class);
    }

    @Override
    public List<MessageTbl> findByMaps(List<MsgDemoMap> m) {
        String sql = "select x from MessageTbl x where x.id in (:m)";
        Query query = entityManager.createQuery(sql);
        List<Integer> ids = new ArrayList<Integer>();
        for (MsgDemoMap temp : m) {
            ids.add(temp.getMessageID());
        }
        query.setParameter("m", ids);
        List<MessageTbl> results = query.getResultList();
        return results;
    }

    @Override
    public List<MessageTbl> findByProviderAndSendBy(String providerNo, Integer sendBy) {
        Query query = createQuery("m", "m.sentByNo = :providerNo and m.sentByLocation = :sendBy");
        query.setParameter("providerNo", providerNo);
        query.setParameter("sendBy", sendBy);
        return query.getResultList();
    }

    @Override
    public List<MessageTbl> findByIds(List<Integer> ids) {
        Query query = createQuery("m", "m.id in (:ids) order by m.date");
        query.setParameter("ids", ids);
        return query.getResultList();
    }
}
