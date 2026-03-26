//CHECKSTYLE:OFF

package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import ca.openosp.openo.billing.CA.BC.model.WcbBpCode;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface WcbBpCodeDao extends AbstractDao<WcbBpCode> {

    public List<WcbBpCode> findByCodeOrAnyLevel(String code);
}
