package tk.tahri.timetable;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tabs.*;
import tk.tahri.timetable.tools.Config;

import java.io.IOException;
import java.nio.file.*;

public class Editor extends VBox {

    private TabPane tabpane;
    private MenuBar menu;
    private final Menu mFichier = new Menu("Fichier");
    private final Menu mGroupe = new Menu("Groupes");
    private final Menu mFormateur = new Menu("Formateurs");
    private final Menu mEspace = new Menu("Espaces");
    private final Menu mParametre = new Menu("Paramètres");

    public Editor() {
        createMenu();
        createTabpane();
        DashboardTab tab = new DashboardTab("Acceuil");
        tabpane.getTabs().add(tab);
    }

    private void createTabpane() {
        tabpane = new TabPane();
        tabpane.setSide(Side.BOTTOM);
        VBox.setVgrow(tabpane, Priority.ALWAYS);
        getChildren().addAll(tabpane);
    }

    private void createMenu() {
        menu = new MenuBar();
        menu.getMenus().addAll(mFichier, mGroupe, mFormateur, mEspace);

        var mNouveau = new MenuItem("Nouveau");
        var nkeyCombination = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        mNouveau.setAccelerator(nkeyCombination);
        mNouveau.setOnAction(actionEvent -> onMenuNew());

        var mOuvrir = new MenuItem("Ouvrir");
        var okeyCombination = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        mOuvrir.setAccelerator(okeyCombination);
        mOuvrir.setOnAction(actionEvent -> onMenuOpen());

        var mEnregister = new MenuItem("Enregister");
        var skeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        mEnregister.setAccelerator(skeyCombination);
        mEnregister.setOnAction(actionEvent -> onMenuSave());

        var mEnregisterSous = new MenuItem("Enregister sous ...");
        var smkeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        mEnregisterSous.setAccelerator(smkeyCombination);
        mEnregisterSous.setOnAction(actionEvent -> onMenuSaveAS());

        var mPrint = new MenuItem("Imprimer");
        var pkeyCombination = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        mPrint.setAccelerator(pkeyCombination);
        mPrint.setOnAction(event -> print());

        var mPrintCenter = new MenuItem("Espace d'impression");
        var pckeyCombination = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        mPrintCenter.setAccelerator(pckeyCombination);
        mPrintCenter.setOnAction(event -> {
            PrintTab printTab = new PrintTab("Espace d'impression");
            loadTab(printTab);

        });

        var mQuitter = new MenuItem("Quitter");
        var qkeyCombination = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        mQuitter.setAccelerator(qkeyCombination);
        mQuitter.setOnAction(event -> ((Stage) this.getScene().getWindow()).close());

        mFichier.getItems().addAll(mNouveau, mOuvrir, mEnregister, mEnregisterSous, new SeparatorMenuItem(), mPrint, mPrintCenter, new SeparatorMenuItem(), mQuitter);

        if (!Config.getProperty("file").trim().isEmpty()) {
            loadMenuGroupe();
            loadMenuFormateur();
            loadMenuEspace();
        }

        var mEtablissement = new MenuItem("Etablissement");

        var mFiliere = new MenuItem("Filières");
        mFiliere.setOnAction(event -> {
            loadTab(new FiliereConfig(this));
        });

        var mParamGroupe = new MenuItem("Groupes");
        var gkeyCombination = new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN);
        mParamGroupe.setAccelerator(gkeyCombination);
        mParamGroupe.setOnAction(event -> {
            loadTab(new GroupeConfig(this));
        });

        var mParamFormateur = new MenuItem("Formateurs");
        var fkeyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        mParamFormateur.setAccelerator(fkeyCombination);
        mParamFormateur.setOnAction(event -> {
            loadTab(new FormateurConfig(this));
        });

        var mParamEspace = new MenuItem("Espace");
        var ekeyCombination = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
        mParamEspace.setAccelerator(ekeyCombination);
        mParamEspace.setOnAction(event -> {
            loadTab(new EspaceConfig(this));
        });

