//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.PMmodule.model.ClientReferral;

public interface ClientReferralDAO {

    public List<ClientReferral> getReferrals();

    public List<ClientReferral> getReferrals(Long clientId);

    public List<ClientReferral> getReferralsByFacility(Long clientId, Integer facilityId);

    // [ 1842692 ] RFQ Feature - temp change for pmm referral history report
    // - suggestion: to add a new field to the table client_referral (Referring program/agency)
    public List<ClientReferral> displayResult(List<ClientReferral> lResult);

    public List<ClientReferral> getActiveReferrals(Long clientId, Integer facilityId);

    public List<ClientReferral> getActiveReferralsByClientAndProgram(Long clientId, Long programId);

    public ClientReferral getClientReferral(Long id);

    public void saveClientReferral(ClientReferral referral);

    public List<ClientReferral> search(ClientReferral referral);

    public List<ClientReferral> getClientReferralsByProgram(int programId);

}
 