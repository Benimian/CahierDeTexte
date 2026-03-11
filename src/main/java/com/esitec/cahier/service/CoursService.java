package com.esitec.cahier.service;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Utilisateur;
import java.util.List;

public class CoursService {
    private final CoursDAO coursDAO = new CoursDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public boolean assignerCours(String intitule, int volumeHoraire, int enseignantId, String classe) {
        Utilisateur enseignant = utilisateurDAO.findById(enseignantId);
        if (enseignant == null || !"ENSEIGNANT".equals(enseignant.getRole())) return false;
        Cours c = new Cours(intitule, volumeHoraire, enseignantId, classe);
        return coursDAO.ajouter(c);
    }

    public List<Cours> getCoursDEnseignant(int enseignantId) {
        return coursDAO.listerParEnseignant(enseignantId);
    }

    public List<Cours> getCoursDeLaClasse(String classe) {
        return coursDAO.listerParClasse(classe);
    }

    public List<Cours> getTousLesCours() {
        return coursDAO.listerTous();
    }
}
