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
 * Migrated from CXF OAuth2 to ScribeJava OAuth1 implementation.
 */
package org.oscarehr.ws.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsDateJsonBeanProcessor;

import org.apache.logging.log4j.Logger;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Provider;
import org.oscarehr.managers.DemographicManager;
import org.oscarehr.managers.OscarLogManager;
import org.oscarehr.managers.ProviderManager2;
import org.oscarehr.managers.model.ProviderSettings;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.web.PatientListApptBean;
import org.oscarehr.web.PatientListApptItemBean;
import org.oscarehr.ws.rest.conversion.ProviderConverter;
import org.oscarehr.ws.rest.to.AbstractSearchResponse;
import org.oscarehr.ws.rest.to.GenericRESTResponse;
import org.oscarehr.ws.rest.to.model.ProviderTo1;
import org.oscarehr.ws.transfer_objects.ProviderTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * REST service for provider-related operations using OAuth 1.0a authentication.
 * 
 * This service provides endpoints for managing providers, retrieving provider information,
 * and handling provider settings. It uses ScribeJava OAuth1 for authentication.
 */
@Component("ProviderService")
@Path("/providerService/")
@Produces("application/xml")
public class ProviderService extends AbstractServiceImpl {

    private static final Logger logger = MiscUtils.getLogger();

    @Autowired
    ProviderDao providerDao;

    @Autowired
    ProviderManager2 providerManager;

    @Autowired
    OscarLogManager oscarLogManager;

    @Autowired
    DemographicManager demographicManager;



    public ProviderService() {
    }

