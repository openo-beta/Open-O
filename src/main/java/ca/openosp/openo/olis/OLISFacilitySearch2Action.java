package ca.openosp.openo.olis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.olis.dao.OLISFacilityDao;
import ca.openosp.openo.olis.model.OLISFacility;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

/**
 * AJAX endpoint that returns OLIS facility (Laboratory or Specimen Collection
 * Centre) autocomplete suggestions matching a query string.
 * <p>
 * Backs the typeahead pickers on {@code olis/Search.jsp} (8 lab/SCC dropdowns)
 * and {@code provider/olis_preferences.jsp} (2 default-lab dropdowns) — these
 * previously rendered a 3-entry hard-coded {@code <select>} sourced from the
 * {@code OLISParticipatingLab} enum.
 * <p>
 * Request parameters:
 * <ul>
 * <li>{@code class} - "LAB", "SCC", or "ANY" (default) - which facility class to search</li>
 * <li>{@code query} - String partial name to match (case-insensitive contains)</li>
 * </ul>
 * Response: JSON shape {@code {"results": [{"licence": "...", "name": "...", "city": "..."}, ...]}}
 * where {@code licence} is the OLIS licence number sent in the query message
 * (e.g. {@code ZBR.3.6.2}), {@code name} is the facility name, and {@code city}
 * lets the typeahead disambiguate duplicate names (e.g. the 272 LifeLabs SCCs).
 *
 * @since 2026-05-20
 */
public class OLISFacilitySearch2Action extends ActionSupport {
    private HttpServletRequest request = ServletActionContext.getRequest();
    private HttpServletResponse response = ServletActionContext.getResponse();

    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Defensive ceiling on returned rows. The full {@code OLISFacility} table is
     * ~1,267 rows today (273 LAB + 994 SCC); a query like "li" can match ~600 of
     * them. The picker's scrollable dropdown is happy to hold the whole table,
     * so this cap exists only to prevent pathological payloads if the table
     * grows 10x in some future release. Set well above the table size on
     * purpose so users see all real matches.
     */
    private static final int MAX_RESULTS = 2000;

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() throws Exception {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_lab", "r", null)) {
            throw new SecurityException("missing required sec object");
        }

        String facilityClass = request.getParameter("class");
        if (facilityClass != null) {
            facilityClass = facilityClass.trim().toUpperCase();
        }
        String query = request.getParameter("query");
        if (query == null) {
            query = "";
        }
        query = query.trim();

        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        if (!query.isEmpty()) {
            OLISFacilityDao dao = SpringUtils.getBean(OLISFacilityDao.class);
            for (OLISFacility f : dao.findByClassAndNameLike(facilityClass, query, MAX_RESULTS)) {
                results.add(toEntry(f));
            }
        }

        Map<String, Object> wrap = new HashMap<String, Object>();
        wrap.put("results", results);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(wrap));
        response.getWriter().flush();
        return null;
    }

    private static Map<String, String> toEntry(OLISFacility f) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("licence", f.getLicenceNumber() == null ? "" : f.getLicenceNumber());
        m.put("name", f.getName() == null ? "" : f.getName().trim());
        m.put("addressLine1", f.getAddressLine1() == null ? "" : f.getAddressLine1().trim());
        m.put("city", f.getCity() == null ? "" : f.getCity().trim());
        m.put("facilityClass", f.getFacilityClass() == null ? "" : f.getFacilityClass());
        return m;
    }
}
