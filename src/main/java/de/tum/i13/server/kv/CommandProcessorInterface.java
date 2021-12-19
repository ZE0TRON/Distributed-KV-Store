package de.tum.i13.server.kv;


import de.tum.i13.server.exception.CommunicationTerminatedException;

public interface CommandProcessorInterface {
    String process(String command) throws CommunicationTerminatedException;
}
