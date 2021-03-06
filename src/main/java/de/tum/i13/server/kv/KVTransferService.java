package de.tum.i13.server.kv;

import de.tum.i13.shared.Server;
import de.tum.i13.shared.Util;
import de.tum.i13.ecs.keyring.ConsistentHashingService;

import java.util.ArrayList;
import java.util.HashMap;
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

    public String sendSubData(String from, String to) {
        LOGGER.info("KVTransferService.sendSubData called with from: " + from + ", and to: " + to);
        StringBuilder sb = new StringBuilder();
        HashMap<String, ArrayList<Server>> subscriptions = kvStore.getSubscriptions();
        for (String key : subscriptions.keySet()) {
            if(Util.isKeyInRange(from, to, ConsistentHashingService.findHash(key))){
                sb.append(key);
                sb.append(",");
                ArrayList<Server> list = subscriptions.get(key);
                for (Server server : list) {
                    sb.append(server.getAddress());
                    sb.append(":");
                    sb.append(server.getPort());
                    sb.append("-");
                }
                sb.deleteCharAt(sb.length()-1); // delete last -
                sb.append(";");
            }
        }
        return sb.toString();
    }

    public String receiveSubData(String from, String to, String data) {
        LOGGER.info("KVTransferService.receiveSubData called with from: " + from + ", and to: " + to + ", and data: " + data);

        String[] elems = data.split(";");
        for (String keyClientPair : elems) {
            String key = keyClientPair.split(",")[0];
            String clientsString = keyClientPair.split(",")[1];
            String[] clients = clientsString.split("-");
            for (String client : clients) {
                try {
                    kvStore.addSubscription(key, client);
                } catch (Exception e ) {
                    LOGGER.warning("Exception while adding subscription(transfered from other server)" + e.getMessage() );
                }
            }
        }

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

    public String getValue(String key) {
        try {
            KVClientMessage clientMessage = kvStore.get(key);

            if (clientMessage.getStatus() == KVClientMessage.StatusType.GET_SUCCESS) {
                return clientMessage.getValue();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void put(String key, String value) {
        try {
            LOGGER.info("Put reflection call with key: " + key + " and value: " + value);
            kvStore.put(key, value, "self");
        } catch (Exception e) {
            LOGGER.warning("Exception during put reflection put call " + e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            LOGGER.info("Delete reflection call with key: " + key);
            kvStore.put(key, null, "self");
        } catch (Exception e) {
            LOGGER.warning("Exception during delete reflection delete call " + e.getMessage());
        }
    }
}
