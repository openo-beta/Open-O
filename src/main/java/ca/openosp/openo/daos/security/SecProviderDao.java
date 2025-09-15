//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 * Contributors:
 * <Quatro Group Software Systems inc.>  <OSCAR Team>
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package ca.openosp.openo.daos.security;

import java.util.List;

import ca.openosp.openo.model.security.SecProvider;

/**
 * Data Access Object interface for managing security provider records in the OpenO EMR system.
 * <p>
 * This interface defines the contract for CRUD operations and queries on SecProvider entities,
 * which represent healthcare providers (doctors, nurses, administrative staff, etc.) who have
 * access to the EMR system. Each provider has authentication credentials and associated security
 * roles that determine their access privileges within the system.
 * </p>
 * <p>
 * The SecProvider entity is central to the security architecture, linking providers to their
 * roles, privileges, and organizational units. This DAO provides comprehensive search
 * capabilities to support various administrative and operational requirements.
 * </p>
 * <p>
 * Property constants are provided for consistent field references across the application,
 * reducing the risk of typos and facilitating refactoring. These constants should be used
 * when calling property-based query methods.
 * </p>
 *
 * @since 2005-01-01
 * @see ca.openosp.openo.model.security.SecProvider
 * @see ca.openosp.openo.daos.security.SecuserroleDao
 */
public interface SecProviderDao {

    /** Property name constant for provider's last name field */
    public static final String LAST_NAME = "lastName";
    /** Property name constant for provider's first name field */
    public static final String FIRST_NAME = "firstName";
    /** Property name constant for provider type (doctor, nurse, admin, etc.) */
    public static final String PROVIDER_TYPE = "providerType";
    /** Property name constant for medical specialty */
    public static final String SPECIALTY = "specialty";
    /** Property name constant for team assignment */
    public static final String TEAM = "team";
    /** Property name constant for provider's sex/gender */
    public static final String SEX = "sex";
    /** Property name constant for provider's address */
    public static final String ADDRESS = "address";
    /** Property name constant for provider's phone number */
    public static final String PHONE = "phone";
    /** Property name constant for provider's work phone number */
    public static final String WORK_PHONE = "workPhone";
    /** Property name constant for Ontario Health Insurance Plan number */
    public static final String OHIP_NO = "ohipNo";
    /** Property name constant for RMA (Regional Medical Association) number */
    public static final String RMA_NO = "rmaNo";
    /** Property name constant for billing identification number */
    public static final String BILLING_NO = "billingNo";
    /** Property name constant for Health Service Organization number */
    public static final String HSO_NO = "hsoNo";
    /** Property name constant for provider status (active/inactive) */
    public static final String STATUS = "status";
    /** Property name constant for administrative comments */
    public static final String COMMENTS = "comments";
    /** Property name constant for provider activity tracking */
    public static final String PROVIDER_ACTIVITY = "providerActivity";

    /**
     * Persists a new provider record to the database.
     *
     * @param transientInstance SecProvider the new provider entity to save
     * @throws org.hibernate.HibernateException if persistence fails
     */
    public void save(SecProvider transientInstance);

    /**
     * Persists or updates a provider record based on its state.
     * <p>
     * If the provider doesn't exist (transient), it will be inserted.
     * If it exists (persistent/detached), it will be updated.
     * </p>
     *
     * @param transientInstance SecProvider the provider entity to save or update
     * @throws org.hibernate.HibernateException if persistence fails
     */
    public void saveOrUpdate(SecProvider transientInstance);

    /**
     * Removes a provider record from the database.
     * <p>
     * Note: This physically deletes the record. Consider using status-based
     * deactivation for audit trail preservation.
     * </p>
     *
     * @param persistentInstance SecProvider the provider entity to delete
     * @throws org.hibernate.HibernateException if deletion fails
     */
    public void delete(SecProvider persistentInstance);

    /**
     * Retrieves a provider by their unique identifier.
     *
     * @param id String the provider's unique ID (provider number)
     * @return SecProvider the provider entity, or null if not found
     */
    public SecProvider findById(java.lang.String id);

    /**
     * Retrieves a provider by ID and status.
     * <p>
     * Useful for finding only active providers or providers with specific status.
     * </p>
     *
     * @param id String the provider's unique ID
     * @param status String the provider status to filter by (e.g., "1" for active)
     * @return SecProvider the matching provider, or null if not found
     */
    public SecProvider findById(java.lang.String id, String status);

