//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */
package ca.openosp.openo.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.dao.ProgramDao;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.casemgmt.dao.CaseManagementNoteDAO;
import ca.openosp.openo.casemgmt.model.CaseManagementNote;
import ca.openosp.openo.commn.dao.AdmissionDao;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.dao.FunctionalCentreDao;
import ca.openosp.openo.commn.model.Admission;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.FunctionalCentre;
import ca.openosp.openo.utility.EncounterUtil;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Management Information System (MIS) reporting UI bean for healthcare program analytics.
 *
 * This UI bean generates comprehensive statistical reports for healthcare program management,
 * providing critical data for program evaluation, funding justification, and clinical quality
 * improvement initiatives. It analyzes patient encounters, service utilization patterns,
 * and demographic characteristics across various healthcare programs.
 *
 * <p>MIS reports are essential for healthcare administration and regulatory compliance,
 * providing quantitative data on service delivery including:</p>
 * <ul>
 *   <li>Face-to-face patient encounters with clinical staff</li>
 *   <li>Telephone consultations and remote care delivery</li>
 *   <li>Service recipient demographics and anonymity classifications</li>
 *   <li>Total individuals served across programs and time periods</li>
 * </ul>
 *
 * <p>The reporting system supports both functional centre-based and program-based
 * analysis, enabling administrators to track performance metrics at different
 * organizational levels. All report data respects patient privacy classifications
 * and handles anonymous service recipients appropriately.</p>
 *
 * <p>Report data includes standardized MIS codes (e.g., 4502440, 4512440) that
 * align with healthcare reporting standards and enable data comparison across
 * different healthcare facilities and time periods.</p>
 *
 * @since March 2010
 * @see ca.openosp.openo.PMmodule.model.Program
 * @see ca.openosp.openo.casemgmt.model.CaseManagementNote
 * @see ca.openosp.openo.commn.model.FunctionalCentre
 */
public final class MisReportUIBean {

    private static Logger logger = MiscUtils.getLogger();

    private FunctionalCentreDao functionalCentreDao = (FunctionalCentreDao) SpringUtils.getBean(FunctionalCentreDao.class);
    private AdmissionDao admissionDao = (AdmissionDao) SpringUtils.getBean(AdmissionDao.class);
    private ProgramDao programDao = (ProgramDao) SpringUtils.getBean(ProgramDao.class);
    private DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
    private CaseManagementNoteDAO caseManagementNoteDAO = (CaseManagementNoteDAO) SpringUtils.getBean(CaseManagementNoteDAO.class);

    private static int ELDERLY_AGE = 65;

    /**
     * Data row representation for MIS report metrics.
     *
     * Each data row represents a specific healthcare service metric with a standardized
     * MIS reporting code, human-readable description, and numerical results. The reporting
     * codes align with healthcare industry standards for consistent data reporting across
     * different healthcare facilities and jurisdictions.
     *
     * @since March 2010
     */
    public static class DataRow {
        /** Standardized MIS reporting code for the metric */
        public int dataReportId;
        /** Human-readable description of the healthcare metric */
        public String dataReportDescription;
        /** Numerical results for the metric across reporting periods */
        public ArrayList<Integer> dataReportResult = new ArrayList<Integer>();

        /**
         * Creates a new data row for MIS reporting.
         *
         * @param dataReportId Integer standardized MIS code for the metric
         * @param dataReportDescription String human-readable description of the metric
         * @param dataReportResult Integer numerical result for the metric
         */
        private DataRow(int dataReportId, String dataReportDescription, int dataReportResult) {
            this.dataReportId = dataReportId;
            this.dataReportDescription = StringEscapeUtils.escapeHtml(dataReportDescription);
            this.dataReportResult.add(dataReportResult);
        }
    }

    private String reportByDescription = null;
    private GregorianCalendar startDate = null;
    private GregorianCalendar endDate = null;
    private List<Program> selectedPrograms = null;
    private List<Admission> admissions = null;
    private ArrayList<DataRow> dataRows = null;
    private ArrayList<String> headerRow = new ArrayList<String>();

    {
        headerRow.add("Id");
        headerRow.add("Description");
        headerRow.add("Results");
    }

