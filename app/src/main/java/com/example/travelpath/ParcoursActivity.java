package com.example.travelpath;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ParcoursActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ParcoursAdapter adapter;
    List<Parcours> parcoursList = new ArrayList<>();

    ArrayList<String> tags;
    ArrayList<String> categories;
    String budget;
    String duree;
    String effort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parcours_activity);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ParcoursAdapter(parcoursList, this);
        recyclerView.setAdapter(adapter);

        // données utilisateur
        tags = getIntent().getStringArrayListExtra("tags");
        categories = getIntent().getStringArrayListExtra("categories");
        budget = getIntent().getStringExtra("budget");
        duree = getIntent().getStringExtra("duree");
        effort = getIntent().getStringExtra("effort");

        if (tags == null || tags.isEmpty()) {
            Toast.makeText(this,
                    "Aucune ville sélectionnée",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 1er tag = lieu principal
        chercherCoordonnees(tags.get(0));
    }

    // ====================================================
    // ETAPE 1 : NOM -> LATITUDE LONGITUDE
    // ====================================================
    private void chercherCoordonnees(String ville) {

        new Thread(() -> {

            try {

                String urlString =
                        "https://nominatim.openstreetmap.org/search?q="
                                + URLEncoder.encode(ville, "UTF-8")
                                + "&format=json&limit=1";

                URL url = new URL(urlString);

                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();

                conn.setRequestProperty(
                        "User-Agent",
                        "TravelPath"
                );

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        conn.getInputStream()
                                )
                        );

                StringBuilder result =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONArray array =
                        new JSONArray(result.toString());

                if (array.length() > 0) {

                    JSONObject obj =
                            array.getJSONObject(0);

                    double lat =
                            obj.getDouble("lat");

                    double lon =
                            obj.getDouble("lon");

                    chercherLieux(ville, lat, lon);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    // ====================================================
    // ETAPE 2 : LIEUX AUTOUR DU POINT
    // ====================================================
    private void chercherLieux(
            String ville,
            double lat,
            double lon) {

        new Thread(() -> {

            try {

                String query = buildOverpassQuery(lat, lon);

                URL url = new URL(
                        "https://overpass-api.de/api/interpreter"
                );

                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os =
                        conn.getOutputStream();

                os.write(query.getBytes());
                os.flush();
                os.close();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        conn.getInputStream()
                                )
                        );

                StringBuilder result =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json =
                        new JSONObject(result.toString());

                JSONArray elements =
                        json.getJSONArray("elements");

                generateParcours(ville, elements);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    // ====================================================
    // REQUETE OVERPASS
    // ====================================================
    private String buildOverpassQuery(
            double lat,
            double lon) {

        StringBuilder q =
                new StringBuilder("[out:json];(");

        if (categories.contains("Culture")) {

            q.append("node(around:4000,")
                    .append(lat).append(",")
                    .append(lon)
                    .append(")[tourism=museum];");

            q.append("node(around:4000,")
                    .append(lat).append(",")
                    .append(lon)
                    .append(")[historic=monument];");
        }

        if (categories.contains("Restauration")) {

            q.append("node(around:4000,")
                    .append(lat).append(",")
                    .append(lon)
                    .append(")[amenity=restaurant];");
        }

        if (categories.contains("Loisir")) {

            q.append("node(around:4000,")
                    .append(lat).append(",")
                    .append(lon)
                    .append(")[leisure=park];");
        }

        if (categories.contains("Découverte")) {

            q.append("node(around:4000,")
                    .append(lat).append(",")
                    .append(lon)
                    .append(")[tourism=attraction];");
        }

        q.append(");out;");

        return q.toString();
    }

    // ====================================================
    // GÉNÉRATION DES PARCOURS
    // ====================================================

    private void generateParcours(String ville, JSONArray lieux) {

        try {

            int budgetMax = Integer.parseInt(budget);

            Parcours eco = new Parcours(
                    "Parcours Économique",
                    R.drawable.comedie,
                    0,
                    duree,
                    ville
            );

            Parcours equilibre = new Parcours(
                    "Parcours Équilibré",
                    R.drawable.comedie,
                    0,
                    duree,
                    ville
            );

            Parcours confort = new Parcours(
                    "Parcours Confort",
                    R.drawable.comedie,
                    0,
                    duree,
                    ville
            );

            for (int i = 0; i < lieux.length(); i++) {

                JSONObject obj = lieux.getJSONObject(i);
                JSONObject tags = obj.optJSONObject("tags");

                if (tags == null) continue;

                String nom = tags.optString(
                        "name",
                        "Lieu touristique"
                );

                String adresse = ville;

                int prixLieu = getPrix(tags);

                // ======================================
                // ECO
                // lieux gratuits ou peu chers
                // ======================================
                if (eco.etapes.size() < 3
                        && eco.prix + prixLieu <= budgetMax / 2
                        && prixLieu <= 10) {

                    eco.etapes.add(
                            new Etape(
                                    nom,
                                    adresse,
                                    "~10 min",
                                    "45 min",
                                    prixLieu,
                                    R.drawable.comedie,
                                    getImageUrl(nom)
                            )
                    );

                    eco.prix += prixLieu;
                }

                // ======================================
                // EQUILIBRE
                // ======================================
                if (equilibre.etapes.size() < 4
                        && equilibre.prix + prixLieu <= budgetMax) {

                    equilibre.etapes.add(
                            new Etape(
                                    nom,
                                    adresse,
                                    "~12 min",
                                    "1h",
                                    prixLieu,
                                    R.drawable.opera,
                                    getImageUrl(nom)
                            )
                    );

                    equilibre.prix += prixLieu;
                }

                // ======================================
                // CONFORT
                // budget premium
                // ======================================
                if (confort.etapes.size() < 5
                        && confort.prix + prixLieu <= budgetMax * 1.5) {

                    confort.etapes.add(
                            new Etape(
                                    nom,
                                    adresse,
                                    "~5 min taxi",
                                    "1h15",
                                    prixLieu,
                                    R.drawable.resto,
                                    getImageUrl(nom)
                            )
                    );

                    confort.prix += prixLieu;
                }
            }

            parcoursList.clear();

            if (!eco.etapes.isEmpty())
                parcoursList.add(eco);

            if (!equilibre.etapes.isEmpty())
                parcoursList.add(equilibre);

            if (!confort.etapes.isEmpty())
                parcoursList.add(confort);

            runOnUiThread(() ->
                    adapter.notifyDataSetChanged());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


// ======================================================
// PRIX INTELLIGENT SELON TYPE DE LIEU
// ======================================================

    private int getPrix(JSONObject tags) {

        try {

            // -------------------------
            // RESTAURANTS
            // -------------------------
            if (tags.has("amenity")) {

                String type =
                        tags.getString("amenity");

                if (type.equals("restaurant"))
                    return 25;

                if (type.equals("cafe"))
                    return 8;

                if (type.equals("fast_food"))
                    return 12;

                if (type.equals("bar"))
                    return 15;
            }

            // -------------------------
            // TOURISME
            // -------------------------
            if (tags.has("tourism")) {

                String type =
                        tags.getString("tourism");

                if (type.equals("museum"))
                    return 14;

                if (type.equals("attraction"))
                    return 10;

                if (type.equals("gallery"))
                    return 9;

                if (type.equals("zoo"))
                    return 22;
            }

            // -------------------------
            // LOISIRS
            // -------------------------
            if (tags.has("leisure")) {

                String type =
                        tags.getString("leisure");

                if (type.equals("park"))
                    return 0;

                if (type.equals("sports_centre"))
                    return 12;

                if (type.equals("bowling"))
                    return 18;
            }

            // -------------------------
            // HISTORIQUE
            // -------------------------
            if (tags.has("historic")) {

                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 5;
    }

    private String getImageUrl(String lieu) {

        lieu = lieu.replace(" ", "_");

        return "https://commons.wikimedia.org/wiki/Special:FilePath/"
                + lieu
                + ".jpg";
    }
}