package tk.tahri.timetable.Entities;

import java.util.Arrays;
import java.util.List;

public class Groupe {
    public static final List<String> Niveaux = Arrays.asList("Technicien Spécialisé", "Technicien", "Qualification");
    public static final List<String> Secteurs = Arrays.asList("Industriel", "Tertiaire", "Agroalimentaire");

    private Long id;
    private String nom;
    private int annee;
    private String filiere;
    private String niveau;
    private String secteur;

    public Groupe() {
    }

    public Groupe(String nom, int annee, String filiere, String niveau, String secteur) {
        this.nom = nom;
        this.annee = annee;
        this.filiere = filiere;
        this.niveau = niveau;
        this.secteur = secteur;
    }

    public Groupe(Long id, String nom, int annee, String filiere, String niveau, String secteur) {
        this.id = id;
        this.nom = nom;
        this.annee = annee;
        this.filiere = filiere;
        this.niveau = niveau;
        this.secteur = secteur;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    @Override
    public String toString() {
        return nom;
    }
}
