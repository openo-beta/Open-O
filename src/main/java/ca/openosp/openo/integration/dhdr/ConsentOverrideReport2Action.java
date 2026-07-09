package ca.openosp.openo.integration.dhdr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import ca.openosp.openo.integration.dhdr.ConsentOverrideReportService.Row;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Renders the DHDR Temporary Consent Unblock report (DHDR13.02).
 *
 * <p>The report lists every temporary consent-unblock (override) decision recorded against the
 * Digital Health Drug Repository. This action binds the search parameters, guards access, and hands
 * off to {@link ConsentOverrideReportService}, which builds the rows; see that class for the
 * report's data source and its deliberately cross-patient scope.
 *
 * <p>This is a read-only query with lookups; it writes nothing. Access rides the {@code _rx}
 * security object (DHDR medication data), consistent with the DHDR viewer and REST endpoints.
 * Access policy for this surface (the {@code _rx} reuse versus a dedicated object or an admin role)
 * is the open question tracked in the implementation plan, not something this class decides.
 *
 * @since 2026-07-08
 */
public class ConsentOverrideReport2Action extends ActionSupport {

  private static final String SECURITY_OBJECT = "_rx";
  private static final String DATE_FORMAT = "yyyy-MM-dd";

  private HttpServletRequest request = ServletActionContext.getRequest();

  private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
  private ConsentOverrideReportService reportService =
      SpringUtils.getBean(ConsentOverrideReportService.class);

  /**
   * Builds the consent-override report, applying the optional last-name / Unique-ID search, and
   * forwards the resolved rows (plus the echoed search inputs) to the view.
   *
   * @return String the Struts result name ({@code success})
   */
  public String execute() {
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
    if (!securityInfoManager.hasPrivilege(loggedInInfo, SECURITY_OBJECT, "r", null)) {
      throw new SecurityException("missing required security object (_rx)");
    }

    String searchLastName = StringUtils.trimToNull(request.getParameter("searchLastName"));
    String searchUniqueId = StringUtils.trimToNull(request.getParameter("searchUniqueId"));
    String dateFromStr = StringUtils.trimToNull(request.getParameter("dateFrom"));
    String dateToStr = StringUtils.trimToNull(request.getParameter("dateTo"));

    // Empty date filters mean "all history": the form's date fields are blank on first load, so an
    // unbounded lower bound keeps what the user sees (no dates) consistent with what they get. A
    // supplied-but-unparseable date is not silently widened into that default - it is reported.
    List<String> invalidDates = new ArrayList<String>();
    Date from = new Date(0L);
    if (dateFromStr != null) {
      Date parsed = parseDate(dateFromStr, false);
      if (parsed == null) {
        invalidDates.add("From");
      } else {
        from = parsed;
      }
    }
    Date to = new Date();
    if (dateToStr != null) {
      Date parsed = parseDate(dateToStr, true);
      if (parsed == null) {
        invalidDates.add("To");
      } else {
        to = parsed;
      }
    }

    List<Row> rows = reportService.findRows(loggedInInfo, searchLastName, searchUniqueId, from, to);

    request.setAttribute("rows", rows);
    if (!invalidDates.isEmpty()) {
      request.setAttribute("dateWarning",
          "Ignored invalid date filter: " + String.join(", ", invalidDates)
              + ". Showing the unrestricted range for that bound.");
    }
    // Echo the raw search inputs so the form stays populated; the JSP encodes them on output.
    request.setAttribute("searchLastName", searchLastName);
    request.setAttribute("searchUniqueId", searchUniqueId);
    request.setAttribute("dateFrom", dateFromStr);
    request.setAttribute("dateTo", dateToStr);
    return "success";
  }

  /**
   * Parses a {@code yyyy-MM-dd} bound from the search form.
   *
   * @param value String the raw parameter value (non-{@code null})
   * @param endOfDay boolean {@code true} to push the result to 23:59:59.999 so an upper bound
   *     includes the whole day
   * @return Date the parsed bound, or {@code null} if the value is not a valid date
   */
  static Date parseDate(String value, boolean endOfDay) {
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    // Strict parsing: without this, "2026-13-45" would roll over into a valid but unintended date.
    format.setLenient(false);
    Date parsed;
    try {
      parsed = format.parse(value);
    } catch (ParseException e) {
      return null;
    }
    if (!endOfDay) {
      return parsed;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(parsed);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    return cal.getTime();
  }
}
