package chat.controller;

/**
 * MessageSender interface for the P2P Chat application.
 * This interface defines the contract for components that can send messages.
 */

public interface MessageSender {

    /**
     * Sends a message.
     *
     * @param message The message to be sent
     */
    void sendMessage(String message);
}
