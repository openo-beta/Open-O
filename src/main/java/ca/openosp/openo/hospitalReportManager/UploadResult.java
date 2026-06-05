package ca.openosp.openo.hospitalReportManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UploadResult {

    public enum FileStatus {
        COMPLETED,
        FAILED,
        INVALID
    }

    private final FileStatus status;
    private final String errorMessage;
    private final List<String> warnings;

    public UploadResult(FileStatus status, String errorMessage) {
        this(status, errorMessage, Collections.emptyList());
    }

    public UploadResult(FileStatus status, String errorMessage, List<String> warnings) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.warnings = warnings == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    public FileStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public List<String> getWarnings() { return warnings; }
    public boolean isHasWarnings() { return !warnings.isEmpty(); }

    public String getCssClass() {
        switch (status) {
            case COMPLETED: return warnings.isEmpty() ? "success" : "warning";
            case FAILED: return "failed";
            default: return "invalid";
        }
    }

    public String getStatusText() {
        switch (status) {
            case COMPLETED:
                return warnings.isEmpty() ? "Uploaded Successfully" : "Uploaded with Warnings";
            case FAILED: return "Failed to handle HRM report";
            default: return "Invalid File";
        }
    }
}
