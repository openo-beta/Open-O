//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Flowsheet;

public interface FlowsheetDao extends AbstractDao<Flowsheet> {
    List<Flowsheet> findAll();

    Flowsheet findByName(String name);
}
