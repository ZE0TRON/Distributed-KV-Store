package de.tum.i13.ecs.keyring;

import de.tum.i13.shared.Constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConsistentHashingService implements HashService{
    private final String hashingAlgorithm;
    private final MessageDigest hasher;


    public ConsistentHashingService(String hashingAlgorithm) throws NoSuchAlgorithmException {
        this.hashingAlgorithm = hashingAlgorithm;
        this.hasher = MessageDigest.getInstance(this.hashingAlgorithm);
    }

    @Override
    public byte[] hash(String content){
        return this.hasher.digest(content.getBytes(Constants.STRING_CHARSET));
    }
}
