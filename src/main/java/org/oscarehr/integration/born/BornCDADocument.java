//CHECKSTYLE:OFF
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
package org.oscarehr.integration.born;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Provider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * BORN CDA Document generator using standard XML DOM APIs
 * Replaces MARC SHIC/Everest implementation with HAPI FHIR-compatible approach
 */
public class BornCDADocument {

    public static final String TITLE_A1A2 = "Antenatal Record";
    public static final String TITLE_18M = "Well Baby";
    public static final String TITLE_CSD = "Well Baby CSD";

    public static final String OID_CDA_ONTARIO_EHEALTH = "2.16.840.1.113883.3.239.34.1";
    public static final String OID_CONFIDENTIALITY = "2.16.840.1.113883.5.25";
    public static final String OID_LOINC = "2.16.840.1.113883.6.1";
    public static final String OID_DEMOGRAPHIC_HIN = "2.16.840.1.113883.4.59";
    public static final String OID_DEMOGRAPHIC_GENDER = "2.16.840.1.113883.5.1";
    public static final String OID_DEMOGRAPHIC_MARITAL_STATUS = "2.16.840.1.113883.5.2";
    public static final String OID_DEMOGRAPHIC_ID = "2.16.840.1.113883.3.239.36.1.1.2";

    private Document document;
    private Element root;
    private BORNCDADocumentType type;
    private byte[] nonXmlBody;
    private String nonXmlBodyMediaType;

    public enum BORNCDADocumentType {
        A1A2(1),
        EighteenMonth(2),
        CSD(3);

        private Integer value;

