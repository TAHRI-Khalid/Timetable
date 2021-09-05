package tk.tahri.timetable.tabs;

import javafx.scene.control.Tab;

public abstract class EditorTab extends Tab {
    public EditorTab(String nom) {
        super(nom);
        this.setOnSelectionChanged(event -> selectionChanged());
    }

    protected abstract void selectionChanged();

    public abstract void print();

}
