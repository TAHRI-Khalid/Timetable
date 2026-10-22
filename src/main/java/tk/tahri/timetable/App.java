package tk.tahri.timetable;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tk.tahri.timetable.tools.Config;
import tk.tahri.timetable.tools.NotificationService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class App extends Application {
    Image icon;
    String css;

    @Override
    public void init() throws Exception {
        super.init();
        Config.load();
        icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png")));
        css = Objects.requireNonNull(getClass().getResource("/css/style.css")).toString();

        if (Config.getProperty("file") != null && !Config.getProperty("file").trim().isEmpty()) {
            var source = Path.of(Config.getProperty("file"));
            if (Files.exists(source)) {
                Path dest = Files.createTempFile("temp", ".tk");
                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                Config.setProperty("temp", dest.toString());
            } else {
                Config.setProperty("file", "");
                Config.setProperty("temp", "");
            }
        } else {
            Config.setProperty("file", "");
            Config.setProperty("temp", "");
        }
        Config.setProperty("modified", Boolean.FALSE.toString());
    }

    @Override
    public void start(Stage stage) {
        var parent = new Editor();
        Scene scene = new Scene(parent, 1368, 768);
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.getIcons().add(icon);
        stage.setTitle("Time Table");
        stage.setOnHiding(event -> onStageClose(parent, event));
        stage.setMaximized(true);
        stage.show();
    }

    private void onStageClose(Editor parent, WindowEvent event) {
        var modified = Boolean.parseBoolean(Config.getProperty("modified"));
        if (modified) {
            var choice = NotificationService.confirmSaveOnClose("de quitter l'application");
            if (choice == NotificationService.SaveChoice.SAVE) {
                parent.onMenuSave();
            } else if (choice == NotificationService.SaveChoice.CANCEL) {
                event.consume();
                return;
            }
        }
        Config.setProperty("modified", Boolean.FALSE.toString());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Config.save();
    }

    public static void main(String[] args) {
        launch(args);
    }
}