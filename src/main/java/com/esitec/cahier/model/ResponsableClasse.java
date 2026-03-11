package com.esitec.cahier.model;

public class ResponsableClasse extends Utilisateur {
    private String classe;

    public ResponsableClasse(String nom, String prenom, String email, String motDePasse, String classe) {
        super(nom, prenom, email, motDePasse, "RESPONSABLE");
        this.classe = classe;
    }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
}