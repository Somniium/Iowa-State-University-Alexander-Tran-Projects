package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final Context context;
    private final List<DTOmodels.SearchResult> results;
    private final OnResultClickListener listener;

    public SearchResultAdapter(Context context, List<DTOmodels.SearchResult> results, OnResultClickListener listener) {
        this.context = context;
        this.results = results;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        DTOmodels.SearchResult r = results.get(position);

        h.title.setText(r.title);
        h.subtitle.setText(r.subtitle);
        h.typeBadge.setText(r.mediaType);

        h.cover.setImageResource(R.drawable.cyval_logo);

        if (r.imageUrl != null && !r.imageUrl.isEmpty()) {
            ApiClient.loadImage(context, r.imageUrl, h.cover);
        } else {
            h.cover.setImageResource(R.drawable.cyval_logo);
        }

        // Set badge color based on media type
        int badgeColor;
        switch (r.mediaType) {
            case "ALBUM":
            case "MUSIC": // legacy value — kept for backward compatibility
                badgeColor = android.graphics.Color.parseColor("#E53935");
                break;
            case "GAME":
                badgeColor = android.graphics.Color.parseColor("#8E24AA");
                break;
            case "MOVIE":
                badgeColor = android.graphics.Color.parseColor("#1E88E5");
                break;
            case "BOOK":
                badgeColor = android.graphics.Color.parseColor("#43A047");
                break;
            case "SHOW":
                badgeColor = android.graphics.Color.parseColor("#FB8C00");
                break;
            case "ARTIST":
                badgeColor = android.graphics.Color.parseColor("#FB12C0");
                break;
            default:
                badgeColor = android.graphics.Color.parseColor("#888888");
                break;
        }
        h.typeBadge.getBackground().setColorFilter(badgeColor, android.graphics.PorterDuff.Mode.SRC_IN);

        if (r.imageUrl != null && !r.imageUrl.isEmpty()) {
            ApiClient.loadImage(context, r.imageUrl, h.cover);
        }

        h.itemView.setOnClickListener(v -> listener.onResultClick(r));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public interface OnResultClickListener {
        void onResultClick(DTOmodels.SearchResult result);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, subtitle, typeBadge;

        ViewHolder(View v) {
            super(v);
            cover = v.findViewById(R.id.result_cover);
            title = v.findViewById(R.id.result_title);
            subtitle = v.findViewById(R.id.result_subtitle);
            typeBadge = v.findViewById(R.id.result_type_badge);
        }
    }
}