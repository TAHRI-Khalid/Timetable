package tk.tahri.timetable.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();
    private static Path configFile;

    private static void init() {
        configFile = Path.of(System.getProperty("user.home") + "/.timetable/config.properties");

        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(Path.of(System.getProperty("user.home") + "/.timetable/"));
                Files.writeString(configFile, "");

                Config.setProperty("file", "");
                Config.setProperty("temp", "");
                Config.setProperty("modified",Boolean.FALSE.toString());

            } catch (Exception io) {
                io.printStackTrace();
            }
        }
    }

    public static void load() {
        init();
        if (Files.exists(configFile)) {
            try (var in = new FileInputStream(configFile.toString())) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No props");
        }
    }

    public static void save() {
        try (var out = new FileOutputStream(configFile.toString())) {
            props.store(out, "---No Comment---");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

}
