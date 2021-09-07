package tk.tahri.timetable.dao;


import tk.tahri.timetable.Entities.*;
import tk.tahri.timetable.tools.Config;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Database instance;
    private static String url;

    public static Database getInstance() {
        url = "jdbc:sqlite:" + Config.getProperty("temp");
        if (instance == null) {
            instance = new Database();
            return instance;
        }
        return instance;
    }

    public void execute(String sql) {
        try (var connection = DriverManager.getConnection(url);
             var statement = connection.createStatement()) {
            statement.execute(sql);
            Config.setProperty("modified", Boolean.TRUE.toString());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Espace> getEspaces() {
        String sql = "SELECT * FROM espace";
        var list = new ArrayList<Espace>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var espace = new Espace();
                espace.setId(rs.getLong("id"));
                espace.setNom(rs.getString("nom"));
                espace.setType(rs.getString("type"));
                list.add(espace);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public void createDatabase() {
        String sql_create_espace = """
                CREATE TABLE IF NOT EXISTS espace (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                type text NOT NULL);
                """;
        String sql_create_groupe = """
                CREATE TABLE IF NOT EXISTS groupe (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                annee text NOT NULL,
                filiere text NOT NULL,
                niveau text NOT NULL,
                secteur text NOT NULL,
                type text NOT NULL);
                """;
        String sql_create_formateur = """
                CREATE TABLE IF NOT EXISTS formateur (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                prenom text NOT NULL,
                matricule text NOT NULL,
                type text NOT NULL);
                """;
        String sql_create_seance = """
                CREATE TABLE IF NOT EXISTS seance (
                id integer PRIMARY KEY AUTOINCREMENT,
                debut integer,
                fin integer,
                jour integer,
                idGroupe integer,
                idFormateur integer,
                idEspace integer,
                FOREIGN KEY(idGroupe) REFERENCES groupe(id),
                FOREIGN KEY (idFormateur) REFERENCES formateur(id),
                FOREIGN KEY (idEspace) REFERENCES espace(id)
                );
                """;
        try (var connection = DriverManager.getConnection(url);
             var statement = connection.createStatement()) {

            statement.execute(sql_create_espace);
            statement.execute(sql_create_groupe);
            statement.execute(sql_create_formateur);
            statement.execute(sql_create_seance);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<Formateur> getFormateurs() {
        String sql = "SELECT * FROM formateur";
        var list = new ArrayList<Formateur>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var formateur = new Formateur();
                formateur.setId(rs.getLong("id"));
                formateur.setNom(rs.getString("nom"));
                formateur.setPrenom(rs.getString("prenom"));
                formateur.setMatricule(rs.getString("matricule"));
                formateur.setType(rs.getString("type"));
                list.add(formateur);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public List<Groupe> getGroupes() {
        String sql = "SELECT * FROM groupe";
        var list = new ArrayList<Groupe>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var groupe = new Groupe();
                groupe.setId(rs.getLong("id"));
                groupe.setNom(rs.getString("nom"));
                groupe.setAnnee(rs.getInt("annee"));
                groupe.setFiliere(rs.getString("filiere"));
                groupe.setNiveau(rs.getString("niveau"));
                groupe.setSecteur(rs.getString("secteur"));
                groupe.setType(rs.getString("type"));
                list.add(groupe);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public List<Seance> getSeances(Formateur formateur) {
        String sql = """
                SELECT
                s.id, s.debut, s.fin ,s.jour ,s.idFormateur,s.idEspace,s.idGroupe,
                f.nom as fnom, f.prenom, f.matricule, f.type,
                e.nom as enom, e.type,
                g.nom as gnom, g.annee, g.filiere, g.niveau, g.secteur,g.type
                FROM seance s
                INNER JOIN formateur f on  f.id=s.idFormateur
                INNER JOIN espace e on e.id = s.idEspace
                INNER JOIN groupe g on g.id = s.idGroupe
                WHERE idFormateur='%s';
                """.formatted(formateur.getId());
        return getSeances(sql);
    }

    public List<Seance> getSeances(Groupe groupe) {
        String sql = """
                SELECT
                s.id, s.debut, s.fin ,s.jour ,s.idFormateur,s.idEspace,s.idGroupe,
                f.nom as fnom, f.prenom, f.matricule, f.type,
                e.nom as enom, e.type,
                g.nom as gnom, g.annee, g.filiere, g.niveau, g.secteur,g.type
                FROM seance s
                INNER JOIN formateur f on  f.id=s.idFormateur
                INNER JOIN espace e on e.id = s.idEspace
                INNER JOIN groupe g on g.id = s.idGroupe
                WHERE idGroupe='%s';
                """.formatted(groupe.getId());
        return getSeances(sql);
    }

    public List<Seance> getSeances(Espace espace) {
        String sql = """
                SELECT
                s.id, s.debut, s.fin ,s.jour ,s.idFormateur,s.idEspace,s.idGroupe,
                f.nom as fnom, f.prenom, f.matricule, f.type,
                e.nom as enom, e.type,
                g.nom as gnom, g.annee, g.filiere, g.niveau, g.secteur,g.type
                FROM seance s
                INNER JOIN formateur f on  f.id=s.idFormateur
                INNER JOIN espace e on e.id = s.idEspace
                INNER JOIN groupe g on g.id = s.idGroupe
                WHERE idEspace='%s';
                """.formatted(espace.getId());
        return getSeances(sql);
    }

    private List<Seance> getSeances(String sql) {
        var list = new ArrayList<Seance>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setDebut(LocalTime.ofSecondOfDay(rs.getLong("debut")));
                seance.setFin(LocalTime.ofSecondOfDay(rs.getLong("fin")));
                seance.setJour(DayOfWeek.of(rs.getInt("jour")));

                var e = new Espace();
                e.setId(rs.getLong("idEspace"));
                e.setNom(rs.getString("enom"));
                e.setType(rs.getString("type"));
                seance.setEspace(e);

                var f = new Formateur();
                f.setId(rs.getLong("idFormateur"));
                f.setNom(rs.getString("fnom"));
                f.setPrenom(rs.getString("prenom"));
                f.setMatricule(rs.getString("matricule"));
                f.setType(rs.getString("type"));
                seance.setFormateur(f);

                var g = new Groupe();
                g.setId(rs.getLong("idGroupe"));
                g.setNom(rs.getString("gnom"));
                g.setAnnee(rs.getInt("annee"));
                g.setFiliere(rs.getString("filiere"));
                g.setNiveau(rs.getString("niveau"));
                g.setSecteur(rs.getString("secteur"));
                g.setType(rs.getString("type"));
                seance.setGroupe(g);

                list.add(seance);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public Dashboard getDashboard() {
        var dashboard = new Dashboard();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM formateur");
            if (rs.next()) dashboard.setNombreFormateurs(rs.getInt(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM groupe");
            if (rs.next()) dashboard.setNombreGroupes(rs.getInt(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM espace");
            if (rs.next()) dashboard.setNombreEspaces(rs.getInt(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM seance");
            if (rs.next()) dashboard.setNombreSeances(rs.getInt(1));

            rs = stmt.executeQuery("""
                    SELECT  SUM(s.fin-s.debut) as duree, e.id ,e.nom,e.type
                    FROM espace e
                    left join seance s on e.id = s.idEspace
                    group by e.id;
                    """);
            while (rs.next()) {
                var e = new Espace();
                e.setId(rs.getLong("id"));
                e.setNom(rs.getString("nom"));
                e.setType(rs.getString("type"));
                dashboard.getBarChartEspace().put(e, rs.getLong("duree"));
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dashboard;
    }
}
