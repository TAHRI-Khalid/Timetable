module timetable {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires org.xerial.sqlitejdbc;
    requires jakarta.mail;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.compress;

    exports tk.tahri.timetable;
    exports tk.tahri.timetable.dao;
    exports tk.tahri.timetable.tabs;
    exports tk.tahri.timetable.Entities;
    exports tk.tahri.timetable.print;
    exports tk.tahri.timetable.view;
}