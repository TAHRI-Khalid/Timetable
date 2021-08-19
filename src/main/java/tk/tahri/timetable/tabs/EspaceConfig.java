package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Editor;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.dao.Database;

public class EspaceConfig extends Tab {

    private Espace espace;
    private final TableView<Espace> table;
    private final TextField txtNom;
    private final ComboBox<String> comboType;
    private final Editor editor;

    public EspaceConfig(Editor editor) {
        super("Espaces");
        this.editor=editor;
        FontAwesome fontAwesome = new FontAwesome();
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

        table.getColumns().addAll(colNom, colType, colUpdate, colDelete);
        loadData();
        vBox.getChildren().addAll(grid, hBox, table);
        setContent(vBox);

    }

    private void loadData() {
        table.getItems().clear();
        table.getItems().addAll(Database.getInstance().getEspaces());
        editor.loadMenuEspace();
    }

    private void clearForm() {
        txtNom.clear();
        comboType.getSelectionModel().select(0);
        espace = null;
    }

    private void saveForm() {
        if (!txtNom.getText().trim().isEmpty()) {
            if (espace == null) {
                espace = new Espace(txtNom.getText(), comboType.getSelectionModel().getSelectedItem());
                Database.getInstance().execute("INSERT INTO espace VALUES(null,'%s','%s')".formatted(espace.getNom(), espace.getType()));
            } else {
                espace = new Espace(espace.getId(), txtNom.getText(), comboType.getSelectionModel().getSelectedItem());
                Database.getInstance().execute("UPDATE espace SET nom='%s' , type='%s' WHERE id=%d".formatted(espace.getNom(), espace.getType(), espace.getId()));
            }
            loadData();
            clearForm();
        }
    }

    private void deleteItem(long id) {
        Database.getInstance().execute("DELETE FROM espace WHERE id= '%d'".formatted(id));
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
