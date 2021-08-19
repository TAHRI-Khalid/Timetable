package tk.tahri.timetable.Entities;

import java.util.Arrays;
import java.util.List;

public class Espace {
    public static final List<String> Types = Arrays.asList("Salle", "Atelier");

    private long id;
    private String nom;
    private String type;

    public Espace() {
    }

    public Espace(String nom, String type) {
        this.nom = nom;
        this.type = type;
    }

    public Espace(long id, String nom, String type) {
        this.id = id;
        this.nom = nom;
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
