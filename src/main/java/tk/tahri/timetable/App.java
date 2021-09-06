package tk.tahri.timetable;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import tk.tahri.timetable.tools.Config;

import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Config.load();
        Editor parent = new Editor();
        Scene scene = new Scene(parent, 1368, 768);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toString());
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png"))));
        stage.setTitle("Time Table");
        //stage.setMaximized(true);
        stage.setOnHiding(event -> {
            var modified=Boolean.parseBoolean(Config.getProperty("modified"));
            if(modified){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Attension");
                alert.setContentText("le fichier a ete modifié voulez vous le sauvegarder ?");
                ButtonType okButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
                ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
                alert.showAndWait().ifPresent(type -> {
                    if (type == okButton) {
                        parent.onMenuSave();
                        System.out.println(type);
                    } else if (type == cancelButton) {
                        event.consume();
                    }
                });
            }
            Config.setProperty("modified",Boolean.FALSE.toString());
            Config.save();
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}