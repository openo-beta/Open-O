/**
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package ca.openosp.openo.prevention;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for {@link Prevention#getAgeInYears()}, which the prevention eligibility rules
 * use to decide age-based recommendations.
 *
 * <p>The age must be a count of completed years: it increases on the birthday itself and not
 * before. Dates of birth are computed relative to today because {@code getAgeInYears()} always
 * evaluates the age as of now.</p>
 *
 * @since 2026-11-13
 */
@DisplayName("Prevention.getAgeInYears")
@Tag("unit")
@Tag("fast")
class PreventionAgeInYearsTest {

    private static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Prevention preventionBornOn(LocalDate dateOfBirth) {
        return new Prevention("F", toDate(dateOfBirth));
    }

    @ParameterizedTest(name = "{0} years")
    @ValueSource(ints = {1, 2, 18, 50, 65})
    void shouldIncreaseAge_whenTodayIsTheBirthday(int years) {
        LocalDate dateOfBirth = LocalDate.now().minusYears(years);

        assertThat(preventionBornOn(dateOfBirth).getAgeInYears()).isEqualTo(years);
    }

    @ParameterizedTest(name = "{0} years")
    @ValueSource(ints = {1, 2, 18, 50, 65})
    void shouldNotIncreaseAge_whenBirthdayIsTomorrow(int years) {
        LocalDate dateOfBirth = LocalDate.now().minusYears(years).plusDays(1);

        assertThat(preventionBornOn(dateOfBirth).getAgeInYears()).isEqualTo(years - 1);
    }

    @Test
    void shouldReturnZero_whenBornLessThanAYearAgo() {
        LocalDate dateOfBirth = LocalDate.now().minusMonths(11);

        assertThat(preventionBornOn(dateOfBirth).getAgeInYears()).isZero();
    }

    @Test
    void shouldReturnZero_whenDateOfBirthIsMissing() {
        assertThat(new Prevention("F", null).getAgeInYears()).isZero();
    }
}
