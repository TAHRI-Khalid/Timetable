package tk.tahri.timetable.view;

import javafx.scene.input.TransferMode;
import tk.tahri.timetable.Entities.CreneauHoraire;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

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
            double offset = (seanceView.getPrefWidth() > 0 ? seanceView.getPrefWidth() / 2.0 : colWidth * 2);
            seanceView.setTranslateX(mouseDragEvent.getX() - offset);
            seanceView.setTranslateY(mouseDragEvent.getY() - rowHeight / 2);
            mouseDragEvent.consume();
        });

        this.setOnDragOver(mouseEvent -> {
            double offset = (seanceView.getPrefWidth() > 0 ? seanceView.getPrefWidth() / 2.0 : colWidth * 2);
            seanceView.setTranslateX(mouseEvent.getX() - offset);
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
            saveSeance(seanceView.getSeance());
            event.setDropCompleted(true);
            event.consume();
        });
    }

    public void placeSeance(SeanceView seanceView) {
        var duree = seanceView.getSeance().getDuree();
        double currentWidth = (duree > 0 ? (duree / 15.0) * colWidth : colWidth * 4);
        double centerX = seanceView.getTranslateX() + (currentWidth / 2.0);
        var centerCol = (int) Math.round((centerX - tableX) / colWidth);
        var y = (int) ((seanceView.getTranslateY() + (rowHeight / 2) - tableY) / rowHeight);

        y = Math.max(y, 0);
        y = Math.min(y, 5);
        seanceView.getSeance().setJour(DayOfWeek.of(y + 1));

        var creneaux = Database.getInstance().getCreneaux();
        CreneauHoraire matchedCreneau = null;

        if (creneaux != null && !creneaux.isEmpty()) {
            for (var c : creneaux) {
                int startCol = (int) (ChronoUnit.MINUTES.between(beginTime, c.getHeureDebut()) / 15);
                int endCol = (int) (ChronoUnit.MINUTES.between(beginTime, c.getHeureFin()) / 15);
                if (centerCol >= startCol && centerCol < endCol) {
                    matchedCreneau = c;
                    break;
                }
            }
        }

        if (matchedCreneau != null) {
            seanceView.getSeance().setDebut(matchedCreneau.getHeureDebut());
            seanceView.getSeance().setFin(matchedCreneau.getHeureFin());
            int startCol = (int) (ChronoUnit.MINUTES.between(beginTime, matchedCreneau.getHeureDebut()) / 15);
            seanceView.setTranslateX((startCol * colWidth) + tableX);
            seanceView.setTranslateY((y * rowHeight) + tableY);
        } else {
            var x = (int) ((seanceView.getTranslateX() + (colWidth / 2) - tableX) / colWidth);
            x = Math.max(0, Math.min(x, totalCols - 1));
            seanceView.setTranslateX((x * colWidth) + tableX);
            seanceView.setTranslateY((y * rowHeight) + tableY);

            LocalTime debut = beginTime.plusMinutes(x * 15L);
            if (debut.isBefore(beginTime)) {
                debut = beginTime;
            }
            seanceView.getSeance().setDebut(debut);
            LocalTime fin = debut.plusMinutes(duree);
            LocalTime maxFin = beginTime.plusMinutes(totalCols * 15L);
            if (fin.isAfter(maxFin)) {
                debut = maxFin.minusMinutes(duree);
                seanceView.getSeance().setDebut(debut);
                seanceView.getSeance().setFin(maxFin);
            } else {
                seanceView.getSeance().setFin(fin);
            }
        }
    }

    protected abstract Seance getDraggedSeance();

    protected abstract void refreshView(Seance seance);

    protected void saveSeance(Seance s) {
        var id = Database.getInstance().execute("INSERT INTO seance VALUES(null,?,?,?,?,?,?)",
                String.valueOf(s.getDebut().toSecondOfDay()),
                String.valueOf(s.getFin().toSecondOfDay()),
                String.valueOf(s.getJour().getValue()),
                s.getType(),
                String.valueOf(s.getFormateur().getId()),
                String.valueOf(s.getEspace().getId()));

        Database.getInstance().execute("INSERT INTO seance_groupe VALUES(?,?)",
                String.valueOf(s.getGroupes().get(0).getId()),
                String.valueOf(id));

        loadSeanceViews(true);
        refreshView(s);
    }

    protected void updateSeance(Seance s) {
        Database.getInstance().execute("UPDATE seance set debut=?, fin=?, jour=? ,type=?,idEspace=? ,idFormateur=? WHERE id=?",
                String.valueOf(s.getDebut().toSecondOfDay()),
                String.valueOf(s.getFin().toSecondOfDay()),
                String.valueOf(s.getJour().getValue()),
                s.getType(),
                String.valueOf(s.getEspace().getId()),
                String.valueOf(s.getFormateur().getId()),
                String.valueOf(s.getId()));
        loadSeanceViews(true);
        refreshView(s);
    }

    protected void deleteSeance(Seance s) {
        Database.getInstance().execute("DELETE FROM seance_groupe WHERE idSeance=?", String.valueOf(s.getId()));
        Database.getInstance().execute("DELETE FROM seance WHERE id=?", String.valueOf(s.getId()));
        loadSeanceViews(true);
        refreshView(s);
    }

}
