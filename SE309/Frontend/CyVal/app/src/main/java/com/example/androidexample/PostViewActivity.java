package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.like.LikeButton;
import com.like.OnLikeListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * PostViewActivity — full-screen post detail view.
 *
 * <p>Receives post data via Intent extras (set by {@link FeedAdapter} when a post card
 * is tapped) and displays:
 * <ul>
 *   <li>Author avatar, name, and relative timestamp</li>
 *   <li>Media-type chip and optional caption</li>
 *   <li>Review body (fetched from {@code /reviews/{reviewId}})</li>
 *   <li>An inflated media card (album, book, game, or movie) linked to {@link MediaDetailActivity}</li>
 *   <li>Like/comment counts and a scrollable comments section</li>
 * </ul>
 *
 * <p>The post owner sees Edit / Delete menu items; other users see a Report option.
 * Follow/unfollow is available for all users except the post's own author.
 */
public class PostViewActivity extends AppCompatActivity {

    private static final String TAG = "PostDetail";

    // =========================================================================
    // Views
    // =========================================================================

    private TextView authorInitial, authorName, timestamp;
    private TextView mediaTypeChip, caption, reviewBody, followingChip;
    private View captionDivider;
    private LikeButton likeButton;
    private TextView likeCount, commentCount, commentsHeader;
    private ImageView postMenu, btnBack;
    private FrameLayout mediaCardContainer;
    private CardView authorAvatar;

    private RecyclerView commentsRecycler;
    private CommentAdapter commentAdapter;
    private List<DTOmodels.FeedComment> comments;
    private EditText commentInput;
    private Button submitCommentBtn;

    // =========================================================================
    // Post data (received via Intent)
    // =========================================================================

    private int postId;
    private int reviewId;
    private int authorId;
    private String authorNameStr;
    private String captionStr;
    private String mediaTitleStr;
    private String mediaTypeStr;
    private int rating;
    private int likes;
    private int commentCt;
    private String publishedAt;

    // =========================================================================
    // Session state
    // =========================================================================

