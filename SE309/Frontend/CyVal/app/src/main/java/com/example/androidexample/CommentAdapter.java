package com.example.androidexample;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final Context context;
    private final List<DTOmodels.FeedComment> comments;
    private final int currentUserId;
    private final int postAuthorId;
    private final OnCommentActionListener listener;

    public CommentAdapter(Context context, List<DTOmodels.FeedComment> comments,
                          int currentUserId, int postAuthorId,
                          OnCommentActionListener listener) {
        this.context = context;
        this.comments = comments;
        this.currentUserId = currentUserId;
        this.postAuthorId = postAuthorId;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        DTOmodels.FeedComment comment = comments.get(position);

        Log.d("CommentAdapter", "Binding comment " + comment.id + " by " + comment.authorName);

        // Author initial with randomized color
        h.authorInitial.setText(comment.authorName != null && !comment.authorName.isEmpty()
                ? String.valueOf(comment.authorName.charAt(0)) : "?");
        int avatarColor = RatingUtility.getUserColor(comment.authorName);
        h.authorInitial.getBackground().setColorFilter(avatarColor, android.graphics.PorterDuff.Mode.SRC_IN);

        h.authorName.setText(comment.authorName);
        h.body.setText(comment.body);
        h.timestamp.setText(RatingUtility.timeAgo(comment.createdAt));

        // Three dot menu: options change based on ownership
        boolean isCommentAuthor = comment.authorId == currentUserId;
        boolean isPostAuthor = currentUserId == postAuthorId;

        h.menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);

            if (isCommentAuthor) {
                popup.getMenu().add("Edit");
                Log.d("CommentAdapter", "Menu: showing edit for comment " + comment.id);
            }
            if (isCommentAuthor || isPostAuthor) {
                popup.getMenu().add("Delete");
                Log.d("CommentAdapter", "Menu: showing delete for comment " + comment.id);
            }
            if (!isCommentAuthor) {
                popup.getMenu().add("Report");
                Log.d("CommentAdapter", "Menu: showing report for comment " + comment.id);
            }

            popup.setOnMenuItemClickListener(item -> {
                Log.d("CommentAdapter", "Menu item tapped: " + item.getTitle() + " on comment " + comment.id);
                if (item.getTitle().equals("Edit")) {
                    listener.onEditComment(comment);
                } else if (item.getTitle().equals("Delete")) {
                    listener.onDeleteComment(comment);
                } else if (item.getTitle().equals("Report")) {
                    // TODO: implement reporting
                    Log.d("CommentAdapter", "Report tapped for comment " + comment.id);
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public interface OnCommentActionListener {
        void onEditComment(DTOmodels.FeedComment comment);

        void onDeleteComment(DTOmodels.FeedComment comment);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorInitial, authorName, timestamp, body;
        ImageView menu;

        ViewHolder(View v) {
            super(v);
            authorInitial = v.findViewById(R.id.comment_author_initial);
            authorName = v.findViewById(R.id.comment_author_name);
            timestamp = v.findViewById(R.id.comment_timestamp);
            body = v.findViewById(R.id.comment_body);
            menu = v.findViewById(R.id.comment_menu);
        }
    }
}