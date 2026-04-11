//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.RaHeader;

public interface RaHeaderDao extends AbstractDao<RaHeader> {
    List<RaHeader> findCurrentByFilenamePaymentDate(String filename, String paymentDate);

    List<RaHeader> findByFilenamePaymentDate(String filename, String paymentDate);

    List<RaHeader> findAllExcludeStatus(String status);

    List<RaHeader> findByHeaderDetailsAndProviderMagic(String status, String providerNo);

    List<RaHeader> findByStatusAndProviderMagic(String status, String providerNo);

    List<Object[]> findHeadersAndProvidersById(Integer id);
}
