package de.tum.i13.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Milestone1Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        ActiveConnection activeConnection = null;
        for(;;) {
            System.out.print("EchoClient> ");
            String line = reader.readLine();
            String[] command = line.split(" ");
            //System.out.print("command:");
            //System.out.println(line);
            switch (command[0]) {
                case "connect": activeConnection = buildConnection(command); break;
                case "put" : sendPutRequest(activeConnection, command, line); break;
                case "get" : sendGetRequest(activeConnection, command, line); break;
                case "disconnect": closeConnection(activeConnection); break;
                case "help": printHelp(); break;
                case "quit": printEchoLine("Application exit!"); return;
                default: printEchoLine("Unknown command");
            }
        }
    }



    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("connect <address> <port> - Tries to establish a TCP- connection to the echo server based on the given server address and the port number of the echo service.");
        System.out.println("disconnect - Tries to disconnect from the connected server.");
        System.out.println("send <message> - Sends a text message to the echo server according to the communication protocol.");
        System.out.println("logLevel <level> - Sets the logger to the specified log level (ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF)");
        System.out.println("help - Display this help");
        System.out.println("quit - Tears down the active connection to the server and exits the program execution.");
    }

    private static void printEchoLine(String msg) {
        System.out.println("EchoClient> " + msg);
    }

    private static void closeConnection(ActiveConnection activeConnection) {
        if(activeConnection != null) {
            try {
                activeConnection.close();
            } catch (Exception e) {
                //e.printStackTrace();
                //TODO: handle gracefully
                activeConnection = null;
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
        if (command.length > 3 || command.length == 1 || firstSpace == -1 ){
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
                EchoConnectionBuilder kvcb = new EchoConnectionBuilder(command[1], Integer.parseInt(command[2]));
                ActiveConnection ac = kvcb.connect();
                System.out.println("Waiting read");
                String confirmation = ac.readline();
                System.out.println("Confirmation passed");
                printEchoLine(confirmation);
                return ac;
            } catch (Exception e) {
                //TODO: separate between could not connect, unknown host, invalid port and no port
                printEchoLine("Could not connect to server");
            }
        }
        return null;
    }
}
