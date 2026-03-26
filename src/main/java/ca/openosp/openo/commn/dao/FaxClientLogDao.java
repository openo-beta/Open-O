//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import ca.openosp.openo.commn.model.FaxClientLog;

public interface FaxClientLogDao extends AbstractDao<FaxClientLog> {

    public FaxClientLog findClientLogbyFaxId(int faxId);

    public List<FaxClientLog> findClientLogbyFaxIds(List<Integer> faxIds);

    public List<FaxClientLog> findClientLogbyRequestId(int requestId);
}
