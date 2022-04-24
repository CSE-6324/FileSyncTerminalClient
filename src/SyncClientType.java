public enum SyncClientType {
    MAC ("mac"),
    WINDOWS ("windows");

    private final String clientName;

    SyncClientType (String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return this.clientName;
    }
}
