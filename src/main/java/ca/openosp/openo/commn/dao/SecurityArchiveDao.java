//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.commn.model.SecurityArchive;

public interface SecurityArchiveDao extends AbstractDao<SecurityArchive> {
    List<SecurityArchive> findBySecurityNo(Integer securityNo);

    List<String> findPreviousPasswordsByProviderNo(String providerNo, int maxResult);

    Integer archiveRecord(Security s);
}
