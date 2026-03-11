package com.esitec.cahier.model;

import java.util.List;

public class FicheSuivi {
    private Cours cours;
    private Utilisateur enseignant;
    private List<Seance> seances;

    public FicheSuivi(Cours cours, Utilisateur enseignant, List<Seance> seances) {
        this.cours = cours;
        this.enseignant = enseignant;
        this.seances = seances;
    }

    public Cours getCours() { return cours; }
    public Utilisateur getEnseignant() { return enseignant; }
    public List<Seance> getSeances() { return seances; }

    public int getTotalHeures() {
        return seances.stream().mapToInt(Seance::getDuree).sum();
    }

    public long getNbSeancesValidees() {
        return seances.stream().filter(s -> "VALIDEE".equals(s.getStatut())).count();
    }
}