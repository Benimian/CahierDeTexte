package com.esitec.cahier.dao;

import java.sql.*;

public class DatabaseConnection {

    // ── Configuration MySQL XAMPP ──────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/cahier_texte?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // XAMPP = pas de mot de passe par défaut

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connecté à MySQL !");
                initialiserBase(connection);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable : " + e.getMessage());
            }
        }
        return connection;
    }

    private static void initialiserBase(Connection conn) {
        String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS utilisateur (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nom VARCHAR(100) NOT NULL,
                prenom VARCHAR(100) NOT NULL,
                email VARCHAR(150) UNIQUE NOT NULL,
                mot_de_passe VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL,
                valide TINYINT DEFAULT 0
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS cours (
                id INT PRIMARY KEY AUTO_INCREMENT,
                intitule VARCHAR(200) NOT NULL,
                volume_horaire INT,
                enseignant_id INT,
                classe VARCHAR(100),
                FOREIGN KEY (enseignant_id) REFERENCES utilisateur(id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS seance (
                id INT PRIMARY KEY AUTO_INCREMENT,
                cours_id INT NOT NULL,
                date VARCHAR(20) NOT NULL,
                heure VARCHAR(10) NOT NULL,
                duree INT NOT NULL,
                contenu TEXT,
                observations TEXT,
                statut VARCHAR(20) DEFAULT 'EN_ATTENTE',
                commentaire_rejet TEXT,
                FOREIGN KEY (cours_id) REFERENCES cours(id)
            )
            """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }

            // Compte admin par défaut
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utilisateur WHERE email = 'chef@esitec.sn'");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, valide)
                    VALUES ('Admin', 'Chef', 'chef@esitec.sn', 'admin123', 'CHEF', 1)
                """);
                System.out.println("✅ Compte admin créé !");
            }

        } catch (SQLException e) {
            System.err.println("Erreur initialisation BDD : " + e.getMessage());
        }
    }

    public static void fermer() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Connexion MySQL fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture : " + e.getMessage());
        }
    }
}
