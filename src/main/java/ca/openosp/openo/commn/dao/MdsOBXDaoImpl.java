//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.MdsOBX;
import org.springframework.stereotype.Repository;

import ca.openosp.openo.util.ParamAppender;

@Repository
public class MdsOBXDaoImpl extends AbstractDaoImpl<MdsOBX> implements MdsOBXDao {

    public MdsOBXDaoImpl() {
        super(MdsOBX.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MdsOBX> findByIdObrAndCodes(Integer id, String associatedOBR, List<String> codes) {
        ParamAppender pa = getAppender("obx");
        pa.and("obx.id = :id", "id", id);
        pa.and("obx.associatedOBR = :associatedOBR", "associatedOBR", associatedOBR);

        if (!codes.isEmpty()) {
            ParamAppender codesPa = new ParamAppender();
            for (int i = 0; i < codes.size(); i++) {
                String paramName = "observationSubId" + i;
                codesPa.or("obx.observationSubId like :" + paramName, paramName, "%" + codes.get(i) + "%");
            }
            pa.and(codesPa);
        }

        Query query = entityManager.createQuery(pa.getQuery());
        pa.setParams(query);
        return query.getResultList();
    }
}
