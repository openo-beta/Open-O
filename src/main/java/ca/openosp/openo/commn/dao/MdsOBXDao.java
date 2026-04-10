//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MdsOBX;

public interface MdsOBXDao extends AbstractDao<MdsOBX> {
    List<MdsOBX> findByIdObrAndCodes(Integer id, String associatedOBR, List<String> codes);
}
