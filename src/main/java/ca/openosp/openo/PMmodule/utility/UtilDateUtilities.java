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
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Legacy date utility class providing date conversion and age calculation functions.
 * <p>
 * This class offers a comprehensive set of static methods for common date operations
 * including string-to-date conversions, date formatting, age calculations, and date
 * arithmetic. It was designed to support the PMmodule's date handling requirements
 * with support for multiple locales and format patterns.
 * </p>
 * <p>
 * Key Features:
 * <ul>
 *   <li>Flexible date parsing with configurable patterns and locales</li>
 *   <li>Age calculation with different units (years, months, weeks, days)</li>
 *   <li>Localized date formatting using ResourceBundle for internationalization</li>
 *   <li>Date arithmetic for calculating differences between dates</li>
 *   <li>Date construction from individual year, month, day components</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Thread Safety Warning:</strong> SimpleDateFormat instances are created
 * for each operation, which is thread-safe but may impact performance. Consider
 * using ThreadLocal<SimpleDateFormat> or the newer java.time API for better
 * performance in multi-threaded environments.
 * </p>
 * <p>
 * <strong>Age Calculation Logic:</strong> The age calculation methods include
 * sophisticated logic to display age in the most appropriate units:
 * <ul>
 *   <li>For ages >= 2 years: displays in years</li>
 *   <li>For ages < 2 years but >= 9 weeks: displays in months</li>
 *   <li>For ages >= 14 days but < 9 weeks: displays in weeks</li>
 *   <li>For ages < 14 days: displays in days</li>
 * </ul>
 * </p>
 *
 * @deprecated 2013-04-28 use org.oscarehr.util.DateUtils instead. This class is
 *             maintained for backward compatibility but should not be used in new code.
 * @since 2003-07-21
 * @see ca.openosp.openo.PMmodule.utility.DateUtils
 * @see ca.openosp.openo.PMmodule.utility.DateTimeFormatUtils
 */
@Deprecated
public class UtilDateUtilities {

    /**
     * Converts a string to Date using default pattern and locale.
     * <p>
     * Parses the input string using the default date pattern (yyyy-MM-dd)
     * and the default locale (Canada). This is the most commonly used
     * conversion method in the PMmodule.
     * </p>
     *
     * @param s String the date string to parse
     * @return Date the parsed date object, or null if parsing fails
     */
    public static Date StringToDate(String s) {
        return StringToDate(s, defaultPattern, defaultLocale);
    }

    /**
     * Converts a string to Date using default pattern and specified locale.
     *
     * @param s String the date string to parse
     * @param locale Locale the locale for date parsing
     * @return Date the parsed date object, or null if parsing fails
     */
    public static Date StringToDate(String s, Locale locale) {
        return StringToDate(s, defaultPattern, locale);
    }

    /**
     * Converts a string to Date using specified pattern and default locale.
     *
     * @param s String the date string to parse
     * @param spattern String the date format pattern (e.g., "dd/MM/yyyy")
     * @return Date the parsed date object, or null if parsing fails
     */
    public static Date StringToDate(String s, String spattern) {
        return StringToDate(s, spattern, defaultLocale);
    }

    /**
     * Converts a string to Date using specified pattern and locale.
     * <p>
     * This is the core string-to-date conversion method that all other
     * StringToDate methods delegate to. It creates a new SimpleDateFormat
     * instance for each call to ensure thread safety.
     * </p>
     *
     * @param s String the date string to parse
     * @param spattern String the date format pattern
     * @param locale Locale the locale for date parsing
     * @return Date the parsed date object, or null if parsing fails
     */
    public static Date StringToDate(String s, String spattern, Locale locale) {
        try {
            SimpleDateFormat simpledateformat = new SimpleDateFormat(spattern, locale);
            return simpledateformat.parse(s);
        } catch (Exception exception) {
            // Return null on any parsing error
            return null;
        }
    }

    /**
     * Converts a Date to string using default pattern and locale.
     *
     * @param date Date the date to format
     * @return String the formatted date string
     */
    public static String DateToString(Date date) {
        return DateToString(date, defaultPattern, defaultLocale);
    }

    /**
     * Converts a Date to string using default pattern and specified locale.
     *
     * @param date Date the date to format
     * @param locale Locale the locale for date formatting
     * @return String the formatted date string
     */
    public static String DateToString(Date date, Locale locale) {
        return DateToString(date, defaultPattern, locale);
    }

    /**
     * Converts a Date to string using specified pattern and default locale.
     *
     * @param date Date the date to format
     * @param spattern String the date format pattern
     * @return String the formatted date string
     */
    public static String DateToString(Date date, String spattern) {
        return DateToString(date, spattern, defaultLocale);
    }

