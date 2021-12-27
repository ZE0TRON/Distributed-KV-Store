package de.tum.i13.shared;

import de.tum.i13.client.KeyRange;

import java.util.ArrayList;

public class Util {
    public static String trimCRNL(String str) {
        while (str.charAt(str.length() -1) == '\r' ||str.charAt(str.length() -1) == '\n' ) {
            str = str.substring(0,str.length() -1);
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

    public static void parseMetadata(ArrayList<KeyRange> metaData, String[] parts) {
        int index;
        String from = parts[0];
        String to = parts[1];
        index = parts[2].indexOf(":");
        String serverIP = parts[2].substring(0, index);
        int serverPort = Integer.parseInt(parts[2].substring(index + 1));
        metaData.add(new KeyRange(from, to, serverIP, serverPort));
    }

    public static void parseKeyrange(String[] keyRanges, ArrayList<KeyRange> metaData) {
        for (String keyRange : keyRanges) {
            String[] parts = keyRange.split(",");
            if (parts.length != 3 || !parts[2].contains(":")) {
                throw new RuntimeException("Invalid key range: " + keyRange);
            }
            Util.parseMetadata(metaData, parts);
        }
    }
}
