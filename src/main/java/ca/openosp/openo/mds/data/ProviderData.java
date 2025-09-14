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

package ca.openosp.openo.mds.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Provides healthcare provider information retrieval and name formatting for MDS laboratory reports.
 * <p>
 * This class handles the extraction and formatting of healthcare provider information from MDS
 * (Medical Data Systems) laboratory data, including referring doctors, consulting physicians,
 * and admitting doctors. It includes utilities for parsing HL7 provider name formats and
 * retrieving provider lists for laboratory routing and assignment.
 * <p>
 * Key functionality includes:
 * <ul>
 * <li>HL7 provider name parsing and formatting for clinical display</li>
 * <li>Provider list retrieval with filtering capabilities</li>
 * <li>Integration with OpenO EMR provider management system</li>
 * <li>Support for laboratory routing and provider assignment workflows</li>
 * </ul>
 *
 * @since February 4, 2004
 */
public class ProviderData {

    /**
     * Constructs a ProviderData instance with formatted provider names.
     * <p>
     * This constructor processes raw HL7 provider data and formats it for clinical display.
     *
     * @param refDoctor String raw referring doctor information from HL7
     * @param conDoctor String raw consulting doctor information from HL7
     * @param admDoctor String raw admitting doctor information from HL7
     */
    public ProviderData(String refDoctor, String conDoctor, String admDoctor) {
        referringDoctor = beautifyProviderName(refDoctor);
        consultingDoctor = beautifyProviderName(conDoctor);
        admittingDoctor = beautifyProviderName(admDoctor);
    }

    /** Formatted referring doctor name for clinical display */
    public String referringDoctor;
    /** Formatted consulting doctor name for clinical display */
    public String consultingDoctor;
    /** Formatted admitting doctor name for clinical display */
    public String admittingDoctor;

    /**
     * Formats raw HL7 provider names into human-readable clinical display format.
     * <p>
     * This method parses complex HL7 provider name formats and extracts the most
     * relevant information for clinical display, handling various field configurations.
     *
     * @param name String raw HL7 provider name with field separators (^)
     * @return String formatted provider name for clinical display
     */
    public static String beautifyProviderName(String name) {
        String[] subStrings;

        if (name.length() > 0) {
            try {
                subStrings = name.split("\\^");
                if (subStrings.length >= 18) {
                    return subStrings[5] + " " + subStrings[1] + ", " + subStrings[17] + " " + subStrings[13];
                } else if (subStrings.length >= 14) {
                    return subStrings[5] + " " + subStrings[1] + ", " + subStrings[13];
                } else if (subStrings.length >= 6) {
                    return subStrings[5] + " " + subStrings[1];
                } else {
                    return subStrings[1];
                }
            } catch (Exception e) {
                MiscUtils.getLogger().debug("Error in ProviderData: " + e.toString());
                return name.replace('^', ' ');
            }
        } else {
            return "";
        }

    }

    /**
     * Retrieves a list of all doctors and residents for laboratory routing.
     * <p>
     * This method returns all providers who can receive laboratory results,
     * including both staff doctors and residents, sorted alphabetically by name.
     *
     * @return ArrayList<ArrayList<String>> list of provider information [providerNo, firstName, lastName]
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<ArrayList<String>> getProviderList() {
        try {
            ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
            ProviderDao dao = SpringUtils.getBean(ProviderDao.class);
            List<Provider> providers = dao.getProvidersByType(ProviderDao.PR_TYPE_DOCTOR);
            List<Provider> residents = dao.getProvidersByType(ProviderDao.PR_TYPE_RESIDENT);

            providers.addAll(residents);
            Collections.sort(providers, new BeanComparator("formattedName"));
            for (Provider p : providers) {
                ArrayList<String> provider = new ArrayList<String>();
                provider.add(p.getProviderNo());
                provider.add(p.getFirstName());
                provider.add(p.getLastName());
                result.add(provider);
            }


            return result;
        } catch (Exception e) {
            MiscUtils.getLogger().debug("exception in ProviderData:" + e);
            return null;
        }
    }

    /**
     * Retrieves a list of doctors with non-empty OHIP numbers for billing purposes.
     * <p>
     * This method returns providers who have valid OHIP billing numbers,
     * necessary for certain laboratory billing and reporting functions.
     *
     * @return ArrayList<ArrayList<String>> list of providers with billing numbers [providerNo, firstName, lastName]
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<ArrayList<String>> getProviderListWithLabNo() {
        try {
            ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

            ProviderDao dao = SpringUtils.getBean(ProviderDao.class);
            List<Provider> providers = dao.getProvidersByTypeWithNonEmptyOhipNo(ProviderDao.PR_TYPE_DOCTOR);
            Collections.sort(providers, new BeanComparator("formattedName"));
            for (Provider p : providers) {
                ArrayList<String> provider = new ArrayList<String>();
                provider.add(p.getProviderNo());
                provider.add(p.getFirstName());
                provider.add(p.getLastName());
                result.add(provider);
            }
            return result;
        } catch (Exception e) {
            MiscUtils.getLogger().debug("exception in ProviderData:" + e);
            return null;
        }
    }

    /**
     * Gets the full name of a provider by provider number.
     *
     * @param providerNo String the provider number to look up
     * @return String the provider's full name, or empty string if not found
     */
    public static String getProviderName(String providerNo) {
        ProviderDao dao = SpringUtils.getBean(ProviderDao.class);
        Provider p = dao.getProvider(providerNo);
        if (p == null) {
            return "";
        }

        return p.getFullName();
    }

    /**
     * Gets a complete Provider object by provider number.
     *
     * @param providerNo String the provider number to look up
     * @return Provider the complete provider object, or null if not found
     */
    public static Provider getProvider(String providerNo) {
        ProviderDao dao = SpringUtils.getBean(ProviderDao.class);
        return dao.getProvider(providerNo);
    }
}
