package chat;

import chat.network.Client;
import chat.network.Server;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main class for the P2P Chat application.
 * Provides the entry point and user interface for starting a server or client.
 */
public class Main {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    /**
     * Prompts the user for a port number, validates it, and returns a valid port.
     *
     * @param scanner The Scanner instance for user input.
     * @param promptMessage The message to display to the user.
     * @return A valid port number (1-65535).
     */
    private static int getValidPort(Scanner scanner, String promptMessage) {
        int port = 0;
        while (true) {
            System.out.print(promptMessage + " (1-65535): ");
            String portInput = scanner.nextLine().trim();
            try {
                port = Integer.parseInt(portInput);
                if (port >= 1 && port <= 65535) {
                    break; // Valid port, exit loop
                } else {
                    System.err.println("Error: Port number must be between 1 and 65535.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid input. Port must be a number.");
            }
        }
        return port;
    }

    /**
     * Prompts the user for a non-empty hostname or IP address.
     *
     * @param scanner The Scanner instance for user input.
     * @param promptMessage The message to display to the user.
     * @return A non-empty, trimmed hostname or IP string.
     */
    private static String getValidHost(Scanner scanner, String promptMessage) {
        String host;
        while (true) {
            System.out.print(promptMessage + ": ");
            host = scanner.nextLine().trim();
            if (!host.isEmpty()) {
                break; // Valid host, exit loop
            } else {
                System.err.println("Error: Hostname/IP address cannot be empty.");
            }
        }
        return host;
    }

    /**
     * Main entry point of the application.
     * Manages the primary user interaction flow.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            mainMenuLoop:
            while (true) {
                System.out.println("\n--- Main Menu ---");
                System.out.println("Options: 'server', 'client', 'exit'");
                System.out.print("Enter your choice: ");
                String choice = scanner.nextLine().trim().toLowerCase();

                switch (choice) {
                    case "server":
                        runServer(scanner);
                        // Server mode chosen. The runServer method handles its lifecycle.
                        // After server stops or fails, control returns here to re-prompt.
                        break;
                    case "client":
                        runClient(scanner);
                        // Client mode chosen. After client operations, control returns here.
                        break;
                    case "exit":
                        System.out.println("Exiting application...");
                        break mainMenuLoop; // Exit the main application loop
                    default:
                        System.err.println("Error: Invalid choice. Please try again.");
                }
            }
        }
        System.out.println("Application terminated.");
    }

    /**
     * Configures and starts the chat server.
     *
     * @param scanner The Scanner instance for user input.
     */
    private static void runServer(Scanner scanner) {
        System.out.println("\n--- Server Setup ---");
        int port = getValidPort(scanner, "Enter port to start the server on");
        Server server = new Server(); // [3]
        try {
            server.start(port); // [3] This method will block until the server is stopped.
            System.out.println("Server has been stopped."); // Reached if server.start() was blocking and completed.
        } catch (IOException e) {
            System.err.println("Error: Failed to start the server: " + e.getMessage());
            // Returns to the main menu loop.
        }
    }

    /**
     * Manages client-side operations: setup, connection, authentication, and chat.
     *
     * @param scanner The Scanner instance for user input.
     */
    private static void runClient(Scanner scanner) {
        System.out.println("\n--- Client Setup ---");
        String host = getValidHost(scanner, "Enter server IP address or hostname");
        int port = getValidPort(scanner, "Enter server port");

        Client client = new Client(); // [1]
        String authenticatedUsername = null;

        clientActionLoop:
        while (true) {
            System.out.println("\n--- Client Actions ---");
            System.out.println("Options: (1) Login | (2) Register | (3) Back to Main Menu");
            System.out.print("Enter your choice: ");
            String actionChoice = scanner.nextLine().trim();

            try {
                switch (actionChoice) {
                    case "1": // LOGIN
                        if (!client.isConnected()) { // [1]
                            client.connect(host, port); // [1]
                        }
                        System.out.println("--- Login ---");
                        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
                            System.out.print("Username: ");
                            String username = scanner.nextLine().trim();
                            System.out.print("Password: ");
                            String password = scanner.nextLine();

                            if (client.login(username, password)) { // [1]
                                System.out.println("Login successful!");
                                authenticatedUsername = username;
                                startChatSession(scanner, client, authenticatedUsername);
                                break clientActionLoop; // Chat ended, exit client actions to main menu
                            } else {
                                System.err.println("Login failed. " + (attempt < MAX_LOGIN_ATTEMPTS ? "Please try again." : "Max attempts reached."));
                                if (attempt == MAX_LOGIN_ATTEMPTS && client.isConnected()) {
                                    client.disconnect(); // Disconnect after final failed attempt [1]
                                }
                            }
                        }
                        // If login attempts exhausted, loop continues to client actions menu
                        break;

                    case "2": // REGISTER
                        if (!client.isConnected()) { // [1]
                            client.connect(host, port); // [1]
                        }
                        System.out.println("--- Register ---");
                        System.out.print("Choose username: ");
                        String newUsername = scanner.nextLine().trim();
                        System.out.print("Choose password: ");
                        String newPassword = scanner.nextLine();

                        if (client.register(newUsername, newPassword)) { // [1]
                            System.out.println("Registration successful! Please login with your new account.");
                        } else {
                            // Server should provide specific error (e.g., username taken) via Client.java [1]
                            System.err.println("Registration failed. See server message for details.");
                        }
                        if (client.isConnected()) {
                            client.disconnect(); // Always disconnect after a registration attempt [1]
                        }
                        // Loop continues to client actions menu (e.g., to login)
                        break;

                    case "3": // BACK TO MAIN MENU
                        System.out.println("Returning to Main Menu...");
                        if (client.isConnected()) {
                            client.disconnect(); // [1]
                        }
                        break clientActionLoop; // Exit client actions loop

                    default:
                        System.err.println("Error: Invalid action. Please enter '1', '2', or '3'.");
                }
            } catch (IOException e) {
                System.err.println("Client Error: " + e.getMessage() + ". Please check server status and your connection.");
                if (client.isConnected()) { // [1]
                    try { client.disconnect(); } catch (IOException ex) { System.err.println("Error during client auto-disconnect: " + ex.getMessage()); } // [1]
                }
                // After an IO error, let user decide next action from client menu or go back
            }
        } // End clientActionLoop

        // Final cleanup if client is still connected when runClient scope is about to be left
        try {
            if (client.isConnected()) { // [1]
                System.out.println("Ensuring client is disconnected before returning to main menu.");
                client.disconnect(); // [1]
            }
        } catch (IOException e) {
            System.err.println("Error during final client disconnect: " + e.getMessage());
        }
    }

