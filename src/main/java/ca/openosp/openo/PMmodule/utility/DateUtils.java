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

package ca.openosp.openo.PMmodule.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Utility class for date manipulation and formatting operations in the PMmodule.
 * <p>
 * This class provides static methods for common date operations including formatting,
 * parsing, date arithmetic, and comparison. It was originally designed for the Program
 * Management module to handle client admission dates, discharge dates, and age calculations.
 * </p>
 * <p>
 * Key Features:
 * <ul>
 *   <li>Date formatting with configurable patterns (default: dd/MM/yyyy)</li>
 *   <li>Date arithmetic including next day calculations and date differences</li>
 *   <li>Age calculation and date comparison for intake processing</li>
 *   <li>Date string manipulation and parsing with error handling</li>
 *   <li>Support for both leap year and regular year calculations</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Thread Safety Warning:</strong> The static SimpleDateFormat instance is not
 * thread-safe. Concurrent access may lead to incorrect formatting or parsing results.
 * Consider using ThreadLocal<SimpleDateFormat> for multi-threaded environments.
 * </p>
 * <p>
 * <strong>Known Issues:</strong>
 * <ul>
 *   <li>NextDay methods have complex leap year logic that could be simplified using Calendar</li>
 *   <li>Date parsing silently returns null on errors rather than throwing exceptions</li>
 *   <li>Some methods modify input parameters (e.g., NextDay modifies day/month/year)</li>
 * </ul>
 * </p>
 *
 * @deprecated 2013-12-09 use org.oscarehr.util.DateUtils instead. This class is
 *             maintained for backward compatibility but should not be used in new code.
 * @since 2003-10-03
 * @see ca.openosp.openo.PMmodule.utility.UtilDateUtilities
 * @see ca.openosp.openo.PMmodule.utility.DateTimeFormatUtils
 */
@Deprecated
public class DateUtils {


    /** Logger for error handling and debugging */
    private static Logger cat = MiscUtils.getLogger();

    /**
     * Cached SimpleDateFormat instance for date formatting.
     * WARNING: Not thread-safe - concurrent access may cause corruption
     */
    private static SimpleDateFormat sdf;

    /** Default date format pattern used throughout the PMmodule */
    private static String formatDate = "dd/MM/yyyy";
    /**
     * Standard delimiter used in intake date processing.
     * Used for parsing and formatting dates in intake forms.
     */
    public final static String intakeADelimiter = "/";

//##########################################################################

    /**
     * Gets the singleton SimpleDateFormat instance with default pattern.
     * <p>
     * Returns a cached SimpleDateFormat configured with the default date pattern
     * (dd/MM/yyyy). The formatter is created lazily on first access.
     * </p>
     * <p>
     * WARNING: SimpleDateFormat is not thread-safe. Multiple threads accessing
     * this shared instance concurrently may experience data corruption or
     * exceptions.
     * </p>
     *
     * @return SimpleDateFormat configured with default pattern
     */
    public static SimpleDateFormat getDateFormatter() {
        // Lazy initialization of formatter
        if (sdf == null) {
            sdf = new SimpleDateFormat(formatDate);
        }
        return sdf;
    }
//##########################################################################

    /**
     * Sets a new SimpleDateFormat with the specified pattern.
     * <p>
     * Replaces the current cached formatter with a new instance using
     * the provided pattern. This affects all subsequent calls to
     * getDateFormatter() until changed again.
     * </p>
     *
     * @param pattern String the new date format pattern (e.g., "yyyy-MM-dd")
     */
    public static void setDateFormatter(String pattern) {
        sdf = new SimpleDateFormat(pattern);
    }
//##########################################################################

    /**
     * Gets the current date formatted in localized style.
     * <p>
     * Returns today's date formatted using the default locale's date format.
     * For example, in English locale this might return "Sept 23, 2005".
     * Used for display purposes where localized formatting is preferred.
     * </p>
     *
     * @return String current date in localized format (e.g., "Sept 23, 2005")
     */
    public static String getDate() {
        Date date = new Date();
        return DateFormat.getDateInstance().format(date);
    }
//##########################################################################

