package chat.network;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port: " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getInetAddress());

            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) { // Don't send the message back to the sender
                    client.sendMessage(message);
                }
            }
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private static class ClientHandler implements Runnable {
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

                // Ask for the client's name
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
}