    /**
     * Retrieves all active providers.
     * 
     * @return OscarSearchResponse containing list of providers
     * @deprecated Use getProvidersAsJSON instead
     */
    @GET
    @Path("/providers")
    @Deprecated
    public org.oscarehr.ws.rest.to.OscarSearchResponse<ProviderTransfer> getProviders() {
        try {
            // Validate OAuth1 access token
            com.github.scribejava.core.model.OAuth1AccessToken accessToken = getOAuthAccessToken();
            if (accessToken == null) {
                logger.warn("No valid OAuth1 access token found for providers request");
                throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.UNAUTHORIZED);
            }

            logger.debug("Retrieving active providers with OAuth1 token: {}", accessToken.getToken());

            org.oscarehr.ws.rest.to.OscarSearchResponse<ProviderTransfer> lst = new
                    org.oscarehr.ws.rest.to.OscarSearchResponse<ProviderTransfer>();

            for (Provider p : providerDao.getActiveProviders()) {
                lst.getContent().add(ProviderTransfer.toTransfer(p));
            }
            lst.setTimestamp(new Date());
            lst.setTotal(lst.getContent().size());

            logger.info("Successfully retrieved {} providers", lst.getTotal());
            return lst;
            
        } catch (Exception e) {
            logger.error("Error retrieving providers: {}", e.getMessage(), e);
            throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all active providers as JSON.
     * 
     * @return AbstractSearchResponse containing list of providers in JSON format
     */
    @GET
    @Path("/providers_json")
    @Produces("application/json")
    public AbstractSearchResponse<ProviderTo1> getProvidersAsJSON() {
        try {
            // Validate OAuth1 access token
            com.github.scribejava.core.model.OAuth1AccessToken accessToken = getOAuthAccessToken();
            if (accessToken == null) {
                logger.warn("No valid OAuth1 access token found for providers JSON request");
                throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.UNAUTHORIZED);
            }

            logger.debug("Retrieving active providers as JSON with OAuth1 token: {}", accessToken.getToken());

            JsonConfig config = new JsonConfig();
            config.registerJsonBeanProcessor(java.sql.Date.class, new JsDateJsonBeanProcessor());

            List<ProviderTo1> providers = new ProviderConverter().getAllAsTransferObjects(getLoggedInInfo(), providerDao.getActiveProviders());

            AbstractSearchResponse<ProviderTo1> response = new AbstractSearchResponse<ProviderTo1>();
            response.setContent(providers);
            response.setTimestamp(new Date());
            response.setTotal(response.getContent().size());

            logger.info("Successfully retrieved {} providers as JSON", response.getTotal());
            return response;
            
        } catch (Exception e) {
            logger.error("Error retrieving providers as JSON: {}", e.getMessage(), e);
            throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a specific provider by ID.
     * 
     * @param id The provider ID
     * @return ProviderTransfer object
     */
    @GET
    @Path("/provider/{id}")
    @Produces({"application/xml", "application/json"})
    public ProviderTransfer getProvider(@PathParam("id") String id) {
        try {
            // Validate OAuth1 access token
            com.github.scribejava.core.model.OAuth1AccessToken accessToken = getOAuthAccessToken();
            if (accessToken == null) {
                logger.warn("No valid OAuth1 access token found for provider request: {}", id);
                throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.UNAUTHORIZED);
            }

            logger.debug("Retrieving provider {} with OAuth1 token: {}", id, accessToken.getToken());

            Provider provider = providerDao.getProvider(id);
            if (provider == null) {
                logger.warn("Provider not found: {}", id);
                throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.NOT_FOUND);
            }

            logger.info("Successfully retrieved provider: {}", id);
            return ProviderTransfer.toTransfer(provider);
            
        } catch (javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving provider {}: {}", id, e.getMessage(), e);
            throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/provider/me")
    @Produces("application/json")
    public String getLoggedInProvider() {
        Provider provider = getLoggedInInfo().getLoggedInProvider();

        if (provider != null) {
            JsonConfig config = new JsonConfig();
            config.registerJsonBeanProcessor(java.sql.Date.class, new JsDateJsonBeanProcessor());
            return JSONObject.fromObject(provider, config).toString();
        }
        return null;
    }

    @GET
    @Path("/providerjson/{id}")
    public String getProviderAsJSON(@PathParam("id") String id) {
        JsonConfig config = new JsonConfig();
        config.registerJsonBeanProcessor(java.sql.Date.class, new JsDateJsonBeanProcessor());
        return JSONObject.fromObject(providerDao.getProvider(id), config).toString();
    }

    @GET
    @Path("/providers/bad")
    public Response getBadRequest() {
        return Response.status(Status.BAD_REQUEST).build();
    }

    @POST
    @Path("/providers/search")
    @Produces("application/json")
    @Consumes("application/json")
    public AbstractSearchResponse<ProviderTo1> search(JSONObject json, @QueryParam("startIndex") Integer startIndex, @QueryParam("itemsToReturn") Integer itemsToReturn) {
        AbstractSearchResponse<ProviderTo1> response = new AbstractSearchResponse<ProviderTo1>();

        int startIndexVal = startIndex == null ? 0 : startIndex.intValue();
        int itemsToReturnVal = itemsToReturn == null ? 5000 : itemsToReturn.intValue();
        boolean active = Boolean.valueOf(json.getString("active"));

        String term = null;
        if (json.containsKey("searchTerm")) {
            term = json.getString("searchTerm");
        }

        List<Provider> results = providerManager.search(getLoggedInInfo(), term, active, startIndexVal, itemsToReturnVal);


        ProviderConverter converter = new ProviderConverter();
        response.setContent(converter.getAllAsTransferObjects(getLoggedInInfo(), results));
        response.setTotal(response.getContent().size());

        return response;
    }

    @GET
    @Path("/getRecentDemographicsViewed")
    @Produces("application/json")
    public PatientListApptBean getRecentDemographicsViewed(@QueryParam("startIndex") Integer startIndex, @QueryParam("itemsToReturn") Integer itemsToReturn) {
        List<Object[]> results = oscarLogManager.getRecentDemographicsViewedByProvider(getLoggedInInfo(), getLoggedInInfo().getLoggedInProviderNo(), startIndex, itemsToReturn);

        return convertRecentViewedDemographicsResults(results);
    }

    @GET
    @Path("/getRecentDemographicsViewedAfterDateIncluded")
    @Produces("application/json")
    public PatientListApptBean getRecentDemographicsViewed(@QueryParam("startIndex") Integer startIndex, @QueryParam("itemsToReturn") Integer itemsToReturn, @QueryParam("date") String dateStr) {
        Date date = new Date(Long.parseLong(dateStr));
        List<Object[]> results = oscarLogManager.getRecentDemographicsViewedByProviderAfterDateIncluded(getLoggedInInfo(), getLoggedInInfo().getLoggedInProviderNo(), date, startIndex, itemsToReturn);

        return convertRecentViewedDemographicsResults(results);
    }

    private PatientListApptBean convertRecentViewedDemographicsResults(List<Object[]> results) {
        PatientListApptBean response = new PatientListApptBean();

        for (Object[] r : results) {
            Demographic d = demographicManager.getDemographic(getLoggedInInfo(), (Integer) r[0]);
            if (d != null) {
                PatientListApptItemBean item = new PatientListApptItemBean();
                item.setDemographicNo((Integer) r[0]);
                item.setDate((Date) r[1]);
                item.setName(d.getFormattedName());
                response.getPatients().add(item);
            }
        }

        return response;
    }

    @GET
    @Path("/getActiveTeams")
    @Produces("application/json")
    public AbstractSearchResponse<String> getActiveTeams() {
        List<String> teams = providerManager.getActiveTeams(getLoggedInInfo());

        AbstractSearchResponse<String> response = new AbstractSearchResponse<String>();

        response.setContent(teams);
        response.setTotal(response.getContent().size());
        return response;
    }

    @GET
    @Path("/settings/get")
    @Produces("application/json")
    public AbstractSearchResponse<ProviderSettings> getProviderSettings() {
        AbstractSearchResponse<ProviderSettings> response = new AbstractSearchResponse<ProviderSettings>();

        ProviderSettings settings = providerManager.getProviderSettings(getLoggedInInfo(), getLoggedInInfo().getLoggedInProviderNo());
        List<ProviderSettings> content = new ArrayList<ProviderSettings>();
        content.add(settings);
        response.setContent(content);
        response.setTotal(1);
        return response;
    }

    @POST
    @Path("/settings/{providerNo}/save")
    @Produces("application/json")
    @Consumes("application/json")
    public GenericRESTResponse saveProviderSettings(ProviderSettings json, @PathParam("providerNo") String providerNo) {
        GenericRESTResponse response = new GenericRESTResponse();

        MiscUtils.getLogger().warn(json.toString());

        providerManager.updateProviderSettings(getLoggedInInfo(), providerNo, json);
        return response;
    }

    @GET
    @Path("/suggestProviderNo")
    @Produces("application/json")
    public GenericRESTResponse suggestProviderNo() {

        List<Provider> providers = providerManager.getProviders(getLoggedInInfo(), null);
        List<Integer> providerList = new ArrayList<Integer>();
        for (Provider h : providers) {
            try {
                String pn = h.getProviderNo();
                providerList.add(Integer.valueOf(pn));
            } catch (
                    Exception e) {/*empty*/} /*No need to do anything. Just want to avoid a NumberFormatException from provider numbers with alphanumeric Characters*/
        }

        String suggestProviderNo = "";
        for (Integer i = 1; i < 1000000; i++) {
            if (!providerList.contains(i)) {
                suggestProviderNo = i.toString();
                break;
            }
        }


        return new GenericRESTResponse(true, suggestProviderNo);
    }

}
