//CHECKSTYLE:OFF


package ca.openosp.openo.daos;

import java.util.List;

import ca.openosp.openo.commn.model.Provider;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * This couldn't possibly work, it's not a spring managed bean according to the xml files.
 * But oh well, some one imports this class and tries to have it injected so I'll
 * leave the code here so it compiles. what ever...
 */
public class ProviderDAOImpl extends HibernateDaoSupport implements ProviderDAO {

    @SuppressWarnings("unchecked")
    public List<Provider> getProviders() {
        return (List<Provider>) getHibernateTemplate().find("from Provider p order by p.lastName");
    }

    public Provider getProvider(String provider_no) {
        return getHibernateTemplate().get(Provider.class, provider_no);
    }

    public Provider getProviderByName(String lastName, String firstName) {
        return (Provider) getHibernateTemplate().find("from Provider p where p.first_name = ?0 and p.last_name = ?1", firstName, lastName).get(0);
    }

}
