package com.esitec.cahier.dao;

import com.esitec.cahier.model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public Utilisateur findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Utilisateur> listerTous() {
        return listerAvecSql("SELECT * FROM utilisateur ORDER BY nom");
    }

    public List<Utilisateur> listerEnseignants() {
        return listerParRole("ENSEIGNANT");
    }

    public List<Utilisateur> listerEnseignantsDisponibles() {
        return listerAvecSql("SELECT * FROM utilisateur WHERE role='ENSEIGNANT' AND valide=true ORDER BY nom");
    }

    public List<Utilisateur> listerParRole(String role) {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = ? ORDER BY nom";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Utilisateur> listerNonValides() {
        return listerAvecSql("SELECT * FROM utilisateur WHERE valide=false AND role!='CHEF' ORDER BY nom");
    }

    public boolean inscrire(Utilisateur u) { return ajouter(u); }

    public boolean ajouter(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom,prenom,email,mot_de_passe,role,classe,valide) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getRole());
            ps.setString(6, u.getClasse());
            ps.setBoolean(7, false);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean validerCompte(int id) { return valider(id); }

    public boolean valider(int id) {
        String sql = "UPDATE utilisateur SET valide=true WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean rejeter(int id) {
        String sql = "DELETE FROM utilisateur WHERE id=? AND role!='CHEF'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateMotDePasse(int id, String hashed) {
        String sql = "UPDATE utilisateur SET mot_de_passe=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hashed); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateProfil(int id, String nom, String prenom, String email) {
        String sql = "UPDATE utilisateur SET nom=?,prenom=?,email=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom); ps.setString(2, prenom);
            ps.setString(3, email); ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean enseignantDejaAssigneClasse(int enseignantId, String classe) {
        String sql = "SELECT COUNT(*) FROM cours WHERE enseignant_id=? AND classe=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enseignantId); ps.setString(2, classe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private List<Utilisateur> listerAvecSql(String sql) {
        List<Utilisateur> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Utilisateur map(ResultSet rs) throws SQLException {
        // Lecture défensive de la colonne classe
        String classe = "";
        try { classe = rs.getString("classe"); } catch (SQLException ignored) {}

        return new Utilisateur(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("email"),
            rs.getString("mot_de_passe"),
            rs.getString("role"),
            classe,
            rs.getBoolean("valide"));
    }
}
