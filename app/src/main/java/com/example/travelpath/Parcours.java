package com.example.travelpath;

import java.util.ArrayList;
import java.util.List;

public class Parcours {

    public String titre, duree, adresse;
    public int image, prix;
    public boolean isExpanded = false;
    public boolean liked = false;

    public List<Etape> etapes = new ArrayList<>();

    public Parcours(String titre, int image, int prix, String duree, String adresse) {
        this.titre = titre;
        this.image = image;
        this.prix = prix;
        this.duree = duree;
        this.adresse = adresse;
    }
}