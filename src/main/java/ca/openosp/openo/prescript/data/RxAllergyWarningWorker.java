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
import java.util.List;

import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.utility.DbConnectionFilter;
import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.openo.prescript.pageUtil.RxSessionBean;

/**
 * Background worker thread for processing allergy warnings during prescription creation.
 *
 * This class operates as a separate thread to perform allergy-drug interaction checks
 * without blocking the main prescription workflow. It processes ATC (Anatomical Therapeutic
 * Chemical) codes against a patient's known allergies to identify potential adverse reactions.
 *
 * The worker is designed to run asynchronously to improve user experience by allowing
 * the prescription interface to remain responsive while complex allergy checks are performed
 * in the background. Results are stored in the session bean for retrieval when needed.
 *
 * Thread safety is ensured through proper resource management and database connection
 * handling via the DbConnectionFilter.
 *
 * @since 2006-03-01
 */
public class RxAllergyWarningWorker extends Thread {
    /**
     * Session bean containing prescription workflow state and allergy warning results.
     * Used to store computed allergy warnings for later retrieval by the UI.
     */
    RxSessionBean sessionBean = null;

    /**
     * ATC code of the drug being checked for allergy interactions.
     * The ATC classification system is used for standardized drug identification.
     */
    String atcCode = null;

    /**
     * Array of patient's known allergies to check against the prescribed drug.
     * Each allergy contains substance information and severity details.
     */
    Allergy[] allergies = null;

    /**
     * Default constructor for RxAllergyWarningWorker.
     *
     * Creates an uninitialized worker instance. This constructor is typically used
     * for testing or when the worker will be configured separately before execution.
     */
    public RxAllergyWarningWorker() {
    }

    /**
     * Constructs a configured allergy warning worker ready for execution.
     *
     * Initializes the worker with all necessary data to perform allergy-drug interaction
     * checks. The worker will process the provided ATC code against the patient's allergies
     * and store results in the session bean.
     *
     * @param sessionBean RxSessionBean instance to store computed allergy warnings
     * @param actCode String representing the ATC code of the drug to check
     * @param allergies Array of Allergy objects representing the patient's known allergies
     */
    public RxAllergyWarningWorker(RxSessionBean sessionBean, String actCode, Allergy[] allergies) {
        this.atcCode = actCode;
        this.sessionBean = sessionBean;
        this.allergies = allergies;
    }

    /**
     * Executes the allergy warning check process in a separate thread.
     *
     * This method performs the following operations:
     * 1. Validates that all required data (ATC code, session bean, allergies) is present
     * 2. Creates a RxDrugData instance to perform the actual allergy checks
     * 3. Identifies both matching allergy warnings and missing allergy data
     * 4. Stores results in the session bean for later retrieval
     * 5. Removes the ATC code from the working set to indicate completion
     *
     * The method includes comprehensive error handling and ensures proper cleanup of
     * database resources through the finally block. Execution time is logged for
     * performance monitoring.
     *
     * Thread safety is maintained by releasing all thread-local database resources
     * at the end of execution.
     */
    public void run() {
        MiscUtils.getLogger().debug("STARTING THREAD - RxAllergyWarningWorker ");

        long start = System.currentTimeMillis();

        Allergy[] allergyWarnings = null;
        try {
            if (atcCode != null && sessionBean != null && allergies != null) {
                RxDrugData drugData = new RxDrugData();

                // List to track allergies that couldn't be matched in the drug database
                List<Allergy> missing = new ArrayList<Allergy>();

                // Perform the actual allergy-drug interaction check
                allergyWarnings = drugData.getAllergyWarnings(atcCode, allergies, missing);

                if (allergyWarnings != null) {
                    // Store successful matches and missing data in session
                    sessionBean.addAllergyWarnings(atcCode, allergyWarnings);
                    sessionBean.addMissingAllergyWarnings(atcCode, missing.toArray(new Allergy[missing.size()]));

                    // Mark this ATC code as processed
                    sessionBean.removeFromWorkingAllergyWarnings(atcCode);
                } else {
                    MiscUtils.getLogger().debug("What to do will allergies atc codes " + atcCode);
                }
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        } finally {
            // Critical: Release database connections held by this thread
            DbConnectionFilter.releaseAllThreadDbResources();
        }

        long end = System.currentTimeMillis() - start;
        MiscUtils.getLogger().debug("THREAD ENDING -RxAllergyWarningWorker " + end);
    }

}
