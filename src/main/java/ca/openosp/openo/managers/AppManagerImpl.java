//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.AppDefinitionDao;
import ca.openosp.openo.commn.dao.AppUserDao;
import ca.openosp.openo.commn.model.AppDefinition;
import ca.openosp.openo.commn.model.AppUser;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.webserv.rest.to.model.AppDefinitionTo1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.openosp.openo.log.LogAction;

@Service
public class AppManagerImpl implements AppManager {
    protected Logger logger = MiscUtils.getLogger();

    @Autowired
    private AppDefinitionDao appDefinitionDao;

    @Autowired
    private AppUserDao appUserDao;

    @Autowired
    private SecurityInfoManager securityInfoManager;


    @Override
    public List<AppDefinitionTo1> getAppDefinitions(LoggedInInfo loggedInInfo) {
        List<AppDefinition> appList = appDefinitionDao.findAll();
        List<AppDefinitionTo1> returningAppList = new ArrayList<AppDefinitionTo1>(appList.size());
        for (AppDefinition app : appList) {
            AppDefinitionTo1 appTo = new AppDefinitionTo1();
            appTo.setId(app.getId());
            appTo.setAdded(app.getAdded());
            appTo.setAppType(app.getAppType());
            appTo.setName(app.getName());
            appTo.setActive(app.getActive());
            appTo.setAddedBy(app.getAddedBy());
            AppUser appuser = appUserDao.findForProvider(app.getId(), loggedInInfo.getLoggedInProviderNo());
            if (appuser != null) {
                appTo.setAuthenticated(true);
            } else {
                appTo.setAuthenticated(false);
            }
            returningAppList.add(appTo);
        }

        //--- log action ---
        if (returningAppList != null && returningAppList.size() > 0) {
            String resultIds = AppDefinition.getIdsAsStringList(appList);
            LogAction.addLogSynchronous(loggedInInfo, "AppManager.getAppDefinitions", "ids returned=" + resultIds);
        }

        return returningAppList;
    }

    @Override
    public AppDefinition saveAppDefinition(LoggedInInfo loggedInInfo, AppDefinition appDef) {
        //Can user create new AppDefinitions?
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_appDefinition", "w", null)) {
            throw new RuntimeException("Access Denied");
        }

        appDefinitionDao.persist(appDef);

        //--- log action ---
        if (appDef != null) {
            LogAction.addLogSynchronous(loggedInInfo, "AppManager.saveAppDefinition", "id=" + appDef.getId());
        }
        return appDef;
    }

    @Override
    public AppDefinition updateAppDefinition(LoggedInInfo loggedInInfo, AppDefinition appDef) {
        //Can user create new AppDefinitions?
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_appDefinition", "w", null)) {
            throw new RuntimeException("Access Denied");
        }

        appDefinitionDao.merge(appDef);

        //--- log action ---
        if (appDef != null) {
            LogAction.addLogSynchronous(loggedInInfo, "AppManager.updateAppDefinition", "id=" + appDef.getId());
        }
        return appDef;
    }

    @Override
    public AppDefinition getAppDefinition(LoggedInInfo loggedInInfo, String appName) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_appDefinition", "r", null)) {
            throw new RuntimeException("Access Denied");
        }
        AppDefinition appDef = appDefinitionDao.findByName(appName);

        //--- log action ---
        if (appDef != null) {
            LogAction.addLogSynchronous(loggedInInfo, "AppManager.getAppDefinition", "id=" + appDef.getId());
        }
        return appDef;
    }

    @Override
    public boolean hasAppDefinition(LoggedInInfo loggedInInfo, String appName) {
        AppDefinition appDef = appDefinitionDao.findByName(appName);

        //--- log action ---
        if (appDef != null) {
            LogAction.addLogSynchronous(loggedInInfo, "AppManager.hasAppDefinition", "id=" + appDef.getId());
            return true;
        }

        LogAction.addLogSynchronous(loggedInInfo, "AppManager.hasAppDefinition", "Not found:" + appName);
        return false;
    }


    @Override
    public Integer getAppDefinitionConsentId(LoggedInInfo loggedInInfo, String appName) {
        AppDefinition appDef = appDefinitionDao.findByName(appName);
        Integer retval = null;
        //--- log action ---
        if (appDef != null) {
            retval = appDef.getConsentTypeId();
            LogAction.addLogSynchronous(loggedInInfo, "AppManager.getAppDefinitionConsentId", "id=" + retval);
        }
        return retval;
    }


}
 