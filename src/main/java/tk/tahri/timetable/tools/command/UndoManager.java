package tk.tahri.timetable.tools.command;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
    private static final UndoManager instance = new UndoManager();
    private static final int MAX_HISTORY = 50;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private final BooleanProperty canRedo = new SimpleBooleanProperty(false);
    private final StringProperty lastUndoDescription = new SimpleStringProperty("");
    private final StringProperty lastRedoDescription = new SimpleStringProperty("");

    public static UndoManager getInstance() {
        return instance;
    }

    private UndoManager() {
    }

    public synchronized void execute(Command cmd) {
        if (cmd == null) return;
        cmd.execute();
        pushExecuted(cmd);
    }

    public synchronized void pushExecuted(Command cmd) {
        if (cmd == null) return;
        if (undoStack.size() >= MAX_HISTORY) {
            undoStack.removeLast();
        }
        undoStack.push(cmd);
        redoStack.clear();
        updateProperties();
    }

    public synchronized void undo() {
        if (undoStack.isEmpty()) return;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        updateProperties();
    }

    public synchronized void redo() {
        if (redoStack.isEmpty()) return;
        Command cmd = redoStack.pop();
        cmd.redo();
        undoStack.push(cmd);
        updateProperties();
    }

    public synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
        updateProperties();
    }

    private void updateProperties() {
        canUndo.set(!undoStack.isEmpty());
        canRedo.set(!redoStack.isEmpty());
        lastUndoDescription.set(undoStack.isEmpty() ? "" : undoStack.peek().getDescription());
        lastRedoDescription.set(redoStack.isEmpty() ? "" : redoStack.peek().getDescription());
    }

    public BooleanProperty canUndoProperty() {
        return canUndo;
    }

    public BooleanProperty canRedoProperty() {
        return canRedo;
    }

    public boolean canUndo() {
        return canUndo.get();
    }

    public boolean canRedo() {
        return canRedo.get();
    }

    public String getLastUndoDescription() {
        return lastUndoDescription.get();
    }

    public String getLastRedoDescription() {
        return lastRedoDescription.get();
    }
}
