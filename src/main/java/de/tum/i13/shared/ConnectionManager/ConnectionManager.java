package de.tum.i13.shared.ConnectionManager;

import de.tum.i13.shared.Constants;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

import static de.tum.i13.shared.Util.trimCRNL;

public class ConnectionManager implements ConnectionManagerInterface{
    private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public ConnectionManager(Socket clientSocket) throws IOException {
        this.socket = clientSocket;
        in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), Constants.TELNET_ENCODING));
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), Constants.TELNET_ENCODING));

    }


    public void send(String message) {
        LOGGER.info("sending message " + message + " to the client");
        LOGGER.info(" Message length" + message.length());
        message = trimCRNL(message);
        out.write(message + "\r\n");
        out.flush();
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void disconnect() {

    }
}