    /**
     * Finds providers matching the given example instance.
     *
     * @param instance SecProviderDao example instance with properties to match
     * @return List list of matching SecProvider entities
     */
    public List findByExample(SecProviderDao instance);

    /**
     * Generic property-based search method.
     *
     * @param propertyName String the property name to search by
     * @param value Object the value to match
     * @return List list of matching SecProvider entities
     */
    public List findByProperty(String propertyName, Object value);

    /**
     * Finds providers by last name.
     *
     * @param lastName Object the last name to search for
     * @return List list of providers with matching last name
     */
    public List findByLastName(Object lastName);

    /**
     * Finds providers by first name.
     *
     * @param firstName Object the first name to search for
     * @return List list of providers with matching first name
     */
    public List findByFirstName(Object firstName);

    /**
     * Finds providers by type (doctor, nurse, admin, etc.).
     *
     * @param providerType Object the provider type to search for
     * @return List list of providers with matching type
     */
    public List findByProviderType(Object providerType);

    /**
     * Finds providers by medical specialty.
     *
     * @param specialty Object the specialty to search for
     * @return List list of providers with matching specialty
     */
    public List findBySpecialty(Object specialty);

    /**
     * Finds providers by team assignment.
     *
     * @param team Object the team identifier to search for
     * @return List list of providers in the specified team
     */
    public List findByTeam(Object team);

    /**
     * Finds providers by sex/gender.
     *
     * @param sex Object the sex value to search for
     * @return List list of providers with matching sex
     */
    public List findBySex(Object sex);

    /**
     * Finds providers by address.
     *
     * @param address Object the address to search for
     * @return List list of providers with matching address
     */
    public List findByAddress(Object address);

    /**
     * Finds providers by phone number.
     *
     * @param phone Object the phone number to search for
     * @return List list of providers with matching phone
     */
    public List findByPhone(Object phone);

    /**
     * Finds providers by work phone number.
     *
     * @param workPhone Object the work phone to search for
     * @return List list of providers with matching work phone
     */
    public List findByWorkPhone(Object workPhone);

    /**
     * Finds providers by OHIP (Ontario Health Insurance Plan) number.
     *
     * @param ohipNo Object the OHIP number to search for
     * @return List list of providers with matching OHIP number
     */
    public List findByOhipNo(Object ohipNo);

    /**
     * Finds providers by RMA (Regional Medical Association) number.
     *
     * @param rmaNo Object the RMA number to search for
     * @return List list of providers with matching RMA number
     */
    public List findByRmaNo(Object rmaNo);

    /**
     * Finds providers by billing number.
     *
     * @param billingNo Object the billing number to search for
     * @return List list of providers with matching billing number
     */
    public List findByBillingNo(Object billingNo);

    /**
     * Finds providers by HSO (Health Service Organization) number.
     *
     * @param hsoNo Object the HSO number to search for
     * @return List list of providers with matching HSO number
     */
    public List findByHsoNo(Object hsoNo);

    /**
     * Finds providers by status (active/inactive).
     *
     * @param status Object the status to search for ("1" = active, "0" = inactive)
     * @return List list of providers with matching status
     */
    public List findByStatus(Object status);

    /**
     * Finds providers by comments field.
     *
     * @param comments Object the comments text to search for
     * @return List list of providers with matching comments
     */
    public List findByComments(Object comments);

    /**
     * Finds providers by activity status.
     *
     * @param providerActivity Object the activity status to search for
     * @return List list of providers with matching activity
     */
    public List findByProviderActivity(Object providerActivity);

    /**
     * Retrieves all provider records from the database.
     * <p>
     * Use with caution in large installations as this may return many records.
     * </p>
     *
     * @return List list of all SecProvider entities
     */
    public List findAll();

    /**
     * Merges a detached provider instance with the current session.
     *
     * @param detachedInstance SecProviderDao the detached instance to merge
     * @return SecProviderDao the merged instance
     */
    public SecProviderDao merge(SecProviderDao detachedInstance);

    /**
     * Attaches a modified provider instance to the session.
     *
     * @param instance SecProviderDao the modified instance to attach
     */
    public void attachDirty(SecProviderDao instance);

    /**
     * Attaches an unmodified provider instance to the session.
     *
     * @param instance SecProviderDao the clean instance to attach
     */
    public void attachClean(SecProviderDao instance);
}
