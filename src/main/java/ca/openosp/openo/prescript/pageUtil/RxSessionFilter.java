package ca.openosp.openo.prescript.pageUtil;

import org.apache.logging.log4j.Logger;
import org.owasp.encoder.Encode;
import ca.openosp.openo.prescript.data.RxPatientData;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet filter that swaps the correct per-patient {@link RxSessionBean} into the
 * legacy {@code "RxSessionBean"} session key before each Rx-related request.
 * <p>
 * This allows existing action and JSP code that reads
 * {@code session.getAttribute("RxSessionBean")} to transparently receive the
 * bean for the patient whose tab initiated the request, enabling multiple
 * patient Medications tabs without data cross-contamination.
 * <p>
 * Mapped in {@code web.xml} to {@code /oscarRx/*},
 * {@code /CaseManagementView.do}, and {@code /messenger/generatePreviewPDF.jsp}.
 *
 * @since 2026-01-30
 */
public class RxSessionFilter implements Filter {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String LEGACY_KEY = "RxSessionBean";
    private static final String PATIENT_KEY = "Patient";

    /**
     * {@inheritDoc}
     *
     * @param filterConfig FilterConfig the filter configuration from web.xml
     * @throws ServletException if initialization fails
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RxSessionFilter initialized");
    }

    /**
     * Extracts the {@code demographicNo} request parameter, looks up the
     * matching per-patient {@link RxSessionBean}, and sets it as the legacy
     * {@code "RxSessionBean"} session attribute so downstream actions and
     * JSPs receive the correct patient context.
     * <p>
     * If no {@code demographicNo} parameter is present, falls back to the
     * demographic stored in the current legacy bean.
     *
     * @param req ServletRequest the incoming request
     * @param res ServletResponse the outgoing response
     * @param chain FilterChain the remaining filter chain
     * @throws IOException if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false);

        if (session != null) {
            int demographicNo = parseDemographicNo(request.getParameter("demographicNo"));
            if (demographicNo <= 0) {
                demographicNo = parseDemographicNo(request.getParameter("demographic_no"));
            }
            boolean usedFallback = false;

            if (demographicNo <= 0) {
                RxSessionBean currentBean = (RxSessionBean) session.getAttribute(LEGACY_KEY);
                if (currentBean != null) {
                    demographicNo = currentBean.getDemographicNo();
                    usedFallback = true;
                }
            }

            if (demographicNo > 0) {
                RxSessionBean perPatientBean = (RxSessionBean) session.getAttribute(
                        RxSessionBean.getSessionKey(demographicNo));
                if (perPatientBean != null) {
                    session.setAttribute(LEGACY_KEY, perPatientBean);
                } else if (!usedFallback) {
                    // demographicNo was explicitly provided but no per-patient bean exists.
                    // Clear the legacy key to prevent cross-patient leakage from a stale bean.
                    session.removeAttribute(LEGACY_KEY);
                } else {
                    logger.warn("RxSessionFilter: No demographicNo param and no per-patient bean " +
                            "found for {}. Using legacy bean as-is.",
                            Encode.forJava(request.getRequestURI()));
                }

                // Swap the Patient session attribute to match the requested demographic
                RxPatientData.Patient currentPatient =
                        (RxPatientData.Patient) session.getAttribute(PATIENT_KEY);
                if (currentPatient == null || currentPatient.getDemographicNo() != demographicNo) {
                    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
                    if (loggedInInfo != null) {
                        RxPatientData.Patient patient = RxPatientData.getPatient(loggedInInfo, demographicNo);
                        if (patient != null && patient.getDemographic() != null) {
                            session.setAttribute(PATIENT_KEY, patient);
                        }
                    }
                }
            }
        }

        chain.doFilter(req, res);
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // no-op
    }

    /**
     * Parses a demographic number string into an integer.
     *
     * @param param String the raw parameter value, may be null
     * @return int the parsed demographic number, or {@code -1} if null, empty, or non-numeric
     */
    private static int parseDemographicNo(String param) {
        if (param == null || param.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
