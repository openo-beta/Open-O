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

package ca.openosp.openo.web.admin;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import ca.openosp.openo.commn.dao.OscarKeyDao;
import ca.openosp.openo.commn.dao.ProfessionalSpecialistDao;
import ca.openosp.openo.commn.dao.PublicKeyDao;
import ca.openosp.openo.commn.model.OscarKey;
import ca.openosp.openo.commn.model.ProfessionalSpecialist;
import ca.openosp.openo.commn.model.PublicKey;
import ca.openosp.openo.utility.SpringUtils;

/**
 * UI Bean for managing cryptographic keys and security credentials in the OpenO EMR system.
 * This class provides functionality for managing public keys, system keys, and their associations
 * with professional specialists for secure healthcare data exchange and electronic communications.
 *
 * <p>Key management features include:</p>
 * <ul>
 *   <li>Public key retrieval and management for external services</li>
 *   <li>Professional specialist mapping for secure referrals</li>
 *   <li>System key management for OpenO EMR installation</li>
 *   <li>HTML encoding for safe display of key data</li>
 * </ul>
 *
 * <p>This utility is used in administrative interfaces for configuring secure communication
 * channels with external healthcare systems, laboratories, and specialist providers.</p>
 *
 * @since June 16, 2010
 * @see PublicKey
 * @see ProfessionalSpecialist
 * @see OscarKey
 */
public final class KeyManagerUIBean {

    private static final PublicKeyDao publicKeyDao = (PublicKeyDao) SpringUtils.getBean(PublicKeyDao.class);
    private static final OscarKeyDao oscarKeyDao = (OscarKeyDao) SpringUtils.getBean(OscarKeyDao.class);
    private static final ProfessionalSpecialistDao professionalSpecialistDao = (ProfessionalSpecialistDao) SpringUtils.getBean(ProfessionalSpecialistDao.class);

    /**
     * Retrieves all public keys stored in the system for external healthcare service integrations.
     * These keys are used for secure communication with external systems such as laboratories,
     * imaging centers, specialist offices, and other healthcare providers.
     *
     * @return List<PublicKey> all public keys in the system, or empty list if none exist
     */
    public static List<PublicKey> getPublicKeys() {
        return (publicKeyDao.findAll());
    }

    /**
     * Retrieves a specific public key by service name identifier.
     * Service names typically correspond to external healthcare systems or services
     * that require secure communication channels.
     *
     * @param serviceName String the unique identifier for the external service
     * @return PublicKey the public key associated with the service, or null if not found
     */
    public static PublicKey getPublicKey(String serviceName) {
        return (publicKeyDao.find(serviceName));
    }

    /**
     * Retrieves all professional specialists registered in the system.
     * Professional specialists include external healthcare providers such as
     * consultants, radiologists, laboratory directors, and other specialists
     * who may receive secure referrals and communications.
     *
     * @return List<ProfessionalSpecialist> all professional specialists, or empty list if none exist
     */
    public static List<ProfessionalSpecialist> getProfessionalSpecialists() {
        return (professionalSpecialistDao.findAll());
    }

    /**
     * Returns the HTML-escaped service name identifier for safe display in web interfaces.
     * This prevents XSS attacks when displaying service names in administrative screens.
     *
     * @param publicKey PublicKey the public key whose service name should be escaped
     * @return String HTML-escaped service name identifier
     */
    public static String getSericeNameEscaped(PublicKey publicKey) {
        return (StringEscapeUtils.escapeHtml(publicKey.getId()));
    }

    /**
     * Returns a formatted, HTML-escaped display string showing service name and type.
     * This provides a user-friendly representation of the public key service including
     * both the identifier and the key type for administrative interfaces.
     *
     * @param publicKey PublicKey the public key to format for display
     * @return String formatted display string in format "serviceName (keyType)"
     */
    public static String getSericeDisplayString(PublicKey publicKey) {
        return (StringEscapeUtils.escapeHtml(publicKey.getId() + " (" + publicKey.getType() + ')'));
    }

    /**
     * Returns a formatted, HTML-escaped display string for professional specialist information.
     * This provides a standardized format for displaying specialist names and IDs in
     * administrative interfaces and selection lists.
     *
     * @param professionalSpecialist ProfessionalSpecialist the specialist to format for display
     * @return String formatted display string in format "LastName, FirstName (ID)"
     */
    public static String getProfessionalSpecialistDisplayString(ProfessionalSpecialist professionalSpecialist) {
        return (StringEscapeUtils.escapeHtml(professionalSpecialist.getLastName() + ", " + professionalSpecialist.getFirstName() + " (" + professionalSpecialist.getId() + ')'));
    }

    /**
     * Updates the professional specialist association for a public key service.
     * This creates a mapping between external services and internal specialist records,
     * enabling automatic routing of referrals and secure communications to the
     * appropriate healthcare providers.
     *
     * @param serviceName String the unique identifier for the external service
     * @param matchingProfessionalSpecialistId Integer the ID of the specialist to associate
     * @throws RuntimeException if the service name is not found
     */
    public static void updateMatchingProfessionalSpecialist(String serviceName, Integer matchingProfessionalSpecialistId) {
        PublicKey publicKey = publicKeyDao.find(serviceName);
        publicKey.setMatchingProfessionalSpecialistId(matchingProfessionalSpecialistId);
        publicKeyDao.merge(publicKey);
    }

    /**
     * Retrieves the HTML-escaped public key for this OpenO EMR installation.
     * This system-wide public key is used for secure communication with other
     * OpenO EMR installations and external healthcare systems that require
     * cryptographic verification of this system's identity.
     *
     * @return String HTML-escaped public key for this OpenO EMR installation, or empty string if not configured
     */
    public static String getPublicOscarKeyEscaped() {
        OscarKey oscarKey = oscarKeyDao.find("oscar");

        if (oscarKey == null) return ("");

        return (StringEscapeUtils.escapeHtml(oscarKey.getPublicKey()));
    }
}
