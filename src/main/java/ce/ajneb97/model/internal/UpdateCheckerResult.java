package ce.ajneb97.model.internal;

public class UpdateCheckerResult {

    private final String latestVersion;
    private final boolean error;

    public UpdateCheckerResult(String latestVersion, boolean error) {
        this.latestVersion = latestVersion;
        this.error = error;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean isError() {
        return error;
    }

    public static UpdateCheckerResult noErrors(String latestVersion) {
        return new UpdateCheckerResult(latestVersion, false);
    }

    public static UpdateCheckerResult error() {
        return new UpdateCheckerResult(null, true);
    }
}
