package tk.tahri.timetable.Entities;

import java.util.Arrays;
import java.util.List;

public class Groupe {
    public static final List<String> Niveaux = Arrays.asList("Technicien Spécialisé", "Technicien", "Qualification","Spécialisation","Bac Pro","Parcours Collégial");
    public static final List<String> Secteurs = Arrays.asList("Bâtiment et Travaux Publics", "Digital& IA & Offshoring", "Hôtellerie et Tourisme","Industriel","Tertiaire","Transport et Logistique");
    public static final List<String> typeFormation = Arrays.asList("Diplômante","Parcours Professionnalisant","Qualifiante");

    private Long id;
    private String nom;
    private int annee;
    private String filiere;
    private String niveau;
    private String secteur;
    private String type;

    public Groupe() {
    }

    public Groupe(String nom, int annee, String filiere, String niveau, String secteur,String type) {
        this.nom = nom;
        this.annee = annee;
        this.filiere = filiere;
        this.niveau = niveau;
        this.secteur = secteur;
        this.type =type;
    }

    public Groupe(Long id, String nom, int annee, String filiere, String niveau, String secteur,String type) {
        this.id = id;
        this.nom = nom;
        this.annee = annee;
        this.filiere = filiere;
        this.niveau = niveau;
        this.secteur = secteur;
        this.type =type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return nom;
    }
}
