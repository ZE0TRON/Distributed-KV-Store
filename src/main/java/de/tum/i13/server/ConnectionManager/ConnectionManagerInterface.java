package de.tum.i13.server.ConnectionManager;

import java.io.IOException;

public interface ConnectionManagerInterface {
   void send(String message);
   String receive() throws IOException;
   void disconnect();
}
