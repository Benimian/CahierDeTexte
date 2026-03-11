package com.esitec.cahier.model;

public class Cours {
    private int id;
    private String intitule;
    private int volumeHoraire;
    private int enseignantId;
    private String classe;
    private String nomEnseignant; // pour affichage

    public Cours() {}

    public Cours(String intitule, int volumeHoraire, int enseignantId, String classe) {
        this.intitule = intitule;
        this.volumeHoraire = volumeHoraire;
        this.enseignantId = enseignantId;
        this.classe = classe;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public int getVolumeHoraire() { return volumeHoraire; }
    public void setVolumeHoraire(int volumeHoraire) { this.volumeHoraire = volumeHoraire; }

    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public String getNomEnseignant() { return nomEnseignant; }
    public void setNomEnseignant(String nomEnseignant) { this.nomEnseignant = nomEnseignant; }

    @Override
    public String toString() { return intitule + " - " + classe; }
}
