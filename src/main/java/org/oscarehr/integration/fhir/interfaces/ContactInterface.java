//CHECKSTYLE:OFF
package org.oscarehr.integration.fhir.interfaces;
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


import org.oscarehr.integration.fhir.resources.constants.ContactRelationship;
import org.oscarehr.integration.fhir.resources.constants.ContactType;

import java.io.Serializable;


/**
 * An interface used for all contact types in Oscar.
 */
public interface ContactInterface extends Serializable {

    void setContactRelationship(ContactRelationship contactRelationship);

    ContactRelationship getContactRelationship();

    void setContactType(ContactType contactType);

    ContactType getContactType();

    Integer getId();

    void setLocationCode(String locationCode);

    String getLocationCode();

    void setFirstName(String firstName);

    String getFirstName();

    void setLastName(String lastName);

    String getLastName();

    void setOrganizationName(String organizationName);

    String getOrganizationName();

    void setAddress(String address);

    String getAddress();

    void setAddress2(String address2);

    String getAddress2();

    void setCity(String city);

    String getCity();

    void setProvince(String province);

    String getProvince();

    void setPostal(String postal);

    String getPostal();

    void setFax(String fax);

    String getFax();

    void setWorkPhone(String workphone);

    String getWorkPhone();

    void setPhone(String phone);

    String getPhone();

    void setProviderCpso(String providerCPSO);

    String getProviderCpso();
}
