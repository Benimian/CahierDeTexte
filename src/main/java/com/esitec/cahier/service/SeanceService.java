package com.esitec.cahier.service;

import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Seance;
import java.util.List;

public class SeanceService {
    private final SeanceDAO seanceDAO = new SeanceDAO();

    public boolean ajouterSeance(int coursId, String date, String heure, int duree, String contenu, String observations) {
        if (contenu == null || contenu.isEmpty()) return false;
        Seance s = new Seance(coursId, date, heure, duree, contenu, observations);
        return seanceDAO.ajouter(s);
    }

    public boolean modifierSeance(Seance s) {
        return seanceDAO.modifier(s);
    }

    public boolean validerSeance(int id) {
        return seanceDAO.valider(id);
    }

    public boolean rejeterSeance(int id, String commentaire) {
        return seanceDAO.rejeter(id, commentaire);
    }

    public List<Seance> getSeancesDuCours(int coursId) {
        return seanceDAO.listerParCours(coursId);
    }

    public List<Seance> getSeancesDeLaClasse(String classe) {
        return seanceDAO.listerParClasse(classe);
    }
}
