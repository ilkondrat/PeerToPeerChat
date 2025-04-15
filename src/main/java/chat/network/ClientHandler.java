package chat.network;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Запрашиваем имя клиента
            out.println("Enter your name:");
            clientName = in.readLine();
            System.out.println(clientName + " has joined the chat!");
            server.broadcastMessage(clientName + " has joined the chat!", this);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("[" + clientName + "]: " + message);
                server.broadcastMessage("[" + clientName + "]: " + message, this);
            }
        } catch (IOException e) {
            System.err.println("Connection with " + clientName + " lost.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            server.removeClient(this);
            System.out.println(clientName + " has left the chat.");
            server.broadcastMessage(clientName + " has left the chat.", this);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
