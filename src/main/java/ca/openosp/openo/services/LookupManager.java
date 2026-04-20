//CHECKSTYLE:OFF

package ca.openosp.openo.services;

import java.sql.SQLException;
import java.util.List;

import ca.openosp.openo.model.LookupCodeValue;
import ca.openosp.openo.model.LookupTableDefValue;

public interface LookupManager {
    List LoadCodeList(String tableId, boolean activeOnly, String code, String codeDesc);

    List LoadCodeList(String tableId, boolean activeOnly, String parentCode, String code, String codeDesc);

    LookupTableDefValue GetLookupTableDef(String tableId);

    LookupCodeValue GetLookupCode(String tableId, String code);

    List LoadFieldDefList(String tableId);

    List GetCodeFieldValues(LookupTableDefValue tableDef, String code);

    List GetCodeFieldValues(LookupTableDefValue tableDef);

    String SaveCodeValue(boolean isNew, LookupTableDefValue tableDef, List fieldDefList) throws SQLException;

    int getCountOfActiveClient(String orgCd) throws SQLException;
}
