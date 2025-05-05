package chat.network;

import chat.controller.MessageSender;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client class for the P2P Chat application.
 * This class handles the client-side communication with the server.
 * It implements MessageSender interface to send messages to the server.
 */
public class Client implements MessageSender {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Connects to the chat server with the specified parameters.
     *
     * @param host The hostname or IP address of the server
     * @param port The port number of the server
     * @param name The name of the client to be displayed in the chat
     * @throws IOException If an I/O error occurs when creating the socket or streams
     */
    public void connect(String host, int port, String name) throws IOException {
        socket = new Socket(host, port);
        System.out.println("Connected to server: " + host + ":" + port);

        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

        // Send the client's name to the server
        String serverPrompt = in.readLine();
        if (serverPrompt != null) {
            System.out.println(serverPrompt); // Display the prompt
            out.println(name); // Send the name to the server
        }

        // Start a thread to listen for messages from the server
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Connection to server lost.");
            }
        }).start();
    }

    /**
     * Sends a message to the server.
     *
     * @param message The message to be sent
     */
    @Override
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }

    }

    /**
     * Disconnects from the server by closing the socket.
     *
     * @throws IOException If an I/O error occurs when closing the socket
     */
    public void disconnect() throws IOException {
        if (socket != null) socket.close();
    }

    /**
     * Checks if the client is currently connected to the server.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }


}
