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

import ca.openosp.openo.commn.model.OMDGatewayTransactionLog;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public class OMDGatewayTransactionLogDao extends AbstractDaoImpl<OMDGatewayTransactionLog> {

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

  /**
   * Finds the log records for one external system whose transaction type is in the given set,
   * within an inclusive date range, most recent first.
   *
   * <p>The transaction-type whitelist is supplied by the caller rather than fixed here, because the
   * meaning of a transaction type belongs to the integration that writes it. The DHDR consent
   * unblock report, for example, passes {@code "PCOI"} with the four override decisions, which
   * excludes the {@code "consentViewletLaunch"} row that the same external system also writes.
   *
   * @param externalSystem String the {@code externalSystem} discriminator to match exactly
   * @param transactionTypes Collection&lt;String&gt; the transaction types to include; must not be
   *     empty
   * @param from Date the inclusive lower bound on the event timestamp ({@code started})
   * @param to Date the inclusive upper bound on the event timestamp ({@code started})
   * @return List&lt;OMDGatewayTransactionLog&gt; the matching records, newest first
   */
  @SuppressWarnings("unchecked")
  public List<OMDGatewayTransactionLog> findByExternalSystemAndTransactionTypes(
      String externalSystem, Collection<String> transactionTypes, Date from, Date to) {
    Query query = entityManager.createQuery(
        "select x from OMDGatewayTransactionLog x where x.externalSystem = ?1 and x.transactionType in (?2) and x.started between ?3 and ?4 order by x.started desc");
    query.setParameter(1, externalSystem);
    query.setParameter(2, transactionTypes);
    query.setParameter(3, from);
    query.setParameter(4, to);
    return (List<OMDGatewayTransactionLog>) query.getResultList();
  }
}
