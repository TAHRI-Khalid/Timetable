package tk.tahri.timetable.print;

import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.view.FormateurPrintTimeTableView;


public class FormateurTimeTablePrint extends TimeTablePrint {
    private final Formateur formateur;

    public FormateurTimeTablePrint(Formateur formateur) {
        super(10, new FormateurPrintTimeTableView(formateur));
        this.formateur = formateur;
        this.getChildren().addAll(
                header(),
                titre(formateur != null ? formateur.toString() : ""),
                info(false, 30, infoFormateur()),
                body(),
                footer()
        );
    }

    private GridPane infoFormateur() {
        var grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(3);

        grid.add(new Text("Matricule"), 0, 0);
        grid.add(new Text("Statut"), 0, 1);

        String mat = (formateur != null && formateur.getMatricule() != null) ? formateur.getMatricule() : "-";
        String statut = (formateur != null && formateur.getType() != null) ? formateur.getType() : "-";

        grid.add(new Text(" : " + mat), 1, 0);
        grid.add(new Text(" : " + statut), 1, 1);

        return grid;
    }
}
