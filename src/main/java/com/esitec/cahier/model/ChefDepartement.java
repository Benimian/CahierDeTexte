package com.esitec.cahier.model;

public class ChefDepartement extends Utilisateur {
    public ChefDepartement(String nom, String prenom, String email, String motDePasse) {
        super(nom, prenom, email, motDePasse, "CHEF");
        this.setValide(true);
    }
}