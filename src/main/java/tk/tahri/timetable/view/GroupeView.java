package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Groupe;

public class GroupeView extends Label {
    private final Groupe groupe;

    public GroupeView(Groupe groupe) {
        super(groupe.getNom());
        this.groupe = groupe;
        var fontAwesome = new FontAwesome();
        this.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(16));
        this.getStyleClass().add("groupe-view");
    }

    public Groupe getGroupe() {
        return groupe;
    }
}
