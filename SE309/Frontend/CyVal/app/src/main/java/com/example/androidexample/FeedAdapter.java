package com.example.androidexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FeedAdapter — RecyclerView adapter for the main feed.
 *
 * <p>Supports two distinct card layouts driven by {@link DTOmodels.FeedItem#type}:
 * <ul>
 *   <li>{@code TYPE_POST  (0)} → {@code item_feed_user_post.xml}  — a user's published review</li>
 *   <li>{@code TYPE_MEDIA (1)} → {@code item_feed_post.xml}        — a cover-art media card</li>
 * </ul>
 *
 * <p>The adapter holds a reference to {@code displayedItems} in FeedFragment.
 * That list is always mutated in-place (clear + addAll) so this reference
 * never goes stale — no need for a setter.
 *
 * <p>Item click events are forwarded to the host fragment via {@link OnItemClickListener}.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // -------------------------------------------------------------------------
    // View type constants — must match DTOmodels.FeedItem.TYPE_* values
    // -------------------------------------------------------------------------

    private static final int TYPE_POST  = DTOmodels.FeedItem.TYPE_POST;
    private static final int TYPE_MEDIA = DTOmodels.FeedItem.TYPE_MEDIA;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final Context context;
    private final OnItemClickListener listener;
    /** The live, in-place list managed by FeedFragment. Never replaced — only mutated. */
    private final List<DTOmodels.FeedItem> items;
    /**
     * Set of post IDs the current user has liked. Drives the ♥/♡ toggle on feed cards.
     * Managed by the host (FeedFragment) so that like state persists across re-binds.
     * Passed by reference — mutations made by the host are immediately visible here.
     */
    private final Set<Integer> likedPostIds;

    // =========================================================================
    // Constructors
    // =========================================================================

    /**
     * Full constructor — use in FeedFragment to enable the inline like toggle.
     *
     * @param likedPostIds live set of post IDs the current user has liked;
     *                     mutated by FeedFragment on each toggle
     */
    public FeedAdapter(Context context,
                       List<DTOmodels.FeedItem> items,
                       OnItemClickListener listener,
                       Set<Integer> likedPostIds) {
        this.context      = context;
        this.items        = items;
        this.listener     = listener;
        this.likedPostIds = likedPostIds;
    }

    /**
     * Backward-compatible constructor for contexts that don't need inline likes
     * (e.g. the reviews section in MediaDetailActivity).
     */
    public FeedAdapter(Context context,
                       List<DTOmodels.FeedItem> items,
                       OnItemClickListener listener) {
        this(context, items, listener, new HashSet<>());
    }

    // =========================================================================
    // RecyclerView.Adapter overrides
    // =========================================================================

    /** Returns TYPE_POST or TYPE_MEDIA so the correct layout is inflated. */
    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_POST) {
            View v = inflater.inflate(R.layout.item_feed_user_post, parent, false);
            return new PostViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_feed_post, parent, false);
            return new MediaViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DTOmodels.FeedItem item = items.get(position);
        if (holder instanceof PostViewHolder) {
            bindPost((PostViewHolder) holder, item.post);
        } else {
            bindMedia((MediaViewHolder) holder, item.media);
        }
    }

    // =========================================================================
    // Bind helpers
    // =========================================================================

    /**
     * Populates a user-post card.
     *
     * <p>The outer CardView border is tinted with the rating colour so the
     * score is immediately visible at a glance. The inner FrameLayout stays
     * white/neutral via {@code rounded_inner_bg}.
     */
    private void bindPost(PostViewHolder h, DTOmodels.FeedPost post) {
        int ratingColor = RatingUtility.getRatingColor(post.rating);

        // Card border tint + rating badge
        h.cardRoot.setCardBackgroundColor(ratingColor);
        h.ratingBadge.setText(post.rating > 0 ? String.valueOf(post.rating) : "-");
        h.ratingBadge.getBackground().setColorFilter(ratingColor, android.graphics.PorterDuff.Mode.SRC_IN);

        // Author avatar — circular CardView with a deterministic colour + initial
        String name = post.authorName != null ? post.authorName : "";
        h.authorInitial.setText(!name.isEmpty() ? String.valueOf(name.charAt(0)) : "?");
        h.authorAvatar.setCardBackgroundColor(RatingUtility.getUserColor(name));

        // Post metadata
        h.authorName.setText(post.authorName);
        h.mediaType.setText(post.mediaType);
        h.mediaTitle.setText(post.mediaTitle);
        h.timestamp.setText(RatingUtility.timeAgo(post.publishedAt));

        // Caption is optional — hide the view when empty to avoid blank space
        if (post.caption != null && !post.caption.isEmpty()) {
            h.caption.setText(post.caption);
            h.caption.setVisibility(View.VISIBLE);
        } else {
            h.caption.setVisibility(View.GONE);
        }

        // Social counters — LikeButton reflects liked state; count is a plain number
        boolean liked = likedPostIds.contains(post.id);
        h.likeBtn.setLiked(liked);
        h.likeCount.setText(String.valueOf(post.likeCount));
        h.commentCount.setText("💬 " + post.commentCount);

        // Like tap fires the OnLikeListener; card tap opens the detail view
        h.likeBtn.setOnLikeListener(new OnLikeListener() {
            @Override public void liked(LikeButton lb)   { listener.onLikeClick(post); }
            @Override public void unLiked(LikeButton lb) { listener.onLikeClick(post); }
        });
        h.itemView.setOnClickListener(v -> listener.onPostClick(post));
    }

    /**
     * Populates a media cover-art card (album, book, game, movie, or artist).
     *
     * <p>Cover image is loaded asynchronously via {@link ApiClient#loadImage}.
     * The card border is tinted by the media's average community rating.
     */
    private void bindMedia(MediaViewHolder h, DTOmodels.FeedMedia media) {
        int ratingColor = RatingUtility.getRatingColor(media.rating);

        // Card border tint + rating badge
        h.cardRoot.setCardBackgroundColor(ratingColor);
        h.ratingBadge.setText(media.rating > 0 ? String.valueOf(media.rating) : "-");
        h.ratingBadge.getBackground().setColorFilter(ratingColor, android.graphics.PorterDuff.Mode.SRC_IN);

        // Media details
        h.mediaType.setText(media.mediaType);
        h.title.setText(media.title);
        h.subtitle.setText(media.subtitle);
        h.extraInfo.setText(media.extraInfo);

        // Async cover image — falls back to placeholder if null/empty
        ApiClient.loadImage(context, media.imageUrl, h.coverImg);

        h.itemView.setOnClickListener(v -> listener.onMediaClick(media));
    }

    // =========================================================================
    // Click listener interface
    // =========================================================================

    /**
     * Implemented by the host (FeedFragment, MediaDetailActivity) to handle item events.
     *
     * <ul>
     *   <li>{@link #onPostClick} — card tap → open PostViewActivity</li>
     *   <li>{@link #onMediaClick} — media card tap → open MediaDetailActivity</li>
     *   <li>{@link #onLikeClick} — heart tap → toggle like (default no-op so existing
     *       implementations that don't need inline likes require no changes)</li>
     * </ul>
     */
    public interface OnItemClickListener {
        void onPostClick(DTOmodels.FeedPost post);
        void onMediaClick(DTOmodels.FeedMedia media);
        /** Called when the user taps the ♡/♥ heart on a feed card. Default is a no-op. */
        default void onLikeClick(DTOmodels.FeedPost post) { /* no-op */ }
    }

    // =========================================================================
    // ViewHolder classes
    // =========================================================================

    /**
     * ViewHolder for a published user post ({@code item_feed_user_post.xml}).
     *
     * <p>The card border is tinted by the rating colour. The inner surface stays
     * white via {@code rounded_inner_bg}. The rating badge overlays the bottom-right corner.
     * Caption is set to {@code GONE} when blank.
     */
    static class PostViewHolder extends RecyclerView.ViewHolder {
        CardView   cardRoot;      // outer card — background tinted by rating
        CardView   authorAvatar;  // circular avatar card
        TextView   authorInitial; // first letter of the author's name
        TextView   authorName;
        TextView   mediaType;     // e.g. "ALBUM", "MOVIE"
        TextView   timestamp;     // relative time string from RatingUtility.timeAgo()
        TextView   mediaTitle;
        TextView   caption;       // optional — hidden when blank
        LikeButton likeBtn;       // animated heart (jd-alexander/LikeButton)
        TextView   likeCount;     // plain numeric count next to the heart
        TextView   commentCount;
        TextView   ratingBadge;   // bottom-right overlay showing numeric score

        PostViewHolder(View v) {
            super(v);
            cardRoot      = v.findViewById(R.id.card_root);
            authorAvatar  = v.findViewById(R.id.feed_author_avatar);
            authorInitial = v.findViewById(R.id.feed_author_initial);
            authorName    = v.findViewById(R.id.feed_author_name);
            mediaType     = v.findViewById(R.id.feed_media_type);
            timestamp     = v.findViewById(R.id.feed_timestamp);
            mediaTitle    = v.findViewById(R.id.feed_media_title);
            caption       = v.findViewById(R.id.feed_caption);
            likeBtn       = v.findViewById(R.id.feed_like_btn);
            likeCount     = v.findViewById(R.id.feed_like_count);
            commentCount  = v.findViewById(R.id.feed_comment_count);
            ratingBadge   = v.findViewById(R.id.feed_rating_badge);
        }
    }

    /**
     * ViewHolder for a media cover-art card ({@code item_feed_post.xml}).
     *
     * <p>The card border is tinted by the community rating colour. Cover image (90×90 dp)
     * is loaded asynchronously by {@link ApiClient#loadImage}. The rating badge overlays
     * the bottom-right corner.
     */
    static class MediaViewHolder extends RecyclerView.ViewHolder {
        CardView  cardRoot;    // outer card — background tinted by rating
        ImageView coverImg;    // album art / movie poster / game cover
        TextView  mediaType;   // e.g. "ALBUM", "GAME"
        TextView  title;
        TextView  subtitle;    // artist name / director / author / developer
        TextView  extraInfo;   // genre / release date
        TextView  ratingBadge; // bottom-right overlay showing numeric score

        MediaViewHolder(View v) {
            super(v);
            cardRoot    = v.findViewById(R.id.card_root);
            coverImg    = v.findViewById(R.id.feed_cover_img);
            mediaType   = v.findViewById(R.id.feed_media_type);
            title       = v.findViewById(R.id.feed_title);
            subtitle    = v.findViewById(R.id.feed_subtitle);
            extraInfo   = v.findViewById(R.id.feed_extra_info);
            ratingBadge = v.findViewById(R.id.feed_rating_badge);
        }
    }
}
