package ca.openosp.openo.olis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.olis.dao.OLISRequestNomenclatureDao;
import ca.openosp.openo.olis.dao.OLISResultNomenclatureDao;
import ca.openosp.openo.olis.model.OLISRequestNomenclature;
import ca.openosp.openo.olis.model.OLISResultNomenclature;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * AJAX endpoint that returns OLIS nomenclature (test result or test request)
 * autocomplete suggestions matching a query string.
 * <p>
 * Replaces the previous pattern of rendering all ~48,200 result-nomenclature
 * rows plus ~3,000 request-nomenclature rows as inline {@code <option>}
 * elements on every OLIS Search.jsp render. The endpoint accepts a
 * partial-name query and returns at most {@link #MAX_RESULTS} matches as
 * JSON suitable for the YUI XHRDataSource pattern already used elsewhere
 * on the page (e.g. the patient field).
 * <p>
 * Request parameters:
 * <ul>
 * <li>{@code type} - "result" (default) or "request" - which nomenclature table to search</li>
 * <li>{@code query} - String partial name to match (case-insensitive contains)</li>
 * </ul>
 * Response: JSON shape {@code {"results": [{"code": "...", "name": "..."}, ...]}}
 * where {@code code} is the OLIS nomenclature wire-format code (LOINC for results,
 * the TR-prefixed OLIS code for requests) — that is, the value OLIS expects in
 * OBX-3 / OBR-4 component 1, not the local Hibernate primary key.
 *
 * @since 2026-05-15
 */
public class OLISNomenclatureSearch2Action extends ActionSupport {
    private HttpServletRequest request = ServletActionContext.getRequest();
    private HttpServletResponse response = ServletActionContext.getResponse();

    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Defensive ceiling on returned rows. The result + request nomenclature
     * tables together hold ~52,800 rows; common search terms like "glucose"
     * can match ~80 LOINC variants. The previous 25-cap silently clipped
     * legitimate matches and could mislead a user into picking the wrong
     * code thinking those were all that existed. 500 covers realistic
     * queries while keeping payload sensible for the broader nomenclature
     * table (40x larger than facility). The scrollable autocomplete
     * dropdown shipped alongside the facility-picker work renders the
     * larger result lists usably.
     */
    private static final int MAX_RESULTS = 500;

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() throws Exception {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "r", null)) {
            MiscUtils.getLogger().warn("Security violation: provider "
                    + (loggedInInfo != null ? loggedInInfo.getLoggedInProviderNo() : "unknown")
                    + " denied access to OLIS nomenclature search (_lab)");
            throw new SecurityException("missing required sec object");
        }

        String type = request.getParameter("type");
        String query = request.getParameter("query");
        if (query == null) {
            query = "";
        }
        query = query.trim();

        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        if (!query.isEmpty()) {
            if ("request".equalsIgnoreCase(type)) {
                OLISRequestNomenclatureDao dao = SpringUtils.getBean(OLISRequestNomenclatureDao.class);
                for (OLISRequestNomenclature n : dao.findByNameLike(query, MAX_RESULTS)) {
                    results.add(toEntry(n.getNameId(), n.getName()));
                }
            } else {
                OLISResultNomenclatureDao dao = SpringUtils.getBean(OLISResultNomenclatureDao.class);
                for (OLISResultNomenclature n : dao.findByNameLike(query, MAX_RESULTS)) {
                    results.add(toEntry(n.getNameId(), n.getName()));
                }
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

    private static Map<String, String> toEntry(String code, String name) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("code", code == null ? "" : code);
        m.put("name", name == null ? "" : name.trim());
        return m;
    }
}
