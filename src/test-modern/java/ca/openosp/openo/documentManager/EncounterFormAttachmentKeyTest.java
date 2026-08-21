package ca.openosp.openo.documentManager;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
@Tag("fast")
class EncounterFormAttachmentKeyTest {

    @Test
    void roundTripsQualifiedFormIdentity() {
        EncounterFormAttachmentKey key = EncounterFormAttachmentKey.parse("formGrowthChart|42");

        assertEquals("formGrowthChart", key.getFormTable());
        assertEquals(42, key.getFormId());
        assertEquals("formGrowthChart|42", key.encode());
        assertEquals(key, EncounterFormAttachmentKey.of("formGrowthChart", 42));
    }

    @Test
    void rejectsMalformedAndUnsafeValues() {
        assertThrows(IllegalArgumentException.class, () -> EncounterFormAttachmentKey.parse(null));
        assertThrows(IllegalArgumentException.class, () -> EncounterFormAttachmentKey.parse("42"));
        assertThrows(IllegalArgumentException.class, () -> EncounterFormAttachmentKey.parse("formAnnual|0"));
        assertThrows(IllegalArgumentException.class, () -> EncounterFormAttachmentKey.parse("formAnnual|-1"));
        assertThrows(IllegalArgumentException.class,
                () -> EncounterFormAttachmentKey.parse("formGrowthChart;DROP TABLE consultdocs|1"));
        assertThrows(IllegalArgumentException.class,
                () -> EncounterFormAttachmentKey.parse("formGrowthChart|1|2"));
    }
}
