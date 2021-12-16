package de.tum.i13.server.kv;

import java.util.Map;

public class ServerMetadata {
    private Map<KVServerRange, KVServerAddr> metadata;

    public class KVServerRange {
        private String rangeStart;
        private String rangeEnd;
    }

    public class KVServerAddr {
        private String ip;
        private String port;
    }

    public Map<KVServerRange, KVServerAddr> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<KVServerRange, KVServerAddr> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        String metadataString = " ";
        for (Map.Entry<KVServerRange, KVServerAddr> entry : metadata.entrySet()) {
            KVServerRange range = entry.getKey();
            KVServerAddr addr = entry.getValue();
            String metadataEntryStr = range.rangeStart + "," + range.rangeEnd + "," + addr.ip + ":" + addr.port + ";";
            metadataString.concat(metadataEntryStr);
        }
        return metadataString;
    }
}
