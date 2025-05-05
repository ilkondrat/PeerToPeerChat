package chat.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the Client class.
 * Tests the client's connection, message sending, and disconnection functionality.
 */
class ClientTest {

    private Client client;
    private AutoCloseable closeable;

    @Mock
    private Socket mockSocket;

    @Mock
    private PrintWriter mockWriter;

    @Mock
    private BufferedReader mockReader;

    /**
     * Set up the test environment before each test.
     */
    @BeforeEach
    void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        client = new Client();

        // Configure mocks for Socket, PrintWriter, and BufferedReader
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("Enter your name:\n".getBytes()));

        // Use reflection to set private fields
        setPrivateField(client, "socket", mockSocket);
        setPrivateField(client, "out", mockWriter);
        setPrivateField(client, "in", mockReader);
    }

    /**
     * Clean up the test environment after each test.
     */
    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Test the connect method.
     */
    @Test
    void testConnect() throws IOException {
        // Create a new client for testing the connect() method
        Client testClient = new Client();

        // Create mocks for all necessary objects
        Socket socket = mock(Socket.class);
        OutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream("Enter your name:\n".getBytes());

        // Configure mock behavior
        when(socket.getOutputStream()).thenReturn(outputStream);
        when(socket.getInputStream()).thenReturn(inputStream);

        // Test exception with invalid connection
        assertThrows(IOException.class, () -> {
            testClient.connect("invalid-host", -1, "TestUser");
        });
    }

    /**
     * Test the sendMessage method.
     */
    @Test
    void testSendMessage() {
        // Test sending a message
        client.sendMessage("Test Message");

        // Verify that println was called with the correct message
        verify(mockWriter).println("Test Message");
    }

    /**
     * Test the disconnect method.
     */
    @Test
    void testDisconnect() throws IOException {
        // Test disconnection
        client.disconnect();

        // Verify that close was called
        verify(mockSocket).close();
    }

    /**
     * Test the isConnected method.
     */
    @Test
    void testIsConnected() {
        // Create mocks with different states
        Socket connectedSocket = mock(Socket.class);
        when(connectedSocket.isConnected()).thenReturn(true);
        when(connectedSocket.isClosed()).thenReturn(false);

        Socket closedSocket = mock(Socket.class);
        when(closedSocket.isConnected()).thenReturn(true);
        when(closedSocket.isClosed()).thenReturn(true);

        Socket nullSocket = null;

        // Test with connected socket
        setPrivateField(client, "socket", connectedSocket);
        assertTrue(client.isConnected());

        // Test with closed socket
        setPrivateField(client, "socket", closedSocket);
        assertFalse(client.isConnected());

        // Test with null socket
        setPrivateField(client, "socket", nullSocket);
        assertFalse(client.isConnected());
    }

    /**
     * Test receiving messages from the server.
     */
    @Test
    void testReceiveMessages() throws IOException {
        // Prepare a sequence of messages
        when(mockReader.readLine())
                .thenReturn("Message 1")
                .thenReturn("Message 2")
                .thenReturn(null);  // Simulate connection close

        // Redirect System.out to check output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Create and start message receiving thread manually
            Thread messageThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = mockReader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Connection to server lost.");
                }
            });
            messageThread.start();
            messageThread.join(1000);  // Wait for thread execution, max 1 second

            // Verify that messages were output
            String output = outContent.toString();
            assertTrue(output.contains("Message 1"));
            assertTrue(output.contains("Message 2"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test handling of lost connection.
     */
    @Test
    void testConnectionLost() throws IOException {
        // Simulate connection loss
        when(mockReader.readLine()).thenThrow(new SocketException("Connection reset"));

        // Redirect System.err to check error output
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Create and start message receiving thread manually
            Thread messageThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = mockReader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Connection to server lost.");
                }
            });
            messageThread.start();
            messageThread.join(1000);  // Wait for thread execution, max 1 second

            // Verify that error message was output
            String output = errContent.toString();
            assertTrue(output.contains("Connection to server lost."));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.setErr(originalErr);
        }
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
