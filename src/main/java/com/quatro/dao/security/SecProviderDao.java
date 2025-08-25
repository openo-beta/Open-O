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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.oscarehr.util.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import com.quatro.model.security.SecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

/**
 * @author JZhang
 */

public interface SecProviderDao {

    String LAST_NAME = "lastName";
    String FIRST_NAME = "firstName";
    String PROVIDER_TYPE = "providerType";
    String SPECIALTY = "specialty";
    String TEAM = "team";
    String SEX = "sex";
    String ADDRESS = "address";
    String PHONE = "phone";
    String WORK_PHONE = "workPhone";
    String OHIP_NO = "ohipNo";
    String RMA_NO = "rmaNo";
    String BILLING_NO = "billingNo";
    String HSO_NO = "hsoNo";
    String STATUS = "status";
    String COMMENTS = "comments";
    String PROVIDER_ACTIVITY = "providerActivity";

    void save(SecProvider transientInstance);

    void saveOrUpdate(SecProvider transientInstance);

    void delete(SecProvider persistentInstance);

    SecProvider findById(java.lang.String id);

    SecProvider findById(java.lang.String id, String status);

    List findByExample(SecProviderDao instance);

    List findByProperty(String propertyName, Object value);

    List findByLastName(Object lastName);

    List findByFirstName(Object firstName);

    List findByProviderType(Object providerType);

    List findBySpecialty(Object specialty);

    List findByTeam(Object team);

    List findBySex(Object sex);

    List findByAddress(Object address);

    List findByPhone(Object phone);

    List findByWorkPhone(Object workPhone);

    List findByOhipNo(Object ohipNo);

    List findByRmaNo(Object rmaNo);

    List findByBillingNo(Object billingNo);

    List findByHsoNo(Object hsoNo);

    List findByStatus(Object status);

    List findByComments(Object comments);

    List findByProviderActivity(Object providerActivity);

    List findAll();

    SecProviderDao merge(SecProviderDao detachedInstance);

    void attachDirty(SecProviderDao instance);

    void attachClean(SecProviderDao instance);
}
