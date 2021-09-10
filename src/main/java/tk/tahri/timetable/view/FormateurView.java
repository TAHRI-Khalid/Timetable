package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Formateur;

public class FormateurView extends Label {
    private final FontAwesome fontAwesome;
    private final Formateur formateur;

    public FormateurView(Formateur formateur) {
        super(formateur.toString());
        this.formateur=formateur;
        fontAwesome = new FontAwesome();
        this.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(16));
        this.getStyleClass().add("formateur-view");
    }

    public void setSelectedStyle(boolean b) {
        this.getStyleClass().clear();
        if(b){
            this.getStyleClass().add("formateur-view-selected");
            this.setGraphic(fontAwesome.create(FontAwesome.Glyph.CHECK_CIRCLE).color(Color.web("#038C4C")).size(18));
        }else {
            this.getStyleClass().add("formateur-view");
            this.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(16));
        }
    }

    public Formateur getFormateur() {
        return formateur;
    }
}
