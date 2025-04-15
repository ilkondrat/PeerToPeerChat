package chat.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServerTest {
    @Test
    public void terstServerConnnection() throws IOException {
        Server server = new Server();
        server.start(0);
        assertTrue(server.getPort() > 0);
        server.stop();

    }

    @Test
    public void testBroadcastMessage() throws IOException {
        Server server = new Server();
        //fake client handlers

        ClientHandler client1 = mock(ClientHandler.class);
        ClientHandler client2 = mock(ClientHandler.class);
        ClientHandler client3 = mock(ClientHandler.class);
        ClientHandler client4 = mock(ClientHandler.class);
        ClientHandler sender = mock(ClientHandler.class);

        //adding them to Server
        server.getClients().add(client1);
        server.getClients().add(client2);
        server.getClients().add(client3);
        server.getClients().add(client4);
        server.getClients().add(sender);
        server.broadcastMessage("Test message!!!", sender);

        verify(client1).sendMessage("Hello World");
        verify(client2).sendMessage("Bye world");

        //sender should not get a message
        verify(sender, never()).sendMessage(anyString());
        server.stop();
    }

    // integration test

    @Test
    public void testClientServerInteraction() throws IOException, InterruptedException{

        Server server = new Server();
        server.start(0);
        int port = server.getPort();

        // client connection

        Client client1 = new Client();
        Client client2 = new Client();

        client1.connect("localhost", port, "User1");
        client2.connect("localhost", port, "User2");

        Thread.sleep(500);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        client1.sendMessage("Привет от User1!");

        // message to come
        Thread.sleep(500);

        // other client gets message
        String output = outputStream.toString();
        assertTrue(output.contains("[User1]: Привет от User1!"));

        // resources clean
        client1.disconnect();
        client2.disconnect();
        server.stop();

    }
}
