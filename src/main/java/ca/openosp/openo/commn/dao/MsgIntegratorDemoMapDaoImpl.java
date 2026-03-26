//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MsgIntegratorDemoMap;
import org.springframework.stereotype.Repository;

@Repository
public class MsgIntegratorDemoMapDaoImpl extends AbstractDaoImpl<MsgIntegratorDemoMap>
        implements MsgIntegratorDemoMapDao {

    public MsgIntegratorDemoMapDaoImpl() {
        super(MsgIntegratorDemoMap.class);
    }

    /**
     * Find remote integrated demographics where the local message demographic
     * number has not
     * been linked to the remote demographic number
     */
    @Override
    public List<MsgIntegratorDemoMap> findByMessageIdandMsgDemoMapId(Integer messageId, long msgDemoMapId) {
        String sql = "select x from MsgIntegratorDemoMap x where x.messageId=?1 and x.msgDemoMapId = ?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, messageId);
        query.setParameter(2, msgDemoMapId);

        @SuppressWarnings("unchecked")
        List<MsgIntegratorDemoMap> results = query.getResultList();
        if (results == null) {
            results = Collections.emptyList();
        }
        return results;
    }
}
