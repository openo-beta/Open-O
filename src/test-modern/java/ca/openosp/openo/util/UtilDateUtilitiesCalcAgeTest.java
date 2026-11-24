/**
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package ca.openosp.openo.util;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@code calcAge(String, String, String)}, the whole-years age used by
 * {@code Demographic.getAge()} and {@code DemographicData.getAge()}.
 *
 * <p>The age must increase on the birthday itself, not the day after.</p>
 *
 * @since 2026-11-08
 */
@DisplayName("calcAge (whole years today)")
@Tag("unit")
@Tag("fast")
class UtilDateUtilitiesCalcAgeTest {

    private static String[] parts(LocalDate d) {
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
        assertThat(ca.openosp.openo.PMmodule.utility.Utility.calcAge(dob[0], dob[1], dob[2])).isEqualTo(25);
        assertThat(ca.openosp.openo.PMmodule.utility.UtilDateUtilities.calcAge(dob[0], dob[1], dob[2])).isEqualTo(25);
    }

    @Test
    void shouldIncreaseAge_whenBirthdayWasYesterday() {
        String[] dob = parts(LocalDate.now().minusYears(26).minusDays(1));
        assertThat(UtilDateUtilities.calcAge(dob[0], dob[1], dob[2])).isEqualTo(26);
    }
}
