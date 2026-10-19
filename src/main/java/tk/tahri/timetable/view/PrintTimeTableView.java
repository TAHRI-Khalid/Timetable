package tk.tahri.timetable.view;

import javafx.scene.text.Font;
import tk.tahri.timetable.Entities.SeanceType;

public abstract class PrintTimeTableView extends TimeTableView {

    public PrintTimeTableView(SeanceType type) {
        super(type);
        super.colWidth = 15.5;
        super.rowHeight = 42;
        super.tableX = colWidth * 4;
        super.tableY = rowHeight / 2;
        super.colHeader = new Font(9.5);
        super.rowHeader = new Font(12);
        super.thickStroke = 2;
        super.thinStroke = 1;
        super.dashedLine = true;
        super.timeHeaderX = tableX + 3;
        super.timeHeaderY = rowHeight / 4 + 4;
        super.dayHeaderX = 4;
        super.dayHeaderY = tableY + rowHeight / 2 + 4;
        this.draw();

        double totalW = tableX + (colWidth * totalCols);
        double totalH = tableY + (rowHeight * 6);
        this.setPrefSize(totalW, totalH);
        this.setMinSize(totalW, totalH);
        this.setMaxSize(totalW, totalH);
    }
}

