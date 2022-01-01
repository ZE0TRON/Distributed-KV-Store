package de.tum.i13.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tum.i13.shared.Util;
import de.tum.i13.shared.keyring.ConsistentHashingService;

public class KVStoreClientLibraryImpl implements KVStoreClientLibrary {


    private ArrayList<KeyRange> metaDataCoordinator;
    private ArrayList<KeyRange> metaDataAll;

    private static final int MAX_SLEEP_IN_MILLI_SECOND = 1000;
    private static final int SLEEP_BASE_IN_MILLI_SECOND = 10;

    /**
     * Constructs a {@code KVStoreClientLibraryImpl} with the given address and port
     * and initializes its metadata list.
     *
     * @param host the address of this {@code KVStoreClientLibraryImpl}
     * @param port the port of this {@code KVStoreClientLibraryImpl}
     */
    public KVStoreClientLibraryImpl(String host, int port) {
        this.metaDataCoordinator = new ArrayList<>();
        this.metaDataCoordinator.add(new KeyRange(null, null, host, port));

        this.metaDataAll = new ArrayList<>();
        this.metaDataAll.add(new KeyRange(null, null, host, port));
    }

    /**
     * Sends the given get request to the server if it is valid request.
     *
     * @param line the request to be sent.
     * @return the response of the server if the given request line is a valid get
     * request, error message otherwise.
     */
    public String sendGetRequest(String line) throws Exception {
        String[] parts = line.split(" ");

        if (parts.length != 2)
            return "Error! Invalid command!" + line;

        return sendRequest(line, true);
    }

    /**
     * Sends the given put request to the server if it is valid request.
     *
     * @param line the request to be sent.
     * @return the response of the server if the given request line is a valid put
     * request, error message otherwise.
     */
    public String sendPutRequest(String line) throws Exception {
        String[] parts = line.split(" ");

        if (parts.length != 3)
            return "Error! Invalid command!" + line;

        return sendRequest(line, false);

    }

    /**
     * Sends the given delete request to the server if it is valid request.
     *
     * @param line the request to be sent.
     * @return the response of the server if the given request line is a valid
     * delete request, error message otherwise.
     */
    public String sendDeleteRequest(String line) throws Exception {
        String[] parts = line.split(" ");

        if (parts.length != 2)
            return "Error! Invalid command!" + line;

        return sendRequest(line, false);
    }

    /**
     * Realizes the parts in sending get, put and delete requests.
     *
     * @param line the request to be sent.
     * @return the response of the server if the given request line is a valid
     * request, error message otherwise.
     *
     * @throws Exception if an error occurs while connection the server
     */
    private String sendRequest(String line, boolean read) throws Exception {
        return sendRequest(line, 0, read);
    }

    private String sendRequest(String line, int attempt, boolean read) throws Exception {
        String[] parts = line.split(" ");


        KeyRange kr;
        if (read) {
            List<KeyRange> list = findCorrectKeyRanges(parts[1]);
            kr = list.get((int) (Math.random() * list.size()));
        } else {
            kr = findCorrectKeyRange(parts[1]);
        }
        String response = CommandSender.sendCommandToServer(kr.host, kr.port, line);

        parts = response.split(" ");

        switch (parts[0]) {
            case "not_responsible":
                if (read) {
                    updateKeyRangesRead(kr.host, kr.port);
                } else {
                    updateKeyRanges(kr.host, kr.port);
                }
                return sendRequest(line, read);
            case "server_stopped":
                sleepBackoffAndJitter(attempt);
                attempt++;
                return sendRequest(line, attempt, read);

            default:
                return response;
        }
    }

    /**
     * Causes the currently executing thread to sleep for the number of milliseconds
     * specified by "Full Jitter" algorithm explained in
     * https://aws.amazon.com/tr/blogs/architecture/exponential-backoff-and-jitter/
     *
     * @param attempt number of attempts made by the client
     * @throws InterruptedException if any thread has interrupted the current
     *                              thread.
     */
    private void sleepBackoffAndJitter(int attempt) throws InterruptedException {
        int sleep = Math.min(MAX_SLEEP_IN_MILLI_SECOND, SLEEP_BASE_IN_MILLI_SECOND * (int) Math.pow(2, attempt));
        sleep = (int) (Math.random() * sleep);
        Thread.sleep(sleep);
    }

    /**
     * Sends the "keyrange" command to the server and updates the key ranges
     * according to the response of the server.
     *
     * @param host the address of the server.
     * @param port the port of the server.
     * @throws IOException      if an error occurs while sending the "keyrange"
     *                          command to the server.
     * @throws RuntimeException if the provided key ranges in the response of the
     *                          server are not in a valid form.
     */
    public void updateKeyRanges(String host, int port) throws IOException {
        String response = CommandSender.sendCommandToServer(host, port, "keyrange");
        int index = response.indexOf("keyrange_success");

        metaDataCoordinator = new ArrayList<>();
        parseKeyRange(response, index, metaDataCoordinator);
    }

    public void updateKeyRangesRead(String host, int port) throws IOException {
        String response = CommandSender.sendCommandToServer(host, port, "keyrange_read");
        int index = response.indexOf("keyrange_success");

        metaDataAll = new ArrayList<>();
        parseKeyRange(response, index, metaDataAll);
    }

    private void parseKeyRange(String response, int index, ArrayList<KeyRange> metaData) {
        if (index == -1) {
            if (response.contains("server_stopped")) {
                // Do nothing. next request can handle the situation
                return;
            } else {
                throw new RuntimeException("Invalid key range: " + response);
            }
        }
        String[] keyRanges = response.substring(index + "keyrange_success".length() + 1).split(";");
        for (String v : keyRanges) {
            String[] parts = v.split(",");
            if (parts.length != 3 || !parts[2].contains(":"))
                throw new RuntimeException("Invalid key range: " + v);
            Util.parseMetadata(metaData, parts);
        }
    }

    /**
     * finds the key range that the given key belongs to.
     *
     * @param key the key whose key range is searched for.
     * @return {@code KeyRange} that the given key belongs to.
     */
    public KeyRange findCorrectKeyRange(String key) {

        if (metaDataCoordinator.size() == 1) {
            return metaDataCoordinator.get(0);
        }

        String hashCode = ConsistentHashingService.findHash(key);
        for (KeyRange keyRange : metaDataCoordinator) {
            // Wrap around
            if (Util.isKeyInRange(keyRange.from, keyRange.to, hashCode)) {
                return keyRange;
            }
        }
        return null;
    }

    public List<KeyRange> findCorrectKeyRanges(String key) {
        List<KeyRange> list = new ArrayList<>();

        if (metaDataAll.size() == 1) {
            list.add(metaDataAll.get(0));
            return list;
        }

        String hashCode = ConsistentHashingService.findHash(key);
        for (KeyRange keyRange : metaDataAll) {
            // Wrap around
            if (Util.isKeyInRange(keyRange.from, keyRange.to, hashCode)) {
                list.add(keyRange);
            }
        }
        return list;
    }
}
