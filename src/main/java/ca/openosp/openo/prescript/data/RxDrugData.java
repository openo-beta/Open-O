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


package ca.openosp.openo.prescript.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.openo.prescript.util.RxDrugRef;

/**
 * Comprehensive data access layer for drug-related information in the prescription module.
 *
 * This class serves as the primary interface to the drug reference database, providing
 * methods to search for drugs, retrieve detailed drug monographs, check for interactions,
 * and perform allergy-drug cross-checking. It encapsulates all drug data operations
 * and transforms raw database results into structured domain objects.
 *
 * The class integrates with the RxDrugRef utility to access the underlying drug database,
 * which may be either a local database or a remote drug reference service. It provides
 * multiple search strategies (by name, by route, by component) and supports both
 * branded and generic drug lookups.
 *
 * Key responsibilities include:
 * - Drug search and retrieval operations
 * - Drug interaction checking between multiple medications
 * - Allergy-drug contraindication verification
 * - Drug monograph data structure management
 * - Drug component and strength information retrieval
 *
 * The class uses inner classes to model complex drug data structures like DrugMonograph,
 * DrugSearch results, and Interaction warnings, providing a rich object model for
 * prescription-related operations.
 *
 * @since 2006-03-01
 */
public class RxDrugData {

    /**
     * Submits a suggested alias for a drug to the drug reference database.
     *
     * This method allows healthcare providers to suggest alternative names or aliases
     * for drugs that may not be recognized by the standard drug database. These suggestions
     * can help improve drug search capabilities and handle regional variations in drug naming.
     *
     * The suggestion is forwarded to the drug reference system for review and potential
     * inclusion in future database updates.
     *
     * @param alias String the suggested alias name for the drug
     * @param aliasComment String explanatory comment about why this alias is suggested
     * @param id String the drug reference ID this alias should map to
     * @param name String the official drug name this alias represents
     * @param provider String identifier of the healthcare provider making the suggestion
     * @throws Exception if the suggestion cannot be submitted to the drug reference system
     */
    public void suggestAlias(String alias, String aliasComment, String id, String name, String provider) throws Exception {
        RxDrugRef d = new RxDrugRef();
        d.suggestAlias(alias, aliasComment, id, name, provider);
    }


    /**
     * Comprehensive drug monograph containing all available information about a medication.
     *
     * This inner class represents a complete drug monograph with detailed pharmaceutical,
     * clinical, and safety information. It includes drug identification (name, ATC code),
     * clinical usage (indications, contraindications, dosing), safety considerations
     * (pregnancy, lactation, renal/hepatic impairment), and adverse effects.
     *
     * The monograph structure supports both simple drugs and compound formulations,
     * with component information available through the drugComponentList. Special
     * populations considerations are modeled through dedicated inner classes for
     * pregnancy, lactation, and organ impairment scenarios.
     *
     * This comprehensive data structure enables informed prescribing decisions by
     * providing all relevant drug information in a single, well-organized object.
     */
    public class DrugMonograph {

        /**
         * International nonproprietary name (INN) of the drug (generic name).
         */
        public String name;

        /**
         * ATC (Anatomical Therapeutic Chemical) classification code.
         */
        public String atc;

        /**
         * Regional identifier such as DIN (Drug Identification Number) for Canada.
         */
        public String regionalIdentifier;

        /**
         * Indicates if this drug is on the WHO essential drug list.
         */
        public boolean essential;

        /**
         * Brand/product name if this is not a generic drug.
         */
        public String product;

        /**
         * Description of the drug's mechanism of action.
         */
        public String action;

        /**
         * List of medical indications for this drug.
         */
        public Vector indications;

        /**
         * List of drug components (active ingredients).
         */
        public Vector components = new Vector();

        /**
         * List of contraindications with severity levels.
         */
        public Vector contraindications;

        /**
         * Clinical practice points and prescribing tips.
         */
        public String[] practice_points;

        /**
         * Special considerations for pediatric use.
         */
        public String paediatric_use;

        /**
         * Common adverse effects experienced by patients.
         */
        public String[] common_adverse_effects;

        /**
         * Rare but serious adverse effects.
         */
        public String[] rare_adverse_effects;

        /**
         * Dosage recommendations for various indications.
         */
        public Vector dosage;

        /**
         * Pharmaceutical form (tablet, capsule, liquid, etc.).
         */
        public String drugForm;

        /**
         * Available routes of administration (oral, IV, IM, etc.).
         */
        public Vector route = new Vector();

        public ArrayList<DrugComponent> drugComponentList = new ArrayList<DrugComponent>();
        public PregnancyUse pregnancyUse;
        public LactationUse lactationUse;
        public RenalImpairment renalImpairment;
        public HepaticImpairment hepaticImpairment;

        public String drugCode;

        /**
         * Default constructor for DrugMonograph.
         *
         * Creates an empty monograph instance. Typically used when building
         * a monograph programmatically or for testing purposes.
         */
        public DrugMonograph() {
        }

