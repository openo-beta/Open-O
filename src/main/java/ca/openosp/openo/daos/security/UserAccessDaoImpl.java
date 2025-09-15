//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 * Contributors:
 * <Quatro Group Software Systems inc.>  <OSCAR Team>
 * <p>
 * Modifications made by Magenta Health in 2024.
 */
package ca.openosp.openo.daos.security;

import java.util.List;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * Hibernate-based implementation of the UserAccessDao interface.
 * <p>
 * This class queries the UserAccessValue view/entity which aggregates user permissions
 * across roles and organizational units. The implementation supports multi-facility
 * deployments with shelter-based filtering.
 * </p>
 * <p>
 * The queries use LIKE patterns to match shelter IDs within comma-separated value fields,
 * which is a legacy database design pattern that should be considered for normalization
 * in future refactoring.
 * </p>
 *
 * @since 2005-01-01
 * @see UserAccessDao
 */
public class UserAccessDaoImpl extends HibernateDaoSupport implements UserAccessDao {

    /**
     * {@inheritDoc}
     * <p>
     * Queries UserAccessValue with optional shelter filtering using LIKE pattern.
     * The shelter ID is embedded in a CSV field using pattern 'S{id},' for matching.
     * This legacy pattern should be refactored to use proper relational design.
     * </p>
     */
    @Override
    public List GetUserAccessList(String providerNo, Integer shelterId) {
        String sSQL = "";
        if (shelterId != null && shelterId.intValue() > 0) {
            // Building LIKE pattern for CSV field - legacy design
            // Pattern: '%S{shelterId},%' matches shelter ID in CSV list
            String s = "'%S" + shelterId.toString() + ",%'";
            sSQL = "from UserAccessValue s where s.providerNo= ?0 " +
                    " and s.orgCdcsv like " + s + " order by s.functionCd, s.privilege desc, s.orgCd";
        } else {
            // No shelter filter - return all access for provider
            sSQL = "from UserAccessValue s where s.providerNo= ?0 " +
                    " order by s.functionCd, s.privilege desc, s.orgCd";
        }
        return getHibernateTemplate().find(sSQL, providerNo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Joins UserAccessValue with LstOrgcd to retrieve organization codes.
     * Filters for at least read privilege ('r' or higher) to determine access.
     * Uses string comparison for privilege levels which relies on character ordering.
     * </p>
     */
    @Override
    public List GetUserOrgAccessList(String providerNo, Integer shelterId) {
        String sSQL = "";
        if (shelterId != null && shelterId.intValue() > 0) {
            // Complex join with shelter filtering in CSV field
            // privilege>='r' uses string comparison (r=read, w=write, x=delete)
            sSQL = "select distinct o.codecsv from UserAccessValue s, LstOrgcd o " +
                    "where s.providerNo= ?0 and s.privilege>='r' and s.orgCd=o.code " +
                    " and o.codecsv like '%S" + shelterId.toString() + ",%'" +
                    " order by o.codecsv";
            return getHibernateTemplate().find(sSQL, providerNo);
        } else {
            // No shelter filter - all organizations with read access
            sSQL = "select distinct o.codecsv from UserAccessValue s, LstOrgcd o where s.providerNo= ?0 and s.privilege>='r' and s.orgCd=o.code order by o.codecsv";
            return getHibernateTemplate().find(sSQL, providerNo);
        }
    }
}
