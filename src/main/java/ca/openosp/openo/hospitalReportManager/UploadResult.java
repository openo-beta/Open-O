package ca.openosp.openo.hospitalReportManager;

public class UploadResult {

    public enum FileStatus {
        COMPLETED,
        FAILED,
        INVALID
    }

    private final FileStatus status;
    private final String errorMessage;

    public UploadResult(FileStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public FileStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }

    public String getCssClass() {
        switch (status) {
            case COMPLETED: return "success";
            case FAILED: return "failed";
            default: return "invalid";
        }
    }

    public String getStatusText() {
        switch (status) {
            case COMPLETED: return "Uploaded Successfully";
            case FAILED: return "Failed to handle HRM report";
            default: return "Invalid File";
        }
    }
}
