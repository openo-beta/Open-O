//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.*;

import ca.openosp.openo.utility.LoggedInInfo;

import ca.openosp.openo.model.security.Secobjprivilege;
import ca.openosp.openo.model.security.Secuserrole;

public interface SecurityInfoManager {
    public static final String READ = "r";
    public static final String WRITE = "w";
    public static final String UPDATE = "u";
    public static final String DELETE = "d";
    public static final String NORIGHTS = "o";

    public List<Secuserrole> getRoles(LoggedInInfo loggedInInfo);

    public List<Secobjprivilege> getSecurityObjects(LoggedInInfo loggedInInfo);

    public boolean hasPrivilege(LoggedInInfo loggedInInfo, String objectName, String privilege, int demographicNo);

    public boolean hasPrivilege(LoggedInInfo loggedInInfo, String objectName, String privilege, String demographicNo);

    public boolean isAllowedAccessToPatientRecord(LoggedInInfo loggedInInfo, Integer demographicNo);
}
