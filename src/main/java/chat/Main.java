package chat;
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
                System.out.println("Server is running. Waiting for a client...");

                // Keep listening for messages from the client
                while (true) {
                    String message = server.receiveMessage();
                    if (message == null || "exit".equalsIgnoreCase(message)) {
                        System.out.println("Client disconnected.");
                        break;
                    }
                    System.out.println("Client: " + message);

                    // Send a response back to the client
                    System.out.print("You (Server): ");
                    String response = scanner.nextLine();
                    server.sendMessage(response);
                }
            } catch (IOException e) {
                System.err.println("Error starting the server: " + e.getMessage());
            } finally {
                try {
                    server.stop();
                } catch (IOException e) {
                    System.err.println("Error stopping the server: " + e.getMessage());
                }
            }
        } else if ("client".equalsIgnoreCase(choice)) {
            // Start the client
            System.out.print("Enter the IP address of the server: ");
            String host = scanner.nextLine();
            System.out.print("Enter the port of the server: ");
            int port = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            Client client = new Client();
            try {
                client.connect(host, port);
                System.out.println("Connected to the server!");

                // Keep sending messages to and receiving messages from the server
                while (true) {
                    // Send a message to the server
                    System.out.print("You (Client): ");
                    String message = scanner.nextLine();
                    client.sendMessage(message);

                    if ("exit".equalsIgnoreCase(message)) {
                        System.out.println("Disconnected from server.");
                        break;
                    }

                    // Receive a response from the server
                    String response = client.receiveMessage();
                    if (response == null) {
                        System.out.println("Server disconnected.");
                        break;
                    }
                    System.out.println("Server: " + response);
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
