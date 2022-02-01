package de.tum.i13.shared;

import de.tum.i13.client.KeyRange;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Util {
    public static String trimCRNL(String str) {
        while (str.charAt(str.length() - 1) == '\r' || str.charAt(str.length() - 1) == '\n') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static boolean isKeyInRange(String from, String to, String key) {
        if (from.compareTo(to) >= 0) {
            return from.compareTo(key) < 0 || to.compareTo(key) > 0;
            // from =5
            // key =12
            // to = 20

            // from = 25
            // key = 3
            // to = 5

            // from = 25
            // key = 27
            // to = 5
        } else return from.compareTo(key) < 0 && to.compareTo(key) >= 0;
    }

    public static void parseMetadata(ArrayList<KeyRange> metadata, String[] parts) {
        int index;
        String from = parts[0];
        String to = parts[1];
        index = parts[2].indexOf(":");
        String serverIP = parts[2].substring(0, index);
        int serverPort = Integer.parseInt(parts[2].substring(index + 1));
        metadata.add(new KeyRange(from, to, serverIP, serverPort));
    }

    public static void parseKeyrange(String[] keyRanges, ArrayList<KeyRange> metadata) {
        for (String keyRange : keyRanges) {
            String[] parts = keyRange.split(",");
            if (parts.length != 3 || !parts[2].contains(":")) {
                throw new RuntimeException("Invalid key range: " + keyRange);
            }
            Util.parseMetadata(metadata, parts);
        }
    }

    public static String clientSocketToIpString(Socket clientSocket) {
        return clientSocket.getInetAddress().toString().substring(1);
    }
    /**
     * Checks to see if a specific port is available.
     * Taken from http://svn.apache.org/viewvc/camel/trunk/components/camel-test/src/main/java/org/apache/camel/test/AvailablePortFinder.java?view=markup#l130
     * @param port the port to check for availability
     */
    public static boolean available(int port) {
        if (port < 1000 || port > 65536) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    public static int getRandomAvailablePort() {
        int port;
        do {
            Random random = new Random();
            port = random.nextInt(55535) + 1001;
        }
        while (!available(port));
        return port;
    }
}
