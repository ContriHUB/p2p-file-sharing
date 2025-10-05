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

     // Getters for broadcast properties
    public static String getBroadcastGroup() 
    { 
        return PROPERTIES.getProperty("broadcast.group","255.255.255.255").trim();
    }
    public static int getBroadcastPort()
     {
        return Integer.parseInt(PROPERTIES.getProperty("broadcast.port","50010")); 
    }
    public static int getBroadcastChunkBytes() 
    { 
        return Integer.parseInt(PROPERTIES.getProperty("broadcast.chunk_bytes","4096"));
    }
    public static int getBroadcastMaxRounds() 
    { 
        return Integer.parseInt(PROPERTIES.getProperty("broadcast.max_rounds","3")); 
    }
    public static int getBroadcastNackPort() 
    {
        return Integer.parseInt(PROPERTIES.getProperty("broadcast.nack_port","50011")); 
    }
    public static int getBroadcastNackDelayMs() 
    {
        return Integer.parseInt(PROPERTIES.getProperty("broadcast.nack_delay_ms","200")); 
    }
    public static int getBroadcastWaitNacksMs() 
    { 
        return Integer.parseInt(PROPERTIES.getProperty("broadcast.wait_nacks_ms","250")); 
    }
    public static int getBeaconPort() 
    {
        return Integer.parseInt(PROPERTIES.getProperty("beacon.port", "12344"));
    }

    // Getters for directory paths
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


