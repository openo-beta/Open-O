package ca.openosp.openo.integration.oneId;

import ca.openosp.openo.commn.dao.AbstractDao;

public interface OneIdSessionDao extends AbstractDao<OneIdSession> {

    /**
     * Clears the authority on a provider's session, but only while it is still the one they are on
     * and no active assignment still grants it.
     *
     * <p>One statement rather than a read and a write. Both conditions move underneath a check made
     * in Java: the provider can pick another authority between the read and the write, and another
     * administrator can add back an assignment carrying the same value. Either would leave this
     * clearing something it should not have.
     *
     * @param providerNo String the provider whose session is being cleared
     * @param uaoValue String the withdrawn authority
     * @return int how many rows were cleared: 1 when it was cleared, 0 when it was not
     */
    int clearSessionUaoIfWithdrawn(String providerNo, String uaoValue);
}
