package com.esitec.cahier.model;

public class Enseignant extends Utilisateur {
    public Enseignant(String nom, String prenom, String email, String motDePasse) {
        super(nom, prenom, email, motDePasse, "ENSEIGNANT");
    }
}