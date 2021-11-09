package de.tum.i13.server.ConnectionManager;

import de.tum.i13.shared.Constants;

import java.io.*;
import java.net.Socket;

public class ConnectionManager implements ConnectionManagerInterface{

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public ConnectionManager(Socket clientSocket) throws IOException {
        this.socket = clientSocket;
        in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), Constants.TELNET_ENCODING));
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), Constants.TELNET_ENCODING));

    }


    public void send(String message) {
        out.write(message+"\r\n");
        out.flush();
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void disconnect() {

    }
}
