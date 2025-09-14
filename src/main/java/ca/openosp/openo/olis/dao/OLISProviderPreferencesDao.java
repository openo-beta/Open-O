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

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import ca.openosp.openo.olis.model.OLISProviderPreferences;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object for managing OLIS provider preferences.
 * <p>
 * This DAO provides database operations for OLISProviderPreferences entities, which store
 * provider-specific settings for OLIS laboratory result polling and processing. Each provider
 * can have individual preferences for when and how OLIS queries should be executed.
 * <p>
 * Key operations:
 * - Find preferences by provider ID
 * - Retrieve all provider preferences
 * - Standard CRUD operations via AbstractDaoImpl
 * <p>
 * The preferences typically include settings such as:
 * - Last run timestamp for polling frequency control
 * - Start time configurations for scheduled operations
 * - Provider-specific OLIS query parameters
 *
 * @since 2008
 */
@Repository
public class OLISProviderPreferencesDao extends AbstractDaoImpl<OLISProviderPreferences> {


    /**
     * Constructs a new OLISProviderPreferencesDao.
     * <p>
     * Initializes the DAO with the OLISProviderPreferences entity class for type-safe operations.
     */
    public OLISProviderPreferencesDao() {
        super(OLISProviderPreferences.class);
    }

    /**
     * Finds OLIS provider preferences by provider ID.
     * <p>
     * This method retrieves the OLIS preferences configuration for a specific healthcare provider.
     * The provider ID typically corresponds to the provider's unique identifier in the system.
     *
     * @param id String the unique provider identifier
     * @return OLISProviderPreferences the preferences for the specified provider, or null if not found
     */
    public OLISProviderPreferences findById(String id) {
        try {
            String sql = "select x from " + this.modelClass.getName() + " x where x.providerId=?1";
            Query query = entityManager.createQuery(sql);
            query.setParameter(1, id);
            return (OLISProviderPreferences) query.getSingleResult();
        } catch (javax.persistence.NoResultException nre) {
            return null;
        }
    }

    /**
     * Retrieves all OLIS provider preferences from the database.
     * <p>
     * This method returns all provider preference configurations that have been
     * set up for OLIS operations, which can be useful for administrative purposes
     * or bulk operations.
     *
     * @return List<OLISProviderPreferences> list of all provider preference records
     */
    @SuppressWarnings("unchecked")
    public List<OLISProviderPreferences> findAll() {
        String sql = "select x from " + this.modelClass.getName() + " x";
        Query query = entityManager.createQuery(sql);
        return query.getResultList();
    }

}
