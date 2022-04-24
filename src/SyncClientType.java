public enum SyncClientType {
    MAC ("mac", "/Users/sudiptasharif/repos/FileSyncer/project_files/mac_files/"),
    WINDOWS ("windows", "/Users/sudiptasharif/repos/FileSyncer/project_files/win_files/"),
    UNKNOWN ("unknown", "");

    private final String clientName;
    private final String localFilePath;

    SyncClientType (String clientName, String localFilePath) {
        this.clientName = clientName;
        this.localFilePath = localFilePath;
    }

    public String getClientName() {
        return this.clientName;
    }

    public String getLocalFilePath() {
        return this.localFilePath;
    }
}
