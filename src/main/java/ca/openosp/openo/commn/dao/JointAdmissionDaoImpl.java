//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import java.util.ListIterator;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.JointAdmission;
import org.springframework.stereotype.Repository;

@Repository
public class JointAdmissionDaoImpl extends AbstractDaoImpl<JointAdmission> implements JointAdmissionDao {

    public JointAdmissionDaoImpl() {
        super(JointAdmission.class);
    }

    public List<JointAdmission> getSpouseAndDependents(Integer clientId) {
        Query query = entityManager.createQuery("SELECT x FROM JointAdmission x WHERE x.archived=0 and x.headClientId=?1");
        query.setParameter(1, clientId);
        @SuppressWarnings("unchecked")
        List<JointAdmission> results = query.getResultList();
        return results;
    }

    public JointAdmission getJointAdmission(Integer clientId) {
        Query query = entityManager.createQuery("SELECT x FROM JointAdmission x WHERE x.archived=0 and x.clientId=?1");
        query.setParameter(1, clientId);
        @SuppressWarnings("unchecked")
        List<JointAdmission> results = query.getResultList();

        ListIterator<JointAdmission> li = results.listIterator();

        if (li.hasNext()) {
            return li.next();
        } else {
            return null;
        }
    }

    public void removeJointAdmission(Integer clientId, String providerNo) {
        JointAdmission jadm = getJointAdmission(clientId);
        if (jadm != null) {
            jadm.setArchivingProviderNo(providerNo);
            removeJointAdmission(jadm);
        }
    }


    public void removeJointAdmission(JointAdmission admission) {
        JointAdmission tmp = find(admission.getId());
        if (tmp != null) {
            tmp.setArchived(true);
            merge(tmp);
        }
    }

}
