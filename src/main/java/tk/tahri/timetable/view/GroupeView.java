package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class GroupeView extends Label {
    public GroupeView(String s) {
        super(s);


        this.setStyle("""
                        -fx-background-color: #8D9AA6;
                        -fx-font-size: 14;
                        -fx-text-fill: #fff;
                        -fx-padding: 5px;
                        """);
    }
}
