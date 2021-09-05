module tk.tahri.TimeTable {
    requires javafx.controls;
    requires java.sql;
    requires org.controlsfx.controls;

    exports tk.tahri.timetable;
    exports tk.tahri.timetable.dao;
    exports tk.tahri.timetable.tabs;
    exports tk.tahri.timetable.Entities;
    exports tk.tahri.timetable.print;
    exports tk.tahri.timetable.view;
}