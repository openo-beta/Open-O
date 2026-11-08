//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package ca.openosp.openo.util;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @deprecated 2013-04-28 use ca.openosp.openo.util.DateUtils instead
 */
@Deprecated
public class UtilDateUtilities {

    public static Date StringToDate(String s) {
        return StringToDate(s, defaultPattern, defaultLocale);
    }

    public static Date StringToDate(String s, Locale locale) {
        return StringToDate(s, defaultPattern, locale);
    }

    public static Date StringToDate(String s, String spattern) {
        return StringToDate(s, spattern, defaultLocale);
    }

    public static Date StringToDate(String s, String spattern, Locale locale) {
        try {
            SimpleDateFormat simpledateformat = new SimpleDateFormat(spattern, locale);
            return simpledateformat.parse(s);
        } catch (Exception exception) {
            return null;
        }
    }

    public static String DateToString(Date date) {
        return DateToString(date, defaultPattern, defaultLocale);
    }

    public static String DateToString(Date date, Locale locale) {
        return DateToString(date, defaultPattern, locale);
    }

    public static String DateToString(Date date, String spattern) {
        return DateToString(date, spattern, defaultLocale);
    }

    public static String DateToString(Date date, String spattern, Locale locale) {
        if (date != null) {
            SimpleDateFormat simpledateformat = new SimpleDateFormat(spattern, locale);
            return simpledateformat.format(date);
        } else {
            return "";
        }
    }

    //"yyyy-MM-dd";
    public static String justYear(Date date) {
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy");
        return simpledateformat.format(date);
    }

    public static String justMonth(Date date) {
        SimpleDateFormat simpledateformat = new SimpleDateFormat("MM");
        return simpledateformat.format(date);
    }

    public static String justDay(Date date) {
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd");
        return simpledateformat.format(date);
    }

    public static Date Tomorrow() {
        Calendar c = GregorianCalendar.getInstance();
        c.roll(Calendar.DATE, 1);
        return c.getTime();
    }

    public static Date calcDate(String s, String s1, String s2) {
        if (s == null || s1 == null || s2 == null || s == "" || s1 == "" || s2 == "") return (null);

        int i = Integer.parseInt(s);
        int j = Integer.parseInt(s1) - 1;
        int k = Integer.parseInt(s2);
        GregorianCalendar gregoriancalendar = new GregorianCalendar(i, j, k);
        return gregoriancalendar.getTime();
    }

    public static String calcAge(Date DOB) {
        return calcAgeAtDate(DOB, new GregorianCalendar().getTime());
    }


    /**
     * Returns the patient's age at a point in time as a display string, for example how old the
     * patient is right now or how old they will be on March 31 of this year.
     *
     * <p>Ages are counted in completed calendar units and are never rounded up:</p>
     * <ul>
     *   <li>under 14 days: whole days (e.g. "9 days")</li>
     *   <li>14 to 69 days: whole weeks (e.g. "6 weeks")</li>
     *   <li>70 days to under 2 years: completed calendar months (e.g. "5 months")</li>
     *   <li>2 years and older: completed years (e.g. "4 years")</li>
     * </ul>
     *
     * <p>A month is complete on the same day-of-month as the birth date. When that day does not
     * exist in the target month (e.g. born on the 31st), the month is complete on the first day of
     * the following month. For example, a child born January 1 is "5 months" on June 30 and
     * "6 months" on July 1; a child born August 31 is "6 months" on March 1.</p>
     *
     * <p>Only the calendar dates are compared; the time of day on either argument is ignored.</p>
     *
     * @param DOB         Date the patient's date of birth
     * @param pointInTime Date the date at which to calculate the age
     * @return String the localized age string (e.g. "5 months", "4 years"), the localized
     *         "Not Born" label if {@code pointInTime} is before {@code DOB}, or {@code null} if
     *         {@code DOB} is {@code null}
     */
    public static String calcAgeAtDate(Date DOB, Date pointInTime) {
        if (DOB == null) return (null);

        LocalDate birthDate = toLocalDate(DOB);
        LocalDate asOfDate = toLocalDate(pointInTime);

        // If as of date is before birth, return "Not born"
        if (asOfDate.isBefore(birthDate)) {
            return getAgeLabel("global.notBorn");
        }

        long ageInYears = ChronoUnit.YEARS.between(birthDate, asOfDate);
        if (ageInYears >= 2) {
            return ageInYears + " " + getAgeLabel("global.years");
        }

        long ageInDays = ChronoUnit.DAYS.between(birthDate, asOfDate);
        if (ageInDays >= 70) {
            return ChronoUnit.MONTHS.between(birthDate, asOfDate) + " " + getAgeLabel("global.months");
        } else if (ageInDays >= 14) {
            return ageInDays / 7 + " " + getAgeLabel("global.weeks");
        }
        return ageInDays + " " + getAgeLabel("global.days");
    }

    /**
     * Converts a {@link Date} to a {@link LocalDate} in the system default time zone.
     *
     * <p>Uses the epoch milliseconds rather than {@link Date#toInstant()} because
     * {@link java.sql.Date#toInstant()} throws {@link UnsupportedOperationException}.</p>
     *
     * @param date Date the date to convert (may be a java.sql.Date or java.sql.Timestamp)
     * @return LocalDate the calendar date of {@code date} in the system default time zone
     */
    private static LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Looks up a localized age unit label from the oscarResources bundle.
     *
     * @param key String the resource key (e.g. "global.months")
     * @return String the localized label
     */
    private static String getAgeLabel(String key) {
        return ResourceBundle.getBundle("oscarResources").getString(key);
    }


