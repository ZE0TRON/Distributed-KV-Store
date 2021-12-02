package de.tum.i13.server.kv;

import java.util.Map;

public class KVServerMetadata {
    private Map<KVServerRange,KVServerConfig> metadata;



    public class KVServerConfig {
        private String ip;
        private String port;
    }

    public class KVServerRange {
        private String rangeStart;
        private String rangeEnd;
    }

    public Map<KVServerRange, KVServerConfig> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<KVServerRange, KVServerConfig> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "KVServerMetadata{" +
                "metadata=" + metadata +
                '}';
    }
}
