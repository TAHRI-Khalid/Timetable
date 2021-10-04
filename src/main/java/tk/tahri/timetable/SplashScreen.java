package tk.tahri.timetable;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen extends Preloader {
    private Stage preloaderStage;
    private Scene scene;

    @Override
    public void init() {
        Pane root = new Pane();
        scene = new Scene(root, 400, 300);
    }

    @Override
    public void start(Stage stage){
        this.preloaderStage = stage;
        preloaderStage.setScene(scene);
        preloaderStage.initStyle(StageStyle.UNDECORATED);
        preloaderStage.show();
    }

    @Override
    public void handleApplicationNotification(Preloader.PreloaderNotification info) {

    }

    @Override
    public void handleStateChangeNotification(Preloader.StateChangeNotification info) {
        StateChangeNotification.Type type = info.getType();
        if (type == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}