    /**
     * Creates a MIS report for a specific functional centre within a date range.
     *
     * This constructor generates healthcare service utilization reports for a specific
     * functional centre, analyzing all programs associated with that centre. Functional
     * centres represent organizational units within healthcare facilities such as
     * departments, clinics, or specialized service areas.
     *
     * <p>The report aggregates data from all programs within the functional centre,
     * providing comprehensive metrics on service delivery, patient encounters, and
     * demographic characteristics during the specified time period.</p>
     *
     * @param loggedInInfo LoggedInInfo current user session with facility context
     * @param functionalCentreId String identifier for the functional centre to report on
     * @param startDate GregorianCalendar start of the reporting period
     * @param endDate GregorianCalendar end of the reporting period (inclusive)
     *
     * @see ca.openosp.openo.commn.model.FunctionalCentre
     * @see ca.openosp.openo.PMmodule.model.Program
     */
    public MisReportUIBean(LoggedInInfo loggedInInfo, String functionalCentreId, GregorianCalendar startDate, GregorianCalendar endDate) {

        this.startDate = startDate;
        this.endDate = endDate;

        FunctionalCentre functionalCentre = functionalCentreDao.find(functionalCentreId);
        reportByDescription = StringEscapeUtils.escapeHtml(functionalCentre.getAccountId() + ", " + functionalCentre.getDescription());
        selectedPrograms = programDao.getProgramsByFacilityIdAndFunctionalCentreId(loggedInInfo.getCurrentFacility().getId(), functionalCentreId);

        populateAdmissions();
        generateDataRows();
    }

    /**
     * Creates a MIS report for specific healthcare programs within a date range.
     *
     * This constructor generates targeted reports for a selected set of healthcare
     * programs, allowing for focused analysis of specific service areas or clinical
     * specialties. This is particularly useful for program-level performance evaluation
     * and comparative analysis between different healthcare services.
     *
     * <p>The report combines data from all specified programs to provide aggregate
     * metrics while maintaining the ability to analyze individual program performance
     * through split reporting functionality.</p>
     *
     * @param programIds String[] array of program identifiers to include in the report
     * @param startDate GregorianCalendar start of the reporting period
     * @param endDate GregorianCalendar end of the reporting period (inclusive)
     *
     * @see ca.openosp.openo.PMmodule.model.Program
     */
    public MisReportUIBean(String[] programIds, GregorianCalendar startDate, GregorianCalendar endDate) {

        this.startDate = startDate;
        this.endDate = endDate;

        selectedPrograms = new ArrayList<Program>();
        StringBuilder programNameList = new StringBuilder();
        for (String programIdString : programIds) {
            int programId = Integer.parseInt(programIdString);
            Program program = programDao.getProgram(programId);
            selectedPrograms.add(program);

            if (programNameList.length() > 0) programNameList.append(", ");
            programNameList.append(program.getName());
        }
        reportByDescription = StringEscapeUtils.escapeHtml(programNameList.toString());

        populateAdmissions();
        generateDataRows();
    }


    /**
     * Retrieves the report header row for display formatting.
     *
     * @return ArrayList&lt;String&gt; list of column headers for the MIS report
     */
    public ArrayList<String> getHeaderRow() {
        return (headerRow);
    }

    /**
     * Retrieves the descriptive title for the report scope.
     *
     * @return String human-readable description of what programs or centres are included
     */
    public String getReportByDescription() {
        return (reportByDescription);
    }

    /**
     * Formats date range for display in MIS reports.
     *
     * @param startDate GregorianCalendar beginning of the reporting period
     * @param endDate GregorianCalendar end of the reporting period
     * @return String formatted date range with inclusive end date notation
     */
    public static String getDateRangeForDisplay(GregorianCalendar startDate, GregorianCalendar endDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return (StringEscapeUtils.escapeHtml(simpleDateFormat.format(startDate.getTime()) + " to " + simpleDateFormat.format(endDate.getTime()) + " (inclusive)"));
    }

    /**
     * Populates program admissions data for the reporting period.
     *
     * Retrieves all patient admissions across selected programs within the
     * specified date range. This data forms the foundation for calculating
     * service utilization metrics and patient encounter statistics.
     */
    private void populateAdmissions() {
        admissions = new ArrayList<Admission>();

        for (Program program : selectedPrograms) {
            List<Admission> programAdmissions = admissionDao.getAdmissionsByProgramAndDate(program.getId(), startDate.getTime(), endDate.getTime());

            logger.debug("corresponding mis admissions count:" + admissions.size());

            for (Admission admission : programAdmissions) {
                admissions.add(admission);
                logger.debug("valid mis admission, id=" + admission.getId());
            }
        }
    }

