package chat.main;

import chat.Main;
import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(System.in);
    }

    @Test
    void testServerModePrintsPrompt() {
        // Simulate user input for server mode with valid port
        String input = "server\n8081\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // Run main in a separate thread and interrupt after a short delay to avoid infinite loop
        Thread mainThread = new Thread(() -> Main.main(new String[]{}));
        mainThread.start();
        try {
            Thread.sleep(200); // Let main print prompts and start the server
            mainThread.interrupt(); // Interrupt to avoid infinite loop
        } catch (InterruptedException ignored) {}
        // Check output
        String output = outContent.toString();
        assertTrue(output.contains("Enter the port to start the server"));
        assertTrue(output.contains("Server started on port: 8081"));
    }

    @Test
    void testClientModePrintsPrompts() {
        // Simulate user input for client mode (exit immediately)
        String input = "client\nlocalhost\n8081\nTestUser\n//exit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // Run main in a separate thread and interrupt after a short delay
        Thread mainThread = new Thread(() -> Main.main(new String[]{}));
        mainThread.start();
        try {
            Thread.sleep(500); // Give time for prompts and connection
            mainThread.interrupt();
        } catch (InterruptedException ignored) {}
        String output = outContent.toString();
        assertTrue(output.contains("Enter the IP address of the server"));
        assertTrue(output.contains("Enter the port of the server"));
        assertTrue(output.contains("Enter your name"));
    }

    @Test
    void testInvalidInput() {
        String input = "invalid\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});
        String output = outContent.toString();
        assertTrue(output.contains("Invalid input. Please type 'server' or 'client'"));
    }

    @Test
    void testServerStartWithInvalidPortThrows() {
        String input = "server\n-1\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{}));
        assertTrue(exception.getMessage().contains("Port value out of range"));
    }
}
