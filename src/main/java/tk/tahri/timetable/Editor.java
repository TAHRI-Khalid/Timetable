package tk.tahri.timetable;

import javafx.application.Platform;
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
    private final Menu mGroupe = new Menu("Groupe");
    private final Menu mFormateur = new Menu("Formateur");
    private final Menu mEspace = new Menu("Espace");

    public Editor() {
        createMenu();
        createTabpane();
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

        var mQuitter = new MenuItem("Quitter");
        var qkeyCombination = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        mQuitter.setAccelerator(qkeyCombination);
        mQuitter.setOnAction(event -> ((Stage) this.getScene().getWindow()).close());

        mFichier.getItems().addAll(mNouveau, mOuvrir, mEnregister, mEnregisterSous, mPrint, new SeparatorMenuItem(), mQuitter);

        if (!(Config.getProperty("file")==null || Config.getProperty("file").trim().isEmpty())) {
            try {
                var source = Path.of(Config.getProperty("file"));
                Path dest = Files.createTempFile("temp", ".tk");
                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                Config.setProperty("temp", dest.toString());
                Config.setProperty("modified", Boolean.FALSE.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            loadMenuGroupe();
            loadMenuFormateur();
            loadMenuEspace();
        }

        getChildren().addAll(menu);
    }

    private void onMenuNew() {
        try {
            var dest = Files.createTempFile("temp", ".tk");
            Config.setProperty("file", "");
            Config.setProperty("temp", dest.toString());
            Config.setProperty("modified", Boolean.TRUE.toString());

            Database.getInstance().createDatabase();
            tabpane.getTabs().clear();
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
        list.stream().map(Groupe::getNiveau).distinct().forEach(niveau -> {
            var menu = new Menu(niveau);
            mGroupe.getItems().add(menu);
            list.stream().filter(groupe -> groupe.getNiveau().equals(niveau)).forEach(groupe -> {
                var submenu = new MenuItem(groupe.getNom());
                submenu.setOnAction(event -> {
                    loadTab(new GroupeTab(groupe));
                });
                menu.getItems().add(submenu);
            });
        });
        var menuConfig = new MenuItem("Parametres");
        var gkeyCombination = new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN);
        menuConfig.setAccelerator(gkeyCombination);
        mGroupe.getItems().addAll(new SeparatorMenuItem(), menuConfig);
        menuConfig.setOnAction(event -> {
            loadTab(new GroupeConfig(this));
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
        var menuConfig = new MenuItem("Parametres");
        var fkeyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        menuConfig.setAccelerator(fkeyCombination);
        mFormateur.getItems().addAll(new SeparatorMenuItem(), menuConfig);
        menuConfig.setOnAction(event -> {
            loadTab(new FormateurConfig(this));
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
        var menuConfig = new MenuItem("Parametres");
        var ekeyCombination = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
        menuConfig.setAccelerator(ekeyCombination);
        mEspace.getItems().addAll(new SeparatorMenuItem(), menuConfig);
        menuConfig.setOnAction(event -> {
            loadTab(new EspaceConfig(this));
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
