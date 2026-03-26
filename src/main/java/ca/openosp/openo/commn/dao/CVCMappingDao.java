//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CVCMapping;

public interface CVCMappingDao extends AbstractDao<CVCMapping> {
    CVCMapping findByOscarName(String oscarName);

    CVCMapping findBySnomedId(String cvcSnomedId);

    List<CVCMapping> findMultipleByOscarName(String oscarName);
}
