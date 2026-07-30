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

package ca.openosp.openo.commn.jobs;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the trigger selection in {@link OscarJobUtils}.
 * <p>
 * HRM auto-polling stores its interval as a cron expression. A step in the cron minute field
 * restarts every hour, so intervals that do not divide 60 must be scheduled at a fixed rate
 * instead. OntarioMD HRM validation requires a 25 minute polling interval, which is one of those.
 *
 * @since 2026-07-29
 */
@Tag("unit")
@Tag("jobs")
class OscarJobUtilsTriggerUnitTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private static String everyMinutes(int minutes) {
        return "0 0/" + minutes + " * 1/1 * ?";
    }

    private static Date at(int hour, int minute) {
        return Date.from(ZonedDateTime.of(2026, 7, 29, hour, minute, 0, 0, ZONE).toInstant());
    }

    private static long minutesBetween(Date earlier, Date later) {
        return (later.getTime() - earlier.getTime()) / 60000L;
    }

    @Test
    @DisplayName("should use a cron trigger when the interval divides 60")
    void shouldUseCronTrigger_whenIntervalDivides60() {
        assertThat(OscarJobUtils.buildTrigger(everyMinutes(30))).isInstanceOf(CronTrigger.class);
        assertThat(OscarJobUtils.buildTrigger(everyMinutes(5))).isInstanceOf(CronTrigger.class);
        assertThat(OscarJobUtils.buildTrigger(everyMinutes(15))).isInstanceOf(CronTrigger.class);
    }

    @Test
    @DisplayName("should use a fixed rate trigger when the interval does not divide 60")
    void shouldUseFixedRateTrigger_whenIntervalDoesNotDivide60() {
        assertThat(OscarJobUtils.buildTrigger(everyMinutes(25))).isInstanceOf(PeriodicTrigger.class);
        assertThat(OscarJobUtils.buildTrigger(everyMinutes(7))).isInstanceOf(PeriodicTrigger.class);
        assertThat(OscarJobUtils.buildTrigger(everyMinutes(45))).isInstanceOf(PeriodicTrigger.class);
    }

    @Test
    @DisplayName("should leave an uneven gap at the top of the hour when 25 minutes is scheduled by cron")
    void shouldLeaveUnevenGap_whenTwentyFiveMinutesScheduledByCron() {
        CronTrigger cron = new CronTrigger(everyMinutes(25));

        Date first = nextAfter(cron, at(9, 0));
        Date second = nextAfter(cron, first);
        Date third = nextAfter(cron, second);

        assertThat(minutesBetween(first, second)).isEqualTo(25);
        // :50 to the next :00 is the gap this fix exists to remove
        assertThat(minutesBetween(second, third)).isEqualTo(10);
    }

    @Test
    @DisplayName("should space runs exactly 25 minutes apart across the top of the hour")
    void shouldSpaceRunsExactly_whenTwentyFiveMinuteIntervalConfigured() {
        Trigger trigger = OscarJobUtils.buildTrigger(everyMinutes(25));

        Date previous = at(9, 0);
        for (int run = 0; run < 6; run++) {
            Date next = nextAfter(trigger, previous);
            assertThat(minutesBetween(previous, next))
                    .as("gap before run %d", run + 1)
                    .isEqualTo(25);
            previous = next;
        }
    }

    @Test
    @DisplayName("should fall back to a cron trigger for expressions that are not a plain interval")
    void shouldFallBackToCronTrigger_whenExpressionIsNotAPlainInterval() {
        assertThat(OscarJobUtils.buildTrigger("0 0 3 1/1 * ?")).isInstanceOf(CronTrigger.class);
        assertThat(OscarJobUtils.buildTrigger("0 0/25 8-17 1/1 * ?")).isInstanceOf(CronTrigger.class);
    }

    /**
     * Asks a trigger for the run that follows a completed run at the given time.
     */
    private static Date nextAfter(Trigger trigger, Date lastCompletion) {
        SimpleTriggerContext context = new SimpleTriggerContext();
        context.update(lastCompletion, lastCompletion, lastCompletion);
        Date next = trigger.nextExecutionTime(context);
        assertThat(next).isNotNull();
        return next;
    }
}
