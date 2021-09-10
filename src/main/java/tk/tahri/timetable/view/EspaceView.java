package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;

public class EspaceView extends Label {
    private final FontAwesome fontAwesome;
    private final Espace espace;

    public EspaceView(Espace espace) {
        super(espace.toString());
        this.espace = espace;
        fontAwesome = new FontAwesome();
        this.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(16));
        this.getStyleClass().add("espace-view");
    }

    public Espace getEspace() {
        return espace;
    }
}
