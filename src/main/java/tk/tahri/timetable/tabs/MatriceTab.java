package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import tk.tahri.timetable.Entities.*;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.view.SeanceView;

import java.time.LocalTime;
import java.util.stream.Collectors;

public class MatriceTab extends EditorTab {
    protected final String[] days = new String[]{"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private final GridPane martix;
    private SeanceType type;

    public MatriceTab() {
        super("Matrice");
        type = SeanceType.GROUPE;

        var parent = new VBox();
        var body = new ScrollPane();
        body.setFitToWidth(true);
        body.setFitToHeight(true);
        martix = new GridPane();
        body.setContent(martix);
        var footer = new HBox();
        var slide = new Slider();
        footer.getChildren().add(slide);
        footer.setAlignment(Pos.CENTER_RIGHT);
        slide.setPadding(new Insets(10));
        slide.setMax(1);
        slide.setMin(0.1);
        slide.setValue(1);
        slide.valueProperty().addListener((observableValue, number, t1) -> {
            martix.getTransforms().clear();
            martix.getTransforms().add(new Scale(number.doubleValue(), number.doubleValue()));
        });

        parent.getChildren().addAll(body, footer);
        setContent(parent);

        martix.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        martix.setPadding(new Insets(25));
        martix.setGridLinesVisible(true);

        martix.getColumnConstraints().add(new ColumnConstraints(150));
        for (int i = 1; i < 1 + (40 * 6); i++) {
            ColumnConstraints column = new ColumnConstraints(15);
            martix.getColumnConstraints().add(column);
        }

        var vBox = new VBox();
        var combo = new ComboBox<SeanceType>();
        combo.getItems().addAll(SeanceType.values());
        combo.getSelectionModel().select(0);
        combo.setOnAction(event -> {
            this.type = combo.getSelectionModel().getSelectedItem();
            selectionChanged();
        });
        vBox.getChildren().add(combo);
        vBox.setAlignment(Pos.CENTER);
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

    @Override
    protected void selectionChanged() {
        if (!this.isSelected()) return;
        martix.getChildren().removeIf(node -> node instanceof VBox && GridPane.getRowIndex(node) >= 2);
        var allSeances = Database.getInstance().getSeances();

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


