package tk.tahri.timetable.view;


import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import tk.tahri.timetable.Entities.Seance;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeanceView extends VBox {

    private final Seance seance;
    private double firstClickX;
    private double firstClickY;

    public SeanceView(Seance seance, double width, double height, boolean draggable) {
        this.seance = seance;
        this.getChildren().addAll(new Label(seance.getFormateur().toString()), new Label(seance.getEspace().toString()));
        this.setPrefSize(seance.getDuree() / 15 * width, height);

        if (draggable) {
            AtomicBoolean isDragging = new AtomicBoolean(false);
            ContextMenu contextMenu = new ContextMenu();

            var mHeurs = new Menu("Heurs");
            var h2 = new MenuItem("02 H 00 Min");
            var h25 = new MenuItem("02 H 30 Min");
            var h3 = new MenuItem("03 H 00 Min");
            var h4 = new MenuItem("04 H 00 Min");
            var h5 = new MenuItem("05 H 00 Min");
            mHeurs.getItems().addAll(h2, h25, h3, h4, h5);

            h2.setOnAction(event -> this.updateTime(Duration.ofMinutes(120)));
            h25.setOnAction(event -> this.updateTime(Duration.ofMinutes(150)));
            h3.setOnAction(event -> this.updateTime(Duration.ofMinutes(180)));
            h4.setOnAction(event -> this.updateTime(Duration.ofMinutes(240)));
            h5.setOnAction(event -> this.updateTime(Duration.ofMinutes(300)));

            var mSuppr = new MenuItem("Suuprimer");
            mSuppr.setOnAction(event -> {
                ((MainTimeTableView) this.getParent()).deleteSeance(this);
            });
            contextMenu.getItems().addAll(mHeurs, mSuppr);

            this.setOnMousePressed(event -> {
                this.firstClickX = event.getX();
                this.firstClickY = event.getY();
                ((MainTimeTableView) this.getParent()).refreshView(this);
            });

            this.setOnMouseDragged(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    this.setTranslateX(this.getTranslateX() - firstClickX + event.getX());
                    this.setTranslateY(this.getTranslateY() - firstClickY + event.getY());
                    isDragging.set(true);
                }
            });

            this.setOnMouseReleased(event -> {
                if (event.getButton() == MouseButton.PRIMARY && isDragging.get() ){
                    ((MainTimeTableView) this.getParent()).placeSeance(this);
                    ((MainTimeTableView) this.getParent()).updateSeance(this);
                    isDragging.set(false);
                } else if (event.getButton() == MouseButton.SECONDARY){
                    contextMenu.show(this, event.getScreenX(), event.getScreenY());
                }
            });
        }
    }

    public Seance getSeance() {
        return seance;
    }

    private void updateTime(Duration duration) {
        this.seance.setFin(this.seance.getDebut().plusMinutes(duration.toMinutes()));
        ((MainTimeTableView) this.getParent()).updateSeance(this);
    }
}
