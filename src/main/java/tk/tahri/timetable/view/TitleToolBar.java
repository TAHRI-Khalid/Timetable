package tk.tahri.timetable.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.Duration;

public class TitleToolBar extends ToolBar {
    private final Label lblMasseHoraire;
    private final Label lblTitle;

    public TitleToolBar() {
        var space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        lblTitle = new Label();
        this.getItems().addAll(lblTitle, space);
        lblMasseHoraire = new Label();
        this.getItems().addAll(lblMasseHoraire);
    }

    public void setTitle(String lblTitle) {
        this.lblTitle.setText(lblTitle);
    }

    public void setMasseHoraire(Duration duration) {
        this.lblMasseHoraire.setText(String.format("%02d H %02d Min", duration.toHours(), duration.toMinutesPart()));
    }

    public void setIcon(Node icon) {
        this.lblTitle.setGraphicTextGap(20);
        this.lblTitle.setGraphic(icon);
    }
}
