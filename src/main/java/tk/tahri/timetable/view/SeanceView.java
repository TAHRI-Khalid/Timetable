package tk.tahri.timetable.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeanceView extends VBox {
    private final Seance seance;
    private double firstClickX;
    private double firstClickY;

    public SeanceView(Seance seance) {
        this.seance = seance;
        this.setSpacing(0);
        this.setPadding(new Insets(3));
        this.getChildren().addAll(new Text(seance.getFormateur().toString()));
        this.getChildren().addAll(new Text(seance.getGroupes().get(0).getNom()));
        this.getChildren().add(new Text("[" + seance.getType() + "] : " + seance.getEspace().getNom()));
        this.getChildren().forEach(txt -> {
            ((Text) txt).setFill(Color.WHITE);
        });
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
            //text.applyCss();
            text.setFill(Color.WHITE);
            var w = this.getMaxWidth() - text.getBoundsInLocal().getWidth();
            if (w < 0) {
                var y=text.getBoundsInLocal().getWidth()+w;
                var length=text.getText().length();
                text.setText(text.getText().substring(0,(int) ((y* length)/text.getBoundsInLocal().getWidth())));
            }
            //text.setWrappingWidth(seance.getDuree() / 15.5 * width);
            text.setTextAlignment(TextAlignment.CENTER);
        });


        if (draggable) {
            AtomicBoolean isDragging = new AtomicBoolean(false);

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setStyle("-fx-font-weight: 100;");

            var mHeurs = new Menu("Créneau horaire");
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

            mHeurs.getItems().addAll(m45,h1, h115, h15, h2,h215, h25, h3, h35,h345, h4, h45, h5);

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

            var mSuppr = new MenuItem("Supprimer");
            mSuppr.setOnAction(event -> {
                ((MainTimeTableView) this.getParent()).deleteSeance(this.getSeance());
            });

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
                    submenu.setOnAction(event -> {
                        this.seance.setEspace(espace);
                        ((MainTimeTableView) this.getParent()).updateSeance(this.seance);
                    });
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
                    submenu.setOnAction(event -> {
                        this.seance.setFormateur(formateur);
                        ((MainTimeTableView) this.getParent()).updateSeance(this.seance);
                    });
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
                    submenu.setOnAction(event -> {
                        Database.getInstance().execute("UPDATE seance_groupe SET idGroupe=? WHERE idSeance=?",
                                String.valueOf(groupe.getId()),
                                String.valueOf(this.seance.getId()));
                        this.seance.getGroupes().clear();
                        this.seance.getGroupes().add(groupe);

                        ((MainTimeTableView) this.getParent()).refreshView(this.seance);
                        ((MainTimeTableView) this.getParent()).loadSeanceViews(true);

                    });
                    menu.getItems().add(submenu);
                });
            });

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
            contextMenu.getItems().addAll(new SeparatorMenuItem(), mSuppr);

            this.setOnMousePressed(event -> {
                this.firstClickX = event.getX();
                this.firstClickY = event.getY();
                ((MainTimeTableView) this.getParent()).refreshView(this.getSeance());
            });

            this.setOnMouseDragged(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    this.setTranslateX(this.getTranslateX() - firstClickX + event.getX());
                    this.setTranslateY(this.getTranslateY() - firstClickY + event.getY());
                    isDragging.set(true);
                }
            });

            this.setOnMouseReleased(event -> {
                if (event.getButton() == MouseButton.PRIMARY && isDragging.get()) {
                    ((MainTimeTableView) this.getParent()).placeSeance(this);
                    ((MainTimeTableView) this.getParent()).updateSeance(this.getSeance());
                    isDragging.set(false);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(this, event.getScreenX(), event.getScreenY());
                }
            });
        }
    }

    public Seance getSeance() {
        return seance;
    }

    private void updateTime(Duration duration) {
        this.seance.setFin(this.seance.getDebut().plusMinutes(duration.toMinutes()));
        ((MainTimeTableView) this.getParent()).updateSeance(this.getSeance());
    }

    private void updateTypeDeSeance(String type) {
        this.seance.setType(type);

        var groupes = Database.getInstance().getGroupesSimilaire(seance.getGroupes().get(0));
        if (type.equals("FAD")) {
            groupes.stream().filter(g -> g.getId() != seance.getGroupes().get(0).getId()).forEach(grp -> {
                Database.getInstance().execute("INSERT INTO seance_groupe VALUES(?,?)",
                        String.valueOf(grp.getId()),
                        String.valueOf(this.seance.getId()));
            });
        }

        ((MainTimeTableView) this.getParent()).updateSeance(this.getSeance());

    }
}
