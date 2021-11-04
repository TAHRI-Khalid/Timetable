package tk.tahri.timetable.print;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.view.FormateurPrintTimeTableView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class FormateurTimeTablePrint extends TimeTablePrint {
    private final Formateur formateur;

    public FormateurTimeTablePrint(Formateur formateur) {
        super(15, new FormateurPrintTimeTableView(formateur));
        this.formateur = formateur;
        this.getChildren().addAll(header(), titre(formateur.toString()), info(info1(), info2()), body(),footer());
    }

    private GridPane info1() {
        var grid = new GridPane();
        grid.add(new Text("Statut"), 0, 0);
        grid.add(new Text(" : " + formateur.getType()), 1, 0);
        return grid;
    }

    private GridPane info2() {
        var grid = new GridPane();

        grid.add(new Text("Matricule"), 0, 0);
        grid.add(new Text("Nom"), 0, 1);
        grid.add(new Text("Prénom"), 0, 2);

        grid.add(new Text(" : " + formateur.getMatricule()), 1, 0);
        grid.add(new Text(" : " + formateur.getNom()), 1, 1);
        grid.add(new Text(" : " + formateur.getPrenom()), 1, 2);

        return grid;
    }
    private HBox footer() {
        var lbl=new Label("Date d’émission : "+ LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lbl.setFont(new Font(9));
        HBox hBox = new HBox(lbl);
        hBox.setFillHeight(true);
        hBox.setAlignment(Pos.BOTTOM_LEFT);
        hBox.setPadding(new Insets(20,0,0,0));
        return hBox;
    }

}
