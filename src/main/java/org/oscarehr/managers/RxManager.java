//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2013-2015. Department of Computer Science, University of Victoria. All Rights Reserved.
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
 * Department of Computer Science
 * LeadLab
 * University of Victoria
 * Victoria, Canada
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package org.oscarehr.managers;

import org.oscarehr.common.model.Drug;
import org.oscarehr.common.model.Favorite;
import org.oscarehr.common.model.Prescription;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.ws.rest.to.model.RxStatus;

import java.util.List;


public interface RxManager {

    //public static class PrescriptionDrugs{};
    List<Drug> getDrugs(LoggedInInfo info, int demographicNo, String status)
            throws UnsupportedOperationException;

    List<Drug> getDrugs(LoggedInInfo info, int demographicNo, RxStatus status);

    List<String> getCurrentSingleLineMedications(LoggedInInfo loggedInInfo, int demographicNo);

    Drug getDrug(LoggedInInfo info, int drugId) throws UnsupportedOperationException;

    Drug addDrug(LoggedInInfo info, Drug d);

    Drug updateDrug(LoggedInInfo info, Drug d);

    boolean discontinue(LoggedInInfo info, int drugId, int demographicId, String reason);

    RxManagerImpl.PrescriptionDrugs prescribe(LoggedInInfo info, List<Drug> drugs, Integer demoNo);

    List<Drug> getHistory(Integer id, LoggedInInfo info, Integer demographicNo);

    List<Favorite> getFavorites(String pid);

    Boolean addFavorite(Favorite f);

    /**
     * Archives a drug. This will remove the drug from the current list. The drug can still be found under the 'All' section.
     *
     * @param info          Logged-in user information.
     * @param drugId        The ID of the drug to archive.
     * @param demographicId The demographic ID.
     * @param reason        The reason for archiving the drug.
     * @return True if the drug was successfully archived, false otherwise.
     */
    boolean archiveDrug(LoggedInInfo info, int drugId, int demographicId, String reason);

    List<Drug> getLongTermDrugs(LoggedInInfo info, int demographicNo);


    /**
     * Utility class for returning a prescription and associated drugs.
     */
    class PrescriptionDrugs {
        public Prescription prescription;
        public List<Drug> drugs;

        public PrescriptionDrugs(Prescription p, List<Drug> d) {
            this.prescription = p;
            this.drugs = d;
        }

    }

    // statuses for drugs
    String ALL = "all";
    String CURRENT = "current";
    String ARCHIVED = "archived";

}
