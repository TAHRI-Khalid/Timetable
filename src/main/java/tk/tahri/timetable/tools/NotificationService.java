package tk.tahri.timetable.tools;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.util.Optional;

/**
 * Service centralisé pour unifier les retours utilisateurs (notifications toast et dialogues).
 */
public final class NotificationService {

    private static final Duration DEFAULT_DURATION = Duration.seconds(3.5);

    public enum SaveChoice {
        SAVE,
        DONT_SAVE,
        CANCEL
    }

    private NotificationService() {
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Affiche une notification toast de succès (non bloquante).
     */
    public static void showSuccess(String title, String message) {
        runOnFxThread(() -> Notifications.create()
                .title(title != null ? title : "Succès")
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(DEFAULT_DURATION)
                .showInformation());
    }

    /**
     * Affiche une notification toast informative (non bloquante).
     */
    public static void showInfo(String title, String message) {
        runOnFxThread(() -> Notifications.create()
                .title(title != null ? title : "Information")
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(DEFAULT_DURATION)
                .showInformation());
    }

    /**
     * Affiche une notification toast d'avertissement (non bloquante).
     */
    public static void showWarning(String title, String message) {
        runOnFxThread(() -> Notifications.create()
                .title(title != null ? title : "Attention")
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(4.5))
                .showWarning());
    }

    /**
     * Affiche une notification toast d'erreur (non bloquante).
     */
    public static void showError(String title, String message) {
        runOnFxThread(() -> Notifications.create()
                .title(title != null ? title : "Erreur")
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(5))
                .showError());
    }

    /**
     * Boîte de dialogue modale de confirmation pour action de suppression.
     */
    public static boolean confirmDestruction(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title != null ? title : "Confirmation de suppression");
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType btnDelete = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnDelete, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnDelete;
    }

    /**
     * Boîte de dialogue modale pour la sauvegarde lors de la fermeture ou du changement de fichier.
     */
    public static SaveChoice confirmSaveOnClose(String actionDescription) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Modifications non enregistrées");
        alert.setHeaderText("Le fichier en cours a été modifié.");
        alert.setContentText("Voulez-vous enregistrer les modifications avant " + actionDescription + " ?");

        ButtonType okButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Ne pas enregistrer", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, noButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == okButton) {
                return SaveChoice.SAVE;
            } else if (result.get() == noButton) {
                return SaveChoice.DONT_SAVE;
            }
        }
        return SaveChoice.CANCEL;
    }

    /**
     * Boîte de dialogue modale d'erreur.
     */
    public static void showErrorDialog(String title, String header, String content) {
        runOnFxThread(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title != null ? title : "Erreur");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Boîte de dialogue modale d'avertissement.
     */
    public static void showWarningDialog(String title, String header, String content) {
        runOnFxThread(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title != null ? title : "Attention");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
