package tk.tahri.timetable.view;

import javafx.scene.text.Font;
import tk.tahri.timetable.Entities.SeanceType;

public abstract class SubTimeTableView extends TimeTableView{

    public SubTimeTableView(SeanceType type) {
        super(type);
        super.colWidth = 12.2;
        super.rowHeight = 38;
        super.tableX = super.colWidth * 4;
        super.tableY = super.rowHeight / 2;
        super.colHeader = new Font(8);
        super.rowHeader = new Font(11);
        super.thickStroke = 2;
        super.dashedLine = false;
        draw();
    }

}
