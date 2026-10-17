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
import tk.tahri.timetable.tools.NotificationService;

public class EtablissementParametre extends EditorTab {

    private Etablissement etablissement;
    private final TextField txtDirection;
    private final TextField txtComplexe;
    private final TextField txtNom;
    private final TextField txtAbreviation;
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
        grid.setVgap(12);
        grid.setHgap(20);

        // Colonne 1 : Informations institutionnelles
        grid.add(new Label("Direction : "), 0, 0);
        txtDirection = new TextField();
        txtDirection.setPromptText("Ex: Direction Régionale...");
        GridPane.setHgrow(txtDirection, Priority.ALWAYS);
        grid.add(txtDirection, 1, 0);

        grid.add(new Label("Complexe : "), 0, 1);
        txtComplexe = new TextField();
        txtComplexe.setPromptText("Ex: Complexe Formation...");
        GridPane.setHgrow(txtComplexe, Priority.ALWAYS);
        grid.add(txtComplexe, 1, 1);

        grid.add(new Label("Établissement : "), 0, 2);
        txtNom = new TextField();
        txtNom.setPromptText("Nom de l'établissement");
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 2);

        grid.add(new Label("Abréviation : "), 0, 3);
        txtAbreviation = new TextField();
        txtAbreviation.setPromptText("Ex: ISTA, CFP...");
        GridPane.setHgrow(txtAbreviation, Priority.ALWAYS);
        grid.add(txtAbreviation, 1, 3);

        grid.add(new Label("Code : "), 0, 4);
        txtCode = new TextField();
        txtCode.setPromptText("Code établissement");
        GridPane.setHgrow(txtCode, Priority.ALWAYS);
        grid.add(txtCode, 1, 4);

        // Colonne 2 : Année et Période
        grid.add(new Label("Année de formation : "), 2, 0);
        txtAnneeDeFormation = new TextField();
        txtAnneeDeFormation.setPromptText("Ex: 2026/2027");
        GridPane.setHgrow(txtAnneeDeFormation, Priority.ALWAYS);
        grid.add(txtAnneeDeFormation, 3, 0);

        grid.add(new Label("Date début : "), 2, 1);
        datePickerDebut = new DatePicker();
        datePickerDebut.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(datePickerDebut, Priority.ALWAYS);
        grid.add(datePickerDebut, 3, 1);

        grid.add(new Label("Date fin : "), 2, 2);
        datePickerFin = new DatePicker();
        datePickerFin.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(datePickerFin, Priority.ALWAYS);
        grid.add(datePickerFin, 3, 2);

        // Panneau de formulaire avec en-tête
        var formCard = new VBox(15);
        formCard.getStyleClass().add("form-card");

        var formHeader = new HBox(10);
        formHeader.setAlignment(Pos.CENTER_LEFT);
        formHeader.getStyleClass().add("form-header-box");

        var lblFormTitle = new Label("Configuration de l'Établissement et Paramètres Globaux");
        lblFormTitle.getStyleClass().add("form-mode-title");
        formHeader.getChildren().add(lblFormTitle);

        var btnSave = new Button("Enregistrer les modifications");
        btnSave.getStyleClass().add("btn-action-primary");
        btnSave.setOnAction(actionEvent -> saveForm());

        var btnCancel = new Button("Rétablir");
        btnCancel.getStyleClass().add("btn-action-secondary");
        btnCancel.setOnAction(actionEvent -> clearForm());

        var hBox = new HBox(10, btnSave, btnCancel);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        formCard.getChildren().addAll(formHeader, grid, hBox);

        loadData();

        var vBox = new VBox(15);
        vBox.setPadding(new Insets(15));
        vBox.getChildren().addAll(formateurTitle, formCard);
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
    }

    private void saveForm() {
        if (datePickerDebut.getValue() != null && datePickerFin.getValue() != null
                && datePickerDebut.getValue().isAfter(datePickerFin.getValue())) {
            NotificationService.showWarning("Dates incohérentes", "La date de début ne peut pas être postérieure à la date de fin.");
            return;
        }

        String username = etablissement.getUsername() != null ? etablissement.getUsername() : "";
        String password = etablissement.getPassword() != null ? etablissement.getPassword() : "";

        Database.getInstance().execute("UPDATE etablissement SET direction=?, complexe=?, nom=?, abreviation=?, code=?, annee=?,username=?,password=?, dateDebut=?, dateFin=? WHERE id=?",
                txtDirection.getText(),
                txtComplexe.getText(),
                txtNom.getText(),
                txtAbreviation.getText(),
                txtCode.getText(),
                txtAnneeDeFormation.getText(),
                username,
                password,
                datePickerDebut.getEditor().getText().isEmpty() ? null : datePickerDebut.getValue().toString(),
                datePickerFin.getEditor().getText().isEmpty() ? null : datePickerFin.getValue().toString(),
                String.valueOf(etablissement.getId()));
        loadData();
        NotificationService.showSuccess("Établissement", "Les paramètres de l'établissement ont été enregistrés avec succès.");
    }

    @Override
    protected void selectionChanged() {

    }

    @Override
    public void print() {

    }
}
