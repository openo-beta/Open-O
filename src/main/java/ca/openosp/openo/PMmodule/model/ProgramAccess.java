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

 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;

 import java.util.Set;
 import ca.openosp.openo.model.security.Secrole;

import ca.openosp.openo.commn.model.AbstractModel;

 /**
  * Entity representing program-specific access permissions.
  *
  * ProgramAccess defines granular access control for specific programs,
  * allowing fine-tuned permission management beyond system-wide defaults.
  * This entity maps security roles to access types within the context
  * of individual programs.
  *
  * Key features:
  * - Program-specific permission overrides
  * - Role-based access control (RBAC)
  * - Multiple access types per program
  * - All-roles wildcard support
  * - Many-to-many role associations
  *
  * Access control hierarchy:
  * - System defaults (DefaultRoleAccess)
  * - Program-specific overrides (this entity)
  * - Resource-level permissions
  *
  * Common access patterns:
  * - Grant all nurses read access to program X
  * - Restrict write access to case managers only
  * - Allow all roles to view public programs
  * - Limit delete permissions to supervisors
  *
  * The all_roles flag enables:
  * - Blanket permissions for all users
  * - Public program visibility
  * - Emergency access protocols
  * - Simplified permission management
  *
  * Integration points:
  * - Security authorization checks
  * - Program visibility filtering
  * - Audit trail generation
  * - Access control reports
  *
  * Database mapping:
  * - Table: program_access
  * - Primary key: id (auto-generated)
  * - Foreign keys: program_id, access_type_id
  * - Junction table: program_access_roles for role associations
  *
  * @since 2005-01-01
  * @see Program
  * @see AccessType
  * @see Secrole
  * @see DefaultRoleAccess
  */
 @Entity
 @Table(name = "program_access")
 public class ProgramAccess extends AbstractModel<Long> {

     /**
      * Cached hash code for performance optimization.
      * Invalidated when ID changes.
      */
     private int hashCode = Integer.MIN_VALUE;

     /**
      * Primary key for the program access record.
      * Auto-generated unique identifier.
      */
     @Id
     @Column(name = "id")
     private Long _id;

     /**
      * Foreign key reference to the program.
      * Links this access rule to a specific program.
      */
     @Column(name = "program_id")
     private Long _programId;

     /**
      * Foreign key reference to the access type.
      * Defines what permission is being granted.
      */
     @Column(name = "access_type_id")
     private String _accessTypeId;

     /**
      * Flag indicating if all roles have this access.
      * When true, bypasses role-specific checks.
      */
     @Column(name = "all_roles")
     private boolean _allRoles;

     /**
      * Access type entity reference.
      * The specific permission being granted.
      * Lazy-loaded for performance.
      */
     @Column(name = "access_type")
     private AccessType _accessType;

     /**
      * Collection of roles granted this access.
      * Many-to-many relationship with security roles.
      * Ignored if all_roles is true.
      */
     @Column(name = "roles")
     private Set<Secrole> _roles;

     /**
      * Default constructor for creating new ProgramAccess.
      *
      * Creates an empty access rule. Program ID and access type
      * must be set before persisting.
      *
      * @since 2005-01-01
      */
     public ProgramAccess() {
 
     }
 
     /**
      * Constructor for primary key initialization.
      *
      * Creates a ProgramAccess with a specific ID.
      * Typically used by persistence framework during
      * object loading from database.
      *
      * @param _id Long the primary key value
      * @since 2005-01-01
      */
     public ProgramAccess(Long _id) {
         this.setId(_id);
     }


     /**
      * Gets the unique identifier of this program access rule.
      *
      * Returns the primary key from the program_access table.
      * Used to uniquely reference this access definition.
      *
      * @return Long the access rule ID, null if not persisted
      * @since 2005-01-01
      */
     @Override
     public Long getId() {
         return _id;
     }

     /**
      * Sets the unique identifier of this program access rule.
      *
      * Updates the primary key value and invalidates the cached
      * hash code. Typically called by persistence framework
      * during save operations.
      *
      * @param _id Long the new access rule ID
      * @since 2005-01-01
      */
     public void setId(Long _id) {
         this._id = _id;
         this.hashCode = Integer.MIN_VALUE;
     }

     /**
      * Gets the program identifier for this access rule.
      *
      * Returns the foreign key reference to the program
      * this access rule applies to.
      *
      * @return Long the program ID
      * @since 2005-01-01
      */
     public Long getProgramId() {
         return _programId;
     }

     /**
      * Sets the program identifier for this access rule.
      *
      * Links this access rule to a specific program.
      * Must reference a valid program record.
      *
      * @param _programId Long the program ID
      * @since 2005-01-01
      */
     public void setProgramId(Long _programId) {
         this._programId = _programId;
     }

     /**
      * Gets the access type identifier.
      *
      * Returns the foreign key reference to the type of
      * permission being granted by this rule.
      *
      * @return String the access type ID
      * @since 2005-01-01
      */
     public String getAccessTypeId() {
         return _accessTypeId;
     }

     /**
      * Sets the access type identifier.
      *
      * Defines what permission is being granted.
      * Must reference a valid access_type record.
      *
      * @param _accessTypeId String the access type ID
      * @since 2005-01-01
      */
     public void setAccessTypeId(String _accessTypeId) {
         this._accessTypeId = _accessTypeId;
     }

     /**
      * Checks if all roles have this access permission.
      *
      * When true, this access is granted to all users regardless
      * of their specific role assignments.
      *
      * @return boolean true if all roles have access, false otherwise
      * @since 2005-01-01
      */
     public boolean isAllRoles() {
         return _allRoles;
     }

     /**
      * Sets whether all roles have this access permission.
      *
      * Enables blanket permissions that bypass role-specific checks.
      * Useful for public programs or emergency access protocols.
      *
      * @param _allRoles boolean true to grant access to all roles
      * @since 2005-01-01
      */
     public void setAllRoles(boolean _allRoles) {
         this._allRoles = _allRoles;
     }

     /**
      * Gets the access type entity.
      *
      * Returns the AccessType object representing the permission
      * granted by this rule. May be lazy-loaded from database.
      *
      * @return AccessType the access type entity
      * @since 2005-01-01
      */
     public AccessType getAccessType() {
         return this._accessType;
     }

     /**
      * Sets the access type entity.
      *
      * Associates an AccessType object with this access rule.
      * Updates the access_type_id foreign key relationship.
      *
      * @param _accessType AccessType the access type entity
      * @since 2005-01-01
      */
     public void setAccessType(AccessType _accessType) {
         this._accessType = _accessType;
     }

     /**
      * Gets the roles that have this access permission.
      *
      * Returns the collection of security roles granted this
      * specific access to the program. Ignored if all_roles is true.
      *
      * @return Set<Secrole> the collection of roles
      * @since 2005-01-01
      */
     public java.util.Set<Secrole> getRoles() {
         return this._roles;
     }

     /**
      * Sets the roles that have this access permission.
      *
      * Defines which security roles are granted this access.
      * This collection is ignored if all_roles flag is true.
      *
      * @param _roles Set<Secrole> the collection of roles
      * @since 2005-01-01
      */
     public void setRoles(java.util.Set<Secrole> _roles) {
         this._roles = _roles;
     }

     /**
      * Adds a role to the collection of roles with this access.
      *
      * Convenience method for granting access to an additional role.
      * Initializes the collection if necessary.
      *
      * @param role Secrole the role to grant access to
      * @since 2005-01-01
      */
     public void addToRoles(Secrole role) {
         if (null == this._roles) {
             this._roles = new java.util.HashSet<Secrole>();
         }
         this._roles.add(role);
     }

     /**
      * Compares this ProgramAccess with another object for equality.
      *
      * Two ProgramAccess objects are considered equal if they have
      * the same ID value. Null IDs are considered unequal.
      *
      * @param obj Object the object to compare with
      * @return boolean true if objects are equal, false otherwise
      * @since 2005-01-01
      */
     @Override
     public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof ProgramAccess)) return false;
        else {
            ProgramAccess mObj = (ProgramAccess) obj;
            if (null == this.getId() || null == mObj.getId()) return false;
            else return (this.getId().equals(mObj.getId()));
        }
    }

    /**
     * Generates a hash code for this ProgramAccess.
     *
     * Uses cached hash code for performance. The hash is based on
     * the class name and ID value. Invalidated when ID changes.
     *
     * @return int the hash code value
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
     * Returns a string representation of this ProgramAccess.
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
