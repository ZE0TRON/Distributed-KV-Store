package de.tum.i13.ecs.keyring;

import de.tum.i13.shared.Constants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConsistentHashingService implements HashService{
    private final String hashingAlgorithm;
    private final MessageDigest hasher;
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(Constants.STRING_CHARSET);


    public ConsistentHashingService(String hashingAlgorithm) throws NoSuchAlgorithmException {
        this.hashingAlgorithm = hashingAlgorithm;
        this.hasher = MessageDigest.getInstance(this.hashingAlgorithm);

    }

    @Override
    public byte[] hash(String content){
        return this.hasher.digest(content.getBytes(Constants.STRING_CHARSET));
    }

    public static String byteHashToStringHash(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
    public static String findHash(String key) {
        try {
            return ConsistentHashingService.byteHashToStringHash((new ConsistentHashingService("md5")).hash(key));
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
