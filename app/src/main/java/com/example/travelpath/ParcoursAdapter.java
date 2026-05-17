package com.example.travelpath;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ParcoursAdapter extends RecyclerView.Adapter<ParcoursAdapter.ViewHolder> {

    List<Parcours> list;
    Context context;
    SharedPreferences prefs;

    public ParcoursAdapter(List<Parcours> list, Context context) {
        this.list = list;
        this.context = context;
        this.prefs = context.getSharedPreferences("travelpath_likes", Context.MODE_PRIVATE);
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
        p.liked = prefs.getBoolean(p.id, false);
        holder.titre.setText(p.titre);
        holder.info.setText(
                p.prix + "€ • " +
                        p.duree + " • " +
                        p.adresse
        );

        holder.image.setImageResource(p.image);

        holder.details.setVisibility(
                p.isExpanded ?
                        View.VISIBLE :
                        View.GONE
        );

        holder.like.setImageResource(
                p.liked ?
                        R.drawable.ic_heart_checked :
                        R.drawable.ic_heart_plus
        );

        holder.itemView.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {

                Parcours parcours = list.get(pos);

                parcours.isExpanded =
                        !parcours.isExpanded;

                notifyItemChanged(pos);
            }
        });

        holder.like.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {

                Parcours parcours = list.get(pos);

                parcours.liked =
                        !parcours.liked;

                // sauvegarde téléphone
                prefs.edit()
                        .putBoolean(
                                parcours.titre + parcours.adresse,
                                parcours.liked
                        )
                        .apply();

                notifyItemChanged(pos);
            }
        });

        holder.details.removeAllViews();

        for (Etape e : p.etapes) {

            View view = LayoutInflater.from(context)
                    .inflate(
                            R.layout.item_etape,
                            holder.details,
                            false
                    );

            ImageView img =
                    view.findViewById(R.id.imgEtape);

            TextView txt =
                    view.findViewById(R.id.txtEtape);

            Glide.with(context)
                    .load(e.imageUrl)
                    .placeholder(R.drawable.comedie)
                    .error(R.drawable.comedie)
                    .into(img);

            txt.setText(
                    e.nom + "\n" +
                            e.adresse + "\n" +
                            "Trajet : " + e.trajet + "\n" +
                            "Durée : " + e.duree + "\n" +
                            e.prix + "€"
            );

            holder.details.addView(view);
        }

        holder.btnPdf.setOnClickListener(v ->
                exportPDF(p));
    }

    //Pour l'instant on fait pas grand chose dans le téléchargement pdf
    private void exportPDF(Parcours p) {

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(24);
        titlePaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setTextSize(14);

        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(2);

        int y = 40;

        // TITRE
        canvas.drawText("TRAVELPATH", 200, y, titlePaint);

        y += 40;

        canvas.drawLine(30, y, 560, y, linePaint);

        y += 35;

        // NOM PARCOURS
        canvas.drawText(p.titre, 30, y, titlePaint);

        y += 30;

        canvas.drawText("Ville : " + p.adresse, 30, y, textPaint);
        y += 20;

        canvas.drawText("Prix : " + p.prix + "€", 30, y, textPaint);
        y += 20;

        canvas.drawText("Durée : " + p.duree, 30, y, textPaint);

        y += 30;

        // IMAGE PARCOURS
        Bitmap mainBmp = BitmapFactory.decodeResource(
                context.getResources(),
                p.image
        );

        Bitmap mainScaled =
                Bitmap.createScaledBitmap(mainBmp, 520, 180, false);

        canvas.drawBitmap(mainScaled, 30, y, null);

        y += 220;

        // ETAPES
        for (int i = 0; i < p.etapes.size(); i++) {

            Etape e = p.etapes.get(i);

            // Si page pleine -> nouvelle page
            if (y > 700) {
                document.finishPage(page);

                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }

            canvas.drawLine(30, y, 560, y, linePaint);
            y += 25;

            canvas.drawText("Étape " + (i + 1) + " : " + e.nom, 30, y, titlePaint);

            y += 25;

            // IMAGE étape
            Bitmap bmp = BitmapFactory.decodeResource(
                    context.getResources(),
                    e.image
            );

            Bitmap scaled =
                    Bitmap.createScaledBitmap(bmp, 220, 120, false);

            canvas.drawBitmap(scaled, 30, y, null);

            // TEXTE à droite image
            canvas.drawText("Adresse : " + e.adresse, 270, y + 20, textPaint);
            canvas.drawText("Trajet : " + e.trajet, 270, y + 40, textPaint);
            canvas.drawText("Durée : " + e.duree, 270, y + 60, textPaint);
            canvas.drawText("Budget : " + e.prix + "€", 270, y + 80, textPaint);

            y += 150;
        }

        y += 20;

        canvas.drawLine(30, y, 560, y, linePaint);
        y += 30;

        canvas.drawText("Merci d'utiliser TravelPath", 180, y, textPaint);

        document.finishPage(page);

        try {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File file = new File(folder, "parcours.pdf");

            document.writeTo(new FileOutputStream(file));

            Toast.makeText(
                    context,
                    "PDF créé : " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        document.close();
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