package com.example.travelpath;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ParcoursActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ParcoursAdapter adapter;
    List<Parcours> parcoursList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parcours_activity);

        // On récupère les informations de l'activite precedente mais on l'utilise pas.
        ArrayList<String> tags = getIntent().getStringArrayListExtra("tags");
        ArrayList<String> categories = getIntent().getStringArrayListExtra("categories");
        String budget = getIntent().getStringExtra("budget");
        String duree = getIntent().getStringExtra("duree");
        String effort = getIntent().getStringExtra("effort");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        parcoursList = new ArrayList<>();

        loadData();

        adapter = new ParcoursAdapter(parcoursList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        // Pour l'instant on crée à la main un parcours
        Parcours p = new Parcours("Parcours Équilibré", R.drawable.comedie, 45, "4h", "Montpellier");

        p.etapes.add(new Etape("Opera Orchestre National", "11 Bd Victor Hugo, 34000 Montpellier", "~2 min", "2h", 15, R.drawable.opera));
        p.etapes.add(new Etape("Le Bistrok Montpellier", "47 Rue de l'Aiguillerie, 34000 Montpellier", "~2 min", "2h", 20, R.drawable.resto));

        parcoursList.add(p);
    }
}
