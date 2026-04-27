package com.example.travelpath;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ChipGroup chipGroup;
    EditText editRecherche;
    EditText editDuree;
    CheckBox cbFaible, cbMoyen, cbFort;
    CheckBox cbCulture, cbLoisir, cbResto, cbDecouverte;
    EditText editBudget;
    Button btnValider;
    int b;

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

        // On part du principe que l'effort est moyen
        cbMoyen.setChecked(true);

        //Ajout d'un lieu en tag
        editRecherche.setOnEditorActionListener((v, actionId, event) -> {
            String text = editRecherche.getText().toString().trim();

            if (!text.isEmpty()) {
                addTag(text);
                editRecherche.setText("");
            }

            return true;
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