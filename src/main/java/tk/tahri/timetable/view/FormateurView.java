package tk.tahri.timetable.view;

import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Formateur;

public class FormateurView extends Label {
    private Formateur formateur;

    public FormateurView(Formateur formateur) {
        super(formateur.toString());
        this.formateur=formateur;

        FontAwesome fontAwesome = new FontAwesome();
        this.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(16));

         this.setStyle("""
                -fx-background-color: #038C4C;
                -fx-font-size: 14;
                -fx-text-fill: #fff;
                -fx-padding: 5px;
                """);
    }

    public Formateur getFormateur() {
        return formateur;
    }
}
