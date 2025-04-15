package chat.network;

import chat.controller.MessageSender;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements MessageSender {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

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

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void disconnect() throws IOException {
        if (socket != null) socket.close();
    }
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the IP address of the server: ");
        String host = scanner.nextLine();

        System.out.print("Enter the port of the server: ");
        int port = scanner.nextInt();
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        Client client = new Client();
        client.connect(host, port, name);

        System.out.println("Connected to the server! You can start chatting now.");

        while (true) {
            String message = scanner.nextLine();
            if ("exit".equalsIgnoreCase(message)) { // Exit command
                client.disconnect();
                break;
            }
            client.sendMessage(message);
        }

        scanner.close();
    }
}
