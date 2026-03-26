//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.decisionSupport.model.DSGuideline;

public interface DSGuidelineDao extends AbstractDao<DSGuideline> {
    DSGuideline findByUUID(String uuid);

    List<DSGuideline> getDSGuidelinesByProvider(String providerNo);
}
