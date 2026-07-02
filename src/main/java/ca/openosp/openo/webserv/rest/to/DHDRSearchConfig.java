package ca.openosp.openo.webserv.rest.to;

import java.util.Date;

/**
 * Search configuration payload for a DHDR (Digital Health Drug Repository) dispense query.
 *
 * <p>Carries the optional bounding dates and pagination cursor supplied by the DHDR viewer when
 * requesting a patient's dispensed-medication history. All fields are optional; a {@code null}
 * date bound means "unbounded" on that side and a {@code null} {@code searchId}/{@code pageId}
 * requests the first page of a fresh search.</p>
 *
 * @since 2026-07-02
 */
public class DHDRSearchConfig {
  private Date startDate = null;
  private Date endDate = null;
  private String searchId = null;
  private String pageId = null;

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getSearchId() {
    return searchId;
  }

  public void setSearchId(String searchId) {
    this.searchId = searchId;
  }

  public String getPageId() {
    return pageId;
  }

  public void setPageId(String pageId) {
    this.pageId = pageId;
  }
}
