//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CdsFormOption;

public interface CdsFormOptionDao extends AbstractDao<CdsFormOption> {
    List<CdsFormOption> findByVersionAndCategory(String formVersion, String mainCatgeory);

    List<CdsFormOption> findByVersion(String formVersion);
}
