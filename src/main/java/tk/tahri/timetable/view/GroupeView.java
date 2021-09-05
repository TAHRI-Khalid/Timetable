package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Groupe;

public class GroupeView extends Label {
    private final FontAwesome fontAwesome;
    private Groupe groupe;
    public GroupeView(Groupe groupe) {
        super(groupe.getNom());
        this.groupe = groupe;
        fontAwesome = new FontAwesome();
        this.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(16));
        this.getStyleClass().add("groupe-view");
    }

    public Groupe getGroupe() {
        return groupe;
    }
}
