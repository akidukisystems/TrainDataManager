package jp.akidukisystems.traindatamanager;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class ConfigManager {
    public static boolean isLogging;
    public static int networkPort;

    public static void init(File configFile) {
        Configuration config = new Configuration(configFile);

        try {
            config.load();
            isLogging = config.getBoolean("isLogging", "General", false, "");
            networkPort = config.getInt("networkPort", "General", 34565, 0, 65535, "");
        } catch (Exception e) {
            System.err.println("configLoad failed: " + e.getMessage());
        } finally {
            config.save();
        }
    }
}