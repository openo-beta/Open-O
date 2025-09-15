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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Utility class for date and time formatting operations in the PMmodule.
 * <p>
 * This class provides convenience methods for converting between various
 * date and time representations commonly used in the PM (Program Management)
 * module. It handles conversions between Date objects, Strings, Timestamps,
 * and long values, with specific formatting for Canadian healthcare contexts.
 * </p>
 * <p>
 * The utility supports:
 * <ul>
 *   <li>Date formatting and parsing with yyyy-MM-dd format (ISO 8601 compatible)</li>
 *   <li>Time formatting using Canadian locale standards (12-hour format with AM/PM)</li>
 *   <li>Past and future date calculations for age and appointment management</li>
 *   <li>Timestamp generation for historical queries and audit trails</li>
 *   <li>Date normalization by removing time components</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Thread Safety Warning:</strong> The DateFormat objects used are not thread-safe.
 * While they are static final, DateFormat instances maintain internal state during
 * formatting and parsing operations. In multi-threaded environments, this can lead to
 * corrupted output or exceptions. Consider using ThreadLocal<DateFormat> or
 * synchronization if used in concurrent contexts.
 * </p>
 * <p>
 * <strong>Design Note:</strong> This class uses the final modifier to prevent
 * instantiation and inheritance, following the utility class pattern. All methods
 * are static for convenient access without object creation.
 * </p>
 * <p>
 * <strong>Known Issues:</strong>
 * <ul>
 *   <li>The getFuture() method ignores its 'start' parameter - likely a bug</li>
 *   <li>The getDateFromString(String, DateFormat) ignores the format parameter</li>
 *   <li>Exception handling converts ParseExceptions to IllegalStateException</li>
 * </ul>
 * </p>
 *
 * @deprecated 2013-12-09 use org.oscarehr.util.DateUtils instead. This class is
 *             maintained for backward compatibility but should not be used in new code.
 * @since 2005-01-01
 */
@Deprecated
public final class DateTimeFormatUtils {

    /**
     * Logger for error handling and debugging.
     * Used primarily for logging ParseException occurrences.
     */
    private static final Logger log = MiscUtils.getLogger();

