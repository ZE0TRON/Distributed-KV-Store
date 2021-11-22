package de.tum.i13.kv;

import de.tum.i13.server.Main;
import de.tum.i13.shared.Constants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class KVStoreTest {

    public static Integer port = 5153;

    private static Thread serverThread;
    private static Socket socket;
    private static PrintWriter output;
    private static BufferedReader input;

    public String doRequest(String req) throws IOException {
        output.write(req + "\r\n");
        output.flush();
        return input.readLine();
    }

    @BeforeAll
    public static void init() throws InterruptedException, IOException {
        String originalDB = "test-data/KVStore-test.jaxb";
        Path originalPath = Paths.get(originalDB);
        String copyDB = "test-data/KVStore.jaxb";
        Path copied = Paths.get(copyDB);
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

        serverThread = new Thread(() -> {
            try {
                Main.main(new String[]{"-p", String.valueOf(port), "-d", "test-data", "-l", "echo.log", "-c", "100", "-s", "FIFO", "-ll", "FINE"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start(); // started the server
        Thread.sleep(2000);
        socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", port));
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Constants.TELNET_ENCODING));
        input = new BufferedReader(new InputStreamReader(socket.getInputStream(),Constants.TELNET_ENCODING));
        // Read welcome message
        input.readLine();

    }

    @AfterAll
    public static void teardown() throws IOException {
        serverThread.interrupt();
        socket.close();
    }

    @Test
    public void putGetTest() throws IOException {
        HashMap<String,String> kvPairs = new HashMap<>();
        for(int i =0; i < 100; i++) {
            String response = doRequest("put " + "aa" + i + " bb" + i );
            assert (response.equals("put_success aa" + i + " bb" + i));
        }
        for(int i =0; i < 100; i++) {
            String response = doRequest("get " + "aa" + i);
            assert (response.equals("get_success aa" + i + " bb" + i));
        }
    }

    @Test
    public void putUpdateTest() throws IOException {
        String response = doRequest("put " + "update_me a");
        assert (response.equals("put_success update_me a"));
        response = doRequest("put " + "update_me b");
        assert (response.equals("put_update update_me b"));
        response = doRequest("get " + "update_me");
        assert (response.equals("get_success update_me b"));
    }

    @Test
    public void getNonExistingTest() throws IOException {
        String response = doRequest("get " + "cant_get_me");
        assert (response.equals("get_error cant_get_me "));
    }


    @Test
    public void deleteTest() throws IOException {
        String response = doRequest("put" + " abc");
        assert (response.equals("delete_success abc null"));
        response = doRequest("put" + " hee");
        assert (response.equals("delete_error hee null"));
    }
}
