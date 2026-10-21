/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.integration.fhir.r4.utils;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.codesystems.ContactPointSystem;
import org.hl7.fhir.r4.model.codesystems.IdentifierUse;

import java.util.ArrayList;
import java.util.List;

public final class FhirUtils {

  public static List<String> fhirAddressLineToString(List<Address> addresses) {
    List<String> addressList = null;
    for (Address address : addresses) {
      if (addressList == null) {
        addressList = new ArrayList<>();
      }
      addressList.add(fhirAddressLineToString(address));
    }
    return addressList;
  }

  public static String fhirAddressLineToString(Address address) {
    List<StringType> addressLine = address.getLine();
    String street = "";
    for (StringType line : addressLine) {
      street += line.asStringValue() + " ";
    }
    return street;
  }

  public static String getFhirFax(List<ContactPoint> contactPointList) {
    return loopContactPointList(contactPointList, ContactPointSystem.FAX);
  }

  public static String getFhirPhone(List<ContactPoint> contactPointList) {
    return loopContactPointList(contactPointList, ContactPointSystem.PHONE);
  }

  public static String getFhirEmail(List<ContactPoint> contactPointList) {
    return loopContactPointList(contactPointList, ContactPointSystem.EMAIL);
  }

  private static String loopContactPointList(List<ContactPoint> contactPointList,
      ContactPointSystem contactPointSystem) {
    String contact = "";
    for (ContactPoint contactPoint : contactPointList) {
      contact = getContactPointBySystem(contactPoint, contactPointSystem);
    }
    return contact;
  }

  private static String getContactPointBySystem(
      ContactPoint contactPoint,
      ContactPointSystem contactPointSystem
  ) {
    switch (contactPointSystem) {
      case EMAIL:
      case FAX:
      case PHONE:
        return contactPoint.getValue();
      case NULL:
      case OTHER:
      case PAGER:
      case SMS:
      case URL:
      default:
        return "";
    }
  }

  public static String getFhirOfficialIdentifier(List<Identifier> identifierList) {
    return loopIdentifierList(identifierList, IdentifierUse.OFFICIAL);
  }

  public static String getFhirSecondaryIdentifier(List<Identifier> identifierList) {
    return loopIdentifierList(identifierList, IdentifierUse.SECONDARY);
  }

  private static String loopIdentifierList(List<Identifier> identifierList,
      IdentifierUse identifierUse) {
    String id = "";
    for (Identifier identifier : identifierList) {
      id = getIdentifierByIdentifierUse(identifier, identifierUse);
    }
    return id;
  }

  private static String getIdentifierByIdentifierUse(
      Identifier identifier,
      IdentifierUse identifierUse
  ) {
    switch (identifierUse) {
      case OFFICIAL:
      case SECONDARY:
        return identifier.getValue();
      case NULL:
      case TEMP:
      case USUAL:
      default:
        return "";
    }
  }
}