    /**
     * Standard date format used throughout the PM module (yyyy-MM-dd).
     * <p>
     * This format follows ISO 8601 date representation and is consistent
     * with database storage formats. It ensures unambiguous date representation
     * across different locales and systems.
     * </p>
     * <p>
     * WARNING: SimpleDateFormat is not thread-safe. Concurrent access to this
     * shared instance can cause data corruption or exceptions.
     * </p>
     */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Canadian locale time format for consistent time display.
     * <p>
     * Uses SHORT format which typically produces output like "3:30 PM" in
     * Canadian locale. This ensures consistent time representation across
     * the application regardless of server locale settings.
     * </p>
     */
    private static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.CANADA);


    /**
     * Gets a timestamp for a date in the past from the current date.
     * <p>
     * Commonly used for calculating date ranges in patient history queries,
     * such as "show all records from the last 5 years". The method subtracts
     * the specified number of years from today's date.
     * </p>
     *
     * @param numYears int number of years to go back from current date
     * @return Timestamp representing the past date with current time preserved
     */
    public static final Timestamp getPast(int numYears) {
        return getPast(Calendar.getInstance(), numYears);
    }

    /**
     * Gets a timestamp for a date in the past from a specific calendar.
     * <p>
     * This method modifies the provided Calendar object by subtracting
     * the specified number of years. Note that this mutates the input
     * calendar, which may be unexpected behavior for callers.
     * </p>
     * <p>
     * The method preserves the time component, only adjusting the year.
     * This is useful for age calculations and historical date ranges.
     * </p>
     *
     * @param calendar Calendar starting point for calculation (will be modified)
     * @param numYears int number of years to subtract
     * @return Timestamp representing the past date
     */
    public static final Timestamp getPast(Calendar calendar, int numYears) {
        // WARNING: This modifies the input calendar object
        calendar.add(Calendar.YEAR, -numYears);

        return new Timestamp(calendar.getTimeInMillis());
    }


    /**
     * Gets a future date by adding days to the current date.
     * <p>
     * WARNING: The 'start' parameter is ignored in the current implementation,
     * which appears to be a bug. The method always calculates from the current
     * date rather than the provided start date. This should be fixed but may
     * have backward compatibility implications.
     * </p>
     * <p>
     * Used for scheduling future appointments or calculating follow-up dates.
     * The returned date has the time component removed (normalized to 00:00:00).
     * </p>
     *
     * @param start Date starting date (BUG: currently ignored)
     * @param daysInFuture Integer number of days to add from today
     * @return Date representing the future date with time normalized to 00:00:00
     */
    public static final Date getFuture(Date start, Integer daysInFuture) {
        // BUG: The 'start' parameter is not used - always uses current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, daysInFuture);
        Date future = calendar.getTime();

        // Normalize to remove time component
        return getDateFromDate(future);
    }

    /**
     * Converts a Date to String using standard format (yyyy-MM-dd).
     *
     * @param date Date to convert
     * @return String formatted date string, empty string if null
     */
    public static final String getStringFromDate(Date date) {
        return getStringFromDate(date, DATE_FORMAT);
    }

    /**
     * Converts a Date to String using specified format.
     *
     * @param date Date to convert
     * @param format DateFormat to use for formatting
     * @return String formatted date string, empty string if null
     */
    public static final String getStringFromDate(Date date, DateFormat format) {
        return format(date, format);
    }

    /**
     * Parses a String to Date using standard format (yyyy-MM-dd).
     *
     * @param date String date string to parse
     * @return Date parsed date object, null if input is null/empty
     */
    public static final Date getDateFromString(String date) {
        return getDateFromString(date, DATE_FORMAT);
    }

    /**
     * Parses a String to Date using specified format.
     * <p>
     * BUG: Currently uses DATE_FORMAT regardless of format parameter.
     * This appears to be an implementation error where the format parameter
     * is ignored. This could cause unexpected behavior if callers expect
     * different format parsing.
     * </p>
     *
     * @param date String date string to parse
     * @param format DateFormat intended format (BUG: not currently used)
     * @return Date parsed date object, null if input is null/empty
     */
    public static final Date getDateFromString(String date, DateFormat format) {
        // BUG: Should use 'format' parameter instead of DATE_FORMAT
        return parse(date, DATE_FORMAT);
    }

    /**
     * Normalizes a Date by removing time component.
     * <p>
     * This method is useful for date comparisons where time should be ignored,
     * such as checking if two appointments are on the same day. The returned
     * date will have time set to 00:00:00 in the system timezone.
     * </p>
     *
     * @param date Date to normalize
     * @return Date with time set to 00:00:00, null if input is null
     */
    public static final Date getDateFromDate(Date date) {
        // Formats to yyyy-MM-dd then parses back, effectively removing time
        return parseFormat(date, DATE_FORMAT);
    }


    /**
     * Converts a long timestamp to a Date with time component only.
     *
     * @param time long timestamp in milliseconds
     * @return Date object with time component
     */
    public static final Date getTimeFromLong(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();

        return getTimeFromDate(date);
    }

    /**
     * Formats a Date's time component as String.
     *
     * @param time Date containing time to format
     * @return String formatted time string using Canadian locale
     */
    public static final String getStringFromTime(Date time) {
        return format(time, TIME_FORMAT);
    }

    /**
     * Parses a time String to Date.
     *
     * @param time String time string to parse
     * @return Date parsed time object
     */
    public static final Date getTimeFromString(String time) {
        return parse(time, TIME_FORMAT);
    }

    /**
     * Extracts time component from a Date.
     *
     * @param date Date containing time to extract
     * @return Date with only time component
     */
    public static final Date getTimeFromDate(Date date) {
        return parse(format(date, TIME_FORMAT), TIME_FORMAT);
    }


    /**
     * Parses and reformats a Date using specified format.
     * <p>
     * This method formats a date and then parses it back,
     * effectively normalizing the date representation. This technique
     * is used to strip unwanted components (like time from a date)
     * by using a format that doesn't include those components.
     * </p>
     * <p>
     * For example, using a date-only format strips the time component.
     * </p>
     *
     * @param date Date to parse and format
     * @param format DateFormat to use for normalization
     * @return Date normalized date according to the format
     */
    private static final Date parseFormat(Date date, DateFormat format) {
        // Format to string, then parse back - normalizes based on format pattern
        return parse(format(date, format), format);
    }

    /**
     * Formats a Date to String safely.
     *
     * @param date Date to format, can be null
     * @param format DateFormat to use for formatting
     * @return String formatted date or empty string if null
     */
    private static final String format(Date date, DateFormat format) {
        return (date != null) ? format.format(date) : new String();
    }

    /**
     * Parses a String to Date safely with null checking.
     * <p>
     * This method wraps DateFormat.parse() with null/empty string checking
     * and exception handling. ParseExceptions are logged and converted to
     * IllegalStateException, which changes them from checked to unchecked
     * exceptions.
     * </p>
     * <p>
     * Design Note: Converting ParseException to IllegalStateException is
     * questionable as it hides the nature of the error from callers who
     * might want to handle parsing errors differently from other errors.
     * </p>
     *
     * @param s String to parse, can be null or empty
     * @param format DateFormat to use for parsing
     * @return Date parsed date or null if input is null/empty
     * @throws IllegalStateException if parsing fails (wraps ParseException)
     */
    private static final Date parse(String s, DateFormat format) {
        try {
            // Return null for null or empty strings rather than attempting parse
            return (s != null && s.length() > 0) ? format.parse(s) : null;
        } catch (ParseException e) {
            // Log and convert to unchecked exception
            // This changes the exception contract which may surprise callers
            log.error("Error", e);
            throw new IllegalStateException(e);
        }
    }

}
