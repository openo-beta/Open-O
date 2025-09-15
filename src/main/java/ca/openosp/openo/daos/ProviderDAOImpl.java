//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * <p>
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package ca.openosp.openo.daos;

import java.util.List;

import ca.openosp.openo.commn.model.Provider;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * Implementation of the ProviderDAO interface.
 *
 * This class provides the concrete Hibernate-based implementation for
 * managing healthcare provider data access. It extends HibernateDaoSupport
 * to leverage Spring's Hibernate integration features.
 *
 * The implementation uses HQL (Hibernate Query Language) to retrieve
 * provider information from the database, supporting operations for:
 * - Fetching all providers ordered by last name
 * - Looking up individual providers by their unique identifier
 * - Searching providers by first and last name combination
 *
 * Note: Historical implementation note indicates this class may have
 * Spring configuration issues. The implementation is maintained for
 * backward compatibility with existing code dependencies.
 *
 * @since 2012-01-01
 * @deprecated Consider using ca.openosp.openo.PMmodule.dao.ProviderDao implementation instead
 */
public class ProviderDAOImpl extends HibernateDaoSupport implements ProviderDAO {

    /**
     * {@inheritDoc}
     *
     * Retrieves all providers ordered alphabetically by last name
     * using HQL query.
     */
    @SuppressWarnings("unchecked")
    public List<Provider> getProviders() {
        return (List<Provider>) getHibernateTemplate().find("from Provider p order by p.lastName");
    }

    /**
     * {@inheritDoc}
     *
     * Uses Hibernate's get method for direct primary key lookup,
     * providing efficient single provider retrieval.
     */
    public Provider getProvider(String provider_no) {
        return getHibernateTemplate().get(Provider.class, provider_no);
    }

    /**
     * {@inheritDoc}
     *
     * Searches for provider using HQL with positional parameters.
     * Returns the first matching provider if multiple exist.
     *
     * Note: This method may throw IndexOutOfBoundsException if no
     * provider matches the name criteria.
     */
    public Provider getProviderByName(String lastName, String firstName) {
        return (Provider) getHibernateTemplate().find("from Provider p where p.first_name = ?0 and p.last_name = ?1", firstName, lastName).get(0);
    }

}
