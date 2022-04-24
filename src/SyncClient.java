/**
 * @author sharif
 */

public class SyncClient {
    private SyncClientType clientType;

    public SyncClient(String clientType) {
        this.clientType = getSyncClientType(clientType);
    }

    private SyncClientType getSyncClientType(String clientType) {
        if (clientType.equals("mac") || clientType.equals("m")) {
            return SyncClientType.MAC;
        } else if (clientType.equals("windows") || clientType.equals("win") || clientType.equals("w")) {
            return SyncClientType.WINDOWS;
        } else {
            return SyncClientType.UNKNOWN;
        }
    }
}

