package com.esitec.cahier.dao;

import com.esitec.cahier.model.Seance;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {

    public boolean ajouter(Seance s) {
        String sql = "INSERT INTO seance (cours_id, date, heure, duree, contenu, observations, statut) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getCoursId());
            ps.setString(2, s.getDate());
            ps.setString(3, s.getHeure());
            ps.setInt(4, s.getDuree());
            ps.setString(5, s.getContenu());
            ps.setString(6, s.getObservations());
            ps.setString(7, "EN_ATTENTE");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur ajout séance : " + e.getMessage());
            return false;
        }
    }

    public boolean modifier(Seance s) {
        String sql = "UPDATE seance SET date=?, heure=?, duree=?, contenu=?, observations=? WHERE id=? AND statut='EN_ATTENTE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getDate());
            ps.setString(2, s.getHeure());
            ps.setInt(3, s.getDuree());
            ps.setString(4, s.getContenu());
            ps.setString(5, s.getObservations());
            ps.setInt(6, s.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur modification séance : " + e.getMessage());
            return false;
        }
    }

    public boolean valider(int id) {
        return changerStatut(id, "VALIDEE", null);
    }

    public boolean rejeter(int id, String commentaire) {
        return changerStatut(id, "REJETEE", commentaire);
    }

    private boolean changerStatut(int id, String statut, String commentaire) {
        String sql = "UPDATE seance SET statut=?, commentaire_rejet=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setString(2, commentaire);
            ps.setInt(3, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur changement statut : " + e.getMessage());
            return false;
        }
    }

    public List<Seance> listerParCours(int coursId) {
        List<Seance> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, c.intitule AS intitule_cours
            FROM seance s
            LEFT JOIN cours c ON s.cours_id = c.id
            WHERE s.cours_id = ?
            ORDER BY s.date DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        return liste;
    }

    public List<Seance> listerParClasse(String classe) {
        List<Seance> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, c.intitule AS intitule_cours
            FROM seance s
            JOIN cours c ON s.cours_id = c.id
            WHERE c.classe = ?
            ORDER BY s.date DESC
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

    private Seance mapper(ResultSet rs) throws SQLException {
        Seance s = new Seance();
        s.setId(rs.getInt("id"));
        s.setCoursId(rs.getInt("cours_id"));
        s.setDate(rs.getString("date"));
        s.setHeure(rs.getString("heure"));
        s.setDuree(rs.getInt("duree"));
        s.setContenu(rs.getString("contenu"));
        s.setObservations(rs.getString("observations"));
        s.setStatut(rs.getString("statut"));
        s.setCommentaireRejet(rs.getString("commentaire_rejet"));
        try { s.setIntituleCours(rs.getString("intitule_cours")); } catch (SQLException ignored) {}
        return s;
    }
}
