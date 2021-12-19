package de.tum.i13.shared.ConnectionManager;

import java.io.IOException;

public interface ConnectionManagerInterface {
   void send(String message);
   String receive() throws IOException;
   void disconnect();
   int getSocketPort();
}
