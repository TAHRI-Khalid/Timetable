package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Editor;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.EspaceTimeTablePrint;
import tk.tahri.timetable.tools.Config;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class EspaceParametre extends Tab {

    private Espace espace;
    private final TableView<Espace> table;
    private final TextField txtNom;
    private final ComboBox<String> comboType;
    private final Editor editor;

    public EspaceParametre(Editor editor) {
        super("Espaces");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label espaceTitle = new Label("Les Espaces");
        espaceTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(26));
        espaceTitle.setGraphicTextGap(10);
        espaceTitle.setMaxWidth(Double.MAX_VALUE);
        espaceTitle.getStyleClass().add("espace-title-main");

        var grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);

        grid.add(new Label("Nom : "), 0, 0);
        txtNom = new TextField();
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Type : "), 0, 1);
        comboType = new ComboBox<String>();
        GridPane.setHgrow(comboType, Priority.ALWAYS);
        comboType.getItems().addAll(Espace.Types);
        comboType.getSelectionModel().select(0);
        grid.add(comboType, 1, 1);

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

        var colNom = new TableColumn<Espace, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colType = new TableColumn<Espace, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        var colPrint = new TableColumn<Espace, Long>("Imprimer");
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
                btn.setOnAction(event -> printEspaceTimetable(item));
            }
        });

        var colUpdate = new TableColumn<Espace, Long>("Modifier");
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

        var colDelete = new TableColumn<Espace, Long>("Supprimer");
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

        table.getColumns().addAll(colNom, colType, colPrint, colUpdate, colDelete);
        loadData();
        vBox.getChildren().addAll(espaceTitle, grid, hBox, table);
        setContent(vBox);

    }

    private void printEspaceTimetable(Long item) {
        var groupeTT = new EspaceTimeTablePrint(table.getItems().stream().filter(g -> Objects.equals(g.getId(), item)).findFirst().get());
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);
        groupeTT.setPrefWidth(pageLayout.getPrintableWidth() - 40);

        if (job.showPrintDialog(table.getScene().getWindow())) {
            boolean success = job.printPage(groupeTT);
            if (success) {
                job.endJob();
            }
        }
    }

    private void loadData() {
        table.getItems().clear();
        table.getItems().addAll(Database.getInstance().getEspaces());
        editor.loadMenuEspace();
    }

    private void clearForm() {
        txtNom.clear();
        espace = null;
    }

    private void saveForm() {
        if (!txtNom.getText().trim().isEmpty()) {
            if (espace == null) {
                espace = new Espace(txtNom.getText(), comboType.getSelectionModel().getSelectedItem());
                Database.getInstance().execute("INSERT INTO espace VALUES(null,?,?)", espace.getNom(), espace.getType());
            } else {
                espace = new Espace(espace.getId(), txtNom.getText(), comboType.getSelectionModel().getSelectedItem());
                Database.getInstance().execute("UPDATE espace SET nom=? , type=? WHERE id=?",
                        espace.getNom(), espace.getType(), String.valueOf(espace.getId()));
            }
            loadData();
            clearForm();
        }
    }

    private void deleteItem(long id) {
        Database.getInstance().execute("DELETE FROM espace WHERE id= ?", String.valueOf(id));
        loadData();
    }

    private void updateForm(long id) {
        table.getItems().stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    espace = e;
                    txtNom.setText(e.getNom());
                    comboType.getSelectionModel().select(e.getType());
                });
    }
}
