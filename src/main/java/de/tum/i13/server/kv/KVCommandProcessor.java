package de.tum.i13.server.kv;

import de.tum.i13.server.exception.CommunicationTerminatedException;
import de.tum.i13.shared.ConnectionManager.ConnectionManagerInterface;
import de.tum.i13.shared.ConnectionManager.EcsConnectionThread;

import java.util.logging.Logger;

public class KVCommandProcessor implements CommandProcessorInterface {
    private final KVTransferService kvTransferService;
    private static final Logger LOGGER = Logger.getLogger(KVCommandProcessor.class.getName());

    public KVCommandProcessor(KVTransferService kvTransferService) {
        this.kvTransferService = kvTransferService ;
    }

    @Override
    public String process(String command) throws CommunicationTerminatedException {
        String[] parts = command.split(" ");
        if (parts.length == 0) {
            parts = new String[]{"help"};
        }
        LOGGER.info("KVCommand Processor: Received command " + command);
        LOGGER.info("KVServer state: " + CommandProcessor.serverState);
        String res = null;
        switch (parts[0]) {
            case "request_data":
                LOGGER.info("Entering SERVER_WRITE_LOCK state.");
                CommandProcessor.serverState = ServerState.SERVER_WRITE_LOCK;
            case "handover_ack":
                String dataToSend = kvTransferService.sendData(parts[1], parts[2]);
                res = "send_data " + parts[1] + " " + parts[2] + ((dataToSend.length() > 0) ? (" " + dataToSend) : "");
                break;
            case "send_data":
                if (parts.length == 4) {
                    res = "ack_data " + kvTransferService.receiveData(parts[1], parts[2], parts[3]); // receiveData(from, to, data), returns ("from to"),
                } else {
                    res = "ack_data " + parts[1] + " " + parts[2];
                }
                break;
            case "ack_data":
                ConnectionManagerInterface ecsConnection = EcsConnectionThread.ECSConnection;
                ecsConnection.send("handover_complete " + parts[1] + " " + parts[2]);
                throw new CommunicationTerminatedException();

            case "handover_data":
                LOGGER.info("Entering SERVER_WRITE_LOCK state.");
                CommandProcessor.serverState = ServerState.SERVER_WRITE_LOCK;
                res = "handover_ack " + parts[1] + " " + parts[2];
                break;
            case "Connection":

            default:
                LOGGER.warning("KVServerCommand not found.");
        }
        return res;
   }
}
