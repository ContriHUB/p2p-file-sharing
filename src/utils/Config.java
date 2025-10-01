package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class Config {

    private static final String DEFAULT_TEST_DIR = "./test";
    private static final String DEFAULT_DOWNLOADS_DIR = "./downloads";

    private static final Properties PROPERTIES = new Properties();

    static {
        // Try to load properties from working directory: config.properties
        // Fallback to defaults if not present or fails
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            PROPERTIES.load(fis);
        } catch (IOException ignored) {
            // Using defaults
        }
    }

    private Config() {}

    public static String getTestDir() {
        String dir = PROPERTIES.getProperty("test.dir", DEFAULT_TEST_DIR);
        ensureDirectory(dir);
        return dir;
    }

    public static String getDownloadsDir() {
        String dir = PROPERTIES.getProperty("downloads.dir", DEFAULT_DOWNLOADS_DIR);
        ensureDirectory(dir);
        return dir;
    }

    private static void ensureDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}


