package tk.tahri.timetable.tools;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.function.BiPredicate;

/**
 * Utilitaires pour standardiser l'expérience utilisateur des formulaires et des tableaux (Phase 2).
 */
public final class FormUtils {

    private FormUtils() {
    }

    /**
     * Lie un écouteur sur le champ texte pour effacer automatiquement le style d'erreur dès la saisie.
     */
    public static void bindFieldValidator(TextInputControl control) {
        control.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                control.getStyleClass().remove("error-field");
            }
        });
    }

    /**
     * Lie un écouteur sur un ComboBox pour effacer le style d'erreur lors de la sélection.
     */
    public static <T> void bindFieldValidator(ComboBox<T> combo) {
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                combo.getStyleClass().remove("error-field");
            }
        });
    }

    /**
     * Marque visuellement un champ comme invalide (bordure rouge) et positionne le focus.
     */
    public static void setFieldError(Control control, boolean isError) {
        if (isError) {
            if (!control.getStyleClass().contains("error-field")) {
                control.getStyleClass().add("error-field");
            }
            control.requestFocus();
        } else {
            control.getStyleClass().remove("error-field");
        }
    }

    /**
     * Crée une barre de recherche réactive avec filtrage en direct et compteur de résultats.
     */
    public static <T> HBox createSearchToolbar(
            TableView<T> table,
            ObservableList<T> masterList,
            String promptText,
            BiPredicate<T, String> filterPredicate) {

        FilteredList<T> filteredData = new FilteredList<>(masterList, p -> true);
        SortedList<T> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        var searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getStyleClass().add("table-toolbar");

        FontAwesome fa = new FontAwesome();
        var icon = fa.create(FontAwesome.Glyph.SEARCH).size(14);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText(promptText);
        txtSearch.getStyleClass().add("search-field");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        Button btnClear = new Button();
        btnClear.setGraphic(fa.create(FontAwesome.Glyph.TIMES).size(12));
        btnClear.getStyleClass().add("btn-clear-search");
        btnClear.visibleProperty().bind(txtSearch.textProperty().isNotEmpty());
        btnClear.setOnAction(e -> txtSearch.clear());

        Label lblCount = new Label();
        lblCount.getStyleClass().add("counter-badge");

        Runnable updateCount = () -> {
            int total = masterList.size();
            int displayed = filteredData.size();
            if (txtSearch.getText() != null && !txtSearch.getText().trim().isEmpty()) {
                lblCount.setText(displayed + " / " + total + " élément(s)");
            } else {
                lblCount.setText(total + " élément(s)");
            }
        };

        masterList.addListener((javafx.collections.ListChangeListener<T>) c -> updateCount.run());

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(item -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    return true;
                }
                return filterPredicate.test(item, newVal.trim().toLowerCase());
            });
            updateCount.run();
        });

        searchBox.getChildren().addAll(icon, txtSearch, btnClear, lblCount);
        updateCount.run();

        Label placeholder = new Label("Aucun enregistrement correspondant trouvé");
        placeholder.getStyleClass().add("table-placeholder");
        table.setPlaceholder(placeholder);

        return searchBox;
    }
}
