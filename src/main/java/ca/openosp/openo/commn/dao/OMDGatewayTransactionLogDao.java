/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.commn.dao;

import org.oscarehr.common.model.OMDGatewayTransactionLog;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository
public class OMDGatewayTransactionLogDao extends AbstractDao<OMDGatewayTransactionLog> {

  public OMDGatewayTransactionLogDao() {
    super(OMDGatewayTransactionLog.class);
  }

  @SuppressWarnings("unchecked")
  public List<OMDGatewayTransactionLog> findByOscarSessionId(String id) {
    Query query = entityManager.createQuery(
        "select x from OMDGatewayTransactionLog x where x.oscarSessionId=?");
    query.setParameter(1, id);
    return (List<OMDGatewayTransactionLog>) query.getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<OMDGatewayTransactionLog> findByUniqueSessionId(String id) {
    Query query = entityManager.createQuery(
        "select x from OMDGatewayTransactionLog x where x.uniqueSessionId=?");
    query.setParameter(1, id);
    return (List<OMDGatewayTransactionLog>) query.getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<OMDGatewayTransactionLog> findByProviderNo(String id) {
    Query query = entityManager.createQuery(
        "select x from OMDGatewayTransactionLog x where x.initiatingProviderNo=? ORDER BY x.started desc");
    query.setParameter(1, id);
    return (List<OMDGatewayTransactionLog>) query.getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<OMDGatewayTransactionLog> getAll() {
    Query query = entityManager.createQuery(
        "select x from OMDGatewayTransactionLog x ORDER BY x.started desc");
    return (List<OMDGatewayTransactionLog>) query.getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<OMDGatewayTransactionLog> findByExternalSystem(String systemType) {
    Query query = entityManager.createQuery(
        "select x from OMDGatewayTransactionLog x  where x.externalSystem=? ORDER BY x.started desc");
    query.setParameter(1, systemType);
    return (List<OMDGatewayTransactionLog>) query.getResultList();
  }
}
