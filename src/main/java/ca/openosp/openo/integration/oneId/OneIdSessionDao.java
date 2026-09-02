package ca.openosp.openo.integration.oneId;

import ca.openosp.openo.commn.dao.AbstractDao;

public interface OneIdSessionDao extends AbstractDao<OneIdSession> {

    /**
     * Clears the authority on a provider's session, but only while it is still the one given.
     *
     * <p>One statement rather than a read and a write. Between reading the session and clearing it,
     * the provider can pick another authority, and an unconditional clear would take that one away
     * instead of the withdrawn one.
     *
     * @param providerNo String the provider whose session is being cleared
     * @param uaoValue String the authority that must still be in force for the clear to happen
     * @return int how many rows were cleared: 1 when it was still in force, 0 otherwise
     */
    int clearSessionUaoIfMatches(String providerNo, String uaoValue);
}
