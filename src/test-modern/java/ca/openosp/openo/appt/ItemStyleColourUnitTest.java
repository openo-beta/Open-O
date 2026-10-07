package ca.openosp.openo.appt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ItemStyleColour}: which colours are accepted, how a see-through status
 * colour is painted over white, and which colour a location chip's icon is drawn in.
 *
 * @since 2026-10-07
 */
@DisplayName("ItemStyleColour unit tests")
@Tag("unit")
@Tag("fast")
public class ItemStyleColourUnitTest {

    @Nested
    @DisplayName("PATTERN")
    class Pattern {

        @ParameterizedTest
        @ValueSource(strings = {"#1a2B3c", "#1a2b3c80", "#FFFFFF00"})
        @DisplayName("should accept #rrggbb and #rrggbbaa")
        void shouldAccept_whenHexColour(String colour) {
            assertThat(ItemStyleColour.PATTERN.matcher(colour).matches()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"red", "#fff", "#1122334", "#112233445", "#1122334g", "#112233;background:url(x)"})
        @DisplayName("should reject anything else")
        void shouldReject_whenNotHexColour(String colour) {
            assertThat(ItemStyleColour.PATTERN.matcher(colour).matches()).isFalse();
        }
    }

    @Nested
    @DisplayName("overWhite")
    class OverWhite {

        @ParameterizedTest
        @CsvSource({
                "#ff000080, #ff7f7f",
                "#00000000, #ffffff",
                "#1A2B3CFF, #1a2b3c",
                "#3ea4e1b3, #78bfea"
        })
        @DisplayName("should mix a see-through colour with white")
        void shouldMixWithWhite_whenSeeThrough(String colour, String expected) {
            assertThat(ItemStyleColour.overWhite(colour)).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"#FDFEC7", "null", "red"})
        @DisplayName("should leave any other value as it is, so solid colours keep today's look")
        void shouldLeaveUnchanged_whenNotSeeThrough(String colour) {
            assertThat(ItemStyleColour.overWhite(colour)).isEqualTo(colour);
        }
    }

    @Nested
    @DisplayName("iconColour")
    class IconColour {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"red", "#00000000", "#0000007e"})
        @DisplayName("should be dark with no colour, an unknown one, or one under 50% opaque")
        void shouldBeDark_whenNoColourOrMostlySeeThrough(String colour) {
            assertThat(ItemStyleColour.iconColour(colour)).isEqualTo(ItemStyleColour.DARK);
        }

        @ParameterizedTest
        @CsvSource({
                "#000000, #ffffff",
                "#1a5fb4, #ffffff",
                "#a51d2d, #ffffff",
                "#0000007f, #ffffff",
                "#e01b24, #ffffff",
                "#3584e4, #ffffff",
                "#26a269, #ffffff",
                "#ff7800, #00283c",
                "#62a0ea, #00283c",
                "#ffffff, #00283c",
                "#f9f06b, #00283c",
                "#99c1f1, #00283c"
        })
        @DisplayName("should be white when it has WCAG's 3:1 icon contrast, otherwise dark, from 50% opaque up")
        void shouldContrast_whenColourMostlyOpaque(String colour, String expected) {
            assertThat(ItemStyleColour.iconColour(colour)).isEqualTo(expected);
        }
    }
}
