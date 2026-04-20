//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import ca.openosp.openo.commn.model.IntegratorFileLog;

public interface IntegratorFileLogDao extends AbstractDao<IntegratorFileLog> {

    public IntegratorFileLog getLastFileData();

    public List<IntegratorFileLog> getFileLogHistory();

    public IntegratorFileLog findByFilenameAndChecksum(String filename, String checksum);

    public List<IntegratorFileLog> findAllWithNoCompletedIntegratorStatus();

    public List<IntegratorFileLog> findAllWithNoCompletedOrErrorIntegratorStatus();

}
