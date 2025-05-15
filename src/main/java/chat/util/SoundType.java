package chat.util;



public enum SoundType {

    CLIENT_CONNECTED("/sounds/connect.wav"),
    CLIENT_DISCONNECTED("/sounds/disconnect.wav"),
    NEW_MESSAGE("/sounds/message.wav");

    private final String filePath;

    SoundType(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}

