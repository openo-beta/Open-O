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

import java.util.Hashtable;
import java.util.Vector;

import ca.openosp.openo.utility.MiscUtils;

/**
 * Singleton cache manager for drug-drug interaction data.
 *
 * This class implements a caching mechanism for drug interaction checks to improve
 * performance and reduce redundant database queries. It uses a singleton pattern
 * to maintain a global cache of interaction results indexed by ATC code combinations.
 *
 * The class supports both synchronous and asynchronous interaction checking:
 * - Cached results are returned immediately
 * - New interaction checks can be preloaded in background threads
 * - In-progress checks can be waited on if needed immediately
 *
 * Thread safety is managed through the use of worker threads and synchronized
 * collections for managing the cache and in-progress work.
 *
 * @since 2006-03-01
 */
public class RxInteractionData {
    /**
     * Singleton instance of the interaction data cache.
     */
    static RxInteractionData rxInteractionData = new RxInteractionData();

    /**
     * Cache of interaction results indexed by ATC code vector hash.
     * Key: Integer hash of ATC code vector, Value: Interaction array
     */
    static Hashtable htable = new Hashtable();

    /**
     * Returns the singleton instance of RxInteractionData.
     *
     * @return RxInteractionData the singleton cache instance
     */
    public static RxInteractionData getInstance() {
        return rxInteractionData;
    }

    /**
     * Tracks in-progress interaction checks to prevent duplicate work.
     * Key: Integer hash of ATC code vector, Value: RxInteractionWorker thread
     */
    static Hashtable working = new Hashtable();


    /**
     * Private constructor enforces singleton pattern.
     */
    private RxInteractionData() {
    }

    /**
     * Initiates background loading of interaction data for a set of ATC codes.
     *
     * This method launches a background thread to check for interactions between
     * the specified drugs. It's useful for preloading interaction data while the
     * user is still working on a prescription, improving perceived performance.
     *
     * If the interaction check is already cached or in progress, no new work is started.
     *
     * @param atccodes Vector of String ATC codes to check for interactions
     */
    public void preloadInteraction(Vector atccodes) {
        MiscUtils.getLogger().debug("PRELOADING" + atccodes.hashCode());
        if (!htable.containsKey(Integer.valueOf(atccodes.hashCode()))) {
            // Launch background thread to check interactions
            RxInteractionWorker worker = new RxInteractionWorker(rxInteractionData, atccodes);
            worker.start();
            addToWorking(atccodes, worker);
        }
    }

    /**
     * Adds interaction results to the cache.
     *
     * @param atccodes Vector of String ATC codes that were checked
     * @param interact Interaction[] array of found interactions
     */
    public void addToHash(Vector atccodes, RxDrugData.Interaction[] interact) {
        htable.put(Integer.valueOf(atccodes.hashCode()), interact);
    }

    /**
     * Registers an in-progress interaction check worker.
     *
     * @param atccodes Vector of String ATC codes being checked
     * @param worker RxInteractionWorker thread performing the check
     */
    public void addToWorking(Vector atccodes, RxInteractionWorker worker) {
        working.put(Integer.valueOf(atccodes.hashCode()), worker);
    }

    /**
     * Removes a completed interaction check from the working set.
     *
     * @param atccodes Vector of String ATC codes that were checked
     */
    public void removeFromWorking(Vector atccodes) {
        working.remove(Integer.valueOf(atccodes.hashCode()));
    }

    /**
     * Retrieves drug interactions for a set of ATC codes.
     *
     * This method implements a three-tier strategy:
     * 1. Returns cached results immediately if available
     * 2. Waits for in-progress checks to complete if already running
     * 3. Performs a new interaction check if no cached data exists
     *
     * The method is thread-safe and will block if waiting for an in-progress
     * check to complete. Results are automatically cached for future use.
     *
     * @param atccodes Vector of String ATC codes to check for interactions
     * @return Interaction[] array of drug interactions, or null if check fails
     */
    public RxDrugData.Interaction[] getInteractions(Vector atccodes) {
        RxDrugData.Interaction[] interact = null;
        MiscUtils.getLogger().debug("h table size " + htable.size() + "RxInteractionData.getInteraction atc code val  " + atccodes.hashCode());
        Integer i = Integer.valueOf(atccodes.hashCode());

        if (htable.containsKey(i)) {
            // Return cached results
            MiscUtils.getLogger().debug("Already been searched!");
            interact = (RxDrugData.Interaction[]) htable.get(i);

        } else if (working.contains(i)) {
            // Wait for in-progress check to complete
            MiscUtils.getLogger().debug("Already been searched but not finished !");
            RxInteractionWorker worker = (RxInteractionWorker) working.get(i);
            if (worker != null) {
                try {
                    worker.join();
                    MiscUtils.getLogger().debug("Already been searched now finished!");
                } catch (InterruptedException e) {
                    MiscUtils.getLogger().debug("Already been searched PROBLEM!");
                    MiscUtils.getLogger().error("Error", e);
                }
            }
            interact = (RxDrugData.Interaction[]) htable.get(i);

        } else {
            // Perform new interaction check
            MiscUtils.getLogger().debug("NEW ATC CODES");
            try {
                RxDrugData drugData = new RxDrugData();
                interact = drugData.getInteractions(atccodes);
                if (interact != null) {
                    addToHash(atccodes, interact);
                }
            } catch (Exception e) {
                MiscUtils.getLogger().error("Error", e);
            }
        }
        return interact;
    }


}