    private int currentUserId;
    private boolean isFollowing = false;
    private boolean isLiked     = false;

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);

        // Views
        btnBack            = findViewById(R.id.btn_back);
        authorAvatar       = findViewById(R.id.detail_author_avatar);
        authorInitial      = findViewById(R.id.detail_author_initial);
        authorName         = findViewById(R.id.detail_author_name);
        timestamp          = findViewById(R.id.detail_timestamp);
        mediaTypeChip      = findViewById(R.id.detail_media_type_chip);
        followingChip      = findViewById(R.id.detail_following_chip);
        caption            = findViewById(R.id.detail_caption);
        captionDivider     = findViewById(R.id.detail_caption_divider);
        reviewBody         = findViewById(R.id.detail_review_body);
        likeButton         = findViewById(R.id.detail_like_button);
        likeCount          = findViewById(R.id.detail_like_count);
        commentCount       = findViewById(R.id.detail_comment_count);
        commentsHeader     = findViewById(R.id.comments_header);
        postMenu           = findViewById(R.id.detail_post_menu);
        mediaCardContainer = findViewById(R.id.detail_media_card_container);
        commentsRecycler   = findViewById(R.id.comments_recycler_view);
        commentInput       = findViewById(R.id.comment_input);
        submitCommentBtn   = findViewById(R.id.btn_submit_comment);

        // Intent extras
        postId        = getIntent().getIntExtra("postId", -1);
        reviewId      = getIntent().getIntExtra("reviewId", -1);
        authorId      = getIntent().getIntExtra("authorId", 0);
        authorNameStr = getIntent().getStringExtra("authorName");
        captionStr    = getIntent().getStringExtra("caption");
        mediaTitleStr = getIntent().getStringExtra("mediaTitle");
        mediaTypeStr  = getIntent().getStringExtra("mediaType");
        rating        = getIntent().getIntExtra("rating", 0);
        likes         = getIntent().getIntExtra("likeCount", 0);
        commentCt     = getIntent().getIntExtra("commentCount", 0);
        publishedAt   = getIntent().getStringExtra("publishedAt");
        currentUserId = getIntent().getIntExtra("USER_ID", -1);

        Log.d(TAG, "Intent data: postId=" + postId + " reviewId=" + reviewId
                + " author=" + authorNameStr + " mediaType=" + mediaTypeStr
                + " rating=" + rating + " title=" + mediaTitleStr);

        // Populate static post fields immediately, then load dynamic data
        displayPost();

        // Comments list
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments, currentUserId, authorId,
                new CommentAdapter.OnCommentActionListener() {
                    @Override
                    public void onEditComment(DTOmodels.FeedComment comment) {
                        Log.d(TAG, "Edit comment tapped: id=" + comment.id);
                        showEditCommentDialog(comment);
                    }

                    @Override
                    public void onDeleteComment(DTOmodels.FeedComment comment) {
                        Log.d(TAG, "Delete comment tapped: id=" + comment.id);
                        deleteComment(comment);
                    }
                });
        commentsRecycler.setAdapter(commentAdapter);

        loadComments();
        loadMediaDetails();

        // Like button — only wired for real posts (postId > 0)
        if (postId > 0) {
            loadLikeStatus();
            likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton lb) {
                    toggleLike();
                }
                @Override
                public void unLiked(LikeButton lb) {
                    toggleLike();
                }
            });
        } else {
            likeButton.setEnabled(false);
        }

        btnBack.setOnClickListener(v -> finish());

        submitCommentBtn.setOnClickListener(v -> {
            String body = commentInput.getText().toString().trim();
            if (body.isEmpty()) {
                Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show();
                return;
            }
            submitComment(body);
        });

        // Follow / unfollow button
        followingChip.setOnClickListener(v -> {
            if (currentUserId == authorId) {
                Toast.makeText(this, "You cannot follow yourself.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isFollowing) {
                unfollowUser();
                Toast.makeText(this, "No longer following user.", Toast.LENGTH_SHORT).show();
            } else {
                followUser();
                Toast.makeText(this, "Followed!", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "onCreate complete");
    }

    // =========================================================================
    // Static post display
    // =========================================================================

    /**
     * Populates all views that can be filled from Intent extras immediately,
     * without waiting for any network calls.
     */
    private void displayPost() {
        Log.d(TAG, "Displaying post content");

        // Author avatar — deterministic colour from username, initial letter
        authorInitial.setText(authorNameStr != null && !authorNameStr.isEmpty()
                ? String.valueOf(authorNameStr.charAt(0)) : "?");
        authorAvatar.setCardBackgroundColor(RatingUtility.getUserColor(authorNameStr));
        authorName.setText(authorNameStr);

        timestamp.setText(RatingUtility.timeAgo(publishedAt));

        // Hide follow chip entirely for your own posts; show it for other authors
        if (currentUserId == authorId) {
            followingChip.setVisibility(View.GONE);
        } else {
            checkIfFollowing();
        }

        // Media type chip (colour from shared RatingUtility)
        mediaTypeChip.setText(mediaTypeStr);
        int chipColor = RatingUtility.getMediaTypeColor(mediaTypeStr);
        mediaTypeChip.getBackground().setColorFilter(chipColor, android.graphics.PorterDuff.Mode.SRC_IN);

        // Caption (optional — hide views rather than show empty string)
        if (captionStr != null && !captionStr.isEmpty()) {
            caption.setText(captionStr);
            caption.setVisibility(View.VISIBLE);
            captionDivider.setVisibility(View.VISIBLE);
            Log.d(TAG, "Caption shown: " + captionStr);
        } else {
            caption.setVisibility(View.GONE);
            captionDivider.setVisibility(View.GONE);
            Log.d(TAG, "No caption — hidden");
        }

        // Review body is loaded asynchronously in loadMediaDetails(); hide until then
        reviewBody.setVisibility(View.GONE);

        likeCount.setText("♡ " + likes);
        commentCount.setText("💬 " + commentCt);
        commentsHeader.setText("Comments (" + commentCt + ")");

        // Post context menu — options differ based on whether the viewer is the author
        postMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            if (authorId == currentUserId) {
                popup.getMenu().add("Edit caption");
                popup.getMenu().add("Edit review");
                popup.getMenu().add("Delete post");
            } else {
                popup.getMenu().add("Report post");
            }
            popup.setOnMenuItemClickListener(item -> {
                Log.d(TAG, "Menu item tapped: " + item.getTitle());
                if ("Edit caption".equals(item.getTitle())) {
                    showEditCaptionDialog();
                } else if ("Edit review".equals(item.getTitle())) {
                    showEditReviewDialog();
                } else if ("Delete post".equals(item.getTitle())) {
                    deletePost();
                } else if ("Report post".equals(item.getTitle())) {
                    Toast.makeText(this, "Post reported", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popup.show();
        });
    }

    // =========================================================================
    // Follow / unfollow
    // =========================================================================

    /**
     * Checks whether the current user already follows the post's author.
     * Updates {@link #followingChip} and {@link #isFollowing} on success.
     *
     * @author Edwin Cepeda
     */
    private void checkIfFollowing() {
        followingChip.setText("Follow");
        ApiClient.getArray(this, "/follower/get-following/" + currentUserId, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                // Scan the followed-user list for the post's author
                for (int i = 0; i < response.length(); i++) {
                    try {
                        int followedId = response.getJSONObject(i).getInt("id");
                        if (followedId == authorId) {
                            followingChip.setText("Followed");
                            followingChip.setBackgroundColor(Color.parseColor("#A9A9A9"));
                            isFollowing = true;
                            return;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing follow entry: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to check following status: " + message);
            }
        });
    }

    /**
     * Sends a follow request and updates the chip on success.
     *
     * @author Edwin Cepeda
     */
    private void followUser() {
        String endpoint = String.format(
                "/follower/follow-user/currUser/%d/followUser/%d", currentUserId, authorId);
        ApiClient.post(this, endpoint, null, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                isFollowing = true;
                followingChip.setText("Followed");
                followingChip.setBackgroundColor(Color.parseColor("#A9A9A9"));
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Follow failed: " + message);
                Toast.makeText(PostViewActivity.this, "Could not follow user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends an unfollow request and resets the chip on success.
     *
     * @author Edwin Cepeda
     */
    private void unfollowUser() {
        String endpoint = String.format(
                "/follower/remove-follower/currUser/%d/followUser/%d", currentUserId, authorId);
        ApiClient.deleteString(this, endpoint, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                isFollowing = false;
                followingChip.setText("Follow");
                followingChip.setBackground(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.media_type_chip_bg, null));
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Unfollow failed: " + message);
                Toast.makeText(PostViewActivity.this, "Could not unfollow user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================================
    // Media card
    // =========================================================================

    /**
     * Fetches the review to get the review body and media id, then loads the
     * appropriate media type (album, book, game, or movie).
     */
    private void loadMediaDetails() {
        if (reviewId == -1) {
            Log.e(TAG, "No reviewId — showing fallback");
            showFallbackCard();
            return;
        }

        Log.d(TAG, "Loading review " + reviewId + " for media details");

        ApiClient.get(this, "/reviews/" + reviewId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    // Show the review body if present
                    if (!response.isNull("body")) {
                        reviewBody.setText(response.getString("body"));
                        reviewBody.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Review body loaded");
                    }

                    // Route to the correct media loader based on which id field is set
                    if (!response.isNull("albumId")) {
                        loadAlbum(response.getLong("albumId"));
                    } else if (!response.isNull("bookId")) {
                        loadBook(response.getLong("bookId"));
                    } else if (!response.isNull("gameId")) {
                        loadGame(response.getLong("gameId"));
                    } else if (!response.isNull("movieId")) {
                        loadMovie(response.getLong("movieId"));
                    } else {
                        // Review has no media FK (old post) — search DB by type + title
                        Log.d(TAG, "No media id in review — searching by title: " + mediaTitleStr);
                        searchMediaByTitle();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing review: " + e.getMessage());
                    showFallbackCard();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to load review: " + message);
                showFallbackCard();
            }
        });
    }

    /**
     * Called when the review has no media FK (posts created before the requesterId
     * fix, or for unsupported types like ARTIST). Searches each backend list endpoint
     * for an item whose title matches {@link #mediaTitleStr}. On a hit calls
     * {@link #showMediaCard} with the real ID; on a miss falls back to
     * {@link #showFallbackCard()}.
     */
    private void searchMediaByTitle() {
        if (mediaTitleStr == null || mediaTitleStr.isEmpty()) {
            showFallbackCard();
            return;
        }
        String lower = mediaTitleStr.toLowerCase();
        String type  = mediaTypeStr != null ? mediaTypeStr : "";

        switch (type) {
            case "ALBUM":
            case "MUSIC":
                ApiClient.getArray(this, "/albums", new Api_Array_Interface() {
                    @Override public void onSuccess(JSONArray response) {
                        try {
                            Gson gson = new Gson();
                            for (int i = 0; i < response.length(); i++) {
                                DTOmodels.album a = gson.fromJson(
                                        response.getJSONObject(i).toString(), DTOmodels.album.class);
                                if (a.name != null && a.name.toLowerCase().contains(lower)) {
                                    showMediaCard(a.name, a.artist, a.genre, a.rating, a.coverURL, a.albumId);
                                    return;
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Album title search: " + e.getMessage()); }
                        showFallbackCard();
                    }
                    @Override public void onError(String m) { showFallbackCard(); }
                });
                break;

            case "GAME":
                ApiClient.getArray(this, "/games", new Api_Array_Interface() {
                    @Override public void onSuccess(JSONArray response) {
                        try {
                            Gson gson = new Gson();
                            for (int i = 0; i < response.length(); i++) {
                                DTOmodels.game g = gson.fromJson(
                                        response.getJSONObject(i).toString(), DTOmodels.game.class);
                                if (g.title != null && g.title.toLowerCase().contains(lower)) {
                                    showMediaCard(g.title, g.developer, g.genre, g.rating, g.coverUrl, g.id);
                                    return;
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Game title search: " + e.getMessage()); }
                        showFallbackCard();
                    }
                    @Override public void onError(String m) { showFallbackCard(); }
                });
                break;

            case "MOVIE":
            case "SHOW":
                ApiClient.getArray(this, "/movies", new Api_Array_Interface() {
                    @Override public void onSuccess(JSONArray response) {
                        try {
                            Gson gson = new Gson();
                            for (int i = 0; i < response.length(); i++) {
                                DTOmodels.movie m = gson.fromJson(
                                        response.getJSONObject(i).toString(), DTOmodels.movie.class);
                                if (m.title != null && m.title.toLowerCase().contains(lower)) {
                                    showMediaCard(m.title, m.director, m.genre, m.rating, m.posterUrl, m.id);
                                    return;
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Movie title search: " + e.getMessage()); }
                        showFallbackCard();
                    }
                    @Override public void onError(String m) { showFallbackCard(); }
                });
                break;

            case "BOOK":
                ApiClient.getArray(this, "/books", new Api_Array_Interface() {
                    @Override public void onSuccess(JSONArray response) {
                        try {
                            Gson gson = new Gson();
                            for (int i = 0; i < response.length(); i++) {
                                DTOmodels.book b = gson.fromJson(
                                        response.getJSONObject(i).toString(), DTOmodels.book.class);
                                if (b.title != null && b.title.toLowerCase().contains(lower)) {
                                    showMediaCard(b.title, b.authors, b.publisheddate, 0, b.thumbnailurl, (long) b.id);
                                    return;
                                }
                            }
                        } catch (Exception e) { Log.e(TAG, "Book title search: " + e.getMessage()); }
                        showFallbackCard();
                    }
                    @Override public void onError(String m) { showFallbackCard(); }
                });
                break;

            default:
                // ARTIST and unknown types — no media detail screen available
                showFallbackCard();
        }
    }

    /**
     * Shows a minimal media card built from the Intent extras when no media detail
     * can be loaded (e.g. artist posts, or a failed network call).
     */
    private void showFallbackCard() {
        View mediaCard = LayoutInflater.from(this).inflate(R.layout.item_feed_post, mediaCardContainer, false);

        TextView titleView    = mediaCard.findViewById(R.id.feed_title);
        TextView subtitleView = mediaCard.findViewById(R.id.feed_subtitle);
        TextView typeView     = mediaCard.findViewById(R.id.feed_media_type);
        TextView extraView    = mediaCard.findViewById(R.id.feed_extra_info);
        TextView badgeView    = mediaCard.findViewById(R.id.feed_rating_badge);
        ImageView coverView   = mediaCard.findViewById(R.id.feed_cover_img);
        CardView cardRoot     = mediaCard.findViewById(R.id.card_root);

        titleView.setText(mediaTitleStr);
        subtitleView.setText(mediaTypeStr);
        typeView.setText(mediaTypeStr);
        extraView.setVisibility(View.GONE);
        coverView.setVisibility(View.GONE);

        int color = RatingUtility.getRatingColor(rating);
        cardRoot.setCardBackgroundColor(color);
        badgeView.setText(rating > 0 ? String.valueOf(rating) : "-");
        badgeView.getBackground().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

        // Fallback card is also tappable so the UI is consistent with the scroll feed.
        // We don't have a real mediaId here (that's why we fell back), so we pass 0.
        // MediaDetailActivity handles mediaId==0 with a "not in database" screen.
        mediaCard.setOnClickListener(v -> {
            Log.d(TAG, "Fallback card tapped: " + mediaTypeStr + " title=" + mediaTitleStr);
            Intent intent = new Intent(this, MediaDetailActivity.class);
            intent.putExtra("mediaType", mediaTypeStr);
            intent.putExtra("mediaId",   0L);
            intent.putExtra("title",     mediaTitleStr);
            intent.putExtra("USER_ID",   currentUserId);
            startActivity(intent);
        });

        mediaCardContainer.addView(mediaCard);
        mediaCardContainer.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fallback card shown: " + mediaTitleStr);
    }

    private void loadAlbum(long id) {
        ApiClient.get(this, "/album/" + id, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.album album = new Gson().fromJson(response.toString(), DTOmodels.album.class);
                    Log.d(TAG, "Album loaded: " + album.name + " rating=" + album.rating);
                    showMediaCard(album.name, album.artist, album.genre, album.rating, album.coverURL, album.albumId);
                } catch (Exception e) {
                    Log.e(TAG, "Album parse error: " + e.getMessage());
                    showFallbackCard();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to load album " + id + ": " + message);
                showFallbackCard();
            }
        });
    }

    private void loadBook(long id) {
        ApiClient.get(this, "/books/" + id, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.book book = new Gson().fromJson(response.toString(), DTOmodels.book.class);
                    Log.d(TAG, "Book loaded: " + book.title);
                    showMediaCard(book.title, book.authors, book.publisheddate, 0, book.thumbnailurl, (long) book.id);
                } catch (Exception e) {
                    Log.e(TAG, "Book parse error: " + e.getMessage());
                    showFallbackCard();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to load book " + id + ": " + message);
                showFallbackCard();
            }
        });
    }

    private void loadGame(long id) {
        ApiClient.get(this, "/games/" + id, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.game game = new Gson().fromJson(response.toString(), DTOmodels.game.class);
                    Log.d(TAG, "Game loaded: " + game.title + " rating=" + game.rating);
                    showMediaCard(game.title, game.developer, game.genre, game.rating, game.coverUrl, game.id);
                } catch (Exception e) {
                    Log.e(TAG, "Game parse error: " + e.getMessage());
                    showFallbackCard();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to load game " + id + ": " + message);
                showFallbackCard();
            }
        });
    }

    private void loadMovie(long id) {
        ApiClient.get(this, "/movies/" + id, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.movie movie = new Gson().fromJson(response.toString(), DTOmodels.movie.class);
                    Log.d(TAG, "Movie/show loaded: " + movie.title + " rating=" + movie.rating);
                    showMediaCard(movie.title, movie.director, movie.genre, movie.rating, movie.posterUrl, movie.id);
                } catch (Exception e) {
                    Log.e(TAG, "Movie parse error: " + e.getMessage());
                    showFallbackCard();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to load movie " + id + ": " + message);
                showFallbackCard();
            }
        });
    }

    /**
     * Inflates a media card, populates it with the provided details, and wires a
     * click listener that opens {@link MediaDetailActivity}.
     */
    private void showMediaCard(String title, String subtitle, String extra, int mediaRating,
                               String imageUrl, long mediaId) {
        Log.d(TAG, "Showing media card: " + title + " rating=" + mediaRating + " image=" + imageUrl);

        View mediaCard    = LayoutInflater.from(this).inflate(R.layout.item_feed_post, mediaCardContainer, false);
        TextView titleView    = mediaCard.findViewById(R.id.feed_title);
        TextView subtitleView = mediaCard.findViewById(R.id.feed_subtitle);
        TextView typeView     = mediaCard.findViewById(R.id.feed_media_type);
        TextView extraView    = mediaCard.findViewById(R.id.feed_extra_info);
        TextView badgeView    = mediaCard.findViewById(R.id.feed_rating_badge);
        ImageView coverView   = mediaCard.findViewById(R.id.feed_cover_img);
        CardView cardRoot     = mediaCard.findViewById(R.id.card_root);

        titleView.setText(title);
        subtitleView.setText(subtitle);
        typeView.setText(mediaTypeStr);
        extraView.setText(extra);

        int color = RatingUtility.getRatingColor(mediaRating);
        cardRoot.setCardBackgroundColor(color);
        badgeView.setText(mediaRating > 0 ? String.valueOf(mediaRating) : "-");
        badgeView.getBackground().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

        ApiClient.loadImage(this, imageUrl, coverView);

        // Tap the media card to open the full media detail screen (mirrors feed behaviour)
        mediaCard.setOnClickListener(v -> {
            Log.d(TAG, "Media card tapped: " + mediaTypeStr + " id=" + mediaId);
            Intent intent = new Intent(this, MediaDetailActivity.class);
            intent.putExtra("mediaType", mediaTypeStr);
            intent.putExtra("mediaId",   mediaId);
            intent.putExtra("title",     title);
            intent.putExtra("USER_ID",   currentUserId);
            startActivity(intent);
        });

        mediaCardContainer.addView(mediaCard);
        mediaCardContainer.setVisibility(View.VISIBLE);
        Log.d(TAG, "Media card visible");
    }

    // =========================================================================
    // Likes
    // =========================================================================

    /**
     * Fetches the like status for the current user and updates {@link #likeCount}.
     * Called once on open so the heart icon reflects the user's existing like.
     */
    private void loadLikeStatus() {
        Log.d(TAG, "Fetching like status for post " + postId + " user " + currentUserId);
        ApiClient.get(this, "/posts/" + postId + "/likes/status?userId=" + currentUserId,
                new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            isLiked = response.getBoolean("liked");
                            int count = response.getInt("count");
                            likes = count;
                            likeButton.setLiked(isLiked);
                            likeCount.setText(String.valueOf(count));
                            Log.d(TAG, "Like status loaded: liked=" + isLiked + " count=" + count);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing like status: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Failed to load like status: " + message);
                        // Keep the count from the intent extra; leave the empty heart
                    }
                });
    }

    /**
     * Toggles the like on the current post and updates the heart icon + count.
     * Calls {@code POST /posts/{postId}/likes?userId={currentUserId}}; the response
     * contains {@code {"liked":bool,"count":N}}.
     */
    private void toggleLike() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Log in to like posts", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Toggling like on post " + postId);
        ApiClient.post(this, "/posts/" + postId + "/likes?userId=" + currentUserId,
                null, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            isLiked = response.getBoolean("liked");
                            int count = response.getInt("count");
                            likes = count;
                            likeButton.setLiked(isLiked);
                            likeCount.setText(String.valueOf(count));
                            Log.d(TAG, "Like toggled: liked=" + isLiked + " count=" + count);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing like response: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Like toggle failed: " + message);
                        // Revert the button animation — isLiked was not updated on failure
                        likeButton.setLiked(isLiked);
                        Toast.makeText(PostViewActivity.this, "Could not update like",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =========================================================================
    // Comments
    // =========================================================================

    private void loadComments() {
        Log.d(TAG, "Loading comments for post " + postId);

        ApiClient.getArray(this, "/posts/" + postId + "/comments", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    comments.clear();
                    Gson gson = new Gson();
                    for (int i = 0; i < response.length(); i++) {
                        comments.add(gson.fromJson(response.getJSONObject(i).toString(),
                                DTOmodels.FeedComment.class));
                    }
                    commentAdapter.notifyDataSetChanged();
                    commentsHeader.setText("Comments (" + comments.size() + ")");
                    commentCount.setText("💬 " + comments.size());
                    Log.d(TAG, "Loaded " + comments.size() + " comments");
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing comments: " + e.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to load comments: " + message);
                Toast.makeText(PostViewActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitComment(String body) {
        submitCommentBtn.setEnabled(false);
        String endpoint = "/posts/" + postId + "/comments?authorId=" + currentUserId;
        Log.d(TAG, "Submitting comment: " + body);

        ApiClient.post(this, endpoint, new DTOmodels.CommentRequest(body), new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                submitCommentBtn.setEnabled(true);
                commentInput.setText("");
                Log.d(TAG, "Comment posted successfully");
                Toast.makeText(PostViewActivity.this, "Comment posted!", Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onError(String message) {
                submitCommentBtn.setEnabled(true);
                Log.e(TAG, "Comment failed: " + message);
                Toast.makeText(PostViewActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================================
    // Edit / delete dialogs
    // =========================================================================

    /**
     * Shows a two-step dialog for editing the review: first rating, then body text.
     * Only shown to the post's author (enforced by the menu setup in {@link #displayPost}).
     */
    private void showEditReviewDialog() {
        if (reviewId == -1) {
            Toast.makeText(this, "Cannot edit — review not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText ratingEdit = new EditText(this);
        ratingEdit.setHint("Rating (0-100)");
        ratingEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        ratingEdit.setText(String.valueOf(rating));

        new AlertDialog.Builder(this)
                .setTitle("Edit rating")
                .setView(ratingEdit)
                .setPositiveButton("Next", (dialog, which) -> {
                    String newRatingStr = ratingEdit.getText().toString().trim();
                    if (newRatingStr.isEmpty()) {
                        Toast.makeText(this, "Please enter a rating", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int newRating = Integer.parseInt(newRatingStr);
                    if (newRating < 0 || newRating > 100) {
                        Toast.makeText(this, "Rating must be between 0 and 100", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    EditText bodyEdit = new EditText(this);
                    bodyEdit.setHint("Review body");
                    bodyEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                            | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    bodyEdit.setMinLines(3);
                    bodyEdit.setText(reviewBody.getText().toString());

                    new AlertDialog.Builder(this)
                            .setTitle("Edit review")
                            .setView(bodyEdit)
                            .setPositiveButton("Save", (d2, w2) ->
                                    updateReview(newRating, bodyEdit.getText().toString().trim()))
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateReview(int newRating, String newBody) {
        Log.d(TAG, "Updating review " + reviewId + " rating=" + newRating);

        DTOmodels.ReviewUpdateRequest request = new DTOmodels.ReviewUpdateRequest(
                reviewId, mediaTypeStr, mediaTitleStr, newRating, newBody);

        ApiClient.put(this, "/reviews/" + reviewId, request, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "Review updated successfully");
                rating = newRating;
                reviewBody.setText(newBody);
                reviewBody.setVisibility(View.VISIBLE);
                likeCount.setText(String.valueOf(likes));
                Toast.makeText(PostViewActivity.this, "Review updated!", Toast.LENGTH_SHORT).show();

                // Reload the media card to reflect the new rating
                mediaCardContainer.removeAllViews();
                mediaCardContainer.setVisibility(View.GONE);
                loadMediaDetails();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Review update failed: " + message);
                Toast.makeText(PostViewActivity.this, "Failed to update review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditCaptionDialog() {
        EditText input = new EditText(this);
        input.setText(captionStr);

        new AlertDialog.Builder(this)
                .setTitle("Edit caption")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newCaption = input.getText().toString().trim();
                    Log.d(TAG, "Editing caption to: " + newCaption);
                    updateCaption(newCaption);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCaption(String newCaption) {
        String endpoint = "/posts/" + postId + "?requesterId=" + currentUserId;

        ApiClient.put(this, endpoint, new DTOmodels.PostUpdateRequest(newCaption), new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                captionStr = newCaption;
                if (newCaption != null && !newCaption.isEmpty()) {
                    caption.setText(newCaption);
                    caption.setVisibility(View.VISIBLE);
                    captionDivider.setVisibility(View.VISIBLE);
                } else {
                    caption.setVisibility(View.GONE);
                    captionDivider.setVisibility(View.GONE);
                }
                Log.d(TAG, "Caption updated successfully");
                Toast.makeText(PostViewActivity.this, "Caption updated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Caption update failed: " + message);
                Toast.makeText(PostViewActivity.this, "Failed to update caption", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePost() {
        new AlertDialog.Builder(this)
                .setTitle("Delete post")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String endpoint = "/posts/" + postId + "?requesterId=" + currentUserId;
                    Log.d(TAG, "Deleting post " + postId);

                    ApiClient.deleteString(this, endpoint, new Api_String_Interface() {
                        @Override
                        public void onSuccess(String response) {
                            Log.d(TAG, "Post deleted successfully");
                            Toast.makeText(PostViewActivity.this, "Post deleted!", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "Post delete failed: " + message);
                            Toast.makeText(PostViewActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCommentDialog(DTOmodels.FeedComment comment) {
        EditText input = new EditText(this);
        input.setText(comment.body);

        new AlertDialog.Builder(this)
                .setTitle("Edit comment")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newBody = input.getText().toString().trim();
                    if (newBody.isEmpty()) {
                        Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d(TAG, "Editing comment " + comment.id);
                    editComment(comment.id, newBody);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editComment(int commentId, String newBody) {
        String endpoint = "/comments/" + commentId + "?authorId=" + currentUserId;

        ApiClient.put(this, endpoint, new DTOmodels.CommentRequest(newBody), new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "Comment " + commentId + " updated");
                Toast.makeText(PostViewActivity.this, "Comment updated!", Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Comment edit failed: " + message);
                Toast.makeText(PostViewActivity.this, "Failed to edit comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteComment(DTOmodels.FeedComment comment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete comment")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String endpoint = "/comments/" + comment.id + "?requesterId=" + currentUserId;
                    Log.d(TAG, "Deleting comment " + comment.id);

                    ApiClient.deleteString(this, endpoint, new Api_String_Interface() {
                        @Override
                        public void onSuccess(String response) {
                            Log.d(TAG, "Comment " + comment.id + " deleted");
                            Toast.makeText(PostViewActivity.this, "Comment deleted!", Toast.LENGTH_SHORT).show();
                            loadComments();
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "Comment delete failed: " + message);
                            Toast.makeText(PostViewActivity.this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
