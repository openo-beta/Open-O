//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CaisiAccessType;

public interface CaisiAccessTypeDao extends AbstractDao<CaisiAccessType> {
    List<CaisiAccessType> findAll();

    CaisiAccessType findByName(String name);
}
