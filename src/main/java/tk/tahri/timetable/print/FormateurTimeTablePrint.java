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
                info(info1(), info2()),
                body(),
                footer("Signature du Formateur", "Visa de la Direction")
        );
    }

    private GridPane info1() {
        var grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(3);

        grid.add(new Text("Statut"), 0, 0);
        String statut = (formateur != null && formateur.getType() != null) ? formateur.getType() : "-";
        grid.add(new Text(" : " + statut), 1, 0);
        return grid;
    }

    private GridPane info2() {
        var grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(3);

        grid.add(new Text("Matricule"), 0, 0);
        grid.add(new Text("Nom"), 0, 1);
        grid.add(new Text("Prénom"), 0, 2);

        String mat = (formateur != null && formateur.getMatricule() != null) ? formateur.getMatricule() : "-";
        String nom = (formateur != null && formateur.getNom() != null) ? formateur.getNom() : "-";
        String prenom = (formateur != null && formateur.getPrenom() != null) ? formateur.getPrenom() : "-";

        grid.add(new Text(" : " + mat), 1, 0);
        grid.add(new Text(" : " + nom), 1, 1);
        grid.add(new Text(" : " + prenom), 1, 2);

        return grid;
    }
}
