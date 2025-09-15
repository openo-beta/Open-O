//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.PMmodule.model;

import java.io.Serializable;

/**
 * Entity representing functional roles within healthcare programs.
 *
 * FunctionalUserType defines the different operational roles that users
 * can have within a program context. These types categorize users based
 * on their functional responsibilities rather than their professional
 * credentials or security permissions.
 *
 * Key features:
 * - Role categorization for program operations
 * - Functional responsibility mapping
 * - User type classification
 * - Program-specific role definitions
 *
 * Common functional types:
 * - Case Manager: Primary client responsibility
 * - Team Member: Collaborative care participant
 * - Supervisor: Oversight and approval authority
 * - Intake Worker: Initial assessment and screening
 * - Support Staff: Administrative and auxiliary roles
 *
 * Functional types differ from security roles:
 * - Security roles control system access
 * - Functional types define operational duties
 * - Users may have multiple functional types
 * - Types are program-context specific
 *
 * Use cases:
 * - Workflow assignment and routing
 * - Responsibility determination
 * - Team composition management
 * - Reporting by functional role
 * - Capacity planning by type
 *
 * Integration points:
 * - ProgramFunctionalUser for user-type associations
 * - Program team management
 * - Workflow engines and task routing
 * - Statistical reporting by role type
 *
 * Database mapping:
 * - Table: functional_user_type
 * - Primary key: id (auto-generated)
 * - Unique constraint: name field
 *
 * @since 2005-01-01
 * @see ProgramFunctionalUser
 * @see ProgramTeam
 * @see Program
 */
public class FunctionalUserType implements Serializable {

    /**
     * Cached hash code for performance optimization.
     * Invalidated when ID changes.
     */
    private int hashCode = Integer.MIN_VALUE;

    /**
     * Primary key for the functional user type.
     * Auto-generated unique identifier.
     */
    private Long _id;

    /**
     * Name of the functional user type.
     * Descriptive label for the role category.
     * Examples: "Case Manager", "Intake Worker", "Supervisor".
     */
    private String _name;

    /**
     * Default constructor for creating new FunctionalUserType.
     *
     * Creates an empty type instance. The name must be
     * set before persisting to the database.
     *
     * @since 2005-01-01
     */
    public FunctionalUserType() {
        initialize();
    }

    /**
     * Constructor for primary key initialization.
     *
     * Creates a FunctionalUserType with a specific ID.
     * Typically used by persistence framework during
     * object loading from database.
     *
     * @param _id Long the primary key value
     * @since 2005-01-01
     */
    public FunctionalUserType(Long _id) {
        this.setId(_id);
        initialize();
    }


    /**
     * Initializes the object after construction.
     *
     * Protected method for subclass extension.
     * Currently empty but provides hook for future
     * initialization logic.
     *
     * @since 2005-01-01
     */
    protected void initialize() {
        // Hook for future initialization logic
    }

    /**
     * Gets the unique identifier of this functional user type.
     *
     * Returns the primary key from the functional_user_type table.
     * Used to uniquely reference this type definition.
     *
     * @return Long the type ID, null if not persisted
     * @since 2005-01-01
     */
    public Long getId() {
        return _id;
    }

    /**
     * Sets the unique identifier of this functional user type.
     *
     * Updates the primary key value and invalidates the cached
     * hash code. Typically called by persistence framework
     * during save operations.
     *
     * @param _id Long the new type ID
     * @since 2005-01-01
     */
    public void setId(Long _id) {
        this._id = _id;
        this.hashCode = Integer.MIN_VALUE;
    }

    /**
     * Gets the name of this functional user type.
     *
     * Returns the descriptive label for this role category.
     * Used for display and selection in user interfaces.
     *
     * @return String the type name
     * @since 2005-01-01
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the name of this functional user type.
     *
     * Defines the descriptive label for this role category.
     * Should be clear and reflect the functional responsibilities.
     *
     * @param _name String the type name
     * @since 2005-01-01
     */
    public void setName(String _name) {
        this._name = _name;
    }

    /**
     * Compares this FunctionalUserType with another object for equality.
     *
     * Two FunctionalUserType objects are considered equal if they have
     * the same ID value. Null IDs are considered unequal.
     *
     * @param obj Object the object to compare with
     * @return boolean true if objects are equal, false otherwise
     * @since 2005-01-01
     */
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof FunctionalUserType)) return false;
        else {
            FunctionalUserType mObj = (FunctionalUserType) obj;
            if (null == this.getId() || null == mObj.getId()) return false;
            else return (this.getId().equals(mObj.getId()));
        }
    }

    /**
     * Generates a hash code for this FunctionalUserType.
     *
     * Uses cached hash code for performance. The hash is based on
     * the class name and ID value. Invalidated when ID changes.
     *
     * @return int the hash code value
     * @since 2005-01-01
     */
    public int hashCode() {
        if (Integer.MIN_VALUE == this.hashCode) {
            if (null == this.getId()) return super.hashCode();
            else {
                String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
                this.hashCode = hashStr.hashCode();
            }
        }
        return this.hashCode;
    }

    /**
     * Returns a string representation of this FunctionalUserType.
     *
     * Currently delegates to the superclass implementation.
     * Override in subclasses for custom string representation.
     *
     * @return String the string representation
     * @since 2005-01-01
     */
    public String toString() {
        return super.toString();
    }
}
