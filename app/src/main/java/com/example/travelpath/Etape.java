package com.example.travelpath;

public class Etape {

    public String nom, adresse, trajet, duree;
    public int prix, image;

    public Etape(String nom, String adresse, String trajet, String duree, int prix, int image) {
        this.nom = nom;
        this.adresse = adresse;
        this.trajet = trajet;
        this.duree = duree;
        this.prix = prix;
        this.image = image;
    }
}