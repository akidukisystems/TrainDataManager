package jp.akidukisystems.software.TrainDataClient;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private final Properties props = new Properties();
    private final File file = new File("config.properties");

    public void load() {
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            props.store(writer, "Train Data Client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    public String get(String key, String def) {
        return props.getProperty(key, def);
    }
}
