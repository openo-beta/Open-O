//CHECKSTYLE:OFF

package ca.openosp.openo.services.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ca.openosp.OscarProperties;

import ca.openosp.openo.daos.security.UserAccessDao;
import ca.openosp.openo.model.security.UserAccessValue;
import ca.openosp.openo.services.LookupManager;

public class UserAccessManagerImpl implements UserAccessManager {
    private UserAccessDao _dao = null;

    @Override
    public SecurityManager getUserSecurityManager(String providerNo, Integer shelterId, LookupManager lkManager) {
        // _list is ordered by Function, privilege (desc) and the org
        SecurityManager secManager = new SecurityManager();

        Hashtable functionList = new Hashtable();
        List list = _dao.GetUserAccessList(providerNo, shelterId);
        if (list.size() > 0) {
            int startIdx = 0;
            List orgList = getAccessListForFunction(list, startIdx);
            UserAccessValue uav = (UserAccessValue) list.get(startIdx);
            functionList.put(uav.getFunctionCd(), orgList);

            while (orgList != null && startIdx + orgList.size() < list.size()) {
                startIdx += orgList.size();
                orgList = getAccessListForFunction(list, startIdx);

                uav = (UserAccessValue) list.get(startIdx);
                functionList.put(uav.getFunctionCd(), orgList);
            }
        }
        secManager.setUserFunctionAccessList(functionList);
        List orgs = _dao.GetUserOrgAccessList(providerNo, shelterId);
        String orgRoot = OscarProperties.getInstance().getProperty("ORGROOT");
        if (orgs.size() > 0 && orgRoot != null && orgRoot.equals(orgs.get(0))) {
            orgs.clear();
        }
        secManager.setUserOrgAccessList(orgs);
        return secManager;
    }

    @Override
    public List getAccessListForFunction(List list, int startIdx) {
        if (startIdx >= list.size()) return null;
        List orgList = new ArrayList();
        UserAccessValue uofv = (UserAccessValue) list.get(startIdx);
        String functionCd = uofv.getFunctionCd();
        orgList.add(uofv);
        startIdx++;
        while (startIdx < list.size()) {
            uofv = (UserAccessValue) list.get(startIdx);
            if (uofv.getFunctionCd().equals(functionCd)) {
                orgList.add(uofv);
                startIdx++;
            } else {
                break;
            }
        }
        return orgList;
    }

    @Override
    public void setUserAccessDao(UserAccessDao dao) {
        _dao = dao;
    }
}
