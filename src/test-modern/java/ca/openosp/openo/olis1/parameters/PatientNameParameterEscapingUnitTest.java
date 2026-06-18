package ca.openosp.openo.olis1.parameters;

import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HL7-delimiter escaping of the free-text Z50 patient-name parameters
 * ({@link PID51} last name, {@link PID52} first name) and the shared
 * {@link Parameter#escapeHl7(String)} helper.
 *
 * <p>Z50 ("Identify Patient by Name") is the one OLIS query whose name fields are
 * free-text request input. A value containing an HL7 delimiter ({@code ~ ^ \ &})
 * must be escaped so it cannot corrupt the {@code @PID.5.x^<value>} query-segment
 * structure (the same class of issue closed for {@code ZSD}).</p>
 *
 * @since 2026-06-18
 */
@DisplayName("Z50 patient-name parameter HL7 escaping")
@Tag("unit")
@Tag("fast")
@Tag("read")
public class PatientNameParameterEscapingUnitTest extends OpenOUnitTestBase {

    @Test
    @DisplayName("PID51 (last name) escapes HL7 delimiters in the query segment")
    void shouldEscapeDelimitersInLastName() {
        assertThat(new PID51("Do~e").toOlisString()).isEqualTo("@PID.5.1^Do\\R\\e");
        assertThat(new PID51("O^Brien").toOlisString()).isEqualTo("@PID.5.1^O\\S\\Brien");
    }

    @Test
    @DisplayName("PID52 (first name) escapes HL7 delimiters in the query segment")
    void shouldEscapeDelimitersInFirstName() {
        assertThat(new PID52("Jo^hn").toOlisString()).isEqualTo("@PID.5.2^Jo\\S\\hn");
        assertThat(new PID52("A&B").toOlisString()).isEqualTo("@PID.5.2^A\\T\\B");
    }

    @Test
    @DisplayName("a null name serializes to an empty value (no NPE)")
    void shouldSerializeNullNameAsEmpty() {
        assertThat(new PID51(null).toOlisString()).isEqualTo("@PID.5.1^");
        assertThat(new PID52(null).toOlisString()).isEqualTo("@PID.5.2^");
    }

    @Test
    @DisplayName("the escape character is substituted first so other escapes are not re-escaped")
    void shouldEscapeBackslashFirst() {
        // Input "a&\" -> "&" becomes \T\ and "\" becomes \E\; because the backslash is
        // replaced first, the backslash introduced by the \T\ escape is NOT re-escaped.
        assertThat(Parameter.escapeHl7("a&\\")).isEqualTo("a\\T\\\\E\\");
        assertThat(Parameter.escapeHl7(null)).isEmpty();
    }
}
