//CHECKSTYLE:OFF

package ca.openosp.openo.PMmodule.service;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.PMmodule.exception.AlreadyAdmittedException;
import ca.openosp.openo.PMmodule.exception.AlreadyQueuedException;
import ca.openosp.openo.PMmodule.exception.ServiceRestrictionException;
import ca.openosp.openo.PMmodule.model.ClientReferral;
import ca.openosp.openo.PMmodule.web.formbean.ClientSearchFormBean;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.JointAdmission;

public interface ClientManager {

    boolean isOutsideOfDomainEnabled();

    Demographic getClientByDemographicNo(String demographicNo);

    List<Demographic> getClients();

    List<Demographic> search(ClientSearchFormBean criteria, boolean returnOptinsOnly, boolean excludeMerged);

    List<Demographic> search(ClientSearchFormBean criteria);

    List<ClientReferral> getReferrals();

    List<ClientReferral> getReferrals(String clientId);

    List<ClientReferral> getReferralsByFacility(Integer clientId, Integer facilityId);

    List<ClientReferral> getActiveReferrals(String clientId, String sourceFacilityId);

    ClientReferral getClientReferral(String id);

    void saveClientReferral(ClientReferral referral);

    void addClientReferralToProgramQueue(ClientReferral referral);

    List<ClientReferral> searchReferrals(ClientReferral referral);

    void saveJointAdmission(JointAdmission admission);

    List<JointAdmission> getDependents(Integer clientId);

    List<Integer> getDependentsList(Integer clientId);

    JointAdmission getJointAdmission(Integer clientId);

    boolean isClientDependentOfFamily(Integer clientId);

    boolean isClientFamilyHead(Integer clientId);

    void removeJointAdmission(Integer clientId, String providerNo);

    void removeJointAdmission(JointAdmission admission);

    void processReferral(ClientReferral referral) throws AlreadyAdmittedException, AlreadyQueuedException, ServiceRestrictionException;

    void processReferral(ClientReferral referral, boolean override) throws AlreadyAdmittedException, AlreadyQueuedException, ServiceRestrictionException;

    void saveClient(Demographic client);

    DemographicExt getDemographicExt(String id);

    List<DemographicExt> getDemographicExtByDemographicNo(int demographicNo);

    DemographicExt getDemographicExt(int demographicNo, String key);

    void updateDemographicExt(DemographicExt de);

    void saveDemographicExt(int demographicNo, String key, String value);

    void removeDemographicExt(String id);

    void removeDemographicExt(int demographicNo, String key);

    boolean checkHealthCardExists(String hin, String hcType);
}
