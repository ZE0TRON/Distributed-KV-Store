//package de.tum.i13.shared.ConnectionManager;
//
//import de.tum.i13.shared.CommandProcessor;
//import de.tum.i13.shared.Server;
//
//import java.io.IOException;
//import java.net.Socket;
//import java.util.HashMap;
//import java.util.logging.Logger;
//
//public class ServerMonitoringThread extends Thread {
//    private static final Logger LOGGER = Logger.getLogger(ServerConnectionThread.class.getName());
//
//    private Server server;
//    private Socket clientSocket;
//
//    public ServerMonitoringThread(Server server) {
//       this.server = server;
//    }
//
//    @Override
//    public void run() {
//        try {
//            this.connectionManager = new ConnectionManager(clientSocket);
//            connections.put(clientSocket.getInetAddress().toString().substring(1) + ":" + clientSocket.getPort(), this.connectionManager);
//        } catch (IOException e) {
//            LOGGER.warning(e.getMessage());
//        }
//        try {
//            String recv, res;
//            while ( (recv = connectionManager.receive()) != null) {
//                res = cp.process(recv);
//                if(res != null) {
//                    this.connectionManager.send(res+ "\r\n");
//                }
//            }
//        } catch(Exception ex) {
//            LOGGER.warning(ex.getMessage());
//            ex.printStackTrace();
//        }
//    }
//}
