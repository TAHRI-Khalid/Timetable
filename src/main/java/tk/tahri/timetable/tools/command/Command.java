package tk.tahri.timetable.tools.command;

public interface Command {
    void execute();
    void undo();
    default void redo() {
        execute();
    }
    String getDescription();
}
