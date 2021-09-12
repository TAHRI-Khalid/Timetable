module timeTable {
    requires java.sql;
    requires javafx.controls;
    requires org.controlsfx.controls;
    requires org.xerial.sqlitejdbc;

    exports tk.tahri.timetable;
    exports tk.tahri.timetable.dao;
    exports tk.tahri.timetable.tabs;
    exports tk.tahri.timetable.Entities;
    exports tk.tahri.timetable.print;
    exports tk.tahri.timetable.view;
}