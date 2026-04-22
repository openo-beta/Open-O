//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.InstitutionDepartment;

public interface InstitutitionDepartmentDao extends AbstractDao<InstitutionDepartment> {
    List<InstitutionDepartment> findByInstitutionId(int institutionId);
}
