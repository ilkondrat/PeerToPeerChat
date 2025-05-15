package chat.network;

import chat.controller.MessageSender;
import chat.util.NotificationSound;
import chat.util.SoundType;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client implements MessageSender {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Establishes a basic socket connection to the server.
     *
     * @param host The server hostname or IP.
     * @param port The server port.
     * @throws IOException If connection fails.
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        System.out.println("Connection established with server: " + host + ":" + port);
    }

    /**
     * Attempts to log in to the server.
     *
     * @param username The username.
     * @param password The password.
     * @return true if login is successful, false otherwise.
     * @throws IOException If communication error occurs.
     */
    public boolean login(String username, String password) throws IOException {
        if (!isConnected() || out == null || in == null) {
            throw new IOException("Client not connected. Call connect() first.");
        }
        out.println("LOGIN " + username + " " + password);
        String serverResponse = in.readLine();
        if (serverResponse != null && serverResponse.startsWith("AUTH_SUCCESS:")) {
            System.out.println("Server: " + serverResponse.substring("AUTH_SUCCESS:".length()));
            NotificationSound connectionSound = new NotificationSound();
            connectionSound.playSound(SoundType.CLIENT_CONNECTED);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            startServerListenerThread(); // Start listening for chat messages
            return true;
        } else {
            System.err.println("Server: " + (serverResponse != null ? serverResponse : "No response or login failed."));
            return false;
        }
    }

    /**
     * Attempts to register a new account on the server.
     *
     * @param username The desired username.
     * @param password The desired password.
     * @return true if registration is successful, false otherwise.
     * @throws IOException If communication error occurs.
     */
    public boolean register(String username, String password) throws IOException {
        if (!isConnected() || out == null || in == null) {
            throw new IOException("Client not connected. Call connect() first.");
        }
        out.println("REGISTER " + username + " " + password);
        String serverResponse = in.readLine();
        if (serverResponse != null && serverResponse.startsWith("REGISTER_SUCCESS:")) {
            return true;
        } else {
            System.err.println("Server: " + (serverResponse != null ? serverResponse : "No response or registration failed."));
            return false;
        }
    }

    private void startServerListenerThread() {
        new Thread(() -> {
            try {
                String messageFromServer;
                while (isConnected() && (messageFromServer = in.readLine()) != null) {
                    System.out.println(messageFromServer); // Display messages from server/other clients
                }
            } catch (IOException e) {
                if (isConnected()) { // Avoid error message if disconnect was intentional
                    System.err.println("Connection to server lost.");
                }
            }
        }).start();
    }

    @Override
    public void sendMessage(String message) {
        if (out != null && isConnected()) {
            out.println(message);
//            NotificationSound newMessageSound = new NotificationSound();
//            newMessageSound.playSound(SoundType.NEW_MESSAGE);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            NotificationSound disconnectionSound = new NotificationSound();
            disconnectionSound.playSound(SoundType.CLIENT_DISCONNECTED);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            socket.close(); // This closes associated in/out streams too
        }
        // Nullify to help GC and ensure isConnected() is accurate
        out = null;
        in = null;
        socket = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

}
