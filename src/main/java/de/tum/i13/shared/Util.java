package de.tum.i13.shared;

public class Util {
    public static String trimCRNL(String str) {
        while (str.charAt(str.length() -1) == '\r' ||str.charAt(str.length() -1) == '\n' ) {
            str = str.substring(0,str.length() -1);
        }
        return str;
    }
}
