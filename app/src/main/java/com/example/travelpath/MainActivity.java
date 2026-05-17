package com.example.travelpath;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    ChipGroup chipGroup;
    AutoCompleteTextView editRecherche;
    EditText editDuree;
    CheckBox cbFaible, cbMoyen, cbFort;
    CheckBox cbCulture, cbLoisir, cbResto, cbDecouverte;
    EditText editBudget;
    Button btnValider;
    int b;
    RecyclerView recyclerSuggestions;
    SuggestionAdapter suggestionAdapter;

    ArrayAdapter<String> adapter;
    ArrayList<String> suggestions = new ArrayList<>();
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable searchRunnable;

    public boolean isValidDuree(String input) {
        String regex = "(?i)^([0-9]{1,2}h([0-5][0-9])?|[0-9]{1,3}min)$";
        return input.matches(regex);
    }

    public boolean isValidBudget(String input) {
        String regex = "(?i)^([0-9]{1,2}h([0-5][0-9])?|[0-9]{1,3}min)$";
        return input.matches(regex);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chipGroup = findViewById(R.id.chipGroup);
        editRecherche = findViewById(R.id.editRecherche);
        editDuree = findViewById(R.id.editDuree);
        cbFaible = findViewById(R.id.cbFaible);
        cbMoyen = findViewById(R.id.cbMoyen);
        cbFort = findViewById(R.id.cbFort);
        cbCulture = findViewById(R.id.cbCulture);
        cbLoisir = findViewById(R.id.cbLoisir);
        cbResto = findViewById(R.id.cbResto);
        cbDecouverte = findViewById(R.id.cbDecouverte);
        editBudget = findViewById(R.id.editBudget);
        btnValider = findViewById(R.id.valider);
        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        editRecherche.setDropDownAnchor(R.id.editRecherche);
        // On part du principe que l'effort est moyen
        cbMoyen.setChecked(true);

        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));

        suggestionAdapter = new SuggestionAdapter(suggestions,
                text -> {
                    addTag(text);
                    editRecherche.setText("");
                    recyclerSuggestions.setVisibility(View.GONE);
                });

        recyclerSuggestions.setAdapter(suggestionAdapter);
        editRecherche.setThreshold(1);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        editRecherche.setAdapter(adapter);

        // Clic suggestion
        editRecherche.setOnItemClickListener((parent, view, position, id) -> {
            String adresse = parent.getItemAtPosition(position).toString();
            addTag(adresse);
            editRecherche.setText("");
        });

        // Recherche live
        editRecherche.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                handler.removeCallbacksAndMessages(null);
                if (s.length() >= 3) {
                    searchRunnable = () -> rechercherAdresse(s.toString());
                    handler.postDelayed(searchRunnable, 800);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupSingleSelection();

        //Envoie des informations pour création de parcours
        btnValider.setOnClickListener(v -> {

            // tags
            ArrayList<String> tags = new ArrayList<>();
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                tags.add(chip.getText().toString());
            }

            // categories
            ArrayList<String> categories = new ArrayList<>();

            if (cbCulture.isChecked()) categories.add("Culture");
            if (cbLoisir.isChecked()) categories.add("Loisir");
            if (cbResto.isChecked()) categories.add("Restauration");
            if (cbDecouverte.isChecked()) categories.add("Découverte");

            // budget
            String budget = editBudget.getText().toString().trim();

            // duree
            String duree = editDuree.getText().toString().trim();

            // 🔹 effort
            String effort = null;
            if (cbFaible.isChecked()) effort = "Faible";
            if (cbMoyen.isChecked()) effort = "Moyen";
            if (cbFort.isChecked()) effort = "Fort";

            // si les tests suivant sont correcte, envoie dans l'activité suivante
            if (tags.isEmpty()) {
                Toast.makeText(this, "Ajoute au moins un tag", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categories.isEmpty()) {
                Toast.makeText(this, "Choisis au moins une catégorie", Toast.LENGTH_SHORT).show();
                return;
            }

            if (budget.isEmpty()) {
                editBudget.setError("Budget requis");
                return;
            }

            try {
                b = Integer.parseInt(budget);
            } catch (NumberFormatException e) {
                editBudget.setError("Le budget doit être un nombre");
                return;
            }

            if (duree.isEmpty() || !isValidDuree(duree)) {
                editDuree.setError("Durée invalide");
                return;
            }

            if (effort == null) {
                Toast.makeText(this, "Choisis un effort", Toast.LENGTH_SHORT).show();
                return;
            }

            // test sont bon, envoie dans l'activite
            Intent intent = new Intent(MainActivity.this, ParcoursActivity.class);

            intent.putStringArrayListExtra("tags", tags);
            intent.putStringArrayListExtra("categories", categories);
            intent.putExtra("budget", budget);
            intent.putExtra("duree", duree);
            intent.putExtra("effort", effort);

            startActivity(intent);
        });

    }

    private void rechercherAdresse(String text) {

        new Thread(() -> {

            try {

                String urlString = "https://photon.komoot.io/api/?q=" + URLEncoder.encode(text, "UTF-8") + "&limit=5";
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                conn.setRequestProperty("User-Agent", "TravelPath");
                conn.setRequestProperty("Accept", "application/json");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                JSONArray features = json.getJSONArray("features");
                suggestions.clear();

                for (int i = 0; i < features.length(); i++) {

                    JSONObject feature = features.getJSONObject(i);
                    JSONObject props = feature.getJSONObject("properties");
                    String nom = props.optString("name", "");
                    String city = props.optString("city", "");
                    String country = props.optString("country", "");
                    String adresse = nom + ", " + city + ", " + country;
                    suggestions.add(adresse);
                }

                runOnUiThread(() -> {
                    suggestionAdapter.notifyDataSetChanged();
                    if (suggestions.isEmpty()) {
                        recyclerSuggestions.setVisibility(View.GONE);
                    } else {
                        recyclerSuggestions.setVisibility(View.VISIBLE);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void addTag(String text) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(text)) {
                return;
            }
        }

        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);

        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
        });

        chipGroup.addView(chip);

    }

    private void setupSingleSelection() {

        cbFaible.setOnClickListener(v -> selectOnly(cbFaible));
        cbMoyen.setOnClickListener(v -> selectOnly(cbMoyen));
        cbFort.setOnClickListener(v -> selectOnly(cbFort));
    }

    private void selectOnly(CheckBox selected) {

        cbFaible.setChecked(selected == cbFaible);
        cbMoyen.setChecked(selected == cbMoyen);
        cbFort.setChecked(selected == cbFort);
    }

}