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
package org.oscarehr.ws.rest;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.common.model.ConsentType;
import org.oscarehr.managers.DemographicManager;
import org.oscarehr.managers.OscarLogManager;
import org.oscarehr.managers.PatientConsentManager;
import org.oscarehr.managers.ProviderManager2;
import org.oscarehr.ws.rest.to.AbstractSearchResponse;
import org.oscarehr.ws.rest.to.GenericRESTResponse;
import org.oscarehr.ws.rest.to.model.ConsentTypeTo1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.scribejava.core.model.OAuth1AccessToken;

@Component("ConsentService")
@Path("/consentService/")
@Produces("application/xml")
public class ConsentService extends AbstractServiceImpl {

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

    /**
     * Get the OAuth1AccessToken from the current request.
     */
    protected OAuth1AccessToken getOAuthAccessToken() {
        HttpServletRequest request = getHttpServletRequest();
        String token = request.getParameter("oauth_token");
        String tokenSecret = request.getParameter("oauth_token_secret");
        if (token == null || tokenSecret == null) {
            throw new IllegalStateException("OAuth access token is not available in the request.");
        }
        return new OAuth1AccessToken(token, tokenSecret);
    }

    public ConsentService() {
    }

    @GET
    @Path("/consentTypes")
    @Produces("application/json")
    public AbstractSearchResponse<ConsentTypeTo1> getActiveConsentTypes() {

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

        return response;
    }

    @GET
    @Path("/consentType/{id}")
    @Produces("application/json")
    public ConsentTypeTo1 getConsentType(@PathParam("id") Integer id) {

        ConsentType consent = patientConsentManager.getConsentTypeByConsentTypeId(id);


        ConsentTypeTo1 consentTypeTo1 = new ConsentTypeTo1();
        consentTypeTo1.setActive(consent.isActive());
        consentTypeTo1.setDescription(consent.getDescription());
        consentTypeTo1.setId(consent.getId());
        consentTypeTo1.setName(consent.getName());
        consentTypeTo1.setProviderNo(consent.getProviderNo());
        consentTypeTo1.setRemoteEnabled(consent.isRemoteEnabled());
        consentTypeTo1.setType(consent.getType());


        return consentTypeTo1;
    }


    @POST
    @Path("/consentType")
    @Produces("application/json")
    @Consumes("application/json")
    public GenericRESTResponse addConsentType(ConsentTypeTo1 consentType) {
        GenericRESTResponse response = new GenericRESTResponse();


        ConsentType consentTypeToAdd = new ConsentType();

        consentTypeToAdd.setActive(true);
        consentTypeToAdd.setDescription(consentType.getDescription());
        consentTypeToAdd.setName(consentType.getName());
        consentTypeToAdd.setProviderNo(consentType.getProviderNo());
        consentTypeToAdd.setRemoteEnabled(consentType.isRemoteEnabled());
        consentTypeToAdd.setType(consentType.getType());

        patientConsentManager.addConsentType(getLoggedInInfo(), consentTypeToAdd);

        response.setSuccess(true);
        return response;
    }


}
