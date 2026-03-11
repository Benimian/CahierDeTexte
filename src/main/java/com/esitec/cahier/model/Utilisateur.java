package com.esitec.cahier.model;

public class Utilisateur {
    private int     id;
    private String  nom, prenom, email, motDePasse, role, classe;
    private boolean valide;

    public Utilisateur() {}

    // Ancien constructeur 5 args — gardé pour compatibilité
    public Utilisateur(String nom, String prenom, String email, String motDePasse, String role) {
        this(0, nom, prenom, email, motDePasse, role, "", false);
    }

    // Constructeur complet
    public Utilisateur(int id, String nom, String prenom, String email,
                       String motDePasse, String role, String classe, boolean valide) {
        this.id = id; this.nom = nom; this.prenom = prenom;
        this.email = email; this.motDePasse = motDePasse;
        this.role = role; this.classe = classe; this.valide = valide;
    }

    public int     getId()         { return id; }
    public String  getNom()        { return nom; }
    public String  getPrenom()     { return prenom; }
    public String  getEmail()      { return email; }
    public String  getMotDePasse() { return motDePasse; }
    public String  getRole()       { return role; }
    public String  getClasse()     { return classe != null ? classe : ""; }
    public boolean isValide()      { return valide; }

    public void setId(int id)                { this.id = id; }
    public void setNom(String nom)           { this.nom = nom; }
    public void setPrenom(String prenom)     { this.prenom = prenom; }
    public void setEmail(String email)       { this.email = email; }
    public void setMotDePasse(String mdp)    { this.motDePasse = mdp; }
    public void setRole(String role)         { this.role = role; }
    public void setClasse(String classe)     { this.classe = classe; }
    public void setValide(boolean valide)    { this.valide = valide; }

    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() { return getNomComplet() + " (" + email + ")"; }
}
