package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tk.tahri.timetable.Entities.*;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.view.SeanceView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MatriceTab extends EditorTab {
    protected final String[] days = new String[]{"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private final GridPane martix;
    private SeanceType type;
    private List<Seance> allSeances;

    public MatriceTab() {
        super("Matrice");
        type = SeanceType.GROUPE;

        var parent = new VBox();
        var body = new ScrollPane();
        body.setFitToWidth(true);
        body.setFitToHeight(true);
        martix = new GridPane();
        body.setContent(martix);

        var toolBar = new ToolBar();
        toolBar.getItems().add(new Label("Filter : "));
        var combo = new ComboBox<SeanceType>();
        combo.getItems().addAll(SeanceType.values());
        combo.getSelectionModel().select(0);
        combo.setOnAction(event -> {
            this.type = combo.getSelectionModel().getSelectedItem();
            selectionChanged();
        });
        toolBar.getItems().add(combo);
        toolBar.getItems().add(new Separator());

        toolBar.getItems().add(new Label("Exportation : "));
        var btn = new Button("Semaine");
        btn.setOnAction(actionEvent -> exportToExcel("semaine"));
        toolBar.getItems().add(btn);
        MenuButton menuButton = new MenuButton("Jour");
        for (var day : days) {
            MenuItem menuItem = new MenuItem(day);
            menuItem.setOnAction(actionEvent -> exportToExcel(day));
            menuButton.getItems().add(menuItem);
        }
        toolBar.getItems().add(menuButton);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().add(new Label("Zoom : "));
        var slide = new Slider();
        toolBar.getItems().add(slide);
        slide.setMax(1);
        slide.setMin(0.1);
        slide.setValue(1);
        slide.valueProperty().addListener((observableValue, number, t1) -> {
            martix.getTransforms().clear();
            martix.getTransforms().add(new Scale(number.doubleValue(), number.doubleValue()));
        });
        btn = new Button("Adapter");
        btn.setOnAction(actionEvent -> {

            if( martix.getWidth() > parent.getWidth()){

                var coef =  (parent.getWidth()-20) /martix.getWidth() ;
                martix.getTransforms().clear();
                martix.getTransforms().add(new Scale(coef, coef));
            }


        });
        toolBar.getItems().add(btn);
        parent.getChildren().addAll(toolBar, body);
        setContent(parent);

        martix.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        martix.setPadding(new Insets(10));
        martix.setGridLinesVisible(true);

        martix.getColumnConstraints().add(new ColumnConstraints(150));
        for (int i = 1; i < 1 + (40 * 6); i++) {
            ColumnConstraints column = new ColumnConstraints(15);
            martix.getColumnConstraints().add(column);
        }

        var vBox = new VBox();
        vBox.setStyle("-fx-background-color:#fff");
        martix.add(vBox, 0, 0, 1, 2);

        for (int i = 0; i < days.length; i++) {
            vBox = new VBox(new Text(days[i]));
            vBox.setStyle("-fx-font-size: 18");
            vBox.setAlignment(Pos.CENTER);
            if (i % 2 == 0) {
                vBox.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                vBox.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            }
            martix.add(vBox, (i * 40) + 1, 0, 40, 1);
            var time = LocalTime.of(8, 30);
            for (int j = 0; j < 10; j++) {
                vBox = new VBox(new Text(time.toString()));
                vBox.setAlignment(Pos.CENTER);
                if (j % 2 == 0) {
                    vBox.setBackground(new Background(new BackgroundFill(i % 2 == 0 ? Color.LIGHTGRAY : Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    vBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                }
                martix.add(vBox, (i * 40) + (j * 4) + 1, 1, 4, 1);
                time = time.plusHours(1);
            }
        }
    }

    private void exportToExcel(String day) {
        List<Seance> list;
        if (day.equals("semaine")) {
            list = allSeances.stream()
                    .sorted(Comparator.comparing(Seance::getJour)).toList();
        } else {
            var index = Arrays.binarySearch(days, day);
            list = allSeances.stream()
                    .filter(seance -> seance.getJour().getValue() == index + 1)
                    .sorted(Comparator.comparing(Seance::getDebut)).toList();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export To Excel");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx")
        );
        fileChooser.setInitialFileName(day + ".xlsx");
        var file = fileChooser.showSaveDialog(martix.getScene().getWindow());

        if (file != null) {
            try (var workbook = new XSSFWorkbook();
                 var outputStream = new FileOutputStream(file)) {
                var sheet = workbook.createSheet(day);

                var row = sheet.createRow(0);
                row.createCell(0).setCellValue("Jour");
                row.createCell(1).setCellValue("début");
                row.createCell(2).setCellValue("Fin");
                row.createCell(3).setCellValue("Durée");
                row.createCell(4).setCellValue("Type");
                row.createCell(5).setCellValue("Formateur");
                row.createCell(6).setCellValue("Groupe");
                row.createCell(7).setCellValue("Espace");

                for (int line = 0; line < list.size(); line++) {
                    row = sheet.createRow(line + 1);
                    row.createCell(0).setCellValue(days[list.get(line).getJour().getValue() - 1]);
                    row.createCell(1).setCellValue(list.get(line).getDebut().toString());
                    row.createCell(2).setCellValue(list.get(line).getFin().toString());
                    row.createCell(3).setCellValue(list.get(line).getDuree());
                    row.createCell(4).setCellValue(list.get(line).getType());
                    row.createCell(5).setCellValue(list.get(line).getFormateur().toString());
                    row.createCell(6).setCellValue(list.get(line).getGroupes().toString());
                    row.createCell(7).setCellValue(list.get(line).getEspace().toString());
                }

                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void selectionChanged() {
        if (!this.isSelected()) return;
        martix.getChildren().removeIf(node -> node instanceof VBox && GridPane.getRowIndex(node) >= 2);
        allSeances = Database.getInstance().getSeances();

        var list = switch (type) {
            case GROUPE -> Database.getInstance().getGroupes();
            case ESPACE -> Database.getInstance().getEspaces();
            case FORMATEUR -> Database.getInstance().getFormateurs();
        };

        for (int i = 0; i < list.size(); i++) {
            var lbl = new Label(list.get(i).toString());
            lbl.setWrapText(true);
            lbl.setTextAlignment(TextAlignment.CENTER);
            var vBox = new VBox(lbl);
            vBox.setStyle("-fx-font-size: 18");
            if (i % 2 == 0) {
                vBox.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                vBox.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            }
            vBox.setAlignment(Pos.CENTER);
            martix.add(vBox, 0, i + 2);

            var seances = switch (type) {
                case GROUPE -> Database.getInstance().getSeances((Groupe) list.get(i));
                case ESPACE -> Database.getInstance().getSeances((Espace) list.get(i));
                case FORMATEUR -> Database.getInstance().getSeances((Formateur) list.get(i));
            };

            for (var seance : seances) {
                var y = i + 2;
                var x = 1 + ((seance.getJour().getValue() - 1) * 40) + (seance.getDebut().minusHours(8).minusMinutes(30).toSecondOfDay() / (60 * 15));
                var span = seance.getFin().minusHours(seance.getDebut().getHour()).minusMinutes(seance.getDebut().getMinute()).toSecondOfDay() / (60 * 15);
                var sv = new SeanceView(seance);
                sv.setOnMouseClicked(this::onSeanceCliked);
                var ch = false;
                for (var s2 : allSeances) {
                    if (chevauchement(seance, s2)) {
                        ch = true;
                        break;
                    }
                }
                var style = switch (type) {
                    case GROUPE -> "groupe-seance-view";
                    case FORMATEUR -> "formateur-seance-view";
                    case ESPACE -> "espace-seance-view";
                };
                sv.getStyleClass().add(ch ? "chevauchement-seance-view" : style);
                martix.add(sv, x, y, span, 1);
            }
        }
    }

    private void onSeanceCliked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            var seance = ((SeanceView) mouseEvent.getSource()).getSeance();
            var tab = switch (type) {
                case GROUPE -> new GroupeTab(seance.getGroupes().get(0));
                case FORMATEUR -> new FormateurTab(seance.getFormateur());
                case ESPACE -> new EspaceTab(seance.getEspace());
            };
            this.getTabPane().getTabs().stream().filter(t -> t.getText().equals(tab.getText())).findFirst().ifPresentOrElse(this.getTabPane().getSelectionModel()::select, () -> {
                this.getTabPane().getTabs().add(tab);
                this.getTabPane().getSelectionModel().select(tab);
            });
        }
    }

    @Override
    public void print() {

    }

    private boolean chevauchement(Seance s1, Seance s2) {
        if (s1.getId() == s2.getId()) {
            return false;
        } else if (s1.getJour().getValue() != s2.getJour().getValue()) {
            return false;
        } else if (s1.getFormateur().getId() != s2.getFormateur().getId() && s1.getEspace().getId() != s2.getEspace().getId() && s1.getGroupes().stream().map(Groupe::getId).noneMatch(s2.getGroupes().stream().map(Groupe::getId).collect(Collectors.toList())::contains)) {
            return false;
        } else if (s2.getDebut().toSecondOfDay() < s1.getDebut().toSecondOfDay() && s2.getFin().toSecondOfDay() > s1.getDebut().toSecondOfDay()) {
            return true;
        } else if (s2.getDebut().toSecondOfDay() < s1.getFin().toSecondOfDay() && s2.getFin().toSecondOfDay() > s1.getFin().toSecondOfDay()) {
            return true;
        } else
            return s2.getDebut().toSecondOfDay() >= s1.getDebut().toSecondOfDay() && s2.getFin().toSecondOfDay() <= s1.getFin().toSecondOfDay();
    }
}


