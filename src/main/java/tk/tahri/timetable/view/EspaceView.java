package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;

public class EspaceView extends Label {
    private Espace espace;

    public EspaceView(Espace espace) {
        super(espace.toString());
        this.espace = espace;
        FontAwesome fontAwesome = new FontAwesome();
        this.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(16));

        this.setStyle("""
                -fx-background-color: #8D9AA6;
                -fx-font-size: 14;
                -fx-text-fill: #fff;
                -fx-padding: 5px;
                """);
    }

    public Espace getEspace() {
        return espace;
    }
}
