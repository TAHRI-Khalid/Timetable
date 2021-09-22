package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Editor;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.FormateurTimeTablePrint;

import java.util.Objects;

public class FormateurParametre extends Tab {

    private Formateur formateur;
    private final TableView<Formateur> table;
    private final TextField txtNom;
    private final TextField txtPrenom;
    private final TextField txtMatricule;
    private final ComboBox<String> comboType;
    private final Editor editor;

    public FormateurParametre(Editor editor) {
        super("Formateurs");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label formateurTitle = new Label("Les Fromateurs");
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

        grid.add(new Label("Prénom : "), 0, 1);
        txtPrenom = new TextField();
        GridPane.setHgrow(txtPrenom, Priority.ALWAYS);
        grid.add(txtPrenom, 1, 1);

        grid.add(new Label("Matricule : "), 0, 2);
        txtMatricule = new TextField();
        GridPane.setHgrow(txtMatricule, Priority.ALWAYS);
        grid.add(txtMatricule, 1, 2);

        grid.add(new Label("Type : "), 0, 3);
        comboType = new ComboBox<String>();
        GridPane.setHgrow(comboType, Priority.ALWAYS);
        comboType.getItems().addAll(Formateur.Types);
        comboType.getSelectionModel().select(0);
        grid.add(comboType, 1, 3);

        var vBox = new VBox(10);
        vBox.setPadding(new Insets(20));
        var hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        var btnSave = new Button("Enregistrer");
        btnSave.setOnAction(actionEvent -> saveForm());

        var btnCancel = new Button("Annuler");
        btnCancel.setOnAction(actionEvent -> clearForm());

        hBox.getChildren().addAll(btnSave, btnCancel);

        table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        var colNom = new TableColumn<Formateur, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colPrenom = new TableColumn<Formateur, String>("Prénom");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        var colMatricule = new TableColumn<Formateur, String>("Matricule");
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));

        var colType = new TableColumn<Formateur, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));


        var colPrint = new TableColumn<Formateur, Long>("Imprimer");
        colPrint.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPrint.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.PRINT).color(Color.WHITE).size(16));
                btn.getStyleClass().add("btn-print");
                setGraphic(btn);
                btn.setOnAction(event -> printFormateurTimetable(item));
            }
        });

        var colUpdate = new TableColumn<Formateur, Long>("Modifier");
        colUpdate.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUpdate.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.PENCIL).color(Color.WHITE).size(16));
                btn.getStyleClass().add("btn-save");
                setGraphic(btn);
                btn.setOnAction(event -> updateForm(item));
            }
        });

        var colDelete = new TableColumn<Formateur, Long>("Supprimer");
        colDelete.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDelete.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.TRASH).color(Color.WHITE).size(16));
                btn.getStyleClass().add("btn-delete");
                setGraphic(btn);
                btn.setOnAction(event -> deleteItem(item));
            }
        });

        table.getColumns().addAll(colNom, colPrenom, colMatricule, colType, colPrint, colUpdate, colDelete);
        loadData();
        vBox.getChildren().addAll(formateurTitle, grid, hBox, table);
        setContent(vBox);

    }

    private void printFormateurTimetable(Long item) {
        var groupeTT = new FormateurTimeTablePrint(table.getItems().stream().filter(g -> Objects.equals(g.getId(), item)).findFirst().get());
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);
        groupeTT.setPrefWidth(pageLayout.getPrintableWidth());

        if (job.showPrintDialog(table.getScene().getWindow())) {
            boolean success = job.printPage(groupeTT);
            if (success) {
                job.endJob();
            }
        }
    }

    private void loadData() {
        table.getItems().clear();
        table.getItems().addAll(Database.getInstance().getFormateurs());
        editor.loadMenuFormateur();
    }

    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtMatricule.clear();
        formateur = null;
    }

    private void saveForm() {
        if (!txtNom.getText().trim().isEmpty()) {
            if (formateur == null) {
                formateur = new Formateur(txtNom.getText(), txtPrenom.getText(), txtMatricule.getText(), comboType.getSelectionModel().getSelectedItem());
                Database.getInstance().execute("INSERT INTO formateur VALUES(null,?,?,?,?)",
                        formateur.getNom(), formateur.getPrenom(), formateur.getMatricule(), formateur.getType());
            } else {
                formateur = new Formateur(formateur.getId(), txtNom.getText(), txtPrenom.getText(), txtMatricule.getText(), comboType.getSelectionModel().getSelectedItem());
                Database.getInstance().execute("UPDATE formateur SET nom=? ,prenom=? ,matricule=? ,type=? WHERE id=?",
                        formateur.getNom(), formateur.getPrenom(), formateur.getMatricule(), formateur.getType(), String.valueOf(formateur.getId()));
            }
            loadData();
            clearForm();
        }
    }

    private void deleteItem(long id) {
        Database.getInstance().execute("DELETE FROM formateur WHERE id= ?", String.valueOf(id));
        loadData();
    }

    private void updateForm(long id) {
        table.getItems().stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    formateur = e;
                    txtNom.setText(e.getNom());
                    txtMatricule.setText(e.getMatricule());
                    txtPrenom.setText(e.getPrenom());
                    comboType.getSelectionModel().select(e.getType());
                });
    }
}
