package tk.tahri.timetable.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class PrintTimeTableView extends TimeTableView {

    public PrintTimeTableView() {
        super.colWidth = 15.5;
        super.rowHeight = 42;
        super.tableX = colWidth * 4;
        super.tableY = rowHeight / 2;
        super.colHeader = new Font(9.5);
        super.rowHeader = new Font(12);
        super.thickStroke = 2;
        super.thinStroke = 1;
        super.dashedLine = true;
        this.draw();
    }
    @Override
    protected void draw() {
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
                txt.setX(tableX + (colWidth * i) + 3);
                txt.setY(rowHeight / 4 + 3);
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
            day.setX(4);
            day.setY(tableY + (rowHeight * i) + rowHeight / 2 + 4);
            this.getChildren().add(day);

            var line = new Line(tableX, tableY + (rowHeight * i), tableX + (colWidth * 40), tableY + (rowHeight * i));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(thinStroke);
            this.getChildren().add(line);
        }
    }

}
