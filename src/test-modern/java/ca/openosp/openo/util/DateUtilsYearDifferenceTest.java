/**
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package ca.openosp.openo.util;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@code DateUtils.getAge(Calendar, Calendar)} / {@code yearDifference}, in both
 * {@link ca.openosp.openo.util.DateUtils} and {@link ca.openosp.openo.utility.DateUtils}.
 *
 * <p>Used by the CDS-4 report age groups, Integrator search results and OpenO-to-OpenO lab
 * results.</p>
 *
 * @since 2026-11-08
 */
@DisplayName("DateUtils.getAge / yearDifference")
@Tag("unit")
@Tag("fast")
class DateUtilsYearDifferenceTest {

    private static Calendar cal(String isoDate) {
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

    @Test
    void shouldReturnNull_whenEitherDateIsNull() {
        assertThat(DateUtils.yearDifference((Calendar) null, cal("2026-01-01"))).isNull();
        assertThat(ca.openosp.openo.utility.DateUtils.yearDifference(cal("2026-01-01"), null)).isNull();
    }
}
