package chat.network;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Server class for the P2P Chat application.
 * This class handles the server-side operations, including accepting client connections
 * and broadcasting messages to connected clients.
 */
public class Server {
    @Getter
    @Setter
    private String welcomeMessage  = "Welcome to the P2P chat!";
    private ServerSocket serverSocket;
    /**
     * -- GETTER --
     *  Gets the list of connected clients.
     *
     * @return The list of connected clients
     */
    @Getter
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    /**
     * Starts the server on the specified port.
     * This method enters an infinite loop waiting for client connections.
     *
     * @param port The port number to start the server on
     * @throws IOException If an I/O error occurs when opening the server socket
     */

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port: " + port + ", Waiting for clients...");


        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getInetAddress());

            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     *
     * @param message The message to broadcast
     * @param sender The client who sent the message (will not receive the broadcast)
     */
    public void broadcastMessage(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) { // Don't send the message back to the sender
                    client.sendMessage(message);
                }
            }
        }
    }

    /**
     * Removes a client from the list of connected clients.
     *
     * @param clientHandler The client handler to remove
     *
     */
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public String getOnlineUserNames() {
        synchronized (clients) {
            return clients.stream()
                    .map(c -> c.clientName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("No users online");
        }
    }

    /**
     * Gets the port that the server is running on.
     *
     * @return The port number
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Stops the server by closing the server socket.
     *
     * @throws IOException If an I/O error occurs when closing the server socket
     */
    public void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
