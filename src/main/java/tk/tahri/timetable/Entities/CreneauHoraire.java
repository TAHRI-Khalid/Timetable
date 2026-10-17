package tk.tahri.timetable.Entities;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class CreneauHoraire {
    private long id;
    private String nom;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private int ordre;

    public CreneauHoraire() {
    }

    public CreneauHoraire(String nom, LocalTime heureDebut, LocalTime heureFin, int ordre) {
        this.nom = nom;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ordre = ordre;
    }

    public CreneauHoraire(long id, String nom, LocalTime heureDebut, LocalTime heureFin, int ordre) {
        this.id = id;
        this.nom = nom;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ordre = ordre;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public long getDureeMinutes() {
        if (heureDebut != null && heureFin != null) {
            return ChronoUnit.MINUTES.between(heureDebut, heureFin);
        }
        return 0;
    }

    public String getDureeFormattee() {
        long minutes = getDureeMinutes();
        if (minutes <= 0) return "--";
        long h = minutes / 60;
        long m = minutes % 60;
        return (m > 0) ? String.format("%02d H %02d Min", h, m) : String.format("%02d H", h);
    }

    public String getHeureDebutStr() {
        return heureDebut != null ? heureDebut.toString() : "";
    }

    public String getHeureFinStr() {
        return heureFin != null ? heureFin.toString() : "";
    }

    @Override
    public String toString() {
        return nom != null ? nom : "";
    }
}
