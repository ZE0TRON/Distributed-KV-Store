package de.tum.i13.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static de.tum.i13.shared.Util.trimCRNL;

/**
 * Created by chris on 19.10.15.
 */
public class ActiveConnection implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter output;
    private final BufferedReader input;

    public ActiveConnection(Socket socket, PrintWriter output, BufferedReader input) {
        this.socket = socket;

        this.output = output;
        this.input = input;
    }

    public void write(String command) {
        command = trimCRNL(command);
        output.write(command + "\r\n");
        output.flush();
    }

    public String readline() throws IOException {
        return input.readLine();
    }

    public void close() throws IOException {
        output.close();
        input.close();
        socket.close();
    }



    public boolean isSocketClosed(){
        return this.socket.isClosed();
    }

    public boolean isSocketInitiated(){
        return this.socket != null;
    }

    public boolean isSocketConnected(){
        return this.socket.isConnected();
    }

    public String getInfo() {
        return this.socket.getRemoteSocketAddress().toString();
    }
}
