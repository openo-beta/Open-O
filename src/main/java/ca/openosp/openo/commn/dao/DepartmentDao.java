//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.Department;

import java.util.List;

public interface DepartmentDao extends AbstractDao<Department> {
    List<Department> findAll();
}
