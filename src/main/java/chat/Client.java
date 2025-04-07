package chat;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Connects to a server at the specified host and port.
     *
     * @param host The server's IP address or hostname.
     * @param port The server's port number.
     * @throws IOException If an I/O error occurs.
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("Connected to server: " + host + ":" + port);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Sends a message to the server.
     *
     * @param message The message to send.
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * Receives a message from the server.
     *
     * @return The received message.
     * @throws IOException If an I/O error occurs.
     */
    public String receiveMessage() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    /**
     * Disconnects from the server and closes all resources.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void disconnect() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
        System.out.println("Disconnected from server.");
    }
}

