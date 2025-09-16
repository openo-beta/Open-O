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

import java.util.Vector;

import ca.openosp.openo.utility.DbConnectionFilter;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Background worker thread for asynchronous drug interaction checking.
 *
 * This class performs drug-drug interaction checks in a separate thread to avoid
 * blocking the user interface during potentially slow database queries. It works
 * in conjunction with RxInteractionData to provide cached interaction results.
 *
 * The worker retrieves interaction data from the drug reference system and stores
 * the results in the shared cache. Upon completion, it removes itself from the
 * working set to indicate the check is complete.
 *
 * Thread safety is ensured through proper database resource management, with all
 * connections being released in the finally block to prevent resource leaks.
 *
 * @since 2006-03-01
 */
public class RxInteractionWorker extends Thread {
    /**
     * Reference to the interaction data cache manager.
     */
    RxInteractionData interactionData = null;

    /**
     * Vector of ATC codes to check for interactions.
     */
    Vector atcCodes = null;

    /**
     * Default constructor for RxInteractionWorker.
     *
     * Creates an uninitialized worker. Typically used for testing or
     * when the worker will be configured separately.
     */
    public RxInteractionWorker() {
    }

    /**
     * Constructs a worker configured for interaction checking.
     *
     * @param rxInt RxInteractionData cache manager to store results
     * @param v Vector of String ATC codes to check for interactions
     */
    public RxInteractionWorker(RxInteractionData rxInt, Vector v) {
        atcCodes = v;
        interactionData = rxInt;
    }

    /**
     * Executes the interaction check in a background thread.
     *
     * This method performs the following operations:
     * 1. Queries the drug reference system for interactions between the ATC codes
     * 2. Stores successful results in the shared cache
     * 3. Removes this worker from the working set to signal completion
     * 4. Ensures all database resources are properly released
     *
     * The method includes comprehensive error handling and logs execution time
     * for performance monitoring. Database connections are always released,
     * even if an error occurs, preventing resource leaks in the thread pool.
     */
    public void run() {
        MiscUtils.getLogger().debug("STARTING THREAD");

        long start = System.currentTimeMillis();

        RxDrugData.Interaction[] interactions = null;
        try {
            if (atcCodes != null && interactionData != null) {
                RxDrugData drugData = new RxDrugData();

                // Perform the actual interaction check
                interactions = drugData.getInteractions(atcCodes);

                if (interactions != null) {
                    // Cache the results and mark as complete
                    interactionData.addToHash(atcCodes, interactions);
                    interactionData.removeFromWorking(atcCodes);
                } else {
                    MiscUtils.getLogger().debug("What to do");
                    MiscUtils.getLogger().debug("atc codes " + atcCodes);
                }
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        } finally {
            // Critical: Release database connections held by this thread
            DbConnectionFilter.releaseAllThreadDbResources();
        }

        long end = System.currentTimeMillis() - start;
        MiscUtils.getLogger().debug("THREAD ENDING " + end);
    }

}
