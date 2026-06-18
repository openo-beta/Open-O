//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

/*
 * OLISHL7Handler.java
 */

package ca.openosp.openo.lab.ca.all.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.Misc;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import ca.openosp.openo.olis.dao.OLISRequestNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISResultNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISFacilityDao;
import ca.openosp.openo.olis.dao.OLISMicroorganismNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISSourceNomenclatureDao;
import ca.openosp.openo.olis.model.OLISSourceNomenclature;
import ca.openosp.openo.olis.model.OLISFacility;
import ca.openosp.openo.olis.model.OLISMicroorganismNomenclature;
import ca.openosp.openo.olis.model.OLISRequestNomenclature;
import ca.openosp.openo.olis.model.OLISResultNomenclature;
import ca.openosp.openo.olis.model.OlisLabChildResultSortable;
import ca.openosp.openo.olis.model.OlisLabRequestSortable;
import ca.openosp.openo.olis.model.OlisLabResultSortable;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.util.UtilDateUtilities;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.GenericComposite;
import ca.uhn.hl7v2.model.GenericMessage;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * @author Adam Balanga
 */
public class OLISHL7Handler implements MessageHandler {

    /**
     * Structured doctor-name data parsed from an HL7 CN/XCN composite field
     * (OBR-16 ordering, PV1-7 attending, PV1-17 admitting, OBR-28 cc-doctors).
     *
     * <p>Replaces the older {@code getFullDocName} return shape, which synthesized
     * inline {@code <span style="...">{licenseType} {licenseNumber}</span>} markup
     * that every downstream renderer (PDF, JSPs, InboxHub) then had to parse back
     * out via Jsoup to get plain text. The structured form lets each renderer
     * decide its own presentation: PDF subscript fonts, JSP CSS classes, plain
     * text for inbox lists, etc.</p>
     */
    public static class DoctorName {
        // XCN-6: "DR."
        public final String prefix;
        // XCN-3: "JOHN"
        public final String givenName;
        // XCN-4: middle initial
        public final String middleName;
        // XCN-2: "SMITH"
        public final String familyName;
        // XCN-5: suffix
        public final String suffix;
        // XCN-7: "M.D." degree credential text
        public final String degree;
        // XCN-13 normalized: "MD" / "RM" / "RN(EC)" / "DDS"
        public final String licenseType;
        // XCN-1: "109753"
        public final String licenseNumber;
        // XCN-22-2: licensing jurisdiction (CT 4.4/16.4/17.4/18.4) — only set for a
        // non-Ontario Canadian jurisdiction, e.g. "MB"; empty otherwise.
        public final String jurisdiction;

        public DoctorName(String prefix, String givenName, String middleName,
                          String familyName, String suffix, String degree,
                          String licenseType, String licenseNumber) {
            this(prefix, givenName, middleName, familyName, suffix, degree,
                    licenseType, licenseNumber, "");
        }

        public DoctorName(String prefix, String givenName, String middleName,
                          String familyName, String suffix, String degree,
                          String licenseType, String licenseNumber, String jurisdiction) {
            this.prefix = nullToEmpty(prefix);
            this.givenName = nullToEmpty(givenName);
            this.middleName = nullToEmpty(middleName);
            this.familyName = nullToEmpty(familyName);
            this.suffix = nullToEmpty(suffix);
            this.degree = nullToEmpty(degree);
            this.licenseType = nullToEmpty(licenseType);
            this.licenseNumber = nullToEmpty(licenseNumber);
            this.jurisdiction = nullToEmpty(jurisdiction);
        }

        /** True when every parsed field is empty — caller can short-circuit rendering. */
        public boolean isEmpty() {
            return prefix.isEmpty() && givenName.isEmpty() && middleName.isEmpty()
                    && familyName.isEmpty() && suffix.isEmpty() && degree.isEmpty()
                    && licenseType.isEmpty() && licenseNumber.isEmpty() && jurisdiction.isEmpty();
        }

        /**
         * Name part without the license — what a renderer puts in the main font
         * (e.g. {@code "DR. JOHN SMITH M.D."}).
         */
        public String getNamePart() {
            StringBuilder sb = new StringBuilder();
            appendWithSpace(sb, prefix);
            appendWithSpace(sb, givenName);
            appendWithSpace(sb, middleName);
            appendWithSpace(sb, familyName);
            appendWithSpace(sb, suffix);
            appendWithSpace(sb, degree);
            return sb.toString();
        }

        /**
         * License credential part — {@code "MD 109753"} or {@code ""} if both
         * licenseType and licenseNumber are empty. Renderers put this in the
         * subscript / small-grey font.
         */
        public String getLicensePart() {
            if (licenseType.isEmpty() && licenseNumber.isEmpty() && jurisdiction.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            appendWithSpace(sb, licenseType);
            appendWithSpace(sb, licenseNumber);
            // CT 4.4/16.4/17.4/18.4: show the licensing jurisdiction for a non-Ontario
            // Canadian jurisdiction (already filtered to that case at parse time).
            if (!jurisdiction.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append('(').append(jurisdiction).append(')');
            }
            return sb.toString();
        }

        /**
         * Full plain-text rendering — name + license joined by a single space.
         * Replaces the historical span-laden output of {@code getFullDocName}.
         */
        public String toPlainText() {
            String name = getNamePart();
            String license = getLicensePart();
            if (name.isEmpty()) return license;
            if (license.isEmpty()) return name;
            return name + " " + license;
        }

        private static String nullToEmpty(String s) {
            return s == null ? "" : s;
        }

        private static void appendWithSpace(StringBuilder sb, String s) {
            if (s == null || s.isEmpty()) return;
            if (sb.length() > 0) sb.append(' ');
            sb.append(s);
        }
    }

    /**
     * Forces every parsed message to resolve as {@link GenericMessage} regardless of MSH-9-3.
     *
     * <p>HAPI's typed structures (e.g. {@code ORU_R01}) nest non-standard Z-segments like
     * {@code ZPD}/{@code ZBR} inside group children, which this handler's
     * {@code terser.getFinder().getRoot().getNames()} segment iteration never sees.
     * Real OLIS query responses are {@code ERP^Znn^ERP_R09} — no HAPI structure class exists,
     * so they already fall back to {@code GenericMessage} (flat layout at root). This factory
     * removes the dependency on that accident so synthetic ORU fixtures, and any future typed
     * OLIS response, also parse flat.</p>
     */
    private static final ModelClassFactory FLAT_MODEL_FACTORY = new DefaultModelClassFactory() {
        @Override
        public Class<? extends Message> getMessageClass(String name, String version, boolean isExplicit)
                throws HL7Exception {
            return GenericMessage.getGenericMessageClass(version);
        }
    };

    Logger logger = MiscUtils.getLogger();
    protected boolean isFinal = true;
    protected boolean isCorrected = false;
    protected Message msg = null;
    protected Terser terser;
    protected ArrayList<ArrayList<Segment>> obrGroups = null;

    // Microorganism codes (OBX-5.1, coding system HL79905) collected during parse, and the
    // catalog map resolving them to organism names for coded micro results (CV06).
    private List<String> microorganismCodes = new ArrayList<String>();
    private Map<String, OLISMicroorganismNomenclature> olisMicroorganismNomenclatureMap = new HashMap<String, OLISMicroorganismNomenclature>();
    private ArrayList<String> obrSpecimenSource;
    private ArrayList<String> obrSiteModifier;
    private ArrayList<String> obrStatus;
    private ArrayList<Character> obrStatusCode;
    private HashMap<String, String> sourceOrganizations;

    private HashMap<String, String> defaultSourceOrganizations;

    private void initDefaultSourceOrganizations() {
        defaultSourceOrganizations = new HashMap<String, String>();
        defaultSourceOrganizations.put("4001", "BSD Lab1");
        defaultSourceOrganizations.put("4002", "BSD Lab2");
        defaultSourceOrganizations.put("4003", "BSD Lab3");
        defaultSourceOrganizations.put("4004", "BSD Lab4");
        defaultSourceOrganizations.put("4005", "BSD Lab5");
        defaultSourceOrganizations.put("4006", "BSD Lab6");
        defaultSourceOrganizations.put("4007", "BSD Lab7");
        defaultSourceOrganizations.put("4008", "BSD Lab8");
        defaultSourceOrganizations.put("4009", "BSD Lab9");
        defaultSourceOrganizations.put("4010", "BSD Lab10");
    }

    public String getSourceOrganization(String org) {
        return sourceOrganizations.containsKey(org) ? sourceOrganizations.get(org) : defaultSourceOrganizations.get(org);
    }

    public String getObrStatus(int index) {
        if (obrStatus == null || index < 0 || index >= obrStatus.size()) return "";
        return obrStatus.get(index);
    }

    /** Red parenthetical to show adjacent to the test request name for this OBR's
     *  status (CT 10.2.x), e.g. "(test was cancelled)"; "" when none applies. */
    public String getObrStatusRedText(int index) {
        if (obrStatusCode == null || index < 0 || index >= obrStatusCode.size()) return "";
        return getTestRequestStatusRedText(obrStatusCode.get(index));
    }

    public String getObrSpecimenSource(int index) {
        if (obrSpecimenSource == null || index < 0 || index >= obrSpecimenSource.size()) return "";
        return obrSpecimenSource.get(index);
    }

    /**
     * Resolve the displayable specimen type for a test request (CT 9.4). Prefers
     * the OLIS Source-nomenclature description looked up by the specimen source
     * code (OBR-15-1-1); falls back to the lab-supplied text (OBR-15-1-2), then to
     * the raw code. The catalog lookup is best-effort: if the Source catalog table
     * is not present (the seed/migration is human-applied), the lookup is skipped
     * and the lab text is used, so this never regresses existing display.
     *
     * @param code String the specimen source code (OBR-15-1-1), may be empty
     * @param text String the lab-supplied specimen text (OBR-15-1-2), may be empty
     * @return String the specimen type to display, never {@code null}
     */
    private String resolveSpecimenType(String code, String text) {
        String c = StringUtils.trimToEmpty(code);
        if (!c.isEmpty()) {
            try {
                OLISSourceNomenclatureDao sourceDao = SpringUtils.getBean(OLISSourceNomenclatureDao.class);
                OLISSourceNomenclature entry = sourceDao.findByValue(c);
                if (entry != null) {
                    String description = StringUtils.trimToEmpty(entry.getDescription());
                    if (!description.isEmpty()) {
                        return description;
                    }
                }
            } catch (Exception e) {
                // Source catalog absent (table not yet loaded) — fall back to lab text.
                logger.debug("OLIS Source nomenclature lookup unavailable; using lab specimen text");
            }
        }
        String t = StringUtils.trimToEmpty(text);
        return !t.isEmpty() ? t : c;
    }

    /** Site modifier (OBR-15-5-2) for the test request, displayed under its own
     *  "Site Modifier" label (CT 9.5); empty when not provided. */
    public String getSiteModifier(int index) {
        if (obrSiteModifier == null || index < 0 || index >= obrSiteModifier.size()) return "";
        return obrSiteModifier.get(index);
    }

    private ArrayList<String> headers = null;

    /**
     * Creates a new instance of OLISHL7Handler
     */
    public OLISHL7Handler() {
        super();
    }

    String[] getDentistLicenceNumber() {
        return patientIdentifiers.get("DDSL");
    }

    String[] getDriversLicenceNumber() {
        return patientIdentifiers.get("DL");
    }

    String[] getJurisdictionalHealthNumber() {
        return patientIdentifiers.get("JHN");
    }

    String[] getPhysicianLicenceNumber() {
        return patientIdentifiers.get("MDL");
    }

    String[] getMidwifeLicenceNumber() {
        return patientIdentifiers.get("ML");
    }

    String[] getMedicalRecordNumber() {
        return patientIdentifiers.get("MR");
    }

    String[] getNursePractitionerLicenceNumber() {
        return patientIdentifiers.get("NPL");
    }

    String[] getPassportNumber() {
        return patientIdentifiers.get("PPN");
    }

    String[] getUSASocialSecurityNumber() {
        return patientIdentifiers.get("SS");
    }

    public String[] getPatientIdentifier(String ident) {
        return patientIdentifiers.get(ident);
    }

    public Set<String> getPatientIdentifiers() {
        return patientIdentifiers.keySet();
    }

    public String getNameOfIdentifier(String ident) {
        return patientIdentifierNames.get(ident);
    }

    HashMap<String, String[]> patientIdentifiers;
    HashMap<String, String> patientIdentifierNames;

    private void initPatientIdentifierNames() {
        patientIdentifierNames.put("ANON", "Non Nominal Identifier");
        patientIdentifierNames.put("DDSL", "Dentist Licence Number");
        patientIdentifierNames.put("DL", "Driver's Licence Number");
        patientIdentifierNames.put("JHN", "Jurisdictional Health Number");
        patientIdentifierNames.put("MDL", "Physician Licence Number");
        patientIdentifierNames.put("ML", "Midwife Licence Number");
        patientIdentifierNames.put("MR", "Medical Record Number");
        patientIdentifierNames.put("NPL", "Nurse Practitioner Licence Number");
        patientIdentifierNames.put("PPN", "Passport Number");
        patientIdentifierNames.put("SS", "USA Social Security number");

    }

    private HashMap<String, String> addressTypeNames;
    private HashMap<String, String> telecomUseCode;
    private HashMap<String, String> telecomEquipType;

    public String getAddressTypeName(String ident) {
        return addressTypeNames.get(ident);
    }

    private void initAddressTypeNames() {
        addressTypeNames.put("M", "Mailing Address");
        addressTypeNames.put("B", "Business");
        addressTypeNames.put("O", "Office");
        addressTypeNames.put("H", "Home Address");
        addressTypeNames.put("E", "Emergency Contact");
    }

    private void initTelecomUseCodes() {
        telecomUseCode.put("PRN", "Primary Residence Number");
        telecomUseCode.put("ORN", "Other Residence Number");
        telecomUseCode.put("WPN", "Work Number");
        telecomUseCode.put("VHN", "Vacation Home Number");
        telecomUseCode.put("ASN", "Answering Service Number");
        telecomUseCode.put("EMR", "Emergency Number");
        telecomUseCode.put("NET", "Network (email) Address");
    }