        /**
         * Constructs a DrugMonograph from drug reference database results.
         *
         * Populates the monograph by extracting and transforming data from a Hashtable
         * returned by the drug reference system. Handles complex nested structures for
         * components, routes, and other multi-valued properties.
         *
         * @param hash Hashtable containing drug data from the reference database
         */
        public DrugMonograph(Hashtable hash) {
            MiscUtils.getLogger().debug(hash);
            name = (String) hash.get("name");
            atc = (String) hash.get("atc");
            product = (String) hash.get("product");
            regionalIdentifier = StringUtils.isEmpty((String) hash.get("regional_identifier")) ? null : (String) hash.get("regional_identifier");
            drugForm = (String) hash.get("drugForm");
            if (hash.get("drugCode") != null) {
                drugCode = (String) hash.get("drugCode");
            }

            Vector drugRoute = (Vector) hash.get("drugRoute");
            if (drugRoute != null) {
                for (int i = 0; i < drugRoute.size(); i++) {
                    String r = (String) drugRoute.get(i);

                    route.add(r);
                }
            }

            Vector comps = (Vector) hash.get("components");
            for (int i = 0; i < comps.size(); i++) {

                Hashtable h = (Hashtable) comps.get(i);
                DrugComponent comp = new DrugComponent(h);
                components.add(comp);
                drugComponentList.add(comp);
            }
            // Example data structure: {name=WARFARIN SODIUM, regional_identifier=02007959, product=COUMADIN TAB 4MG, atc=808774}

        }

        /**
         * Represents a drug contraindication with severity rating.
         *
         * Contraindications indicate medical conditions or situations where
         * the drug should not be used or used with extreme caution. The severity
         * scale helps prescribers assess the risk level.
         */
        class Contraindications {
            /**
             * Drugref condition code primary key identifying the contraindication.
             */
            int code;

            /**
             * Severity level: 1=caution, 2=relative contraindication, 3=absolute contraindication.
             */
            int severity;

            /**
             * Additional clinical context or explanation for the contraindication.
             */
            String comment;
        }

        /**
         * Represents an approved medical indication for the drug.
         *
         * Indications define the medical conditions for which the drug
         * is approved and whether it's considered a first-line treatment.
         */
        class Indications {
            /**
             * Drugref condition code primary key for the indication.
             */
            int code;

            /**
             * True if this drug is considered first-line treatment for this indication.
             */
            boolean firstline;

            /**
             * Additional clinical guidance or restrictions for this indication.
             */
            String comment;
        }

        /**
         * Pregnancy safety information using standard classification systems.
         *
         * Provides pregnancy category rating and specific guidance for
         * prescribing during pregnancy.
         */
        class PregnancyUse {
            /**
             * Pregnancy category code (A, B, C, D, X) per FDA/ADEC classification.
             */
            char code;

            /**
             * Detailed pregnancy safety information and recommendations.
             */
            String comment;
        }

        /**
         * Lactation safety information for breastfeeding mothers.
         *
         * Indicates whether the drug is safe during breastfeeding and
         * provides specific recommendations.
         */
        class LactationUse {
            /**
             * Safety code: 1=compatible with breastfeeding, 2=use with caution, 3=contraindicated.
             */
            int code;

            /**
             * Detailed lactation safety information and clinical guidance.
             */
            String comment;
        }

        /**
         * Dosing adjustments and safety for patients with renal impairment.
         *
         * Provides guidance for prescribing to patients with reduced
         * kidney function.
         */
        class RenalImpairment {
            /**
             * Safety code: 1=no adjustment needed, 2=dose adjustment required, 3=contraindicated.
             */
            int code;

            /**
             * Specific dosing adjustments and monitoring requirements.
             */
            String comment;
        }

        /**
         * Dosing adjustments and safety for patients with hepatic impairment.
         *
         * Provides guidance for prescribing to patients with reduced
         * liver function.
         */
        class HepaticImpairment {
            /**
             * Safety code: 1=no adjustment needed, 2=dose adjustment required, 3=contraindicated.
             */
            int code;

            /**
             * Specific dosing adjustments and monitoring requirements.
             */
            String comment;
        }

        /**
         * Comprehensive dosage information for various indications and patient populations.
         *
         * This class encapsulates complex dosing calculations including weight-based,
         * age-based, and body surface area-based dosing. It supports both simple
         * fixed dosing and complex calculated dosing regimens.
         */
        class Dosage {
            /**
             * Free text dosage information when structured data is unavailable.
             */
            String text;

            /**
             * Condition code for indication-specific dosing (0 = general dosing).
             */
            int indication;

            /**
             * SI units for the dosage (mg, mL, units, etc.).
             */
            String units;

            /**
             * Calculation basis: 0=fixed, 1=age(months), 2=age(years), 3=weight(kg), 4=BSA(m²).
             */
            int calculation_base_units;

