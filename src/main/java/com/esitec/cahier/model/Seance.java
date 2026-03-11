package com.esitec.cahier.model;

public class Seance {
    private int id;
    private int coursId;
    private String date;
    private String heure;
    private int duree;
    private String contenu;
    private String observations;
    private String statut; // EN_ATTENTE, VALIDEE, REJETEE
    private String commentaireRejet;
    private String intituleCours; // pour affichage

    public Seance() {}

    public Seance(int coursId, String date, String heure, int duree, String contenu, String observations) {
        this.coursId = coursId;
        this.date = date;
        this.heure = heure;
        this.duree = duree;
        this.contenu = contenu;
        this.observations = observations;
        this.statut = "EN_ATTENTE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaireRejet() { return commentaireRejet; }
    public void setCommentaireRejet(String commentaireRejet) { this.commentaireRejet = commentaireRejet; }

    public String getIntituleCours() { return intituleCours; }
    public void setIntituleCours(String intituleCours) { this.intituleCours = intituleCours; }

    @Override
    public String toString() {
        return date + " " + heure + " - " + (intituleCours != null ? intituleCours : "Cours #" + coursId);
    }
}
