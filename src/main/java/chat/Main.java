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
     * The main method that serves as the entry point for the application.
     * It prompts the user to choose between starting a server or connecting as a client.
     *
     * @param args Command line arguments (not used)
     */


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type 'server' to start a server or 'client' to connect as a client:");

        String choice = scanner.nextLine();

        if ("server".equalsIgnoreCase(choice)) {
            // Start the server
            System.out.print("Enter the port to start the server: ");
            int port = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            Server server = new Server();
            try {
                server.start(port);
                // Server runs indefinitely, clients are handled in separate threads
            } catch (IOException e) {
                System.err.println("Error starting the server: " + e.getMessage());
            }

        } else if ("client".equalsIgnoreCase(choice)) {
            // Start the client
            System.out.print("Enter the IP address of the server: ");
            String host = scanner.nextLine();

            System.out.print("Enter the port of the server: ");
            int port = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            Client client = new Client();
            try {
                client.connect(host, port, name);
                System.out.println("Connected to the server! You can start chatting now.");

                while (true) {
                    // Send a message to the server
                    System.out.print("You (" + name + "): ");
                    String message = scanner.nextLine();
                    client.sendMessage(message);

                    if (message.equalsIgnoreCase("//exit")
                        || message.equalsIgnoreCase("//disconnect")) {
                        System.err.println("Disconnected from server.");
                        break;
                    } else if (message.equalsIgnoreCase("//help")) {
                        System.out.println(
                                "Available commands:\n" +
                                "//help           - Show this help message\n" +
                                "//time           - Show the current time\n" +
                                "//date           - Show the current date\n" +
                                "//online         - Show the list of users currently online\n" +
                                "//welcome        - Show the current welcome message\n" +
                                "//setwelcome     - Set the welcome message(//setwelcome \"message you want to set\")\n" +
                                "//disconnect     - Disconnect from the server\n" +
                                "//exit           - Disconnect from the server \n");
                    } else if (message.equalsIgnoreCase("//time")) {
                        System.out.println("Current time: " + java.time.LocalTime.now());
                    } else if (message.equalsIgnoreCase("//date")) {
                        System.out.println("Current date: " + java.time.LocalDate.now());
                    } else if (message.startsWith("//setwelcome")) {
                        client.sendMessage(message); // sending message to Server
                    } else if (message.equalsIgnoreCase("//welcome")) {
                        client.sendMessage(message); // sending message to Server
                    } else if (message.equalsIgnoreCase("//online")) {
                        client.sendMessage(message); // sending message to Server
                    } else {
                        client.sendMessage(message); // ordinary message
                    }
                }


            } catch (IOException e) {
                System.err.println("Error connecting to the server: " + e.getMessage());
            } finally {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    System.err.println("Error disconnecting from the server: " + e.getMessage());
                }
            }
        } else {
            System.out.println("Invalid input. Please type 'server' or 'client'.");
        }

        scanner.close();
    }
}
