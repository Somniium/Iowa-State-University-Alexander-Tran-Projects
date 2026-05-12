package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.viewholder>{

    ArrayList<String> sessionMessages = new ArrayList<String>();

    public MessageAdapter(ArrayList<String> sessionMessages) {
        this.sessionMessages = sessionMessages;
    };

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message,parent, false);
        return new viewholder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        holder.bodyText.setText(sessionMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return sessionMessages.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView bodyText;
        public viewholder(@NonNull View itemView) {
            super(itemView);

            bodyText = itemView.findViewById(R.id.body_text);
        }
    }
}
