package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.print.TimeTablePrint;
import tk.tahri.timetable.view.MainTimeTableView;
import tk.tahri.timetable.view.SubTimeTableView;
import tk.tahri.timetable.view.TitleToolBar;

public abstract class AbstractTimetableTab extends EditorTab {
    protected final FontAwesome fontAwesome = new FontAwesome();
    protected final GridPane grid;

    public AbstractTimetableTab(String title) {
        super(title);
        grid = new GridPane();
        setContent(grid);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(43);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(43);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(14);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(7);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(6);
        RowConstraints row4 = new RowConstraints();
        row4.setPercentHeight(37);
        grid.getRowConstraints().addAll(row1, row2, row3, row4);

        grid.widthProperty().addListener((obs, oldVal, newVal) -> resize());
        grid.heightProperty().addListener((obs, oldVal, newVal) -> resize());
    }

    protected void setupLayout(MainTimeTableView mainView, TitleToolBar mainToolBar,
                               SubTimeTableView sub1View, TitleToolBar sub1ToolBar,
                               SubTimeTableView sub2View, TitleToolBar sub2ToolBar,
                               VBox sidebarBox) {
        grid.add(mainToolBar, 0, 0, 2, 1);
        grid.add(wrapInCenteredBoxes(mainView), 0, 1, 2, 1);

        grid.add(sub1ToolBar, 0, 2);
        grid.add(wrapInCenteredBoxes(sub1View), 0, 3);

        grid.add(sub2ToolBar, 1, 2);
        grid.add(wrapInCenteredBoxes(sub2View), 1, 3);

        grid.add(sidebarBox, 2, 0, 1, 4);
    }

    protected HBox wrapInCenteredBoxes(Node content) {
        var vBox = new VBox(content);
        vBox.setAlignment(Pos.CENTER);
        var hBox = new HBox(vBox);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    protected TitledPane createSidebarPane(String title, Node content) {
        var pane = new TitledPane();
        pane.setText(title);

        if (content instanceof ListView<?> || content instanceof ComboBox<?>) {
            var box = new VBox(content);
            box.setPadding(new Insets(10));
            pane.setContent(box);
        } else {
            var scrollPane = new ScrollPane();
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            scrollPane.setPadding(new Insets(10));
            scrollPane.setContent(content);
            pane.setContent(scrollPane);
        }

        pane.setMaxHeight(Double.MAX_VALUE);
        pane.setExpanded(true);
        return pane;
    }

    protected void resize() {
        var scaleX = grid.getWidth() / 1300.0;
        var scaleY = grid.getHeight() / 750.0;
        var scale = Math.min(scaleX, scaleY);

        if (getMainTimeTableView() != null) {
            getMainTimeTableView().setScaleX(scale);
            getMainTimeTableView().setScaleY(scale);
        }
        if (getSubView1() != null) {
            getSubView1().setScaleX(scale);
            getSubView1().setScaleY(scale);
        }
        if (getSubView2() != null) {
            getSubView2().setScaleX(scale);
            getSubView2().setScaleY(scale);
        }
    }

    protected abstract SubTimeTableView getSubView1();
    protected abstract SubTimeTableView getSubView2();

    protected void printTimeTable(TimeTablePrint printable) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && getMainTimeTableView() != null && getMainTimeTableView().getScene() != null) {
            PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
            job.getJobSettings().setPageLayout(pageLayout);
            printable.setPrefWidth(pageLayout.getPrintableWidth());

            if (job.showPrintDialog(getMainTimeTableView().getScene().getWindow())) {
                boolean success = job.printPage(printable);
                if (success) {
                    job.endJob();
                }
            }
        }
    }
}
