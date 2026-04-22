//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MdsOBR;

public interface MdsOBRDao extends AbstractDao<MdsOBR> {
    List<Object[]> findByIdAndResultCodes(Integer id, List<String> resultCodes);
}
