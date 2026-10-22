package tk.tahri.timetable.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Dashboard {

    public record FormateurMasseHoraire(Formateur formateur, long dureeSecondes) {
        public String getNomComplet() {
            return formateur.getNom().toUpperCase() + " " + formateur.getPrenom();
        }
        public String getType() {
            return formateur.getType();
        }
        public String getFormattedDuration() {
            long h = dureeSecondes / 3600;
            long m = (dureeSecondes % 3600) / 60;
            return String.format("%02dh %02dmin", h, m);
        }
        public double getHeuresDecimales() {
            return dureeSecondes / 3600.0;
        }
    }

    public record GroupeMasseHoraire(Groupe groupe, long dureeSecondes) {
        public String getNom() {
            return groupe.getNom();
        }
        public String getFiliereAbreviation() {
            return (groupe.getFiliere() != null && groupe.getFiliere().getAbreviation() != null)
                    ? groupe.getFiliere().getAbreviation()
                    : "";
        }
        public int getAnnee() {
            return groupe.getAnnee();
        }
        public String getFormattedDuration() {
            long h = dureeSecondes / 3600;
            long m = (dureeSecondes % 3600) / 60;
            return String.format("%02dh %02dmin", h, m);
        }
        public double getHeuresDecimales() {
            return dureeSecondes / 3600.0;
        }
    }

    public record JourStat(
            int jourIndex,
            String jourNom,
            long dureeSecondes,
            int nbGroupes,
            int nbSeances,
            int nbFormateurs,
            boolean isTotal
    ) {
        public String getFormattedDuration() {
            long h = dureeSecondes / 3600;
            long m = (dureeSecondes % 3600) / 60;
            return String.format("%02dh %02dmin", h, m);
        }
        public double getHeuresDecimales() {
            return dureeSecondes / 3600.0;
        }
    }

    private int nombreFormateurs;
    private int nombreFormateursPermanents;
    private int nombreFormateursVacataires;
    private int nombreGroupes;
    private int nombreFilieres;
    private int nombreSeances;
    private int nombreEspaces;

    private double totalHeuresPlanifiees;
    private double heuresPermanents;
    private double heuresVacataires;
    private double moyenneHeuresParGroupe;
    private double moyenneHeuresParFormateur;
    private double tauxOccupationGlobal;

    private final Map<Espace, Long> barChartEspace = new HashMap<>();
    private final Map<String, Double> heuresParFiliere = new LinkedHashMap<>();
    private final Map<String, Double> heuresParJour = new LinkedHashMap<>();

    private final List<FormateurMasseHoraire> masseHoraireFormateurs = new ArrayList<>();
    private final List<GroupeMasseHoraire> masseHoraireGroupes = new ArrayList<>();
    private final List<JourStat> statsParJour = new ArrayList<>();

    public Map<Espace, Long> getBarChartEspace() {
        return barChartEspace;
    }

    public Map<String, Double> getHeuresParFiliere() {
        return heuresParFiliere;
    }

    public Map<String, Double> getHeuresParJour() {
        return heuresParJour;
    }

    public List<JourStat> getStatsParJour() {
        return statsParJour;
    }

    public List<FormateurMasseHoraire> getMasseHoraireFormateurs() {
        return masseHoraireFormateurs;
    }

    public List<GroupeMasseHoraire> getMasseHoraireGroupes() {
        return masseHoraireGroupes;
    }

    public int getNombreFormateurs() {
        return nombreFormateurs;
    }

    public void setNombreFormateurs(int nombreFormateurs) {
        this.nombreFormateurs = nombreFormateurs;
    }

    public int getNombreFormateursPermanents() {
        return nombreFormateursPermanents;
    }

    public void setNombreFormateursPermanents(int nombreFormateursPermanents) {
        this.nombreFormateursPermanents = nombreFormateursPermanents;
    }

    public int getNombreFormateursVacataires() {
        return nombreFormateursVacataires;
    }

    public void setNombreFormateursVacataires(int nombreFormateursVacataires) {
        this.nombreFormateursVacataires = nombreFormateursVacataires;
    }

    public int getNombreGroupes() {
        return nombreGroupes;
    }

    public void setNombreGroupes(int nombreGroupes) {
        this.nombreGroupes = nombreGroupes;
    }

    public int getNombreFilieres() {
        return nombreFilieres;
    }

    public void setNombreFilieres(int nombreFilieres) {
        this.nombreFilieres = nombreFilieres;
    }

    public int getNombreSeances() {
        return nombreSeances;
    }

    public void setNombreSeances(int nombreSeances) {
        this.nombreSeances = nombreSeances;
    }

    public int getNombreEspaces() {
        return nombreEspaces;
    }

    public void setNombreEspaces(int nombreEspaces) {
        this.nombreEspaces = nombreEspaces;
    }

    public double getTotalHeuresPlanifiees() {
        return totalHeuresPlanifiees;
    }

    public void setTotalHeuresPlanifiees(double totalHeuresPlanifiees) {
        this.totalHeuresPlanifiees = totalHeuresPlanifiees;
    }

    public double getHeuresPermanents() {
        return heuresPermanents;
    }

    public void setHeuresPermanents(double heuresPermanents) {
        this.heuresPermanents = heuresPermanents;
    }

    public double getHeuresVacataires() {
        return heuresVacataires;
    }

    public void setHeuresVacataires(double heuresVacataires) {
        this.heuresVacataires = heuresVacataires;
    }

    public double getMoyenneHeuresParGroupe() {
        return moyenneHeuresParGroupe;
    }

    public void setMoyenneHeuresParGroupe(double moyenneHeuresParGroupe) {
        this.moyenneHeuresParGroupe = moyenneHeuresParGroupe;
    }

    public double getMoyenneHeuresParFormateur() {
        return moyenneHeuresParFormateur;
    }

    public void setMoyenneHeuresParFormateur(double moyenneHeuresParFormateur) {
        this.moyenneHeuresParFormateur = moyenneHeuresParFormateur;
    }

    public double getTauxOccupationGlobal() {
        return tauxOccupationGlobal;
    }

    public void setTauxOccupationGlobal(double tauxOccupationGlobal) {
        this.tauxOccupationGlobal = tauxOccupationGlobal;
    }
}
