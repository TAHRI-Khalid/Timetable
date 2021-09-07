package tk.tahri.timetable.tabs;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;

public class DashboardTab extends EditorTab {
    private final FontAwesome fontAwesome = new FontAwesome();

    public DashboardTab(String nom) {
        super(nom);
        this.setClosable(false);
        var cards = new HBox(50);
        cards.setAlignment(Pos.CENTER);
        var lblFormateur = new Label("16");
        lblFormateur.setPrefSize(220, 120);
        lblFormateur.setStyle("""
                -fx-background-color: #038C4C;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;

                """);
        lblFormateur.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(52));
        lblFormateur.setGraphicTextGap(50);


        var lblGroupe = new Label("54");
        lblGroupe.setPrefSize(220, 120);
        lblGroupe.setStyle("""
                -fx-background-color: #1C588C;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblGroupe.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(52));
        lblGroupe.setGraphicTextGap(50);

        var lblEspace = new Label("12");
        lblEspace.setPrefSize(220, 120);
        lblEspace.setStyle("""
                -fx-background-color: #8D9AA6;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblEspace.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(52));
        lblEspace.setGraphicTextGap(50);


        var lblSeance = new Label("234");
        lblSeance.setPrefSize(220, 120);
        lblSeance.setStyle("""
                -fx-background-color: #e36b40;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblSeance.setGraphic(fontAwesome.create(FontAwesome.Glyph.CALENDAR).color(Color.WHITE).size(52));
        lblSeance.setGraphicTextGap(50);

        cards.getChildren().addAll(lblFormateur,lblGroupe,lblEspace,lblSeance);
        this.setContent(cards);
    }

    @Override
    protected void selectionChanged() {

    }

    @Override
    public void print() {

    }
}