    /**
     * Formats a Date using the default SimpleDateFormat pattern.
     * <p>
     * Creates a new SimpleDateFormat with default locale settings
     * and formats the provided date. Note that this method creates
     * a new formatter instance each time, which may impact performance.
     * </p>
     *
     * @param date Date the date to format
     * @return String formatted date string
     */
    public static String getDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        return sdf.format(date);
    }

    //###############################################################################
    /**
     * Gets the current date as a string with specified delimiter.
     * <p>
     * Returns today's date formatted as YYYY{delimiter}MM{delimiter}DD.
     * Month and day values are zero-padded to ensure two digits.
     * For example, with delimiter "/", returns "2005/09/23".
     * </p>
     * <p>
     * Note: Calendar.MONTH is zero-based, so 1 is added for correction
     * to get standard month numbering (1-12).
     * </p>
     *
     * @param delimiter String character(s) to separate date components
     * @return String current date in YYYY{delimiter}MM{delimiter}DD format
     */
    public static String getCurrentDateOnlyStr(String delimiter) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        // Calendar.MONTH is zero-based, add 1 for standard month numbering
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        String monthStr = String.valueOf(month);
        String dayStr = String.valueOf(day);

        // Zero-pad single digit months and days
        if (monthStr.length() <= 1) {
            monthStr = "0" + monthStr;
        }
        if (dayStr.length() <= 1) {
            dayStr = "0" + dayStr;
        }

        String dateStr = year + delimiter + monthStr + delimiter + dayStr;
        return dateStr;
    }

    //###############################################################################
    /**
     * Parses a date string into year, month, and day components.
     * <p>
     * Splits a delimited date string into separate year, month, and day strings.
     * Each component is zero-padded as needed: year to 4 digits (with "000" prefix
     * if single digit), month and day to 2 digits. For example, "2005/9/3" with
     * delimiter "/" returns ["2005", "09", "03"].
     * </p>
     * <p>
     * The method assumes the date format is YYYY{delimiter}MM{delimiter}DD.
     * Invalid input (null or empty strings) returns null.
     * </p>
     *
     * @param dateStr String the date string to parse
     * @param delimiter String the delimiter separating date components
     * @return String[] array of [year, month, day] with zero-padding, null if invalid input
     */
    public static String[] getYearMonthDayParams(String dateStr, String delimiter) {
        // Validate input parameters
        if (dateStr == null || dateStr.equals("") ||
                delimiter == null || delimiter.equals("")) {
            return null;
        }

        String[] dateParams = new String[3];
        dateParams = Utility.separateStrComponentsIntoStrArray(dateStr, delimiter);

        // Zero-pad components as needed
        if (dateParams[0].length() <= 1) {
            // Year: pad single digits with "000" prefix
            dateParams[0] = "000" + dateParams[0];
        }
        if (dateParams[1].length() <= 1) {
            // Month: pad single digits with "0" prefix
            dateParams[1] = "0" + dateParams[1];
        }
        if (dateParams[2].length() <= 1) {
            // Day: pad single digits with "0" prefix
            dateParams[2] = "0" + dateParams[2];
        }

        return dateParams;
    }


    //########################################################################
    /**
     * Gets the current date and time in localized format.
     * <p>
     * Returns the current date and time formatted using the default
     * locale's date-time format. Includes both date and time components.
     * </p>
     *
     * @return String current date and time in localized format
     */
    public static String getDateTime() {
        Date date = new Date();
        return DateFormat.getDateTimeInstance().format(date);
    }

    /**
     * Converts a date string from one format to another.
     * <p>
     * Parses the input date string using the actual format, then reformats
     * it using the target format. This is useful for converting between
     * different date representations (e.g., from "dd/MM/yyyy" to "yyyy-MM-dd").
     * </p>
     * <p>
     * Note: This method modifies the global SimpleDateFormat instance twice,
     * which could cause issues in multi-threaded environments.
     * </p>
     *
     * @param date String the date string to convert
     * @param format String the target format pattern
     * @param formatAtual String the current format pattern of the input date
     * @return String the reformatted date string, or empty string on error
     */
    public static String formatDate(String date, String format, String formatAtual) {
        try {
            // Set formatter to parse the input format
            setDateFormatter(formatAtual);
            Date data = getDateFormatter().parse(date);

            cat.debug("[DateUtils] - formatDate: data formatada: " + getDateFormatter().format(data));

            // Set formatter to output format and return formatted date
            setDateFormatter(format);
            return getDateFormatter().format(data);
        } catch (ParseException e) {
            cat.error("[DateUtils] - formatDate: ", e);
        }
        return "";
    }