        mParametre.getItems().addAll(mEtablissement,mFiliere,mParamGroupe,mParamFormateur,mParamEspace);
        menu.getMenus().add(mParametre);
        getChildren().addAll(menu);
    }

    private void onMenuNew() {
        try {
            var modified = Boolean.parseBoolean(Config.getProperty("modified"));
            if (modified) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Attension");
                alert.setContentText("le fichier a ete modifié voulez vous le sauvegarder ?");
                ButtonType okButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
                alert.getButtonTypes().setAll(okButton, noButton);
                alert.showAndWait().ifPresent(type -> {
                    if (type == okButton) {
                        onMenuSave();
                    }
                });
            }

            var dest = Files.createTempFile("temp", ".tk");
            Config.setProperty("file", "");
            Config.setProperty("temp", dest.toString());
            Config.setProperty("modified", Boolean.TRUE.toString());

            Database.getInstance().createDatabase();
            tabpane.getTabs().clear();
            tabpane.getTabs().add(new DashboardTab("Accueil"));
            loadMenuGroupe();
            loadMenuFormateur();
            loadMenuEspace();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onMenuSave() {
        if (Config.getProperty("file").trim().isEmpty()) {
            onMenuSaveAS();
        } else {
            try {
                var temp = Path.of(Config.getProperty("temp"));
                var source = Path.of(Config.getProperty("file"));
                Files.copy(temp, source, StandardCopyOption.REPLACE_EXISTING);
                Config.setProperty("modified", Boolean.FALSE.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onMenuSaveAS() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Time Table");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier TimeTable", "*.tk")
        );
        var file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try {
                var path = file.toString().endsWith(".tk") ? file.toString() : file.toString().concat(".tk");
                var temp = Path.of(Config.getProperty("temp"));
                var source = Path.of(path);
                Files.copy(temp, source, StandardCopyOption.REPLACE_EXISTING);
                Config.setProperty("file", source.toString());
                Config.setProperty("modified", Boolean.FALSE.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onMenuOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TimeTable", "*.tk")
        );
        var file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try {
                var dest = Files.createTempFile("temp", ".tk");
                Files.copy(Path.of(file.getPath()), dest, StandardCopyOption.REPLACE_EXISTING);

                Config.setProperty("file", file.getPath());
                Config.setProperty("temp", dest.toString());
                Config.setProperty("modified", Boolean.FALSE.toString());

                tabpane.getTabs().clear();
                tabpane.getTabs().add(new DashboardTab("Accueil"));

                loadMenuGroupe();
                loadMenuFormateur();
                loadMenuEspace();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadMenuGroupe() {
        mGroupe.getItems().clear();
        var list = Database.getInstance().getGroupes();
        list.stream().map(g->g.getFiliere().getNiveau()).distinct().forEach(niveau -> {
            var menu = new Menu(niveau);
            mGroupe.getItems().add(menu);
            list.stream().filter(groupe -> groupe.getFiliere().getNiveau().equals(niveau)).forEach(groupe -> {
                var submenu = new MenuItem(groupe.getNom());
                submenu.setOnAction(event -> {
                    loadTab(new GroupeTab(groupe));
                });
                menu.getItems().add(submenu);
            });
        });

    }

    public void loadMenuFormateur() {
        mFormateur.getItems().clear();
        var list = Database.getInstance().getFormateurs();
        list.stream().map(Formateur::getType).distinct().forEach(type -> {
            var menu = new Menu(type);
            mFormateur.getItems().add(menu);
            list.stream().filter(formateur -> formateur.getType().equals(type)).forEach(formateur -> {
                var submenu = new MenuItem(formateur.toString());
                submenu.setOnAction(event -> {
                    loadTab(new FormateurTab(formateur));
                });
                menu.getItems().add(submenu);
            });
        });

    }

    public void loadMenuEspace() {
        mEspace.getItems().clear();
        var list = Database.getInstance().getEspaces();
        list.stream().map(Espace::getType).distinct().forEach(type -> {
            var menu = new Menu(type);
            mEspace.getItems().add(menu);
            list.stream().filter(groupe -> groupe.getType().equals(type)).forEach(espace -> {
                var submenu = new MenuItem(espace.getNom());
                submenu.setOnAction(event -> {
                    loadTab(new EspaceTab(espace));
                });
                menu.getItems().add(submenu);
            });
        });

    }

    public void loadTab(Tab tab) {
        tabpane.getTabs().stream()
                .filter(t -> t.getText().equals(tab.getText()))
                .findFirst()
                .ifPresentOrElse(tabpane.getSelectionModel()::select,
                        () -> {
                            tabpane.getTabs().add(tab);
                            tabpane.getSelectionModel().select(tab);
                        });
    }

    public void print() {
        ((EditorTab) tabpane.getSelectionModel().getSelectedItem()).print();
    }
}
