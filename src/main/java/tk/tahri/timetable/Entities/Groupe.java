package tk.tahri.timetable.Entities;

public class Groupe {
    private Long id;
    private String nom;
    private int annee;
    private Filiere filiere;

    public Groupe() {
    }

    public Groupe(String nom, int annee, Filiere filiere) {
        this.nom = nom;
        this.annee = annee;
        this.filiere = filiere;
    }

    public Groupe(Long id, String nom, int annee, Filiere filiere) {
        this.id = id;
        this.nom = nom;
        this.annee = annee;
        this.filiere = filiere;
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

    public Filiere getFiliere() {
        return filiere;
    }

    public void setFiliere(Filiere filiere) {
        this.filiere = filiere;
    }

    @Override
    public String toString() {
        return nom;
    }
}
