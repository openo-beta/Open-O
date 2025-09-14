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
 * Entity model for OLIS laboratory test result nomenclature data.
 * <p>
 * This entity stores standardized nomenclature information for laboratory test results
 * that can be used in OLIS queries and result processing. The nomenclature provides
 * mapping between internal result codes (such as LOINC codes) and human-readable names
 * for laboratory test results.
 * <p>
 * Key attributes:
 * - ID: Primary key identifier
 * - Name ID: The standardized identifier for the test result
 * - Name: Human-readable description of the test result
 * <p>
 * This nomenclature data supports both the OLIS search interface and result processing
 * by providing standardized test result options and code translations.
 *
 * @since 2008
 */
@Entity
public class OLISResultNomenclature extends AbstractModel<String> {
    /** Primary key identifier for the nomenclature record */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    /** Human-readable name/description of the laboratory test result */
    private String name;
    /** Standardized identifier for the laboratory test result */
    private String nameId;

    /**
     * Gets the standardized test result identifier.
     *
     * @return String the name ID for this test result
     */
    public String getNameId() {
        return nameId;
    }

    /**
     * Sets the standardized test result identifier.
     *
     * @param nameId String the name ID to set
     */
    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    /**
     * Gets the primary key identifier.
     *
     * @return String the primary key
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the human-readable test result name.
     *
     * @return String the descriptive name of the test result
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the human-readable test result name.
     *
     * @param name String the descriptive name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Default constructor for OLISResultNomenclature.
     * <p>
     * Creates a new result nomenclature instance with default values.
     */
    public OLISResultNomenclature() {
        super();
    }
}
