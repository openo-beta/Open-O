//CHECKSTYLE:OFF


package ca.openosp.openo.daos;

import java.util.List;

import ca.openosp.openo.commn.model.Provider;

/**
 * This couldn't possibly work, it's not a spring managed bean according to the xml files.
 * But oh well, some one imports this class and tries to have it injected so I'll
 * leave the code here so it compiles. what ever...
 */
public interface ProviderDAO {

    public List<Provider> getProviders();

    public Provider getProvider(String provider_no);

    public Provider getProviderByName(String lastName, String firstName);

}
 