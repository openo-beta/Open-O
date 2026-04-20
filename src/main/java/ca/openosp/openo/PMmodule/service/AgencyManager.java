//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import ca.openosp.openo.PMmodule.model.Agency;

public interface AgencyManager {
    Agency getLocalAgency();

    void saveAgency(Agency agency);
}
