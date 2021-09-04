package tk.tahri.timetable.tabs;

import javafx.scene.control.Tab;

public abstract class EditorTab extends Tab {
    public EditorTab(String nom) {
        super(nom);
    }

    public abstract void print();

    public abstract void refreshTabView();
}
