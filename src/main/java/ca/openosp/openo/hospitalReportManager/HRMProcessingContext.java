package ca.openosp.openo.hospitalReportManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.openosp.openo.hospitalReportManager.model.HRMDocument;

/**
 * Carries mutable state through the HRM report-inbox processing pipeline so that individual
 * steps (demographic routing, provider routing, sub-class linking) share context without
 * passing long parameter lists or nullable warnings lists.
 *
 * @since 2026-06-05
 */
public final class HRMProcessingContext {

    private final HRMReport report;
    private HRMDocument document;
    private final List<String> warnings = new ArrayList<>();

    public HRMProcessingContext(HRMReport report) {
        this.report = report;
    }

    public HRMReport getReport() {
        return report;
    }

    public HRMDocument getDocument() {
        return document;
    }

    public void setDocument(HRMDocument document) {
        this.document = document;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public void addWarnings(Collection<String> additional) {
        warnings.addAll(additional);
    }

    /**
     * @return List&lt;String&gt; an unmodifiable view of warnings; never null
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
