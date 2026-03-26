//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MdsOBR;
import org.springframework.stereotype.Repository;

import ca.openosp.openo.util.ParamAppender;

@Repository
@SuppressWarnings("unchecked")
public class MdsOBRDaoImpl extends AbstractDaoImpl<MdsOBR> implements MdsOBRDao {

    public MdsOBRDaoImpl() {
        super(MdsOBR.class);
    }

    @Override
    public List<Object[]> findByIdAndResultCodes(Integer id, List<String> resultCodes) {
        ParamAppender pa = new ParamAppender("SELECT DISTINCT mdsOBX.associatedOBR, mdsOBR.observationDateTime " +
                "FROM MdsOBX mdsOBX, MdsOBR mdsOBR");
        pa.and("mdsOBX.id = mdsOBR.id");
        pa.and("mdsOBX.associatedOBR = mdsOBR.ObrId");
        pa.and("mdsOBX.id = :id", "id", id);

        if (!resultCodes.isEmpty()) {
            ParamAppender codesPa = new ParamAppender();
            for (int i = 0; i < resultCodes.size(); i++) {
                String paramName = "observationSubId" + i;
                codesPa.or("mdsOBX.observationSubId like :" + paramName, paramName, "%" + resultCodes.get(i) + "%");
            }
            pa.and(codesPa);
        }
        pa.addOrder("mdsOBX.associatedOBR");

        Query query = entityManager.createQuery(pa.getQuery());
        pa.setParams(query);

        return query.getResultList();
    }
}
