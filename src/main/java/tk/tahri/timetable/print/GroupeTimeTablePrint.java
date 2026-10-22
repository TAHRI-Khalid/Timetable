package tk.tahri.timetable.print;

import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.view.GroupePrintTimeTableView;

public class GroupeTimeTablePrint extends TimeTablePrint {
    private final Groupe groupe;

    public GroupeTimeTablePrint(Groupe groupe) {
        super(10, new GroupePrintTimeTableView(groupe));
        this.groupe = groupe;
        this.getChildren().addAll(
                header(),
                titre(groupe != null ? groupe.getNom() : ""),
                info(false, 30, infoGroupe()),
                body(),
                footer()
        );
    }

    private GridPane infoGroupe() {
        var grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(3);

        grid.add(new Text("Filière"), 0, 0);
        grid.add(new Text("Niveau"), 0, 1);
        grid.add(new Text("Année"), 0, 2);

        String filiere = (groupe != null && groupe.getFiliere() != null && groupe.getFiliere().getAbreviation() != null)
                ? groupe.getFiliere().getAbreviation() : "-";
        String niveau = (groupe != null && groupe.getFiliere() != null && groupe.getFiliere().getNiveau() != null)
                ? groupe.getFiliere().getNiveau() : "-";
        String annee = (groupe != null) ? groupe.getAnnee() + "A" : "-";

        grid.add(new Text(" : " + filiere), 1, 0);
        grid.add(new Text(" : " + niveau), 1, 1);
        grid.add(new Text(" : " + annee), 1, 2);

        return grid;
    }

}
