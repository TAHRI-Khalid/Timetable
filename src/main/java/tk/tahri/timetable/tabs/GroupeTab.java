package tk.tahri.timetable.tabs;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.GroupeTimeTablePrint;
import tk.tahri.timetable.view.*;

public class GroupeTab extends AbstractTimetableTab {

    private final Groupe groupe;
    private final GroupeMainTimeTable groupeTimeTable;
    private final FormateurSubTimetableView formateurTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;
    private final VBox formateursVBox = new VBox(5);
    private final VBox espacesVBox = new VBox(5);

    public GroupeTab(Groupe groupe) {
        super(groupe.getNom());
        this.groupe = groupe;

        formateurTimeTable = new FormateurSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();

        var groupeToolBar = new TitleToolBar();
        groupeToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(26));
        groupeToolBar.getStyleClass().add("groupe-title-main");
        groupeTimeTable = new GroupeMainTimeTable(groupe, groupeToolBar, formateurTimeTable, espaceTimeTable);

        var formateurToolBar = new TitleToolBar();
        formateurToolBar.setTitle("Formateur : ");
        formateurTimeTable.setTitleToolBar(formateurToolBar);
        formateurToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(18));
        formateurToolBar.getStyleClass().add("formateur-title-sub");

        var espaceToolBar = new TitleToolBar();
        espaceToolBar.setTitle("Espace : ");
        espaceTimeTable.setTitleToolBar(espaceToolBar);
        espaceToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(18));
        espaceToolBar.getStyleClass().add("espace-title-sub");

        loadFormateurs();
        loadEspaces();

        var sidebar = new VBox();
        sidebar.getChildren().addAll(
                createSidebarPane("Formateurs", formateursVBox),
                createSidebarPane("Espaces", espacesVBox)
        );

        setupLayout(groupeTimeTable, groupeToolBar,
                formateurTimeTable, formateurToolBar,
                espaceTimeTable, espaceToolBar,
                sidebar);
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

    private void loadFormateurs() {
        formateursVBox.getChildren().clear();
        Database.getInstance().getFormateurs().forEach(f -> {
            var formateurView = new FormateurView(f);
            formateurView.setMaxWidth(Double.MAX_VALUE);
            formateurView.setOnMousePressed(mouseEvent -> {
                formateurTimeTable.setFormateur(formateurView.getFormateur());
                formateurTimeTable.loadSeanceViews(false);
            });
            formateurView.setOnDragDetected(event -> {
                Dragboard db = formateurView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("FORMATEUR");
                db.setContent(content);
                formateurTimeTable.setFormateur(formateurView.getFormateur());
                formateurTimeTable.loadSeanceViews(false);
                event.consume();
            });
            formateursVBox.getChildren().add(formateurView);
        });
    }

    @Override
    protected void selectionChanged() {
        if (this.isSelected()) {
            groupeTimeTable.loadSeanceViews(true);
            if (espaceTimeTable.getEspace() != null) {
                espaceTimeTable.loadSeanceViews(false);
            }
            if (formateurTimeTable.getFormateur() != null) {
                formateurTimeTable.loadSeanceViews(false);
            }
            loadFormateurs();
            loadEspaces();
        }
    }

    @Override
    public void print() {
        printTimeTable(new GroupeTimeTablePrint(groupe));
    }

    @Override
    public MainTimeTableView getMainTimeTableView() {
        return groupeTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView1() {
        return formateurTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView2() {
        return espaceTimeTable;
    }
}