//##########################################################################

    /**
     * Formats a date string using a new format pattern.
     * <p>
     * Parses the input date string using default SimpleDateFormat settings,
     * then reformats it using the specified format. The original date format
     * is auto-detected by SimpleDateFormat.
     * </p>
     *
     * @param date String the date string to format
     * @param format String the target format pattern
     * @return String the reformatted date string, or empty string on error
     */
    public static String formatDate(String date, String format) {
        try {
            // Use default SimpleDateFormat to parse input
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date data = sdf.parse(date);

            cat.debug("[DateUtils] - formatDate: data formatada: " + sdf.format(data));

            // Use specified format for output
            setDateFormatter(format);
            return getDateFormatter().format(data);
        } catch (ParseException e) {
            cat.error("[DateUtils] - formatDate: ", e);
        }
        return "";
    }
//##########################################################################

    /**
     * Adds days to the current date and returns formatted result.
     * <p>
     * Takes the current date, adds the specified number of days (positive
     * for future dates, negative for past dates), and returns the result
     * formatted according to the specified pattern.
     * </p>
     *
     * @param format String the format pattern for the output date
     * @param pSum String the number of days to add (as string)
     * @return String the calculated date formatted according to the pattern
     */
    public static String sumDate(String format, String pSum) {
        int iSum = Integer.valueOf(pSum).intValue();
        cat.debug("[DateUtils] - sumDate: iSum = " + iSum);

        Calendar calendar = new GregorianCalendar();
        Date now = new Date();
        calendar.setTime(now);

        // Add the specified number of days
        calendar.add(Calendar.DATE, iSum);
        Date data = calendar.getTime();

        setDateFormatter(format);
        return getDateFormatter().format(data);
    }
