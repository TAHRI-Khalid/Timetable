package tk.tahri.timetable.Entities;

import java.time.LocalDate;

public class Etablissement {
    private Long id;
    private String code;
    private String direction;
    private String complexe;
    private String nom;
    private String abreviation;
    private String anneeDeFormation;
    private String username;
    private String password;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    public Etablissement() {
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getComplexe() {
        return complexe;
    }

    public void setComplexe(String complexe) {
        this.complexe = complexe;
    }

    public String getAnneeDeFormation() {
        return anneeDeFormation;
    }

    public void setAnneeDeFormation(String anneeDeFormation) {
        this.anneeDeFormation = anneeDeFormation;
    }

    public String getAbreviation() {
        return abreviation;
    }

    public void setAbreviation(String abreviation) {
        this.abreviation = abreviation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return this.abreviation;
    }
}
