package ca.openosp.openo.appt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link IconSet}: what it holds, in what order, and how a stored icon's kind is told.
 *
 * @since 2026-10-07
 */
@DisplayName("IconSet unit tests")
@Tag("unit")
@Tag("fast")
public class IconSetUnitTest {

    /* The width of appointment_status.icon, the narrower of the two icon columns. */
    private static final int ICON_COLUMN_WIDTH = 40;

    static List<String> images() {
        return IconSet.IMAGES;
    }

    @Nested
    @DisplayName("ICONS")
    class Icons {

        @Test
        @DisplayName("should offer the status images first, then the glyphs, each once")
        void shouldListImagesThenGlyphs() {
            assertThat(IconSet.ICONS).hasSize(225)
                    .doesNotHaveDuplicates()
                    .containsExactlyElementsOf(Stream.concat(IconSet.IMAGES.stream(), IconSet.GLYPHS.stream()).toList());
            assertThat(IconSet.ICONS.get(0)).isEqualTo("starbill.gif");
            assertThat(IconSet.ICONS.get(25)).isEqualTo("glyphicon-asterisk");
        }

        @Test
        @DisplayName("should fit every icon in the status icon column")
        void shouldFitIconColumn() {
            assertThat(IconSet.ICONS).allSatisfy(icon -> assertThat(icon).hasSizeLessThanOrEqualTo(ICON_COLUMN_WIDTH));
        }

        @ParameterizedTest
        @MethodSource("ca.openosp.openo.appt.IconSetUnitTest#images")
        @DisplayName("should list only images that have signed and verified copies")
        void shouldHaveSignedAndVerifiedCopies_whenImage(String image) {
            Path images = Path.of("src/main/webapp/images");

            assertThat(images.resolve(image)).exists();
            assertThat(images.resolve("S" + image)).exists();
            assertThat(images.resolve("V" + image)).exists();
        }

        @Test
        @DisplayName("should list every glyph the stylesheet draws, in its order")
        void shouldMatchStylesheet_whenGlyphs() throws IOException {
            String css = Files.readString(Path.of("src/main/webapp/css/glyphicons-standalone.css"));
            Matcher glyph = Pattern.compile("\\.(glyphicon-[a-z0-9-]+):before").matcher(css);

            assertThat(IconSet.GLYPHS).containsExactlyElementsOf(glyph.results().map(m -> m.group(1)).toList());
        }
    }

    @Nested
    @DisplayName("contains")
    class Contains {

        @ParameterizedTest
        @ValueSource(strings = {"here.gif", "16.gif", "glyphicon-home", "glyphicon-sort-by-attributes-alt"})
        @DisplayName("should accept an image or a glyph in the set")
        void shouldAccept_whenInSet(String icon) {
            assertThat(IconSet.contains(icon)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"Shere.gif", "lts.gif", "glyphicon-nope", "glyphicon-Home", " here.gif", "../here.gif"})
        @DisplayName("should refuse anything outside the set")
        void shouldRefuse_whenNotInSet(String icon) {
            assertThat(IconSet.contains(icon)).isFalse();
        }
    }

    @Nested
    @DisplayName("isGlyph")
    class IsGlyph {

        @ParameterizedTest
        @ValueSource(strings = {"glyphicon-home", "glyphicon-nope"})
        @DisplayName("should tell a glyph by its class name, in the set or not")
        void shouldBeGlyph_whenGlyphiconClass(String icon) {
            assertThat(IconSet.isGlyph(icon)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"here.gif", "thumb.png", "custom.jpg"})
        @DisplayName("should not take anything else for a glyph")
        void shouldNotBeGlyph_whenNotGlyphiconClass(String icon) {
            assertThat(IconSet.isGlyph(icon)).isFalse();
        }
    }
}
