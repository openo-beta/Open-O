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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import ca.openosp.openo.commn.dao.ResourceStorageDao;
import ca.openosp.openo.commn.model.ResourceStorage;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.OscarProperties;


/**
 * Singleton factory class for loading and accessing Ontario Drug Benefit (ODB) Limited Use Code information.
 * This class parses XML formulary data and provides lookup functionality for Limited Use Codes
 * associated with specific Drug Identification Numbers (DINs).
 *
 * <p>The class maintains a static cache of limited use information loaded from either:</p>
 * <ul>
 * <li>A configured file path via the "odb_formulary_file" property</li>
 * <li>Database storage via ResourceStorage for LU_CODES</li>
 * <li>A default internal resource file (oscar/oscarRx/data_extract_20250730.xml)</li>
 * </ul>
 *
 * <p>Limited Use Codes define specific clinical criteria that must be met for certain
 * medications to be covered under the Ontario Drug Benefit program. This information
 * is critical for prescription processing and coverage determination.</p>
 *
 * @since 2007-04-16
 */
public class LimitedUseLookup {

    /**
     * Logger instance for this class.
     */
    private static Logger log = MiscUtils.getLogger();

    /**
     * Static cache mapping DIN (Drug Identification Number) to lists of Limited Use Codes.
     * Each DIN may have multiple limited use criteria that must be evaluated.
     */
    static Hashtable<String, ArrayList<LimitedUseCode>> luLookup = new Hashtable<String, ArrayList<LimitedUseCode>>();

    /**
     * Flag indicating whether the Limited Use lookup data has been loaded from the data source.
     */
    static boolean loaded = false;

    /**
     * Private constructor to prevent direct instantiation of this singleton utility class.
     * All methods are static and should be accessed through the class name.
     */
    protected LimitedUseLookup() {
    }

    /**
     * Retrieves Limited Use Code information for a specific Drug Identification Number (DIN).
     * This method ensures the lookup data is loaded before performing the search.
     *
     * @param din String the Drug Identification Number to look up
     * @return ArrayList&lt;LimitedUseCode&gt; list of limited use codes for the DIN,
     *         or null if the DIN is null or no limited use codes exist
     */
    static public ArrayList<LimitedUseCode> getLUInfoForDin(String din) {
        loadLULookupInformation();
        if (din == null) {
            return null;
        }
        return luLookup.get(din);
    }

    /**
     * Creates a LimitedUseCode object from an XML element.
     * Parses the XML attributes and text content to populate the Limited Use Code fields.
     *
     * @param e Element the XML element containing limited use code data
     * @return LimitedUseCode populated object with data from the XML element
     */
    static public LimitedUseCode makeLUNote(Element e) {
        LimitedUseCode lu = new LimitedUseCode();
        lu.setSeq(getVal(e, "seq"));
        lu.setUseId(getVal(e, "reasonForUseId"));
        lu.setType(getVal(e, "type"));
        lu.setTxt(e.getText());
        return lu;
    }

    /**
     * Extracts the value of a named attribute from an XML element.
     * Returns an empty string if the attribute doesn't exist.
     *
     * @param e Element the XML element to query
     * @param name String the name of the attribute to retrieve
     * @return String the attribute value, or empty string if not found
     */
    static public String getVal(Element e, String name) {
        if (e.getAttribute(name) != null) {
            return e.getAttribute(name).getValue();
        }
        return "";
    }

    /**
     * Forces a reload of the Limited Use lookup information from the data source.
     * This method clears the loaded flag and reinitializes the lookup cache.
     * Useful when the underlying formulary data has been updated.
     */
    static public void reLoadLookupInformation() {
        loaded = false;
        loadLULookupInformation();
    }

    /**\n     * Loads Limited Use lookup information from the configured data source.\n     * This method implements a lazy-loading pattern and is called automatically\n     * when lookup data is first requested.\n     *\n     * <p>The method attempts to load data from multiple sources in priority order:</p>\n     * <ol>\n     * <li>File system path specified by \"odb_formulary_file\" property</li>\n     * <li>Database ResourceStorage for LU_CODES type</li>\n     * <li>Default internal resource file (oscar/oscarRx/data_extract_20250730.xml)</li>\n     * </ol>\n     *\n     * <p>The XML data is parsed to extract pcgGroup elements containing lccNote\n     * children, which define the limited use criteria. Each drug within a group\n     * inherits all the limited use codes defined in that group.</p>\n     */\n    static private void loadLULookupInformation() {
        log.debug("current LU lookup size " + luLookup.size());
        if (!loaded) {
            LimitedUseLookup rdf = new LimitedUseLookup();
            InputStream is = null;
            ResourceStorageDao resourceStorageDao = SpringUtils.getBean(ResourceStorageDao.class);
            try {

                // First priority: check for configured file path
                String fileName = OscarProperties.getInstance().getProperty("odb_formulary_file");
                if (fileName != null && !fileName.isEmpty()) {
                    is = new BufferedInputStream(new FileInputStream(fileName));
                    log.info("loading odb file from property " + fileName);

                } else {
                    // Second priority: check database resource storage
                    ResourceStorage resourceStorage = resourceStorageDao.findActive(ResourceStorage.LU_CODES);
                    if (resourceStorage != null) {
                        is = new ByteArrayInputStream(resourceStorage.getFileContents());
                        log.info("loading odb file from resource storage id" + resourceStorage.getId());
                    } else {
                        // Third priority: use default internal resource
                        String dosing = "oscar/oscarRx/data_extract_20250730.xml";
                        log.info("loading odb file from internal resource " + dosing);
                        is = rdf.getClass().getClassLoader().getResourceAsStream(dosing);
                    }
                }

                // Parse the XML formulary data
                SAXBuilder parser = new SAXBuilder();
                Document doc = parser.build(is);
                Element root = doc.getRootElement();
                Element formulary = root.getChild("formulary");
                @SuppressWarnings("unchecked")
                Iterator<Element> items = formulary.getDescendants(new ElementFilter("pcgGroup"));

                // Process each pharmaceutical category group
                while (items.hasNext()) {
                    Element pcgGroup = items.next();
                    @SuppressWarnings("unchecked")
                    List<Element> lccNoteList = pcgGroup.getChildren("lccNote");

                    // Only process groups that have limited use codes
                    if (lccNoteList.size() > 0) {
                        ArrayList<LimitedUseCode> luList = new ArrayList<LimitedUseCode>();

                        // Create LimitedUseCode objects for each lccNote element
                        for (Element lccNo : lccNoteList) {
                            luList.add(makeLUNote(lccNo));
                        }

                        // Apply these limited use codes to all drugs in this group
                        @SuppressWarnings("unchecked")
                        Iterator<Element> drugs = pcgGroup.getDescendants(new ElementFilter("drug"));
                        while (drugs.hasNext()) {
                            Element drug = drugs.next();
                            String din = drug.getAttribute("id").getValue();
                            luLookup.put(din, luList);
                        }
                    }
                }

                loaded = true;
            } catch (Exception e) {
                MiscUtils.getLogger().error("Error", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        MiscUtils.getLogger().error("Error", e);
                    }
                }
            }
        }

    }
}
