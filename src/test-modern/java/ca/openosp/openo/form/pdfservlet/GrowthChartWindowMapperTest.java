package ca.openosp.openo.form.pdfservlet;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@Tag("fast")
class GrowthChartWindowMapperTest {
    private static final List<String> FIELDS = List.of("date", "weight");

    @Test
    void leavesEmptyAndFirstWindowRecordsUnchanged() {
        assertTrue(GrowthChartWindowMapper.mostRecentWindow(
                new Properties(), FIELDS, 42, 7).isEmpty());

        Properties firstWindow = measurements(1, 7);
        assertTrue(GrowthChartWindowMapper.mostRecentWindow(
                firstWindow, FIELDS, 42, 7).isEmpty());
    }

    @Test
    void mapsMostRecentCompleteWindowToVisibleSlotsWithoutBreakingPairs() {
        Properties values = measurements(1, 42);

        Map<String, String> overrides = GrowthChartWindowMapper.mostRecentWindow(
                values, FIELDS, 42, 7);

        assertEquals("date-36", overrides.get("date_1"));
        assertEquals("weight-36", overrides.get("weight_1"));
        assertEquals("date-42", overrides.get("date_7"));
        assertEquals("weight-42", overrides.get("weight_7"));
        assertEquals("date-1", overrides.get("date_36"));
        assertEquals("weight-1", overrides.get("weight_36"));
    }

    @Test
    void mapsPartiallyPopulatedLaterWindowUsingLegacyWindowBoundaries() {
        Properties values = measurements(1, 9);

        Map<String, String> overrides = GrowthChartWindowMapper.mostRecentWindow(
                values, FIELDS, 42, 7);

        assertEquals("date-8", overrides.get("date_1"));
        assertEquals("weight-9", overrides.get("weight_2"));
        assertEquals("", overrides.get("date_7"));
    }

    @Test
    void supportsFiveRowInfantHeadCircumferenceWindow() {
        Properties values = measurements(1, 20);

        Map<String, String> overrides = GrowthChartWindowMapper.mostRecentWindow(
                values, FIELDS, 20, 5);

        assertEquals("date-16", overrides.get("date_1"));
        assertEquals("weight-20", overrides.get("weight_5"));
    }

    private Properties measurements(int start, int end) {
        Properties values = new Properties();
        for (int index = start; index <= end; index++) {
            values.setProperty("date_" + index, "date-" + index);
            values.setProperty("weight_" + index, "weight-" + index);
        }
        return values;
    }
}
