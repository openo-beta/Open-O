package ca.openosp.openo.test.mocks;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.model.security.Secobjprivilege;
import ca.openosp.openo.model.security.Secuserrole;

import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of SecurityInfoManager that always grants access.
 * Used for testing to bypass security checks.
 */
public class MockSecurityInfoManager implements SecurityInfoManager {

    @Override
    public List<Secuserrole> getRoles(LoggedInInfo loggedInInfo) {
        return Collections.emptyList();
    }

    @Override
    public List<Secobjprivilege> getSecurityObjects(LoggedInInfo loggedInInfo) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasPrivilege(LoggedInInfo loggedInInfo, String objectName, String privilege, int demographicNo) {
        // Always grant access in tests
        return true;
    }

    @Override
    public boolean hasPrivilege(LoggedInInfo loggedInInfo, String objectName, String privilege, String demographicNo) {
        // Always grant access in tests
        return true;
    }

    @Override
    public boolean isAllowedAccessToPatientRecord(LoggedInInfo loggedInInfo, Integer demographicNo) {
        // Always grant access in tests
        return true;
    }
}