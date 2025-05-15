package chat;
// TestSound.java (поместите в тот же пакет, что и Client, или любой другой)
import chat.network.Client;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestSound {

    public static void playNotificationSound(){ // Сделаем статическим для простоты вызова
        try {
            // Используем Client.class, чтобы контекст загрузки ресурса был таким же, как в вашем клиенте
            // если TestSound в другом пакете. Если в том же, можно this.getClass() или TestSound.class
            InputStream audioSrc = Client.class.getResourceAsStream("/sounds/disconnect.wav");
            System.out.println("Attempting to load: /sounds/message.wav");
            if (audioSrc == null) {
                System.err.println("TestSound: Warning: notification sound file not found.");
                return;
            }
            System.out.println("TestSound: Sound file found, proceeding.");
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();

            // Добавим слушателя событий для отладки Clip
            clip.addLineListener(event -> {
                System.out.println("TestSound Clip Event: " + event.getType());
                if (event.getType() == LineEvent.Type.STOP) {
                    System.out.println("TestSound: Clip stopped. Closing resources.");
                    event.getLine().close(); // Закрываем clip
                    try {
                        audioIn.close(); // Закрываем AudioInputStream
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            clip.open(audioIn);
            clip.start();
            System.out.println("TestSound: Clip started.");

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("TestSound: Error playing notification sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("TestSound: Running sound test...");
        playNotificationSound();
        System.out.println("TestSound: Sound play attempt initiated. Main thread will wait briefly.");
        // Дадим время звуку проиграться, так как clip.start() неблокирующий
        try {
            Thread.sleep(3000); // Ждем 3 секунды
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("TestSound: Test finished.");
    }
}

