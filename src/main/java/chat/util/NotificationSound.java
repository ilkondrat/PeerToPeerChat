package chat.util;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for playing notification sounds for different application events.
 * This class handles loading sound files from resources and playing them using Java Sound API.
 */
public class NotificationSound {

    /**
     * Enumeration representing different types of sound notifications.
     * Each sound type is associated with a specific audio file path located in the application's resources.
     * The paths are expected to be absolute from the root of the classpath (e.g., "/sounds/message.wav").
     */
//    public enum SoundType {
//        /** Sound played when a client successfully connects to the server. */
//        CLIENT_CONNECTED("/sounds/connect.wav"),
//        /** Sound played when a client disconnects from the server. */
//        CLIENT_DISCONNECTED("/sounds/disconnect.wav"),
//        /** Sound played when a new chat message is received. */
//        NEW_MESSAGE("/sounds/message.wav");
//        // /** Optional: Sound played when an error event occurs. */
//        // ERROR("/sounds/error.wav");
//
//        private final String filePath;
//
//        /**
//         * Constructs a SoundType with the specified resource path.
//         * @param filePath The absolute path to the sound file in the resources (e.g., "/sounds/filename.wav").
//         */
//        SoundType(String filePath) {
//            this.filePath = filePath;
//        }
//
//        /**
//         * Gets the resource file path for this sound type.
//         * @return The resource file path.
//         */
//        public String getFilePath() {
//            return filePath;
//        }
//    }

    /**
     * Plays a sound associated with the given {@link SoundType}.
     * <p>
     * This method loads the audio file as a resource, sets up an {@link AudioInputStream} and a {@link Clip},
     * and plays the sound. It uses a {@link LineListener} to close the clip and audio stream
     * when playback stops. The method blocks until the clip is closed by the listener or a timeout occurs.
     * </p>
     * <p>
     * Error messages are printed to {@code System.err} if the sound file cannot be found,
     * if the audio format is unsupported, if an audio line is unavailable, or if other I/O errors occur.
     * </p>
     *
     * @param soundType The type of sound to play, as defined in the {@link SoundType} enum.
     *                  If null, an error message is printed and the method returns.
     */
    public void playSound(SoundType soundType) {
        if (soundType == null) {
            System.err.println("NotificationSound: SoundType cannot be null.");
            return;
        }

        String resourcePath = soundType.getFilePath();

        AudioInputStream audioIn = null;
        Clip clip = null;

        try {
            InputStream audioSrc = getClass().getResourceAsStream(resourcePath);

            if (audioSrc == null) {
                System.err.println("NotificationSound: Warning: Resource file not found: " + resourcePath);
                return;
            }
            // System.out.println("NotificationSound: Resource file found."); // Removed
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            clip = AudioSystem.getClip();

            if (clip == null) {
                System.err.println("NotificationSound: Could not get a clip for " + soundType + ". AudioSystem might not have available lines.");
                if (audioIn != null) try { audioIn.close(); } catch (IOException e) { /* ignore on cleanup */ }
                return;
            }

            // final String currentSoundTypeForListener = soundType.toString(); // Not needed if listener logging is removed
            final AudioInputStream finalAudioIn = audioIn;
            final Clip finalClip = clip;

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (finalClip != null) finalClip.close();
                    try {
                        if (finalAudioIn != null) finalAudioIn.close();
                    } catch (IOException e) {
                        System.err.println("NotificationSound: Error closing AudioInputStream in listener: " + e.getMessage());
                    }
                }
            });

            clip.open(audioIn);
            clip.start();
            // System.out.println("NotificationSound: Clip started for " + soundType); // Removed

            long startTime = System.currentTimeMillis();
            while (clip.isOpen()) {
                if (System.currentTimeMillis() - startTime > 5000) { // 5-second timeout
                    System.err.println("NotificationSound: Timeout waiting for clip to close for " + soundType);
                    if (clip.isRunning()) clip.stop();
                    if (clip.isOpen()) clip.close();
                    if (finalAudioIn != null) {
                        try {
                            // Check if stream is still valid before attempting to close after timeout
                            // This check is a bit of a guess; a robust way is to track if listener closed it.
                            if (finalAudioIn.available() > 0 || finalAudioIn.markSupported()) { // Heuristic
                                finalAudioIn.close();
                            }
                        } catch (IOException e) {
                            // It might have been closed by the listener already, or another issue.
                        }
                    }
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("NotificationSound: Sleep interrupted while waiting for clip to close for " + soundType);
                    break;
                }
            }

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("NotificationSound: Error playing sound for " + soundType + ": " + e.getMessage());
            // e.printStackTrace(); // Keep this commented unless actively debugging specific errors
            try {
                if (clip != null && clip.isOpen()) clip.close();
                if (audioIn != null) audioIn.close();
            } catch (IOException ex) {
                System.err.println("NotificationSound: Error closing resources in main catch block: " + ex.getMessage());
            }
        }
    }


}
