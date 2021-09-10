package tk.tahri.timetable.view;

import javafx.scene.text.Font;
import tk.tahri.timetable.Entities.SeanceType;

public abstract class SubTimeTableView extends TimeTableView{

    public SubTimeTableView(SeanceType type) {
        super(type);
        super.colWidth = 12.40;
        super.rowHeight = 38;
        super.tableX = super.colWidth * 4;
        super.tableY = super.rowHeight / 2;
        super.colHeader = new Font(7.5);
        super.rowHeader = new Font(10);
        super.thickStroke = 2;
        super.dashedLine = false;
        super.timeHeaderX =tableX  + 2;
        super.timeHeaderY =rowHeight / 4 +2;
        super.dayHeaderX =3;
        super.dayHeaderY =tableY + rowHeight / 2 + 5;
        draw();
    }

}
