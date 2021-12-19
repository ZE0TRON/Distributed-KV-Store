package de.tum.i13.shared;

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
}
