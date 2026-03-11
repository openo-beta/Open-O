//CHECKSTYLE:OFF
package ca.openosp.openo.encounter.oscarConsultationRequest.config.pageUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.opensymphony.xwork2.ActionSupport;

import ca.openosp.openo.commn.dao.ProfessionalSpecialistDao;
import ca.openosp.openo.commn.dao.ServiceSpecialistsDao;
import ca.openosp.openo.commn.model.ServiceSpecialists;
import ca.openosp.openo.consultation.dto.SpecialistListDTO;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.util.ConversionUtils;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Struts2 action providing JSON endpoints for specialist list data.
 *
 * <p>Serves specialist data as a separate AJAX endpoint so JSP pages load a lightweight
 * shell first, then fetch data via {@code fetch()} POST. This decouples the page shell
 * from the data payload, reducing perceived load time and keeping the HTML response small.</p>
 *
 * <p>Endpoints (all POST, CSRF-protected via CsrfGuard token header):</p>
 * <ul>
 *   <li>{@code method=getSpecialists} - Returns all specialists for the EditSpecialists page.
 *       Each row: {@code [id, "name", "address", "phone", "fax"]}</li>
 *   <li>{@code method=getSpecialistsForService} - Returns all specialists with checked state
 *       for the DisplayService page. Each row: {@code [id, "name", "address", "phone", "fax", checked]}</li>
 * </ul>
 *
 * @since 2026-03-10
 */
public class SpecialistList2Action extends ActionSupport {

    private HttpServletRequest request = ServletActionContext.getRequest();
    private HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private ProfessionalSpecialistDao professionalSpecialistDao = SpringUtils.getBean(ProfessionalSpecialistDao.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Main dispatcher that routes to the appropriate method based on 'method' parameter.
     *
     * @return String action result (null when response is written directly)
     */
    @Override
    public String execute() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_con", "r", null)) {
            throw new SecurityException("missing required security object (_con)");
        }

        String method = request.getParameter("method");

        if ("getSpecialists".equals(method)) {
            return getSpecialists();
        } else if ("getSpecialistsForService".equals(method)) {
            return getSpecialistsForService();
        }

        MiscUtils.getLogger().warn("SpecialistList2Action: invalid method parameter: " + method);
        try {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing method parameter");
        } catch (IOException e) {
            MiscUtils.getLogger().error("Error sending 400 response", e);
        }
        return null;
    }

    /**
     * Returns all specialists as a JSON array for the EditSpecialists page.
     *
     * <p>Each row is {@code [id, "name", "address", "phone", "fax"]}.</p>
     *
     * @return null (response written directly to output stream)
     */
    private String getSpecialists() {
        try {
            List<SpecialistListDTO> specialists = professionalSpecialistDao.findAllListDTOs();

            ArrayNode array = MAPPER.createArrayNode();
            for (SpecialistListDTO dto : specialists) {
                ArrayNode row = MAPPER.createArrayNode();
                row.add(dto.getId());
                row.add(buildDisplayName(dto));
                row.add(StringUtils.defaultString(dto.getStreetAddress()));
                row.add(StringUtils.defaultString(dto.getPhoneNumber()));
                row.add(StringUtils.defaultString(dto.getFaxNumber()));
                array.add(row);
            }

            writeJsonResponse(array);
            return null;

        } catch (Exception e) {
            return handleServerError("Error loading specialist list", e);
        }
    }

    /**
     * Returns all specialists with checked state for the DisplayService page.
     *
     * <p>Each row is {@code [id, "name", "address", "phone", "fax", checked]}.
     * The {@code checked} boolean indicates whether the specialist is assigned to
     * the given service.</p>
     *
     * @return null (response written directly to output stream)
     */
    private String getSpecialistsForService() {
        try {
            String serviceIdParam = request.getParameter("serviceId");
            Integer serviceId = ConversionUtils.fromIntString(serviceIdParam);

            if (serviceId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid serviceId parameter");
                return null;
            }

            List<SpecialistListDTO> specialists = professionalSpecialistDao.findAllListDTOs();
            Set<String> assignedIds = loadAssignedSpecialistIds(serviceId);

            ArrayNode array = MAPPER.createArrayNode();
            for (SpecialistListDTO dto : specialists) {
                ArrayNode row = MAPPER.createArrayNode();
                row.add(dto.getId());
                row.add(buildDisplayName(dto));
                row.add(StringUtils.defaultString(dto.getStreetAddress()));
                row.add(StringUtils.defaultString(dto.getPhoneNumber()));
                row.add(StringUtils.defaultString(dto.getFaxNumber()));
                row.add(assignedIds.contains(dto.getId().toString()));
                array.add(row);
            }

            writeJsonResponse(array);
            return null;

        } catch (Exception e) {
            return handleServerError("Error loading specialist list for service", e);
        }
    }

    /**
     * Loads the set of specialist IDs assigned to a given service.
     *
     * @param serviceId Integer the consultation service ID
     * @return Set&lt;String&gt; specialist IDs assigned to the service
     */
    private Set<String> loadAssignedSpecialistIds(Integer serviceId) {
        ServiceSpecialistsDao dao = SpringUtils.getBean(ServiceSpecialistsDao.class);
        Set<String> ids = new HashSet<>();
        for (ServiceSpecialists ss : dao.findByServiceId(serviceId)) {
            ids.add(String.valueOf(ss.getId().getSpecId()));
        }
        return ids;
    }

    /**
     * Formats a specialist name for display: "LastName FirstName ProLetters".
     *
     * @param dto SpecialistListDTO the specialist data
     * @return String the formatted display name
     */
    private static String buildDisplayName(SpecialistListDTO dto) {
        String lName = StringUtils.defaultString(dto.getLastName());
        String fName = StringUtils.defaultString(dto.getFirstName());
        String proLetters = dto.getProfessionalLetters();
        StringBuilder name = new StringBuilder();
        name.append(lName).append(" ").append(fName);
        if (StringUtils.isNotBlank(proLetters)) {
            name.append(" ").append(proLetters);
        }
        return name.toString();
    }

    /**
     * Writes a JSON response to the output stream.
     *
     * @param data ArrayNode the JSON data to write
     * @throws IOException if writing to response fails
     */
    private void writeJsonResponse(ArrayNode data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getWriter(), data);
    }

    /**
     * Handles server errors by logging and sending an HTTP 500 response.
     *
     * @param logMessage String message to log
     * @param e Exception that occurred
     * @return null (response written directly)
     */
    private String handleServerError(String logMessage, Exception e) {
        MiscUtils.getLogger().error(logMessage, e);
        try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred loading specialist data");
        } catch (IOException ioException) {
            MiscUtils.getLogger().error("Error sending error response", ioException);
        }
        return null;
    }
}
