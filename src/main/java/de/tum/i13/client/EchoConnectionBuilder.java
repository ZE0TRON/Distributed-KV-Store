package de.tum.i13.client;

import de.tum.i13.shared.Constants;

import java.io.*;
import java.net.Socket;

/**
 * Created by chris on 19.10.15.
 */
public class EchoConnectionBuilder {

    private final String host;
    private final int port;

    public EchoConnectionBuilder(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ActiveConnection connect() throws IOException {
        Socket s = new Socket(this.host, this.port);

        PrintWriter output = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), Constants.TELNET_ENCODING));
        BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream(), Constants.TELNET_ENCODING));

        return new ActiveConnection(s, output, input);
    }
}