    /**
     * Generates all data rows for the MIS report.
     *
     * Creates standardized healthcare service metrics including face-to-face encounters,
     * telephone consultations, service recipients seen, and total individuals served.
     * Each metric follows MIS reporting standards with specific codes and descriptions.
     */
    public void generateDataRows() {
        dataRows = new ArrayList<DataRow>();

        dataRows.add(getFaceToFace());
        dataRows.add(getPhone());
        dataRows.add(getIndividualsSeen());
        dataRows.add(getTotalIndividualsServed());
    }

    /**
     * Retrieves all generated data rows for the MIS report.
     *
     * @return ArrayList&lt;DataRow&gt; complete list of healthcare service metrics
     */
    public ArrayList<DataRow> getDataRows() {
        return (dataRows);
    }






    /**
     * Calculates face-to-face encounter metrics for MIS reporting.
     *
     * Counts all in-person clinical encounters across selected programs,
     * ensuring proper documentation with client records. This metric is
     * essential for measuring direct patient care delivery.
     *
     * @return DataRow with MIS code 4502440 and face-to-face visit count
     */
    private DataRow getFaceToFace() {
        int visitsCount = 0;

        // Count face-to-face notes across all selected programs in the reporting period
        for (Program program : selectedPrograms) {
            visitsCount = visitsCount + countFaceToFaceNotesInProgram(program);
        }

        return (new DataRow(4502440, "Visits Face to Face (must have client record)", visitsCount));
    }

    /**
     * Counts face-to-face encounters within a specific healthcare program.
     *
     * Analyzes case management notes to identify in-person clinical encounters,
     * ensuring each encounter has a valid demographic record. This validation
     * maintains data quality for MIS reporting purposes.
     *
     * @param program Program to analyze for face-to-face encounters
     * @return int number of valid face-to-face encounters
     */
    private int countFaceToFaceNotesInProgram(Program program) {
        List<CaseManagementNote> notes = caseManagementNoteDAO.getCaseManagementNoteByProgramIdAndObservationDate(program.getId(), startDate.getTime(), endDate.getTime());

        int count = 0;

        for (CaseManagementNote note : notes) {
            if (EncounterUtil.EncounterType.FACE_TO_FACE_WITH_CLIENT.getOldDbValue().equals(note.getEncounter_type())) {
                // Per MIS requirements: only count individuals with valid client records
                Demographic demographic = demographicDao.getDemographic(note.getDemographic_no());
                if (demographic != null) count++;
            }
        }

        return (count);
    }

    /**
     * Calculates telephone consultation metrics for MIS reporting.
     *
     * Counts all telephone-based clinical encounters across selected programs,
     * including both named patients and unique anonymous clients. This metric
     * captures remote care delivery and telemedicine activities.
     *
     * @return DataRow with MIS code 4512440 and telephone visit count
     */
    private DataRow getPhone() {
        int visitsCount = 0;

        // Count telephone consultations across all selected programs in the reporting period
        for (Program program : selectedPrograms) {
            visitsCount = visitsCount + countPhoneNotesInProgram(program);
        }

        return (new DataRow(4512440, "Visits - Telephone (named and unique anonymous)", visitsCount));
    }

    /**
     * Counts telephone encounters within a specific healthcare program.
     *
     * Analyzes case management notes for telephone-based clinical encounters,
     * including named patients and unique anonymous clients. Anonymous clients
     * are included to capture comprehensive service delivery while maintaining
     * appropriate privacy protections.
     *
     * @param program Program to analyze for telephone encounters
     * @return int number of valid telephone encounters
     */
    private int countPhoneNotesInProgram(Program program) {
        List<CaseManagementNote> notes = caseManagementNoteDAO.getCaseManagementNoteByProgramIdAndObservationDate(program.getId(), startDate.getTime(), endDate.getTime());

        int count = 0;

        for (CaseManagementNote note : notes) {
            if (EncounterUtil.EncounterType.TELEPHONE_WITH_CLIENT.getOldDbValue().equals(note.getEncounter_type())) {
                // Per MIS requirements: count named individuals and unique anonymous clients
                Demographic demographic = demographicDao.getDemographic(note.getDemographic_no());
                if (demographic != null) {
                    if (demographic.getAnonymous() == null || Demographic.UNIQUE_ANONYMOUS.equals(demographic.getAnonymous()))
                        count++;
                }
            }
        }

        return (count);
    }

    /**
     * Calculates service recipients seen metrics for MIS reporting.
     *
     * Counts unique individuals who received face-to-face services, focusing
     * on anonymous service recipients while excluding unique anonymous clients.
     * This metric provides insight into service delivery to vulnerable populations
     * who may not have formal healthcare registration.
     *
     * @return DataRow with MIS code 4526000 and unique service recipients count
     */
    private DataRow getIndividualsSeen() {
        HashSet<Integer> individuals = new HashSet<Integer>();

        // Count unique face-to-face individuals across all selected programs
        for (Program program : selectedPrograms) {
            addIndividualsSeenInProgram(individuals, program);
        }

        return (new DataRow(4526000, "Service Recipients Seen, (anonymous or no client record, excluding unique anonymous)", individuals.size()));
    }

