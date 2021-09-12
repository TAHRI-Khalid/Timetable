package tk.tahri.timetable.view;

import javafx.scene.input.TransferMode;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;
import java.time.DayOfWeek;
import java.time.LocalTime;

public abstract class MainTimeTableView extends TimeTableView {
    private SeanceView seanceView;

    public MainTimeTableView(SeanceType type) {
        super(type);
        draw();
        addDragAndDropEvents(type);
    }

    private void addDragAndDropEvents(SeanceType type) {
        this.setOnDragEntered(mouseDragEvent -> {
            seanceView = new SeanceView(getDraggedSeance(), colWidth, rowHeight, true, type);
            seanceView.getStyleClass().add(
                    switch (type) {
                        case GROUPE -> "groupe-seance-view";
                        case FORMATEUR -> "formateur-seance-view";
                        case ESPACE -> "espace-seance-view";
                    }
            );
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

        y = Math.max(y, 0);
        y = Math.min(y, 5);

        var duree = seanceView.getSeance().getDuree();
        seanceView.getSeance().setJour(DayOfWeek.of(y + 1));
        LocalTime debut = LocalTime.of(8, 30).plusMinutes(x * 15);

        if (debut.isBefore(LocalTime.of(8, 30))) {
            debut = LocalTime.of(8, 30);
        }
        seanceView.getSeance().setDebut(debut);
        LocalTime fin = seanceView.getSeance().getDebut().plusMinutes(duree);

        if (fin.isAfter(LocalTime.of(18, 30))) {
            debut = LocalTime.of(18, 30).minusMinutes(duree);
            seanceView.getSeance().setDebut(debut);
            seanceView.getSeance().setFin(debut.plusMinutes(duree));
        } else {
            seanceView.getSeance().setFin(fin);
        }
    }

    protected abstract Seance getDraggedSeance();

    protected abstract void refreshView(SeanceView seanceView);

    protected void saveSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("INSERT INTO seance VALUES(null,%d,%d,%d,%d,%d,%d)"
                .formatted(s.getDebut().toSecondOfDay(), s.getFin().toSecondOfDay(), s.getJour().getValue(),
                        s.getGroupe().getId(), s.getFormateur().getId(), s.getEspace().getId()));
        loadSeanceViews(true);
        refreshView(seanceView);
    }

    protected void updateSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("UPDATE seance set debut=%d, fin=%d, jour=%d WHERE id=%d"
                .formatted(s.getDebut().toSecondOfDay(),
                        s.getFin().toSecondOfDay(),
                        s.getJour().getValue(),
                        s.getId()));
        loadSeanceViews(true);
        refreshView(seanceView);
    }

    protected void deleteSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("DELETE FROM seance WHERE id=%d".formatted(s.getId()));
        loadSeanceViews(true);
        refreshView(seanceView);
    }

}