            /**
             * Base value for dosage calculation (interpreted per calculation_base_units).
             */
            double calculation_base;

            /**
             * Minimum recommended dose in specified units.
             */
            double starting_range;

            /**
             * Maximum recommended dose in specified units.
             */
            double upper_range;

            /**
             * Time unit for frequency: 1=seconds, 2=minutes, 3=hours, 4=days, 5=weeks, 6=months, 7=years.
             */
            int frequency_units;

            /**
             * How often the drug should be administered (in frequency_units).
             */
            int frequency;

            /**
             * Time unit for duration (same as frequency_units, plus 8=number of doses).
             */
            int duration_units;

            /**
             * Minimum treatment duration (-1=indefinite, -2=as needed).
             */
            int duration_minimum;

            /**
             * Maximum treatment duration (-1=indefinite, -2=as needed).
             */
            int duration_maximum;

            /**
             * If true, automated dosing must not be used; prescriber must review comment.
             */
            boolean constrained;

            /**
             * Additional dosing instructions or constraints.
             */
            String comment;
        }


        /**
         * Represents a single active ingredient component of a drug.
         *
         * For compound medications, multiple DrugComponent instances represent
         * each active ingredient with its respective strength and unit of measure.
         * This allows accurate representation of combination drugs.
         */
        public class DrugComponent {

            /**
             * Name of the active ingredient.
             */
            public String name;

            /**
             * Numeric strength value of this component.
             */
            public String strength;

            /**
             * Unit of measure for the strength (mg, mL, units, etc.).
             */
            public String unit;

            /**
             * Default constructor for DrugComponent.
             */
            public DrugComponent() {
            }

            /**
             * Constructs a DrugComponent from database results.
             *
             * @param h Hashtable containing component data with keys: name, strength, unit
             */
            public DrugComponent(Hashtable h) {
                name = (String) h.get("name");
                strength = ((Double) h.get("strength")).toString();
                unit = (String) h.get("unit");
            }

            public String getName() {
                return name;
            }

            public String getStrength() {
                return strength;
            }

            public String getUnit() {
                return unit;
            }

            @Override
            public String toString() {
                return ToStringBuilder.reflectionToString(this);
            }

        }

        public String getName() {
            return name;
        }

        public String getAtc() {
            return atc;
        }

        public String getRegionalIdentifier() {
            return regionalIdentifier;
        }

        public boolean isEssential() {
            return essential;
        }

        public String getProduct() {
            return product;
        }

        public String getAction() {
            return action;
        }

        public Vector getIndications() {
            return indications;
        }

        public ArrayList<DrugComponent> getDrugComponentList() {
            return drugComponentList;
        }

        public Vector getComponents() {
            return components;
        }

        public Vector getContraindications() {
            return contraindications;
        }

        public String[] getPractice_points() {
            return practice_points;
        }

        public String getPaediatric_use() {
            return paediatric_use;
        }

        public String[] getCommon_adverse_effects() {
            return common_adverse_effects;
        }

        public String[] getRare_adverse_effects() {
            return rare_adverse_effects;
        }

        public Vector getDosage() {
            return dosage;
        }

        public String getDrugForm() {
            return drugForm;
        }

        public Vector getRoute() {
            return route;
        }

        public PregnancyUse getPregnancyUse() {
            return pregnancyUse;
        }

        public LactationUse getLactationUse() {
            return lactationUse;
        }

        public RenalImpairment getRenalImpairment() {
            return renalImpairment;
        }

