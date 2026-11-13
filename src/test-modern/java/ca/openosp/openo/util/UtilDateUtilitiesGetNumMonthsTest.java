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
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UtilDateUtilities#getNumMonths(Date, Date)}, used by the prevention
 * eligibility rules and prevention reports.
 *
 * <p>Months must be counted from the original start date so that month-end dates do not drift
 * (Jan 31 -> Feb 28 -> Mar 28).</p>
 *
 * @since 2026-11-08
 */
@DisplayName("UtilDateUtilities.getNumMonths")
@Tag("unit")
@Tag("fast")
class UtilDateUtilitiesGetNumMonthsTest {

    private static Date date(String isoDate) {
        return Date.from(LocalDate.parse(isoDate).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date dateTime(String isoDateTime) {
        return Date.from(LocalDateTime.parse(isoDateTime).atZone(ZoneId.systemDefault()).toInstant());
    }

    @ParameterizedTest(name = "{0} to {1} -> {2}")
    @CsvSource({
            "2026-01-01, 2026-06-30, 5",
            "2026-01-01, 2026-07-01, 6",
            // Previously drifted: Jan 31 -> Feb 28 -> Mar 28 counted as 2 months
            "2026-01-31, 2026-03-28, 1",
            "2026-01-31, 2026-03-31, 2",
            "2026-08-31, 2027-02-28, 5",
            "2026-08-31, 2027-03-01, 6",
            "2024-08-31, 2026-08-28, 23",
            "2024-08-31, 2026-08-31, 24",
            "2026-05-01, 2026-04-30, 0"
    })
    void shouldCountCompletedCalendarMonths_whenGivenTwoDates(String start, String end, int expected) {
        assertThat(UtilDateUtilities.getNumMonths(date(start), date(end))).isEqualTo(expected);
        assertThat(ca.openosp.openo.PMmodule.utility.UtilDateUtilities.getNumMonths(date(start), date(end))).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} to {1} -> {2}")
    @CsvSource({
            // Lab matching: four months to the day, but earlier in the day, is not yet four months
            "2026-01-15T10:00, 2026-05-15T09:00, 3",
            "2026-01-15T10:00, 2026-05-15T10:00, 4",
            "2026-01-15T10:00, 2026-05-15T11:00, 4"
    })
    void shouldNotCountMonthBeforeAnniversaryInstant_whenDatesHaveTimes(String start, String end, int expected) {
        assertThat(UtilDateUtilities.getNumMonths(dateTime(start), dateTime(end))).isEqualTo(expected);
    }

    @Test
    void shouldReturnZero_whenEitherDateIsNull() {
        assertThat(UtilDateUtilities.getNumMonths((Date) null, date("2026-01-01"))).isZero();
        assertThat(UtilDateUtilities.getNumMonths(date("2026-01-01"), (Date) null)).isZero();
    }
}
