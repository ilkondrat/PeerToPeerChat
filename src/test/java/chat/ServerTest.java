package chat;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.*;

class ServerTest {
    private Server server;
    private Thread serverThread;

    @Before
    public void setUp() throws Exception {
        // Start the server in a separate thread
        server = new Server();
        serverThread = new Thread(() -> {
            try {
                server.start(12345);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // Give the server some time to start
        Thread.sleep(500);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        serverThread.join();
    }

    @Test
    public void testServerReceivesAndSendsMessages() throws Exception {
        // Simulate a client connecting to the server
        Socket clientSocket = new Socket("localhost", 12345);
        PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Send a message from the client to the server
        clientOut.println("Hello, Server!");

        // Verify that the server receives the message
        String receivedMessage = server.receiveMessage();
        assertEquals("Hello, Server!", receivedMessage);

        // Send a response from the server to the client
        server.sendMessage("Hello, Client!");

        // Verify that the client receives the response
        String response = clientIn.readLine();
        assertEquals("Hello, Client!", response);

        // Close client resources
        clientIn.close();
        clientOut.close();
        clientSocket.close();
    }
}