package tk.tahri.timetable.view;

import javafx.scene.text.Font;

public abstract class SubTimeTableView extends TimeTableView{

    public SubTimeTableView() {
        super.colWidth = 12.2;
        super.rowHeight = 38;
        tableX = super.colWidth * 4;
        tableY = super.rowHeight / 2;
        super.colHeader = new Font(8);
        super.rowHeader = new Font(11);
        super.thickStroke = 2;
        super.dashedLine = false;
        draw();
    }
}
