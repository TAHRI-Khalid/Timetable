package tk.tahri.timetable.view;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import tk.tahri.timetable.Entities.CreneauHoraire;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.NotificationService;
import tk.tahri.timetable.tools.command.AddSeanceCommand;
import tk.tahri.timetable.tools.command.MoveSeanceCommand;
import tk.tahri.timetable.tools.command.ResizeSeanceCommand;
import tk.tahri.timetable.tools.command.UndoManager;
import tk.tahri.timetable.tools.command.UpdateSeanceDetailsCommand;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SeanceView extends VBox {
    private enum InteractionMode {
        NONE,
        RESIZING,
        ALT_DUPLICATING,
        DRAGGING
    }

    private static final double RESIZE_MARGIN = 10.0;

    private final Seance seance;
    private boolean selected = false;

    private InteractionMode interactionMode = InteractionMode.NONE;
    private boolean hasDragged = false;
    private double firstClickX;
    private double firstClickY;
    private DayOfWeek oldJour;
    private LocalTime oldDebut;
    private LocalTime oldFin;
    private SeanceView duplicateGhostView;

    public SeanceView(Seance seance) {
        this.seance = seance;
        this.setSpacing(0);
        this.setPadding(new Insets(3));
        this.getChildren().addAll(new Text(seance.getFormateur().toString()));
        this.getChildren().addAll(new Text(seance.getGroupes().get(0).getNom()));
        this.getChildren().add(new Text("[" + seance.getType() + "] : " + seance.getEspace().getNom()));
        this.getChildren().forEach(txt -> ((Text) txt).setFill(Color.WHITE));
    }

    public SeanceView(Seance seance, double width, double height, boolean draggable, SeanceType type) {
        this.seance = seance;
        switch (type) {
            case FORMATEUR -> {
                if (seance.getType().equals("FP")) {
                    this.getChildren().add(new Text(seance.getGroupes().get(0).toString()));
                    this.getChildren().add(new Text("[" + seance.getType() + "] : " + seance.getEspace()));
                } else {
                    this.getChildren().addAll(new Text(seance.getGroupes().get(0).getFiliere().getAbreviation() + " " + seance.getGroupes().get(0).getAnnee() + "A"));
                    this.getChildren().add(new Text("[" + seance.getType() + "] : " + seance.getEspace()));
                }
            }
            case GROUPE -> {
                this.getChildren().addAll(new Text(seance.getFormateur().toString()));
                if (seance.getType().equals("FP")) {
                    this.getChildren().add(new Text("[" + seance.getType() + "] : " + seance.getEspace()));
                } else {
                    this.getChildren().add(new Text("[" + seance.getType() + "]"));
                }
            }
            case ESPACE -> {
                this.getChildren().addAll(new Text(seance.getFormateur().toString()));
                if (seance.getType().equals("FP")) {
                    this.getChildren().add(new Text("[" + seance.getType() + "] : " + seance.getGroupes().get(0).toString()));
                } else {
                    this.getChildren().add(new Text("[" + seance.getType() + "] : " + seance.getGroupes().get(0).getFiliere().getAbreviation() + " " + seance.getGroupes().get(0).getAnnee() + "A"));
                }
            }
        }
        this.setPrefSize(seance.getDuree() / 15.0 * width, height);
        this.setMaxSize(seance.getDuree() / 15.0 * width, height);

        this.getChildren().forEach(txt -> {
            var text = (Text) txt;
            text.setFill(Color.WHITE);
            var w = this.getMaxWidth() - text.getBoundsInLocal().getWidth();
            if (w < 0) {
                var y = text.getBoundsInLocal().getWidth() + w;
                var length = text.getText().length();
                text.setText(text.getText().substring(0, (int) ((y * length) / text.getBoundsInLocal().getWidth())));
            }
            text.setTextAlignment(TextAlignment.CENTER);
        });

        if (draggable) {
            setFocusTraversable(true);

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setStyle("-fx-font-weight: 100;");

            var mCreneaux = new Menu("Créneaux officiels");
            var listCreneaux = Database.getInstance().getCreneaux();
            if (listCreneaux != null && !listCreneaux.isEmpty()) {
                for (var c : listCreneaux) {
                    var item = new MenuItem(c.getNom() + " (" + c.getHeureDebutStr() + " - " + c.getHeureFinStr() + ")");
                    if (this.seance.getDebut().equals(c.getHeureDebut()) && this.seance.getFin().equals(c.getHeureFin())) {
                        item.setText("✓ " + item.getText());
                    }
                    item.setOnAction(event -> applyCreneau(c));
                    mCreneaux.getItems().add(item);
                }
            }

            var mHeurs = new Menu("Ajuster la durée");
            var m45 = new MenuItem("00 H 45 Min");
            var h1 = new MenuItem("01 H 00 Min");
            var h115 = new MenuItem("01 H 15 Min");
            var h15 = new MenuItem("01 H 30 Min");
            var h2 = new MenuItem("02 H 00 Min");
            var h215 = new MenuItem("02 H 15 Min");
            var h25 = new MenuItem("02 H 30 Min");
            var h3 = new MenuItem("03 H 00 Min");
            var h35 = new MenuItem("03 H 30 Min");
            var h345 = new MenuItem("03 H 45 Min");
            var h4 = new MenuItem("04 H 00 Min");
            var h45 = new MenuItem("04 H 30 Min");
            var h5 = new MenuItem("05 H 00 Min");

            mHeurs.getItems().addAll(m45, h1, h115, h15, h2, h215, h25, h3, h35, h345, h4, h45, h5);

            m45.setOnAction(event -> this.updateTime(Duration.ofMinutes(45)));
            h1.setOnAction(event -> this.updateTime(Duration.ofMinutes(60)));
            h115.setOnAction(event -> this.updateTime(Duration.ofMinutes(75)));
            h15.setOnAction(event -> this.updateTime(Duration.ofMinutes(90)));
            h2.setOnAction(event -> this.updateTime(Duration.ofMinutes(120)));
            h215.setOnAction(event -> this.updateTime(Duration.ofMinutes(135)));
            h25.setOnAction(event -> this.updateTime(Duration.ofMinutes(150)));
            h3.setOnAction(event -> this.updateTime(Duration.ofMinutes(180)));
            h35.setOnAction(event -> this.updateTime(Duration.ofMinutes(210)));
            h345.setOnAction(event -> this.updateTime(Duration.ofMinutes(225)));
            h4.setOnAction(event -> this.updateTime(Duration.ofMinutes(240)));
            h45.setOnAction(event -> this.updateTime(Duration.ofMinutes(270)));
            h5.setOnAction(event -> this.updateTime(Duration.ofMinutes(300)));

            var mtype = new Menu("Type");
            var fad = new MenuItem("FAD : Distancielle");
            mtype.getItems().addAll(fad);
            fad.setOnAction(event -> updateTypeDeSeance("FAD"));

            var mEspace = new Menu("Changer l'espace");
            var listEspaces = Database.getInstance().getEspaces();
            listEspaces.stream().map(Espace::getType).distinct().forEach(typeEspace -> {
                var menu = new Menu(typeEspace);
                mEspace.getItems().add(menu);
                listEspaces.stream().filter(espace -> espace.getType().equals(typeEspace)).forEach(espace -> {
                    var submenu = new MenuItem(espace.getNom());
                    submenu.setOnAction(event -> updateEspace(espace));
                    menu.getItems().add(submenu);
                });
            });

            var mFormateur = new Menu("Changer le formateur");
            var listFormateurs = Database.getInstance().getFormateurs();
            listFormateurs.stream().map(Formateur::getType).distinct().forEach(typeFormateur -> {
                var menu = new Menu(typeFormateur);
                mFormateur.getItems().add(menu);
                listFormateurs.stream().filter(formateur -> formateur.getType().equals(typeFormateur)).forEach(formateur -> {
                    var submenu = new MenuItem(formateur.toString());
                    submenu.setOnAction(event -> updateFormateur(formateur));
                    menu.getItems().add(submenu);
                });
            });

            var mGroupe = new Menu("Changer le groupe");
            var listGroupes = Database.getInstance().getGroupes();
            listGroupes.stream().map(g -> g.getFiliere().getNiveau()).distinct().forEach(niveau -> {
                var menu = new Menu(niveau);
                mGroupe.getItems().add(menu);
                listGroupes.stream().filter(groupe -> groupe.getFiliere().getNiveau().equals(niveau)).forEach(groupe -> {
                    var submenu = new MenuItem(groupe.getNom());
                    submenu.setOnAction(event -> updateGroupe(groupe));
                    menu.getItems().add(submenu);
                });
            });

            if (listCreneaux != null && !listCreneaux.isEmpty()) {
                contextMenu.getItems().add(mCreneaux);
            }
            contextMenu.getItems().addAll(mHeurs, mtype);
            if (type == SeanceType.FORMATEUR || type == SeanceType.GROUPE) {
                contextMenu.getItems().add(mEspace);
            }
            if (type == SeanceType.ESPACE || type == SeanceType.GROUPE) {
                contextMenu.getItems().add(mFormateur);
            }
            if (this.seance.getType().equals("FP") && (type == SeanceType.FORMATEUR || type == SeanceType.ESPACE)) {
                contextMenu.getItems().add(mGroupe);
            }

            var mDupliquerItem = new MenuItem("Dupliquer (Alt + Glisser / Ctrl+D)");
            mDupliquerItem.setOnAction(e -> {
                if (this.getParent() instanceof MainTimeTableView parent) {
                    parent.setSelectedSeanceView(this);
                    parent.duplicateSelectedSeance();
                }
            });

            var mSuppr = new MenuItem("Supprimer (Suppr)");
            mSuppr.setOnAction(event -> {
                if (this.getParent() instanceof MainTimeTableView parent) {
                    parent.setSelectedSeanceView(this);
                    parent.deleteSelectedSeance();
                }
            });

            contextMenu.getItems().addAll(new SeparatorMenuItem(), mDupliquerItem, mSuppr);

            // Gestion de l'apparence du curseur sur le bord droit (Redimensionnement)
            this.setOnMouseMoved(event -> {
                if (event.getX() >= this.getWidth() - RESIZE_MARGIN) {
                    this.setCursor(Cursor.E_RESIZE);
                } else {
                    this.setCursor(Cursor.HAND);
                }
            });

            this.setOnMouseExited(event -> {
                if (interactionMode == InteractionMode.NONE) {
                    this.setCursor(Cursor.DEFAULT);
                }
            });

            // Appui souris
            this.setOnMousePressed(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (this.getParent() instanceof MainTimeTableView parent) {
                        parent.setSelectedSeanceView(this);
                        this.requestFocus();
                        parent.refreshView(this.getSeance());
                    }

                    this.firstClickX = event.getX();
                    this.firstClickY = event.getY();
                    this.oldJour = seance.getJour();
                    this.oldDebut = seance.getDebut();
                    this.oldFin = seance.getFin();
                    this.hasDragged = false;

                    if (event.getX() >= this.getWidth() - RESIZE_MARGIN) {
                        interactionMode = InteractionMode.RESIZING;
                        this.setCursor(Cursor.E_RESIZE);
                    } else if (event.isAltDown()) {
                        interactionMode = InteractionMode.ALT_DUPLICATING;
                        if (this.getParent() instanceof MainTimeTableView parent) {
                            Seance cloneSeance = new Seance(this.seance);
                            cloneSeance.setId(0);
                            duplicateGhostView = new SeanceView(cloneSeance, width, height, false, type);
                            duplicateGhostView.setTranslateX(this.getTranslateX());
                            duplicateGhostView.setTranslateY(this.getTranslateY());
                            duplicateGhostView.getStyleClass().add("seance-duplicate-ghost");
                            parent.getChildren().add(duplicateGhostView);
                        }
                    } else {
                        interactionMode = InteractionMode.DRAGGING;
                    }
                    event.consume();
                }
            });

            // Glisser souris
            this.setOnMouseDragged(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    hasDragged = true;
                    if (interactionMode == InteractionMode.RESIZING) {
                        if (this.getParent() instanceof MainTimeTableView parent) {
                            double proposedWidth = Math.max(width, event.getX());
                            long maxMinutes = ChronoUnit.MINUTES.between(seance.getDebut(), parent.getEndTime());
                            double maxWidth = (maxMinutes / 15.0) * width;
                            proposedWidth = Math.min(proposedWidth, maxWidth);
                            this.setPrefWidth(proposedWidth);
                            this.setMaxWidth(proposedWidth);
                        }
                    } else if (interactionMode == InteractionMode.ALT_DUPLICATING && duplicateGhostView != null) {
                        duplicateGhostView.setTranslateX(duplicateGhostView.getTranslateX() - firstClickX + event.getX());
                        duplicateGhostView.setTranslateY(duplicateGhostView.getTranslateY() - firstClickY + event.getY());
                    } else if (interactionMode == InteractionMode.DRAGGING) {
                        this.setTranslateX(this.getTranslateX() - firstClickX + event.getX());
                        this.setTranslateY(this.getTranslateY() - firstClickY + event.getY());
                    }
                    event.consume();
                }
            });

            // Relâchement souris
            this.setOnMouseReleased(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (this.getParent() instanceof MainTimeTableView parent) {
                        if (interactionMode == InteractionMode.RESIZING) {
                            if (hasDragged) {
                                double currentW = this.getPrefWidth();
                                long rawMinutes = Math.round((currentW / width) * 15.0);
                                long snappedMinutes = Math.max(15, ((rawMinutes + 7) / 15) * 15);

                                var creneaux = Database.getInstance().getCreneaux();
                                if (creneaux != null) {
                                    for (var c : creneaux) {
                                        if (c.getHeureDebut().equals(seance.getDebut())) {
                                            long cDur = ChronoUnit.MINUTES.between(c.getHeureDebut(), c.getHeureFin());
                                            if (Math.abs(snappedMinutes - cDur) <= 15) {
                                                snappedMinutes = cDur;
                                                break;
                                            }
                                        }
                                    }
                                }

                                LocalTime newFin = seance.getDebut().plusMinutes(snappedMinutes);
                                if (newFin.isAfter(parent.getEndTime())) {
                                    newFin = parent.getEndTime();
                                }

                                if (!newFin.equals(oldFin)) {
                                    seance.setFin(newFin);
                                    parent.updateSeance(seance);
                                    UndoManager.getInstance().pushExecuted(new ResizeSeanceCommand(parent, seance, oldFin, newFin));
                                    NotificationService.showInfo("Durée modifiée", "Séance de " + seance.getDebut() + " à " + newFin + " (" + seance.getDuree() + " min, Ctrl+Z pour annuler)");
                                } else {
                                    parent.loadSeanceViews(true);
                                }
                            }
                            this.setCursor(Cursor.DEFAULT);
                        } else if (interactionMode == InteractionMode.ALT_DUPLICATING) {
                            if (hasDragged && duplicateGhostView != null) {
                                parent.placeSeance(duplicateGhostView);
                                var cloneSeance = duplicateGhostView.getSeance();
                                parent.getChildren().remove(duplicateGhostView);
                                UndoManager.getInstance().execute(new AddSeanceCommand(parent, cloneSeance));
                                NotificationService.showSuccess("Séance dupliquée", "Séance dupliquée avec succès (Ctrl+Z pour annuler).");
                            } else if (duplicateGhostView != null) {
                                parent.getChildren().remove(duplicateGhostView);
                            }
                            duplicateGhostView = null;
                        } else if (interactionMode == InteractionMode.DRAGGING) {
                            if (hasDragged) {
                                parent.placeSeance(this);
                                DayOfWeek newJour = seance.getJour();
                                LocalTime newDebut = seance.getDebut();
                                LocalTime newFin = seance.getFin();
                                if (!oldJour.equals(newJour) || !oldDebut.equals(newDebut) || !oldFin.equals(newFin)) {
                                    parent.updateSeance(seance);
                                    UndoManager.getInstance().pushExecuted(new MoveSeanceCommand(parent, seance, oldJour, oldDebut, oldFin, newJour, newDebut, newFin));
                                }
                            }
                        }
                    }
                    interactionMode = InteractionMode.NONE;
                    hasDragged = false;
                    event.consume();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(this, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });

            // Raccourcis clavier (Suppr, Ctrl+D)
            this.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                    if (this.getParent() instanceof MainTimeTableView parent) {
                        parent.deleteSelectedSeance();
                        event.consume();
                    }
                } else if (event.getCode() == KeyCode.D && event.isControlDown()) {
                    if (this.getParent() instanceof MainTimeTableView parent) {
                        parent.duplicateSelectedSeance();
                        event.consume();
                    }
                }
            });
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            if (!this.getStyleClass().contains("seance-selected")) {
                this.getStyleClass().add("seance-selected");
            }
        } else {
            this.getStyleClass().remove("seance-selected");
        }
    }

    public Seance getSeance() {
        return seance;
    }

    private void applyCreneau(CreneauHoraire c) {
        if (this.getParent() instanceof MainTimeTableView parent) {
            DayOfWeek oldJ = seance.getJour();
            LocalTime oldD = seance.getDebut();
            LocalTime oldF = seance.getFin();

            seance.setDebut(c.getHeureDebut());
            seance.setFin(c.getHeureFin());
            parent.updateSeance(seance);
            UndoManager.getInstance().pushExecuted(new MoveSeanceCommand(parent, seance, oldJ, oldD, oldF, oldJ, c.getHeureDebut(), c.getHeureFin()));
            NotificationService.showInfo("Créneau appliqué", c.getNom() + " (" + c.getHeureDebutStr() + " - " + c.getHeureFinStr() + ")");
        }
    }

    private void updateTime(Duration duration) {
        if (this.getParent() instanceof MainTimeTableView parent) {
            LocalTime oldF = seance.getFin();
            LocalTime newF = seance.getDebut().plusMinutes(duration.toMinutes());
            if (newF.isAfter(parent.getEndTime())) {
                newF = parent.getEndTime();
            }
            seance.setFin(newF);
            parent.updateSeance(seance);
            UndoManager.getInstance().pushExecuted(new ResizeSeanceCommand(parent, seance, oldF, newF));
            NotificationService.showInfo("Durée modifiée", "Nouvelle durée : " + seance.getDuree() + " min");
        }
    }

    private void updateEspace(Espace espace) {
        if (this.getParent() instanceof MainTimeTableView parent) {
            Espace oldEspace = seance.getEspace();
            UndoManager.getInstance().execute(new UpdateSeanceDetailsCommand(
                    parent, seance,
                    seance.getFormateur(), seance.getFormateur(),
                    oldEspace, espace,
                    seance.getType(), seance.getType(),
                    seance.getGroupes(), seance.getGroupes()
            ));
            NotificationService.showSuccess("Espace modifié", "Salle assignée : " + espace.getNom());
        }
    }

    private void updateFormateur(Formateur formateur) {
        if (this.getParent() instanceof MainTimeTableView parent) {
            Formateur oldFormateur = seance.getFormateur();
            UndoManager.getInstance().execute(new UpdateSeanceDetailsCommand(
                    parent, seance,
                    oldFormateur, formateur,
                    seance.getEspace(), seance.getEspace(),
                    seance.getType(), seance.getType(),
                    seance.getGroupes(), seance.getGroupes()
            ));
            NotificationService.showSuccess("Formateur modifié", "Formateur assigné : " + formateur);
        }
    }

    private void updateGroupe(Groupe groupe) {
        if (this.getParent() instanceof MainTimeTableView parent) {
            List<Groupe> oldGroupes = new ArrayList<>(seance.getGroupes());
            List<Groupe> newGroupes = List.of(groupe);
            UndoManager.getInstance().execute(new UpdateSeanceDetailsCommand(
                    parent, seance,
                    seance.getFormateur(), seance.getFormateur(),
                    seance.getEspace(), seance.getEspace(),
                    seance.getType(), seance.getType(),
                    oldGroupes, newGroupes
            ));
            NotificationService.showSuccess("Groupe modifié", "Groupe assigné : " + groupe.getNom());
        }
    }

    private void updateTypeDeSeance(String type) {
        if (this.getParent() instanceof MainTimeTableView parent) {
            String oldType = seance.getType();
            List<Groupe> oldGroupes = new ArrayList<>(seance.getGroupes());
            List<Groupe> newGroupes = new ArrayList<>(seance.getGroupes());

            if (type.equals("FAD")) {
                var groupes = Database.getInstance().getGroupesSimilaire(seance.getGroupes().get(0));
                for (var grp : groupes) {
                    if (newGroupes.stream().noneMatch(g -> g.getId() == grp.getId())) {
                        newGroupes.add(grp);
                    }
                }
            }

            UndoManager.getInstance().execute(new UpdateSeanceDetailsCommand(
                    parent, seance,
                    seance.getFormateur(), seance.getFormateur(),
                    seance.getEspace(), seance.getEspace(),
                    oldType, type,
                    oldGroupes, newGroupes
            ));
            NotificationService.showSuccess("Type modifié", "Type de séance : " + type);
        }
    }
}
