//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;

import ca.openosp.openo.commn.model.CaseManagementTmpSave;

public interface CaseManagementTmpSaveDao extends AbstractDao<CaseManagementTmpSave> {
    void remove(String providerNo, Integer demographicNo, Integer programId);

    CaseManagementTmpSave find(String providerNo, Integer demographicNo, Integer programId);

    CaseManagementTmpSave find(String providerNo, Integer demographicNo, Integer programId, Date date);

    boolean noteHasContent(Integer id);
}
