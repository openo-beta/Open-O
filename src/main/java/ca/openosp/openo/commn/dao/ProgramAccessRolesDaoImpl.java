//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ProgramAccessRoles;
import org.springframework.stereotype.Repository;

@Repository
public class ProgramAccessRolesDaoImpl extends AbstractDaoImpl<ProgramAccessRoles> implements ProgramAccessRolesDao {

    public ProgramAccessRolesDaoImpl() {
        super(ProgramAccessRoles.class);
    }

    /**
     * Removes all instances of the model class handled by this DAO.
     *
     * @return Returns the number of all instance removed by this DAO.
     */
    @Override
    public int removeAll() {
        Query q = entityManager.createQuery("DELETE FROM " + modelClass.getSimpleName());
        return q.executeUpdate();
    }
}
