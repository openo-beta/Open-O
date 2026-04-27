package ca.openosp.openo.webserv.rest;

import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/llm")
public class LLMExtractorEndpoint {

    @Autowired
    private LLMExtractorService llmExtractorService;

    @GET
    @Path("/export")
    @Produces(MediaType.TEXT_PLAIN)
    public String exportPatient(
            @QueryParam("demographicId") Integer demographicId,
            @Context HttpServletRequest request) {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromRequest(request);
        return llmExtractorService.generatePatientExport(loggedInInfo, demographicId);
    }
}