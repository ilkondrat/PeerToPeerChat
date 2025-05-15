package chat.network;

import chat.auth.AccountManager; // Ensure this import is correct for your project structure
import lombok.Getter;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private PrintWriter out;
    // Getter for clientName, used by Server.getOnlineUserNames()
    @Getter
    String clientName; // Will be set after successful login

    private final AccountManager accountManager;
    private boolean isAuthenticated = false;
    boolean clientWantsToExit = false;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.accountManager = new AccountManager(); // Or get instance from Server
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

             clientWantsToExit = false;

            // Loop to handle initial authentication (login/register) attempts
            // This loop allows the client to retry if, for example, registration fails due to username taken.
            while (!isAuthenticated && !socket.isClosed() && !clientWantsToExit) {
                String clientRequest = in.readLine(); // Expect "LOGIN user pass" or "REGISTER user pass" or "EXIT"
                if (clientRequest == null) { // Client disconnected
                    clientWantsToExit = true;
                    break;
                }

                //System.out.println("Server received from client: " + clientRequest); // Debug log

                String[] parts = clientRequest.split(" ", 3);
                String command = parts[0].toUpperCase(); // Make command case-insensitive

                switch (command) {
                    case "REGISTER":
                        if (parts.length == 3) {
                            String usernameToRegister = parts[1];
                            String passwordToRegister = parts[2];

                            if (accountManager.accountExists(usernameToRegister)) {
                                out.println("REGISTER_FAILED_USERNAME_TAKEN:Username '" + usernameToRegister + "' is already in use. Please choose another.");
                                System.out.println("ClientHandler: Registration attempt failed for " + usernameToRegister + " - username taken.");
                                // Connection stays open, client can try again or send another command
                            } else {
                                if (accountManager.addAccount(usernameToRegister, passwordToRegister)) {
                                    out.println("REGISTER_SUCCESS:Account for '" + usernameToRegister + "' created successfully. Please login.");
                                    System.out.println("ClientHandler: New account registered: " + usernameToRegister);
                                    // Connection stays open, client should now attempt to log in.
                                } else {
                                    out.println("REGISTER_FAILED_SERVER_ERROR:Could not register account due to a server-side issue.");
                                    System.err.println("ClientHandler: Registration failed for " + usernameToRegister + " due to server error (addAccount returned false).");
                                    // For server errors, we might still close the connection or let them retry.
                                    // For now, let's keep it open for another attempt.
                                }
                            }
                        } else {
                            out.println("ERROR:Invalid REGISTER command format. Expected: REGISTER <username> <password>");
                        }
                        break;

                    case "LOGIN":
                        if (parts.length == 3) {
                            String usernameToLogin = parts[1];
                            String passwordToLogin = parts[2];
                            if (accountManager.validateCredentials(usernameToLogin, passwordToLogin)) {
                                this.clientName = usernameToLogin;
                                out.println("AUTH_SUCCESS:Welcome, " + this.clientName + "!");
                                System.out.println("ClientHandler: " + this.clientName + " has logged in.");
                                if (server != null) {
                                    server.addClient(this); // Add client to server's list *after* successful auth
                                    server.broadcastMessage(this.clientName + " has joined the chat!", this);
                                }
                                isAuthenticated = true; // Exit the authentication loop
                            } else {
                                out.println("AUTH_FAILED:Invalid username or password.");
                                System.out.println("ClientHandler: Login failed for " + usernameToLogin);
                                // Connection stays open, client can try again
                            }
                        } else {
                            out.println("ERROR:Invalid LOGIN command format. Expected: LOGIN <username> <password>");
                        }
                        break;

                    case "EXIT_AUTH": // A command client can send if they want to give up during auth phase
                        out.println("INFO:Disconnecting as per your request during authentication.");
                        clientWantsToExit = true;
                        break;

                    default:
                        out.println("ERROR:Invalid initial command. Expected REGISTER, LOGIN, or EXIT_AUTH.");
                        break;
                }
            } // End of authentication while loop

            if (!isAuthenticated) {
                // If loop exited without authentication (e.g., client sent EXIT_AUTH or disconnected)
                System.out.println("ClientHandler: Client did not authenticate or chose to exit. Closing connection.");
                if (!socket.isClosed()) socket.close();
                return; // End this client handler thread
            }

            // If authenticated, proceed to the main chat message loop
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("//exit") || message.equalsIgnoreCase("//disconnect")) {
                    break;
                }
                // Handle other chat commands (//online, //welcome, etc.)
                // Based on your ClientHandler.java [3] from previous context
                if (message.equalsIgnoreCase("//online")) {
                    assert server != null;
                    out.println("Online users: " + server.getOnlineUserNames());
                } else if (message.equalsIgnoreCase("//welcome")) {
                    assert server != null;
                    out.println(server.getWelcomeMessage());
                } else if (message.startsWith("//setwelcome ")) {
                    String newWelcome = message.substring("//setwelcome ".length());
                    assert server != null;
                    server.setWelcomeMessage(newWelcome);
                    out.println("Welcome message updated.");
                } else {
                    // Default: broadcast chat message
                    String formattedMessage = "[" + clientName + "]: " + message;
                    System.out.println(formattedMessage); // Log on server
                    assert server != null;
                    server.broadcastMessage(formattedMessage, this);
                }
            }
        } catch (IOException e) {
            String logName = (this.clientName != null && !this.clientName.isEmpty()) ? this.clientName : "Client (pre-auth or unknown)";
            if (!socket.isClosed()) { // Only log if not an expected closure
                System.err.println("ClientHandler: Connection with " + logName + " lost or error: " + e.getMessage());
            }
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("ClientHandler: Error closing socket: " + e.getMessage());
            }
            // Only remove and broadcast if client was fully authenticated and added to server's list
            if (this.clientName != null && !this.clientName.isEmpty() && isAuthenticated) {
                assert server != null;
                server.removeClient(this);
                System.out.println("ClientHandler: " + this.clientName + " has left the chat.");
                server.broadcastMessage(this.clientName + " has left the chat.", this);

            }
        }
    }

    // Send a message to this specific client
    public void sendMessage(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
        }
    }

}
