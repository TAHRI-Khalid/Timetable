package tk.tahri.timetable.tabs;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.FormateurTimeTablePrint;
import tk.tahri.timetable.view.*;

public class FormateurTab extends AbstractTimetableTab {

    private final Formateur formateur;
    private final FormateurMainTimeTable formateurTimeTable;
    private final GroupeSubTimetableView groupeTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;
    private final VBox groupesVBox = new VBox(5);
    private final VBox espacesVBox = new VBox(5);

    public FormateurTab(Formateur formateur) {
        super(formateur.toString());
        this.formateur = formateur;

        groupeTimeTable = new GroupeSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();

        var formateurToolBar = new TitleToolBar();
        formateurToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(26));
        formateurToolBar.getStyleClass().add("formateur-title-main");
        formateurTimeTable = new FormateurMainTimeTable(formateur, formateurToolBar, groupeTimeTable, espaceTimeTable);

        var groupeToolBar = new TitleToolBar();
        groupeToolBar.setTitle("Groupe : ");
        groupeTimeTable.setTitleToolBar(groupeToolBar);
        groupeToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(18));
        groupeToolBar.getStyleClass().add("groupe-title-sub");

        var espaceToolBar = new TitleToolBar();
        espaceToolBar.setTitle("Espace : ");
        espaceTimeTable.setTitleToolBar(espaceToolBar);
        espaceToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(18));
        espaceToolBar.getStyleClass().add("espace-title-sub");

        loadGroupes();
        loadEspaces();

        var sidebar = new VBox();
        sidebar.getChildren().addAll(
                createSidebarPane("Groupes", groupesVBox),
                createSidebarPane("Espaces", espacesVBox)
        );

        setupLayout(formateurTimeTable, formateurToolBar,
                groupeTimeTable, groupeToolBar,
                espaceTimeTable, espaceToolBar,
                sidebar);
    }

    private void loadGroupes() {
        groupesVBox.getChildren().clear();
        Database.getInstance().getGroupes().forEach(g -> {
            var groupeView = new GroupeView(g);
            groupeView.setMaxWidth(Double.MAX_VALUE);
            groupeView.setOnMousePressed(mouseEvent -> {
                groupeTimeTable.setGroupe(groupeView.getGroupe());
                groupeTimeTable.loadSeanceViews(false);
            });
            groupeView.setOnDragDetected(event -> {
                Dragboard db = groupeView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("GROUPE");
                db.setContent(content);
                groupeTimeTable.setGroupe(groupeView.getGroupe());
                groupeTimeTable.loadSeanceViews(false);
                event.consume();
            });
            groupesVBox.getChildren().add(groupeView);
        });
    }

    private void loadEspaces() {
        espacesVBox.getChildren().clear();
        Database.getInstance().getEspaces().forEach(e -> {
            var espaceView = new EspaceView(e);
            espaceView.setMaxWidth(Double.MAX_VALUE);
            espaceView.setOnMousePressed(mouseEvent -> {
                espaceTimeTable.setEspace(espaceView.getEspace());
                espaceTimeTable.loadSeanceViews(false);
            });
            espaceView.setOnDragDetected(event -> {
                Dragboard db = espaceView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("ESPACE");
                db.setContent(content);
                espaceTimeTable.setEspace(espaceView.getEspace());
                espaceTimeTable.loadSeanceViews(false);
                event.consume();
            });
            espacesVBox.getChildren().add(espaceView);
        });
    }

    @Override
    protected void selectionChanged() {
        if (this.isSelected()) {
            formateurTimeTable.loadSeanceViews(true);
            if (espaceTimeTable.getEspace() != null) {
                espaceTimeTable.loadSeanceViews(false);
            }
            if (groupeTimeTable.getGroupe() != null) {
                groupeTimeTable.loadSeanceViews(false);
            }
            loadGroupes();
            loadEspaces();
        }
    }

    @Override
    public void print() {
        printTimeTable(new FormateurTimeTablePrint(formateur));
    }

    @Override
    public MainTimeTableView getMainTimeTableView() {
        return formateurTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView1() {
        return groupeTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView2() {
        return espaceTimeTable;
    }
}
