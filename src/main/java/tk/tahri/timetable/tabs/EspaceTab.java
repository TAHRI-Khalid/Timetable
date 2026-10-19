package tk.tahri.timetable.tabs;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.EspaceTimeTablePrint;
import tk.tahri.timetable.view.*;

public class EspaceTab extends AbstractTimetableTab {

    private final Espace espace;
    private final EspaceMainTimeTable espaceTimeTable;
    private final GroupeSubTimetableView groupeTimeTable;
    private final FormateurSubTimetableView formateurTimeTable;
    private final VBox groupesVBox = new VBox(5);
    private final VBox formateursVBox = new VBox(5);

    public EspaceTab(Espace espace) {
        super(espace.getNom());
        this.espace = espace;

        groupeTimeTable = new GroupeSubTimetableView();
        formateurTimeTable = new FormateurSubTimetableView();

        var espaceToolBar = new TitleToolBar();
        espaceToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(26));
        espaceToolBar.getStyleClass().add("espace-title-main");
        espaceTimeTable = new EspaceMainTimeTable(espace, espaceToolBar, groupeTimeTable, formateurTimeTable);

        var groupeToolBar = new TitleToolBar();
        groupeToolBar.setTitle("Groupe : ");
        groupeTimeTable.setTitleToolBar(groupeToolBar);
        groupeToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(18));
        groupeToolBar.getStyleClass().add("groupe-title-sub");

        var formateurToolBar = new TitleToolBar();
        formateurToolBar.setTitle("Formateur : ");
        formateurTimeTable.setTitleToolBar(formateurToolBar);
        formateurToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(18));
        formateurToolBar.getStyleClass().add("formateur-title-sub");

        loadGroupes();
        loadFormateurs();

        var sidebar = new VBox();
        sidebar.getChildren().addAll(
                createSidebarPane("Groupes", groupesVBox),
                createSidebarPane("Formateurs", formateursVBox)
        );

        setupLayout(espaceTimeTable, espaceToolBar,
                groupeTimeTable, groupeToolBar,
                formateurTimeTable, formateurToolBar,
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
            espaceTimeTable.loadSeanceViews(true);
            if (formateurTimeTable.getFormateur() != null) {
                formateurTimeTable.loadSeanceViews(false);
            }
            if (groupeTimeTable.getGroupe() != null) {
                groupeTimeTable.loadSeanceViews(false);
            }
            loadGroupes();
            loadFormateurs();
        }
    }

    @Override
    public void print() {
        printTimeTable(new EspaceTimeTablePrint(espace));
    }

    @Override
    public MainTimeTableView getMainTimeTableView() {
        return espaceTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView1() {
        return groupeTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView2() {
        return formateurTimeTable;
    }
}
