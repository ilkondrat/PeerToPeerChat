package chat;

import chat.network.Client;
import chat.network.Server;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main class for the P2P Chat application.
 * This class provides the entry point for the application, allowing users
 * to start either a server or a client.
 */
public class Main {

    /**
     * Helper method to get a valid port number from the user.
     * It prompts the user, reads input, and validates the port number.
     *
     * @param scanner The Scanner object to read user input.
     * @param promptMessage The message to display to the user when asking for the port.
     * @return A valid port number (1-65535).
     */
    private static int getValidPort(Scanner scanner, String promptMessage) {
        int port = 0;
        boolean validInput = false;
        while (!validInput) {
            System.out.print(promptMessage + " (1-65535): ");
            String portInput = scanner.nextLine();
            try {
                port = Integer.parseInt(portInput);
                if (port >= 1 && port <= 65535) {
                    validInput = true;
                } else {
                    System.err.println("Invalid port number. Please enter a value between 1 and 65535.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Port must be a number.");
            }
        }
        return port;
    }

    /**
     * The main method that serves as the entry point for the application.
     * It prompts the user to choose between starting a server or connecting as a client.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Type 'server' to start a server or 'client' to connect as a client:");
            String choice = scanner.nextLine();

            if ("server".equalsIgnoreCase(choice)) {
                // Start the server
                int port = getValidPort(scanner, "Enter the port to start the server");

                Server server = new Server();
                try {
                    server.start(port);
                    // Server runs indefinitely, clients are handled in separate threads
                } catch (IOException e) {
                    System.err.println("Error starting the server: " + e.getMessage());
                }

            } else if ("client".equalsIgnoreCase(choice)) {
                // Client setup
                String host = "";
                boolean validHost = false;

                // Input validation for host
                while (!validHost) {
                    System.out.print("Enter the IP address or hostname of the server: ");
                    host = scanner.nextLine().trim(); // Trim whitespace
                    if (!host.isEmpty()) {
                        validHost = true;
                    } else {
                        System.err.println("Hostname/IP address cannot be empty. Please enter a valid value.");
                    }
                }

                // Get valid port for the client
                int port = getValidPort(scanner, "Enter the port of the server");

                Client client = new Client();
                boolean proceedToChat = false;
                String authenticatedUsername = null;

                System.out.println("Choose action: (1) Login | (2) Register");
                String actionChoice = scanner.nextLine();

                try {
                    client.connect(host, port); // Establish basic connection

                    if ("1".equals(actionChoice)) { // LOGIN
                        System.out.print("Enter your username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter your password: ");
                        String password = scanner.nextLine();
                        if (client.login(username, password)) { // Assuming Client.java has login method [1]
                            System.out.println("Login successful!");
                            proceedToChat = true;
                            authenticatedUsername = username;
                        } else {
                            System.err.println("Login failed. Please check credentials or server message.");
                        }
                    } else if ("2".equals(actionChoice)) { // REGISTER
                        System.out.print("Choose a username for registration: ");
                        String newUsername = scanner.nextLine();
                        System.out.print("Choose a password for registration: ");
                        String newPassword = scanner.nextLine();
                        if (client.register(newUsername, newPassword)) { // Assuming Client.java has register method [1]
                            System.out.println("Registration successful! Please run the client again to login.");
                        } else {
                            System.err.println("Registration failed. Username might be taken or server error.");
                        }
                    } else {
                        System.out.println("Invalid action choice.");
                    }

                    if (proceedToChat && authenticatedUsername != null) {
                        System.out.println("You are now chatting as " + authenticatedUsername + ".");
                        while (client.isConnected()) { // Assuming Client.java has isConnected method [1]
                            System.out.print("You (" + authenticatedUsername + "): ");
                            String message = scanner.nextLine();
                            if (message.equalsIgnoreCase("//exit") || message.equalsIgnoreCase("//disconnect")) {
                                client.sendMessage(message); // Assuming Client.java has sendMessage method [1]
                                System.out.println("Disconnecting...");
                                break;
                            } else if (message.equalsIgnoreCase("//help")) {
                                System.out.println(
                                        "Available commands:\n" +
                                                "//help         - Show this help message\n" +
                                                "//time         - Show the current time\n" +
                                                "//date         - Show the current date\n" +
                                                "//online       - Show the list of users currently online\n" +
                                                "//welcome      - Show the current welcome message\n" +
                                                "//setwelcome <message> - Set the welcome message (server admin)\n" +
                                                "//disconnect   - Disconnect from the server\n" +
                                                "//exit         - Disconnect from the server");
                            } else if (message.equalsIgnoreCase("//time")) {
                                System.out.println("Current time: " + java.time.LocalTime.now());
                            } else if (message.equalsIgnoreCase("//date")) {
                                System.out.println("Current date: " + java.time.LocalDate.now());
                            } else if (message.startsWith("//setwelcome ")) {
                                client.sendMessage(message);
                            } else if (message.equalsIgnoreCase("//welcome")) {
                                client.sendMessage(message);
                            } else if (message.equalsIgnoreCase("//online")) {
                                client.sendMessage(message);
                            } else {
                                client.sendMessage(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error during client operation (connection, login, or registration): " + e.getMessage());
                } finally {
                    try {
                        if (client.isConnected()) { // Assuming Client.java has isConnected method [1]
                            client.disconnect(); // Assuming Client.java has disconnect method [1]
                            System.out.println("Disconnected from server.");
                        }
                    } catch (IOException e) {
                        System.err.println("Error disconnecting from the server: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Invalid input. Please type 'server' or 'client'.");
            }
        } // Scanner is automatically closed here
    }
}
