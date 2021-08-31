package tk.tahri.timetable.view;


import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.dao.Database;

public class SeanceView extends VBox {

    private final Seance seance;
    private double firstClickX;
    private double firstClickY;

    public SeanceView(Seance seance, double width, double height, boolean draggable) {
        this.seance = seance;
        this.getChildren().addAll(new Label(seance.getFormateur().toString()), new Label(seance.getEspace().toString()));
        this.setPrefSize(seance.getDuree() / 15 * width, height);

        if (draggable) {
            ContextMenu contextMenu = new ContextMenu();

            var mHeurs = new Menu("Heurs");
            var h2 = new MenuItem("02 H 00 Min");
            var h25 = new MenuItem("02 H 30 Min");
            var h3 = new MenuItem("03 H 00 Min");
            var h4 = new MenuItem("04 H 00 Min");
            var h5 = new MenuItem("05 H 00 Min");
            mHeurs.getItems().addAll(h2, h25, h3, h4, h5);

            h2.setOnAction(event -> this.setPrefWidth(width * 8));
            h25.setOnAction(event -> this.setPrefWidth(width * 10));
            h3.setOnAction(event -> this.setPrefWidth(width * 12));
            h4.setOnAction(event -> this.setPrefWidth(width * 16));
            h5.setOnAction(event -> this.setPrefWidth(width * 20));

            var mSuppr = new MenuItem("Suuprimer");
            mSuppr.setOnAction(event -> ((Pane) this.getParent()).getChildren().remove(this));

            contextMenu.getItems().addAll(mHeurs, mSuppr);
            this.setOnContextMenuRequested(event1 -> contextMenu.show(this, event1.getScreenX(), event1.getScreenY()));

            this.setOnMousePressed(mouseEvent -> {
                this.firstClickX = mouseEvent.getX();
                this.firstClickY = mouseEvent.getY();
            });
            this.setOnMouseDragged(event -> {
                this.setTranslateX(this.getTranslateX() - firstClickX + event.getX());
                this.setTranslateY(this.getTranslateY() - firstClickY + event.getY());
            });

            this.setOnMouseReleased(mouseEvent -> {
                ((MainTimeTableView) this.getParent()).placeSeance(this);
                ((MainTimeTableView) this.getParent()).updateSeance(this);
            });
        }
    }

    public Seance getSeance() {
        return seance;
    }
}