    /**
     * Converts a Date to string using specified pattern and locale.
     * <p>
     * This is the core date-to-string conversion method. Returns an empty
     * string for null dates rather than throwing an exception.
     * </p>
     *
     * @param date Date the date to format (can be null)
     * @param spattern String the date format pattern
     * @param locale Locale the locale for date formatting
     * @return String the formatted date string, or empty string if date is null
     */
    public static String DateToString(Date date, String spattern, Locale locale) {
        if (date != null) {
            SimpleDateFormat simpledateformat = new SimpleDateFormat(spattern, locale);
            return simpledateformat.format(date);
        } else {
            return "";
        }
    }

    /**
     * Constructs a Date from individual year, month, and day strings.
     * <p>
     * Creates a Date object from separate string components. The month
     * parameter is adjusted by subtracting 1 since GregorianCalendar
     * uses zero-based months (0=January, 11=December).
     * </p>
     *
     * @param s String the year component
     * @param s1 String the month component (1-12)
     * @param s2 String the day component
     * @return Date the constructed date, or null if any parameter is null
     */
    public static Date calcDate(String s, String s1, String s2) {
        if (s == null || s1 == null || s2 == null) return (null);

        int i = Integer.parseInt(s);
        // Subtract 1 from month since GregorianCalendar uses 0-based months
        int j = Integer.parseInt(s1) - 1;
        int k = Integer.parseInt(s2);
        GregorianCalendar gregoriancalendar = new GregorianCalendar(i, j, k);
        return gregoriancalendar.getTime();
    }

    /**
     * Calculates age from date of birth to current date.
     * <p>
     * Convenience method that calculates age as of today.
     * Delegates to calcAgeAtDate with the current date.
     * </p>
     *
     * @param DOB Date the date of birth
     * @return String formatted age string (e.g., "5 years", "8 months", "3 weeks")
     */
    public static String calcAge(Date DOB) {
        return calcAgeAtDate(DOB, new GregorianCalendar().getTime());
    }


    /**
     * This returns the Patients Age string at a point in time.  IE. How old the patient will be right now or how old will they be on march.31 of this year.
     *
     * @param DOB         Demographics Date of birth
     * @param pointInTime The date you would like to calculate there age at.
     * @return age string ( ie 2 months, 4 years .etc )
     */
    public static String calcAgeAtDate(Date DOB, Date pointInTime) {
        if (DOB == null) return (null);

        GregorianCalendar now = new GregorianCalendar();
        now.setTime(pointInTime);
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH) + 1;
        int curDay = now.get(Calendar.DAY_OF_MONTH);

        GregorianCalendar birthDate = new GregorianCalendar();
        birthDate.setTime(DOB);
        int birthYear = birthDate.get(Calendar.YEAR);
        int birthMonth = birthDate.get(Calendar.MONTH) + 1;
        int birthDay = birthDate.get(5);

        int ageInYears = curYear - birthYear;
        String result = ageInYears + " " + ResourceBundle.getBundle("oscarResources").getString("global.years");


