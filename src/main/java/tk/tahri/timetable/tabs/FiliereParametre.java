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
import tk.tahri.timetable.Entities.Filiere;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.GroupeTimeTablePrint;

import java.util.Objects;

public class FiliereParametre extends Tab {

    private final Editor editor;
    private Filiere filiere;
    private final TableView<Filiere> table;
    private final TextField txtNom;
    private final TextField txtAbreviation;
    private final ComboBox<String> comboNiveau;
    private final ComboBox<String> comboSecteur;
    private final ComboBox<String> comboType;

    public FiliereParametre(Editor editor) {
        super("Filières");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label groupeTitle = new Label("Les Filières ");
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(26));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");


        var grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);

        grid.add(new Label("Nom : "), 0, 0);
        txtNom = new TextField();
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Abréviation : "), 2, 0);
        txtAbreviation = new TextField();
        GridPane.setHgrow(txtAbreviation, Priority.ALWAYS);
        grid.add(txtAbreviation, 3, 0);


        grid.add(new Label("Niveau : "), 0, 1);
        comboNiveau = new ComboBox<>();
        GridPane.setHgrow(comboNiveau, Priority.ALWAYS);
        comboNiveau.getItems().addAll(Filiere.Niveaux);
        comboNiveau.getSelectionModel().select(0);
        grid.add(comboNiveau, 1, 1);

        grid.add(new Label("Secteur : "), 2, 1);
        comboSecteur = new ComboBox<>();
        GridPane.setHgrow(comboSecteur, Priority.ALWAYS);
        comboSecteur.getItems().addAll(Filiere.Secteurs);
        comboSecteur.getSelectionModel().select(0);
        grid.add(comboSecteur, 3, 1);


        grid.add(new Label("Type de formation : "), 0, 2);
        comboType = new ComboBox<>();
        GridPane.setHgrow(comboType, Priority.ALWAYS);
        comboType.getItems().addAll(Filiere.typeFormation);
        comboType.getSelectionModel().select(0);
        grid.add(comboType, 1, 2);

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

        var colNom = new TableColumn<Filiere, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colAbreviation = new TableColumn<Filiere, String>("Abréviation");
        colAbreviation.setCellValueFactory(new PropertyValueFactory<>("abreviation"));

        var colNiveau = new TableColumn<Filiere, String>("Niveau");
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));

        var colSecteur = new TableColumn<Filiere, String>("Secteur");
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("secteur"));

        var colType = new TableColumn<Filiere, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        var colUpdate = new TableColumn<Filiere, Long>("Modifier");
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

        var colDelete = new TableColumn<Filiere, Long>("Supprimer");
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

        table.getColumns().addAll(colNom, colAbreviation, colNiveau, colSecteur, colType, colUpdate, colDelete);
        loadData();
        vBox.getChildren().addAll(groupeTitle, grid, hBox, table);
        setContent(vBox);

    }

    private void loadData() {
        table.getItems().clear();
        table.getItems().addAll(Database.getInstance().getFilieres());
        editor.loadMenuGroupe();
    }

    private void clearForm() {
        txtNom.clear();
        txtAbreviation.clear();
        filiere = null;
    }

    private void saveForm() {
        if (!txtNom.getText().trim().isEmpty()) {
            if (filiere == null) {
                filiere = new Filiere(
                        txtNom.getText(),
                        txtAbreviation.getText(),
                        comboNiveau.getSelectionModel().getSelectedItem(),
                        comboSecteur.getSelectionModel().getSelectedItem(),
                        comboType.getSelectionModel().getSelectedItem()
                );
                Database.getInstance().execute("INSERT INTO filiere VALUES(null,?,?,?,?,?)",
                        filiere.getNom(), filiere.getAbreviation(), filiere.getNiveau(), filiere.getSecteur(), filiere.getType());
            } else {
                filiere = new Filiere(
                        filiere.getId(),
                        txtNom.getText(),
                        txtAbreviation.getText(),
                        comboNiveau.getSelectionModel().getSelectedItem(),
                        comboSecteur.getSelectionModel().getSelectedItem(),
                        comboType.getSelectionModel().getSelectedItem()
                );
                Database.getInstance().execute("UPDATE filiere SET nom=? ,abreviation=? ,niveau=? ,secteur=?,type=? WHERE id=?",
                        filiere.getNom(), filiere.getAbreviation(), filiere.getNiveau(), filiere.getSecteur(), filiere.getType(), String.valueOf(filiere.getId()));
            }
            loadData();
            clearForm();
        }
    }

    private void deleteItem(long id) {
        Database.getInstance().execute("DELETE FROM filiere WHERE id=?", String.valueOf(id));
        loadData();
    }

    private void updateForm(long id) {
        table.getItems().stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    filiere = e;
                    txtNom.setText(e.getNom());
                    txtAbreviation.setText(e.getAbreviation());
                    comboNiveau.getSelectionModel().select(e.getNiveau());
                    comboSecteur.getSelectionModel().select(e.getSecteur());
                    comboType.getSelectionModel().select(e.getType());
                });
    }
}
