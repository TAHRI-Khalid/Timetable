package tk.tahri.timetable.Entities;

import java.util.HashMap;
import java.util.Map;

public class Dashboard {
    private int nombreFormateurs;
    private int nombreGroupes;
    private int nombreSeances;
    private int nombreEspaces;
    private final Map<Espace, Long> barChartEspace = new HashMap<>();

    public Map<Espace, Long> getBarChartEspace() {
        return barChartEspace;
    }

    public int getNombreFormateurs() {
        return nombreFormateurs;
    }

    public void setNombreFormateurs(int nombreFormateurs) {
        this.nombreFormateurs = nombreFormateurs;
    }

    public int getNombreGroupes() {
        return nombreGroupes;
    }

    public void setNombreGroupes(int nombreGroupes) {
        this.nombreGroupes = nombreGroupes;
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
}
