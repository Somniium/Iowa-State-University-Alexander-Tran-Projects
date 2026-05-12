package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onNotificationClick(DTOmodels.Notification notification);
        void onNotificationLongClick(DTOmodels.Notification notification);
    }

    private Context context;
    private List<DTOmodels.Notification> notifications;
    private OnItemClickListener listener;

    public NotificationAdapter(Context context, List<DTOmodels.Notification> notifications, OnItemClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View unreadIndicator;
        TextView message, time, typeBadge;

        ViewHolder(View itemView) {
            super(itemView);
            unreadIndicator = itemView.findViewById(R.id.notif_unread_indicator);
            message = itemView.findViewById(R.id.notif_message);
            time = itemView.findViewById(R.id.notif_time);
            typeBadge = itemView.findViewById(R.id.notif_type_badge);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DTOmodels.Notification notification = notifications.get(position);

        holder.message.setText(notification.message);
        holder.time.setText((RatingUtility.timeAgo(notification.createdAt)));
        holder.typeBadge.setText(notification.type);

        holder.unreadIndicator.setVisibility(notification.read ? View.GONE : View.VISIBLE);

        if(!notification.read) {
            holder.message.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
        } else {
            holder.message.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
        }

        int badgeColor;
        switch (notification.type != null ? notification.type : "") {
            case"COMMENT": badgeColor = android.graphics.Color.parseColor("#1E88E5"); break;
            case"LIKE": badgeColor = android.graphics.Color.parseColor("#E53935"); break;
            case"REVIEW": badgeColor = android.graphics.Color.parseColor("#43A047"); break;
            default: badgeColor = android.graphics.Color.parseColor("#888888"); break;
        }
        holder.typeBadge.getBackground().setColorFilter(badgeColor, android.graphics.PorterDuff.Mode.SRC_IN);

        holder.itemView.setOnClickListener(view -> listener.onNotificationClick(notification));
        holder.itemView.setOnLongClickListener(view -> {
            listener.onNotificationLongClick(notification); return true;
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}
