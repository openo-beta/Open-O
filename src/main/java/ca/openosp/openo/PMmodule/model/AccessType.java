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
 * Entity representing types of access permissions in the Program Management module.
 *
 * This class maps to the access_type table and defines the various types of
 * access that can be granted to users for programs and services. Access types
 * control what operations users can perform within the PMmodule system.
 *
 * Access types typically include:
 * - Read: View-only access to program information
 * - Write: Ability to modify program data
 * - Delete: Permission to remove records
 * - Execute: Ability to perform specific actions
 * - Admin: Full administrative control
 *
 * The access type system works in conjunction with:
 * - ProgramAccess: Links access types to specific programs
 * - DefaultRoleAccess: Defines default access for roles
 * - ProgramProvider: Associates providers with programs
 * - SecUserRole: Maps users to security roles
 *
 * Key features:
 * - Type categorization for grouping related permissions
 * - Named access levels for human-readable permissions
 * - Extensible design for custom access types
 * - Integration with role-based access control (RBAC)
 *
 * Database structure:
 * - access_id: Primary key for unique identification
 * - name: Human-readable name of the access type
 * - type: Category or classification of access
 *
 * Security considerations:
 * - Access types should follow principle of least privilege
 * - Regular audits of access type assignments
 * - Careful management of administrative access types
 * - Consider data sensitivity when defining access levels
 *
 * @since 2005-01-01
 * @see ProgramAccess
 * @see DefaultRoleAccess
 */
public class AccessType implements Serializable {

    /**
     * Cached hash code for performance optimization.
     * Initialized to Integer.MIN_VALUE to indicate uncalculated state.
     * Recalculated when the ID changes.
     */
    private int hashCode = Integer.MIN_VALUE;

    /**
     * Primary key from the access_type table.
     * Uniquely identifies this access type in the database.
     * Generated automatically by the database on insert.
     */
    private Long _id;

    /**
     * Human-readable name of the access type.
     * Examples: "Read", "Write", "Delete", "Admin", "Execute".
     * Used for display in user interfaces and logging.
     */
    private String _name;

    /**
     * Category or classification of the access type.
     * Groups related access types together.
     * Examples: "basic", "administrative", "clinical", "reporting".
     */
    private String _type;

    /**
     * Default constructor for creating a new AccessType instance.
     *
     * Creates an empty AccessType object. The initialize() method
     * is called to perform any necessary setup operations.
     *
     * @since 2005-01-01
     */
    public AccessType() {
        initialize();
    }

    /**
     * Constructor for creating an AccessType with a specific ID.
     *
     * Used when loading an existing access type from the database
     * or when the ID is known in advance. The setId method handles
     * hash code invalidation.
     *
     * @param _id Long the unique identifier for this access type
     * @since 2005-01-01
     */
    public AccessType(Long _id) {
        this.setId(_id);
        initialize();
    }


    /**
     * Initializes the AccessType instance.
     *
     * Protected method called by constructors to perform any
     * necessary initialization. Currently empty but preserved
     * for future enhancements and backward compatibility.
     *
     * @since 2005-01-01
     */
    protected void initialize() {
        // Reserved for future initialization logic
    }

    /**
     * Gets the unique identifier of this access type.
     *
     * Returns the primary key value from the access_type table.
     * This ID is used to reference the access type in related tables
     * and for object equality comparisons.
     *
     * Database mapping:
     * - Column: access_id
     * - Generator: native (auto-increment)
     *
     * @return Long the unique identifier, null if not yet persisted
     * @since 2005-01-01
     */
    public Long getId() {
        return _id;
    }

    /**
     * Sets the unique identifier of this access type.
     *
     * Updates the primary key value and invalidates the cached hash code
     * to ensure it's recalculated with the new ID. This method should
     * typically only be called by persistence frameworks.
     *
     * @param _id Long the new unique identifier
     * @since 2005-01-01
     */
    public void setId(Long _id) {
        this._id = _id;
        this.hashCode = Integer.MIN_VALUE;
    }

    /**
     * Gets the name of this access type.
     *
     * Returns the human-readable name that describes what kind of
     * access this type represents. This name is typically displayed
     * in user interfaces and used in audit logs.
     *
     * Examples:
     * - "Read" - View-only access
     * - "Write" - Modification privileges
     * - "Delete" - Removal permissions
     * - "Admin" - Full control
     *
     * @return String the access type name
     * @since 2005-01-01
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the name of this access type.
     *
     * Updates the human-readable name for this access type.
     * The name should be descriptive and consistent with
     * naming conventions used across the system.
     *
     * @param _name String the new name for this access type
     * @since 2005-01-01
     */
    public void setName(String _name) {
        this._name = _name;
    }

    /**
     * Gets the type category of this access type.
     *
     * Returns the classification that groups this access type
     * with related permissions. Used for organizing and filtering
     * access types in administrative interfaces.
     *
     * Common type categories:
     * - "basic" - Standard user permissions
     * - "clinical" - Medical/clinical operations
     * - "administrative" - System administration
     * - "reporting" - Report generation and viewing
     * - "emergency" - Emergency override access
     *
     * @return String the type category
     * @since 2005-01-01
     */
    public String getType() {
        return _type;
    }

    /**
     * Sets the type category of this access type.
     *
     * Updates the classification for this access type.
     * The type should align with established categories
     * used in the system for consistent grouping.
     *
     * @param _type String the new type category
     * @since 2005-01-01
     */
    public void setType(String _type) {
        this._type = _type;
    }

    /**
     * Determines equality based on the unique identifier.
     *
     * Two AccessType objects are considered equal if they have
     * the same non-null ID. This is consistent with database
     * identity where the primary key determines uniqueness.
     *
     * @param obj Object the object to compare with
     * @return boolean true if the objects have the same ID, false otherwise
     * @since 2005-01-01
     */
    @Override
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof AccessType)) return false;
        else {
            AccessType mObj = (AccessType) obj;
            if (null == this.getId() || null == mObj.getId()) return false;
            else return (this.getId().equals(mObj.getId()));
        }
    }

    /**
     * Generates a hash code based on the unique identifier.
     *
     * The hash code is cached for performance and recalculated
     * only when the ID changes. If the ID is null, delegates
     * to the superclass implementation.
     *
     * The hash code is consistent with equals() - objects with
     * the same ID will have the same hash code.
     *
     * @return int the hash code for this access type
     * @since 2005-01-01
     */
    @Override
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
     * Returns a string representation of this access type.
     *
     * Currently delegates to the superclass implementation.
     * Could be enhanced to return a more meaningful representation
     * including the name and type for debugging purposes.
     *
     * @return String the string representation
     * @since 2005-01-01
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
