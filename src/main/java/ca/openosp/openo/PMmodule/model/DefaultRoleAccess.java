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

import ca.openosp.openo.model.security.Secrole;

import java.io.Serializable;

/**
 * Entity representing default access permissions for security roles.
 *
 * DefaultRoleAccess defines the baseline permissions granted to users
 * with specific security roles. This mapping determines what access types
 * (read, write, delete, etc.) are automatically available to role holders.
 *
 * Key features:
 * - Maps security roles to access types
 * - Provides default permission sets
 * - Supports inheritance of permissions
 * - Enables role-based access control (RBAC)
 *
 * The default access model allows:
 * - Simplified permission management by role
 * - Consistent access control across the system
 * - Override capability at specific resource levels
 * - Audit trail of permission assignments
 *
 * Common role mappings:
 * - Administrator: Full access to all resources
 * - Provider: Read/write access to clinical data
 * - Staff: Limited write, full read access
 * - Auditor: Read-only access to all data
 * - Guest: Minimal read access
 *
 * Access inheritance hierarchy:
 * - System-wide defaults defined here
 * - Program-specific overrides in ProgramAccess
 * - Resource-specific permissions at object level
 *
 * Database mapping:
 * - Table: default_role_access
 * - Primary key: id (auto-generated)
 * - Foreign keys: role_id (secrole), access_id (access_type)
 *
 * @since 2005-01-01
 * @see Secrole
 * @see AccessType
 * @see ProgramAccess
 */
public class DefaultRoleAccess implements Serializable {

    /**
     * Cached hash code for performance optimization.
     * Invalidated when ID changes.
     */
    private int hashCode = Integer.MIN_VALUE;

    /**
     * Primary key for the default role access mapping.
     * Auto-generated unique identifier.
     */
    private Long _id;

    /**
     * Foreign key reference to security role.
     * Links to the role receiving default permissions.
     */
    private long _roleId;

    /**
     * Foreign key reference to access type.
     * Defines the permission being granted.
     */
    private long _accessTypeId;

    /**
     * Security role entity reference.
     * The role receiving the default access permissions.
     * Lazy-loaded for performance optimization.
     */
    private Secrole _caisi_role;

    /**
     * Access type entity reference.
     * The specific permission granted to the role.
     * Lazy-loaded for performance optimization.
     */
    private AccessType _access_type;


    /**
     * Default constructor for creating new DefaultRoleAccess.
     *
     * Creates an empty mapping instance. Role and access type
     * must be set before persisting.
     *
     * @since 2005-01-01
     */
    public DefaultRoleAccess() {
        initialize();
    }

    /**
     * Constructor for primary key initialization.
     *
     * Creates a DefaultRoleAccess with a specific ID.
     * Typically used by persistence framework during
     * object loading from database.
     *
     * @param _id Long the primary key value
     * @since 2005-01-01
     */
    public DefaultRoleAccess(Long _id) {
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
     * Gets the unique identifier of this role access mapping.
     *
     * Returns the primary key from the default_role_access table.
     * Used to uniquely reference this permission assignment.
     *
     * @return Long the mapping ID, null if not persisted
     * @since 2005-01-01
     */
    public Long getId() {
        return _id;
    }

    /**
     * Sets the unique identifier of this role access mapping.
     *
     * Updates the primary key value and invalidates the cached
     * hash code. Typically called by persistence framework
     * during save operations.
     *
     * @param _id Long the new mapping ID
     * @since 2005-01-01
     */
    public void setId(Long _id) {
        this._id = _id;
        this.hashCode = Integer.MIN_VALUE;
    }

    /**
     * Gets the security role ID for this mapping.
     *
     * Returns the foreign key reference to the security role
     * receiving the default permissions.
     *
     * @return long the role ID
     * @since 2005-01-01
     */
    public long getRoleId() {
        return _roleId;
    }

    /**
     * Sets the security role ID for this mapping.
     *
     * Updates the foreign key reference to the security role.
     * Must reference a valid secrole record.
     *
     * @param _roleId long the role ID
     * @since 2005-01-01
     */
    public void setRoleId(long _roleId) {
        this._roleId = _roleId;
    }

    /**
     * Gets the access type ID for this mapping.
     *
     * Returns the foreign key reference to the access type
     * being granted to the role.
     *
     * @return long the access type ID
     * @since 2005-01-01
     */
    public long getAccessTypeId() {
        return _accessTypeId;
    }

    /**
     * Sets the access type ID for this mapping.
     *
     * Updates the foreign key reference to the access type.
     * Must reference a valid access_type record.
     *
     * @param _accessTypeId long the access type ID
     * @since 2005-01-01
     */
    public void setAccessTypeId(long _accessTypeId) {
        this._accessTypeId = _accessTypeId;
    }

    /**
     * Gets the security role entity.
     *
     * Returns the Secrole object associated with this mapping.
     * May be lazy-loaded from the database.
     *
     * @return Secrole the security role entity
     * @since 2005-01-01
     */
    public Secrole getCaisi_role() {
        return this._caisi_role;
    }

    /**
     * Sets the security role entity.
     *
     * Associates a Secrole object with this mapping.
     * Updates the role_id foreign key relationship.
     *
     * @param _caisi_role Secrole the security role entity
     * @since 2005-01-01
     */
    public void setCaisi_role(Secrole _caisi_role) {
        this._caisi_role = _caisi_role;
    }

    /**
     * Gets the access type entity.
     *
     * Returns the AccessType object representing the permission
     * granted to the role. May be lazy-loaded from the database.
     *
     * @return AccessType the access type entity
     * @since 2005-01-01
     */
    public AccessType getAccess_type() {
        return this._access_type;
    }

    /**
     * Sets the access type entity.
     *
     * Associates an AccessType object with this mapping.
     * Updates the access_id foreign key relationship.
     *
     * @param _access_type AccessType the access type entity
     * @since 2005-01-01
     */
    public void setAccess_type(AccessType _access_type) {
        this._access_type = _access_type;
    }

    /**
     * Compares this DefaultRoleAccess with another object for equality.
     *
     * Two DefaultRoleAccess objects are considered equal if they have
     * the same ID value. Null IDs are considered unequal.
     *
     * @param obj Object the object to compare with
     * @return boolean true if objects are equal, false otherwise
     * @since 2005-01-01
     */
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof DefaultRoleAccess)) return false;
        else {
            DefaultRoleAccess mObj = (DefaultRoleAccess) obj;
            if (null == this.getId() || null == mObj.getId()) return false;
            else return (this.getId().longValue() == mObj.getId().longValue());
        }
    }

    /**
     * Generates a hash code for this DefaultRoleAccess.
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
     * Returns a string representation of this DefaultRoleAccess.
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
