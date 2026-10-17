package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.CreneauHoraire;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

public abstract class TimeTableView extends Pane {

    protected final List<Seance> seances = new ArrayList<>();

    protected final String[] days = new String[]{"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    protected final LocalTime beginTime = LocalTime.of(8, 0);
    protected final LocalTime endTime = LocalTime.of(18, 30);
    protected int totalCols = (int) (ChronoUnit.MINUTES.between(beginTime, endTime) / 15); // 42
    protected double colWidth = 25;
    protected double rowHeight = 55;
    protected double tableX = colWidth * 4;
    protected double tableY = rowHeight / 2;
    protected Font colHeader = new Font(15);
    protected Font rowHeader = new Font(20);
    protected int thickStroke = 3;
    protected int thinStroke = 1;
    protected boolean dashedLine = true;
    protected TitleToolBar titleToolBar;
    protected Label Titre;
    protected final SeanceType type;
    protected double timeHeaderX = tableX + 5;
    protected double timeHeaderY = rowHeight / 4 + 5;
    protected double dayHeaderX = 6;
    protected double dayHeaderY = tableY + rowHeight / 2 + 6;

    protected TimeTableView(SeanceType type) {
        this.type = type;
    }

    protected void draw() {

        getChildren().clear();
        var rec = new Rectangle(tableX, tableY, colWidth * totalCols, rowHeight * 6);
        rec.setFill(Color.TRANSPARENT);
        rec.setStroke(Color.BLACK);
        rec.setStrokeWidth(thickStroke);
        this.getChildren().add(rec);

        var creneaux = Database.getInstance().getCreneaux();

        if (creneaux != null && !creneaux.isEmpty()) {
            // Dessiner les créneaux horaires configurés
            for (var c : creneaux) {
                int startCol = (int) Math.max(0, ChronoUnit.MINUTES.between(beginTime, c.getHeureDebut()) / 15);
                int endCol = (int) Math.min(totalCols, ChronoUnit.MINUTES.between(beginTime, c.getHeureFin()) / 15);
                if (endCol <= startCol) continue;

                double x = tableX + (colWidth * startCol);
                double width = colWidth * (endCol - startCol);

                var recTimes = new Rectangle(x, 0, width, tableY);
                recTimes.setFill(Color.TRANSPARENT);
                recTimes.setStroke(Color.BLACK);
                recTimes.setStrokeWidth(thickStroke);
                this.getChildren().add(recTimes);

                String label = c.getNom();
                // Si le libellé ne contient pas déjà les horaires, on peut les afficher si l'espace le permet
                if (!label.contains(c.getHeureDebutStr())) {
                    label = c.getNom() + " (" + c.getHeureDebutStr() + " - " + c.getHeureFinStr() + ")";
                }
                var txt = new Text(label);
                txt.setFont(colHeader);
                if (txt.getBoundsInLocal().getWidth() > width - 4) {
                    label = c.getNom() + " " + c.getHeureDebutStr() + "-" + c.getHeureFinStr();
                    txt.setText(label);
                }
                if (txt.getBoundsInLocal().getWidth() > width - 4) {
                    label = c.getNom();
                    txt.setText(label);
                }
                double textWidth = txt.getBoundsInLocal().getWidth();
                double textX = x + Math.max(2, (width - textWidth) / 2);
                txt.setX(textX);
                txt.setY(timeHeaderY);
                this.getChildren().add(txt);
            }

            // Lignes verticales
            for (int i = 0; i < totalCols; i++) {
                LocalTime colTime = beginTime.plusMinutes(i * 15L);
                boolean isBoundary = false;
                for (var c : creneaux) {
                    if (c.getHeureDebut().equals(colTime) || c.getHeureFin().equals(colTime)) {
                        isBoundary = true;
                        break;
                    }
                }

                if (!dashedLine && !isBoundary && i % 4 != 0) {
                    continue;
                }

                var line = new Line(tableX + (colWidth * i), tableY, tableX + (colWidth * i), tableY + (rowHeight * 6));
                line.setStroke(Color.BLACK);

                if (isBoundary || i == 20) {
                    line.setStrokeWidth(thickStroke);
                } else {
                    line.getStrokeDashArray().addAll(5d);
                    line.setStrokeWidth(thinStroke);
                }
                this.getChildren().add(line);
            }
        } else {
            // Mode classique par défaut (intervalles d'1 heure)
            var time = beginTime;
            for (int i = 0; i < totalCols; i++) {
                if (!dashedLine && i % 4 != 0) {
                    continue;
                }
                var line = new Line(tableX + (colWidth * i), tableY, tableX + (colWidth * i), tableY + (rowHeight * 6));
                line.setStroke(Color.BLACK);
                line.getStrokeDashArray().addAll(5d);
                line.setStrokeWidth(thinStroke);

                if (i % 4 == 0) {
                    line.getStrokeDashArray().clear();
                    line.setStrokeWidth(thinStroke);

                    int colSpan = Math.min(4, totalCols - i);
                    var recTimes = new Rectangle(tableX + (colWidth * i), 0, colWidth * colSpan, tableY);
                    recTimes.setFill(Color.TRANSPARENT);
                    recTimes.setStroke(Color.BLACK);
                    recTimes.setStrokeWidth(thickStroke);
                    this.getChildren().add(recTimes);

                    var debut = time;
                    var fin = time.plusMinutes(colSpan * 15L);
                    var txt = new Text(debut + " - " + fin.toString());
                    txt.setFont(colHeader);
                    txt.setX(timeHeaderX + (colWidth * i));
                    txt.setY(timeHeaderY);
                    this.getChildren().add(txt);
                    time = time.plusHours(1);
                }
                if (i == 20) {
                    line.setStrokeWidth(thickStroke);
                }
                this.getChildren().add(line);
            }
        }

        // Dessiner l'en-tête des jours et les lignes horizontales
        for (int i = 0; i < 6; i++) {
            var recDays = new Rectangle(0, tableY + (rowHeight * i), tableX, rowHeight);
            recDays.setFill(Color.TRANSPARENT);
            recDays.setStroke(Color.BLACK);
            recDays.setStrokeWidth(thickStroke);
            this.getChildren().add(recDays);

            var day = new Text(days[i]);
            day.setFont(rowHeader);
            day.setX(dayHeaderX);
            day.setY(dayHeaderY + (rowHeight * i));
            this.getChildren().add(day);

            var line = new Line(tableX, tableY + (rowHeight * i), tableX + (colWidth * totalCols), tableY + (rowHeight * i));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(thinStroke);
            this.getChildren().add(line);
        }
    }

    public void loadSeanceViews(boolean draggable) {
        seances.clear();
        this.getChildren().removeIf(node -> node instanceof SeanceView);
        var style = switch (type) {
            case GROUPE -> "groupe-seance-view";
            case FORMATEUR -> "formateur-seance-view";
            case ESPACE -> "espace-seance-view";
        };
        var chevechementStyle = "chevauchement-seance-view";
        if (this instanceof SubTimeTableView view) {
            style = "sub-".concat(style);
            chevechementStyle = "sub-".concat(chevechementStyle);
        }
        loadData();
        for (var s1 : seances) {
            var sv = new SeanceView(s1, colWidth, rowHeight, draggable, type);

            var tooltip = new Tooltip();
            tooltip.setText(s1.getFormateur().toString().concat("\n")
                    .concat(s1.getGroupes().toString()).concat("\n")
                    .concat("[" + s1.getType() + "] : " + s1.getEspace().getNom()));
            tooltip.setFont(new Font(14));
            Tooltip.install(sv, tooltip);

            var ch = false;
            for (var s2 : seances) {
                if (chevauchement(s1, s2)) {
                    ch = true;
                    break;
                }
            }
            sv.getStyleClass().add(ch ? chevechementStyle : style);
            this.getChildren().add(sv);
            sv.setTranslateX((((s1.getDebut().toSecondOfDay() - beginTime.toSecondOfDay()) / (60.0 * 15.0)) * colWidth) + tableX);
            sv.setTranslateY(((s1.getJour().getValue() - 1) * rowHeight) + tableY);
        }
    }

    private boolean chevauchement(Seance s1, Seance s2) {
        if (s1 == s2) {
            return false;
        } else if (s1.getJour().getValue() != s2.getJour().getValue()) {
            return false;
        } else if (s2.getDebut().toSecondOfDay() < s1.getDebut().toSecondOfDay() && s2.getFin().toSecondOfDay() > s1.getDebut().toSecondOfDay()) {
            return true;
        } else if (s2.getDebut().toSecondOfDay() < s1.getFin().toSecondOfDay() && s2.getFin().toSecondOfDay() > s1.getFin().toSecondOfDay()) {
            return true;
        } else
            return s2.getDebut().toSecondOfDay() >= s1.getDebut().toSecondOfDay() && s2.getFin().toSecondOfDay() <= s1.getFin().toSecondOfDay();
    }

    public void setTitre(Label titre) {
        Titre = titre;
    }

    protected abstract void loadData();

    public List<Seance> getSeances() {
        return seances;
    }

    public Duration getDuration() {
        var sec = getSeances().stream().mapToLong(s -> s.getFin().toSecondOfDay() - s.getDebut().toSecondOfDay()).sum();
        return Duration.of(sec, ChronoUnit.SECONDS);
    }

    public TitleToolBar getTitleToolBar() {
        return titleToolBar;
    }

    public void setTitleToolBar(TitleToolBar titleToolBar) {
        this.titleToolBar = titleToolBar;
    }
}