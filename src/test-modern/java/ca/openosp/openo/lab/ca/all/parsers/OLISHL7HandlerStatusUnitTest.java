package ca.openosp.openo.lab.ca.all.parsers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks the OLIS test-request / test-result status tokens to the CT-Tracker labels
 * (CT 10.2.x and 12.8.x). These are pure code→label mappings; a regression here
 * silently shows the wrong status text on every lab report, so they are pinned.
 *
 * @since 2026-06-18
 */
@DisplayName("OLIS status token mappings (CT 10.2.x / 12.8.x)")
@Tag("unit")
@Tag("fast")
public class OLISHL7HandlerStatusUnitTest {

    @Test
    @DisplayName("test-request short status tokens match the spec labels")
    void shouldMapRequestShortTokens() {
        assertThat(OLISHL7Handler.getTestRequestStatusMessageShort('O')).isEqualTo("Ordered");
        assertThat(OLISHL7Handler.getTestRequestStatusMessageShort('I')).isEqualTo("Collected");
        assertThat(OLISHL7Handler.getTestRequestStatusMessageShort('P')).isEqualTo("Preliminary");
        assertThat(OLISHL7Handler.getTestRequestStatusMessageShort('A')).isEqualTo("Partial");
        assertThat(OLISHL7Handler.getTestRequestStatusMessageShort('F')).isEqualTo("Final");
        assertThat(OLISHL7Handler.getTestRequestStatusMessageShort('C')).isEqualTo("Amended");
        assertThat(OLISHL7Handler.getTestRequestStatusMessageShort('X')).isEqualTo("Cancelled");
    }

    @Test
    @DisplayName("test-request red parenthetical text matches the spec (10.2.x [R,P])")
    void shouldMapRequestRedText() {
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('O')).isEqualTo("(specimen not yet collected)");
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('I')).isEqualTo("(pending)");
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('P')).isEqualTo("(preliminary)");
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('A')).isEqualTo("(partial)");
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('C')).isEqualTo("(amended)");
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('X')).isEqualTo("(test was cancelled)");
        // Final and OLIS-specific statuses carry no red annotation.
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('F')).isEmpty();
        assertThat(OLISHL7Handler.getTestRequestStatusRedText('E')).isEmpty();
    }

    @Test
    @DisplayName("test-result status tokens match the spec labels (12.8.x)")
    void shouldMapResultTokens() {
        assertThat(OLISHL7Handler.getTestResultStatusMessage('F')).isEqualTo("Final");
        assertThat(OLISHL7Handler.getTestResultStatusMessage('W')).isEqualTo("Invalid");
        assertThat(OLISHL7Handler.getTestResultStatusMessage('C')).isEqualTo("Amended");
        assertThat(OLISHL7Handler.getTestResultStatusMessage('P')).isEqualTo("Preliminary");
        assertThat(OLISHL7Handler.getTestResultStatusMessage('X')).isEqualTo("Could not obtain result");
        assertThat(OLISHL7Handler.getTestResultStatusMessage('N')).isEqualTo("Test not performed");
    }

    @Test
    @DisplayName("test-result red parenthetical (adjacent to result name) matches the spec (12.8.x [R,P])")
    void shouldMapResultRedText() {
        assertThat(OLISHL7Handler.getTestResultStatusRedText('W')).isEqualTo("(invalid result)");
        assertThat(OLISHL7Handler.getTestResultStatusRedText('P')).isEqualTo("(preliminary)");
        assertThat(OLISHL7Handler.getTestResultStatusRedText('C')).isEqualTo("(amended)");
        assertThat(OLISHL7Handler.getTestResultStatusRedText('X')).isEqualTo("(could not obtain result)");
        assertThat(OLISHL7Handler.getTestResultStatusRedText('N')).isEqualTo("(test not performed)");
        // Final's adjacent annotation is optional (12.8.1) — omitted here.
        assertThat(OLISHL7Handler.getTestResultStatusRedText('F')).isEmpty();
    }
}
