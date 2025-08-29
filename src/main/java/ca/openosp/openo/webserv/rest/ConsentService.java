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
 * 
 */
package ca.openosp.openo.webserv.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.commn.model.ConsentType;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.managers.OscarLogManager;
import ca.openosp.openo.managers.PatientConsentManager;
import ca.openosp.openo.managers.ProviderManager2;
import ca.openosp.openo.webserv.rest.to.AbstractSearchResponse;
import ca.openosp.openo.webserv.rest.to.GenericRESTResponse;
import ca.openosp.openo.webserv.rest.to.model.ConsentTypeTo1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST service for consent-related operations using OAuth 1.0a authentication.
 * 
 * This service provides endpoints for managing consent types and patient consents.
 * It uses ScribeJava OAuth1 for authentication.
 */
@Component("ConsentService")
@Path("/consentService/")
@Produces("application/xml")
public class ConsentService extends AbstractServiceImpl {

    private static final Logger logger = MiscUtils.getLogger();

    @Autowired
    ProviderDao providerDao;

    @Autowired
    ProviderManager2 providerManager;

    @Autowired
    OscarLogManager oscarLogManager;

    @Autowired
    DemographicManager demographicManager;

    @Autowired
    PatientConsentManager patientConsentManager;



    public ConsentService() {
    }

    /**
     * Retrieves all active consent types.
     * 
     * @return AbstractSearchResponse containing list of active consent types
     */
    @GET
    @Path("/consentTypes")
    @Produces("application/json")
    public AbstractSearchResponse<ConsentTypeTo1> getActiveConsentTypes() {
        try {
            logger.debug("Retrieving active consent types");

            List<ConsentType> consents = patientConsentManager.getActiveConsentTypes();
            List<ConsentTypeTo1> consentTypes = new ArrayList<ConsentTypeTo1>();

            for (ConsentType consent : consents) {
                ConsentTypeTo1 consentTypeTo1 = new ConsentTypeTo1();
                consentTypeTo1.setActive(consent.isActive());
                consentTypeTo1.setDescription(consent.getDescription());
                consentTypeTo1.setId(consent.getId());
                consentTypeTo1.setName(consent.getName());
                consentTypeTo1.setProviderNo(consent.getProviderNo());
                consentTypeTo1.setRemoteEnabled(consent.isRemoteEnabled());
                consentTypeTo1.setType(consent.getType());
                consentTypes.add(consentTypeTo1);
            }

            AbstractSearchResponse<ConsentTypeTo1> response = new AbstractSearchResponse<ConsentTypeTo1>();
            response.setContent(consentTypes);
            response.setTimestamp(new Date());
            response.setTotal(consentTypes.size());

            logger.info("Successfully retrieved {} consent types", consentTypes.size());
            return response;
            
        } catch (javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving consent types: {}", e.getMessage(), e);
            throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a specific consent type by ID.
     * 
     * @param id The consent type ID
     * @return ConsentTypeTo1 object
     */
    @GET
    @Path("/consentType/{id}")
    @Produces("application/json")
    public ConsentTypeTo1 getConsentType(@PathParam("id") Integer id) {
        try {
            logger.debug("Retrieving consent type {}", id);

            ConsentType consent = patientConsentManager.getConsentTypeByConsentTypeId(id);
            if (consent == null) {
                logger.warn("Consent type not found: {}", id);
                throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.NOT_FOUND);
            }

            ConsentTypeTo1 consentTypeTo1 = new ConsentTypeTo1();
            consentTypeTo1.setActive(consent.isActive());
            consentTypeTo1.setDescription(consent.getDescription());
            consentTypeTo1.setId(consent.getId());
            consentTypeTo1.setName(consent.getName());
            consentTypeTo1.setProviderNo(consent.getProviderNo());
            consentTypeTo1.setRemoteEnabled(consent.isRemoteEnabled());
            consentTypeTo1.setType(consent.getType());

            logger.info("Successfully retrieved consent type: {}", id);
            return consentTypeTo1;
            
        } catch (javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving consent type {}: {}", id, e.getMessage(), e);
            throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Adds a new consent type.
     * 
     * @param consentType The consent type to add
     * @return GenericRESTResponse indicating success or failure
     */
    @POST
    @Path("/consentType")
    @Produces("application/json")
    @Consumes("application/json")
    public GenericRESTResponse addConsentType(ConsentTypeTo1 consentType) {
        try {
            logger.debug("Adding consent type");

            if (consentType == null) {
                logger.warn("Consent type data is null");
                throw new javax.ws.rs.WebApplicationException(
                    Response.Status.BAD_REQUEST);
            }

            ConsentType ct = new ConsentType();
            ct.setActive(true);
            ct.setDescription(consentType.getDescription());
            ct.setName(consentType.getName());
            ct.setProviderNo(consentType.getProviderNo());
            ct.setRemoteEnabled(consentType.isRemoteEnabled());
            ct.setType(consentType.getType());

            patientConsentManager.addConsentType(getLoggedInInfo(), ct);

            logger.info("Successfully added consent type: {}", consentType.getName());
            return new GenericRESTResponse(true, "Consent type added successfully.");

        } catch (javax.ws.rs.WebApplicationException e) {
            // propagate JAX-RS errors (e.g. 400 Bad Request)
            throw e;
        } catch (Exception e) {
            // log full stack trace internally
            logger.error("Error adding consent type {}", consentType, e);
            // return a generic message to the client
            return new GenericRESTResponse(false,
                "An error occurred while processing your request.");
        }
    }


}
