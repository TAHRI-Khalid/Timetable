package tk.tahri.timetable.view;

import javafx.scene.input.TransferMode;
import tk.tahri.timetable.Entities.Seance;
import java.time.DayOfWeek;
import java.time.LocalTime;

public abstract class MainTimeTableView extends TimeTableView {
    private SeanceView seanceView;

    public MainTimeTableView() {
        draw();
        addDragAndDropEvents();
    }

    private void addDragAndDropEvents() {
        this.setOnDragEntered(mouseDragEvent -> {
            seanceView = new SeanceView(getDraggedSeance(), colWidth, rowHeight, true);
            seanceView.getStyleClass().add("groupe-seance-view");
            this.getChildren().add(seanceView);
            seanceView.setTranslateX(mouseDragEvent.getX() - colWidth * 5);
            seanceView.setTranslateY(mouseDragEvent.getY() - rowHeight / 2);
            mouseDragEvent.consume();
        });

        this.setOnDragOver(mouseEvent -> {
            seanceView.setTranslateX(mouseEvent.getX() - colWidth * 5);
            seanceView.setTranslateY(mouseEvent.getY() - rowHeight / 2);
            mouseEvent.acceptTransferModes(TransferMode.ANY);
            mouseEvent.consume();

        });

        this.setOnDragExited(dragEvent -> {
            if (dragEvent.isDropCompleted()) {
                return;
            }
            this.getChildren().remove(seanceView);
            dragEvent.consume();
        });

        this.setOnDragDropped(event -> {
            placeSeance(seanceView);
            saveSeance(seanceView);
            event.setDropCompleted(true);
            event.consume();
        });
    }

    public void placeSeance(SeanceView seanceView) {
        var x = (int) ((seanceView.getTranslateX() + (colWidth / 2) - tableX) / colWidth);
        var y = (int) ((seanceView.getTranslateY() + (rowHeight / 2) - tableY) / rowHeight);
        seanceView.setTranslateX((x * colWidth) + tableX);
        seanceView.setTranslateY((y * rowHeight) + tableY);

        var duree = seanceView.getSeance().getDuree();
        seanceView.getSeance().setJour(DayOfWeek.of(y + 1));
        seanceView.getSeance().setDebut(LocalTime.of(8, 30).plusMinutes(x * 15));
        seanceView.getSeance().setFin(seanceView.getSeance().getDebut().plusMinutes(duree));
    }

    protected abstract Seance getDraggedSeance();
    protected abstract void saveSeance(SeanceView seanceView);
    protected abstract void updateSeance(SeanceView seanceView);
    protected abstract void deleteSeance(SeanceView seanceView);
    protected abstract void refreshView();

}
