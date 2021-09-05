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
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tabs.*;

public class Editor extends VBox {

    private TabPane tabpane;
    private MenuBar menu;
    private final Menu mFichier = new Menu("Fichier");
    private final Menu mGroupe = new Menu("Groupe");
    private final Menu mFormateur = new Menu("Formateur");
    private final Menu mEspace = new Menu("Espace");

    public Editor() {
        //Database.getInstance().createDatabase();
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

        var mOuvrir = new MenuItem("Ouvrir");
        mOuvrir.setOnAction(actionEvent -> save());
        var mEnregister = new MenuItem("Enregister");

        var mPrint = new MenuItem("Imprimer");
        var pkeyCombination = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        mPrint.setAccelerator(pkeyCombination);
        mPrint.setOnAction(event -> print());

        var mQuitter = new MenuItem("Quitter");
        var qkeyCombination = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        mQuitter.setAccelerator(qkeyCombination);
        mQuitter.setOnAction(event-> Platform.exit());

        mFichier.getItems().addAll(mOuvrir, mEnregister, mPrint, new SeparatorMenuItem(), mQuitter);

        loadMenuGroupe();
        loadMenuFormateur();
        loadMenuEspace();

        getChildren().addAll(menu);
    }

    private void save() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Time Table");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier TimeTable", "*.tk")
        );
        var file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try {
                var path = file.toString().endsWith(".tk") ? file.toString() : file.toString().concat(".tk");
                tabpane.getTabs().add(new EspaceConfig(this));
            } catch (Exception e) {
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
