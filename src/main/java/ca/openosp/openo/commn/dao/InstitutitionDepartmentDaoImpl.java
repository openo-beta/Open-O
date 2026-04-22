//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.InstitutionDepartment;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutitionDepartmentDaoImpl extends AbstractDaoImpl<InstitutionDepartment> implements InstitutitionDepartmentDao {

    public InstitutitionDepartmentDaoImpl() {
        super(InstitutionDepartment.class);
    }

    @Override
    public List<InstitutionDepartment> findByInstitutionId(int institutionId) {
        Query q = entityManager.createQuery("select x from InstitutionDepartment x where x.id.institutionId = ?1");
        q.setParameter(1, institutionId);

        @SuppressWarnings("unchecked")
        List<InstitutionDepartment> results = q.getResultList();

        return results;
    }
}