    /**
     * Adds individuals seen in face-to-face encounters within a specific program.
     *
     * Identifies unique anonymous service recipients who received in-person services.
     * This method maintains patient privacy while enabling service utilization tracking
     * for populations that may not have traditional healthcare registration.
     *
     * @param individuals HashSet&lt;Integer&gt; set to collect unique demographic IDs
     * @param program Program to analyze for service recipients
     */
    private void addIndividualsSeenInProgram(HashSet<Integer> individuals, Program program) {
        List<CaseManagementNote> notes = caseManagementNoteDAO.getCaseManagementNoteByProgramIdAndObservationDate(program.getId(), startDate.getTime(), endDate.getTime());

        for (CaseManagementNote note : notes) {
            if (EncounterUtil.EncounterType.FACE_TO_FACE_WITH_CLIENT.getOldDbValue().equals(note.getEncounter_type())) {
                // Per MIS requirements: count anonymous individuals, excluding unique anonymous
                Demographic demographic = demographicDao.getDemographic(note.getDemographic_no());
                if (demographic != null && Demographic.ANONYMOUS.equals(demographic.getAnonymous()))
                    individuals.add(demographic.getDemographicNo());
            }
        }
    }

    /**
     * Calculates total individuals served metrics for MIS reporting.
     *
     * Counts all unique individuals who received any type of clinical service
     * across selected programs, regardless of anonymity status. This provides
     * the comprehensive count of total service recipients and represents the
     * overall program reach and impact.
     *
     * @return DataRow with MIS code 4552440 and total individuals served count
     */
    private DataRow getTotalIndividualsServed() {
        HashSet<Integer> individuals = new HashSet<Integer>();

        // Count all unique individuals served across all selected programs
        for (Program program : selectedPrograms) {
            addIndividualsServedInProgram(individuals, program);
        }

        return (new DataRow(4552440, "Individuals Served, (total clients)", individuals.size()));
    }

    /**
     * Adds all individuals served within a specific healthcare program.
     *
     * Collects unique demographic identifiers for all patients who received
     * any type of clinical service, regardless of encounter type or anonymity
     * status. This provides comprehensive service recipient tracking.
     *
     * @param individuals HashSet&lt;Integer&gt; set to collect unique demographic IDs
     * @param program Program to analyze for total service recipients
     */
    private void addIndividualsServedInProgram(HashSet<Integer> individuals, Program program) {
        List<CaseManagementNote> notes = caseManagementNoteDAO.getCaseManagementNoteByProgramIdAndObservationDate(program.getId(), startDate.getTime(), endDate.getTime());

        for (CaseManagementNote note : notes) {
            Demographic demographic = demographicDao.getDemographic(note.getDemographic_no());
            if (demographic != null) individuals.add(demographic.getDemographicNo());
        }
    }

    /**
     * Creates a comparative MIS report across multiple healthcare programs.
     *
     * Generates individual reports for each specified program and combines them
     * into a single comparative analysis. This enables side-by-side program
     * performance evaluation and identification of service delivery patterns
     * across different clinical areas or specialties.
     *
     * <p>The resulting report maintains individual program metrics while
     * providing aggregate totals, supporting both detailed program analysis
     * and overall performance assessment.</p>
     *
     * @param programIds String[] array of program identifiers to compare
     * @param startDate GregorianCalendar start of the reporting period
     * @param endDate GregorianCalendar end of the reporting period (inclusive)
     * @return MisReportUIBean combined report with comparative program data
     */
    public static MisReportUIBean getSplitProgramReports(String[] programIds, GregorianCalendar startDate, GregorianCalendar endDate) {
        ArrayList<MisReportUIBean> misReportBeans = new ArrayList<MisReportUIBean>();
        StringBuilder description = new StringBuilder();
        MisReportUIBean tempMisReportBean = null;

        ArrayList<String> headerRow = new ArrayList<String>();
        headerRow.add("Id");
        headerRow.add("Description");

        // Generate individual program reports
        for (String programIdString : programIds) {
            tempMisReportBean = new MisReportUIBean(new String[]{programIdString}, startDate, endDate);

            if (description.length() > 0) description.append(", ");
            description.append(tempMisReportBean.getReportByDescription());

            headerRow.add(tempMisReportBean.getReportByDescription());

            misReportBeans.add(tempMisReportBean);
        }

        // Combine individual reports into comparative format
        tempMisReportBean.reportByDescription = description.toString();
        tempMisReportBean.dataRows = combineDataSet(misReportBeans);
        tempMisReportBean.headerRow = headerRow;
        return (tempMisReportBean);
    }

