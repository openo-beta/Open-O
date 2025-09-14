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


package ca.openosp.openo.prescript.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Factory class for loading and accessing renal dosing recommendations for medications.
 * This class parses XML data containing medication dosing adjustments based on creatinine
 * clearance ranges and provides lookup functionality by ATC (Anatomical Therapeutic Chemical) codes.
 *
 * <p>The class implements a singleton pattern with lazy loading to efficiently manage
 * renal dosing information. It loads data from an internal XML resource file containing
 * dosing recommendations for medications that require dose adjustment in patients with
 * varying degrees of renal impairment.</p>
 *
 * <p>Dosing recommendations include specific dose adjustments for different creatinine
 * clearance ranges (e.g., &gt;50, 10-50, &lt;10 mL/min) and may include additional
 * clinical information such as contraindications and monitoring requirements.</p>
 *
 * @since 2007-04-16
 */
public class RenalDosingFactory {

    /**
     * Static cache mapping ATC codes to renal dosing recommendations.
     * Provides fast lookup of dosing information by medication ATC code.
     */
    static Hashtable<String, DosingRecomendation> currentDosingInformation = new Hashtable<String, DosingRecomendation>();

    /**
     * Flag indicating whether the renal dosing data has been loaded from the XML resource.
     */
    static boolean loaded = false;


    /**
     * Private constructor to prevent direct instantiation of this singleton utility class.
     * All methods are static and should be accessed through the class name.
     */
    protected RenalDosingFactory() {
    }

    /**
     * Retrieves renal dosing information for a medication by its ATC code.
     * This method ensures the dosing data is loaded before performing the lookup.
     *
     * @param atc String the ATC (Anatomical Therapeutic Chemical) code of the medication
     * @return DosingRecomendation object containing renal dosing recommendations,
     *         or null if no dosing information exists for the given ATC code
     */
    static public DosingRecomendation getDosingInformation(String atc) {
        loadDosingInformation();
        return currentDosingInformation.get(atc);
    }


    /**\n     * Loads renal dosing information from the internal XML resource file.\n     * This method implements lazy loading and is called automatically when dosing\n     * information is first requested.\n     *\n     * <p>The method parses the RenalDosing.xml file which contains medication elements\n     * with the following structure:</p>\n     * <pre>\n     * &lt;medication name=\"metformin\" atccode=\"A10BA02\"&gt;\n     *     &lt;dose clcrrange=\"&gt;50\"&gt;give normal dose&lt;/dose&gt;\n     *     &lt;dose clcrrange=\"10-50\"&gt;give 50% of dose&lt;/dose&gt;\n     *     &lt;dose clcrrange=\"&lt;10\"&gt;AVOID&lt;/dose&gt;\n     *     &lt;moreinfo&gt;Additional clinical information&lt;/moreinfo&gt;\n     * &lt;/medication&gt;\n     * </pre>\n     *\n     * <p>Each medication can have multiple dose elements for different creatinine\n     * clearance ranges, plus optional additional information.</p>\n     */\n    static private void loadDosingInformation() {
        MiscUtils.getLogger().debug("current dosing size " + currentDosingInformation.size());
        if (!loaded) {
            String dosing = "oscar/oscarRx/RenalDosing.xml";
            RenalDosingFactory rdf = new RenalDosingFactory();
            InputStream is = rdf.getClass().getClassLoader().getResourceAsStream(dosing);

            try {
                // Parse the XML document
                SAXBuilder parser = new SAXBuilder();
                Document doc = parser.build(is);
                Element root = doc.getRootElement();

                // Process each medication element in the XML
                @SuppressWarnings("unchecked")
                List<Element> meas = root.getChildren("medication");
                for (int j = 0; j < meas.size(); j++) {
                    Element e = meas.get(j);
                    String atccode = e.getAttributeValue("atccode");
                    String name = e.getAttributeValue("name");

                    // Create new dosing recommendation object
                    DosingRecomendation rec = new DosingRecomendation();
                    rec.setAtccode(atccode);
                    rec.setName(name);

                    // Process all dose elements for this medication
                    @SuppressWarnings("unchecked")
                    List<Element> doses = e.getChildren("dose");
                    ArrayList<Hashtable<String, String>> recDoses = new ArrayList<Hashtable<String, String>>();
                    for (int d = 0; d < doses.size(); d++) {
                        Element dose = doses.get(d);
                        MiscUtils.getLogger().debug(dose.getName());
                        Hashtable<String, String> h = new Hashtable<String, String>();
                        String clcrrange = dose.getAttributeValue("clcrrange");
                        String recommendation = dose.getText();

                        MiscUtils.getLogger().debug("clcrrange " + clcrrange + " recommendation " + recommendation);

                        // Ensure non-null values
                        if (recommendation == null) {
                            recommendation = "";
                        }
                        if (clcrrange == null) {
                            clcrrange = "";
                        }

                        h.put("clcrrange", clcrrange);
                        h.put("recommendation", recommendation);
                        recDoses.add(h);
                    }
                    rec.setDose(recDoses);

                    // Process additional clinical information
                    @SuppressWarnings("unchecked")
                    List<Element> moreinformation = e.getChildren("moreinfo");
                    StringBuilder sb = new StringBuilder();
                    for (int m = 0; m < moreinformation.size(); m++) {
                        Element info = moreinformation.get(m);
                        sb.append(info.getText());
                    }
                    rec.setMoreinfo(sb.toString());
                    MiscUtils.getLogger().debug(rec.toString());

                    // Store in cache using ATC code as key
                    currentDosingInformation.put(rec.getAtccode(), rec);
                }

            } catch (Exception e) {
                MiscUtils.getLogger().error("Error", e);
            }
            loaded = true;
        }

    }


}
