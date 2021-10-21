package tk.tahri.timetable.tabs;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.CheckListView;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.FormateurTimeTablePrint;
import tk.tahri.timetable.tools.MailService;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MailTab extends EditorTab {

    private final CheckListView<Formateur> formateurCheckListView;
    private final ProgressBar progressBar;
    private final TextField txtSubject;
    private final TextArea txtMessage;

    public MailTab(String nom) {
        super(nom);
        FontAwesome fontAwesome = new FontAwesome();
        var vBox = new VBox(15);
        vBox.setPadding(new Insets(20));

        var groupeTitle = new Label("Espace d'e-mail : ");
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.SEND).color(Color.WHITE).size(26));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");
        vBox.getChildren().add(groupeTitle);

        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setAlignment(Pos.TOP_CENTER);

        progressBar = new ProgressBar(0);
        GridPane.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        grid.add(progressBar, 0, 0, 2, 1);

        grid.add(new Label("Formateurs :"), 0, 1);
        formateurCheckListView = new CheckListView<>();
        formateurCheckListView.prefHeightProperty().bind(vBox.heightProperty().subtract(60));
        grid.add(formateurCheckListView, 0, 2, 1, 3);

        var hBox2 = new HBox(20);
        var btnFormateurAdd = new Button();
        btnFormateurAdd.setOnAction(actionEvent ->
                IntStream.range(0, formateurCheckListView.getItems().size())
                        .forEach(formateurCheckListView.getCheckModel()::check));
        btnFormateurAdd.setGraphic(fontAwesome.create(FontAwesome.Glyph.PLUS_SQUARE).size(16));

        var btnFormateurRemove = new Button();
        btnFormateurRemove.setOnAction(actionEvent ->
                IntStream.range(0, formateurCheckListView.getItems().size())
                        .forEach(formateurCheckListView.getCheckModel()::clearCheck));
        btnFormateurRemove.setGraphic(fontAwesome.create(FontAwesome.Glyph.MINUS_SQUARE).size(16));
        hBox2.getChildren().addAll(btnFormateurAdd, btnFormateurRemove);
        hBox2.setAlignment(Pos.CENTER);
        grid.add(hBox2, 0, 5);

        var lblSujet = new Label("Sujet :");
        grid.add(lblSujet, 1, 1);
        txtSubject = new TextField("Emploi du temps");
        grid.add(txtSubject, 1, 2);

        var lblMessage = new Label("Message :");
        grid.add(lblMessage, 1, 3);
        txtMessage = new TextArea("Salam\nVous trouverez ci-joint l'emploi du temps");
        grid.add(txtMessage, 1, 4);

        var btnPrint = new Button("Envoyer");
        btnPrint.setMaxWidth(Double.MAX_VALUE);
        btnPrint.setOnAction(e -> sendMail());
        GridPane.setHalignment(btnPrint, HPos.CENTER);
        grid.add(btnPrint, 1, 5);

        vBox.getChildren().add(grid);
        this.setContent(vBox);
    }

    private void sendMail() {
        progressBar.progressProperty().setValue(-1);
        var etab = Database.getInstance().getItablissment();
        var map = new HashMap<Formateur, WritableImage>();
        formateurCheckListView.getCheckModel().getCheckedItems().forEach(formateur -> {
            var tt = new FormateurTimeTablePrint(formateur);
            tt.setPadding(new Insets(10, 10, 10, 10));
            tt.applyCss();
            tt.layout();
            tt.autosize();
            var snapshot = tt.snapshot(null, null);
            map.put(formateur, snapshot);
        });
        MailService service = new MailService(map, txtSubject.getText(), txtMessage.getText(), etab);
        progressBar.progressProperty().bind(service.progressProperty());
        service.valueProperty().addListener((observableValue, formateur, t1) -> {
            var index = formateurCheckListView.getCheckModel().getItemIndex(t1);
            formateurCheckListView.getCheckModel().clearCheck(index);
        });
        service.setOnSucceeded(workerStateEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Les emplois du temps ont été envoyés avec succés");
            alert.showAndWait();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });
        service.setOnFailed(workerStateEvent -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Envoi du message impossible. Vérifiez votre réseau et réessaye.");
            alert.show();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });
        service.start();
    }

    @Override
    protected void selectionChanged() {
        formateurCheckListView.getItems().clear();
        formateurCheckListView.getItems().addAll(Database.getInstance().getFormateurs().stream().filter(f -> Objects.nonNull(f.getMail())).collect(Collectors.toList()));
    }

    @Override
    public void print() {

    }
}
