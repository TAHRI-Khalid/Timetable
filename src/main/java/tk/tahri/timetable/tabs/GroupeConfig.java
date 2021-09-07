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
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.GroupeTimeTablePrint;

import java.util.Objects;

public class GroupeConfig extends Tab {

    private final Editor editor;
    private Groupe groupe;
    private final TableView<Groupe> table;
    private final TextField txtNom;
    private final TextField txtAnnee;
    private final TextField txtFiliere;
    private final ComboBox<String> comboNiveau;
    private final ComboBox<String> comboSecteur;
    private final ComboBox<String> comboType;

    public GroupeConfig(Editor editor) {
        super("Groupes");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label groupeTitle = new Label("Les Groupes " );
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

        grid.add(new Label("Filiére : "), 2, 0);
        txtFiliere = new TextField();
        GridPane.setHgrow(txtFiliere, Priority.ALWAYS);
        grid.add(txtFiliere, 3, 0);


        grid.add(new Label("Année : "), 0, 1);
        txtAnnee = new TextField();
        GridPane.setHgrow(txtAnnee, Priority.ALWAYS);
        grid.add(txtAnnee, 1, 1);

        grid.add(new Label("Niveau : "), 2, 1);
        comboNiveau = new ComboBox<>();
        GridPane.setHgrow(comboNiveau, Priority.ALWAYS);
        comboNiveau.getItems().addAll(Groupe.Niveaux);
        comboNiveau.getSelectionModel().select(0);
        grid.add(comboNiveau, 3, 1);


        grid.add(new Label("Secteur : "), 0, 2);
        comboSecteur = new ComboBox<>();
        GridPane.setHgrow(comboSecteur, Priority.ALWAYS);
        comboSecteur.getItems().addAll(Groupe.Secteurs);
        comboSecteur.getSelectionModel().select(0);
        grid.add(comboSecteur, 1, 2);


        grid.add(new Label("Type de formation : "), 2, 2);
        comboType = new ComboBox<>();
        GridPane.setHgrow(comboType, Priority.ALWAYS);
        comboType.getItems().addAll(Groupe.typeFormation);
        comboType.getSelectionModel().select(0);
        grid.add(comboType, 3, 2);

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

        var colNom = new TableColumn<Groupe, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colAnnee = new TableColumn<Groupe, String>("Année");
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("annee"));

        var colFiliere = new TableColumn<Groupe, String>("Filiére");
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiere"));

        var colNiveau = new TableColumn<Groupe, String>("Niveau");
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));

        var colSecteur = new TableColumn<Groupe, String>("Secteur");
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("secteur"));

        var colType = new TableColumn<Groupe, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        var colPrint = new TableColumn<Groupe, Long>("Imprimer");
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
                btn.setOnAction(event -> printGroupeTimetable(item));
            }
        });


        var colUpdate = new TableColumn<Groupe, Long>("Modifier");
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

        var colDelete = new TableColumn<Groupe, Long>("Supprimer");
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

        table.getColumns().addAll(colNom, colAnnee, colFiliere, colNiveau, colSecteur,colType,colPrint, colUpdate, colDelete);
        loadData();
        vBox.getChildren().addAll(groupeTitle,grid, hBox, table);
        setContent(vBox);

    }

    private void printGroupeTimetable(Long item) {
        var groupeTT = new GroupeTimeTablePrint(table.getItems().stream().filter(g-> Objects.equals(g.getId(), item)).findFirst().get());
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);
        groupeTT.setPrefWidth(pageLayout.getPrintableWidth()-40);

        if (job.showPrintDialog(table.getScene().getWindow())) {
            boolean success = job.printPage(groupeTT);
            if (success) {
                job.endJob();
            }
        }
    }

    private void loadData() {
        table.getItems().clear();
        table.getItems().addAll(Database.getInstance().getGroupes());
        editor.loadMenuGroupe();
    }

    private void clearForm() {
        txtNom.clear();
        txtAnnee.clear();
        txtFiliere.clear();
        comboNiveau.getSelectionModel().select(0);
        groupe = null;
    }

    private void saveForm() {
        if (!txtNom.getText().trim().isEmpty()) {
            if (groupe == null) {
                groupe = new Groupe(
                        txtNom.getText(),
                        Integer.parseInt(txtAnnee.getText()),
                        txtFiliere.getText(),
                        comboNiveau.getSelectionModel().getSelectedItem(),
                        comboSecteur.getSelectionModel().getSelectedItem(),
                        comboType.getSelectionModel().getSelectedItem()
                );
                Database.getInstance().execute("INSERT INTO groupe VALUES(null,'%s','%d','%s','%s','%s','%s')"
                        .formatted(groupe.getNom(), groupe.getAnnee(), groupe.getFiliere(), groupe.getNiveau(), groupe.getSecteur(),groupe.getType()));
            } else {
                groupe = new Groupe(
                        groupe.getId(),
                        txtNom.getText(),
                        Integer.parseInt(txtAnnee.getText()),
                        txtFiliere.getText(),
                        comboNiveau.getSelectionModel().getSelectedItem(),
                        comboSecteur.getSelectionModel().getSelectedItem(),
                        comboType.getSelectionModel().getSelectedItem()
                );
                Database.getInstance().execute("UPDATE groupe SET nom='%s' ,annee='%d',filiere='%s' ,niveau='%s' ,secteur='%s',type='%s' WHERE id=%d"
                        .formatted(groupe.getNom(), groupe.getAnnee(), groupe.getFiliere(), groupe.getNiveau(), groupe.getSecteur(),groupe.getType(), groupe.getId()));
            }
            loadData();
            clearForm();
        }
    }

    private void deleteItem(long id) {
        Database.getInstance().execute("DELETE FROM groupe WHERE id= '%d'".formatted(id));
        loadData();
    }

    private void updateForm(long id) {
        table.getItems().stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    groupe = e;
                    txtNom.setText(e.getNom());
                    txtFiliere.setText(e.getFiliere());
                    txtAnnee.setText(String.valueOf(e.getAnnee()));
                    comboNiveau.getSelectionModel().select(e.getNiveau());
                    comboSecteur.getSelectionModel().select(e.getSecteur());
                    comboType.getSelectionModel().select(e.getType());
                });
    }
}
