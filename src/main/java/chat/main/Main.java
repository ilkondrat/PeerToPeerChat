package chat.main;

import chat.network.Client;
import chat.network.Server;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to P2P Chat!");
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
                System.out.println("Server is running. Waiting for clients...");
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

                    if ("exit".equalsIgnoreCase(message)) {
                        System.out.println("Disconnected from server.");
                        break;
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
