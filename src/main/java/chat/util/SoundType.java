package chat.util;

import lombok.Getter;

/**
 * Enumeration representing different types of sound notifications.
 * Each sound type is associated with a specific audio file path located in the application's resources.
 * The paths are expected to be absolute from the root of the classpath (e.g., "/sounds/message.wav").
 */

@Getter
public enum SoundType {

    CLIENT_CONNECTED("/sounds/connect.wav"),
    CLIENT_DISCONNECTED("/sounds/disconnect.wav"),
    NEW_MESSAGE("/sounds/message.wav");

    private final String filePath;

    SoundType(String filePath) {
        this.filePath = filePath;
    }

}

