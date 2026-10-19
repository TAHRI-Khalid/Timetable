package tk.tahri.timetable.view;

import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import tk.tahri.timetable.Entities.CreneauHoraire;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.NotificationService;
import tk.tahri.timetable.tools.command.AddSeanceCommand;
import tk.tahri.timetable.tools.command.DeleteSeanceCommand;
import tk.tahri.timetable.tools.command.UndoManager;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public abstract class MainTimeTableView extends TimeTableView {
    private SeanceView seanceView;
    private SeanceView selectedSeanceView;

    public MainTimeTableView(SeanceType type) {
        super(type);
        setFocusTraversable(true);
        draw();
        addDragAndDropEvents(type);
        addKeyAndMouseEvents();
    }

    private void addKeyAndMouseEvents() {
        this.setOnMouseClicked(event -> {
            if (event.getTarget() == this || event.getTarget() instanceof Rectangle || event.getTarget() instanceof Line) {
                setSelectedSeanceView(null);
            }
        });

        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                if (selectedSeanceView != null) {
                    deleteSelectedSeance();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.D && event.isControlDown()) {
                if (selectedSeanceView != null) {
                    duplicateSelectedSeance();
                    event.consume();
                }
            }
        });
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
            var seance = seanceView.getSeance();
            this.getChildren().remove(seanceView);
            UndoManager.getInstance().execute(new AddSeanceCommand(this, seance));
            NotificationService.showSuccess("Séance ajoutée", "Séance posée avec succès (Ctrl+Z pour annuler).");
            event.setDropCompleted(true);
            event.consume();
        });
    }

    public void placeSeance(SeanceView seanceView) {
        var duree = seanceView.getSeance().getDuree();
        if (duree <= 0) {
            duree = 60;
        }

        // Déterminer la colonne cible à partir du début de la séance
        double startX = seanceView.getTranslateX();
        var targetCol = (int) Math.round((startX + (colWidth / 2.0) - tableX) / colWidth);
        targetCol = Math.max(0, Math.min(targetCol, totalCols - 1));

        var y = (int) ((seanceView.getTranslateY() + (rowHeight / 2) - tableY) / rowHeight);
        y = Math.max(0, Math.min(y, 5));
        seanceView.getSeance().setJour(DayOfWeek.of(y + 1));

        var creneaux = Database.getInstance().getCreneaux();
        CreneauHoraire matchedCreneau = null;

        if (creneaux != null && !creneaux.isEmpty()) {
            for (var c : creneaux) {
                int startCol = (int) (ChronoUnit.MINUTES.between(beginTime, c.getHeureDebut()) / 15);
                int endCol = (int) (ChronoUnit.MINUTES.between(beginTime, c.getHeureFin()) / 15);
                if (targetCol >= startCol && targetCol < endCol) {
                    matchedCreneau = c;
                    break;
                }
            }
        }

        LocalTime debut;
        if (matchedCreneau != null) {
            debut = matchedCreneau.getHeureDebut();
        } else {
            debut = beginTime.plusMinutes(targetCol * 15L);
            if (debut.isBefore(beginTime)) {
                debut = beginTime;
            }
        }

        // Conserver strictement la durée originale de la séance
        LocalTime maxFin = beginTime.plusMinutes(totalCols * 15L);
        LocalTime fin = debut.plusMinutes(duree);
        if (fin.isAfter(maxFin)) {
            debut = maxFin.minusMinutes(duree);
            if (debut.isBefore(beginTime)) {
                debut = beginTime;
            }
            fin = debut.plusMinutes(duree);
            if (fin.isAfter(maxFin)) {
                fin = maxFin;
            }
        }

        seanceView.getSeance().setDebut(debut);
        seanceView.getSeance().setFin(fin);

        int finalCol = (int) (ChronoUnit.MINUTES.between(beginTime, debut) / 15);
        seanceView.setTranslateX((finalCol * colWidth) + tableX);
        seanceView.setTranslateY((y * rowHeight) + tableY);
        seanceView.setPrefWidth((seanceView.getSeance().getDuree() / 15.0) * colWidth);
        seanceView.setMaxWidth((seanceView.getSeance().getDuree() / 15.0) * colWidth);
    }

    public SeanceView getSelectedSeanceView() {
        return selectedSeanceView;
    }

    public void setSelectedSeanceView(SeanceView sv) {
        if (selectedSeanceView != null && selectedSeanceView != sv) {
            selectedSeanceView.setSelected(false);
        }
        selectedSeanceView = sv;
        if (selectedSeanceView != null) {
            selectedSeanceView.setSelected(true);
        }
    }

    public void deleteSelectedSeance() {
        if (selectedSeanceView != null) {
            var s = selectedSeanceView.getSeance();
            setSelectedSeanceView(null);
            UndoManager.getInstance().execute(new DeleteSeanceCommand(this, s));
            NotificationService.showSuccess("Séance supprimée", "La séance a été supprimée (Ctrl+Z pour annuler).");
        }
    }

    public void duplicateSelectedSeance() {
        if (selectedSeanceView != null) {
            var orig = selectedSeanceView.getSeance();
            var clone = new Seance(orig);
            clone.setId(0);

            long dur = orig.getDuree();
            LocalTime nextDebut = orig.getFin();
            LocalTime nextFin = nextDebut.plusMinutes(dur);
            if (!nextFin.isAfter(endTime)) {
                clone.setDebut(nextDebut);
                clone.setFin(nextFin);
                clone.setJour(orig.getJour());
            } else {
                int nextDay = (orig.getJour().getValue() % 6) + 1;
                clone.setJour(DayOfWeek.of(nextDay));
            }

            UndoManager.getInstance().execute(new AddSeanceCommand(this, clone));
            NotificationService.showSuccess("Séance dupliquée", "Séance dupliquée avec succès (Ctrl+Z pour annuler).");
        }
    }

    protected abstract Seance getDraggedSeance();

    public abstract void refreshView(Seance seance);

    public void saveSeance(Seance s) {
        var id = Database.getInstance().execute("INSERT INTO seance VALUES(null,?,?,?,?,?,?)",
                String.valueOf(s.getDebut().toSecondOfDay()),
                String.valueOf(s.getFin().toSecondOfDay()),
                String.valueOf(s.getJour().getValue()),
                s.getType(),
                String.valueOf(s.getFormateur().getId()),
                String.valueOf(s.getEspace().getId()));

        s.setId(id);
        for (Groupe g : s.getGroupes()) {
            Database.getInstance().execute("INSERT INTO seance_groupe VALUES(?,?)",
                    String.valueOf(g.getId()),
                    String.valueOf(id));
        }

        loadSeanceViews(true);
        refreshView(s);
    }

    public void updateSeance(Seance s) {
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

    public void deleteSeance(Seance s) {
        Database.getInstance().execute("DELETE FROM seance_groupe WHERE idSeance=?", String.valueOf(s.getId()));
        Database.getInstance().execute("DELETE FROM seance WHERE id=?", String.valueOf(s.getId()));
        loadSeanceViews(true);
        refreshView(s);
    }

}
