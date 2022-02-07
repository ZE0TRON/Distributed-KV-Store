package de.tum.i13.client;

import de.tum.i13.shared.ClientConfig;
import de.tum.i13.shared.ConnectionManager.ClientNotificationHandlerThread;
import de.tum.i13.shared.Util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
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

        scenario1(kvStore, 1000);
        System.out.println("--------------------------");
        scenario2(kvStore, 1000);
        System.out.println("--------------------------");
        scenario1(kvStore, 20000);
        System.out.println("--------------------------");
        scenario2(kvStore, 20000);
    }

    public static void shutdownProcedure() throws IOException, InterruptedException {
        Thread.sleep(750);
        ClientNotificationHandlerThread.kill();
    }

    //together
    public static void scenario1(KVStoreClientLibrary kvStore, int numberOfIteration) {
        String[] numOfIters = new String[(numberOfIteration / 100) + 1];
        String[] totalPutTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctPutCounts = new String[(numberOfIteration / 100) + 1];
        String[] totalGetTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctGetCounts = new String[(numberOfIteration / 100) + 1];
        String[] totalUpdateTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctUpdateCounts = new String[(numberOfIteration / 100) + 1];
        String[] totalDeleteTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctDeleteCounts = new String[(numberOfIteration / 100) + 1];

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

                if(i % 100 == 0){
                    numOfIters [i/100] = "" + i;
                    totalPutTimes [i/100] = "" + totalPutTime;
                    correctPutCounts [i/100] = "" + correctPutCount;
                    totalGetTimes [i/100] = "" + totalGetTime;
                    correctGetCounts [i/100] = "" + correctGetCount;
                    totalUpdateTimes [i/100] = "" + totalUpdateTime;
                    correctUpdateCounts [i/100] = "" + correctUpdateCount;
                    totalDeleteTimes [i/100] = "" + totalDeleteTime;
                    correctDeleteCounts [i/100] = "" + correctDeleteCount;
                }
                // if doesn't work
                //Thread.sleep(1);
            }

            numOfIters [numberOfIteration/100] = "" + numberOfIteration;
            totalPutTimes [numberOfIteration/100] = "" + totalPutTime;
            correctPutCounts [numberOfIteration/100] = "" + correctPutCount;
            totalGetTimes [numberOfIteration/100] = "" + totalGetTime;
            correctGetCounts [numberOfIteration/100] = "" + correctGetCount;
            totalUpdateTimes [numberOfIteration/100] = "" + totalUpdateTime;
            correctUpdateCounts [numberOfIteration/100] = "" + correctUpdateCount;
            totalDeleteTimes [numberOfIteration/100] = "" + totalDeleteTime;
            correctDeleteCounts [numberOfIteration/100] = "" + correctDeleteCount;


                System.out.println("Scenario 1 (one loop), numberOfIterations: " + numberOfIteration);
                System.out.println("numberOfIteration: " + Arrays.toString(numOfIters));
                System.out.println("totalPutTime: " + Arrays.toString(totalPutTimes));
                System.out.println("correctPutCount: " + Arrays.toString(correctPutCounts));
                System.out.println("totalGetTime: " + Arrays.toString(totalGetTimes));
                System.out.println("correctGetCount: " + Arrays.toString(correctGetCounts));
                System.out.println("totalUpdateTime: " + Arrays.toString(totalUpdateTimes));
                System.out.println("correctUpdateCount: " + Arrays.toString(correctUpdateCounts));
                System.out.println("totalDeleteTime: " + Arrays.toString(totalDeleteTimes));
                System.out.println("correctDeleteCount: " + Arrays.toString(correctDeleteCounts));

        } catch (Exception e) {
            System.out.println("Exception caught " + e.getMessage());
            System.out.println(e.toString());
        }
    }


    //separate
    public static void scenario2(KVStoreClientLibrary kvStore, int numberOfIteration) {
        String[] numOfIters = new String[(numberOfIteration / 100) + 1];
        String[] totalPutTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctPutCounts = new String[(numberOfIteration / 100) + 1];
        String[] totalGetTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctGetCounts = new String[(numberOfIteration / 100) + 1];
        String[] totalUpdateTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctUpdateCounts = new String[(numberOfIteration / 100) + 1];
        String[] totalDeleteTimes = new String[(numberOfIteration / 100) + 1];
        String[] correctDeleteCounts = new String[(numberOfIteration / 100) + 1];
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

                if(i % 100 == 0){
                    numOfIters [i/100] = "" + i;
                    totalPutTimes [i/100] = "" + totalPutTime;
                    correctPutCounts [i/100] = "" + correctPutCount;
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
                if(i % 100 == 0){
                    totalGetTimes [i/100] = "" + totalGetTime;
                    correctGetCounts [i/100] = "" + correctGetCount;
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

                if(i % 100 == 0){
                    totalUpdateTimes [i/100] = "" + totalUpdateTime;
                    correctUpdateCounts [i/100] = "" + correctUpdateCount;
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

                if(i % 100 == 0){
                    totalDeleteTimes [i/100] = "" + totalDeleteTime;
                    correctDeleteCounts [i/100] = "" + correctDeleteCount;
                }

                // if doesn't work
                //Thread.sleep(1);
            }

            numOfIters [numberOfIteration/100] = "" + numberOfIteration;
            totalPutTimes [numberOfIteration/100] = "" + totalPutTime;
            correctPutCounts [numberOfIteration/100] = "" + correctPutCount;
            totalGetTimes [numberOfIteration/100] = "" + totalGetTime;
            correctGetCounts [numberOfIteration/100] = "" + correctGetCount;
            totalUpdateTimes [numberOfIteration/100] = "" + totalUpdateTime;
            correctUpdateCounts [numberOfIteration/100] = "" + correctUpdateCount;
            totalDeleteTimes [numberOfIteration/100] = "" + totalDeleteTime;
            correctDeleteCounts [numberOfIteration/100] = "" + correctDeleteCount;

            System.out.println("Scenario 2 (separate loops), numberOfIterations: " + numberOfIteration);
            System.out.println("numberOfIteration: " + Arrays.toString(numOfIters));
            System.out.println("totalPutTime: " + Arrays.toString(totalPutTimes));
            System.out.println("correctPutCount: " + Arrays.toString(correctPutCounts));
            System.out.println("totalGetTime: " + Arrays.toString(totalGetTimes));
            System.out.println("correctGetCount: " + Arrays.toString(correctGetCounts));
            System.out.println("totalUpdateTime: " + Arrays.toString(totalUpdateTimes));
            System.out.println("correctUpdateCount: " + Arrays.toString(correctUpdateCounts));
            System.out.println("totalDeleteTime: " + Arrays.toString(totalDeleteTimes));
            System.out.println("correctDeleteCount: " + Arrays.toString(correctDeleteCounts));

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
