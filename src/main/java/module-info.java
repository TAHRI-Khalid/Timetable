module timetable {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires org.xerial.sqlitejdbc;
    requires jakarta.mail;

    exports tk.tahri.timetable;
    exports tk.tahri.timetable.dao;
    exports tk.tahri.timetable.tabs;
    exports tk.tahri.timetable.Entities;
    exports tk.tahri.timetable.print;
    exports tk.tahri.timetable.view;
}