        private BORNCDADocumentType(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    public BornCDADocument(BORNCDADocumentType type, Demographic demographic, List<Provider> providers, 
                          BornHialProperties props, Calendar effectiveDate, String id) throws Exception {
        this.type = type;
        
        // Create the base CDA document structure
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        document = dBuilder.newDocument();
        
        // Create root ClinicalDocument element
        root = document.createElementNS("urn:hl7-org:v3", "ClinicalDocument");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:sdtc", "urn:hl7-org:sdtc");
        document.appendChild(root);
        
        // Add template ID for Ontario eHealth
        addTemplateId(OID_CDA_ONTARIO_EHEALTH);
        
        // Set realm code
        Element realmCode = createElement("realmCode");
        realmCode.setAttribute("code", "CA-ON");
        root.appendChild(realmCode);
        
        // Type ID
        Element typeId = createElement("typeId");
        typeId.setAttribute("root", "2.16.840.1.113883.1.3");
        typeId.setAttribute("extension", "POCD_HD000040");
        root.appendChild(typeId);
        
        // Document ID
        Element idElement = createElement("id");
        idElement.setAttribute("root", props.getIdCodingSystem());
        idElement.setAttribute("extension", id);
        root.appendChild(idElement);
        
        // Document code
        Element code = createElement("code");
        String[] codeInfo = getBORNCode();
        code.setAttribute("code", codeInfo[0]);
        code.setAttribute("codeSystem", codeInfo[1]);
        code.setAttribute("displayName", codeInfo[2]);
        root.appendChild(code);
        
        // Title
        Element title = createElement("title");
        title.setTextContent(getBORNTitle());
        root.appendChild(title);
        
        // Effective time
        Element effectiveTime = createElement("effectiveTime");
        effectiveTime.setAttribute("value", formatDate(effectiveDate.getTime()));
        root.appendChild(effectiveTime);
        
        // Confidentiality code
        Element confidentialityCode = createElement("confidentialityCode");
        confidentialityCode.setAttribute("code", "N");
        confidentialityCode.setAttribute("codeSystem", OID_CONFIDENTIALITY);
        root.appendChild(confidentialityCode);
        
        // Language code
        Element languageCode = createElement("languageCode");
        languageCode.setAttribute("code", "en-CA");
        root.appendChild(languageCode);
        
        // Set ID and version
        Element setId = createElement("setId");
        setId.setAttribute("root", props.getSetIdCodingSystem());
        setId.setAttribute("extension", id);
        root.appendChild(setId);
        
        Element versionNumber = createElement("versionNumber");
        versionNumber.setAttribute("value", "1");
        root.appendChild(versionNumber);
        
        // Add patient (recordTarget)
        addRecordTarget(demographic, props);
        
        // Add authors
        for (Provider provider : providers) {
            addAuthor(provider, props);
        }
        
        // Add custodian
        addCustodian(props);
        
        // Component for nonXMLBody will be added when setNonXmlBody is called
    }
    
    private void addTemplateId(String oid) {
        Element templateId = createElement("templateId");
        templateId.setAttribute("root", oid);
        root.appendChild(templateId);
    }
    
    private void addRecordTarget(Demographic demographic, BornHialProperties props) {
        Element recordTarget = createElement("recordTarget");
        Element patientRole = createElement("patientRole");
        
        // Patient IDs
        Element id1 = createElement("id");
        id1.setAttribute("root", OID_DEMOGRAPHIC_HIN);
        id1.setAttribute("extension", demographic.getHin() != null ? demographic.getHin() : "");
        patientRole.appendChild(id1);
        
        Element id2 = createElement("id");
        id2.setAttribute("root", OID_DEMOGRAPHIC_ID);
        id2.setAttribute("extension", demographic.getDemographicNo().toString());
        patientRole.appendChild(id2);
        
        // Address
        Element addr = createElement("addr");
        addr.setAttribute("use", "HP");
        
        if (demographic.getAddress() != null) {
            Element streetAddressLine = createElement("streetAddressLine");
            streetAddressLine.setTextContent(demographic.getAddress());
            addr.appendChild(streetAddressLine);
        }
        
        if (demographic.getCity() != null) {
            Element city = createElement("city");
            city.setTextContent(demographic.getCity());
            addr.appendChild(city);
        }
        
        if (demographic.getProvince() != null) {
            Element state = createElement("state");
            state.setTextContent(demographic.getProvince());
            addr.appendChild(state);
        }
        
        if (demographic.getPostal() != null) {
            Element postalCode = createElement("postalCode");
            postalCode.setTextContent(demographic.getPostal());
            addr.appendChild(postalCode);
        }
        
        patientRole.appendChild(addr);
        
        // Telecom
        if (demographic.getPhone() != null) {
            Element telecom = createElement("telecom");
            telecom.setAttribute("value", "tel:" + demographic.getPhone().replaceAll("[^0-9+]", ""));
            telecom.setAttribute("use", "HP");
            patientRole.appendChild(telecom);
        }
        
        // Patient
        Element patient = createElement("patient");
        
        // Name
        Element name = createElement("name");
        if (demographic.getFirstName() != null) {
            Element given = createElement("given");
            given.setTextContent(demographic.getFirstName());
            name.appendChild(given);
        }
        if (demographic.getLastName() != null) {
            Element family = createElement("family");
            family.setTextContent(demographic.getLastName());
            name.appendChild(family);
        }
        patient.appendChild(name);
        
        // Gender
        Element administrativeGenderCode = createElement("administrativeGenderCode");
        String genderCode = "UN"; // Undifferentiated
        if ("M".equals(demographic.getSex())) {
            genderCode = "M";
        } else if ("F".equals(demographic.getSex())) {
            genderCode = "F";
        }
        administrativeGenderCode.setAttribute("code", genderCode);
        administrativeGenderCode.setAttribute("codeSystem", OID_DEMOGRAPHIC_GENDER);
        patient.appendChild(administrativeGenderCode);
        
        // Birth time
        if (demographic.getBirthDay() != null) {
            Element birthTime = createElement("birthTime");
            birthTime.setAttribute("value", formatDate(demographic.getBirthDay().getTime()));
            patient.appendChild(birthTime);
        }
        
        patientRole.appendChild(patient);
        recordTarget.appendChild(patientRole);
        root.appendChild(recordTarget);
    }
    
    private void addAuthor(Provider provider, BornHialProperties props) {
        Element author = createElement("author");
        
        // Time
        Element time = createElement("time");
        time.setAttribute("value", formatDate(new Date()));
        author.appendChild(time);
        
        // Assigned author
        Element assignedAuthor = createElement("assignedAuthor");
        
        // Author ID
        Element id = createElement("id");
        id.setAttribute("root", props.getOrganization());
        id.setAttribute("extension", provider.getProviderNo());
        assignedAuthor.appendChild(id);
        
        // Author person
        Element assignedPerson = createElement("assignedPerson");
        Element name = createElement("name");
        
        if (provider.getFirstName() != null) {
            Element given = createElement("given");
            given.setTextContent(provider.getFirstName());
            name.appendChild(given);
        }
        
        if (provider.getLastName() != null) {
            Element family = createElement("family");
            family.setTextContent(provider.getLastName());
            name.appendChild(family);
        }
        
        assignedPerson.appendChild(name);
        assignedAuthor.appendChild(assignedPerson);
        
        // Represented organization
        Element representedOrganization = createElement("representedOrganization");
        Element orgId = createElement("id");
        orgId.setAttribute("root", props.getOrganization());
        representedOrganization.appendChild(orgId);
        
        Element orgName = createElement("name");
        orgName.setTextContent(props.getOrganizationName());
        representedOrganization.appendChild(orgName);
        
        assignedAuthor.appendChild(representedOrganization);
        author.appendChild(assignedAuthor);
        root.appendChild(author);
    }
    
    private void addCustodian(BornHialProperties props) {
        Element custodian = createElement("custodian");
        Element assignedCustodian = createElement("assignedCustodian");
        Element representedCustodianOrganization = createElement("representedCustodianOrganization");
        
        Element id = createElement("id");
        id.setAttribute("root", props.getOrganization());
        representedCustodianOrganization.appendChild(id);
        
        Element name = createElement("name");
        name.setTextContent(props.getOrganizationName());
        representedCustodianOrganization.appendChild(name);
        
        assignedCustodian.appendChild(representedCustodianOrganization);
        custodian.appendChild(assignedCustodian);
        root.appendChild(custodian);
    }
    
    public void setNonXmlBody(byte[] content, String mediaType) {
        this.nonXmlBody = content;
        this.nonXmlBodyMediaType = mediaType;
        
        // Add component with nonXMLBody
        Element component = createElement("component");
        Element nonXMLBody = createElement("nonXMLBody");
        
        Element text = createElement("text");
        text.setAttribute("mediaType", mediaType);
        text.setAttribute("representation", "B64");
        
        // Convert content to base64
        String base64Content = java.util.Base64.getEncoder().encodeToString(content);
        text.setTextContent(base64Content);
        
        nonXMLBody.appendChild(text);
        component.appendChild(nonXMLBody);
        root.appendChild(component);
    }
    
    private Element createElement(String name) {
        return document.createElementNS("urn:hl7-org:v3", name);
    }
    
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }
    
    private String[] getBORNCode() {
        switch (type) {
            case A1A2:
                return new String[]{"57073-4", OID_LOINC, "Prenatal records"};
            case EighteenMonth:
                return new String[]{"11502-2", OID_LOINC, "Laboratory report"};
            case CSD:
                return new String[]{"11502-2", OID_LOINC, "Laboratory report"};
            default:
                return new String[]{"11502-2", OID_LOINC, "Laboratory report"};
        }
    }
    
    private String getBORNTitle() {
        switch (type) {
            case A1A2:
                return TITLE_A1A2;
            case EighteenMonth:
                return TITLE_18M;
            case CSD:
                return TITLE_CSD;
            default:
                return "Clinical Document";
        }
    }
    
    public String toXmlString(boolean formatted) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        if (formatted) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
        
        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        
        transformer.transform(source, result);
        return writer.toString();
    }
    
    public Document getDocument() {
        return document;
    }
}