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

public class EtablissementParametre extends EditorTab {

    private Etablissement etablissement;
    private final TextField txtDirection;
    private final TextField txtComplexe;
    private final TextField txtNom;
    private final TextField txtAbreviation;
    private final PasswordField txtPassword;
    private final TextField txtMail;
    private final TextField txtCode;
    private final TextField txtAnneeDeFormation;
    private final DatePicker datePickerDebut;
    private final DatePicker datePickerFin;

    public EtablissementParametre(Editor editor) {
        super("Etablissement");
        FontAwesome fontAwesome = new FontAwesome();

        Label formateurTitle = new Label("Etablissement");
        formateurTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(26));
        formateurTitle.setGraphicTextGap(10);
        formateurTitle.setMaxWidth(Double.MAX_VALUE);
        formateurTitle.getStyleClass().add("formateur-title-main");

        var grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);

        grid.add(new Label("Direction : "), 0, 0);
        txtDirection = new TextField();
        GridPane.setHgrow(txtDirection, Priority.ALWAYS);
        grid.add(txtDirection, 1, 0);

        grid.add(new Label("Complexe : "), 0, 1);
        txtComplexe = new TextField();
        GridPane.setHgrow(txtComplexe, Priority.ALWAYS);
        grid.add(txtComplexe, 1, 1);

        grid.add(new Label("Etablissement : "), 0, 2);
        txtNom = new TextField();
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 2);

        grid.add(new Label("Abreviation : "), 0, 3);
        txtAbreviation = new TextField();
        GridPane.setHgrow(txtAbreviation, Priority.ALWAYS);
        grid.add(txtAbreviation, 1, 3);

        grid.add(new Label("Code : "), 0, 4);
        txtCode = new TextField();
        GridPane.setHgrow(txtCode, Priority.ALWAYS);
        grid.add(txtCode, 1, 4);

        grid.add(new Label("E-Mail : "), 0, 5);
        txtMail = new TextField();
        GridPane.setHgrow(txtMail, Priority.ALWAYS);
        grid.add(txtMail, 1, 5);

        grid.add(new Label("Mot de Passe : "), 0, 6);
        txtPassword = new PasswordField();
        GridPane.setHgrow(txtPassword, Priority.ALWAYS);
        grid.add(txtPassword, 1, 6);

        grid.add(new Label("Année de formation : "), 0, 7);
        txtAnneeDeFormation = new TextField();
        GridPane.setHgrow(txtAnneeDeFormation, Priority.ALWAYS);
        grid.add(txtAnneeDeFormation, 1, 7);

        grid.add(new Label("Date début : "), 0, 8);
        datePickerDebut = new DatePicker();
        GridPane.setHgrow(datePickerDebut, Priority.ALWAYS);
        grid.add(datePickerDebut, 1, 8);

        grid.add(new Label("Date fin : "), 0, 9);
        datePickerFin = new DatePicker();
        GridPane.setHgrow(datePickerFin, Priority.ALWAYS);
        grid.add(datePickerFin, 1, 9);

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
        clearForm();
    }

    private void clearForm() {
        txtDirection.setText(etablissement.getDirection());
        txtComplexe.setText(etablissement.getComplexe());
        txtAbreviation.setText(etablissement.getAbreviation());
        txtCode.setText(etablissement.getCode());
        txtAnneeDeFormation.setText(etablissement.getAnneeDeFormation());
        txtNom.setText(etablissement.getNom());
        datePickerDebut.setValue(etablissement.getDateDebut());
        datePickerFin.setValue(etablissement.getDateFin());
        txtMail.setText(etablissement.getUsername());
        txtPassword.setText(etablissement.getPassword());
    }

    private void saveForm() {
        Database.getInstance().execute("UPDATE etablissement SET direction=?, complexe=?, nom=?, abreviation=?, code=?, annee=?,username=?,password=?, dateDebut=?, dateFin=? WHERE id=?",
                txtDirection.getText(),
                txtComplexe.getText(),
                txtNom.getText(),
                txtAbreviation.getText(),
                txtCode.getText(),
                txtAnneeDeFormation.getText(),
                txtMail.getText(),
                txtPassword.getText(),
                datePickerDebut.getEditor().getText().isEmpty() ? null : datePickerDebut.getValue().toString(),
                datePickerFin.getEditor().getText().isEmpty() ? null : datePickerFin.getValue().toString(),
                String.valueOf(etablissement.getId()));
        loadData();
    }

    @Override
    protected void selectionChanged() {

    }

    @Override
    public void print() {

    }
}
