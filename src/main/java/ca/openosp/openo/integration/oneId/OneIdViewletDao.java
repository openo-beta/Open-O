/**
 * Copyright (c) 2021 WELL EMR Group Inc. This software is made available under the terms of the GNU
 * General Public License, Version 2, 1991 (GPLv2). License details are available via
 * "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.integration.oneId;

import lombok.extern.slf4j.Slf4j;
import org.oscarehr.common.dao.AbstractDao;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.util.List;

@Slf4j
@Repository
public class OneIdViewletDao extends AbstractDao<OneIdViewlet> {
  public OneIdViewletDao() {
    super(OneIdViewlet.class);
  }

  public OneIdViewlet queryOneIdViewletForKey(final String key) {
    var query = entityManager.createQuery("SELECT o from OneIdViewlet o where o.keyValue = ?1");
    query.setParameter(1, key);
    try {
      return (OneIdViewlet) query.getSingleResult();
    } catch (NoResultException nre) {
      log.debug("Cannot find OneIdViewlet of key '" + key + "' | " + nre);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public List<OneIdViewlet> findAllActiveAndShowInEchartTrue() {
    var query =
        entityManager.createQuery(
            "SELECT o FROM OneIdViewlet o WHERE o.deleted = false AND o.showInEchart = true");
    try {
      return query.getResultList();
    } catch (NoResultException nre) {
      log.debug("No active viewlets to display in the eChart | " + nre);
      return null;
    }
  }
}
