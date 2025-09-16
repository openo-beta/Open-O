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
import ca.openosp.openo.olis.model.OLISResultNomenclature;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object for managing OLIS result nomenclature data.
 * <p>
 * This DAO provides database operations for OLISResultNomenclature entities, which store
 * standardized laboratory test result codes and names used in OLIS queries. These nomenclature
 * records map internal result identifiers to human-readable names for use in the OLIS search
 * interface and result processing.
 * <p>
 * Key operations:
 * - Find nomenclature records by name ID
 * - Retrieve all result nomenclature entries
 * - Standard CRUD operations via AbstractDaoImpl
 * <p>
 * The nomenclature data typically includes:
 * - Standardized test result codes (e.g., LOINC codes)
 * - Human-readable result names
 * - Mapping between different coding systems
 *
 * @since 2008
 */
@Repository
public class OLISResultNomenclatureDao extends AbstractDaoImpl<OLISResultNomenclature> {


    /**
     * Constructs a new OLISResultNomenclatureDao.
     * <p>
     * Initializes the DAO with the OLISResultNomenclature entity class for type-safe operations.
     */
    public OLISResultNomenclatureDao() {
        super(OLISResultNomenclature.class);
    }

    /**
     * Finds an OLIS result nomenclature record by its name identifier.
     * <p>
     * This method retrieves the nomenclature entry that corresponds to a specific
     * laboratory test result code used in OLIS queries and result processing.
     *
     * @param id String the name identifier for the test result
     * @return OLISResultNomenclature the nomenclature record, or null if not found
     */
    public OLISResultNomenclature findByNameId(String id) {
        String sql = "select x from " + this.modelClass.getName() + " x where x.nameId=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, id);
        return this.getSingleResultOrNull(query);
    }

    /**
     * Retrieves all OLIS result nomenclature records from the database.
     * <p>
     * This method returns all available test result nomenclature entries,
     * which can be used to populate selection lists in the OLIS search interface
     * and for result code translation during processing.
     *
     * @return List<OLISResultNomenclature> list of all result nomenclature records
     */
    @SuppressWarnings("unchecked")
    public List<OLISResultNomenclature> findAll() {
        String sql = "select x from " + this.modelClass.getName() + " x";
        Query query = entityManager.createQuery(sql);
        return query.getResultList();
    }
}
