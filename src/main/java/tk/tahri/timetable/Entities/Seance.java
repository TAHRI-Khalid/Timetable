package tk.tahri.timetable.Entities;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Seance {
    private long id;
    private LocalTime debut;
    private LocalTime fin;
    private DayOfWeek jour;
    private String type="FP";
    private Groupe groupe;
    private Formateur formateur;
    private Espace espace;

    public Seance() {
    }

    public Seance(LocalTime debut, LocalTime fin, DayOfWeek dayOfWeek,String  type, Groupe groupe, Formateur formateur, Espace espace) {
        this.debut = debut;
        this.fin = fin;
        this.jour = dayOfWeek;
        this.type=type;
        this.groupe = groupe;
        this.formateur = formateur;
        this.espace = espace;
    }

    public Seance(Groupe groupe, Formateur formateur, Espace espace) {
        this.groupe = groupe;
        this.formateur = formateur;
        this.espace = espace;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalTime getDebut() {
        return debut;
    }

    public void setDebut(LocalTime debut) {
        this.debut = debut;
    }

    public LocalTime getFin() {
        return fin;
    }

    public void setFin(LocalTime fin) {
        this.fin = fin;
    }

    public DayOfWeek getJour() {
        return jour;
    }

    public void setJour(DayOfWeek jour) {
        this.jour = jour;
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }

    public Formateur getFormateur() {
        return formateur;
    }

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
    }

    public Espace getEspace() {
        return espace;
    }

    public void setEspace(Espace espace) {
        this.espace = espace;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", debut=" + debut +
                ", fin=" + fin +
                ", jour=" + jour +
                ", type='" + type + '\'' +
                ", groupe=" + groupe +
                ", formateur=" + formateur +
                ", espace=" + espace +
                '}';
    }

    public long getDuree() {
        return ChronoUnit.MINUTES.between(this.getDebut(), this.getFin());
    }


}
