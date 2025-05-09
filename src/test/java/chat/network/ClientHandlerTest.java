package chat.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the ClientHandler class.
 * Tests the client handler's functionality for processing client messages and managing connections.
 */
class ClientHandlerTest {

    private ClientHandler clientHandler;
    private AutoCloseable closeable;

    @Mock
    private Socket mockSocket;

    @Mock
    private Server mockServer;

    @Mock
    private PrintWriter mockWriter;

    @Mock
    private BufferedReader mockReader;

    private ByteArrayOutputStream outputStream;
    private ByteArrayInputStream inputStream;

    /**
     * Set up the test environment before each test.
     */
    @BeforeEach
    void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);

        // Create real streams for testing
        outputStream = new ByteArrayOutputStream();
        inputStream = new ByteArrayInputStream("TestUser\nHello World\n".getBytes());

        // Configure mock socket to return our test streams
        when(mockSocket.getInputStream()).thenReturn(inputStream);
        when(mockSocket.getOutputStream()).thenReturn(outputStream);

        // Create client handler with our mocked socket and server
        //clientHandler = new ClientHandler(mockSocket, mockServer);

        // We won't set the private fields directly, instead we'll let the run method
        // initialize them naturally using our mocked streams
    }

    /**
     * Clean up the test environment after each test.
     */
    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Test the sendMessage method.
     */
    @Test
    void testSendMessage() throws IOException {
        // First we need to initialize the PrintWriter by calling run partially
        // or by setting it directly using reflection
        setPrivateField(clientHandler, "out", mockWriter);

        // Test sending a message
        clientHandler.sendMessage("Test Message");

        // Verify that the message was sent
        verify(mockWriter).println("Test Message");
    }

    /**
     * Test a modified version of the run method that we can control better.
     */
    @Test
    void testRunWithMockedIO() throws IOException {
        // Set up the private fields directly
        setPrivateField(clientHandler, "in", mockReader);
        setPrivateField(clientHandler, "out", mockWriter);

        // Configure mock reader behavior
        when(mockReader.readLine())
                .thenReturn("TestUser")    // First call returns username
                .thenReturn("Hello World") // Second call returns a message
                .thenReturn(null);         // Third call simulates disconnection

        // Run the method
        clientHandler.run();

        // Verify expected interactions
        verify(mockWriter).println("Enter your name:");
        verify(mockServer).broadcastMessage("TestUser has joined the chat!", clientHandler);
        verify(mockServer).broadcastMessage("[TestUser]: Hello World", clientHandler);
        verify(mockServer).removeClient(clientHandler);
        verify(mockSocket).close();
    }

    /**
     * Test handling of IOException.
     */
    @Test
    void testHandleIOException() throws IOException {
        // Set up the private fields directly
        setPrivateField(clientHandler, "in", mockReader);
        setPrivateField(clientHandler, "out", mockWriter);
        setPrivateField(clientHandler, "clientName", "TestUser");

        // Configure mock to throw exception
        when(mockReader.readLine()).thenThrow(new IOException("Connection reset"));

        // Run the method
        clientHandler.run();

        // Verify that client was removed and socket was closed
        verify(mockServer).removeClient(clientHandler);
        verify(mockSocket).close();
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
