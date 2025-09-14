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
import ca.openosp.openo.olis.model.OLISRequestNomenclature;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object for managing OLIS request nomenclature data.
 * <p>
 * This DAO provides database operations for OLISRequestNomenclature entities, which store
 * standardized laboratory test request codes and names used in OLIS queries. These nomenclature
 * records map internal test identifiers to human-readable names and categories for use in
 * the OLIS search interface.
 * <p>
 * Key operations:
 * - Find nomenclature records by name ID
 * - Retrieve all request nomenclature entries
 * - Standard CRUD operations via AbstractDaoImpl
 * <p>
 * The nomenclature data typically includes:
 * - Standardized test request codes
 * - Human-readable test names
 * - Categorization for organizing tests in the UI
 *
 * @since 2008
 */
@Repository
public class OLISRequestNomenclatureDao extends AbstractDaoImpl<OLISRequestNomenclature> {


    /**
     * Constructs a new OLISRequestNomenclatureDao.
     * <p>
     * Initializes the DAO with the OLISRequestNomenclature entity class for type-safe operations.
     */
    public OLISRequestNomenclatureDao() {
        super(OLISRequestNomenclature.class);
    }

    /**
     * Finds an OLIS request nomenclature record by its name identifier.
     * <p>
     * This method retrieves the nomenclature entry that corresponds to a specific
     * laboratory test request code used in OLIS queries.
     *
     * @param id String the name identifier for the test request
     * @return OLISRequestNomenclature the nomenclature record, or null if not found
     */
    public OLISRequestNomenclature findByNameId(String id) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.nameId=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, id);
        return this.getSingleResultOrNull(query);
    }

    /**
     * Retrieves all OLIS request nomenclature records from the database.
     * <p>
     * This method returns all available test request nomenclature entries,
     * which can be used to populate selection lists in the OLIS search interface.
     *
     * @return List<OLISRequestNomenclature> list of all request nomenclature records
     */
    @SuppressWarnings("unchecked")
    public List<OLISRequestNomenclature> findAll() {
        String sql = "select x from " + this.modelClass.getName() + " x";
        Query query = entityManager.createQuery(sql);
        return query.getResultList();
    }

}
