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
package ca.openosp.openo.webserv.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ca.openosp.openo.commn.model.DemographicMerge;
import ca.openosp.openo.demographic.merge.DemographicMergeManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.webserv.rest.conversion.DemographicMergedConverter;
import ca.openosp.openo.webserv.rest.to.OscarSearchResponse;
import ca.openosp.openo.webserv.rest.to.model.DemographicMergedTo1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST service for demographic merge operations.
 * <p>
 * Exposes endpoints to query merge history, merge two patient records,
 * and unmerge a previously merged record.
 *
 * @since 2026-04-13
 */
@Path("/demographics/merge")
@Component("demographicMergeService")
@Consumes(MediaType.APPLICATION_JSON)
public class DemographicMergeService extends AbstractServiceImpl {

    @Autowired
    private DemographicMergeManager demographicMergeManager;

    @Autowired
    private SecurityInfoManager securityInfoManager;

    /**
     * Gets the merge event for the given merged demographic (C).
     * Pass the demographic_no of the merged output record to retrieve its merge history.
     *
     * @param mergedDemographicNo Integer the demographic_no of the merged record (C)
     * @return OscarSearchResponse&lt;DemographicMergedTo1&gt; the merge event for the given demographic, or empty if not found
     */
    @GET
    @Path("/{mergedDemographicNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public OscarSearchResponse<DemographicMergedTo1> getMergeEvent(@PathParam("mergedDemographicNo") Integer mergedDemographicNo) {
        if (!securityInfoManager.hasPrivilege(getLoggedInInfo(), "_demographic", SecurityInfoManager.READ, null)) {
            throw new SecurityException("missing required privilege: _demographic/r");
        }
        DemographicMergedConverter converter = new DemographicMergedConverter();
        Map<Integer, DemographicMerge> events = demographicMergeManager.findMergeEventsForDemographics(
                getLoggedInInfo(), Collections.singletonList(mergedDemographicNo));
        OscarSearchResponse<DemographicMergedTo1> response = new OscarSearchResponse<DemographicMergedTo1>();
        for (DemographicMerge dm : events.values()) {
            response.getContent().add(converter.getAsTransferObject(getLoggedInInfo(), dm));
        }
        return response;
    }

    /**
     * Merges a secondary patient record into a new record cloned from the primary.
     *
     * @param parentId Integer the demographic_no of the primary patient (A)
     * @param childId  Integer the demographic_no of the secondary patient (B) to merge
     */
    @PUT
    @Path("/")
    public void mergeDemographic(@QueryParam("parentId") Integer parentId, @QueryParam("childId") Integer childId) {
        if (!securityInfoManager.hasPrivilege(getLoggedInInfo(), "_demographic", SecurityInfoManager.WRITE, null)) {
            throw new SecurityException("missing required privilege: _demographic/w");
        }
        List<Integer> children = new ArrayList<Integer>();
        children.add(childId);
        demographicMergeManager.merge(getLoggedInInfo(), parentId, children);
        demographicMergeManager.applyMergeStatuses(getLoggedInInfo(), parentId, children);
    }

    /**
     * Unmerges a previously merged demographic record, restoring the original source records.
     *
     * @param mergedDemographicNo Integer the demographic_no of the merged record (C) to unmerge
     */
    @DELETE
    @Path("/")
    public void unmergeDemographic(@QueryParam("mergedDemographicNo") Integer mergedDemographicNo) {
        if (!securityInfoManager.hasPrivilege(getLoggedInInfo(), "_demographic", SecurityInfoManager.WRITE, null)) {
            throw new SecurityException("missing required privilege: _demographic/w");
        }
        demographicMergeManager.unmerge(getLoggedInInfo(), mergedDemographicNo);
    }

}
