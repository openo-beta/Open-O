//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.PreventionsLotNrs;

public interface PreventionsLotNrsDao extends AbstractDao<PreventionsLotNrs> {
    List<PreventionsLotNrs> findLotNrData(Boolean bDeleted);

    PreventionsLotNrs findByName(String prevention, String lotNr, Boolean bDeleted);

    List<String> findLotNrs(String prevention, Boolean bDeleted);

    List<PreventionsLotNrs> findPagedData(String prevention, Boolean bDeleted, Integer offset, Integer limit);
}
