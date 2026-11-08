/**
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package ca.openosp.openo.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the patient age calculations used across OpenO EMR screens.
 *
 * <p>Ages must be counted in completed calendar units and never rounded up, because users rely on
 * the displayed age for date-based eligibility (e.g. a child born January 1 is not "6 months"
 * until July 1).</p>
 *
 * @since 2026-11-07
 */
@DisplayName("Patient age calculations")
@Tag("unit")
@Tag("fast")
class UtilDateUtilitiesAgeTest {

    private static Locale originalLocale;

    @BeforeAll
    static void useEnglishLabels() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.CANADA);
    }

    @AfterAll
    static void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    private static Date date(String isoDate) {
        return Date.from(LocalDate.parse(isoDate).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Nested
    @DisplayName("calcAgeAtDate (displayed age)")
    class CalcAgeAtDate {

        @ParameterizedTest(name = "born {0}, on {1} -> {2}")
        @CsvSource({
                // The reported case: 180 days old is still 5 months
                "2026-01-01, 2026-06-30, 5 months",
                "2026-01-01, 2026-07-01, 6 months",
                // Previously displayed one month early (days / 30)
                "2025-12-01, 2026-11-26, 11 months",
                "2025-12-01, 2026-12-01, 12 months",
                "2025-01-01, 2026-05-26, 16 months",
                "2025-01-15, 2026-12-10, 22 months",
                // Day before and day of turning 2
                "2025-01-15, 2027-01-14, 23 months",
                "2025-01-15, 2027-01-15, 2 years",
                // Month-end birth: the month completes on the 1st of the next month
                "2026-08-31, 2027-02-28, 5 months",
                "2026-08-31, 2027-03-01, 6 months",
                // Leap-day birth
                "2024-02-29, 2026-02-28, 23 months",
                "2024-02-29, 2026-03-01, 2 years",
                // Days / weeks / months thresholds are unchanged
                "2026-01-01, 2026-01-01, 0 days",
                "2026-01-01, 2026-01-14, 13 days",
                "2026-01-01, 2026-01-15, 2 weeks",
                "2026-01-01, 2026-03-11, 9 weeks",
                "2026-01-01, 2026-03-12, 2 months",
                // Older patients: the year increases on the birthday itself
                "2000-11-07, 2026-11-06, 25 years",
                "2000-11-07, 2026-11-07, 26 years"
        })
        void shouldReturnCompletedUnits_whenCalculatingAge(String dob, String asOf, String expected) {
            assertThat(UtilDateUtilities.calcAgeAtDate(date(dob), date(asOf))).isEqualTo(expected);
        }

        @Test
        void shouldReturnNotBorn_whenAsOfDateIsBeforeBirth() {
            assertThat(UtilDateUtilities.calcAgeAtDate(date("2026-05-01"), date("2026-04-30"))).isEqualTo("Not Born");
        }

        @Test
        void shouldReturnNull_whenDateOfBirthIsNull() {
            assertThat(UtilDateUtilities.calcAgeAtDate(null, date("2026-04-30"))).isNull();
        }

        @Test
        void shouldIgnoreTimeOfDay_whenAsOfDateHasATime() {
            Date lateOnJune30 = Date.from(LocalDateTime.parse("2026-06-30T23:59:59").atZone(ZoneId.systemDefault()).toInstant());
            assertThat(UtilDateUtilities.calcAgeAtDate(date("2026-01-01"), lateOnJune30)).isEqualTo("5 months");
        }

        @Test
        void shouldAcceptSqlDates_whenCalledWithDatabaseValues() {
            Date dob = java.sql.Date.valueOf("2026-01-01");
            Date asOf = java.sql.Date.valueOf("2026-07-01");
            assertThat(UtilDateUtilities.calcAgeAtDate(dob, asOf)).isEqualTo("6 months");
        }

        @Test
        void shouldMatchSharedImplementation_whenCalledThroughPmmoduleCopies() {
            Date dob = date("2026-01-01");
            Date asOf = date("2026-06-30");
            assertThat(ca.openosp.openo.PMmodule.utility.Utility.calcAgeAtDate(dob, asOf)).isEqualTo("5 months");
            assertThat(ca.openosp.openo.PMmodule.utility.UtilDateUtilities.calcAgeAtDate(dob, asOf)).isEqualTo("5 months");
        }
    }

    @Nested
    @DisplayName("getNumMonths (eligibility rules)")
    class GetNumMonths {

        @ParameterizedTest(name = "{0} to {1} -> {2}")
        @CsvSource({
                "2026-01-01, 2026-06-30, 5",
                "2026-01-01, 2026-07-01, 6",
                // Previously drifted: Jan 31 -> Feb 28 -> Mar 28 counted as 2 months
                "2026-01-31, 2026-03-28, 1",
                "2026-01-31, 2026-03-31, 2",
                "2026-08-31, 2027-02-28, 5",
                "2026-08-31, 2027-03-01, 6",
                "2026-05-01, 2026-04-30, 0"
        })
        void shouldCountCompletedCalendarMonths_whenGivenTwoDates(String start, String end, int expected) {
            assertThat(UtilDateUtilities.getNumMonths(date(start), date(end))).isEqualTo(expected);
        }

        @Test
        void shouldReturnZero_whenEitherDateIsNull() {
            assertThat(UtilDateUtilities.getNumMonths((Date) null, date("2026-01-01"))).isZero();
            assertThat(UtilDateUtilities.getNumMonths(date("2026-01-01"), (Date) null)).isZero();
        }
    }

    @Nested
    @DisplayName("calcAge (whole years today)")
    class CalcAgeToday {

        private String[] parts(LocalDate d) {
            return new String[]{String.valueOf(d.getYear()), String.valueOf(d.getMonthValue()), String.valueOf(d.getDayOfMonth())};
        }

        @Test
        void shouldIncreaseAge_whenTodayIsTheBirthday() {
            String[] dob = parts(LocalDate.now().minusYears(26));
            assertThat(UtilDateUtilities.calcAge(dob[0], dob[1], dob[2])).isEqualTo(26);
            assertThat(ca.openosp.openo.PMmodule.utility.Utility.calcAge(dob[0], dob[1], dob[2])).isEqualTo(26);
            assertThat(ca.openosp.openo.PMmodule.utility.UtilDateUtilities.calcAge(dob[0], dob[1], dob[2])).isEqualTo(26);
        }

        @Test
        void shouldNotIncreaseAge_whenBirthdayIsTomorrow() {
            String[] dob = parts(LocalDate.now().minusYears(26).plusDays(1));
            assertThat(UtilDateUtilities.calcAge(dob[0], dob[1], dob[2])).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("DateUtils.getAge / yearDifference")
    class DateUtilsAge {

        private Calendar cal(String isoDate) {
            LocalDate d = LocalDate.parse(isoDate);
            return new GregorianCalendar(d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
        }

        @ParameterizedTest(name = "born {0}, on {1} -> {2}")
        @CsvSource({
                // Previously inverted: birthday earlier in the year returned one year too few
                "2000-03-15, 2026-11-07, 26",
                // ... and birthday later in the year returned one year too many
                "2000-12-15, 2026-11-07, 25",
                "2000-11-07, 2026-11-06, 25",
                "2000-11-07, 2026-11-07, 26",
                "2000-11-07, 2026-11-08, 26"
        })
        void shouldReturnCompletedYears_whenCalculatingAge(String dob, String onDay, int expected) {
            assertThat(DateUtils.getAge(cal(dob), cal(onDay))).isEqualTo(expected);
            assertThat(ca.openosp.openo.utility.DateUtils.getAge(cal(dob), cal(onDay))).isEqualTo(expected);
        }
    }
}
