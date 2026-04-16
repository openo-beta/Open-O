//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.decisionSupport.model.DSGuidelineProviderMapping;

import java.util.List;

public interface DSGuidelineProviderMappingDao extends AbstractDao<DSGuidelineProviderMapping> {
    List<DSGuidelineProviderMapping> getMappingsByProvider(String providerNo);

    boolean mappingExists(DSGuidelineProviderMapping dsGuidelineProviderMapping);
}
