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

package com.quatro.dao.security;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.oscarehr.PMmodule.web.formbean.StaffForm;
import org.oscarehr.util.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.quatro.model.security.Secuserrole;

/**
 * A data access object (DAO) providing persistence and search support for
 * Secuserrole entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 *
 * @author MyEclipse Persistence Tools
 * @see com.quatro.model.security.Secuserrole
 */

public interface SecuserroleDao {
    String PROVIDER_NO = "providerNo";
    String ROLE_NAME = "roleName";
    String ORGCD = "orgcd";
    String ACTIVEYN = "activeyn";

    void saveAll(List list);

    void save(Secuserrole transientInstance);

    void updateRoleName(Integer id, String roleName);

    void delete(Secuserrole persistentInstance);

    int deleteByOrgcd(String orgcd);

    int deleteByProviderNo(String providerNo);

    int deleteById(Integer id);

    int update(Secuserrole instance);

    Secuserrole findById(java.lang.Integer id);

    List findByExample(Secuserrole instance);

    List findByProperty(String propertyName, Object value);

    List findByProviderNo(Object providerNo);

    List findByRoleName(Object roleName);

    List findByOrgcd(Object orgcd, boolean activeOnly);

    List searchByCriteria(StaffForm staffForm);

    List findByActiveyn(Object activeyn);

    List findAll();

    Secuserrole merge(Secuserrole detachedInstance);

    void attachDirty(Secuserrole instance);

    void attachClean(Secuserrole instance);
}
