package com.example.travelpath;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SuggestionAdapter
        extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    ArrayList<String> list;
    OnClickListener listener;

    public interface OnClickListener {
        void onClick(String text);
    }

    public SuggestionAdapter(ArrayList<String> list, OnClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txt;

        public ViewHolder(View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txtSuggestion);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String text = list.get(position);
        holder.txt.setText(text);
        holder.itemView.setOnClickListener(v -> listener.onClick(text));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}