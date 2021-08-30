package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;

public class EspaceView extends Label {
    private final FontAwesome fontAwesome;
    private Espace espace;

    public EspaceView(Espace espace) {
        super(espace.toString());
        this.espace = espace;
        fontAwesome = new FontAwesome();
        this.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(16));
        this.getStyleClass().add("espace-view");
    }
    public void setSelectedStyle(boolean b) {
        this.getStyleClass().clear();
        if(b){
            this.getStyleClass().add("espace-view-selected");
            this.setGraphic(fontAwesome.create(FontAwesome.Glyph.CHECK_CIRCLE).color(Color.web("#8D9AA6")).size(18));
        }else {
            this.getStyleClass().add("espace-view");
            this.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(16));
        }
    }
    public Espace getEspace() {
        return espace;
    }
}
