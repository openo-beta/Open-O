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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.dao.ProgramDao;
import ca.openosp.openo.PMmodule.model.Program;
import ca.openosp.openo.casemgmt.dao.CaseManagementNoteDAO;
import ca.openosp.openo.casemgmt.dao.CaseManagementNoteDAO.EncounterCounts;
import ca.openosp.openo.commn.dao.SecRoleDao;
import ca.openosp.openo.commn.model.SecRole;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.util.DateUtils;

/**
 * Provider service utilization reporting UI bean for healthcare program analysis.
 *
 * This UI bean generates comprehensive reports on healthcare provider service delivery
 * across multiple programs and time periods. It analyzes provider encounters with
 * patients to track service utilization patterns, program effectiveness, and clinical
 * productivity metrics essential for healthcare administration and quality improvement.
 *
 * <p>The reporting system focuses specifically on physician encounters across active
 * service programs, providing detailed monthly breakdowns and aggregate statistics.
 * This data is crucial for:</p>
 * <ul>
 *   <li>Provider productivity analysis and performance evaluation</li>
 *   <li>Program resource allocation and staffing decisions</li>
 *   <li>Clinical service planning and capacity management</li>
 *   <li>Healthcare funding and billing validation</li>
 *   <li>Quality assurance and clinical outcome correlation</li>
 * </ul>
 *
 * <p>Reports include both individual program metrics and agency-wide aggregates,
 * enabling administrators to analyze service delivery at multiple organizational
 * levels. Monthly and date-range reporting supports both operational monitoring
 * and strategic planning activities.</p>
 *
 * <p>The system filters for active service programs (excluding bed programs) to
 * focus on outpatient and clinical services where provider encounter tracking
 * is most relevant for performance measurement and billing purposes.</p>
 *
 * @since September 2008
 * @see ca.openosp.openo.PMmodule.model.Program
 * @see ca.openosp.openo.casemgmt.dao.CaseManagementNoteDAO.EncounterCounts
 * @see ca.openosp.openo.commn.model.SecRole
 */
public class ProviderServiceReportUIBean {

    private static Logger logger = MiscUtils.getLogger();

    private ProgramDao programDao = (ProgramDao) SpringUtils.getBean(ProgramDao.class);
    private SecRoleDao secRoleDao = SpringUtils.getBean(SecRoleDao.class);

    /** Start date for the reporting period */
    private Date startDate = null;
    /** End date for the reporting period */
    private Date endDate = null;
    /** Date formatter for monthly report display */
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM");

    /**
     * Creates a provider service report for the specified date range.
     *
     * Initializes the reporting bean with start and end dates to analyze
     * healthcare provider service delivery patterns across programs and
     * time periods. The report will include monthly breakdowns and aggregate
     * statistics for all active service programs.
     *
     * @param startDate Date beginning of the reporting period
     * @param endDate Date end of the reporting period
     */
    public ProviderServiceReportUIBean(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Data row representation for provider service report metrics.
     *
     * Each data row represents service utilization metrics for a specific
     * program and time period, including encounter counts and provider
     * activity statistics essential for healthcare program evaluation.
     *
     * @since September 2008
     */
    public static class DataRow {
        /** Name of the healthcare program */
        public String programName = null;
        /** Type classification of the program */
        public String programType = null;
        /** Date or date range for the reported metrics */
        public String date = null;
        /** Detailed encounter counts and statistics */
        public EncounterCounts encounterCounts = null;
    }

    /**
     * Generates comprehensive provider service utilization data rows for reporting.
     *
     * Creates detailed service delivery metrics for all active healthcare programs,
     * focusing on physician encounters and clinical service utilization. The report
     * includes both individual program statistics and agency-wide aggregates to
     * provide complete visibility into provider productivity and program effectiveness.
     *
     * <p>The method processes data by calendar months and generates metrics for:</p>
     * <ul>
     *   <li>Individual service program encounter statistics</li>
     *   <li>Monthly breakdowns for trend analysis</li>
     *   <li>Agency-wide aggregate service delivery metrics</li>
     *   <li>Provider productivity across all programs</li>
     * </ul>
     *
     * <p>Only active service programs are included to focus on outpatient and
     * clinical services where provider encounter tracking is most relevant.</p>
     *
     * @return List&lt;DataRow&gt; comprehensive service utilization metrics, or null if doctor role not found
     */
    public List<DataRow> getDataRows() {
        // Align reporting period to month boundaries
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startDate.getTime());
        DateUtils.setToBeginningOfMonth(startCal);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endDate.getTime());
        endCal.add(Calendar.MONTH, 1);
        DateUtils.setToBeginningOfMonth(endCal);

        // Retrieve active programs and identify doctor role for encounter filtering
        List<Program> activePrograms = programDao.getAllActivePrograms();
        SecRole doctorRole = null;
        for (SecRole role : secRoleDao.findAll())
            if ("doctor".equals(role.getName()))
                doctorRole = role;
        if (doctorRole == null) {
            logger.error("Error, no CAISI role named 'doctor' found in database.");
            return (null);
        }

        ArrayList<DataRow> results = new ArrayList<DataRow>();

