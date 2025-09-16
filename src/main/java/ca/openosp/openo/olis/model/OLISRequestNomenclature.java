//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.olis.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * Entity model for OLIS laboratory test request nomenclature data.
 * <p>
 * This entity stores standardized nomenclature information for laboratory test requests
 * that can be used in OLIS queries. The nomenclature provides mapping between internal
 * test codes and human-readable names, along with categorization for organizing tests
 * in user interfaces.
 * <p>
 * Key attributes:
 * - Name ID: The standardized identifier for the test request
 * - Name: Human-readable description of the test
 * - Category: Grouping classification for UI organization
 * <p>
 * This nomenclature data supports the OLIS search interface by providing standardized
 * test request options that can be selected by users when building laboratory queries.
 *
 * @since 2008
 */
@Entity
public class OLISRequestNomenclature extends AbstractModel<Integer> {
    /** Primary key identifier for the nomenclature record */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /** Standardized identifier for the laboratory test request */
    private String nameId;
    /** Human-readable name/description of the laboratory test */
    private String name;
    /** Category classification for organizing tests in the user interface */
    private String category;

    /**
     * Default constructor for OLISRequestNomenclature.
     * <p>
     * Creates a new request nomenclature instance with default values.
     */
    public OLISRequestNomenclature() {
        super();
    }

    /**
     * Gets the primary key identifier.
     *
     * @return Integer the auto-generated primary key
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Gets the standardized test request identifier.
     *
     * @return String the name ID for this test request
     */
    public String getNameId() {
        return nameId;
    }

    /**
     * Sets the standardized test request identifier.
     *
     * @param nameId String the name ID to set
     */
    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    /**
     * Gets the human-readable test name.
     *
     * @return String the descriptive name of the test
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the human-readable test name.
     *
     * @param name String the descriptive name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the test category for UI organization.
     *
     * @return String the category classification
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the test category for UI organization.
     *
     * @param category String the category classification to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

}
