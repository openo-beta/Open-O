//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.olis.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.olis.model.OLISSystemPreferences;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object for managing OLIS system-wide preferences.
 * <p>
 * This DAO provides database operations for OLISSystemPreferences entities, which store
 * global configuration settings for OLIS operations. These preferences control the
 * automated polling schedule, time windows, and other system-wide behaviors for
 * laboratory result retrieval.
 * <p>
 * Key operations:
 * - Retrieve system preferences (singleton pattern)
 * - Save/update system preferences
 * - Standard CRUD operations via AbstractDaoImpl
 * <p>
 * System preferences typically include:
 * - Polling frequency settings
 * - Start and end time windows for automated operations
 * - Last run timestamp for scheduling control
 * - Patient filtering options
 *
 * @since 2008
 */
@Repository
public class OLISSystemPreferencesDao extends AbstractDaoImpl<OLISSystemPreferences> {


    /**
     * Constructs a new OLISSystemPreferencesDao.
     * <p>
     * Initializes the DAO with the OLISSystemPreferences entity class for type-safe operations.
     */
    public OLISSystemPreferencesDao() {
        super(OLISSystemPreferences.class);
    }

    /**
     * Retrieves the system-wide OLIS preferences configuration.
     * <p>
     * This method implements a singleton pattern for system preferences, returning
     * the single configuration record that controls OLIS operations. If no preferences
     * have been configured yet, it returns a new default instance.
     *
     * @return OLISSystemPreferences the system preferences, or a new default instance if none exist
     */
    public OLISSystemPreferences getPreferences() {
        try {
            String sql = "select x from " + this.modelClass.getName() + " x";
            Query query = entityManager.createQuery(sql);
            return (OLISSystemPreferences) query.getSingleResult();
        } catch (javax.persistence.NoResultException nre) {
            return new OLISSystemPreferences();
        }
    }

    /**
     * Saves or updates the OLIS system preferences.
     * <p>
     * This method persists changes to the system-wide OLIS configuration.
     * It uses merge to handle both new and existing preference records.
     *
     * @param olisPrefs OLISSystemPreferences the preferences to save
     */
    public void save(OLISSystemPreferences olisPrefs) {
        entityManager.merge(olisPrefs);
    }
}
