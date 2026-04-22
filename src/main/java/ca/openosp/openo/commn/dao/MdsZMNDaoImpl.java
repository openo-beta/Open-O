//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MdsZMN;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class MdsZMNDaoImpl extends AbstractDaoImpl<MdsZMN> implements MdsZMNDao {

    public MdsZMNDaoImpl() {
        super(MdsZMN.class);
    }

    @Override
    public MdsZMN findBySegmentIdAndReportName(Integer id, String reportName) {
        Query query = createQuery("z", "z.id = ?1 AND z.reportName = ?2");
        query.setParameter(1, id);
        query.setParameter(2, reportName);
        return getSingleResultOrNull(query);
    }

    @Override
    public MdsZMN findBySegmentIdAndResultMnemonic(Integer id, String rm) {
        Query query = createQuery("z", "z.id = ?1 and z.resultMnemonic = ?2");
        query.setParameter(1, id);
        query.setParameter(2, rm);
        return getSingleResultOrNull(query);
    }

    @Override
    public List<String> findResultCodes(Integer id, String reportSequence) {
        String sql = "SELECT zmn.resultCode FROM MdsZMN zmn WHERE zmn.id = ?1 " +
                "AND zmn.reportGroup = ?2 ";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, id);
        query.setParameter(2, reportSequence);
        List<Object[]> resultCodes = query.getResultList();
        List<String> result = new ArrayList<String>(resultCodes.size());
        for (Object[] o : resultCodes) {
            result.add(String.valueOf(o[0]));
        }
        return result;
    }
}
