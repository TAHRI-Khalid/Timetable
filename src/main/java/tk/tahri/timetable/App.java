package tk.tahri.timetable;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Editor(), 1368, 768);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toString());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.setTitle("Time Table");
        //stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}