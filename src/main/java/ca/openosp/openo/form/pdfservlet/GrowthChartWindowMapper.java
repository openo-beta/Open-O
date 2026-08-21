package ca.openosp.openo.form.pdfservlet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Maps the most recent populated measurement window into a template's visible table slots.
 */
public final class GrowthChartWindowMapper {
    private GrowthChartWindowMapper() {
    }

    /**
     * Produces the same pair-preserving swaps as the legacy growth-chart print JSPs.
     */
    public static Map<String, String> mostRecentWindow(
            Properties values, List<String> fieldPrefixes, int maximumIndex, int windowSize) {
        int highestPopulated = 0;
        for (int index = 1; index <= maximumIndex; index++) {
            for (String prefix : fieldPrefixes) {
                if (!values.getProperty(prefix + "_" + index, "").trim().isEmpty()) {
                    highestPopulated = index;
                    break;
                }
            }
        }
        if (highestPopulated <= windowSize) {
            return Map.of();
        }

        int windowStart = ((highestPopulated - 1) / windowSize) * windowSize + 1;
        if (windowStart + windowSize - 1 > maximumIndex) {
            windowStart = maximumIndex - windowSize + 1;
        }

        Map<String, String> overrides = new LinkedHashMap<>();
        for (String prefix : fieldPrefixes) {
            for (int offset = 0; offset < windowSize; offset++) {
                int visibleIndex = offset + 1;
                int sourceIndex = windowStart + offset;
                String visibleKey = prefix + "_" + visibleIndex;
                String sourceKey = prefix + "_" + sourceIndex;
                overrides.put(visibleKey, values.getProperty(sourceKey, ""));
                overrides.put(sourceKey, values.getProperty(visibleKey, ""));
            }
        }
        return overrides;
    }
}
