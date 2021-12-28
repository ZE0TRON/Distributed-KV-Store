package de.tum.i13.server.kv;

import de.tum.i13.shared.Util;
import de.tum.i13.shared.keyring.ConsistentHashingService;

import java.util.ArrayList;
import java.util.logging.Logger;

public class KVTransferService {
    private final KVStore kvStore;
    private static final Logger LOGGER = Logger.getLogger(KVTransferService.class.getName());

    public KVTransferService(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    public String sendData(String from, String to) {
        LOGGER.info("KVTransferService.sendData called with from: " + from + ", and to: " + to);
        ArrayList<PersistItem> dataToSend = new ArrayList<>();
        ArrayList<PersistItem> allItems = kvStore.getAll();
        for (PersistItem item : allItems) {
            if(Util.isKeyInRange(from, to, ConsistentHashingService.findHash(item.key))){
                dataToSend.add(item);
            }
        }
        return serializeData(dataToSend);
    }

    public String receiveData(String from, String to, String data) {
        LOGGER.info("KVTransferService.receiveData called with from: " + from + ", and to: " + to + ", and data: " + data);

        ArrayList<PersistItem> dataToAdd = deserializeData(data);
        dataToAdd.stream().map((item) -> {
            try {
                return kvStore.put(item.key, item.value, "self");
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }
            return null;
        });

        return from + " " + to ;
    }

    private String serializeData(ArrayList<PersistItem> data) {
        LOGGER.info("KVTransferService.serializeData called with data: " + data.toString());
        StringBuilder sb = new StringBuilder();
        for(PersistItem item : data) {
            sb.append(item.toString());
            sb.append("\f");
        }
        if(sb.length() > 0 ) {
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return "";
    }

    private ArrayList<PersistItem> deserializeData(String data) {
        LOGGER.info("KVTransferService.deserializeData called with data: " + data);
        String[] parts = data.split("\f");
        ArrayList<PersistItem> result = new ArrayList<>();
        for (String part : parts) {
            String[] keyValue = part.split("\t");
            result.add(new PersistItem(keyValue[0], keyValue[1]));
        }
        return result;
    }
}
