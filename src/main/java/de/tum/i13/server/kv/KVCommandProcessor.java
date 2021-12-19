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
        String res = null;
        switch (parts[0]) {
            case "request_data":
                CommandProcessor.serverState = ServerState.SERVER_WRITE_LOCK;
            case "handover_ack":
                res = "send_data " + parts[1] + " " + parts[2] + " " + kvTransferService.sendData(parts[1], parts[2]);
                break;
            case "send_data":
                res = "ack_data " + kvTransferService.receiveData(parts[1], parts[2], parts[3]); // receiveData(from, to, data), returns ("from to"),
                break;
            case "ack_data":
                ConnectionManagerInterface ecsConnection = EcsConnectionThread.ECSConnection;
                ecsConnection.send("handover_completed " + parts[1] + " " + parts[2]);
                throw new CommunicationTerminatedException();

            case "handover_data":
                CommandProcessor.serverState = ServerState.SERVER_WRITE_LOCK;
                res = "handover_ack " + parts[1] + " " + parts[2];
                break;
            default:
                LOGGER.warning("KVServerCommand not found");
        }
        return res;
   }
}
