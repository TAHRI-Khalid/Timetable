package tk.tahri.timetable.Entities;

import java.util.Arrays;
import java.util.List;

public class Formateur {
    public static final List<String> Types = Arrays.asList("Permanent", "Vacataire");

    private long id;
    private String nom;
    private String prenom;
    private String matricule;
    private String type;

    public Formateur() {
    }

    public Formateur(String nom, String prenom, String matricule, String type) {
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.type = type;
    }

    public Formateur(Long id, String nom, String prenom, String matricule, String type) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.type = type;
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return nom.toUpperCase() + " " + prenom;
    }
}
