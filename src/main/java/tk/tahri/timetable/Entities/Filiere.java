package tk.tahri.timetable.Entities;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Filiere {
    public static final List<String> Niveaux = Arrays.asList("Technicien Spécialisé", "Technicien", "Qualification","Spécialisation","Bac Pro","Parcours Collégial");
    public static final List<String> Secteurs = Arrays.asList("Bâtiment et Travaux Publics", "Digital& IA & Offshoring", "Hôtellerie et Tourisme","Industriel","Tertiaire","Transport et Logistique");
    public static final List<String> typeFormation = Arrays.asList("Diplômante","Parcours Professionnalisant","Qualifiante");

    private Long id;
    private String nom;
    private String abreviation;
    private String niveau;
    private String secteur;
    private String type;

    public Filiere() {
    }

    public Filiere(String nom, String abreviation, String niveau, String secteur, String type) {
        this.nom = nom;
        this.abreviation = abreviation;
        this.niveau = niveau;
        this.secteur = secteur;
        this.type = type;
    }

    public Filiere(Long id, String nom, String abreviation, String niveau, String secteur, String type) {
        this.id = id;
        this.nom = nom;
        this.abreviation = abreviation;
        this.niveau = niveau;
        this.secteur = secteur;
        this.type = type;
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

    public String getAbreviation() {
        return abreviation;
    }

    public void setAbreviation(String abreviation) {
        this.abreviation = abreviation;
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
        return this.nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filiere filiere = (Filiere) o;
        return id.equals(filiere.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
