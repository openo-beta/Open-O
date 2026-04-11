//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.model.AppDefinition;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.webserv.rest.to.model.AppDefinitionTo1;

public interface AppManager {


    public List<AppDefinitionTo1> getAppDefinitions(LoggedInInfo loggedInInfo);

    public AppDefinition saveAppDefinition(LoggedInInfo loggedInInfo, AppDefinition appDef);

    public AppDefinition updateAppDefinition(LoggedInInfo loggedInInfo, AppDefinition appDef);

    public AppDefinition getAppDefinition(LoggedInInfo loggedInInfo, String appName);

    public boolean hasAppDefinition(LoggedInInfo loggedInInfo, String appName);

    public Integer getAppDefinitionConsentId(LoggedInInfo loggedInInfo, String appName);

}
