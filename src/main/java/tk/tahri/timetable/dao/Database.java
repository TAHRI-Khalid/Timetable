package tk.tahri.timetable.dao;


import tk.tahri.timetable.Entities.*;
import tk.tahri.timetable.tools.Config;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
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

    public int execute(String sql, String... args) {
        try (var connection = DriverManager.getConnection(url);
             var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                statement.setString(i + 1, args[i]);
            }
            statement.executeUpdate();
            Config.setProperty("modified", Boolean.TRUE.toString());
            var rs = statement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }


    public void createDatabase() {
        String sql_create_etablissment = """
                CREATE TABLE IF NOT EXISTS etablissement (
                id integer PRIMARY KEY AUTOINCREMENT,
                direction text NOT NULL,
                complexe text NOT NULL,
                nom text NOT NULL,
                abreviation text NOT NULL,
                annee text NOT NULL,
                code text,
                username text,
                password text,
                dateDebut text,
                dateFin text);
                """;

        String sql_create_espace = """
                CREATE TABLE IF NOT EXISTS espace (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                type text NOT NULL);
                """;

        String sql_create_filiere = """
                CREATE TABLE IF NOT EXISTS filiere (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                abreviation text NOT NULL,
                niveau text NOT NULL,
                secteur text NOT NULL,
                type text NOT NULL);
                """;

        String sql_create_groupe = """
                CREATE TABLE IF NOT EXISTS groupe (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                annee text NOT NULL,
                idFiliere text NOT NULL,
                FOREIGN KEY (idFiliere) REFERENCES filiere(id));
                """;

        String sql_create_formateur = """
                CREATE TABLE IF NOT EXISTS formateur (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                prenom text NOT NULL,
                matricule text,
                type text NOT NULL);
                """;
        String sql_create_seance = """
                CREATE TABLE IF NOT EXISTS seance (
                id integer PRIMARY KEY AUTOINCREMENT,
                debut integer,
                fin integer,
                jour integer,
                type text NOT NULL,
                idFormateur integer,
                idEspace integer,
                FOREIGN KEY (idFormateur) REFERENCES formateur(id),
                FOREIGN KEY (idEspace) REFERENCES espace(id)
                );
                """;
        String sql_create_seance_groupe = """
                CREATE TABLE IF NOT EXISTS seance_groupe (
                idGroupe integer,
                idSeance integer,
                FOREIGN KEY(idGroupe) REFERENCES groupe(id),
                FOREIGN KEY (idSeance) REFERENCES seance(id)
                );
                """;
        String sql_create_creneau = """
                CREATE TABLE IF NOT EXISTS creneau (
                id integer PRIMARY KEY AUTOINCREMENT,
                nom text NOT NULL,
                heureDebut text NOT NULL,
                heureFin text NOT NULL,
                ordre integer DEFAULT 1
                );
                """;
        try (var connection = DriverManager.getConnection(url);
             var statement = connection.createStatement()) {
            statement.execute(sql_create_etablissment);
            statement.execute("""
                    INSERT INTO etablissement 
                    VALUES (null,
                        'DR Oriental',
                        'CF Berkane',
                        'Institut Spécialisé de Technologie Appliquée Berkane',
                        'ISTA Berkane',
                        '2021/2022',
                        'S140',
                        null,
                        null,
                        null,
                        null)""");
            statement.execute(sql_create_espace);
            statement.execute(sql_create_filiere);
            statement.execute(sql_create_groupe);
            statement.execute(sql_create_formateur);
            statement.execute(sql_create_seance);
            statement.execute(sql_create_seance_groupe);
            statement.execute(sql_create_creneau);
            insertDefaultCreneaux(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Etablissement getItablissment() {
        String sql = "SELECT * FROM etablissement";
        var etab = new Etablissement();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                etab.setId(rs.getLong("id"));
                etab.setDirection(rs.getString("direction"));
                etab.setComplexe(rs.getString("complexe"));
                etab.setNom(rs.getString("nom"));
                etab.setAbreviation(rs.getString("abreviation"));
                etab.setCode(rs.getString("code"));
                etab.setUsername(rs.getString("username"));
                etab.setPassword(rs.getString("password"));

                etab.setAnneeDeFormation(rs.getString("annee"));
                var d1 = rs.getString("dateDebut");
                if (d1 != null) {
                    etab.setDateDebut(LocalDate.parse(d1));
                }
                var d2 = rs.getString("dateFin");
                if (d2 != null) {
                    etab.setDateFin(LocalDate.parse(d2));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return etab;
    }

    public List<Espace> getEspaces() {
        String sql = "SELECT * FROM espace ORDER BY nom";
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

    public List<Formateur> getFormateurs() {
        String sql = "SELECT * FROM formateur ORDER BY nom";
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

    public List<Filiere> getFilieres() {
        String sql = "SELECT * FROM filiere ORDER BY abreviation";
        var list = new ArrayList<Filiere>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var filiere = new Filiere();
                filiere.setId(rs.getLong("id"));
                filiere.setNom(rs.getString("nom"));
                filiere.setAbreviation(rs.getString("abreviation"));
                filiere.setNiveau(rs.getString("niveau"));
                filiere.setSecteur(rs.getString("secteur"));
                filiere.setType(rs.getString("type"));
                list.add(filiere);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public List<Groupe> getGroupesSimilaire(Groupe groupe) {
        String sql = """
                SELECT g.id, g.nom , annee
                FROM groupe g
                WHERE g.idFiliere=%d AND g.annee=%d
                """.formatted(groupe.getFiliere().getId(), groupe.getAnnee());
        var list = new ArrayList<Groupe>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var grp = new Groupe();
                grp.setId(rs.getLong("id"));
                grp.setNom(rs.getString("nom"));
                grp.setAnnee(rs.getInt("annee"));
                grp.setFiliere(groupe.getFiliere());
                list.add(grp);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public List<Groupe> getGroupes() {
        String sql = """
                SELECT g.id, g.nom as gnom, annee, idFiliere, f.nom as fnom, abreviation, niveau, secteur, type
                FROM groupe g
                INNER JOIN filiere f on g.idFiliere = f.id
                ORDER BY g.nom
                """;
        var list = new ArrayList<Groupe>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var groupe = new Groupe();
                groupe.setId(rs.getLong("id"));
                groupe.setNom(rs.getString("gnom"));
                groupe.setAnnee(rs.getInt("annee"));

                var filiere = new Filiere();
                filiere.setId(rs.getLong("idFiliere"));
                filiere.setNom(rs.getString("fnom"));
                filiere.setAbreviation(rs.getString("abreviation"));
                filiere.setNiveau(rs.getString("niveau"));
                filiere.setSecteur(rs.getString("secteur"));
                filiere.setType(rs.getString("type"));

                groupe.setFiliere(filiere);
                list.add(groupe);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }


    public boolean isFiliereHasGroupes(long idFiliere) {
        String sql = "SELECT * FROM groupe WHERE idFiliere=%d".formatted(idFiliere);
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean isFormateurHasSeances(long idFormateur) {
        String sql = "SELECT * FROM seance WHERE idFormateur=%d".formatted(idFormateur);
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean isEspaceHasSeances(long idEspace) {
        String sql = "SELECT * FROM seance WHERE idEspace=%d".formatted(idEspace);
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean isGroupeHasSeances(long idGroupe) {
        String sql = "SELECT * FROM seance_groupe WHERE idGroupe=%d".formatted(idGroupe);
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public List<Seance> getSeances(Groupe groupe) {
        var list = new ArrayList<Seance>();
        String sql = """
                SELECT
                    s.id, s.debut, s.fin ,s.jour ,s.type as stype,
                    s.idEspace,e.nom as enom,e.type as etype,
                    s.idFormateur,f.id, f.nom as fnom, f.prenom, f.matricule, f.type as ftype
                FROM seance_groupe sg
                INNER JOIN seance s on s.id = sg.idSeance
                INNER JOIN formateur f on f.id = s.idFormateur
                INNER JOIN espace e on e.id = s.idEspace
                WHERE sg.idGroupe = %d;
                """.formatted(groupe.getId());

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setDebut(LocalTime.ofSecondOfDay(rs.getLong("debut")));
                seance.setFin(LocalTime.ofSecondOfDay(rs.getLong("fin")));
                seance.setJour(DayOfWeek.of(rs.getInt("jour")));
                seance.setType(rs.getString("stype"));

                var e = new Espace();
                e.setId(rs.getLong("idEspace"));
                e.setNom(rs.getString("enom"));
                e.setType(rs.getString("etype"));
                seance.setEspace(e);

                var f = new Formateur();
                f.setId(rs.getLong("idFormateur"));
                f.setNom(rs.getString("fnom"));
                f.setPrenom(rs.getString("prenom"));
                f.setMatricule(rs.getString("matricule"));
                f.setType(rs.getString("ftype"));
                seance.setFormateur(f);

                seance.getGroupes().add(groupe);
                list.add(seance);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public List<Seance> getSeances(Formateur formateur) {
        String sql = """
                SELECT
                s.id, s.debut, s.fin ,s.jour ,s.type as stype,s.idFormateur,s.idEspace,
                f.nom as fnom, f.prenom, f.matricule, f.type as ftype,
                e.nom as enom, e.type
                FROM seance s
                INNER JOIN formateur f on  f.id=s.idFormateur
                INNER JOIN espace e on e.id = s.idEspace
                WHERE idFormateur='%s';
                """.formatted(formateur.getId());
        return getSeances(sql);
    }

    public List<Seance> getSeances(Espace espace) {
        String sql = """
                SELECT
                s.id, s.debut, s.fin ,s.jour ,s.type as stype,s.idFormateur,s.idEspace,
                f.nom as fnom, f.prenom, f.matricule, f.type as ftype,
                e.nom as enom, e.type
                FROM seance s
                INNER JOIN formateur f on  f.id=s.idFormateur
                INNER JOIN espace e on e.id = s.idEspace
                WHERE idEspace='%s';
                """.formatted(espace.getId());
        return getSeances(sql);
    }

    public List<Seance> getSeances() {
        String sql = """
                SELECT
                s.id, s.debut, s.fin ,s.jour ,s.type as stype,s.idFormateur,s.idEspace,
                f.nom as fnom, f.prenom, f.matricule, f.type as ftype,
                e.nom as enom, e.type
                FROM seance s
                INNER JOIN formateur f on  f.id=s.idFormateur
                INNER JOIN espace e on e.id = s.idEspace;
                """;
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
                seance.setType(rs.getString("stype"));

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
                f.setType(rs.getString("ftype"));
                seance.setFormateur(f);
                list.add(seance);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        var req = """
                SELECT g.id, g.nom, g.annee, g.idFiliere,f.abreviation
                FROM seance_groupe sg
                         INNER JOIN groupe g on g.id = sg.idGroupe
                         INNER JOIN filiere f on g.idFiliere = f.id
                WHERE sg.idSeance = %d
                """;
        for (var seance : list) {
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(req.formatted(seance.getId()))) {
                while (rs.next()) {
                    var g = new Groupe();
                    g.setId(rs.getLong("id"));
                    g.setNom(rs.getString("nom"));
                    g.setAnnee(rs.getInt("annee"));
                    var f = new Filiere();
                    f.setId(rs.getLong("idFiliere"));
                    f.setAbreviation(rs.getString("abreviation"));
                    g.setFiliere(f);

                    seance.getGroupes().add(g);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
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

    public List<CreneauHoraire> getCreneaux() {
        var list = new ArrayList<CreneauHoraire>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS creneau (
                    id integer PRIMARY KEY AUTOINCREMENT,
                    nom text NOT NULL,
                    heureDebut text NOT NULL,
                    heureFin text NOT NULL,
                    ordre integer DEFAULT 1
                    );
                    """);

            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM creneau")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertDefaultCreneaux(stmt);
                }
            }

            // Migration automatique si seuls les 4 anciens créneaux temporaires sont présents
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM creneau WHERE nom LIKE 'Séance %'")) {
                if (rs.next() && rs.getInt(1) == 4) {
                    try (ResultSet rsTotal = stmt.executeQuery("SELECT count(*) FROM creneau")) {
                        if (rsTotal.next() && rsTotal.getInt(1) == 4) {
                            stmt.execute("DELETE FROM creneau");
                            insertDefaultCreneaux(stmt);
                        }
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM creneau ORDER BY ordre ASC, heureDebut ASC")) {
                while (rs.next()) {
                    var creneau = new CreneauHoraire();
                    creneau.setId(rs.getLong("id"));
                    creneau.setNom(rs.getString("nom"));
                    String hDeb = rs.getString("heureDebut");
                    String hFin = rs.getString("heureFin");
                    if (hDeb != null && !hDeb.isEmpty()) {
                        creneau.setHeureDebut(LocalTime.parse(hDeb));
                    }
                    if (hFin != null && !hFin.isEmpty()) {
                        creneau.setHeureFin(LocalTime.parse(hFin));
                    }
                    creneau.setOrdre(rs.getInt("ordre"));
                    list.add(creneau);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public void resetCreneauxToDefault() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM creneau");
            insertDefaultCreneaux(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertDefaultCreneaux(Statement stmt) throws SQLException {
        stmt.execute("""
                INSERT INTO creneau (nom, heureDebut, heureFin, ordre) VALUES
                ('8:00 - 9:00', '08:00', '09:00', 1),
                ('9:00 - 10:00', '09:00', '10:00', 2),
                ('10:00 - 11:00', '10:00', '11:00', 3),
                ('11:00 - 12:00', '11:00', '12:00', 4),
                ('12:00 - 13:00', '12:00', '13:00', 5),
                ('Pause', '13:00', '13:30', 6),
                ('13:30 - 14:30', '13:30', '14:30', 7),
                ('14:30 - 15:30', '14:30', '15:30', 8),
                ('15:30 - 16:30', '15:30', '16:30', 9),
                ('16:30 - 17:30', '16:30', '17:30', 10),
                ('17:30 - 18:30', '17:30', '18:30', 11)
                """);
    }

}