//##########################################################################

    /**
     * Calculates the next day given day, month, and year components.
     * <p>
     * Increments the provided date by one day, handling month and year
     * transitions including leap year calculations. The method modifies
     * the input parameters and returns a formatted date string.
     * </p>
     * <p>
     * Leap year logic: A year is a leap year if it's divisible by 4,
     * except for years divisible by 100 (unless also divisible by 400).
     * </p>
     * <p>
     * Note: This method uses manual date arithmetic rather than Calendar
     * which could be error-prone. Consider using Calendar.add() instead.
     * </p>
     *
     * @param day int the day component (1-31)
     * @param month int the month component (1-12)
     * @param year int the year component
     * @return String the next day in format "YYYY-M-D" (no zero-padding)
     */
    public String NextDay(int day, int month, int year) {

        boolean leapyear;

        switch (month) {

            // the months with 31 days without december

            case 1:

            case 3:

            case 5:

            case 7:

            case 8:

            case 10:

                if (day < 31) {

                    day++;

                } else {

                    day = 1;

                    month++;

                }

                break;

            case 12:

                if (day < 31) {

                    day++;

                } else {

                    day = 1;

                    month = 1;

                    year++;

                }

                break;

            case 2:

                if (day < 28) {

                    day++;

                } else {

                    if (((year % 4 == 0) && !(year % 100 == 0)) || (year % 400 == 0)) {

                        leapyear = true;

                    } else {

                        leapyear = false;

                        // in a leapyear 29 days

                    }
                    if (leapyear == true) {

                        if (day == 28) {

                            day++;

                        } else {

                            day = 1;

                            month++;

                        }
                    } else {

                        day = 1;

                        month++;

                    }

                }

                break;

            // these are the other month 4 6 9 11

            default:

                if (day < 30) {

                    day++;

                } else {

                    day = 1;

                    month++;

                }

        } // switch

        String nextDay = year + "-" + month + "-" + day;

        return nextDay;

    }

    //##########################################################################
    /**
     * Calculates a future date by adding multiple days to the given date.
     * <p>
     * Adds the specified number of days to the provided date, handling
     * month and year transitions. The method processes large numbers of days
     * in chunks to avoid overflow issues.
     * </p>
     * <p>
     * The algorithm uses a modulus approach with a base value of 28 days
     * to process large day additions efficiently. However, this approach
     * may be less accurate than using Calendar arithmetic.
     * </p>
     * <p>
     * WARNING: This method contains complex date arithmetic that could
     * produce incorrect results for edge cases. Consider using Calendar
     * or java.time.LocalDate for more reliable date calculations.
     * </p>
     *
     * @param day int the starting day component (1-31)
     * @param month int the starting month component (1-12)
     * @param year int the starting year component
     * @param numDays int the number of days to add
     * @return String the calculated date in format "YYYY-M-D" (no zero-padding)
     */
    public String NextDay(int day, int month, int year, int numDays) {


        int modValue = 28;

        while (numDays > 0) {

            int curNumDays = numDays % modValue;

            if (curNumDays == 0) {

                curNumDays = modValue;

            }

            switch (month) {

                // the months with 31 days without december

                case 1:

                case 3:

                case 5:

                case 7:

                case 8:

                case 10:

                    if (day + curNumDays < 31) {

                        day = day + curNumDays;

                    } else if (((day + curNumDays) % 31) == 0) {

                        day = 31;

                    } else {

                        day = ((day + curNumDays) % 31);

                        month++;

                    }

                    break;

                case 12:

                    if (day + curNumDays < 31) {

                        day = day + curNumDays;

                    } else if (((day + curNumDays) % 31) == 0) {

                        day = 31;

                    } else {

                        day = ((day + curNumDays) % 31);

                        month = 1;

                        year++;

                    }

                    break;

                case 2:

                    if (((year % 4 == 0) && !(year % 100 == 0)) || (year % 400 == 0)) {

                        if (day + curNumDays < 29) {

                            day = day + curNumDays;

                        } else if (((day + curNumDays) % 29) == 0) {

                            day = 29;

                        } else {

                            day = ((day + curNumDays) % 29);

                            month++;

                        }

                    } else {

                        if (day + curNumDays < 28) {

                            day = day + curNumDays;

                        } else if (((day + curNumDays) % 28) == 0) {

                            day = 28;

                        } else {

                            day = ((day + curNumDays) % 28);

                            month++;

                        }

                    }

                    break;

                // these are the other month 4 6 9 11

                default:

                    if (day + curNumDays < 30) {

                        day = day + curNumDays;

                    } else if (((day + curNumDays) % 30) == 0) {

                        day = 30;

                    } else {

                        day = ((day + curNumDays) % 30);

                        month++;

                    }

            } // switch

            numDays = numDays - curNumDays;

        }

        String nextDay = year + "-" + month + "-" + day;
        return nextDay;

    }
