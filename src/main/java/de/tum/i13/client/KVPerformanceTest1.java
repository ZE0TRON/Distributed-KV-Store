package de.tum.i13.client;

import de.tum.i13.shared.ClientConfig;
import de.tum.i13.shared.ConnectionManager.ClientNotificationHandlerThread;
import de.tum.i13.shared.Util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tum.i13.shared.LogSetup.setupLogging;

public class KVPerformanceTest1 {
    private static final Logger LOGGER = Logger.getLogger(KVPerformanceTest1.class.getName());
    public static int LISTEN_PORT;
    public static boolean IS_CONTAINER;

    public static void main(String[] args) {
        ClientConfig cfg = ClientConfig.parseCommandlineArgs(args);

        setupLogging(Paths.get("echo-client.log"), Level.ALL);

        String serverIp = cfg.serverAddr;
        int serverPort = cfg.serverPort;
        IS_CONTAINER = cfg.isContainer;
        LISTEN_PORT = cfg.port;
        if (LISTEN_PORT == 0) {
            LISTEN_PORT = Util.getRandomAvailablePort();
        }
        LOGGER.info("Initial server info host/port: " + serverIp + "/" + serverPort);

        KVStoreClientLibrary kvStore = new KVStoreClientLibraryImpl(serverIp, serverPort);

        ClientNotificationHandlerThread clientNotificationHandlerThread = new ClientNotificationHandlerThread(cfg.listenaddr, LISTEN_PORT);
        clientNotificationHandlerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing thread main KVClient. Shutdown procedure has been started.");
            try {
                shutdownProcedure();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }));
        //scenario1(kvStore, 10000);
         scenario2(kvStore, 10000);
    }

    public static void shutdownProcedure() throws IOException, InterruptedException {
        Thread.sleep(750);
        ClientNotificationHandlerThread.kill();
    }

    public static void scenario1(KVStoreClientLibrary kvStore, int numberOfIteration) {
        try {
            String[][] keyValueUpdate = createKeyValueUpdateMatrix(numberOfIteration);
            long totalPutTime = 0;
            long totalGetTime = 0;
            long totalUpdateTime = 0;
            long totalDeleteTime = 0;

            int correctPutCount = 0;
            int correctGetCount = 0;
            int correctUpdateCount = 0;
            int correctDeleteCount = 0;

            long start, end;
            String response;
            for (int i = 0; i < numberOfIteration; i++) {

                // PUT
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendPutRequest("put " + keyValueUpdate[i][0] + " " + keyValueUpdate[i][1]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalPutTime += (end - start);
                if (response.startsWith("put_success")) {
                    correctPutCount++;
                }

                // GET
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendGetRequest("get " + keyValueUpdate[i][0]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalGetTime += (end - start);
                if (response.startsWith("get_success")) {
                    correctGetCount++;
                }

                // UPDATE
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendPutRequest("put " + keyValueUpdate[i][0] + " " + keyValueUpdate[i][2]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalUpdateTime += (end - start);
                if (response.startsWith("put_update")) {
                    correctUpdateCount++;
                }

                // DELETE
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendGetRequest("delete " + keyValueUpdate[i][0]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalDeleteTime += (end - start);
                if (response.startsWith("delete_success")) {
                    correctDeleteCount++;
                }

                // if doesn't work
                //Thread.sleep(1);
            }

            System.out.println("numberOfIteration: " + numberOfIteration);
            System.out.println("totalPutTime: " + totalPutTime);
            System.out.println("correctPutCount: " + correctPutCount);
            System.out.println("totalGetTime: " + totalGetTime);
            System.out.println("correctGetCount: " + correctGetCount);
            System.out.println("totalUpdateTime: " + totalUpdateTime);
            System.out.println("correctUpdateCount: " + correctUpdateCount);
            System.out.println("totalDeleteTime: " + totalDeleteTime);
            System.out.println("correctDeleteCount: " + correctDeleteCount);

        } catch (Exception e) {
            System.out.println("Exception caught " + e.getMessage());
        }
    }


    public static void scenario2(KVStoreClientLibrary kvStore, int numberOfIteration) {
        try {
            String[][] keyValueUpdate = createKeyValueUpdateMatrix(numberOfIteration);
            long totalPutTime = 0;
            long totalGetTime = 0;
            long totalUpdateTime = 0;
            long totalDeleteTime = 0;

            int correctPutCount = 0;
            int correctGetCount = 0;
            int correctUpdateCount = 0;
            int correctDeleteCount = 0;

            long start, end;
            String response;
            for (int i = 0; i < numberOfIteration; i++) {

                // PUT
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendPutRequest("put " + keyValueUpdate[i][0] + " " + keyValueUpdate[i][1]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalPutTime += (end - start);
                if (response.startsWith("put_success")) {
                    correctPutCount++;
                }
            }

            for (int i = 0; i < numberOfIteration; i++) {
                // GET
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendGetRequest("get " + keyValueUpdate[i][0]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalGetTime += (end - start);
                if (response.startsWith("get_success")) {
                    correctGetCount++;
                }
            }

            for (int i = 0; i < numberOfIteration; i++) {

                // UPDATE
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendPutRequest("put " + keyValueUpdate[i][0] + " " + keyValueUpdate[i][2]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalUpdateTime += (end - start);
                if (response.startsWith("put_update")) {
                    correctUpdateCount++;
                }
            }

            for (int i = 0; i < numberOfIteration; i++) {
                // DELETE
                //Time start for requests
                start = System.currentTimeMillis();
                response = kvStore.sendGetRequest("delete " + keyValueUpdate[i][0]);
                // time stop for requests
                end = System.currentTimeMillis();
                totalDeleteTime += (end - start);
                if (response.startsWith("delete_success")) {
                    correctDeleteCount++;
                }

                // if doesn't work
                //Thread.sleep(1);
            }

            System.out.println("numberOfIteration: " + numberOfIteration);
            System.out.println("totalPutTime: " + totalPutTime);
            System.out.println("correctPutCount: " + correctPutCount);
            System.out.println("totalGetTime: " + totalGetTime);
            System.out.println("correctGetCount: " + correctGetCount);
            System.out.println("totalUpdateTime: " + totalUpdateTime);
            System.out.println("correctUpdateCount: " + correctUpdateCount);
            System.out.println("totalDeleteTime: " + totalDeleteTime);
            System.out.println("correctDeleteCount: " + correctDeleteCount);

        } catch (Exception e) {
            System.out.println("Exception caught " + e.getMessage());
        }
    }

    private static String[][] createKeyValueUpdateMatrix(int numberOfIteration) {
        String[][] keyValueUpdate = new String[numberOfIteration][];
        for (int i = 0; i < numberOfIteration; i++) {
            String key = randomString();
            String value = randomString();
            String update = randomString();

            keyValueUpdate[i] = new String[]{key, value, update};
        }
        return keyValueUpdate;
    }

    // Taken from https://www.baeldung.com/java-random-string
    public static String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }
}