    private void initTelecomEquipTypes() {
        telecomEquipType.put("PH", "Telephone");
        telecomEquipType.put("FX", "Fax");
        telecomEquipType.put("CP", "Cellular Phone");
        telecomEquipType.put("BP", "Beeper");
        telecomEquipType.put("Internet", "Internet Address");
    }

    private ArrayList<HashMap<String, String>> patientAddresses;

    public ArrayList<HashMap<String, String>> getPatientAddresses() {
        return patientAddresses;
    }

    private ArrayList<HashMap<String, String>> patientHomeTelecom;

    public ArrayList<HashMap<String, String>> getPatientHomeTelecom() {
        return patientHomeTelecom;
    }

    public ArrayList<HashMap<String, String>> getPatientWorkTelecom() {
        return patientWorkTelecom;
    }

    private ArrayList<HashMap<String, String>> patientWorkTelecom;

    public String getAdmittingProviderName() {
        try {
            return getFullDocName("/.PV1-17-");
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }

    /** Structured form of {@link #getAdmittingProviderName()} — see {@link #getDocNameStructured()}. */
    public DoctorName getAdmittingProviderStructured() {
        try {
            return getFullDoctorName("/.PV1-17-");
        } catch (Exception e) {
            return new DoctorName("", "", "", "", "", "", "", "");
        }
    }

    public String getAdmittingProviderNameShort() {
        try {
            return getShortName("/.PV1-17-");
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }

    public String getAttendingProviderName() {
        try {
            return getFullDocName("/.PV1-7-");
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }

    /** Structured form of {@link #getAttendingProviderName()} — see {@link #getDocNameStructured()}. */
    public DoctorName getAttendingProviderStructured() {
        try {
            return getFullDoctorName("/.PV1-7-");
        } catch (Exception e) {
            return new DoctorName("", "", "", "", "", "", "", "");
        }
    }

    public boolean reportBlocked = false;

    public Boolean isReportBlocked() {
        return reportBlocked;
    }

    public boolean isOBRBlocked(int obr) {
        obr++;
        try {
            String indicator;
            Segment zbr = null;
            if (obr == 1) {
                zbr = terser.getSegment("/.ZBR");
            } else {
                zbr = (Segment) terser.getFinder().getRoot().get("ZBR" + obr);
            }
            indicator = Terser.get(zbr, 1, 0, 1, 1);
            return "Y".equals(indicator);

        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return false;
    }

    public String getOBRPerformingFacilityName(int obr) {
        obr++;
        try {
            String key = "", value = "", ident = "", rawOid = "";
            Segment zbr = null;
            if (obr == 1) {
                zbr = terser.getSegment("/.ZBR");
            } else {
                zbr = (Segment) terser.getFinder().getRoot().get("ZBR" + obr);
            }
            key = getString(Terser.get(zbr, 6, 0, 6, 2));
            if (key != null && key.indexOf(":") > 0) {
                rawOid = key.substring(0, key.indexOf(":"));
                ident = getOrganizationType(rawOid);
                key = key.substring(key.indexOf(":") + 1);
            }
            if (key == null || "".equals(key.trim())) {
                return "";
            }
            // Enrich the per-test-request performing facility from the local OLIS facility
            // catalog, symmetrically with the report-header getter getPerformingFacilityName.
            // Without this the per-OBR value stays the bare licence and also spuriously
            // differs from the enriched header value, firing the "Performing Lab" exception
            // row (CT Tracker req 5.8.2) even when it is the report's primary performing lab.
            value = catalogFacilityName(rawOid, key, getString(Terser.get(zbr, 6, 0, 1, 1)));
            return String.format("%s (%s %s)", value, ident, key);

        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public HashMap<String, String> getPerformingFacilityAddress(int obr) {
        obr++;
        try {
            String value = "";
            Segment zbr = null;
            if (obr == 1) {
                zbr = terser.getSegment("/.ZBR");
            } else {
                zbr = (Segment) terser.getFinder().getRoot().get("ZBR" + obr);
            }
            HashMap<String, String> address;

            String identifier = getString(Terser.get(zbr, 7, 0, 7, 1));
            if ("".equals(identifier)) {
                return null;
            }
            address = new HashMap<String, String>();
            value = getString(Terser.get(zbr, 7, 0, 1, 1));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Street Address", value);
            }
            value = getString(Terser.get(zbr, 7, 0, 2, 1));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Other Designation", value);
            }
            value = getString(Terser.get(zbr, 7, 0, 3, 1));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("City", value);
            }
            value = getString(Terser.get(zbr, 7, 0, 4, 1));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Province", value);
            }
            value = getString(Terser.get(zbr, 7, 0, 5, 1));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Postal Code", value);
            }
            value = getString(Terser.get(zbr, 7, 0, 6, 1));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Country", value);
            }
            address.put("Address Type", addressTypeNames.get(identifier));
            return address;

        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return null;
    }

    public String getTestResultStatuses() {
        List<String> vals = new ArrayList<String>();

        for (int x = 0; x < getOBRCount(); x++) {
            for (int y = 0; y < getOBXCount(x); y++) {
                String value = getOBXField(x, y, 11, 0, 1);
                if (!vals.contains(value)) {
                    vals.add(value);
                }
            }
        }

        return StringUtils.join(vals, ',');
    }

    public List<String[]> getTestResultInfo() {
        List<String[]> vals = new ArrayList<String[]>();

        for (int x = 0; x < getOBRCount(); x++) {
            for (int y = 0; y < getOBXCount(x); y++) {
                String type = getOBXField(x, y, 2, 0, 1);
                String name = getOBXField(x, y, 3, 0, 1);
                String value = "";
                if (type.equals("SN")) {
                    value = getOBXField(x, y, 5, 0, 1) + getOBXField(x, y, 5, 0, 2);
                } else {
                    value = getOBXField(x, y, 5, 0, 1);
                }

                String units = getOBXField(x, y, 6, 0, 1);
                String abnormal = getOBXField(x, y, 8, 0, 1);
                String status = getOBXField(x, y, 11, 0, 1);
                vals.add(new String[]{name, value, units, abnormal, status});
            }
        }

        return vals;
    }

    public String getTestRequestStatuses() {
        List<String> vals = new ArrayList<String>();

        for (int x = 0; x < getOBRCount(); x++) {

            int i = 1;
            Segment test;
            try {

                test = terser.getSegment("/.OBR");
                while (test != null) {
                    i++;
                    test = (Segment) terser.getFinder().getRoot().get("OBR" + i);
                    if (test != null) {
                        String value = getString(Terser.get(test, 25, 0, 1, 1));
                        if (!vals.contains(value)) {
                            vals.add(value);
                        }
                    }
                }

            } catch (Exception e) {
                // ignore exceptions
            }

        }

        return StringUtils.join(vals, ',');
    }

    public String getCategoryList() {
        return getCategoryList(" / ");
    }

    public String getCategoryList(String delimeter) {
        String result = "";
        ArrayList<String> categories = new ArrayList<String>();
        for (int i = 0; i < getOBRCount(); i++) {
            categories.add(getOBRCategory(i));
        }
        String[] uniqueCategories = new HashSet<String>(categories).toArray(new String[0]);
        Arrays.sort(uniqueCategories);
        int count = 0;
        for (String category : uniqueCategories) {
            result += (count++ > 0 ? delimeter : "") + category;
        }
        return result;
    }

    /* Test Request Name */
    public String getTestList() {
        return getTestList(" / ");
    }

    public String getTestList(String delimeter) {
        String result = "";
        String[] uniqueTests = new HashSet<String>(headers).toArray(new String[0]);
        Arrays.sort(uniqueTests);
        int count = 0;
        for (String test : uniqueTests) {
            result += (count++ > 0 ? delimeter : "") + test;
        }
        return result;
    }


    /*
    Return the sending lab in the format of 2.16.840.1.113883.3.59.1:9999 where 9999 is the lab identifier

     5047 Canadian Medical Laboratories
     5552 Gamma Dynacare
     5687 LifeLabs
     5254 Alpha Laboratories
     */
    public String getPlacerGroupNumber() {
        try {
            String value = getString(terser.get("/.ORC-4-3"));
            return value;
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return null;
    }

    public String getPerformingFacilityNameOnly() {
        try {
            String value = getString(terser.get("/.ZBR-6-1"));
            return value;
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public String getPerformingFacilityName() {
        try {
            String key = "", value = "", ident = "", rawOid = "";
            key = getString(terser.get("/.ZBR-6-6-2"));
            if (key != null && key.indexOf(":") > 0) {
                rawOid = key.substring(0, key.indexOf(":"));
                ident = getOrganizationType(rawOid);
                key = key.substring(key.indexOf(":") + 1);
            } else {
                key = "";
            }
            if (key == null || key.trim().equals("")) {
                return "";
            }
            // Enrich the facility name from the local OLIS facility catalog when the
            // message carries only the licence (a bare id like "5552"); fall back to the
            // raw ZBR name. Informed by oscarpro's reporting-facility enrichment, applied
            // symmetrically to performing + reporting.
            value = catalogFacilityName(rawOid, key, getString(terser.get("/.ZBR-6-1")));

            return String.format("%s (%s %s)", value, ident, key);
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * Returns the OLIS facility catalog name for a licence when a seeded
     * {@link OLISFacility} matches, otherwise the supplied raw name. Used to
     * resolve a performing/reporting facility that the HL7 carries by licence only.
     *
     * @param licence String the facility licence parsed from the ZBR identifier
     * @param rawName String the facility name from the HL7 (fallback)
     * @return String the catalog name if matched + non-empty, else {@code rawName}
     */
    private String catalogFacilityName(String oid, String licence, String rawName) {
        if (licence != null && !licence.trim().isEmpty()) {
            try {
                OLISFacilityDao facilityDao = SpringUtils.getBean(OLISFacilityDao.class);
                // Prefer the OID+licence match (disambiguates LAB/SCC/HOS, which share the
                // licence space); fall back to licence-only when the OID is absent/unmatched.
                OLISFacility matched = facilityDao.findByOidAndLicence(oid, licence.trim());
                if (matched == null) {
                    matched = facilityDao.findByLicenceNumber(licence.trim());
                }
                if (matched != null && stringIsNotNullOrEmpty(matched.getName())) {
                    return matched.getName();
                }
            } catch (Exception e) {
                MiscUtils.getLogger().error("OLIS HL7 Error", e);
            }
        }
        return rawName;
    }

    public HashMap<String, String> getPerformingFacilityAddress() {
        try {
            String value;
            HashMap<String, String> address;
            String identifier = getString(terser.get("/.ZBR-7-7"));
            if ("".equals(identifier)) {
                return null;
            }
            address = new HashMap<String, String>();
            value = getString(terser.get("/.ZBR-7-1"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Street Address", value);
            }
            value = getString(terser.get("/.ZBR-7-2"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Other Designation", value);
            }
            value = getString(terser.get("/.ZBR-7-3"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("City", value);
            }
            value = getString(terser.get("/.ZBR-7-4"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Province", value);
            }
            value = getString(terser.get("/.ZBR-7-5"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Postal Code", value);
            }
            value = getString(terser.get("/.ZBR-7-6"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Country", value);
            }
            address.put("Address Type", addressTypeNames.get(identifier));
            return address;
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return null;
        }
    }

    public String getReportingFacilityName() {
        try {
            String key = "", value = "", ident = "", rawOid = "";
            key = getString(terser.get("/.ZBR-4-6-2"));
            if (key != null && key.indexOf(":") > 0) {
                rawOid = key.substring(0, key.indexOf(":"));
                ident = getOrganizationType(rawOid);
                key = key.substring(key.indexOf(":") + 1);
            } else {
                key = "";
            }
            if (key == null || key.trim().equals("")) {
                return "";
            }
            // Catalog-enrich the reporting facility name (licence-only messages); see
            // catalogFacilityName / getPerformingFacilityName.
            value = catalogFacilityName(rawOid, key, getString(terser.get("/.ZBR-4-1")));
            return String.format("%s (%s %s)", value, ident, key);
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public HashMap<String, String> getReportingFacilityAddress() {
        try {
            String value;
            HashMap<String, String> address;
            String identifier = getString(terser.get("/.ZBR-5-7"));
            if ("".equals(identifier)) {
                return null;
            }
            address = new HashMap<String, String>();
            value = getString(terser.get("/.ZBR-5-1"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Street Address", value);
            }
            value = getString(terser.get("/.ZBR-5-2"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Other Designation", value);
            }
            value = getString(terser.get("/.ZBR-5-3"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("City", value);
            }
            value = getString(terser.get("/.ZBR-5-4"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Province", value);
            }
            value = getString(terser.get("/.ZBR-5-5"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Postal Code", value);
            }
            value = getString(terser.get("/.ZBR-5-6"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Country", value);
            }
            address.put("Address Type", addressTypeNames.get(identifier));
            return address;
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return null;
        }
    }

    public String getOrderingFacilityName() {
        try {
            return (getString(terser.get("/.ORC-21-1")));
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }

    public HashMap<String, String> getOrderingFacilityAddress() {
        try {
            String value;
            HashMap<String, String> address;
            String identifier = getString(terser.get("/.ORC-22-7"));
            if ("".equals(identifier)) {
                return null;
            }
            address = new HashMap<String, String>();
            value = getString(terser.get("/.ORC-22-1"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Street Address", value);
            }
            value = getString(terser.get("/.ORC-22-2"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Other Designation", value);
            }
            value = getString(terser.get("/.ORC-22-3"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("City", value);
            }
            value = getString(terser.get("/.ORC-22-4"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Province", value);
            }
            value = getString(terser.get("/.ORC-22-5"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Postal Code", value);
            }
            value = getString(terser.get("/.ORC-22-6"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Country", value);
            }
            address.put("Address Type", addressTypeNames.get(identifier));
            return address;
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return null;
        }
    }

    public String getOrderingProviderName() {
        try {
            return (getString(terser.get("/.ORC-21-1")));
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }

    public HashMap<String, String> getOrderingProviderAddress() {
        try {
            String value;
            HashMap<String, String> address;
            String identifier = getString(terser.get("/.ORC-24-7"));
            if ("".equals(identifier)) {
                return null;
            }
            address = new HashMap<String, String>();
            value = getString(terser.get("/.ORC-24-1"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Street Address", value);
            }
            value = getString(terser.get("/.ORC-24-2"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Other Designation", value);
            }
            value = getString(terser.get("/.ORC-24-3"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("City", value);
            }
            value = getString(terser.get("/.ORC-24-4"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Province", value);
            }
            value = getString(terser.get("/.ORC-24-5"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Postal Code", value);
            }
            value = getString(terser.get("/.ORC-24-6"));
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Country", value);
            }
            address.put("Address Type", addressTypeNames.get(identifier));
            return address;
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return null;
        }
    }

    private boolean stringIsNotNullOrEmpty(String value) {
        return value != null && value.trim().length() > 0;
    }

    public ArrayList<HashMap<String, String>> getOrderingProviderPhones() {
        ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
        try {
            int rep = -1;

            String value;
            HashMap<String, String> telecom;
            String identifier;
            while (!"".equals((identifier = getString(terser.get("/.OBR-17(" + (++rep) + ")-2"))))) {
                telecom = new HashMap<String, String>();
                value = getString(terser.get("/.OBR-17(" + (rep) + ")-1"));
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("phoneNumber", value);
                }
                value = getString(terser.get("/.OBR-17(" + (rep) + ")-3"));
                if (stringIsNotNullOrEmpty(value)) {
                    value = telecomEquipType.get(value);
                    if (stringIsNotNullOrEmpty(value)) {
                        telecom.put("equipType", value);
                    }
                }
                value = getString(terser.get("/.OBR-17(" + (rep) + ")-4"));
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("email", value);
                }
                value = getString(terser.get("/.OBR-17(" + (rep) + ")-5"));
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("countryCode", value);
                }
                value = getString(terser.get("/.OBR-17(" + (rep) + ")-6"));
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("areaCode", value);
                }
                value = getString(terser.get("/.OBR-17(" + (rep) + ")-7"));
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("localNumber", value);
                }
                value = getString(terser.get("/.OBR-17(" + (rep) + ")-8"));
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("extension", value);
                }
                telecom.put("useCode", telecomUseCode.get(identifier));
                results.add(telecom);
            }

            return results;
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return null;
        }
    }

    public String getSpecimenReceivedDateTime() {
        try {
            String date = getString(terser.get("/.OBR-14-1"));
            if (date.length() > 13) {
                return formatDateTime(date);
            }
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public String getOrderDate() {
        try {
            String value = getString(terser.get("/.OBR-27-4"));
            if (value == null || value.length() < 8) return "";
            return (formatDate(value.substring(0, 8)));
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }


    public String getLastUpdateInOLISUnformated() {
        try {
            String date = null;

            int obrNum = getOBRCount();
            Segment obr = null;
            if (obrNum == 1) {
                obr = terser.getSegment("/.OBR");
            } else {
                obr = (Segment) terser.getFinder().getRoot().get("OBR" + obrNum);
            }

            date = Terser.get(obr, 22, 0, 1, 1);

            return date;
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }

    public String getLastUpdateInOLIS() {
        String date = getLastUpdateInOLISUnformated();
        if (date.length() > 0) return formatDateTime(date);
        return "";
    }

    public String getOBXCEParentId(int obr, int obx) {
        return getOBXField(obr, obx, 4, 0, 1);
    }

    HashMap<String, String> obrParentMap;

    public int getChildOBR(String parentId) {
        try {
            return Integer.valueOf(obrParentMap.get(parentId));
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean isChildOBR(int obr) {
        return obrParentMap.containsValue(String.valueOf(obr));
    }

    public String getDiagnosis(int obr) {
        try {
            return obrDiagnosis.containsKey(obr) ? getString(Terser.get(obrDiagnosis.get(obr), 3, 0, 2, 1)) : "";
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * Maps a sorted request position to its original OBR index in the message.
     * {@code obrSortMap} is keyed by sorted position (0-based) after the OLIS
     * request display-sequence sort (CV04/05/06/15); the value's {@code obrIndex}
     * is the request's position in the raw message.
     */
    public int getMappedOBR(int obr) {
        try {
            OlisLabRequestSortable sortable = obrSortMap.get(obr);
            if (sortable != null) {
                return sortable.getObrIndex();
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return obr;
    }

    /**
     * Maps a sorted result position (within a request) to its original 0-based OBX
     * index. {@code obxSortMap} holds the results sorted by the OLIS result
     * display-sequence rules; each sortable's {@code setId} carries the 1-based
     * original OBX position, so {@code setId - 1} is the index the {@code getOBX*}
     * accessors expect.
     */
    public int getMappedOBX(int obr, int obx) {
        try {
            List<OlisLabResultSortable> obxResults = obxSortMap.get(obr);
            if (obxResults != null && obx >= 0 && obx < obxResults.size()) {
                return obxResults.get(obx).getSetId() - 1;
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return obx;
    }

    HashMap<Integer, Segment> obrDiagnosis;

    private ArrayList<String> disciplines;

    public ArrayList<String> getDisciplines() {
        return disciplines;
    }

    List<OLISError> errors;

    public List<OLISError> getReportErrors() {
        List<OLISError> result = new ArrayList<OLISError>();
        if (errors == null) {
            return result;
        }
        for (OLISError error : errors) {
            if (error.segment == null || error.segment.equals("") || error.segment.equals("ERR") || error.segment.equals("SPR")) {
                result.add(error);
            }
        }
        return result;
    }

    @Override
    public void init(String hl7Body) throws HL7Exception {
        initDefaultSourceOrganizations();

        obrDiagnosis = new HashMap<Integer, Segment>();

        obrParentMap = new HashMap<String, String>();

        patientIdentifiers = new HashMap<String, String[]>();
        patientAddresses = new ArrayList<HashMap<String, String>>();
        patientHomeTelecom = new ArrayList<HashMap<String, String>>();
        patientWorkTelecom = new ArrayList<HashMap<String, String>>();
        patientIdentifierNames = new HashMap<String, String>();
        initPatientIdentifierNames();

        addressTypeNames = new HashMap<String, String>();
        initAddressTypeNames();

        telecomUseCode = new HashMap<String, String>();
        initTelecomUseCodes();

        telecomEquipType = new HashMap<String, String>();
        initTelecomEquipTypes();

        sourceOrganizations = new HashMap<String, String>();
        obrSpecimenSource = new ArrayList<String>();
        obrSiteModifier = new ArrayList<String>();
        obrStatus = new ArrayList<String>();
        obrStatusCode = new ArrayList<Character>();
        Parser p = new PipeParser(FLAT_MODEL_FACTORY);

        p.setValidationContext(new NoValidation());

        msg = p.parse(hl7Body.replaceAll("\n", "\r\n"));
        headers = new ArrayList<String>();
        terser = new Terser(msg);
        int zbrNum = 1;
        int obrCount = getOBRCount();
        int obrNum = 1;
        boolean obrFlag;
        String segmentName;
        String[] segments = terser.getFinder().getRoot().getNames();
        obrGroups = new ArrayList<ArrayList<Segment>>();
        int k = 0;

        // We only need to parse a few segments if there are no OBRs.
        if (obrCount == 0) {
            for (; k < segments.length; k++) {
                segmentName = segments[k].substring(0, 3);
                if (segmentName.equals("ZPD")) {
                    parseZPDSegment();
                }
                if (segmentName.equals("ERR")) {
                    parseERRSegment();
                }
                if (segmentName.equals("PID")) {
                    parsePIDSegment();
                }
            }
            return;
        }
        for (int i = 0; i < obrCount; i++) {
            ArrayList<Segment> obxSegs = new ArrayList<Segment>();

            headers.add(getOBRName(i));
            obrNum = i + 1;
            obrFlag = false;
            for (; k < segments.length; k++) {
                try {
                    segmentName = segments[k].substring(0, 3);
                    if (segmentName.equals("ZPD")) {
                        parseZPDSegment();
                    }
                    if (segmentName.equals("ERR")) {
                        parseERRSegment();
                    }
                    if (segmentName.equals("PID")) {
                        parsePIDSegment();
                    } else if (segmentName.equals("ZBR")) {
                        parseZBRSegment(zbrNum++);
                    } else if (obrFlag && segmentName.equals("OBX")) {
                        Structure[] segs = terser.getFinder().getRoot().getAll(segments[k]);
                        for (int l = 0; l < segs.length; l++) {
                            Segment obxSeg = (Segment) segs[l];
                            obxSegs.add(obxSeg);

                            // Collect microorganism codes (coded CE results) so they can be
                            // bulk-resolved to organism names after parse (CV06 micro display).
                            String microorganismCode = getMicroorganismCode(obxSeg);
                            if (!microorganismCode.isEmpty()) {
                                microorganismCodes.add(microorganismCode);
                            }
                        }

                    } else if (obrFlag && segmentName.equals("OBR")) {
                        break;
                    } else if (segments[k].equals("OBR" + obrNum) || (obrNum == 1 && segments[k].equals("OBR"))) {
                        obrFlag = true;
                        Segment obr = null;
                        if (obrNum == 1) {
                            obr = terser.getSegment("/.OBR");
                        } else {
                            obr = (Segment) terser.getFinder().getRoot().get("OBR" + obrNum);
                        }

                        String weirdFixToGetObr1512 = null;
                        Type obr15Types[] = obr.getField(15);
                        if (obr15Types != null && obr15Types.length > 0) {
                            Type obr15Type = obr.getField(15)[0];
                            if (obr15Type instanceof Varies) {
                                Type tt = ((Varies) obr15Type).getData();
                                if (tt instanceof GenericComposite) {
                                    Type comp = ((GenericComposite) tt).getComponent(1);
                                    if (comp instanceof Varies) {
                                        Type ttt = ((Varies) comp).getData();
                                        weirdFixToGetObr1512 = ttt.toString();
                                    }
                                }
                            }
                        }

                        String s1 = getString(Terser.get(obr, 15, 0, 1, 2)); // getString(terser.get("/.OBR-15-1-2"));
                        if (Terser.get(obr, 15, 0, 1, 2) == null && weirdFixToGetObr1512 != null) {
                            s1 = weirdFixToGetObr1512;
                        }
                        String s1code = getString(Terser.get(obr, 15, 0, 1, 1)); // OBR-15-1-1: specimen source code
                        String s2 = getString(Terser.get(obr, 15, 0, 5, 2)); // getString(terser.get("/.OBR-15-5-2"));
                        // CT 9.4/9.5: specimen type (OBR-15) and site modifier (OBR-15-5-2)
                        // are distinct labelled fields — keep them separate rather than concatenated.
                        // CT 9.4: prefer the OLIS Source-nomenclature display name (looked up by the
                        // source code OBR-15-1-1); fall back to the lab-supplied text (OBR-15-1-2).
                        obrSpecimenSource.add(resolveSpecimenType(s1code, s1));
                        obrSiteModifier.add(s2);
                        char status = getString(Terser.get(obr, 25, 0, 1, 1)).charAt(0);
                        isFinal &= isStatusFinal(status);
                        isCorrected |= status == 'C';
                        // CT 10.2.x: display the short status token (e.g. "Final"/"Partial")
                        // rather than the long descriptive sentence; keep the raw code so the
                        // renderer can show the red parenthetical (getObrStatusRedText).
                        obrStatus.add(getTestRequestStatusMessageShort(status));
                        obrStatusCode.add(status);

                        String parent = getString(Terser.get(obr, 26, 0, 2, 1));
                        if (!"".equals(parent)) {
                            obrParentMap.put(parent, String.valueOf(obrNum));
                        }

                    } else if (segmentName.equals("DG1")) {
                        Structure[] segs = terser.getFinder().getRoot().getAll(segments[k]);
                        for (int l = 0; l < segs.length; l++) {
                            Segment dg1Seg = (Segment) segs[l];
                            obrDiagnosis.put(obrNum - 1, dg1Seg);
                        }
                    }
                } catch (Exception e) {
                    MiscUtils.getLogger().error("OLIS HL7 Error", e);
                }
            }
            obrGroups.add(obxSegs);
        }
        obxSortMap = new HashMap<Integer, List<OlisLabResultSortable>>();
        obrSortMap = new HashMap<Integer, OlisLabRequestSortable>();
        mapOBRSortKeys();
        buildMicroorganismNomenclatureMap();

        disciplines = new ArrayList<String>();
        for (int i = 0; i < getOBRCount(); i++) {
            disciplines.add(getOBRCategory(i));
        }
    }

    private void parseZBRSegment(int zbrNum) {
        try {
            String key = "", value = "";
            Segment zbr = null;
            if (zbrNum == 1) {
                zbr = terser.getSegment("/.ZBR");
            } else {
                zbr = (Segment) terser.getFinder().getRoot().get("ZBR" + zbrNum);
            }
            int[] indexes = {2, 3, 4, 6, 8};
            for (int index : indexes) {
                if (getString(Terser.get(zbr, index, 0, 6, 2)).equals("")) {
                    continue;
                }
                key = getString(Terser.get(zbr, index, 0, 6, 2));
                if (key != null && key.indexOf(":") > 0) {
                    key = key.substring(key.indexOf(":") + 1);
                }
                value = getString(Terser.get(zbr, index, 0, 1, 1));
                sourceOrganizations.put(key, value);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
    }

    private void parsePIDSegment() throws HL7Exception {
        Segment pid = terser.getSegment("/.PID");
        int rep = -1;
        String identifier = "";
        String value = "";
        String attrib = "";

        patientIdentifiers = new HashMap<String, String[]>();
        while ((identifier = Terser.get(pid, 3, ++rep, 5, 1)) != null) {

            value = Terser.get(pid, 3, rep, 1, 1);

            attrib = Terser.get(pid, 3, rep, 4, 2);
            if (attrib != null) {
                attrib = attrib.substring(attrib.indexOf(":") + 1);
            }

            patientIdentifiers.put(identifier, new String[]{value, attrib});

        }
        patientAddresses = new ArrayList<HashMap<String, String>>();
        rep = -1;
        HashMap<String, String> address;
        while ((identifier = Terser.get(pid, 11, ++rep, 7, 1)) != null) {
            address = new HashMap<String, String>();
            value = Terser.get(pid, 11, rep, 1, 1);
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Street Address", value);
            }
            value = Terser.get(pid, 11, rep, 2, 1);
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Other Designation", value);
            }
            value = Terser.get(pid, 11, rep, 3, 1);
            if (stringIsNotNullOrEmpty(value)) {
                address.put("City", value);
            }
            value = Terser.get(pid, 11, rep, 4, 1);
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Province", value);
            }
            value = Terser.get(pid, 11, rep, 5, 1);
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Postal Code", value);
            }
            value = Terser.get(pid, 11, rep, 6, 1);
            if (stringIsNotNullOrEmpty(value)) {
                address.put("Country", value);
            }
            address.put("Address Type", addressTypeNames.get(identifier));
            patientAddresses.add(address);
        }

        patientHomeTelecom = new ArrayList<HashMap<String, String>>();
        rep = -1;
        HashMap<String, String> telecom;
        while ((identifier = Terser.get(pid, 13, ++rep, 2, 1)) != null) {
            telecom = new HashMap<String, String>();
            value = Terser.get(pid, 13, rep, 1, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("phoneNumber", value);
            }
            value = Terser.get(pid, 13, rep, 3, 1);
            if (stringIsNotNullOrEmpty(value)) {
                value = telecomEquipType.get(value);
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("equipType", value);
                }
            }
            value = Terser.get(pid, 13, rep, 4, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("email", value);
            }
            value = Terser.get(pid, 13, rep, 5, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("countryCode", value);
            }
            value = Terser.get(pid, 13, rep, 6, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("areaCode", value);
            }
            value = Terser.get(pid, 13, rep, 7, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("localNumber", value);
            }
            value = Terser.get(pid, 13, rep, 8, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("extension", value);
            }
            telecom.put("useCode", telecomUseCode.get(identifier));
            patientHomeTelecom.add(telecom);
        }

        patientWorkTelecom = new ArrayList<HashMap<String, String>>();
        rep = -1;
        while ((identifier = Terser.get(pid, 14, ++rep, 2, 1)) != null) {
            telecom = new HashMap<String, String>();
            value = Terser.get(pid, 14, rep, 1, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("phoneNumber", value);
            }
            value = Terser.get(pid, 14, rep, 3, 1);
            if (stringIsNotNullOrEmpty(value)) {
                value = telecomEquipType.get(value);
                if (stringIsNotNullOrEmpty(value)) {
                    telecom.put("equipType", value);
                }
            }
            value = Terser.get(pid, 14, rep, 4, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("email", value);
            }
            value = Terser.get(pid, 14, rep, 5, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("countryCode", value);
            }
            value = Terser.get(pid, 14, rep, 6, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("areaCode", value);
            }
            value = Terser.get(pid, 14, rep, 7, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("localNumber", value);
            }
            value = Terser.get(pid, 14, rep, 8, 1);
            if (stringIsNotNullOrEmpty(value)) {
                telecom.put("extension", value);
            }
            telecom.put("useCode", telecomUseCode.get(identifier));
            patientWorkTelecom.add(telecom);
        }
    }

    private void parseZPDSegment() throws HL7Exception {
        Segment zpd = terser.getSegment("/.ZPD");
        boolean rb = "Y".equals(Misc.getStr(Terser.get(zpd, 3, 0, 1, 1), ""));
        if (!reportBlocked && rb) {
            reportBlocked = true;
        }
    }

    private void parseERRSegment() throws HL7Exception {
        Segment err = terser.getSegment("/.ERR");
        errors = new ArrayList<OLISError>();
        String segment, sequence, field, identifier, text;
        int rep = -1;
        while ((identifier = Terser.get(err, 1, ++rep, 4, 1)) != null) {
            if (identifier.trim().equals("320")) {
                reportBlocked = true;
            }
            segment = Terser.get(err, 1, rep, 1, 1);
            sequence = Terser.get(err, 1, rep, 1, 2);
            field = Terser.get(err, 1, rep, 1, 3);
            text = Terser.get(err, 1, rep, 4, 2);
            errors.add(new OLISError(segment, sequence, field, identifier, text));
        }
    }

    HashMap<Integer, OlisLabRequestSortable> obrSortMap;

    /**
     * Builds the OLIS request (OBR) display order. Each request becomes an
     * {@link OlisLabRequestSortable} carrying the keys the OLIS sequence rules use
     * (collection date/time, group placer, ZBR.11 sort key, catalog nomenclature
     * sort key + alternate name, OBR set ID); the list is sorted with
     * {@link OlisLabRequestSortable#OLIS_REQUEST_COMPARATOR} and re-indexed so
     * {@code obrSortMap} maps sorted position to the request's data.
     *
     * <p>Per-request OBX results are sorted as a side effect (each OBR calls
     * {@link #mapOBXSortKey(int)}). Derived from oscarpro's OLISHL7Handler.</p>
     */
    private void mapOBRSortKeys() {
        int obrCount = getOBRCount();
        List<OlisLabRequestSortable> requestSortables = new ArrayList<OlisLabRequestSortable>();
        OLISRequestNomenclatureDao requestDao = SpringUtils.getBean(OLISRequestNomenclatureDao.class);
        try {
            for (int i = 0; i < obrCount; i++) {
                // Sort this request's OBX results first.
                mapOBXSortKey(i);
                try {
                    String name = StringUtils.trimToEmpty(getOBRName(i));
                    Date collectionDateTime = getObrCollectionDate(i);
                    String groupPlacerNo = StringUtils.trimToEmpty(getAccessionNum());
                    String sortKey = StringUtils.trimToEmpty(getZBR11(i));
                    String requestCode = getNomenclatureRequestCode(i);
                    OLISRequestNomenclature nomenclature =
                            StringUtils.isEmpty(requestCode) ? null : requestDao.findByNameId(requestCode);
                    if (nomenclature == null) {
                        // OLIS_REQUEST_COMPARATOR dereferences the nomenclature; substitute a
                        // blank one on a catalog miss so a missing/unknown code can't NPE the sort.
                        nomenclature = new OLISRequestNomenclature();
                    }
                    String setId = StringUtils.trimToEmpty(getObrSetId(i));
                    requestSortables.add(new OlisLabRequestSortable(
                            name, i, collectionDateTime, groupPlacerNo, sortKey, nomenclature, setId));
                } catch (Exception e) {
                    MiscUtils.getLogger().error("OLIS HL7 Error", e);
                }
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        Collections.sort(requestSortables, OlisLabRequestSortable.OLIS_REQUEST_COMPARATOR);
        int index = 0;
        for (OlisLabRequestSortable request : requestSortables) {
            obrSortMap.put(index++, request);
        }
    }

    HashMap<Integer, List<OlisLabResultSortable>> obxSortMap;

    /**
     * Builds the OLIS result (OBX) display order within a single request. Each OBX
     * becomes an {@link OlisLabResultSortable} carrying the keys the OLIS 9-step
     * result rule uses (ancillary flag from OBX.11=Z, ZBX.2 sort key, catalog
     * nomenclature sort key + alternate name, OBX.4 sub-ID, ZBX.1 release date);
     * the list is sorted with {@link OlisLabResultSortable#OLIS_RESULT_COMPARATOR}.
     * Every OBX is included (results without a ZBX segment get empty/null keys) so
     * the sorted list always covers the full result set. Derived from oscarpro.
     */
    private void mapOBXSortKey(int obr) {
        List<OlisLabResultSortable> resultList = new ArrayList<OlisLabResultSortable>();
        OLISResultNomenclatureDao resultDao = SpringUtils.getBean(OLISResultNomenclatureDao.class);
        String[] segments = terser.getFinder().getRoot().getNames();
        int obxCount = getOBXCount(obr);
        for (int i = 0; i < obxCount; i++) {
            try {
                String subId = StringUtils.trimToEmpty(getOBXField(obr, i, 4, 0, 1));
                String resultStatus = StringUtils.trimToEmpty(getOBXResultStatus(obr, i));
                String loincCode = StringUtils.trimToEmpty(getOBXField(obr, i, 3, 0, 1));
                OLISResultNomenclature nomenclature =
                        loincCode.isEmpty() ? null : resultDao.findByNameId(loincCode);
                String nomenclatureSortKey = "";
                String alternateName = "";
                if (nomenclature != null) {
                    nomenclatureSortKey = StringUtils.trimToEmpty(nomenclature.getSortKey());
                    alternateName = StringUtils.trimToEmpty(nomenclature.getName());
                }

                String zbxSortKey = "";
                Date releaseDate = null;
                int k = getZBXLocation(obr, i);
                if (k >= 0 && k < segments.length && segments[k].startsWith("ZBX")) {
                    Structure[] zbxSegs = terser.getFinder().getRoot().getAll(segments[k]);
                    Segment zbxSeg = (Segment) zbxSegs[0];
                    zbxSortKey = getString(Terser.get(zbxSeg, 2, 0, 1, 1));
                    releaseDate = parseHl7Timestamp(getString(Terser.get(zbxSeg, 1, 0, 1, 1)));
                }
                // setId carries the 1-based original OBX position so getMappedOBX can map a
                // sorted slot back to the 0-based index used by the getOBX* accessors. A null
                // release date is normalized to the epoch so the release-date tiebreak (only
                // reached by results otherwise equal on every key) cannot NPE.
                resultList.add(new OlisLabResultSortable(
                        i + 1, subId, nomenclatureSortKey, alternateName,
                        "Z".equals(resultStatus),
                        releaseDate != null ? releaseDate : new Date(0L),
                        zbxSortKey));
            } catch (Exception e) {
                MiscUtils.getLogger().error("OLIS HL7 Error", e);
            }
        }
        Collections.sort(resultList, OlisLabResultSortable.OLIS_RESULT_COMPARATOR);
        obxSortMap.put(obr, resultList);
    }

    /**
     * @param obrIndex int 0-based OBR index
     * @return String the request sort key from ZBR.11 (empty string if absent)
     */
    private String getZBR11(int obrIndex) {
        try {
            Segment zbr = (obrIndex == 0)
                    ? terser.getSegment("/.ZBR")
                    : (Segment) terser.getFinder().getRoot().get("ZBR" + (obrIndex + 1));
            return getString(Terser.get(zbr, 11, 0, 1, 1));
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * @param obr int 0-based OBR index
     * @return String the OLIS request code from OBR.4.1 (empty string if absent),
     *         used to look up the request nomenclature
     */
    private String getNomenclatureRequestCode(int obr) {
        try {
            Segment obrSeg = (obr == 0)
                    ? terser.getSegment("/.OBR")
                    : (Segment) terser.getFinder().getRoot().get("OBR" + (obr + 1));
            return getString(Terser.get(obrSeg, 4, 0, 1, 1));
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * @param obr int 0-based OBR index
     * @return String the OBR set ID from OBR.1 (empty string if absent)
     */
    private String getObrSetId(int obr) {
        try {
            Segment obrSeg = (obr == 0)
                    ? terser.getSegment("/.OBR")
                    : (Segment) terser.getFinder().getRoot().get("OBR" + (obr + 1));
            return getString(Terser.get(obrSeg, 1, 0, 1, 1));
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * @param obr int 0-based OBR index
     * @return Date the request collection date/time parsed from OBR.7, or
     *         {@code null} if absent/unparseable (the comparator sorts nulls last)
     */
    private Date getObrCollectionDate(int obr) {
        try {
            Segment obrSeg = (obr == 0)
                    ? terser.getSegment("/.OBR")
                    : (Segment) terser.getFinder().getRoot().get("OBR" + (obr + 1));
            return parseHl7Timestamp(getString(Terser.get(obrSeg, 7, 0, 1, 1)));
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return null;
    }

    /**
     * Parses an HL7 timestamp (DTM/TS) to a {@link Date}, tolerating a timezone
     * offset and fractional seconds by reading only the leading
     * {@code YYYYMMDD[HHMMSS]} digits.
     *
     * @param ts String the raw HL7 timestamp (e.g. {@code 20130713180000-0400})
     * @return Date the parsed instant, or {@code null} if blank/unparseable
     */
    private Date parseHl7Timestamp(String ts) {
        if (ts == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ts.length(); i++) {
            char c = ts.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            } else {
                break;
            }
        }
        String d = sb.toString();
        String pattern;
        if (d.length() >= 14) {
            d = d.substring(0, 14);
            pattern = "yyyyMMddHHmmss";
        } else if (d.length() >= 12) {
            d = d.substring(0, 12);
            pattern = "yyyyMMddHHmm";
        } else if (d.length() >= 8) {
            d = d.substring(0, 8);
            pattern = "yyyyMMdd";
        } else {
            return null;
        }
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(pattern);
            fmt.setLenient(false);
            return fmt.parse(d);
        } catch (Exception e) {
            return null;
        }
    }

    private int getZBXLocation(int i, int j) {

        int obrCount = 0;
        int obxCount = 0;
        // Compensating for -1 parameters for OBR and OBX
        j++;
        i++;
        String[] segments = terser.getFinder().getRoot().getNames();

        String segId = "";
        for (int k = 0; k != segments.length && obxCount <= j && obrCount <= i; k++) {
            segId = segments[k].substring(0, 3);

            // We count all OBRs we see.
            if (segId.equals("OBR")) {
                obrCount++;
            }

            // We count only OBX's for the desired OBR
            else if (segId.equals("OBX") && obrCount == i) {
                obxCount++;
            }

            // Check that this segment is an NTE and we are in the right OBR/OBX position.
            else if (segId.equals("ZBX") && obxCount == j && obrCount == i) {
                return k;
            }
        }
        // No ZBX exists for the requested OBR/OBX. Return -1 (not a fallback index):
        // callers guard with k >= 0, so returning the last segment index would let a
        // trailing ZBX belonging to a *different* OBX bind the wrong sort key/release
        // date and corrupt result display ordering (CT 7.2).
        return -1;
    }

    private String finalStatus = "CFEX";

    public boolean isStatusFinal(char status) {
        return finalStatus.contains(String.valueOf(status));
    }

    public String getNatureOfAbnormalTest(int obr, int obx) {
        String nature = getString(getOBXField(obr, obx, 10, 0, 1));
        return stringIsNotNullOrEmpty(nature) ? getNatureOfAbnormalTest(nature.charAt(0)) : "";
    }

    public String getNatureOfAbnormalTest(char nature) {
        switch (nature) {
            case 'A':
                return "An age-based population";
            case 'N':
                return "None ‚Äì generic normal range";
            case 'R':
                return "A race-based population";
            case 'S':
                return "A sex-based population";
            default:
                return "";
        }
    }

    public static String getTestResultStatusMessage(char status) {
        switch (status) {
            case 'C':
                return "Amended";
            case 'F':
                return "Final";
            case 'P':
                return "Preliminary";
            case 'X':
                return "Could not obtain result";
            case 'W':
                return "Invalid";
            case 'Z':
                return "Ancillary information";
            case 'N':
                return "Test not performed";
            default:
                return "";
        }
    }

    public static String getTestRequestStatusMessage(char status) {
        switch (status) {
            case 'A':
                return "Some, but not all, results available";
            case 'C':
                return "Correction to results";
            case 'E':
                return "OLIS has expired the test request because no activity has occurred within a reasonable amount of time.";
            case 'F':
                return "Final results; results stored and verified. Can only be changed with a corrected result.";
            case 'I':
                return "No results available; specimen received, procedure incomplete.";
            case 'O':
                return "Order received; specimen not yet received. ";
            case 'P':
                return "Preliminary: A verified early result is available, final results not yet obtained.";
            case 'X':
                return "No results available; Order canceled";
            default:
                return "";
        }
    }

    /**
     * Short test-request status token shown in context to the test request name
     * (CT 10.2.x). Codes map to the spec labels: O=Ordered, I=Collected, P=Preliminary,
     * A=Partial, F=Final, C=Amended, X=Cancelled (E=Expired is OLIS-specific).
     */
    public static String getTestRequestStatusMessageShort(char status) {
        switch (status) {
            case 'A':
                return "Partial";
            case 'C':
                return "Amended";
            case 'E':
                return "Expired";
            case 'F':
                return "Final";
            case 'I':
                return "Collected";
            case 'O':
                return "Ordered";
            case 'P':
                return "Preliminary";
            case 'X':
                return "Cancelled";
            default:
                return "";
        }
    }

    /**
     * Red parenthetical text displayed adjacent to the test request name for a
     * non-final test request status (CT 10.2.x [R,P]), e.g. {@code "(test was cancelled)"}.
     * Returns "" for Final and any status without a mandated red annotation.
     */
    public static String getTestRequestStatusRedText(char status) {
        switch (status) {
            case 'O':
                return "(specimen not yet collected)";
            case 'I':
                return "(pending)";
            case 'P':
                return "(preliminary)";
            case 'A':
                return "(partial)";
            case 'C':
                return "(amended)";
            case 'X':
                return "(test was cancelled)";
            default:
                return "";
        }
    }

    /**
     * Red parenthetical text displayed adjacent to the test result name for a non-final
     * test result status (CT 12.8.x [R,P]): W &rarr; "(invalid result)", P &rarr;
     * "(preliminary)", C &rarr; "(amended)", X &rarr; "(could not obtain result)",
     * N &rarr; "(test not performed)". Returns "" for Final (12.8.1, where the adjacent
     * annotation is optional and may be omitted).
     */
    public static String getTestResultStatusRedText(char status) {
        switch (status) {
            case 'W':
                return "(invalid result)";
            case 'P':
                return "(preliminary)";
            case 'C':
                return "(amended)";
            case 'X':
                return "(could not obtain result)";
            case 'N':
                return "(test not performed)";
            default:
                return "";
        }
    }

    public String getPointOfCare(int i) {
        i++;
        try {
            Segment obr = null;
            if (i == 1) {
                obr = terser.getSegment("/.OBR");
            } else {
                obr = (Segment) terser.getFinder().getRoot().get("OBR" + i);
            }
            return getString(Terser.get(obr, 30, 0, 1, 1));
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    @Override
    public String getMsgType() {
        return ("OLIS");
    }

    /**
     * Returns the acknowledged Message Control ID (MSA-2) of the parsed message.
     * In an OLIS query response (ERP), OLIS populates MSA-2 with the Message Control
     * ID (MSH-10) of the initiating request — i.e. the transaction the response is
     * acknowledging. Per the OLIS Interface Specification (§10.2.5.12.2.3) OLIS does
     * not issue a distinct transaction identifier; MSA-2 is the correlation value
     * captured for the OLIS06.02 / OLIS03.06 audit trail. Returns an empty string
     * for messages without an MSA segment (e.g. a bare ORU).
     *
     * @return String the MSA-2 Message Control ID, or an empty string if absent/unparseable
     */
    public String getMsaControlId() {
        try {
            return getString(terser.get("/.MSA-2"));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getMsgDate() {
        //return
        //Temporary fix until we change how the MessageUploader grabs the observation date.

        try {
            String dateString = getCollectionDateTime(0);
            return dateString.substring(0, 19);
        } catch (Exception e) {
            return ("");
        }

    }

    @Override
    public String getRequestDate(int i) {
        return getOrderDate();
    }

    @SuppressWarnings("unused")
    public void processEncapsulatedData(HttpServletRequest request, HttpServletResponse response, int obr, int obx) {
        getOBXField(obr, obx, 5, 0, 2);
        String subtype = getOBXField(obr, obx, 5, 0, 3);
        String data = getOBXEDField(obr, obx, 5, 0, 5);
        try {
            if (subtype.equals("PDF")) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + getAccessionNum().replaceAll("\\s", "_") + "_" + obr + "-" + obx + "_Document.pdf\"");
            } else if (subtype.equals("JPEG")) {
                response.setContentType("image/jpeg");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + getAccessionNum().replaceAll("\\s", "_") + "_" + obr + "-" + obx + "_Image.jpg\"");
            } else if (subtype.equals("GIF")) {
                response.setContentType("image/gif");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + getAccessionNum().replaceAll("\\s", "_") + "_" + obr + "-" + obx + "_Image.gif\"");
            } else if (subtype.equals("RTF")) {
                response.setContentType("application/rtf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + getAccessionNum().replaceAll("\\s", "_") + "_" + obr + "-" + obx + "_Document.rtf\"");
            } else if (subtype.equals("HTML")) {
                response.setContentType("text/html");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + getAccessionNum().replaceAll("\\s", "_") + "_" + obr + "-" + obx + "_Document.html\"");
            } else if (subtype.equals("XML")) {
                response.setContentType("text/xml");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + getAccessionNum().replaceAll("\\s", "_") + "_" + obr + "-" + obx + "_Document.xml\"");
            }


            byte[] buf = Base64.decode(data);
			/*
			int pos = 0;
			int read;
			while (pos < buf.length) {
				read = buf.length - pos > 1024 ? 1024 : buf.length - pos;
				response.getOutputStream().write(buf, pos, read);
				pos += read;
			}
			*/
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(buf, 0, buf.length);
            baos.writeTo(response.getOutputStream());


        } catch (IOException e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
    }

    public String getCollectorsComment(int i) {
        String comment;
        i++;
        try {
            if (i == 1) {
                comment = getString(terser.get("/.OBR-39-2"));
            } else {
                Segment obrSeg = (Segment) terser.getFinder().getRoot().get("OBR" + i);
                comment = getString(Terser.get(obrSeg, 39, 0, 2, 1));
            }
            return comment;

        } catch (Exception e) {
            return ("");
        }

    }

    public String getCollectorsCommentSourceOrganization(int i) {
        String ident;
        String id;
        i++;
        try {
            if (i == 1) {
                id = getString(terser.get("/.ZBR-3-6-2"));
                ident = getString(terser.get("/.ZBR-3-1"));

            } else {
                Segment zbrSeg = (Segment) terser.getFinder().getRoot().get("ZBR" + i);
                ident = getString(Terser.get(zbrSeg, 3, 0, 1, 1));
                id = getString(Terser.get(zbrSeg, 3, 0, 6, 2));
            }
            String orgType = "";
            if (id != null && id.indexOf(":") > 0) {
                orgType = getString(getOrganizationType(id.substring(0, id.indexOf(":"))));
                id = id.substring(id.indexOf(":") + 1);
            }
            // Match the "Name (Type ID)" form used by the other source-org getters.
            return ident + " (" + (orgType.isEmpty() ? "" : orgType + " ") + id + ")";

        } catch (Exception e) {
            return ("");
        }

    }

    @Override
    public String getMsgPriority() {
        // TODO: Check if need implementation
        return ("");
    }

    /**
     * Methods to get information about the Observation Request
     */
    @Override
    public int getOBRCount() {

        if (obrGroups != null) {
            return (obrGroups.size());
        } else {
            int i = 1;
            // String test;
            Segment test;
            try {

                test = terser.getSegment("/.OBR");
                while (test != null) {
                    i++;
                    test = (Segment) terser.getFinder().getRoot().get("OBR" + i);
                }

            } catch (Exception e) {
                // ignore exceptions
            }

            return (i - 1);
        }
    }

    @Override
    public String getOBRName(int i) {

        String obrName;
        i++;
        try {
            if (i == 1) {

                obrName = getString(terser.get("/.OBR-4-2"));
                if (obrName.equals("")) obrName = getString(terser.get("/.OBR-4-1"));

            } else {
                Segment obrSeg = (Segment) terser.getFinder().getRoot().get("OBR" + i);
                obrName = getString(Terser.get(obrSeg, 4, 0, 2, 1));
                if (obrName.equals("")) obrName = getString(Terser.get(obrSeg, 4, 0, 1, 1));

            }

            return (obrName);

        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getOBRIdentifier(int i) {
        return null;
    }

    @Override
    public String getTimeStamp(int i, int j) {
        String timeStamp;
        i++;
        try {
            if (i == 1) {
                timeStamp = formatDateTime(getString(terser.get("/.OBR-7-1")));
            } else {
                Segment obrSeg = (Segment) terser.getFinder().getRoot().get("OBR" + i);
                timeStamp = formatDateTime(getString(Terser.get(obrSeg, 7, 0, 1, 1)));
            }
            return (timeStamp);
        } catch (Exception e) {
            return ("");
        }
    }

    public String getLastUpdateDate(int i, int j) {
        String timeStamp;
        i++;
        try {
            if (i == 1) {
                timeStamp = formatDateTime(getString(terser.get("/.OBR-22-1")));
            } else {
                Segment obrSeg = (Segment) terser.getFinder().getRoot().get("OBR" + i);
                timeStamp = formatDateTime(getString(Terser.get(obrSeg, 22, 0, 1, 1)));
            }
            return (timeStamp);
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public boolean isOBXAbnormal(int i, int j) {
        String abnormalFlag = getOBXAbnormalFlag(i, j);
        if (abnormalFlag.equals("") || abnormalFlag.equals("N")) return (false);
        else return (true);
    }

    @Override
    public String getOBXAbnormalFlag(int i, int j) {
        return (getOBXField(i, j, 8, 0, 1));
    }

    @Override
    public String getObservationHeader(int i, int j) {

        return getOBRName(i);
        // stored in different places for different messages
        // return("");

    }

    @Override
    public int getOBXCount(int i) {
        try {
            ArrayList<Segment> obxSegs = obrGroups.get(i);
            return (obxSegs.size());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getOBXValueType(int i, int j) {
        return (getOBXField(i, j, 2, 0, 1));
    }

    @Override
    public String getOBXIdentifier(int i, int j) {
        return (getOBXField(i, j, 3, 0, 1));
    }

    public String getOBXObservationMethod(int i, int j) {
        return getOBXField(i, j, 17, 0, 2);
    }

    public String getOBXObservationDate(int i, int j) {
        try {
            String date = getOBXField(i, j, 14, 0, 1);
            if (date == null || date.trim().length() == 0) {
                return "";
            }
            return formatDateTime(date);
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
            return "";
        }
    }

    public String getOBRCategory(int i) {
        i++;
        try {
            Segment obr = null;
            if (i == 1) {
                obr = terser.getSegment("/.OBR");
            } else {
                obr = (Segment) terser.getFinder().getRoot().get("OBR" + i);
            }

            String obxCategory = Terser.get(obr, 4, 0, 1, 1);
            OLISRequestNomenclatureDao requestDao = SpringUtils.getBean(OLISRequestNomenclatureDao.class);
            OLISRequestNomenclature requestNomenclature = requestDao.findByNameId(obxCategory);
            String category = "";
            if (requestNomenclature != null) {
                category = StringUtils.trimToEmpty(requestNomenclature.getCategory());
            }
            return category;
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    @Override
    public String getOBXName(int i, int j) {
        String obxName = getOBXField(i, j, 3, 0, 1);

        try {
            OLISResultNomenclatureDao resultDao = (OLISResultNomenclatureDao) SpringUtils.getBean(OLISResultNomenclatureDao.class);
            OLISResultNomenclature resultNomenclature = resultDao.findByNameId(obxName);
            // A missing nomenclature row (e.g. the nomenclature has not been imported yet) is an
            // expected absence, not an error — fall through to the OBX-3-2 parse below instead of
            // NPE-ing on resultNomenclature.getName() and logging a stack trace per OBX.
            if (resultNomenclature != null) {
                return StringUtils.trimToEmpty(resultNomenclature.getName());
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        // If we're unable to find a LOINC match for the identifier then try to parse out the obx name.
        obxName = getOBXField(i, j, 3, 0, 2);
        return "".equals(obxName) ? " " : obxName.indexOf(":") == -1 ? obxName : obxName.substring(0, obxName.indexOf(":"));
    }

    @Override
    public String getOBXNameLong(int i, int j) {
        String obxNameLong = "";

        try {
            obxNameLong = getOBXField(i, j, 3, 0, 2);
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }

        return obxNameLong;
    }

    /**
     * Display name for a coded-entry (CE) result value. When the result is a coded
     * microorganism (OBX value type {@code CE}, coding system {@code HL79905}),
     * resolves the OBX-5.1 code to the OLIS microorganism catalog's alternate name;
     * otherwise (or on a catalog miss) falls back to the OBX-5.2 text component.
     * Without the catalog, a code-only organism would display as a raw code.
     * Derived from oscarpro.
     *
     * @param i int 0-based OBR index
     * @param j int 0-based OBX index
     * @return String the organism/coded-entry display name
     */
    public String getOBXCEName(int i, int j) {
        String microorganismCode = getMicroorganismCode(i, j);
        if (!microorganismCode.isEmpty()) {
            OLISMicroorganismNomenclature nomenclature = olisMicroorganismNomenclatureMap.get(microorganismCode);
            if (nomenclature != null) {
                String alternateName = StringUtils.trimToEmpty(nomenclature.getAlternateName1());
                if (!alternateName.isEmpty()) {
                    return alternateName;
                }
            }
        }
        return getOBXField(i, j, 5, 0, 2);
    }

    /**
     * Builds the ordered list of child results (e.g. antibiotic sensitivities under
     * a microbiology culture) for a child OBR, sorted by the OLIS child sort key
     * (CV06). Each child OBX becomes an {@link OlisLabChildResultSortable} carrying
     * its status, name, sensitivity, comment count, ZBX.2 sort key and
     * susceptibility (OBX-8). Derived from oscarpro.
     *
     * @param childObr int 0-based index of the child OBR
     * @return List&lt;OlisLabChildResultSortable&gt; the child results in display order
     */
    public List<OlisLabChildResultSortable> getChildObrResults(int childObr) {
        List<OlisLabChildResultSortable> childResults = new ArrayList<OlisLabChildResultSortable>();
        int childLength = getOBXCount(childObr);
        for (int obx = 0; obx < childLength; obx++) {
            String status = getOBXResultStatus(childObr, obx);
            String name = getOBXName(childObr, obx);
            String sensitivity = getOBXCESensitivity(childObr, obx);
            int commentCount = getOBXCommentCount(childObr, obx);
            String susceptibility = StringUtils.trimToEmpty(getOBXAbnormalFlag(childObr, obx));
            String sortKey = StringUtils.trimToEmpty(getChildSortKey(childObr, obx));
            childResults.add(new OlisLabChildResultSortable(
                    obx, status, name, sensitivity, commentCount, sortKey, susceptibility));
        }
        Collections.sort(childResults, OlisLabChildResultSortable.CHILD_RESULT_COMPARATOR);
        return childResults;
    }

    /**
     * @param obr int 0-based OBR index
     * @param obx int 0-based OBX index
     * @return String the child result sort key (ZBX-2 of the OBX), empty if absent
     */
    private String getChildSortKey(int obr, int obx) {
        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getZBXLocation(obr, obx);
            if (k >= 0 && k < segments.length && segments[k].startsWith("ZBX")) {
                Structure[] zbxSegs = terser.getFinder().getRoot().getAll(segments[k]);
                Segment zbxSeg = (Segment) zbxSegs[0];
                return getString(Terser.get(zbxSeg, 2, 0, 1, 1));
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * Bulk-resolves the microorganism codes collected during parse into
     * {@link #olisMicroorganismNomenclatureMap} (one query per report rather than
     * per-result lookups during a render).
     */
    private void buildMicroorganismNomenclatureMap() {
        if (microorganismCodes.isEmpty()) {
            return;
        }
        try {
            OLISMicroorganismNomenclatureDao microDao = SpringUtils.getBean(OLISMicroorganismNomenclatureDao.class);
            // Preserve the field's non-null invariant: only overwrite when the lookup
            // yields a map. The current DAO never returns null, but a future change or a
            // test mock could — and result rendering dereferences this map unconditionally.
            Map<String, OLISMicroorganismNomenclature> resolved = microDao.findByMicroorganismCodes(microorganismCodes);
            if (resolved != null) {
                this.olisMicroorganismNomenclatureMap = resolved;
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
    }

    /**
     * @param obxSegment Segment an OBX segment
     * @return String the microorganism code (OBX-5.1) when the result is a coded
     *         microorganism (CE + coding system HL79905), else empty string
     */
    private String getMicroorganismCode(Segment obxSegment) {
        try {
            if (checkIfMicroorganism(obxSegment)) {
                return StringUtils.trimToEmpty(Terser.get(obxSegment, 5, 0, 1, 1));
            }
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("Could not retrieve microorganism code", e);
        }
        return "";
    }

    /**
     * @param obr int 0-based OBR index
     * @param obx int 0-based OBX index
     * @return String the microorganism code for the result, or empty string
     */
    private String getMicroorganismCode(int obr, int obx) {
        try {
            ArrayList<Segment> obxSegments = obrGroups.get(obr);
            return getMicroorganismCode(obxSegments.get(obx));
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * @param obxSegment Segment an OBX segment
     * @return boolean {@code true} if the OBX is a coded microorganism — value type
     *         {@code CE} (OBX-2) with coding system {@code HL79905} (OBX-5.3)
     */
    private boolean checkIfMicroorganism(Segment obxSegment) {
        try {
            String valueType = StringUtils.trimToEmpty(Terser.get(obxSegment, 2, 0, 1, 1));
            if (valueType.equals("CE")) {
                String codeSystem = StringUtils.trimToEmpty(Terser.get(obxSegment, 5, 0, 3, 1));
                return codeSystem.equals("HL79905");
            }
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OBX segment could not be checked for microorganism", e);
        }
        return false;
    }

    public boolean renderAsFT(int i, int j) {
        String[] parts = getOBXField(i, j, 3, 0, 2).split(":");
        if (parts.length < 5) return false;
        String obxIdent = parts[4];
        return obxIdent != null && obxIdent.toUpperCase().startsWith("NAR");
    }

    public boolean renderAsNM(int i, int j) {
        String[] parts = getOBXField(i, j, 3, 0, 2).split(":");
        if (parts.length < 5) return false;
        String obxIdent = parts[4];
        return obxIdent != null && (obxIdent.toUpperCase().startsWith("ORD") || obxIdent.toUpperCase().startsWith("QN"));
    }

    public boolean isAncillary(int i, int j) {
        // CT category 11 scopes ancillary order information by OBX-11 (result status) = "Z",
        // not by the OBX-3-3 coding system. This matches the ancillary-first sort rule (7.2.1),
        // which the OLIS result comparator already keys off OBX-11 = Z.
        String status = getOBXField(i, j, 11, 0, 1);
        return status != null && status.trim().toUpperCase().startsWith("Z");
    }

    public String getOBXCESensitivity(int i, int j) {
        return (getOBXField(i, j, 5, 0, 1));
    }

    @Override
    public String getOBXResult(int i, int j) {
        return (getOBXField(i, j, 5, 0, 1));
    }

    public String getOBXTSResult(int i, int j) {
        String date = getOBXField(i, j, 5, 0, 1);
        return formatDateTime(date);
    }

    public String getOBXDTResult(int i, int j) {
        String date = getOBXField(i, j, 5, 0, 1);
        return formatDate(date);
    }

    public String getOBXTMResult(int i, int j) {
        String date = getOBXField(i, j, 5, 0, 1);
        return formatTime(date);
    }

    public String getOBXSNResult(int i, int j) {
        // SN (Structured Numeric) = <comparator>^<num1>^<separator>^<num2>^<suffix>
        // (CT 13.2). Strip the component delimiters into a readable value, e.g.
        // "<"+"100" -> "<100", or "100"+"-"+"200" -> "100-200".
        String comparator = StringUtils.trimToEmpty(getOBXField(i, j, 5, 0, 1));
        String num1 = StringUtils.trimToEmpty(getOBXField(i, j, 5, 0, 2));
        String separator = StringUtils.trimToEmpty(getOBXField(i, j, 5, 0, 3));
        String num2 = StringUtils.trimToEmpty(getOBXField(i, j, 5, 0, 4));
        String suffix = StringUtils.trimToEmpty(getOBXField(i, j, 5, 0, 5));
        StringBuilder sb = new StringBuilder();
        sb.append(comparator).append(num1);
        if (!separator.isEmpty()) {
            sb.append(separator).append(num2);
        }
        sb.append(suffix);
        return sb.toString();
    }

    @Override
    public String getOBXReferenceRange(int i, int j) {
        return (getOBXField(i, j, 7, 0, 1));
    }

    @Override
    public String getOBXUnits(int i, int j) {
        return (getOBXField(i, j, 6, 0, 1));
    }

    @Override
    public String getOBXResultStatus(int i, int j) {
        return (getOBXField(i, j, 11, 0, 1));
    }

    @Override
    public int getOBXFinalResultCount() {
        int obrCount = getOBRCount();
        int obxCount;
        int count = 0;
        String status;
        for (int i = 0; i < obrCount; i++) {
            obxCount = getOBXCount(i);
            for (int j = 0; j < obxCount; j++) {
                status = getOBXResultStatus(i, j);
                if (status.startsWith("F") || status.startsWith("f")) count++;
            }
        }
        return count;
    }

    /**
     * Retrieve the possible segment headers from the OBX fields
     */
    @Override
    public ArrayList<String> getHeaders() {
        return this.headers;
    }

    /**
     * Methods to get information from observation notes
     */
    @Override
    public int getOBRCommentCount(int i) {

        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(i, -1);
            int count = 0;

            // make sure to count all the nte segments in the group
            if (k >= 0 && k < segments.length && segments[k].substring(0, 3).equals("NTE")) {
                count++;
                ++k;
                while ((k = indexOfNextNTE(segments, k)) != -1) {
                    count++;
                    k++;
                }
            }

            return (count);
        } catch (Exception e) {
            logger.error("OBR Comment count error", e);

            return (0);
        }

    }

    @Override
    public String getOBRComment(int i, int j) {

        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(i, -1);
            if (j > 0) {
                k = indexOfNextNTE(segments, k + 1, j);
            }
            Structure[] nteSegs = terser.getFinder().getRoot().getAll(segments[k]);
            Segment nteSeg = (Segment) nteSegs[0];
            return Hl7FormattedText.toPlainText(getString(Terser.get(nteSeg, 3, 0, 1, 1)));

        } catch (Exception e) {
            logger.error("Could not retrieve OBR comments", e);

            return ("");
        }
    }

    public String getOBRSourceOrganization(int i, int j) {
        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(i, -1);
            if (j > 0) {
                k = indexOfNextNTE(segments, k + 1, j);
            }
            k++;
            // The note's source organization is in the ZNT segment that immediately follows
            // the NTE. When the note has no trailing ZNT (or the NTE is the last segment),
            // there is no source org — return "" rather than indexing past the segment array.
            if (k < 0 || k >= segments.length || !segments[k].startsWith("ZNT")) {
                return "";
            }
            Structure[] ZNTSegs = terser.getFinder().getRoot().getAll(segments[k]);
            Segment ZNTSeg = (Segment) ZNTSegs[0];
            String key = Terser.get(ZNTSeg, 1, 0, 2, 1);
            if (key == null || key.indexOf(":") == -1) {
                return "";
            }
            String ident = key.substring(0, key.indexOf(":"));
            ident = getOrganizationType(ident);
            key = key.substring(key.indexOf(":") + 1);
            return sourceOrganizations.get(key) + " (" + ident + " " + key + ")";

        } catch (Exception e) {
            logger.error("Could not retrieve OBX comment ZNT", e);

            return ("");
        }
    }

    public String getCollectionDateTime(int obrIndex) {
        obrIndex++;
        Segment obr;

        try {
            if (obrIndex == 1) {
                obr = terser.getSegment("/.OBR");
            } else {
                obr = (Segment) terser.getFinder().getRoot().get("OBR" + obrIndex);
            }
            String from = getString(Terser.get(obr, 7, 0, 1, 1));
            if (from.length() > 13) {
                from = formatDateTime(from);
            }
            String to = getString(Terser.get(obr, 8, 0, 1, 1));
            if (to.length() > 13) {
                to = formatDateTime(to);
            }
            boolean hasBoth = stringIsNotNullOrEmpty(from) && stringIsNotNullOrEmpty(to);
            return String.format("%s %s %s", from, hasBoth ? "-" : "", to);
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public String getOrganizationType(String ident) {
        if (ident.equals("2.16.840.1.113883.3.59.1")) {
            return "Lab";
        }
        if (ident.equals("2.16.840.1.113883.3.59.2")) {
            return "SCC";
        }
        if (ident.equals("2.16.840.1.113883.3.59.3")) {
            return "Hospital";
        }
        return "";
    }

    public String getSpecimenCollectedBy(int obr) {
        try {
            obr++;
            String key = "", value = "", ident = "";
            Segment zbr = null;
            if (obr == 1) {
                zbr = terser.getSegment("/.ZBR");
            } else {
                zbr = (Segment) terser.getFinder().getRoot().get("ZBR" + obr);
            }
            key = getString(Terser.get(zbr, 3, 0, 6, 2));
            if (key != null && key.indexOf(":") > 0) {
                ident = key.substring(0, key.indexOf(":"));
                ident = getOrganizationType(ident);
                key = key.substring(key.indexOf(":") + 1);
            }
            if (key == null || key.trim().equals("")) {
                return "";
            }
            value = getString(Terser.get(zbr, 3, 0, 1, 1));
            return String.format("%s (%s %s)", value, ident, key);
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public String getCollectionVolume(int obrIndex) {
        obrIndex++;
        Segment obr;

        try {
            if (obrIndex == 1) {
                obr = terser.getSegment("/.OBR");
            } else {
                obr = (Segment) terser.getFinder().getRoot().get("OBR" + obrIndex);
            }
            String volume = getString(Terser.get(obr, 9, 0, 1, 1));
            String units = getString(Terser.get(obr, 9, 0, 2, 1));
            return volume + " " + units;
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public String getNoOfSampleContainers(int obrIndex) {
        obrIndex++;
        Segment obr;

        try {
            if (obrIndex == 1) {
                obr = terser.getSegment("/.OBR");
            } else {
                obr = (Segment) terser.getFinder().getRoot().get("OBR" + obrIndex);
            }
            String count = getString(Terser.get(obr, 37, 0, 1, 1));

            return count;
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    /**
     * Methods to get information from observation notes
     */
    public int getReportCommentCount() {

        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(-1, -1);
            int count = 0;

            // make sure to count all the nte segments in the group
            if (k >= 0 && k < segments.length && segments[k].substring(0, 3).equals("NTE")) {
                count++;
                ++k;
                while ((k = indexOfNextNTE(segments, k)) != -1) {
                    count++;
                    k++;
                }
            }

            return (count);
        } catch (Exception e) {
            logger.error("OBR Comment count error", e);

            return (0);
        }

    }

    public String getReportComment(int j) {

        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(-1, -1);
            if (j > 0) {
                k = indexOfNextNTE(segments, k + 1, j);
            }
            Structure[] nteSegs = terser.getFinder().getRoot().getAll(segments[k]);
            Segment nteSeg = (Segment) nteSegs[0];
            return Hl7FormattedText.toPlainText(getString(Terser.get(nteSeg, 3, 0, 1, 1)));

        } catch (Exception e) {
            logger.error("Could not retrieve OBR comments", e);

            return ("");
        }
    }

    public String getReportSourceOrganization(int j) {
        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(-1, -1);
            if (j > 0) {
                k = indexOfNextNTE(segments, k + 1, j);
            }
            k++;
            // No trailing ZNT means no source organization — guard the array index.
            if (k < 0 || k >= segments.length || !segments[k].startsWith("ZNT")) {
                return "";
            }
            Structure[] ZNTSegs = terser.getFinder().getRoot().getAll(segments[k]);
            Segment ZNTSeg = (Segment) ZNTSegs[0];
            String key = Terser.get(ZNTSeg, 1, 0, 2, 1);
            if (key == null || key.indexOf(":") == -1) {
                return "";
            }
            String ident = key.substring(0, key.indexOf(":"));
            ident = getOrganizationType(ident);
            key = key.substring(key.indexOf(":") + 1);
            return String.format("%s (%s %s)", sourceOrganizations.get(key), ident, key);
        } catch (Exception e) {
            logger.error("Could not retrieve OBX comment ZNT", e);

            return ("");
        }
    }

    public int indexOfNextNTE(String[] segments, int pos) {
        return indexOfNextNTE(segments, pos, 1);
    }

    public int indexOfNextNTE(String[] segments, int pos, int skip) {
        String segId = "";
        int count = 0;
        while (pos < segments.length) {
            segId = segments[pos].substring(0, 3);
            if (segId.equals("OBR")) {
                break;
            }
            if (segId.equals("OBX")) {
                break;
            }
            if (segId.equals("NTE")) {
                count++;
                if (count >= skip) {
                    return pos;
                }
            }
            pos++;
        }
        return -1;
    }

    /**
     * Methods to get information from observation notes
     */
    @Override
    public int getOBXCommentCount(int i, int j) {
        // jth obx of the ith obr

        try {

            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(i, j);

            int count = 0;
            if (k >= 0 && k < segments.length && segments[k].substring(0, 3).equals("NTE")) {
                count++;
                ++k;
                while ((k = indexOfNextNTE(segments, k)) != -1) {
                    count++;
                    k++;
                }
            }

            return (count);
        } catch (Exception e) {
            logger.error("OBR Comment count error", e);

            return (0);
        }

    }

    @Override
    public String getOBXComment(int i, int j, int nteNum) {
        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(i, j);
            if (nteNum > 0) {
                k = indexOfNextNTE(segments, k, nteNum + 1);
            }
            Structure[] nteSegs = terser.getFinder().getRoot().getAll(segments[k]);
            Segment nteSeg = (Segment) nteSegs[0];
            return Hl7FormattedText.toPlainText(getString(Terser.get(nteSeg, 3, 0, 1, 1)));

        } catch (Exception e) {
            logger.error("Could not retrieve OBX comments", e);

            return ("");
        }
    }

    public String getOBXSourceOrganization(int i, int j, int nteNum) {
        try {
            String[] segments = terser.getFinder().getRoot().getNames();
            int k = getNTELocation(i, j);
            if (nteNum > 0) {
                k = indexOfNextNTE(segments, k, nteNum + 1);
            }
            k++;
            // No trailing ZNT means no source organization — guard the array index.
            if (k < 0 || k >= segments.length || !segments[k].startsWith("ZNT")) {
                return "";
            }
            Structure[] ZNTSegs = terser.getFinder().getRoot().getAll(segments[k]);
            Segment ZNTSeg = (Segment) ZNTSegs[0];
            String key = Terser.get(ZNTSeg, 1, 0, 2, 1);
            if (key == null || key.indexOf(":") == -1) {
                return "";
            }
            String ident = key.substring(0, key.indexOf(":"));
            ident = getOrganizationType(ident);
            key = key.substring(key.indexOf(":") + 1);
            return String.format("%s (%s %s)", sourceOrganizations.get(key), ident, key);

        } catch (Exception e) {
            logger.error("Could not retrieve OBX comment ZNT", e);

            return ("");
        }
    }

    /*
     * Patient Name 1 Last Name 2 First Name 3 Second Name 4 Suffix (e.g., JR or III) 5 Prefix (e.g., DR) 6 Degree 7 Name Type Code
     */

    public String parseFullNameFromSegment(String ident) {
        String name = "";
        String temp = null;

        // get name prefix ie/ DR.
        try {
            temp = terser.get(ident + "5");
        } catch (HL7Exception e) {
            // TODO Auto-generated catch block
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        if (temp != null) {
            name = temp;
        }

        // get the name
        try {
            temp = terser.get(ident + "2");
        } catch (HL7Exception e) {
            temp = null;
        }
        if (temp != null) {
            if (name.equals("")) {
                name = temp;
            } else {
                name = name + " " + temp;
            }
        }
        try {
            if (terser.get(ident + "3") != null) name = name + " " + terser.get(ident + "3");
        } catch (HL7Exception e) {
            name = null;
        }
        try {
            if (terser.get(ident + "1") != null) name = name + " " + terser.get(ident + "1");
        } catch (HL7Exception e) {
            temp = null;
        }
        try {
            if (terser.get(ident + "4") != null) name = name + " " + terser.get(ident + "4");
        } catch (HL7Exception e) {
            temp = null;
        }
        try {
            if (terser.get(ident + "6") != null) name = name + " " + terser.get(ident + "6");
        } catch (HL7Exception e) {
            temp = null;
        }

        return (name);
    }

    public String getFillerOrderNumber() {
        return "";
    }

    public String getEncounterId() {
        return "";
    }

    public String getRadiologistInfo() {
        return "";
    }

    public String getNteForOBX(int i, int j) {

        return "";
    }

    /**
     * Methods to get information about the patient
     */
    @Override
    public String getPatientName() {
        return (parseFullNameFromSegment("/.PID-5-"));
    }

    @Override
    public String getFirstName() {
        try {
            return (getString(terser.get("/.PID-5-2")));
        } catch (HL7Exception ex) {
            return ("");
        }
    }

    @Override
    public String getLastName() {
        try {
            return (getString(terser.get("/.PID-5-1")));
        } catch (HL7Exception ex) {
            return ("");
        }
    }

    @Override
    public String getDOB() {
        try {
            return (formatDateTime(getString(terser.get("/.PID-7-1"))));
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getAge() {
        String age = "N/A";
        String dob = getDOB();
        try {
            // Some examples
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date date = formatter.parse(dob);
            age = UtilDateUtilities.calcAge(date);
        } catch (ParseException e) {
            logger.error("Could not get age", e);

        }
        return age;
    }

    @Override
    public String getSex() {
        try {
            return (getString(terser.get("/.PID-8-1")));
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getHealthNum() {
        String healthNum;

        try {

            // Try finding the health number in the external ID
            healthNum = getString(terser.get("/.PID-2-1"));
            if (healthNum.length() == 10) return (healthNum);

            // Try finding the health number in the alternate patient ID
            healthNum = getString(terser.get("/.PID-4-1"));
            if (healthNum.length() == 10) return (healthNum);

            // Try finding the health number in the internal ID
            healthNum = getString(terser.get("/.PID-3-1"));
            if (healthNum.length() == 10) return (healthNum);

            // Try finding the health number in the SSN field
            healthNum = getString(terser.get("/.PID-19-1"));
            if (healthNum.length() == 10) return (healthNum);
        } catch (Exception e) {
            // ignore exceptions
        }

        return ("");
    }

    /**
     * The health number formatted for display: a 10-digit Ontario health number is
     * spaced after the 4th and 7th digits (e.g. {@code 2000 010 534}) per CT 3.1.1.
     * Non-ON / non-standard numbers are returned unchanged. Use {@link #getHealthNum()}
     * (the raw value) for patient matching — only the displayed value is spaced.
     *
     * @return String the display-formatted health number
     */
    public String getFormattedHealthNum() {
        String hn = getHealthNum();
        if (hn != null && hn.length() == 10 && hn.matches("\\d{10}")) {
            return hn.substring(0, 4) + " " + hn.substring(4, 7) + " " + hn.substring(7);
        }
        return hn;
    }

    @Override
    public String getHomePhone() {
        try {
            String ext = getString(terser.get("/.PID-13-8"));
            return (getString(terser.get("/.PID-13-6")) + "-" + getString(terser.get("/.PID-13-7")) + " " + (ext != null && ext.length() > 0 ? "x" : "") + ext);
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getWorkPhone() {
        try {
            String ext = getString(terser.get("/.PID-14-8"));
            return (getString(terser.get("/.PID-14-6")) + "-" + getString(terser.get("/.PID-14-7")) + " " + (ext != null && ext.length() > 0 ? "x" : "") + ext);
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getPatientLocation() {
        /*
         * try{ String address = getString(terser.get("/.PID-11-1")); String mailing = String.format("%s %s %s", getString(terser.get("/.PID-11-3")), getString(terser.get("/.PID-11-4")), getString(terser.get("/.PID-11-5"))); return address + "<br/>" +
         * mailing; }catch(Exception e){ return(""); }
         */
        return getPerformingFacilityName();
    }

    public String getWorkLocation() {
        try {
            String address = getString(terser.get("/.PID-11-1"));
            String mailing = String.format("%s %s %s", getString(terser.get("/.PID-11-3")), getString(terser.get("/.PID-11-4")), getString(terser.get("/.PID-11-5")));
            return address + "<br/>" + mailing;
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getServiceDate() {
        try {
            Date mshDate = UtilDateUtilities.StringToDate(getMsgDate(), "yyyy-MM-dd");
            return UtilDateUtilities.DateToString(mshDate, "dd-MMM-yyyy");
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getOrderStatus() {
        return isCorrected ? "C" : isFinal ? "F" : "P";
    }

    /**
     * Report-level amendment banner text per CT 5.1, or "" if the report carries no
     * amendment. "(Amended/Invalidation report)" when any test result is invalidated
     * (OBX.11='W'); otherwise "(Amended report)" when any test request (OBR.25='C'),
     * test result (OBX.11='C'), or test-request replacement (ZBR.14='Y') is amended.
     * ZBR.13 full-replacement is exempt (a known OLIS defect prevents it being returned).
     *
     * @return String the red banner text, or "" when the report is not amended
     */
    public String getAmendedReportStatusText() {
        boolean invalidation = false;
        boolean amended = isCorrected; // any OBR.25='C', captured at parse time
        for (int obr = 0; obr < getOBRCount(); obr++) {
            for (int obx = 0; obx < getOBXCount(obr); obx++) {
                String st = StringUtils.trimToEmpty(getOBXResultStatus(obr, obx));
                if (st.startsWith("W")) {
                    invalidation = true;
                } else if (st.startsWith("C")) {
                    amended = true;
                }
            }
            if (isTestRequestReplaced(obr)) {
                amended = true;
            }
        }
        if (invalidation) {
            return "(Amended/Invalidation report)";
        }
        return amended ? "(Amended report)" : "";
    }

    /** CT 5.1: whether this test request is a replacement of previously reported
     *  results (ZBR.14 Test Request Replacement Flag = 'Y'). */
    public boolean isTestRequestReplaced(int obr) {
        obr++;
        try {
            Segment zbr = (obr == 1)
                    ? terser.getSegment("/.ZBR")
                    : (Segment) terser.getFinder().getRoot().get("ZBR" + obr);
            return "Y".equals(Terser.get(zbr, 14, 0, 1, 1));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getClientRef() {
        try {
            return (getString(terser.get("/.OBR-16-1")));
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getAccessionNum() {
        try {
            return (getString(terser.get("/.ORC-4-1")));
        } catch (Exception e) {
            return ("");
        }
    }

    public String getAccessionNumSourceOrganization() {
        try {
            String raw = getString(terser.get("/.ORC-4-3"));
            if (raw == null || raw.indexOf(":") <= 0) {
                return "";
            }
            String oid = raw.substring(0, raw.indexOf(":"));
            String orgType = getOrganizationType(oid);
            String licence = raw.substring(raw.indexOf(":") + 1);
            if (licence == null || licence.trim().isEmpty()) {
                return "";
            }
            licence = licence.trim();
            String sourceOrg = sourceOrganizations.get(licence);
            if (sourceOrg == null) {
                sourceOrg = defaultSourceOrganizations.get(licence);
            }
            // CT 5.2: look up and display the placer group's org name. When the message
            // didn't carry it, resolve from the OLIS Lab/SCC facility catalog (the same
            // enrichment as the performing/reporting labs) instead of printing "null".
            if (sourceOrg == null || sourceOrg.trim().isEmpty()) {
                sourceOrg = catalogFacilityName(oid, licence, null);
            }
            String orgIdPart = (orgType + " " + licence).trim();
            if (sourceOrg == null || sourceOrg.trim().isEmpty()) {
                // Org name still unknown — show only the type/id, never the literal "null".
                return "(" + orgIdPart + ")";
            }
            return sourceOrg + " (" + orgIdPart + ")";
        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }


    public List<String> getAllPractitioners() {
        List<String> docs = new ArrayList<String>();

        try {
            String ordering = getShortName("/.OBR-16-");
            String admitting = getShortName("/.OBR-17-");
            String attending = getShortName("/.OBR-7-");

            String cc = getShortName("/.OBR-28(0)-");
            int i = 1;
            String nextDoc = getShortName("/.OBR-28(" + i + ")-");

            while (!nextDoc.equals("")) {
                cc = cc + "," + nextDoc;
                i++;
                nextDoc = getShortName("/.OBR-28(" + i + ")-");
            }

            if (!StringUtils.isEmpty(ordering)) {
                docs.add(ordering);
            }
            if (!StringUtils.isEmpty(admitting)) {
                docs.add(admitting);
            }
            if (!StringUtils.isEmpty(attending)) {
                docs.add(attending);
            }
            if (!StringUtils.isEmpty(cc)) {
                for (String c : cc.split(",")) {
                    docs.add(c);
                }
            }

        } catch (Exception e) {
            return new ArrayList<String>();
        }

        return docs;
    }

    @Override
    public String getDocName() {
        try {
            return (getFullDocName("/.OBR-16-"));
        } catch (Exception e) {
            return ("");
        }
    }

    /**
     * Structured form of {@link #getDocName()} for renderers that need to style the
     * license credential differently from the name (PDF subscript font, JSP CSS class).
     * Returns an empty {@link DoctorName} (every field empty) when OBR-16 is absent.
     */
    public DoctorName getDocNameStructured() {
        try {
            return getFullDoctorName("/.OBR-16-");
        } catch (Exception e) {
            return new DoctorName("", "", "", "", "", "", "", "");
        }
    }

    public String getShortDocName() {
        try {
            return (getShortName("/.OBR-16-"));
        } catch (Exception e) {
            return ("");
        }
    }

    @Override
    public String getCCDocs() {

        try {
            int i = 0;
            String docs = getFullDocName("/.OBR-28(" + i + ")-");
            i++;
            String nextDoc = getFullDocName("/.OBR-28(" + i + ")-");

            while (!nextDoc.equals("")) {
                docs = docs + ", " + nextDoc;
                i++;
                nextDoc = getFullDocName("/.OBR-28(" + i + ")-");
            }

            return (docs);
        } catch (Exception e) {
            return ("");
        }
    }

    /**
     * Structured form of {@link #getCCDocs()} — iterates OBR-28 repeats and returns one
     * {@link DoctorName} per cc doctor. Renderers that want a comma-joined string can call
     * {@link #getCCDocs()} as before; the structured list lets the PDF/JSP path subscript-style
     * each doctor's license credential individually.
     */
    public List<DoctorName> getCCDocsStructured() {
        List<DoctorName> ccDocs = new ArrayList<DoctorName>();
        try {
            int i = 0;
            DoctorName next = getFullDoctorName("/.OBR-28(" + i + ")-");
            while (!next.isEmpty()) {
                ccDocs.add(next);
                i++;
                next = getFullDoctorName("/.OBR-28(" + i + ")-");
            }
        } catch (Exception e) {
            // fall through — return whatever we collected so far
        }
        return ccDocs;
    }

    public String getTestRequestCode() {
        try {
            String code = getString(terser.get("/.OBR-4-1"));
            return code;
        } catch (HL7Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }
        return "";
    }

    public boolean hasAbnormalResult() {
        for (int x = 0; x < getOBRCount(); x++) {
            for (int y = 0; y < getOBXCount(x); y++) {
                if (isOBXAbnormal(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public ArrayList<String> getDocNums() {
        ArrayList<String> nums = new ArrayList<String>();
        String docNum;
        try {
            if ((docNum = terser.get("/.OBR-16-1")) != null) nums.add(docNum);

            int i = 0;
            while ((docNum = terser.get("/.OBR-28(" + i + ")-1")) != null) {
                nums.add(docNum);
                i++;
            }

        } catch (Exception e) {
            MiscUtils.getLogger().error("OLIS HL7 Error", e);
        }

        return (nums);
    }

    @Override
    public String audit() {
        return "";
    }

    @Override
    public String getNteForPID() {
        return "";
    }

    protected String getOBXField(int i, int j, int field, int rep, int comp) {
        try {
            ArrayList<Segment> obxSegs = obrGroups.get(i);
            Segment obxSeg = obxSegs.get(j);
            return (getString(Terser.get(obxSeg, field, rep, comp, 1))).trim();
        } catch (Exception e) {
            return ("");
        }
    }

    protected String getOBXEDField(int i, int j, int field, int rep, int comp) {
        try {
            ArrayList<Segment> obxSegs = obrGroups.get(i);
            Segment obxSeg = obxSegs.get(j);
            return Terser.get(obxSeg, field, rep, comp, 1);
        } catch (Exception e) {
            return ("");
        }
    }

    private int getNTELocation(int i, int j) {

        int obrCount = 0;
        int obxCount = 0;
        // Compensating for -1 parameters for OBR and OBX
        j++;
        i++;
        String[] segments = terser.getFinder().getRoot().getNames();

        String segId = "";
        for (int k = 0; k != segments.length && obxCount <= j && obrCount <= i; k++) {
            segId = segments[k].substring(0, 3);

            // We count all OBRs we see.
            if (segId.equals("OBR")) {
                obrCount++;
            }

            // We count only OBX's for the desired OBR
            else if (segId.equals("OBX") && obrCount == i) {
                obxCount++;
            }

            // Check that this segment is an NTE and we are in the right OBR/OBX position.
            else if (segId.equals("NTE") && obxCount == j && obrCount == i) {
                return k;
            }
        }
        // No NTE exists for the requested OBR/OBX. Return -1 (not a fallback index):
        // the comment-count guards check k >= 0, so returning the last segment index
        // would let a trailing NTE belonging to a different OBR/OBX be counted and
        // surfaced as a comment under the wrong result. Mirrors getZBXLocation.
        return -1;
    }


    /**
     * Parse a CN/XCN composite at {@code docSeg} into a {@link DoctorName}. The path argument is
     * the terser prefix up to the component separator (e.g. {@code "/.OBR-16-"} for ordering,
     * {@code "/.PV1-7-"} for attending). Component mapping:
     *
     * <ul>
     *   <li>XCN-1 → licenseNumber</li>
     *   <li>XCN-2 → familyName</li>
     *   <li>XCN-3 → givenName</li>
     *   <li>XCN-4 → middleName</li>
     *   <li>XCN-5 → suffix</li>
     *   <li>XCN-6 → prefix (e.g. "DR.")</li>
     *   <li>XCN-7 → degree (e.g. "M.D.")</li>
     *   <li>XCN-13 → licenseType (normalized: MDL→MD, ML→RM, NPL→RN(EC), DDSL→DDS)</li>
     * </ul>
     */
    private DoctorName getFullDoctorName(String docSeg) throws HL7Exception {
        // When the segment doesn't exist (typical for PV1 on most OLIS labs).
        // Without this guard, each terser.get(...) below throws HL7Exception "End of message
        // reached while iterating without loop" and the catch in the public getter logs a full
        // stack trace on every render — caught + harmless, but buried real diagnostic signal.
        if (!segmentExists(docSeg)) {
            return new DoctorName("", "", "", "", "", "", "", "");
        }

        String prefix = terser.get(docSeg + "6");
        String givenName = terser.get(docSeg + "3");
        String middleName = terser.get(docSeg + "4");
        String familyName = terser.get(docSeg + "2");
        String suffix = terser.get(docSeg + "5");
        String degree = terser.get(docSeg + "7");
        String licenseNumber = terser.get(docSeg + "1");

        String licenseType = terser.get(docSeg + "13");
        if (licenseType != null) {
            licenseType = licenseType.toUpperCase();
            if (licenseType.equals("MDL")) licenseType = "MD";
            else if (licenseType.equals("ML")) licenseType = "RM";
            else if (licenseType.equals("NPL")) licenseType = "RN(EC)";
            else if (licenseType.equals("DDSL")) licenseType = "DDS";
        }

        // XCN-22-2 = licensing jurisdiction (state/province). CT 4.4/16.4/17.4/18.4 only
        // require it for a Canadian jurisdiction *other than* Ontario, so blank "ON" out.
        String jurisdiction = terser.get(docSeg + "22-2");
        if (jurisdiction != null && jurisdiction.trim().equalsIgnoreCase("ON")) {
            jurisdiction = "";
        }

        return new DoctorName(prefix, givenName, middleName, familyName,
                suffix, degree, licenseType, licenseNumber, jurisdiction);
    }

    /**
     * Checks whether a HAPI segment exists in the parsed message. Extracts the 3-character
     * segment name from a terser path like {@code "/.PV1-7-"} → {@code "PV1"} and asks HAPI for
     * matching root-level structures. Returns {@code false} on any parse error so the caller can
     * safely short-circuit without provoking an HL7Exception.
     */
    private boolean segmentExists(String docSeg) {
        if (docSeg == null || docSeg.length() < 5) return false;
        String segmentName = docSeg.substring(2, 5);
        try {
            return terser.getFinder().getRoot().getAll(segmentName).length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Plain-text rendering of {@link #getFullDoctorName(String)} — name + license credential
     * joined by a single space (e.g. {@code "DR. JOHN SMITH MD 109753"}). Previously synthesized
     * inline {@code <span style="...">} markup around the license; that intent now lives in
     * {@link DoctorName#getLicensePart()} so each renderer can style it appropriately. Returns
     * {@code ""} when every parsed field is empty (preserving caller-empty-check semantics).
     */
    private String getFullDocName(String docSeg) throws HL7Exception {
        return getFullDoctorName(docSeg).toPlainText();
    }

    private String getShortName(String docSeg) throws HL7Exception {
        String docName = "";
        String temp;

        // get name prefix ie/ DR.
        temp = terser.get(docSeg + "6");
        if (temp != null) docName = temp;

        // get the name
        temp = terser.get(docSeg + "3");
        if (temp != null) {
            if (docName.equals("")) {
                docName = temp;
            } else {
                docName = docName + " " + temp;
            }
        }

        if (terser.get(docSeg + "4") != null) {
            docName = docName + " " + terser.get(docSeg + "4");
        }
        if (terser.get(docSeg + "2") != null) {
            docName = docName + " " + terser.get(docSeg + "2");
        }
        if (terser.get(docSeg + "5") != null) {
            docName = docName + " " + terser.get(docSeg + "5");
        }
        if (terser.get(docSeg + "7") != null) {
            docName = docName + " " + terser.get(docSeg + "7");
        }

        return docName;
    }

    protected String formatTime(String plain) {
        if (plain == null || plain.isBlank()) return "";
        try {
            String dateFormat = "HHmmss";
            if (plain.length() > dateFormat.length()) plain = plain.substring(0, dateFormat.length());
            dateFormat = dateFormat.substring(0, plain.length());
            String stringFormat = "HH:mm:ss";
            stringFormat = stringFormat.substring(0, stringFormat.lastIndexOf(dateFormat.charAt(dateFormat.length() - 1)) + 1);

            Date date = UtilDateUtilities.StringToDate(plain, dateFormat);
            return UtilDateUtilities.DateToString(date, stringFormat);
        } catch (Exception e) {
            return "";
        }
    }

    protected String formatDateTime(String plain) {
        if (plain == null || plain.isBlank()) return "";
        try {
            String timezoneOffset = "";
            if (plain.length() >= 19) {
                timezoneOffset = plain.substring(14, 19);
                plain = plain.substring(0, 14);
            } else if (plain.length() > 14) {
                // Length 15-18: partial timezone like "+05" would have thrown on
                // substring(14, 19). Drop the partial offset and keep the 14-char body.
                plain = plain.substring(0, 14);
            }
            String dateFormat = "yyyyMMddHHmmss";
            dateFormat = dateFormat.substring(0, plain.length());
            String stringFormat = "yyyy-MM-dd HH:mm:ss";
            stringFormat = stringFormat.substring(0, stringFormat.lastIndexOf(dateFormat.charAt(dateFormat.length() - 1)) + 1);

            Date date = UtilDateUtilities.StringToDate(plain, dateFormat);
            return UtilDateUtilities.DateToString(date, stringFormat) + " " + getOffsetName(timezoneOffset);
        } catch (Exception e) {
            return "";
        }
    }

    private String getOffsetName(String offset) {
        if (offset.equals("-0400")) {
            return "EDT";
        } else if (offset.equals("-0500")) {
            return "EST";
        } else if (offset.equals("-0600")) {
            return "CST";
        } else if (!offset.trim().equals("")) {
            return "UTC" + offset;
        }
        return "";
    }

    public void importSourceOrganizations(OLISHL7Handler instance) {
        if (instance == null) {
            return;
        }
        HashMap<String, String> foreignSource = instance.sourceOrganizations;
        for (String key : foreignSource.keySet()) {
            if (!sourceOrganizations.containsKey(key)) {
                sourceOrganizations.put(key, foreignSource.get(key));
            }
        }
    }

    protected String formatDate(String plain) {
        if (plain == null || plain.isBlank()) return "";
        try {
            String dateFormat = "yyyyMMdd";
            if (plain.length() > dateFormat.length()) plain = plain.substring(0, dateFormat.length());
            dateFormat = dateFormat.substring(0, plain.length());
            String stringFormat = "yyyy-MM-dd";
            stringFormat = stringFormat.substring(0, stringFormat.lastIndexOf(dateFormat.charAt(dateFormat.length() - 1)) + 1);

            Date date = UtilDateUtilities.StringToDate(plain, dateFormat);
            return UtilDateUtilities.DateToString(date, stringFormat);
        } catch (Exception e) {
            return "";
        }
    }

    protected String getString(String retrieve) {
        if (retrieve != null) {
            return retrieve.trim();
        } else {
            return ("");
        }
    }


    public class OLISError {
        public OLISError(String segment, String sequence, String field, String indentifer, String text) {
            super();
            this.segment = segment;
            this.sequence = sequence;
            this.field = field;
            this.indentifer = indentifer;
            this.text = text;
        }

        String segment, sequence, field, indentifer, text;

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getIndentifer() {
            return indentifer;
        }

        public void setIndentifer(String indentifer) {
            this.indentifer = indentifer;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((field == null) ? 0 : field.hashCode());
            result = prime * result + ((indentifer == null) ? 0 : indentifer.hashCode());
            result = prime * result + ((segment == null) ? 0 : segment.hashCode());
            result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
            return result;
        }

        /**
         * OLIS Errors are identified by the error code for global errors or the segment, sequence and field of the error for localised errors.
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (obj instanceof String) {
                return this.indentifer.equals(obj);
            }
            if (getClass() != obj.getClass()) return false;
            OLISError other = (OLISError) obj;
            if (!getOuterType().equals(other.getOuterType())) return false;
            if (field == null) {
                if (other.field != null) return false;
            } else if (!field.equals(other.field)) return false;
            if (segment == null) {
                if (other.segment != null) return false;
            } else if (!segment.equals(other.segment)) return false;
            if (sequence == null) {
                if (other.sequence != null) return false;
            } else if (!sequence.equals(other.sequence)) return false;
            return true;
        }

        private OLISHL7Handler getOuterType() {
            return OLISHL7Handler.this;
        }

    }

    //for OMD validation
    public boolean isTestResultBlocked(int i, int j) {
        int obr = getMappedOBR(i);
        if (isOBRBlocked(obr)) {
            return true;
        }

        return false;
    }
}
