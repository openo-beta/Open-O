//CHECKSTYLE:OFF


package ca.openosp.openo.daos;

import java.sql.SQLException;
import java.util.List;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.commn.model.Facility;

import ca.openosp.openo.model.LookupCodeValue;
import ca.openosp.openo.model.LookupTableDefValue;

public interface LookupDao {

    public List LoadCodeList(String tableId, boolean activeOnly, String code, String codeDesc);

    public LookupCodeValue GetCode(String tableId, String code);

    public List LoadCodeList(String tableId, boolean activeOnly, String parentCode, String code, String codeDesc);

    public LookupTableDefValue GetLookupTableDef(String tableId);

    public List LoadFieldDefList(String tableId);

    public List GetCodeFieldValues(LookupTableDefValue tableDef, String code);

    public List<List> GetCodeFieldValues(LookupTableDefValue tableDef);

    public String SaveCodeValue(boolean isNew, LookupTableDefValue tableDef, List fieldDefList) throws SQLException;

    public String SaveCodeValue(boolean isNew, LookupCodeValue codeValue) throws SQLException;

    public void SaveAsOrgCode(Program program) throws SQLException;

    public boolean inOrg(String org1, String org2);

    public void SaveAsOrgCode(Facility facility) throws SQLException;

    ;

    public void SaveAsOrgCode(LookupCodeValue orgVal, String tableId) throws SQLException;

    public void runProcedure(String procName, String[] params) throws SQLException;

    public int getCountOfActiveClient(String orgCd) throws SQLException;

    public void setProviderDao(ProviderDao providerDao);

}
