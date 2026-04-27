package com.example.travelpath;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ParcoursAdapter extends RecyclerView.Adapter<ParcoursAdapter.ViewHolder> {

    List<Parcours> list;
    Context context;

    public ParcoursAdapter(List<Parcours> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parcours, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Parcours p = list.get(position);

        holder.titre.setText(p.titre);
        holder.info.setText(p.prix + "€ • " + p.duree + " • " + p.adresse);
        holder.image.setImageResource(p.image);

        holder.details.setVisibility(p.isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            p.isExpanded = !p.isExpanded;
            notifyItemChanged(position);
        });

        // like ou suppression du like
        holder.like.setImageResource(
                p.liked ? R.drawable.ic_heart_plus : R.drawable.ic_heart_checked
        );

        holder.like.setOnClickListener(v -> {
            p.liked = !p.liked;
            notifyItemChanged(position);
        });

        // affichage de chaque etape du parcours
        holder.details.removeAllViews();

        for (Etape e : p.etapes) {

            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_etape, null);

            ImageView img = view.findViewById(R.id.imgEtape);
            TextView txt = view.findViewById(R.id.txtEtape);

            img.setImageResource(e.image);

            txt.setText(e.nom + "\n" + e.adresse + "\n" + "Trajet: " + e.trajet + "\n" + "Durée: " + e.duree + "\n" + e.prix + "€");

            holder.details.addView(view);
        }

        // transformation en pdf
        holder.btnPdf.setOnClickListener(v -> exportPDF(p));
    }

    //Pour l'instant on fait pas grand chose dans le téléchargement pdf
    private void exportPDF(Parcours p) {

        PdfDocument doc = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(300, 600, 1).create();

        PdfDocument.Page page = doc.startPage(pageInfo);

        int y = 25;

        for (Etape e : p.etapes) {
            page.getCanvas().drawText(e.nom + " - " + e.prix + "€", 10, y, new android.graphics.Paint());
            y += 20;
        }

        doc.finishPage(page);

        try {
            File file = new File(context.getExternalFilesDir(null), "parcours.pdf");
            doc.writeTo(new FileOutputStream(file));
            doc.close();
            Toast.makeText(context, "PDF enregistré", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titre, info;
        ImageView image, like;
        LinearLayout details;
        Button btnPdf;

        public ViewHolder(View itemView) {
            super(itemView);

            titre = itemView.findViewById(R.id.titreParcours);
            info = itemView.findViewById(R.id.infoParcours);
            image = itemView.findViewById(R.id.imageParcours);
            like = itemView.findViewById(R.id.likeBtn);
            details = itemView.findViewById(R.id.layoutDetails);
            btnPdf = itemView.findViewById(R.id.btnPdf);
        }
    }
}