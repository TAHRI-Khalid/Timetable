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
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tabs.*;
import tk.tahri.timetable.tools.Config;
import tk.tahri.timetable.tools.NotificationService;
import java.io.IOException;
import java.nio.file.*;


public class Editor extends VBox {

    private TabPane tabpane;
    private final Menu mFichier = new Menu("Fichier");
    private final Menu mEdition = new Menu("Édition");
    private final Menu mGroupe = new Menu("Groupes");
    private final Menu mFormateur = new Menu("Formateurs");
    private final Menu mEspace = new Menu("Espaces");
    private final Menu mParametre = new Menu("Paramètres");

    public Editor() {
        setStyle("-fx-background-color: #ffffff;");
        createMenu();
        createTabpane();
        DashboardTab tab = new DashboardTab("Accueil");
        tabpane.getTabs().add(tab);

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN), this::undo);
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN), this::redo);
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), this::redo);
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), this::deleteSelected);
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), this::duplicateSelected);
            }
        });
    }

    private void createTabpane() {
        tabpane = new TabPane();
        tabpane.setStyle("-fx-background-color: #ffffff;");
        tabpane.setSide(Side.TOP);
        tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        VBox.setVgrow(tabpane, Priority.ALWAYS);
        getChildren().addAll(tabpane);
    }

    private void createMenu() {
        MenuBar menu = new MenuBar();
        menu.getMenus().addAll(mFichier, mEdition, mGroupe, mFormateur, mEspace, mParametre);

        createEditionMenu();

        var mNouveau = new MenuItem("Nouveau");
        var nkeyCombination = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        mNouveau.setAccelerator(nkeyCombination);
        mNouveau.setOnAction(actionEvent -> onMenuNew());

        var mOuvrir = new MenuItem("Ouvrir");
        var okeyCombination = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        mOuvrir.setAccelerator(okeyCombination);
        mOuvrir.setOnAction(actionEvent -> onMenuOpen());

        var mEnregistrer = new MenuItem("Enregistrer");
        var skeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        mEnregistrer.setAccelerator(skeyCombination);
        mEnregistrer.setOnAction(actionEvent -> onMenuSave());

        var mEnregistrerSous = new MenuItem("Enregistrer sous ...");
        var smkeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        mEnregistrerSous.setAccelerator(smkeyCombination);
        mEnregistrerSous.setOnAction(actionEvent -> onMenuSaveAS());

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

        mFichier.getItems().addAll(mNouveau, mOuvrir, mEnregistrer, mEnregistrerSous, new SeparatorMenuItem(), mPrint, mPrintCenter, new SeparatorMenuItem(), mQuitter);

        if (!Config.getProperty("file").trim().isEmpty()) {
            loadMenuGroupe();
            loadMenuFormateur();
            loadMenuEspace();
            loadMenuParametre();
        }

        getChildren().addAll(menu);
    }

    private void createEditionMenu() {
        mEdition.getItems().clear();

        var mAnnuler = new MenuItem("Annuler");
        mAnnuler.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        mAnnuler.setOnAction(e -> undo());
        mAnnuler.disableProperty().bind(tk.tahri.timetable.tools.command.UndoManager.getInstance().canUndoProperty().not());

        var mRetablir = new MenuItem("Rétablir");
        mRetablir.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        mRetablir.setOnAction(e -> redo());
        mRetablir.disableProperty().bind(tk.tahri.timetable.tools.command.UndoManager.getInstance().canRedoProperty().not());

        var mSuppr = new MenuItem("Supprimer la séance");
        mSuppr.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        mSuppr.setOnAction(e -> deleteSelected());

        var mDupliquer = new MenuItem("Dupliquer la séance");
        mDupliquer.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        mDupliquer.setOnAction(e -> duplicateSelected());

        mEdition.getItems().addAll(mAnnuler, mRetablir, new SeparatorMenuItem(), mSuppr, mDupliquer);
    }

    private void onMenuNew() {
        try {
            var modified = Boolean.parseBoolean(Config.getProperty("modified"));
            if (modified) {
                var choice = NotificationService.confirmSaveOnClose("de créer un nouveau fichier");
                if (choice == NotificationService.SaveChoice.SAVE) {
                    onMenuSave();
                } else if (choice == NotificationService.SaveChoice.CANCEL) {
                    return;
                }
            }

            var dest = Files.createTempFile("temp", ".tk");
            Config.setProperty("file", "");
            Config.setProperty("temp", dest.toString());
            Config.setProperty("modified", Boolean.TRUE.toString());

            Database.getInstance().createDatabase();
            tk.tahri.timetable.tools.command.UndoManager.getInstance().clear();
            tabpane.getTabs().clear();
            tabpane.getTabs().add(new DashboardTab("Accueil"));
            loadMenuGroupe();
            loadMenuFormateur();
            loadMenuEspace();
            loadMenuParametre();
            NotificationService.showInfo("Nouveau fichier", "Un nouveau projet d'emploi du temps a été initialisé.");

        } catch (IOException e) {
            NotificationService.showError("Erreur", "Impossible de créer le nouveau fichier : " + e.getMessage());
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
                NotificationService.showSuccess("Enregistré", "Le fichier a été enregistré avec succès.");

            } catch (IOException e) {
                NotificationService.showError("Erreur d'enregistrement", "Impossible d'enregistrer le fichier : " + e.getMessage());
            }
        }
    }

    private void onMenuSaveAS() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Time Table - Enregistrer sous");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier TimeTable (*.tk)", "*.tk")
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
                NotificationService.showSuccess("Enregistré sous", "Fichier sauvegardé avec succès : " + source.getFileName());

            } catch (Exception e) {
                NotificationService.showError("Erreur d'enregistrement", "Impossible de sauvegarder le fichier : " + e.getMessage());
            }
        }
    }

    private void onMenuOpen() {
        var modified = Boolean.parseBoolean(Config.getProperty("modified"));
        if (modified) {
            var choice = NotificationService.confirmSaveOnClose("d'ouvrir un autre fichier");
            if (choice == NotificationService.SaveChoice.SAVE) {
                onMenuSave();
            } else if (choice == NotificationService.SaveChoice.CANCEL) {
                return;
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir un fichier TimeTable");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TimeTable (*.tk)", "*.tk")
        );
        var file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            try {
                var dest = Files.createTempFile("temp", ".tk");
                Files.copy(Path.of(file.getPath()), dest, StandardCopyOption.REPLACE_EXISTING);

                Config.setProperty("file", file.getPath());
                Config.setProperty("temp", dest.toString());
                Config.setProperty("modified", Boolean.FALSE.toString());

                tk.tahri.timetable.tools.command.UndoManager.getInstance().clear();
                tabpane.getTabs().clear();
                tabpane.getTabs().add(new DashboardTab("Accueil"));

                loadMenuGroupe();
                loadMenuFormateur();
                loadMenuEspace();
                loadMenuParametre();
                NotificationService.showSuccess("Fichier ouvert", "Le fichier '" + file.getName() + "' a été ouvert avec succès.");

            } catch (IOException e) {
                NotificationService.showError("Erreur d'ouverture", "Impossible d'ouvrir le fichier : " + e.getMessage());
            }
        }
    }

    public void loadMenuGroupe() {
        mGroupe.getItems().clear();
        var list = Database.getInstance().getGroupes();
        list.stream().map(g -> g.getFiliere().getNiveau()).distinct().forEach(niveau -> {
            var menu = new Menu(niveau);
            mGroupe.getItems().add(menu);
            list.stream().filter(groupe -> groupe.getFiliere().getNiveau().equals(niveau)).forEach(groupe -> {
                var submenu = new MenuItem(groupe.getNom());
                submenu.setOnAction(event -> loadTab(new GroupeTab(groupe)));
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
            for (Formateur formateur : list) {
                if (formateur.getType().equals(type)) {
                    var submenu = new MenuItem(formateur.toString());
                    submenu.setOnAction(event -> loadTab(new FormateurTab(formateur)));
                    menu.getItems().add(submenu);
                }
            }
        });

    }

    public void loadMenuEspace() {
        mEspace.getItems().clear();
        var list = Database.getInstance().getEspaces();
        list.stream().map(Espace::getType).distinct().forEach(type -> {
            var menu = new Menu(type);
            mEspace.getItems().add(menu);
            for (Espace espace : list) {
                if (espace.getType().equals(type)) {
                    var submenu = new MenuItem(espace.getNom());
                    submenu.setOnAction(event -> loadTab(new EspaceTab(espace)));
                    menu.getItems().add(submenu);
                }
            }
        });

    }

    private void loadMenuParametre() {
        mParametre.getItems().clear();

        var mEtablissement = new MenuItem("Etablissement");
        mEtablissement.setOnAction(event -> loadTab(new EtablissementParametre(this)));
        var mFiliere = new MenuItem("Filières");
        mFiliere.setOnAction(event -> loadTab(new FiliereParametre(this)));

        var mParamGroupe = new MenuItem("Groupes");
        var gkeyCombination = new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN);
        mParamGroupe.setAccelerator(gkeyCombination);
        mParamGroupe.setOnAction(event -> loadTab(new GroupeParametre(this)));

        var mParamFormateur = new MenuItem("Formateurs");
        var fkeyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        mParamFormateur.setAccelerator(fkeyCombination);
        mParamFormateur.setOnAction(event -> loadTab(new FormateurParametre(this)));

        var mParamEspace = new MenuItem("Espace");
        var ekeyCombination = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
        mParamEspace.setAccelerator(ekeyCombination);
        mParamEspace.setOnAction(event -> loadTab(new EspaceParametre(this)));

        var mCreneaux = new MenuItem("Créneaux horaires");
        var hkeyCombination = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN);
        mCreneaux.setAccelerator(hkeyCombination);
        mCreneaux.setOnAction(event -> loadTab(new CreneauParametre(this)));

        var mMatrice = new MenuItem("Matrice");
        mMatrice.setOnAction(event -> loadTab(new MatriceTab()));

        mParametre.getItems().addAll(mEtablissement, mFiliere, mParamGroupe, mParamFormateur, mParamEspace, mCreneaux, mMatrice);

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

    public tk.tahri.timetable.view.MainTimeTableView getActiveMainTimeTableView() {
        Tab selected = tabpane.getSelectionModel().getSelectedItem();
        if (selected instanceof EditorTab editorTab) {
            return editorTab.getMainTimeTableView();
        }
        return null;
    }

    public void undo() {
        if (tk.tahri.timetable.tools.command.UndoManager.getInstance().canUndo()) {
            String desc = tk.tahri.timetable.tools.command.UndoManager.getInstance().getLastUndoDescription();
            tk.tahri.timetable.tools.command.UndoManager.getInstance().undo();
            NotificationService.showInfo("Annuler", "Action annulée : " + (desc.isEmpty() ? "Opération" : desc));
        }
    }

    public void redo() {
        if (tk.tahri.timetable.tools.command.UndoManager.getInstance().canRedo()) {
            String desc = tk.tahri.timetable.tools.command.UndoManager.getInstance().getLastRedoDescription();
            tk.tahri.timetable.tools.command.UndoManager.getInstance().redo();
            NotificationService.showInfo("Rétablir", "Action rétablie : " + (desc.isEmpty() ? "Opération" : desc));
        }
    }

    public void deleteSelected() {
        var view = getActiveMainTimeTableView();
        if (view != null) {
            view.deleteSelectedSeance();
        }
    }

    public void duplicateSelected() {
        var view = getActiveMainTimeTableView();
        if (view != null) {
            view.duplicateSelectedSeance();
        }
    }
}