    /**
     * Combines multiple MIS report datasets into a single comparative dataset.
     *
     * Merges data from individual program reports, ensuring all metric types
     * are represented and results are properly aligned for comparison.
     *
     * @param misReportBeans ArrayList&lt;MisReportUIBean&gt; individual program reports
     * @return ArrayList&lt;DataRow&gt; combined dataset for comparative analysis
     */
    private static ArrayList<DataRow> combineDataSet(ArrayList<MisReportUIBean> misReportBeans) {
        ArrayList<DataRow> combinedData = new ArrayList<DataRow>();

        combineDataIds(combinedData, misReportBeans);
        combineDataResults(combinedData, misReportBeans);

        return (combinedData);
    }

    /**
     * Combines metric IDs from multiple reports to ensure all metrics are represented.
     *
     * @param combinedData ArrayList&lt;DataRow&gt; target dataset for combined metrics
     * @param misReportBeans ArrayList&lt;MisReportUIBean&gt; source reports to combine
     */
    private static void combineDataIds(ArrayList<DataRow> combinedData, ArrayList<MisReportUIBean> misReportBeans) {
        for (MisReportUIBean misReportUIBean : misReportBeans) combineDataIds(combinedData, misReportUIBean);
    }

    /**
     * Adds metric IDs from a single report to the combined dataset.
     *
     * @param combinedData ArrayList&lt;DataRow&gt; target dataset for metrics
     * @param misReportUIBean MisReportUIBean source report to process
     */
    private static void combineDataIds(ArrayList<DataRow> combinedData, MisReportUIBean misReportUIBean) {
        for (DataRow beanRow : misReportUIBean.getDataRows()) {
            DataRow dataRow = getDataRowFromId(combinedData, beanRow.dataReportId);
            if (dataRow == null) combinedData.add(new DataRow(beanRow.dataReportId, beanRow.dataReportDescription, 0));
        }
    }

    /**
     * Retrieves a data row by its MIS report ID.
     *
     * @param combinedData ArrayList&lt;DataRow&gt; dataset to search
     * @param dataReportId int MIS report ID to find
     * @return DataRow matching the ID, or null if not found
     */
    private static DataRow getDataRowFromId(ArrayList<DataRow> combinedData, int dataReportId) {
        for (DataRow dataRow : combinedData) {
            if (dataRow.dataReportId == dataReportId) return (dataRow);
        }

        return (null);
    }

    /**
     * Combines numerical results from multiple reports for each metric.
     *
     * @param combinedData ArrayList&lt;DataRow&gt; target dataset to populate with results
     * @param misReportBeans ArrayList&lt;MisReportUIBean&gt; source reports with data
     */
    private static void combineDataResults(ArrayList<DataRow> combinedData, ArrayList<MisReportUIBean> misReportBeans) {
        for (DataRow dataRow : combinedData) {
            combineDataResultsFromBeans(dataRow, misReportBeans);
        }
    }

    /**
     * Combines results for a specific metric across multiple report beans.
     *
     * @param combinedDataRow DataRow to populate with combined results
     * @param misReportBeans ArrayList&lt;MisReportUIBean&gt; source reports with data
     */
    private static void combineDataResultsFromBeans(DataRow combinedDataRow, ArrayList<MisReportUIBean> misReportBeans) {
        ArrayList<Integer> tempResults = new ArrayList<Integer>();

        for (MisReportUIBean reportBean : misReportBeans) {
            tempResults.add(getDataResultsFromArrayRows(reportBean.getDataRows(), combinedDataRow.dataReportId));
        }

        combinedDataRow.dataReportResult = tempResults;
    }

    /**
     * Extracts the numerical result for a specific metric from data rows.
     *
     * @param dataRows ArrayList&lt;DataRow&gt; dataset to search
     * @param dataReportId int MIS report ID to find
     * @return Integer result value, or null if metric not found
     */
    private static Integer getDataResultsFromArrayRows(ArrayList<DataRow> dataRows, int dataReportId) {
        DataRow tempDataRow = getDataRowFromId(dataRows, dataReportId);
        if (tempDataRow == null) return (null);
        else return (tempDataRow.dataReportResult.get(0));
    }

}
