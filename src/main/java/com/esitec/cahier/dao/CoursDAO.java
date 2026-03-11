package com.esitec.cahier.dao;

import com.esitec.cahier.model.Cours;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    public boolean ajouter(Cours c) {
        String sql = "INSERT INTO cours (intitule, volume_horaire, enseignant_id, classe) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getIntitule());
            ps.setInt(2, c.getVolumeHoraire());
            ps.setInt(3, c.getEnseignantId());
            ps.setString(4, c.getClasse());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout cours : " + e.getMessage());
            return false;
        }
    }

    public List<Cours> listerParEnseignant(int enseignantId) {
        List<Cours> liste = new ArrayList<>();
        String sql = """
            SELECT c.*, u.nom || ' ' || u.prenom AS nom_enseignant
            FROM cours c
            LEFT JOIN utilisateur u ON c.enseignant_id = u.id
            WHERE c.enseignant_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("Erreur liste cours : " + e.getMessage());
        }
        return liste;
    }

    public List<Cours> listerParClasse(String classe) {
        List<Cours> liste = new ArrayList<>();
        String sql = """
            SELECT c.*, u.nom || ' ' || u.prenom AS nom_enseignant
            FROM cours c
            LEFT JOIN utilisateur u ON c.enseignant_id = u.id
            WHERE c.classe = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classe);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        return liste;
    }

    public List<Cours> listerTous() {
        List<Cours> liste = new ArrayList<>();
        String sql = """
            SELECT c.*, u.nom || ' ' || u.prenom AS nom_enseignant
            FROM cours c
            LEFT JOIN utilisateur u ON c.enseignant_id = u.id
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        return liste;
    }

    private Cours mapper(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setId(rs.getInt("id"));
        c.setIntitule(rs.getString("intitule"));
        c.setVolumeHoraire(rs.getInt("volume_horaire"));
        c.setEnseignantId(rs.getInt("enseignant_id"));
        c.setClasse(rs.getString("classe"));
        try { c.setNomEnseignant(rs.getString("nom_enseignant")); } catch (SQLException ignored) {}
        return c;
    }
}