        if (curMonth > birthMonth || curMonth == birthMonth && curDay >= birthDay) {
            ageInYears = curYear - birthYear;
            result = ageInYears + " " + ResourceBundle.getBundle("oscarResources").getString("global.years");
        } else {
            ageInYears = curYear - birthYear - 1;
            result = ageInYears + " " + ResourceBundle.getBundle("oscarResources").getString("global.years");
        }
        if (ageInYears < 2) {
            int yearDiff = curYear - birthYear;
            int ageInDays;
            if (yearDiff == 2) {
                ageInDays = (birthDate.getActualMaximum(Calendar.DAY_OF_YEAR) - birthDate.get(Calendar.DAY_OF_YEAR)) + now.get(Calendar.DAY_OF_YEAR) + 365;
            } else if (yearDiff == 1) {
                ageInDays = (birthDate.getActualMaximum(Calendar.DAY_OF_YEAR) - birthDate.get(Calendar.DAY_OF_YEAR)) + now.get(Calendar.DAY_OF_YEAR);
            } else {
                ageInDays = now.get(Calendar.DAY_OF_YEAR) - birthDate.get(Calendar.DAY_OF_YEAR);
            }
            if (ageInDays / 7 > 9) {
                result = ageInDays / 30 + " " + ResourceBundle.getBundle("oscarResources").getString("global.months");
            } else if (ageInDays >= 14) {
                result = ageInDays / 7 + " " + ResourceBundle.getBundle("oscarResources").getString("global.weeks");
            } else {
                result = ageInDays + " " + ResourceBundle.getBundle("oscarResources").getString("global.days");
            }
        }
        return result;
    }


    public static int calcAge(String year_of_birth, String month_of_birth, String date_of_birth) {
        GregorianCalendar now = new GregorianCalendar();
        int curYear = now.get(Calendar.YEAR);
        int curMonth = (now.get(Calendar.MONTH) + 1);
        int curDay = now.get(Calendar.DAY_OF_MONTH);
        int age = 0;

        if (curMonth > Integer.parseInt(month_of_birth)) {
            age = curYear - Integer.parseInt(year_of_birth);
        } else {
            if (curMonth == Integer.parseInt(month_of_birth) && curDay > Integer.parseInt(date_of_birth)) {
                age = curYear - Integer.parseInt(year_of_birth);
            } else {
                age = curYear - Integer.parseInt(year_of_birth) - 1;
            }
        }
        return age;
    }

    /** Default date format pattern used throughout the utility */
    private static String defaultPattern = "yyyy-MM-dd";
    //    private static String dateTimePattern = "yyyy-MM-dd HH:mm:ss"; timeStampPattern = "yyyyMMddHHmmss";
    /** Default locale for date formatting operations */
    private static Locale defaultLocale = Locale.CANADA;

    /**
     * Gets today's date formatted with the specified pattern.
     *
     * @param datePattern String the format pattern for the date
     * @return String today's date formatted according to the pattern
     */
    public static String getToday(String datePattern) {
        Format formatter = new SimpleDateFormat(datePattern);
        return formatter.format(new Date());
    }

    /**
     * Parses a date string using the specified pattern.
     * <p>
     * Creates a DateFormat instance with the given pattern and attempts
     * to parse the input string. Returns null if parsing fails, rather
     * than throwing an exception.
     * </p>
     *
     * @param dateStr String the date string to be parsed
     * @param datePattern String the date pattern to use for parsing
     * @return Date the parsed date object, or null if parsing fails
     */
    public static Date getDateFromString(String dateStr, String datePattern) {
        Date date = null;
        try {
            DateFormat formatter = new SimpleDateFormat(datePattern);
            date = formatter.parse(dateStr);
        } catch (ParseException e) {
            // Return null on parse failure
        }
        return date;
    }


    //This if probably not the most effiecent way to calcu

    /**
     * Gets the number of months between two date objects.
     * <p>
     * Calculates the number of complete months between the start and end dates
     * by iteratively adding months to the start date until it reaches or exceeds
     * the end date. The result is adjusted to return complete months only.
     * </p>
     * <p>
     * Note: This implementation may not be the most efficient for large date
     * ranges. Consider using Period.between() from java.time package for
     * better performance.
     * </p>
     *
     * @param dStart Date the start date
     * @param dEnd Date the end date
     * @return int the number of complete months between the dates
     */
    public static int getNumMonths(Date dStart, Date dEnd) {
        int i = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dStart);
        // Count months by iteratively adding until we reach/exceed end date
        while (calendar.getTime().before(dEnd) || calendar.getTime().equals(dEnd)) {
            calendar.add(Calendar.MONTH, 1);
            i++;
        }
        // Adjust for the extra month added in the final iteration
        i--;
        // Ensure non-negative result
        if (i < 0) {
            i = 0;
        }
        return i;
    }


    /**
     * Gets the number of complete years between two date objects.
     * <p>
     * Calculates the age in complete years by comparing year, month, and day
     * components. The calculation considers whether the end date has passed
     * the anniversary of the start date to determine complete years.
     * </p>
     * <p>
     * This is commonly used for age calculations where partial years should
     * not be counted until the birth anniversary has passed.
     * </p>
     *
     * @param dStart Date the start date (e.g., birth date)
     * @param dEnd Date the end date (e.g., current date)
     * @return int the number of complete years between the dates
     */
    public static int getNumYears(Date dStart, Date dEnd) {
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(dEnd);
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH) + 1;
        int curDay = now.get(Calendar.DAY_OF_MONTH);

        GregorianCalendar birthDate = new GregorianCalendar();
        birthDate.setTime(dStart);
        int birthYear = birthDate.get(Calendar.YEAR);
        int birthMonth = birthDate.get(Calendar.MONTH) + 1;
        int birthDay = birthDate.get(Calendar.DAY_OF_MONTH);

        int ageInYears = curYear - birthYear;

        // Check if the anniversary has passed this year
        if (curMonth > birthMonth || (curMonth == birthMonth && curDay >= birthDay)) {
            ageInYears = curYear - birthYear;
        } else {
            // Anniversary hasn't passed yet, subtract one year
            ageInYears = curYear - birthYear - 1;
        }
        return ageInYears;
    }


    /**
     * Gets the number of months between two Calendar objects.
     * <p>
     * Convenience method that delegates to the Date-based implementation.
     * </p>
     *
     * @param dStart Calendar the start date
     * @param dEnd Calendar the end date
     * @return int the number of complete months between the dates
     */
    public static int getNumMonths(Calendar dStart, Calendar dEnd) {
        return getNumMonths(dStart.getTime(), dEnd.getTime());
    }


}
