//CHECKSTYLE:OFF

package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.billing.CA.BC.model.WcbBpCode;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class WcbBpCodeDaoImpl extends AbstractDaoImpl<WcbBpCode> implements WcbBpCodeDao {

    protected WcbBpCodeDaoImpl() {
        super(WcbBpCode.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WcbBpCode> findByCodeOrAnyLevel(String code) {
        String codeParamValue = code.substring(0, Math.min(code.length() - 1, 5));
        Query query = createQuery("c",
                "c.code like :codeParamValue OR c.level1 like :c OR c.level2 like :c OR c.level3 like :c ORDER BY c.level1, c.level2, c.level3");
        query.setParameter("codeParamValue", codeParamValue + "%");
        query.setParameter("c", code + "%");
        return query.getResultList();
    }
}