        // Generate metrics for each active service program
        for (Program program : activePrograms) {
            // Focus on service programs (bed programs excluded)
            if (!Program.SERVICE_TYPE.equals(program.getType()))
                continue;

            results.addAll(getProgramNumbers(startCal, endCal, doctorRole, program));
        }

        // Add agency-wide aggregate statistics
        results.addAll(getEntireAgencyNumbers(startCal, endCal, doctorRole));

        return (results);
    }

    /**
     * Generates agency-wide service delivery metrics across all programs.
     *
     * Creates aggregate statistics for overall healthcare agency performance,
     * providing monthly breakdowns and total service delivery metrics across
     * all programs and providers. This data supports strategic planning and
     * overall organizational performance assessment.
     *
     * <p>Agency-wide metrics help administrators understand total service
     * capacity utilization, identify seasonal trends, and compare overall
     * productivity across different time periods.</p>
     *
     * @param startCal Calendar start of reporting period
     * @param endCal Calendar end of reporting period
     * @param doctorRole SecRole doctor role for encounter filtering
     * @return Collection&lt;DataRow&gt; agency-wide service delivery statistics
     */
    private Collection<? extends DataRow> getEntireAgencyNumbers(Calendar startCal, Calendar endCal,
                                                                 SecRole doctorRole) {
        ArrayList<DataRow> results = new ArrayList<DataRow>();

        // Generate monthly aggregate statistics
        Calendar tempStart = (Calendar) startCal.clone();
        while (tempStart.compareTo(endCal) < 0) {
            Calendar tempEnd = (Calendar) tempStart.clone();
            tempEnd.add(Calendar.MONTH, 1);

            DataRow dataRow = new DataRow();
            dataRow.programName = "all programs";
            dataRow.programType = "all program types";
            dataRow.date = dateFormatter.format(tempStart.getTime());
            dataRow.encounterCounts = CaseManagementNoteDAO.getDemographicEncounterCountsByProgramAndRoleId(null,
                    doctorRole.getId().intValue(),
                    tempStart.getTime(), tempEnd.getTime());

            results.add(dataRow);

            tempStart.add(Calendar.MONTH, 1);
        }

        // Add overall period aggregate
        DataRow dataRow = new DataRow();
        dataRow.programName = "all programs";
        dataRow.programType = "all program types";
        dataRow.date = dateFormatter.format(startCal.getTime()) + " to " + dateFormatter.format(endCal.getTime());
        dataRow.encounterCounts = CaseManagementNoteDAO.getDemographicEncounterCountsByProgramAndRoleId(null,
                doctorRole.getId().intValue(), startCal.getTime(),
                endCal.getTime());

        results.add(dataRow);

        return (results);
    }

    /**
     * Generates service delivery metrics for a specific healthcare program.
     *
     * Creates detailed monthly statistics and aggregate totals for physician
     * encounters within a single healthcare program. This granular data enables
     * program-specific performance evaluation, resource allocation decisions,
     * and clinical service optimization.
     *
     * <p>Program-level metrics support targeted quality improvement initiatives
     * and help identify high-performing or underutilized clinical services.
     * Monthly breakdowns reveal seasonal patterns and service delivery trends
     * important for operational planning.</p>
     *
     * @param startCal Calendar start of reporting period
     * @param endCal Calendar end of reporting period
     * @param doctorRole SecRole doctor role for encounter filtering
     * @param program Program specific healthcare program to analyze
     * @return ArrayList&lt;DataRow&gt; program-specific service delivery metrics
     */
    private ArrayList<DataRow> getProgramNumbers(Calendar startCal, Calendar endCal, SecRole doctorRole,
                                                 Program program) {
        ArrayList<DataRow> results = new ArrayList<DataRow>();

        // Generate monthly program statistics
        Calendar tempStart = (Calendar) startCal.clone();
        while (tempStart.compareTo(endCal) < 0) {
            Calendar tempEnd = (Calendar) tempStart.clone();
            tempEnd.add(Calendar.MONTH, 1);

            DataRow dataRow = new DataRow();
            dataRow.programName = program.getName();
            dataRow.programType = program.getType();
            dataRow.date = dateFormatter.format(tempStart.getTime());
            dataRow.encounterCounts = CaseManagementNoteDAO.getDemographicEncounterCountsByProgramAndRoleId(
                    program.getId(), doctorRole.getId().intValue(),
                    tempStart.getTime(), tempEnd.getTime());

            results.add(dataRow);

            tempStart.add(Calendar.MONTH, 1);
        }

        // Add program aggregate for entire reporting period
        DataRow dataRow = new DataRow();
        dataRow.programName = program.getName();
        dataRow.programType = program.getType();
        dataRow.date = dateFormatter.format(startCal.getTime()) + " to " + dateFormatter.format(endCal.getTime());
        dataRow.encounterCounts = CaseManagementNoteDAO.getDemographicEncounterCountsByProgramAndRoleId(
                program.getId(),
                doctorRole.getId().intValue(), startCal.getTime(),
                endCal.getTime());

        results.add(dataRow);

        return (results);
    }
}