    /**
     * Manages an active chat session for an authenticated client.
     *
     * @param scanner The Scanner instance for user input.
     * @param client The connected and authenticated Client instance. [1]
     * @param username The username of the authenticated client.
     */
    private static void startChatSession(Scanner scanner, Client client, String username) {
        System.out.println("\n--- Chat Session (" + username + ") ---");
        System.out.println("Type //help for commands, //exit to disconnect.");

        try {
            while (client.isConnected()) { // [1]
                System.out.print(username + "> "); // Prompt indicating user
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("//exit") || message.equalsIgnoreCase("//disconnect")) {
                    client.sendMessage(message); // Inform server [1]
                    System.out.println("Disconnecting from chat...");
                    // The client.disconnect() will be handled by the finally block in runClient
                    // or by explicit disconnect in clientActionLoop after chat ends.
                    break; // Exit chat loop
                } else if (message.equalsIgnoreCase("//help")) {
                    displayHelp();
                } else if (message.equalsIgnoreCase("//time")) {
                    System.out.println("Current client time: " + java.time.LocalTime.now().withNano(0));
                } else if (message.equalsIgnoreCase("//date")) {
                    System.out.println("Current client date: " + java.time.LocalDate.now());
                } else {
                    client.sendMessage(message); // Send other messages/commands to server [1]
                }
            }
        } catch (Exception e) { // Catch unexpected errors during chat
            System.err.println("Critical error during chat session: " + e.getMessage());
            // Consider logging e.printStackTrace() for debugging
        } finally {
            System.out.println("Chat session for " + username + " ended.");
            // Actual disconnection is typically managed by the calling method (runClient)
            // or if the server closes the connection, client.isConnected() will become false.
        }
    }

    /**
     * Displays the help message with available chat commands.
     */
    private static void displayHelp() {
        System.out.println(
                "Available commands:\n" +
                "  //help         - Show this help message\n" +
                "  //time         - Show current client time\n" +
                "  //date         - Show current client date\n" +
                "  //online       - Request list of online users (sent to server)\n" +
                "  //welcome      - Request server's welcome message (sent to server)\n" +
                "  //setwelcome <msg> - (Admin) Set server welcome message (sent to server)\n" +
                "  //disconnect   - Disconnect from the server\n" +
                "  //exit         - Disconnect from the server"
        );
    }
}