    /**
     * Calculates a person's age today in completed years. The age increases on the birthday itself.
     *
     * @param year_of_birth  String the four digit year of birth
     * @param month_of_birth String the month of birth (1-12)
     * @param date_of_birth  String the day of the month of birth
     * @return int the age in completed years
     * @throws NumberFormatException if any argument is not a valid integer
     */
    public static int calcAge(String year_of_birth, String month_of_birth, String date_of_birth) {
        GregorianCalendar now = new GregorianCalendar();
        int curYear = now.get(Calendar.YEAR);
        int curMonth = (now.get(Calendar.MONTH) + 1);
        int curDay = now.get(Calendar.DAY_OF_MONTH);
        int age = 0;

        if (curMonth > Integer.parseInt(month_of_birth)) {
            age = curYear - Integer.parseInt(year_of_birth);
        } else {
            if (curMonth == Integer.parseInt(month_of_birth) && curDay >= Integer.parseInt(date_of_birth)) {
                age = curYear - Integer.parseInt(year_of_birth);
            } else {
                age = curYear - Integer.parseInt(year_of_birth) - 1;
            }
        }
        return age;
    }

    private static String defaultPattern = "yyyy-MM-dd";
    //    private static String dateTimePattern = "yyyy-MM-dd HH:mm:ss"; timeStampPattern = "yyyyMMddHHmmss";
    private static Locale defaultLocale = Locale.CANADA;

    public static String getToday(String datePattern) {
        Format formatter = new SimpleDateFormat(datePattern);
        return formatter.format(new Date());
    }

    /**
     * For Parsing Dates.
     *
     * @param dateStr     The date string to be parsed
     * @param datePattern The date pattern to use to parse the date string
     * @return Date object. If date was unable to be parsed the object will be null
     */
    public static Date getDateFromString(String dateStr, String datePattern) {
        Date date = null;
        try {
            // Some examples
            DateFormat formatter = new SimpleDateFormat(datePattern);
            date = formatter.parse(dateStr);
        } catch (ParseException e) {
            //no point logging this..returns null
            //MiscUtils.getLogger().error("Looks bad, too bad original author didn't document how bad", e);
        }
        return date;
    }


    //This if probably not the most effiecent way to calcu

    /**
     * Gets the number of months between two date objects
     *
     * @param dStart Start Date
     * @param dEnd   End Date
     * @return the number of months
     */
    public static int getNumMonths(Date dStart, Date dEnd) {
        if (dStart == null || dEnd == null) return (0);

        // Count completed calendar months. Unlike repeatedly adding one month to a Calendar,
        // this does not drift for month-end start dates (Jan 31 -> Feb 28 -> Mar 28).
        long months = ChronoUnit.MONTHS.between(toLocalDate(dStart), toLocalDate(dEnd));
        return (int) Math.max(months, 0);
    }


    //This if probably not the most effiecent way to calcu

    /**
     * Gets the number of months between two date objects
     *
     * @param dStart Start Date
     * @param dEnd   End Date
     * @return the number of months
     */
    public static long getNumDays(Date dStart, Date dEnd) {
        if (dStart == null || dEnd == null) return (0);

        long msDifference = dEnd.getTime() - dStart.getTime();
        long daysDifference = msDifference / (1000 * 60 * 60 * 24);
        return daysDifference;
    }

    /**
     * Gets the number of years between two date objects
     *
     * @param dStart Start Date
     * @param dEnd   End Date
     * @return Number of year between
     */
    public static int getNumYears(Date dStart, Date dEnd) {
        if (dStart == null || dEnd == null) return (0);

        GregorianCalendar now = new GregorianCalendar();
        now.setTime(dEnd);
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH) + 1;
        int curDay = now.get(Calendar.DAY_OF_MONTH);

        GregorianCalendar birthDate = new GregorianCalendar();
        birthDate.setTime(dStart);
        int birthYear = birthDate.get(Calendar.YEAR);
        int birthMonth = birthDate.get(Calendar.MONTH) + 1;
        int birthDay = birthDate.get(5);

        int ageInYears = curYear - birthYear;

        if (curMonth > birthMonth || curMonth == birthMonth && curDay >= birthDay) {
            ageInYears = curYear - birthYear;
        } else {
            ageInYears = curYear - birthYear - 1;
        }
        return ageInYears;
    }


    /**
     * Gets the number of months between two Calendar objects
     *
     * @param dStart start date
     * @param dEnd   end date
     * @return number of months between
     */
    public static int getNumMonths(Calendar dStart, Calendar dEnd) {
        return getNumMonths(dStart.getTime(), dEnd.getTime());
    }


    public static int calculateGestationAge(Date today, Date edd) {
        int i = 40;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(edd);
        //Is Today before edd
        if (today.before(edd)) {

            while (today.before(calendar.getTime()) || today.equals(calendar.getTime())) {
                i--;
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                if (i < 0) {
                    break;
                }
            }
            i++;
        } else if (today.after(edd)) {
            // Weeks past 40 weeks?
            while (today.after(calendar.getTime())) {
                i++;
                calendar.add(Calendar.DAY_OF_YEAR, 7);
            }
        }

        if (i < 0) {
            i = 0;
        }
        return i;
    }


    public static int nullSafeCompare(Date d1, Date d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return 1;
        if (d2 == null) return -1;

        if (d1.equals(d2)) return 0;
        if (d1.before(d2)) {
            return 1;
        } else {
            return -1;
        }

    }
}
