package chat.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientTest {
    @Test
    public void testClientConnectsToServer() throws IOException {

        // Creating fake server "mock"
        ServerSocket mockServer = new ServerSocket(0);
        int port = mockServer.getLocalPort();

        // Thread for connection

        Thread serveerThread = new Thread(() -> {
            // reading the client's name

            try {
                Socket socket = mockServer.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                writer.println("Enter your name: ");
                String clientName = reader.readLine();
                assertEquals("TestUser", clientName);
                socket.close();
            } catch (IOException e) {
                fail("Test server error: " + e.getMessage());
            }

        });
        serveerThread.start();
        // CLient creation and connection
        Client client = new Client();
        client.connect("localhost", port, "TestUser");

        //check
        assertTrue(client.isConnected());
        //resources clean

        client.disconnect();
        mockServer.close();
    }
}