        public HepaticImpairment getHepaticImpairment() {
            return hepaticImpairment;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    /**
     * Minimal drug data structure for search results and listings.
     *
     * This lightweight class contains only essential drug identification
     * information, making it suitable for search results, dropdown lists,
     * and other scenarios where full monograph data is not required.
     * The type field distinguishes between brand names, generics, and
     * drug classes.
     */
    public class MinDrug {

        public String pKey;
        public String name;
        public String type;
        public Tag tag;

        /**
         * Default constructor for MinDrug.
         */
        MinDrug() {
        }

        /**
         * Constructs MinDrug from drug reference search results.
         *
         * @param h Hashtable containing drug data with keys: id, name, category
         */
        MinDrug(Hashtable h) {
            this.pKey = String.valueOf(h.get("id"));
            this.name = (String) h.get("name");
            // Category indicates drug type: brand, generic, or class
            this.type = ((Integer) h.get("category")).toString();
            MiscUtils.getLogger().debug("pkey " + pKey + " name " + name + " type " + type);
        }

        public String getpKey() {
            return pKey;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Tag getTag() {
            return tag;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    /**
     * Container for drug search results organized by drug type.
     *
     * This class processes and organizes drug search results into categories:
     * brand names, generics, and AFHC (drug) classes. It provides structured
     * access to search results and handles error states when searches fail.
     *
     * The class automatically categorizes results based on drug type codes
     * from the drug reference database, making it easy to present organized
     * search results in the user interface.
     */
    public class DrugSearch {

        ArrayList brand = null;
        ArrayList gen = null;
        ArrayList afhcClass = null;
        int totalSearch;
        boolean empty = false;
        public boolean failed = false;
        public String errorMessage = null;

        /**
         * Constructs an empty DrugSearch result container.
         *
         * Initializes the category lists for organizing search results.
         */
        DrugSearch() {
            brand = new ArrayList();
            gen = new ArrayList();
            afhcClass = new ArrayList();
        }

        /**
         * Processes raw search results and categorizes drugs by type.
         *
         * Drugs are categorized based on their type codes:
         * - Type 13: Brand name drugs
         * - Types 11, 12: Generic drugs
         * - Types 8, 10: AFHC drug classes
         *
         * @param vec Vector of Hashtables containing drug search results
         */
        public void processResult(Vector vec) {
            for (int i = 0; i < vec.size(); i++) {
                Hashtable h = (Hashtable) vec.get(i);
                if (!h.get("name").equals("None found")) {
                    MinDrug d = new MinDrug(h);

                    // Categorize by drug type code
                    if (d.type.equals("13")) {
                        brand.add(d);
                    } else if (d.type.equals("11") || d.type.equals("12")) {
                        gen.add(d);
                    } else if (d.type.equals("8") || d.type.equals("10")) {
                        afhcClass.add(d);
                    }
                } else {
                    this.setEmpty(true);
                }
            }
        }

        public boolean isEmpty() {
            return empty;
        }

        public void setEmpty(boolean b) {
            empty = b;
        }

        public java.util.ArrayList getBrand() {
            return brand;
        }

        public void setBrand(java.util.ArrayList brand) {
            this.brand = brand;
        }

        public java.util.ArrayList getGen() {
            return gen;
        }

        public void setGen(java.util.ArrayList gen) {
            this.gen = gen;
        }

        public java.util.ArrayList getAfhcClass() {
            return afhcClass;
        }

        public void setAfhcClass(java.util.ArrayList afhcClass) {
            this.afhcClass = afhcClass;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    /**
     * Metadata tag for drug information filtering and source tracking.
     *
     * This class represents filtering criteria and metadata for drug searches,
     * including source database, language, country of origin, and authorship
     * information. It's used to filter drug results based on regional preferences
     * and data source requirements.
     */
    public class Tag {

        /**
         * Primary source database identifier.
         */
        public int source;

        /**
         * Bitmask of available source databases.
         */
        public int sources;

        /**
         * Language code for drug information.
         */
        public String language;

        /**
         * Bitmask of available languages.
         */
        public int languages;

        /**
         * Country code for regional drug information.
         */
        public String country;

        /**
         * Bitmask of available countries.
         */
        public int countries;

        /**
         * Author identifier for drug information.
         */
        public int author;

        /**
         * Bitmask of available authors.
         */
        public int authors;

        /**
         * Date filter for recently modified entries.
         */
        public String modified_after;

        /**
         * Flag to include tag metadata in results.
         */
        boolean return_tags;

        /**
         * Default constructor for Tag.
         */
        public Tag() {
        }

        /**
         * Constructs a Tag from drug reference metadata.
         *
         * @param h Hashtable containing tag metadata from drug reference
         */
        public Tag(Hashtable h) {
            source = getInt(h.get("source"));
            sources = getInt(h.get("sources"));
            language = (String) h.get("language");
            languages = getInt(h.get("languages"));
            country = (String) h.get("country");
            countries = getInt(h.get("countries"));
            author = getInt(h.get("author"));
            authors = getInt(h.get("authors"));
            modified_after = (String) h.get("modified_after");
        }

        /**
         * Safely converts an object to integer, returning -1 on failure.
         *
         * @param obj Object to convert to integer
         * @return int parsed value or -1 if conversion fails
         */
        int getInt(Object obj) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (Exception e) {
                return -1;
            }
        }

        public int getSource() {
            return source;
        }

        public int getSources() {
            return sources;
        }

        public String getLanguage() {
            return language;
        }

        public int getLanguages() {
            return languages;
        }

        public String getCountry() {
            return country;
        }

        public int getCountries() {
            return countries;
        }

        public int getAuthor() {
            return author;
        }

        public int getAuthors() {
            return authors;
        }

        public String getModified_after() {
            return modified_after;
        }

        public boolean isReturn_tags() {
            return return_tags;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    /**
     * Searches for drugs by partial name match.
     *
     * Performs a comprehensive search across all drug types (brand names,
     * generics, and drug classes) using the provided search string.
     * Results are automatically categorized by drug type.
     *
     * @param searchStr String partial drug name or search term
     * @return DrugSearch object containing categorized results or error information
     */
    public DrugSearch listDrug(String searchStr) {
        DrugSearch drugSearch = new DrugSearch();
        RxDrugRef drugRef = new RxDrugRef();
        Vector vec = new Vector();
        //Vector vec = drugRef.list_drugs(searchStr,hashtable);
        try {
            vec = drugRef.list_drug_element(searchStr);
        } catch (Exception connEx) {
            drugSearch.failed = true;
            drugSearch.errorMessage = connEx.getMessage();
        }
        if (!drugSearch.failed) {
            drugSearch.processResult(vec);
        }
        return drugSearch;
    }

    /**
     * Enhanced drug search using version 2 of the drug reference API.
     *
     * This method uses an improved search algorithm that may provide
     * better matching results or additional drug information compared
     * to the original listDrug method.
     *
     * @param searchStr String partial drug name or search term
     * @return DrugSearch object containing categorized results or error information
     */
    public DrugSearch listDrug2(String searchStr) {
        DrugSearch drugSearch = new DrugSearch();
        RxDrugRef drugRef = new RxDrugRef();
        Vector vec = new Vector();
        //Vector vec = drugRef.list_drugs(searchStr,hashtable);
        try {
            vec = drugRef.list_drug_element2(searchStr);
        } catch (Exception connEx) {
            drugSearch.failed = true;
            drugSearch.errorMessage = connEx.getMessage();
        }
        if (!drugSearch.failed) {
            drugSearch.processResult(vec);
        }
        return drugSearch;
    }

    /**
     * Searches for drugs filtered by administration route.
     *
     * Useful for finding drugs available in specific formulations,
     * such as oral medications only or injectable forms only.
     * This helps prescribers find appropriate alternatives when
     * a patient requires a specific route of administration.
     *
     * @param searchStr String partial drug name or search term
     * @param searchRoute String administration route (e.g., "oral", "IV", "IM")
     * @return DrugSearch object containing filtered results
     */
    public DrugSearch listDrugByRoute(String searchStr, String searchRoute) {
        DrugSearch drugSearch = new DrugSearch();
        RxDrugRef drugRef = new RxDrugRef();
        Vector vec = new Vector();
        //Vector vec = drugRef.list_drugs(searchStr,hashtable);
        try {
            vec = drugRef.list_drug_element_route(searchStr, searchRoute);
        } catch (Exception connEx) {
            drugSearch.failed = true;
            drugSearch.errorMessage = connEx.getMessage();
        }
        if (!drugSearch.failed) {
            drugSearch.processResult(vec);
        }
        return drugSearch;
    }

    /**
     * Searches for drugs containing a specific active ingredient.
     *
     * This method finds all drug products (primarily brand names)
     * that contain the specified element or active ingredient.
     * Useful for finding all available formulations of a generic drug.
     *
     * @param searchStr String name of the active ingredient to search for
     * @return DrugSearch object containing all drugs with this ingredient
     */
    public DrugSearch listDrugFromElement(String searchStr) {
        DrugSearch drugSearch = new DrugSearch();
        RxDrugRef drugRef = new RxDrugRef();
        Vector vec = new Vector();
        try {
            vec = drugRef.list_brands_from_element(searchStr);
        } catch (Exception connEx) {
            drugSearch.failed = true;
            drugSearch.errorMessage = connEx.getMessage();
        }
        if (!drugSearch.failed) {
            drugSearch.processResult(vec);
        }
        return drugSearch;
    }

    /**
     * Retrieves complete drug monograph by drug reference ID.
     *
     * Fetches comprehensive drug information including indications,
     * contraindications, dosing, and safety information for the
     * specified drug.
     *
     * @param pKey String drug reference database primary key
     * @return DrugMonograph containing complete drug information
     * @throws Exception if drug cannot be retrieved from reference database
     */
    public DrugMonograph getDrug(String pKey) throws Exception {
        RxDrugRef d = new RxDrugRef();
        return new DrugMonograph(d.getDrug(pKey, Boolean.valueOf(true)));
    }


    /**
     * Retrieves drug monograph using version 2 of the drug reference API.
     *
     * This enhanced version may provide additional drug information
     * or use improved data structures compared to the original getDrug method.
     *
     * @param pKey String drug reference database primary key
     * @return DrugMonograph containing complete drug information
     * @throws Exception if drug cannot be retrieved from reference database
     */
    public DrugMonograph getDrug2(String pKey) throws Exception {
        RxDrugRef d = new RxDrugRef();
        return new DrugMonograph(d.getDrug2(pKey, Boolean.valueOf(true)));
    }

    /**
     * Retrieves drug monograph by regional Drug Identification Number (DIN).
     *
     * DIN is a unique identifier used in Canada for drug products.
     * This method allows lookup of drugs using their regulatory
     * identification number rather than the internal database key.
     *
     * @param DIN String the Drug Identification Number to search for
     * @return DrugMonograph containing drug information, or null if not found
     * @throws Exception if database lookup fails
     */
    public DrugMonograph getDrugByDIN(String DIN) throws Exception {
        RxDrugRef drugRef = new RxDrugRef();
        Hashtable<String, Object> returnVal = drugRef.getDrugByDIN(DIN, Boolean.TRUE);
        if (returnVal == null) {
            return null;
        }
        return new DrugMonograph(returnVal);
    }


    /**
     * Retrieves the pharmaceutical form of a drug.
     *
     * Returns the drug's dosage form such as "tablet", "capsule",
     * "liquid", "injection", etc. This information is useful for
     * determining appropriate administration methods.
     *
     * @param pKey String drug reference database primary key
     * @return String describing the pharmaceutical form
     * @throws Exception if drug form cannot be retrieved
     */
    public String getDrugForm(String pKey) throws Exception {
        RxDrugRef d = new RxDrugRef();
        Hashtable h = d.getDrugForm(pKey);
        return (String) h.get("pharmaceutical_cd_form");
    }

    /**
     * Retrieves the generic (non-proprietary) name of a drug.
     *
     * For brand name drugs, this returns the generic equivalent name.
     * Note: When the drug ID already refers to a generic drug, the
     * returned name may not be meaningful as it's already generic.
     *
     * @param pKey String drug reference database primary key
     * @return String generic drug name, empty string if not available
     * @throws Exception if generic name cannot be retrieved
     */
    public String getGenericName(String pKey) throws Exception {
        RxDrugRef d = new RxDrugRef();
        Hashtable h = d.getGenericName(pKey);
        return (String) h.getOrDefault("name", "");
    }


    /**
     * Retrieves the generic name of a drug using integer ID.
     *
     * Convenience overload that accepts an integer drug ID
     * and converts it to string format for lookup.
     *
     * @param pKey int drug reference database primary key
     * @return String generic drug name, empty string if not available
     * @throws Exception if generic name cannot be retrieved
     */
    public String getGenericName(int pKey) throws Exception {
        return getGenericName(pKey + "");
    }

    /**
     * Retrieves available pharmaceutical forms for a drug code.
     *
     * Returns all available formulations (tablet, liquid, etc.)
     * for drugs with the specified drug code.
     *
     * @param drugCode String official drug code
     * @return ArrayList of available drug forms
     * @deprecated Use getDrugForm with drug ID instead
     */
    @Deprecated
    public ArrayList getFormFromDrugCode(String drugCode) {
        ArrayList lst = new ArrayList();
        Vector v = new Vector();
        RxDrugRef d = new RxDrugRef();
        v = d.getFormFromDrugCode(drugCode);
        for (int i = 0; i < v.size(); i++) {
            Hashtable h = (Hashtable) v.get(i);
            lst.add(h.get("form"));
        }
        return lst;
    }

    /**
     * Retrieves active ingredient names for a drug code.
     *
     * Returns a list of all active components/ingredients
     * in drugs with the specified drug code.
     *
     * @param drugCode String official drug code
     * @return ArrayList of component names
     * @deprecated Use DrugMonograph.getComponents() instead
     */
    @Deprecated
    public ArrayList getComponentsFromDrugCode(String drugCode) {
        ArrayList lst = new ArrayList();
        Vector v = new Vector();
        RxDrugRef d = new RxDrugRef();
        v = d.listComponents(drugCode);
        for (int i = 0; i < v.size(); i++) {
            Hashtable h = (Hashtable) v.get(i);
            lst.add(h.get("name"));
        }
        return lst;
    }

    /**
     * Retrieves all distinct pharmaceutical forms in the database.
     *
     * Returns a comprehensive list of all unique drug forms
     * available in the drug reference database, useful for
     * populating selection lists.
     *
     * @return ArrayList of all distinct drug forms
     * @deprecated Consider using filtered searches instead
     */
    @Deprecated
    public ArrayList getDistinctForms() {
        ArrayList lst = new ArrayList();
        Vector v = new Vector();
        RxDrugRef d = new RxDrugRef();
        v = d.getDistinctForms();
        for (int i = 0; i < v.size(); i++) {
            Hashtable h = (Hashtable) v.get(i);
            lst.add(h.get("form"));
        }
        return lst;
    }

    /**
     * Retrieves available administration routes for a drug code.
     *
     * Returns all possible routes of administration (oral, IV, IM, etc.)
     * for drugs with the specified drug code.
     *
     * @param drugCode String official drug code
     * @return ArrayList of available administration routes
     * @deprecated Use DrugMonograph.getRoute() instead
     */
    @Deprecated
    public ArrayList getRouteFromDrugCode(String drugCode) {
        ArrayList lst = new ArrayList();
        Vector v = new Vector();
        RxDrugRef d = new RxDrugRef();
        v = d.getRouteFromDrugCode(drugCode);
        for (int i = 0; i < v.size(); i++) {
            Hashtable h = (Hashtable) v.get(i);
            lst.add(h.get("route"));
        }
        return lst;
    }

    /**
     * Retrieves drug strength information formatted for display.
     *
     * Returns a Hashtable with two entries:
     * - "dosage": formatted strength string (e.g., "5mg/10mg")
     * - "dosageDef": ingredient names (e.g., "amoxicillin/clavulanate")
     *
     * @param drugCode String official drug code
     * @return Hashtable with keys "dosage" and "dosageDef"
     * @deprecated Use getStrengthsLists for structured data
     */
    @Deprecated
    public Hashtable getStrengths(String drugCode) {
        Hashtable retHash = new Hashtable();
        String dosage = "";
        String dosageDef = "";
        ArrayList lst = new ArrayList();
        Vector v = new Vector();
        RxDrugRef d = new RxDrugRef();
        v = d.getStrengths(drugCode);
        for (int i = 0; i < v.size(); i++) {
            Hashtable h = (Hashtable) v.get(i);
            dosage = dosage + ((String) h.get("strength")) + " " + ((String) h.get("strength_unit"));
            dosageDef = dosageDef + ((String) h.get("ingredient"));
            if (i < (v.size() - 1)) {
                dosage = dosage + "/";
                dosageDef = dosageDef + "/";
            }
        }

        // Format: ingredient, strength, strength_unit from database
        retHash.put("dosage", dosage);
        retHash.put("dosageDef", dosageDef);

        return retHash;

    }


    /**
     * Retrieves structured drug strength information.
     *
     * Returns a Hashtable with two ArrayLists:
     * - "dosage": list of formatted strengths with units
     * - "dosageDef": list of corresponding ingredient names
     *
     * This structured format is more suitable for programmatic use
     * than the concatenated strings returned by getStrengths().
     *
     * @param drugCode String official drug code
     * @return Hashtable with ArrayLists for "dosage" and "dosageDef"
     * @deprecated Use DrugMonograph.getDrugComponentList() for component details
     */
    @Deprecated
    public Hashtable getStrengthsLists(String drugCode) {
        Hashtable retHash = new Hashtable();

        ArrayList lst = new ArrayList();
        Vector v = new Vector();
        RxDrugRef d = new RxDrugRef();
        v = d.getStrengths(drugCode);
        ArrayList dosage = new ArrayList();
        ArrayList dosageDef = new ArrayList();
        for (int i = 0; i < v.size(); i++) {
            Hashtable h = (Hashtable) v.get(i);
            dosage.add(((String) h.get("strength")) + " " + ((String) h.get("strength_unit")));
            dosageDef.add((h.get("ingredient")));

        }

        // Format: ingredient, strength, strength_unit from database
        retHash.put("dosage", dosage);
        retHash.put("dosageDef", dosageDef);

        return retHash;

    }

    /**
     * Checks for potential allergy-drug interactions.
     *
     * Compares a drug's ATC code against a patient's known allergies
     * to identify potential allergic reactions. This is a critical
     * safety check that should be performed before prescribing.
     *
     * @param atcCode String ATC code of the drug being prescribed
     * @param allerg Allergy[] array of patient's known allergies
     * @return Allergy[] subset of allergies that may react with this drug
     * @throws Exception if allergy checking service fails
     */
    public Allergy[] getAllergyWarnings(String atcCode, Allergy[] allerg) throws Exception {
        return getAllergyWarnings(atcCode, allerg, null);
    }

    /**
     * Checks for allergy-drug interactions with missing allergy tracking.
     *
     * Enhanced version that also identifies allergies that couldn't be
     * matched in the drug database. This helps identify data quality issues
     * where allergy entries may not have proper drug reference codes.
     *
     * @param atcCode String ATC code of the drug being prescribed
     * @param allerg Allergy[] array of patient's known allergies
     * @param missing List<Allergy> output parameter populated with unmatched allergies
     * @return Allergy[] subset of allergies that may react with this drug
     * @throws Exception if allergy checking service fails
     */
    public Allergy[] getAllergyWarnings(String atcCode, Allergy[] allerg, List<Allergy> missing) throws Exception {
        // Convert allergy objects to format expected by drug reference service
        Vector vec = new Vector();
        for (int i = 0; i < allerg.length; i++) {
            Hashtable h = new Hashtable();
            h.put("id", "" + i);
            h.put("description", allerg[i].getDescription());
            h.put("type", "" + allerg[i].getTypeCode());
            if (allerg[i].getRegionalIdentifier() != null) {
                h.put("din", allerg[i].getRegionalIdentifier());
            }
            if (allerg[i].getAtc() != null) {
                h.put("atc", allerg[i].getAtc());
            } else if (allerg[i].getTypeCode() == 8) {
                // Type 8 allergies use drugref ID as ATC substitute
                h.put("atc", allerg[i].getDrugrefId());
            }
            vec.add(h);
        }
        RxDrugRef d = new RxDrugRef();
        Vector res = d.getAlergyWarnings(atcCode, vec);

        // Process results to identify matching allergies
        Allergy[] actualAllergies = {};
        ArrayList li = new ArrayList();
        if (res != null) {
            Hashtable hashObject = (Hashtable) res.get(0);
            if (hashObject != null) {
                // Extract allergies that match the drug
                Vector alli = (Vector) hashObject.get("warnings");
                if (alli != null) {
                    for (int k = 0; k < alli.size(); k++) {
                        String str = (String) alli.get(k);
                        int id = Integer.parseInt(str);
                        li.add(allerg[id]);
                        MiscUtils.getLogger().debug(str);
                    }
                }

                // Track allergies that couldn't be matched in drug database
                Vector allmissing = (Vector) hashObject.get("missing");
                if (allmissing != null) {
                    for (int k = 0; k < allmissing.size(); k++) {
                        String str = (String) allmissing.get(k);
                        int id = Integer.parseInt(str);
                        if (missing != null) {
                            missing.add(allerg[id]);
                        }
                    }
                }
            }
        }
        actualAllergies = (Allergy[]) li.toArray(actualAllergies);

        return actualAllergies;
    }


    /**
     * Checks for drug-drug interactions between multiple medications.
     *
     * Analyzes a list of ATC codes (representing different drugs) to identify
     * potential interactions between them. This is essential for polypharmacy
     * safety checking when patients are on multiple medications.
     *
     * The returned interactions are sorted by significance, with the most
     * severe interactions appearing first.
     *
     * @param atcCodes Vector of String ATC codes for drugs to check
     * @return Interaction[] array of identified drug interactions, sorted by severity
     * @throws Exception if interaction checking service fails
     */
    public Interaction[] getInteractions(Vector atcCodes) throws Exception {
        Interaction[] arr = {};
        ArrayList lst = new ArrayList();
        Vector v = new Vector();
        RxDrugRef d = new RxDrugRef();
        for (int i = 0; i < atcCodes.size(); i++) {
            String ss = (String) atcCodes.get(i);
            MiscUtils.getLogger().debug(ss);
        }

        // Convert drug reference results to Interaction objects
        v = d.getInteractions(atcCodes);
        for (int i = 0; i < v.size(); i++) {
            Hashtable h = (Hashtable) v.get(i);
            Interaction inact = new Interaction();
            inact.affectedatc = (String) h.get("affectedatc");
            inact.affecteddrug = (String) h.get("affecteddrug");
            inact.affectingatc = (String) h.get("affectingatc");
            inact.affectingdrug = (String) h.get("affectingdrug");
            inact.effect = (String) h.get("effect");
            inact.evidence = (String) h.get("evidence");
            inact.significance = (String) h.get("significance");
            inact.comment = (String) h.get("comment");
            lst.add(inact);
            MiscUtils.getLogger().debug("affectingDrug" + inact.affectingdrug);
        }
        MiscUtils.getLogger().debug(lst.size());
        arr = (Interaction[]) lst.toArray(arr);
        MiscUtils.getLogger().debug(arr.length);
        return arr;
    }


    /**
     * Represents a drug-drug interaction between two medications.
     *
     * This class models the interaction between an affecting drug and an affected drug,
     * including the clinical significance, expected effects, and supporting evidence.
     * Implements Comparable to allow sorting by significance level, with more severe
     * interactions ranking higher.
     */
    public class Interaction implements Comparable {
        /**
         * Compares interactions by significance level for sorting.
         *
         * Lower significance numbers indicate more severe interactions,
         * so they sort first (reverse numeric order). Non-numeric
         * significance values are handled gracefully.
         *
         * @param obj Object the Interaction to compare to
         * @return int negative if this is more significant, positive if less, 0 if equal
         */
        public int compareTo(Object obj) {
            int retval = 0;
            int compVal = 0;
            int thisVal = 0;
            Interaction t = (Interaction) obj;
            String sig = t.significance;
            try {
                compVal = Integer.parseInt(sig);
            } catch (Exception e1) {
                retval = -1;
            }
            try {
                thisVal = Integer.parseInt(significance);
            } catch (Exception e2) {
                retval = 1;
            }

            if (retval == 0) {
                // Lower numbers = higher severity, so reverse comparison
                if (thisVal < compVal) {
                    retval = 1;
                } else if (thisVal > compVal) {
                    retval = -1;
                }
            }
            return retval;
        }

        /**
         * Clinical significance level (1=major, 2=moderate, 3=minor).
         */
        public String significance = null;

        /**
         * ATC code of the drug causing the interaction.
         */
        public String affectingatc = null;

        /**
         * Name of the drug causing the interaction.
         */
        public String affectingdrug = null;

        /**
         * Level of clinical evidence supporting this interaction.
         */
        public String evidence = null;

        /**
         * Description of the clinical effect of the interaction.
         */
        public String effect = null;

        /**
         * Name of the drug affected by the interaction.
         */
        public String affecteddrug = null;

        /**
         * ATC code of the drug affected by the interaction.
         */
        public String affectedatc = null;

        /**
         * Additional clinical guidance or management recommendations.
         */
        public String comment = null;
    }

}
