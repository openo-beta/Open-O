//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ProfessionalContact;

public interface ProfessionalContactDao extends AbstractDao<ProfessionalContact> {
    List<ProfessionalContact> findAll();

    List<ProfessionalContact> search(String searchMode, String orderBy, String keyword);
}