//################################################################################

    /**
     * Gets the difference between two dates, in days.
     * <p>
     * Takes two dates represented as milliseconds since epoch and returns
     * the difference in days. The calculation divides by 86400000 (milliseconds
     * in a day) and truncates to integer.
     * </p>
     * <p>
     * Note: This method performs integer truncation rather than rounding,
     * so fractional days are lost. For more precise calculations, consider
     * using java.time.Period or java.time.Duration.
     * </p>
     *
     * @param greater long the later date in milliseconds
     * @param lesser long the earlier date in milliseconds
     * @return int the number of days between the dates (truncated)
     */
    public static int getDifDays(long greater, long lesser) {
        // Calculate difference in milliseconds and convert to days
        // 86400000 ms = 24 hours * 60 minutes * 60 seconds * 1000 ms
        double x = (greater - lesser) / 86400000;
        int ret = (int) x;
        return ret;
    }

    //################################################################################
    /**
     * Extracts and reformats the date portion from a datetime string.
     * <p>
     * Takes a datetime string in format "YYYY-MM-DD HH:MM:SS.s" and extracts
     * just the date portion, reformatting it with the specified delimiter.
     * For example, "2005-11-09 19:02:41.0" with delimiter "/" becomes "2005/11/09".
     * </p>
     * <p>
     * Assumes the input follows ISO datetime format with at least 10 characters
     * for the date portion. Returns empty string if input is too short.
     * </p>
     *
     * @param dateTimeStr String the datetime string to trim
     * @param delimiter String the delimiter for the output date format
     * @return String the date portion with specified delimiter, or empty string if invalid
     */
    public static String getTrimmedDateStr(String dateTimeStr, String delimiter) {
        String trimmedDateStr = "";

        // Validate minimum length for date portion
        if (dateTimeStr != null && dateTimeStr.length() < 10) {
            return "";
        }

        // Extract YYYY-MM-DD portion and reformat with delimiter
        trimmedDateStr = dateTimeStr.substring(0, 4) + delimiter +
                dateTimeStr.substring(5, 7) + delimiter +
                dateTimeStr.substring(8, 10);

        return trimmedDateStr;
    }


    //################################################################################
    /**
     * Compares two dates for intake processing and returns the difference in days.
     * <p>
     * This method is specifically designed for intake form processing where
     * dates are provided as delimited strings. It parses both dates and
     * calculates the difference in days between them.
     * </p>
     * <p>
     * The method uses the standard intake delimiter (/) to parse date components,
     * then constructs Date objects for comparison. Returns -1 for any error
     * conditions including null inputs or parsing failures.
     * </p>
     * <p>
     * Used primarily in client intake workflows to validate date ranges
     * and calculate durations between admission dates and current dates.
     * </p>
     *
     * @param currDate String the current/reference date in delimited format
     * @param enteredDate String the entered/comparison date in delimited format
     * @return int the difference in days between the dates, or -1 on error
     */
    public static int compareDateForIntakeB(String currDate, String enteredDate) {
        // Validate input parameters
        if (currDate == null || currDate.length() <= 0 ||
                enteredDate == null || enteredDate.length() <= 0) {
            return -1;
        }

        int intDiff = -1;
        try {
            // Parse both dates into component arrays
            String[] currDateParams = DateUtils.getYearMonthDayParams(currDate, intakeADelimiter);
            String[] enteredDateParams = DateUtils.getYearMonthDayParams(enteredDate, intakeADelimiter);
            java.util.Date currDateObj = null;
            java.util.Date enteredDateObj = null;

            // Construct Date objects from parsed components
            if (currDateParams != null && currDateParams[0] != null &&
                currDateParams[1] != null && currDateParams[2] != null) {
                currDateObj = UtilDateUtilities.calcDate(
                        currDateParams[0], currDateParams[1], currDateParams[2]);
            }

            if (enteredDateParams != null && enteredDateParams[0] != null &&
                enteredDateParams[1] != null && enteredDateParams[2] != null) {
                enteredDateObj = UtilDateUtilities.calcDate(
                        enteredDateParams[0], enteredDateParams[1], enteredDateParams[2]);
            }

            // Calculate difference in days
            intDiff = DateUtils.getDifDays(currDateObj.getTime(), enteredDateObj.getTime());
        } catch (Exception ex1) {
            // Return -1 for any parsing or calculation errors
            return -1;
        }

        return intDiff;
    }

//########################################################################  

}
