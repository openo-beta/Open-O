//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.JointAdmission;

public interface JointAdmissionDao extends AbstractDao<JointAdmission> {
    List<JointAdmission> getSpouseAndDependents(Integer clientId);

    JointAdmission getJointAdmission(Integer clientId);

    void removeJointAdmission(Integer clientId, String providerNo);

    void removeJointAdmission(JointAdmission admission);
}
