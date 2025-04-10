package chat;

import chat.network.Client;
import org.junit.After;
import org.junit.Before;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class ClientTest {
    private ServerSocket testServer;
    private Thread testServerThread;
    private Client client;

    @Before
    public void setUp() throws Exception {
        // Start a test server in a separate thread
        testServer = new ServerSocket(12345);

        testServerThread = new Thread(() -> {
            try (Socket socket = testServer.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Echo messages back to the client
                String message;
                while ((message = in.readLine()) != null) {
                    out.println("Echo: " + message);
                }
            } catch (IOException e) {
                // TODO: @Handle
            }
        });

        testServerThread.start();

        // Create a client instance
        client = new Client();

        // Give the test server some time to start
        Thread.sleep(500);

    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.disconnect();
        }

        if (testServer != null) {
            testServer.close();
        }

        testServerThread.join();
    }
}