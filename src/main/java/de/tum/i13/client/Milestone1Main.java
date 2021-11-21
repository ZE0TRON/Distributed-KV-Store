package de.tum.i13.client;

import de.tum.i13.client.exception.ConnectionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tum.i13.shared.LogSetup.setupLogging;


public class Milestone1Main {
    private static Logger LOGGER;
    public static void main(String[] args) throws IOException {
       setupLogging(Paths.get("echo-client.log"), Level.ALL);
       LOGGER = Logger.getLogger(Milestone1Main.class.getName());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        ActiveConnection activeConnection = null;
        for(;;) {
            System.out.print("EchoClient> ");
            String line = reader.readLine().trim();
            LOGGER.info("Client received command " + line);
            String[] command = line.split(" ");
            switch (command[0]) {
                case "connect":
                    activeConnection = buildConnection(command);
                    LOGGER.info("Client connecting");
                    break;
                case "put" :
                    sendPutRequest(activeConnection, command, line);
                    LOGGER.info("Client connecting");
                    break;
                case "get" :
                    sendGetRequest(activeConnection, command, line);
                    LOGGER.info("Client requesting get");
                    break;
                case "disconnect":
                    closeConnection(activeConnection);
                    LOGGER.info("Client disconnecting");
                    break;
                case "help":
                case "":
                    printHelp();
                    LOGGER.info("Client printing help");
                    break;
                case "quit":
                    printEchoLine("Application exit!");
                    LOGGER.info("Client exiting");
                    return;
                default:
                    printEchoLine("Unknown command.");
                    LOGGER.info("Client unknown command");
            }
        }
    }

    private static void printHelp() {
        System.out.println("\tAvailable commands:");
        System.out.println("\t\tconnect <address> <port> - Tries to establish a TCP connection to the server.");
        System.out.println("\t\tdisconnect - Tries to disconnect from the connected server.");
        System.out.println("\t\tsend <message> - Sends a text message to the server according to the communication protocol.");
        System.out.println("\t\tlogLevel <level> - Sets the logger to the specified log level (ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF)");
        System.out.println("\t\thelp - Displays this help description.");
        System.out.println("\t\tquit - Tears down the active connection to the server and exits the program execution.");
    }

    private static void printEchoLine(String msg) {
        String[] parts = msg.split(" ");
        StringBuilder output = new StringBuilder();
        for (int i=0; i < parts.length; i++)  {
            String part = parts[i];
//            if (i == 0) {
//              part = part.toLowerCase(Locale.ROOT);
//            }
            if (!Objects.equals(part, "null")) {
               output.append(part).append(" ");
            }
        }
        output.deleteCharAt(output.length() - 1);
        System.out.println("EchoClient> " + output);
    }

    private static void closeConnection(ActiveConnection ac) {
        if (ac == null || !ac.isSocketInitiated() || !ac.isSocketConnected() || ac.isSocketClosed()) {
            printEchoLine("Not connected.");
        }
        else {
            try {
                String info = ac.getInfo();
                ac.close();
                printEchoLine("Connection with " + info  + " has been terminated.");
            } catch (IOException e) {
                throw new ConnectionException("Error while disconnecting ", e);
            }
        }
    }

    private static void sendGetRequest(ActiveConnection activeConnection, String[] command, String line) {
        if(activeConnection == null) {
            printEchoLine("Error! Not connected!");
            return;
        }
        int firstSpace = line.indexOf(" ");
        if(firstSpace == -1 || firstSpace + 1 >= line.length()) {
            printEchoLine("Error! Nothing to send!");
            return;
        }
        activeConnection.write(line);

        try {
            String response = activeConnection.readline();
            printEchoLine(response);
        } catch (IOException e) {
            printEchoLine("Error! Not connected!");
        }
    }

    // This method replicates a lot of lines of the sendGetRequest method, to avoid line duplication we can put handle both
    // of the requests in one func but it didn't felt like good practice (we should keep them separate I believe). Maybe
    // we can generate helper funcs to check inputs or read/write to buffers. Would like feedbacks here. -cenk
    private static void sendPutRequest(ActiveConnection activeConnection, String[] command, String line) {
        if(activeConnection == null) {
            printEchoLine("Error! Not connected!");
            return;
        }
        int firstSpace = line.indexOf(" ");
        if (command.length == 1 || firstSpace == -1 ){
            printEchoLine("Error! A put request must be like this: put <key> <value>.");
            return;
        }
        activeConnection.write(line);

        try {
            String response = activeConnection.readline();
            printEchoLine(response);
        } catch (IOException e) {
            printEchoLine("Error! Not connected!");
        }
    }

    private static ActiveConnection buildConnection(String[] command) {
        if(command.length == 3){
            try {
                String host = command[1];
                Milestone1Main.checkValidInternetAddress(host);
                int port = Integer.parseInt(command[2]);
                EchoConnectionBuilder kvcb = new EchoConnectionBuilder(host, port);
                ActiveConnection ac = kvcb.connect();
                String confirmation = ac.readline();
                printEchoLine(confirmation + " with " + ac.getInfo() + ".");
                return ac;
            } catch (NumberFormatException e) {
                printEchoLine("Invalid port number for connect command!");
            } catch (ConnectionException e) {
                printEchoLine(e.getMessage());
            } catch (ConnectException e) {
                printEchoLine("Server is unreachable, connection refused.");
            } catch (Exception e) {
                printEchoLine("Could not connect to server!");
            }
        }
        else {
            printEchoLine("Invalid number of arguments, the command must be in this format: connect <addr> <port>");
        }
        return null;
    }

    private static void checkValidInternetAddress(String url) {
        try {
            InetAddress.getByName(url);
        } catch (UnknownHostException ex) {
            throw new ConnectionException("Invalid address for connect command!");
        }
    }
}
