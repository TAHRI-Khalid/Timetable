package tk.tahri.timetable.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Seance;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public abstract class TimeTableView extends Pane {

    protected final List<Seance> seances = new ArrayList<>();

    private final String[] days = new String[]{"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private final LocalTime beginTime = LocalTime.of(8, 30);
    protected double colWidth = 24.5;
    protected double rowHeight = 50;
    protected double tableX = colWidth * 4;
    protected double tableY = rowHeight / 2;
    protected Font colHeader = new Font(15);
    protected Font rowHeader = new Font(20);
    protected int thickStroke = 3;
    protected int thinStroke = 1;
    protected boolean dashedLine = true;

    protected void draw() {
        getChildren().clear();
        var rec = new Rectangle(tableX, tableY, colWidth * 40, rowHeight * 6);
        rec.setFill(Color.TRANSPARENT);
        rec.setStroke(Color.BLACK);
        rec.setStrokeWidth(thickStroke);
        this.getChildren().add(rec);

        //draw times header and vertical lines
        var time = beginTime;
        for (int i = 0; i < 40; i++) {
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

                var recTimes = new Rectangle(tableX + (colWidth * i), 0, colWidth * 4, tableY);
                recTimes.setFill(Color.TRANSPARENT);
                recTimes.setStroke(Color.BLACK);
                recTimes.setStrokeWidth(thickStroke);
                this.getChildren().add(recTimes);

                var debut = time;
                var fin = time.plusHours(1);
                var txt = new Text(debut.toString() + " - " + fin.toString());
                txt.setFont(colHeader);
                txt.setX(tableX + (colWidth * i) + 1);
                txt.setY(rowHeight / 4 + 5);
                this.getChildren().add(txt);
                time = time.plusHours(1);
            }
            if (i == 20) {
                line.setStrokeWidth(thickStroke);
            }
            this.getChildren().add(line);
        }

        //draw days header and horizontal lines
        for (int i = 0; i < 6; i++) {
            var recDays = new Rectangle(0, tableY + (rowHeight * i), tableX, rowHeight);
            recDays.setFill(Color.TRANSPARENT);
            recDays.setStroke(Color.BLACK);
            recDays.setStrokeWidth(thickStroke);
            this.getChildren().add(recDays);

            var day = new Text(days[i]);
            day.setFont(rowHeader);
            day.setX(2);
            day.setY(tableY + (rowHeight * i) + rowHeight / 2 + 9);
            this.getChildren().add(day);

            var line = new Line(tableX, tableY + (rowHeight * i), tableX + (colWidth * 40), tableY + (rowHeight * i));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(thinStroke);
            this.getChildren().add(line);
        }
    }

    public void loadSeanceViews(boolean draggable, String style) {
        seances.clear();
        this.getChildren().removeIf(node -> node instanceof SeanceView);
        loadData();
        for (var s1 : seances) {
            var sv = new SeanceView(s1, colWidth, rowHeight, draggable);
            var ch = false;
            for (var s2 : seances) {
                if (chevauchement(s1, s2)) {
                    ch = true;
                    break;
                }
            }
            sv.getStyleClass().add(ch ? "chevauchement-seance-view" : style);
            this.getChildren().add(sv);
            sv.setTranslateX((((s1.getDebut().toSecondOfDay() - LocalTime.of(8, 30).toSecondOfDay()) / (60 * 15)) * colWidth) + tableX);
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
        } else if (s2.getDebut().toSecondOfDay() >= s1.getDebut().toSecondOfDay() && s2.getFin().toSecondOfDay() <= s1.getFin().toSecondOfDay()) {
            return true;
        }
        return false;
    }

    protected abstract void loadData();
}