package com.esitec.cahier.service;

import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.utils.PasswordUtils;

public class AuthService {

    private static Utilisateur utilisateurConnecte;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public Utilisateur connecter(String email, String motDePasse) {
        Utilisateur u = utilisateurDAO.findByEmail(email);
        if (u == null) return null;
        if (!u.isValide()) return null;

        // Migration automatique : si le mdp est en clair, on le hashe
        if (PasswordUtils.needsMigration(u.getMotDePasse())) {
            if (!u.getMotDePasse().equals(motDePasse)) return null;
            String hashed = PasswordUtils.hash(motDePasse);
            utilisateurDAO.updateMotDePasse(u.getId(), hashed);
            u.setMotDePasse(hashed);
        } else {
            if (!PasswordUtils.verify(motDePasse, u.getMotDePasse())) return null;
        }

        utilisateurConnecte = u;
        return u;
    }

    public static Utilisateur getUtilisateurConnecte() { return utilisateurConnecte; }
    public static void deconnecter() { utilisateurConnecte = null; }

    public boolean inscrire(String nom, String prenom, String email,
                            String motDePasse, String role, String classe) {
        if (utilisateurDAO.findByEmail(email) != null) return false;
        String hashed = PasswordUtils.hash(motDePasse);
        Utilisateur u = new Utilisateur(0, nom, prenom, email, hashed, role, classe, false);
        return utilisateurDAO.inscrire(u);
    }

    public boolean changerMotDePasse(int userId, String ancien, String nouveau) {
        Utilisateur u = utilisateurDAO.findById(userId);
        if (u == null) return false;
        if (!PasswordUtils.verify(ancien, u.getMotDePasse())) return false;
        String hashed = PasswordUtils.hash(nouveau);
        return utilisateurDAO.updateMotDePasse(userId, hashed);
    }
}
