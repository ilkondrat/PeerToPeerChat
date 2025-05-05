package chat.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the Server class.
 * Tests the server's functionality for managing client connections and broadcasting messages.
 */
class ServerTest {

    private Server server;
    private AutoCloseable closeable;

    @Mock
    private ServerSocket mockServerSocket;

    @Mock
    private Socket mockClientSocket;

    @Mock
    private ClientHandler mockClientHandler1;

    @Mock
    private ClientHandler mockClientHandler2;

    /**
     * Set up the test environment before each test.
     */
    @BeforeEach
    void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        server = new Server();

        // Use reflection to set private fields
        setPrivateField(server, "serverSocket", mockServerSocket);

        // Configure ServerSocket behavior
        when(mockServerSocket.accept()).thenReturn(mockClientSocket);
        when(mockServerSocket.getLocalPort()).thenReturn(8080);
    }

    /**
     * Clean up the test environment after each test.
     */
    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Test the start method.
     */
    @Test
    void testStart() throws IllegalArgumentException {
        // Create a separate test server instance
        Server testServer = new Server();

        // Test exception with invalid port
        assertThrows(IllegalArgumentException.class, () -> {
            testServer.start(-1);  // Invalid port
        });
    }

    /**
     * Test the broadcastMessage method.
     */
    @Test
    void testBroadcastMessage() {
        // Add clients to the list
        List<ClientHandler> clients = new ArrayList<>();
        clients.add(mockClientHandler1);
        clients.add(mockClientHandler2);
        setPrivateField(server, "clients", clients);

        // Send a message
        server.broadcastMessage("Test Message", mockClientHandler1);

        // Verify that the message was sent to client 2 but not client 1 (the sender)
        verify(mockClientHandler2, times(1)).sendMessage("Test Message");
        verify(mockClientHandler1, never()).sendMessage("Test Message");
    }

    /**
     * Test the removeClient method.
     */
    @Test
    void testRemoveClient() {
        // Create a client list
        List<ClientHandler> clients = new ArrayList<>();
        clients.add(mockClientHandler1);
        clients.add(mockClientHandler2);
        setPrivateField(server, "clients", clients);

        // Remove a client
        server.removeClient(mockClientHandler1);

        // Verify that the client was removed
        List<ClientHandler> resultClients = server.getClients();
        assertEquals(1, resultClients.size());
        assertTrue(resultClients.contains(mockClientHandler2));
        assertFalse(resultClients.contains(mockClientHandler1));
    }

    /**
     * Test the getPort method.
     */
    @Test
    void testGetPort() {
        // Verify that the port is returned correctly
        assertEquals(8080, server.getPort());
    }

    /**
     * Test the stop method.
     */
    @Test
    void testStop() throws IOException {
        // Test server shutdown
        server.stop();

        // Verify that close was called
        verify(mockServerSocket).close();
    }

    /**
     * Helper method to set private fields using reflection.
     */
    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            fail("Failed to set private field: " + e.getMessage());
        }
    }
}
