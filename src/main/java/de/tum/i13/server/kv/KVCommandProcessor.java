package de.tum.i13.server.kv;

import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.ConnectionManager.EcsConnectionThread;

import java.util.logging.Logger;

public class KVCommandProcessor implements de.tum.i13.shared.CommandProcessor {
    private final KVTransferService kvTransferService;
    private static final Logger LOGGER = Logger.getLogger(KVCommandProcessor.class.getName());

    public KVCommandProcessor(KVTransferService kvTransferService) {
        this.kvTransferService = kvTransferService ;
    }

    @Override
    public String process(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 0) {
            parts = new String[]{"help"};
        }
        String res = null;
        try {
            switch (parts[0]) {
                case "request_data":
                    res = "send_data " + kvTransferService.sendData(parts[1], parts[2]);
                    break;
                case "send_data":
                    res = "ack_data " + kvTransferService.receiveData(parts[1], parts[2], parts[3]);
                    break;
                case "ack_data":
                    ConnectionManagerInterface ecsConnection = EcsConnectionThread.ECSConnection;
                    ecsConnection.send("handover_completed " + parts[1] + " " + parts[2]);
                    break;
                case "handover_data":
                    res = "handover_ack " + parts[1] + " " + parts[2];
                    break;
                default:
                    LOGGER.warning("command not found");
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
   }
}
