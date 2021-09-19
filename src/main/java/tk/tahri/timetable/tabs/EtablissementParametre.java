package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Editor;
import tk.tahri.timetable.Entities.Etablissement;
import tk.tahri.timetable.dao.Database;

public class EtablissementParametre extends Tab {

    private Etablissement etablissement;
    private final TextField txtNom;
    private final TextField txtAbreviation;
    private final TextField txtCode;
    private final Editor editor;

    public EtablissementParametre(Editor editor) {
        super("Etablissement");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label formateurTitle = new Label("Etablissement");
        formateurTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(26));
        formateurTitle.setGraphicTextGap(10);
        formateurTitle.setMaxWidth(Double.MAX_VALUE);
        formateurTitle.getStyleClass().add("formateur-title-main");


        var grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);

        grid.add(new Label("Nom : "), 0, 0);
        txtNom = new TextField();
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("abreviation : "), 0, 1);
        txtAbreviation = new TextField();
        GridPane.setHgrow(txtAbreviation, Priority.ALWAYS);
        grid.add(txtAbreviation, 1, 1);

        grid.add(new Label("Matricule : "), 0, 2);
        txtCode = new TextField();
        GridPane.setHgrow(txtCode, Priority.ALWAYS);
        grid.add(txtCode, 1, 2);


        var vBox = new VBox(10);
        vBox.setPadding(new Insets(20));
        var hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        var btnSave = new Button("Enregistrer");
        btnSave.setOnAction(actionEvent -> saveForm());

        var btnCancel = new Button("Annuler");
        btnCancel.setOnAction(actionEvent -> clearForm());

        hBox.getChildren().addAll(btnSave, btnCancel);

        loadData();
        vBox.getChildren().addAll(formateurTitle, grid, hBox);
        setContent(vBox);

    }

    private void loadData() {
        etablissement = Database.getInstance().getItablissment();
        txtAbreviation.setText(etablissement.getAbreviation());
        txtCode.setText(etablissement.getCode());
        txtNom.setText(etablissement.getNom());
    }

    private void clearForm() {
        txtAbreviation.setText(etablissement.getAbreviation());
        txtCode.setText(etablissement.getCode());
        txtNom.setText(etablissement.getNom());
    }

    private void saveForm() {
        Database.getInstance().execute("UPDATE etablissement SET nom=? ,abreviation=? ,code=? WHERE id=?",
                txtNom.getText(), txtAbreviation.getText(), txtCode.getText(), String.valueOf(etablissement.getId()));
        loadData();
    }

}
