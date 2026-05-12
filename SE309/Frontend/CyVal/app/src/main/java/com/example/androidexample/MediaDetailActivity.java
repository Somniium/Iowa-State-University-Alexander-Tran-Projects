package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MediaDetailActivity — full-screen detail view for a single media item.
 *
 * <p>Receives {@code mediaType}, {@code mediaId}, and {@code title} via Intent extras.
 * Loads the appropriate backend endpoint based on {@code mediaType} and populates:
 * <ul>
 *   <li>Cover image, title, subtitle (artist/author/director), and extra info (genre/date)</li>
 *   <li>Media-type badge and aggregate rating badge</li>
 *   <li>Optional description (games, movies, books)</li>
 *   <li>A reviews section using the feed post card layout (albums only — other types
 *       do not yet expose per-review lists from the backend)</li>
 *   <li>For artists: an album grid using the feed media card layout</li>
 * </ul>
 *
 * <p>When {@code mediaId == 0} (item exists externally but not in the database),
 * {@link #showNotInDatabase()} is called instead of making a network request.
 *
 * <p>Media type colours are provided by {@link RatingUtility#getMediaTypeColor(String)}
 * so they stay consistent with the feed and post detail screens.
 */
public class MediaDetailActivity extends AppCompatActivity {

    private static final String TAG = "MediaDetail";

    // =========================================================================
    // Views
    // =========================================================================

    private ImageView backBtn, coverImg;
    private TextView headerText, titleText, subtitleText, extraText;
    private TextView typeBadge, ratingBadge;
    private TextView descriptionLabel, descriptionText;
    private TextView reviewsLabel, noReviewsText;
    private RecyclerView reviewsRecycler;
    private TextView albumsLabel;
    private LinearLayout albumsContainer;   // direct-child cards; parent ScrollView scrolls

    // =========================================================================
    // Intent data
    // =========================================================================

    private String mediaType;
    private long mediaId;
    private String title;
    private int currentUserId;

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_detail);

        backBtn          = findViewById(R.id.media_detail_back);
        headerText       = findViewById(R.id.media_detail_header);
        coverImg         = findViewById(R.id.media_detail_cover);
        titleText        = findViewById(R.id.media_detail_title);
        subtitleText     = findViewById(R.id.media_detail_subtitle);
        extraText        = findViewById(R.id.media_detail_extra);
        typeBadge        = findViewById(R.id.media_detail_type_badge);
        ratingBadge      = findViewById(R.id.media_detail_rating_badge);
        descriptionLabel = findViewById(R.id.media_detail_description_label);
        descriptionText  = findViewById(R.id.media_detail_description);
        reviewsLabel     = findViewById(R.id.media_detail_reviews_label);
        noReviewsText    = findViewById(R.id.media_detail_no_reviews);
        reviewsRecycler  = findViewById(R.id.media_detail_reviews_recycler);
        albumsLabel      = findViewById(R.id.media_detail_albums_label);
        albumsContainer  = findViewById(R.id.media_detail_albums_container);

        mediaType     = getIntent().getStringExtra("mediaType");
        mediaId       = getIntent().getLongExtra("mediaId", -1);
        title         = getIntent().getStringExtra("title");
        currentUserId = getIntent().getIntExtra("USER_ID", -1);

        headerText.setText(title != null ? title : "Media");
        backBtn.setOnClickListener(v -> finish());

        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecycler.setNestedScrollingEnabled(false);

        if (mediaType == null || mediaId == -1) {
            showError("Error: missing media info");
            return;
        }

        // mediaId == 0 means the item came from an external API but is not in the DB yet
        if (mediaId == 0) {
            showNotInDatabase();
            return;
        }

        loadMediaDetail();
    }

    // =========================================================================
    // Display
    // =========================================================================

    /** Routes to the correct loader based on media type. */
    private void loadMediaDetail() {
        switch (mediaType) {
            case "ALBUM":
            case "MUSIC":  // legacy value stored before the ALBUM rename
                loadAlbum();
                break;
            case "BOOK":
                loadBook();
                break;
            case "GAME":
                loadGame();
                break;
            case "MOVIE":
            case "SHOW":   // shows share the movie table and endpoint
                loadMovie();
                break;
            case "ARTIST":
                loadArtist();
                break;
            default:
                showError("Unknown media type: " + mediaType);
        }
    }

    /**
     * Populates the common header section (cover, title, badges, description).
     * Called by every load method once the API response has been parsed.
     *
     * @param title       primary display name
     * @param subtitle    secondary line (artist, author, director, etc.)
     * @param extra       tertiary line (genre, release date, etc.) — hidden if empty
     * @param description long-form description — section hidden if empty
     * @param rating      aggregate rating (0 = unrated)
     * @param imageUrl    cover/poster URL (null/empty → no image loaded)
     * @param type        mediaType string used to colour the type badge
     */
    private void displayMedia(String title, String subtitle, String extra, String description,
                              int rating, String imageUrl, String type) {
        titleText.setText(title);
        subtitleText.setText(subtitle);

        if (extra != null && !extra.isEmpty()) {
            extraText.setText(extra);
            extraText.setVisibility(View.VISIBLE);
        } else {
            extraText.setVisibility(View.GONE);
        }

        // Type badge — colour from shared RatingUtility so it matches the feed
        typeBadge.setText(type);
        int typeColor = RatingUtility.getMediaTypeColor(type);
        typeBadge.getBackground().setColorFilter(typeColor, android.graphics.PorterDuff.Mode.SRC_IN);

        // Rating badge
        int ratingColor = RatingUtility.getRatingColor(rating);
        ratingBadge.setText(rating > 0 ? String.valueOf(rating) : "-");
        ratingBadge.getBackground().setColorFilter(ratingColor, android.graphics.PorterDuff.Mode.SRC_IN);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            ApiClient.loadImage(this, imageUrl, coverImg);
        }

        if (description != null && !description.isEmpty()) {
            descriptionText.setText(description);
            descriptionLabel.setVisibility(View.VISIBLE);
            descriptionText.setVisibility(View.VISIBLE);
        } else {
            descriptionLabel.setVisibility(View.GONE);
            descriptionText.setVisibility(View.GONE);
        }
    }

    /** Shows a generic error state and logs the message. */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        titleText.setText("Not available");
        subtitleText.setText("Something went wrong");
        reviewsLabel.setVisibility(View.GONE);
        noReviewsText.setVisibility(View.GONE);
        Log.e(TAG, message);
    }

    /**
     * Shows a placeholder state when the item is not yet in the database.
     * This happens when the user taps a media card that was added from an external
     * API result (RAWG, TMDB, Hardcover) but not yet saved via a review.
     */
    private void showNotInDatabase() {
        titleText.setText(title != null ? title : "Unknown");
        subtitleText.setText("Not in database");

        if (mediaType != null) {
            typeBadge.setText(mediaType);
            typeBadge.getBackground().setColorFilter(
                    RatingUtility.getMediaTypeColor(mediaType), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        ratingBadge.setText("-");
        ratingBadge.getBackground().setColorFilter(
                android.graphics.Color.parseColor("#848482"), android.graphics.PorterDuff.Mode.SRC_IN);

        descriptionLabel.setVisibility(View.VISIBLE);
        descriptionLabel.setText("Not in database");
        descriptionText.setVisibility(View.VISIBLE);
        descriptionText.setText("This item hasn't been added to the database yet. " + "Search for it in the Post tab and create a review to add it.");

        reviewsLabel.setVisibility(View.GONE);
        noReviewsText.setVisibility(View.GONE);

        Toast.makeText(this, "This item is not in the database yet", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Media not in database: " + mediaType + " id=" + mediaId);
    }

    // =========================================================================
    // Load methods
    // =========================================================================

    private void loadAlbum() {
        ApiClient.get(this, "/album/" + mediaId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.album album = new Gson().fromJson(response.toString(), DTOmodels.album.class);
                    displayMedia(album.name, album.genre, album.releaseDate, null,
                            album.rating, album.coverURL, "ALBUM");

                    // Always use the 2-step loader so we get real postIds (needed for comments).
                    loadReviewsForMedia("ALBUM", mediaId);
                    Log.d(TAG, "Album loaded: " + album.name);
                } catch (Exception e) {
                    Log.e(TAG, "Album parse error: " + e.getMessage());
                    showNotInDatabase();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Album load failed: " + message);
                showNotInDatabase();
            }
        });
    }

    private void loadBook() {
        ApiClient.get(this, "/books/" + mediaId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.book book = new Gson().fromJson(response.toString(), DTOmodels.book.class);
                    // Normalise empty thumbnail to null so the image loader skips it
                    String thumbnail = (book.thumbnailurl != null && !book.thumbnailurl.isEmpty())
                            ? book.thumbnailurl : null;
                    displayMedia(book.title, book.authors, book.publisheddate, book.description,
                            0, thumbnail, "BOOK");
                    loadReviewsForMedia("BOOK", mediaId);
                    Log.d(TAG, "Book loaded: " + book.title);
                } catch (Exception e) {
                    Log.e(TAG, "Book parse error: " + e.getMessage());
                    showNotInDatabase();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Book load failed: " + message);
                showNotInDatabase();
            }
        });
    }

    private void loadGame() {
        ApiClient.get(this, "/games/" + mediaId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.game game = new Gson().fromJson(response.toString(), DTOmodels.game.class);
                    displayMedia(game.title, game.developer, game.releaseDate, game.description,
                            game.rating, game.coverUrl, "GAME");
                    loadReviewsForMedia("GAME", mediaId);
                    Log.d(TAG, "Game loaded: " + game.title);
                } catch (Exception e) {
                    Log.e(TAG, "Game parse error: " + e.getMessage());
                    showNotInDatabase();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Game load failed: " + message);
                showNotInDatabase();
            }
        });
    }

    private void loadMovie() {
        ApiClient.get(this, "/movies/" + mediaId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.movie movie = new Gson().fromJson(response.toString(), DTOmodels.movie.class);
                    // Use the actual mediaType from the intent (MOVIE or SHOW) for the badge
                    displayMedia(movie.title, movie.director, movie.releaseDate, movie.overview,
                            movie.rating, movie.posterUrl, mediaType);
                    // mediaType is "MOVIE" or "SHOW"; both link reviews via the movie FK
                    loadReviewsForMedia(mediaType, mediaId);
                    Log.d(TAG, "Movie/show loaded: " + movie.title);
                } catch (Exception e) {
                    Log.e(TAG, "Movie parse error: " + e.getMessage());
                    showNotInDatabase();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Movie load failed: " + message);
                showNotInDatabase();
            }
        });
    }

    /**
     * Loads the artist detail screen in two sequential steps:
     *
     * <ol>
     *   <li>Fetch {@code GET /artist/get-artist/{mediaId}} and display the artist
     *       header (name, photo, genre, rating) immediately so the screen isn't blank.</li>
     *   <li>Fire {@code POST /add-artist-albums/id/{mediaId}} to ingest the full
     *       discography from Spotify into the database, then re-fetch the artist via
     *       {@code GET /artist/get-artist/{mediaId}} to get the saved albums and render
     *       them as scrollable cards.</li>
     * </ol>
     */
    private void loadArtist() {
        ApiClient.get(this, "/artist/get-artist/" + mediaId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.artist artist = new Gson().fromJson(response.toString(), DTOmodels.artist.class);
                    // Step 1 — show header immediately
                    displayMedia(artist.name, artist.genre, "", null,
                            artist.rating, artist.photoURL, "ARTIST");
                    reviewsLabel.setVisibility(View.GONE);
                    noReviewsText.setVisibility(View.GONE);
                    albumsLabel.setVisibility(View.VISIBLE);
                    albumsLabel.setText("Albums (loading…)");
                    Log.d(TAG, "Artist header displayed: " + artist.name);

                    // Step 2 — ingest from Spotify, then fetch and render albums
                    ingestThenShowAlbums();
                } catch (Exception e) {
                    Log.e(TAG, "Artist parse error: " + e.getMessage());
                    showNotInDatabase();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Artist load failed: " + message);
                showNotInDatabase();
            }
        });
    }

    /**
     * Fires {@code POST /add-artist-albums/id/{mediaId}} to save the artist's
     * discography from Spotify, then calls {@link #fetchAndShowAlbums()} to render
     * the result. If ingestion fails the fetch runs anyway so any albums already in
     * the database are still shown.
     */
    private void ingestThenShowAlbums() {
        Log.d(TAG, "Ingesting artist albums from Spotify: /add-artist-albums/id/" + mediaId);
        ApiClient.post(this, "/add-artist-albums/id/" + mediaId, null, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "Ingest complete — fetching updated artist");
                fetchAndShowAlbums();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Ingest failed (showing existing albums): " + message);
                fetchAndShowAlbums(); // show whatever is already in the DB
            }
        });
    }

    /**
     * Re-fetches the artist via {@code GET /artist/get-artist/{mediaId}} and hands
     * the response JSON object to {@link #loadArtistAlbums(JSONObject)} to render
     * album cards. Updates the label to "Albums (0)" if the fetch fails.
     */
    private void fetchAndShowAlbums() {
        ApiClient.get(this, "/artist/get-artist/" + mediaId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                loadArtistAlbums(response);
                Log.d(TAG, "Albums refreshed after ingest");
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Album fetch failed: " + message);
                albumsLabel.setText("Albums (0)");
            }
        });
    }

    // =========================================================================
    // Artist albums — cards inflated directly into a LinearLayout
    // =========================================================================

    /**
     * Inflates an {@code item_feed_post} card for each album in the artist JSON and
     * adds them as direct children of {@link #albumsContainer} (a LinearLayout).
     *
     * <p>Using a LinearLayout instead of a RecyclerView eliminates nested-scrolling
     * conflicts: every card is always in the layout tree and the parent
     * {@code ScrollView} handles all scrolling, so every album is reachable.
     *
     * <p>Tapping a card opens {@link MediaDetailActivity} for that album.
     */
    private void loadArtistAlbums(JSONObject artistObj) {
        try {
            JSONArray albumsArray = artistObj.optJSONArray("albums");
            if (albumsArray == null || albumsArray.length() == 0) {
                albumsLabel.setText("Albums (0)");
                return;
            }

            albumsLabel.setText("Albums (" + albumsArray.length() + ")");
            albumsContainer.setVisibility(View.VISIBLE);
            albumsContainer.removeAllViews(); // clear any cards from a previous call

            Gson gson = new Gson();
            for (int i = 0; i < albumsArray.length(); i++) {
                DTOmodels.album album = gson.fromJson(
                        albumsArray.getJSONObject(i).toString(), DTOmodels.album.class);

                View card = LayoutInflater.from(this)
                        .inflate(R.layout.item_feed_post, albumsContainer, false);

                TextView titleView    = card.findViewById(R.id.feed_title);
                TextView subtitleView = card.findViewById(R.id.feed_subtitle);
                TextView typeView     = card.findViewById(R.id.feed_media_type);
                TextView extraView    = card.findViewById(R.id.feed_extra_info);
                TextView badgeView    = card.findViewById(R.id.feed_rating_badge);
                ImageView coverView   = card.findViewById(R.id.feed_cover_img);
                CardView cardRoot     = card.findViewById(R.id.card_root);

                titleView.setText(album.name);
                subtitleView.setText(album.artist);
                typeView.setText("ALBUM");
                extraView.setText(album.genre != null ? album.genre : "");

                int color = RatingUtility.getRatingColor(album.rating);
                cardRoot.setCardBackgroundColor(color);
                badgeView.setText(album.rating > 0 ? String.valueOf(album.rating) : "-");
                badgeView.getBackground().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

                ApiClient.loadImage(this, album.coverURL, coverView);

                final long albumId   = album.albumId;
                final String albumName = album.name;
                card.setOnClickListener(v -> {
                    Intent intent = new Intent(this, MediaDetailActivity.class);
                    intent.putExtra("mediaType", "ALBUM");
                    intent.putExtra("mediaId",   albumId);
                    intent.putExtra("title",     albumName);
                    intent.putExtra("USER_ID",   currentUserId);
                    startActivity(intent);
                });

                albumsContainer.addView(card);
            }

            Log.d(TAG, "Rendered " + albumsArray.length() + " album cards for artist");
        } catch (Exception e) {
            Log.e(TAG, "Error loading artist albums: " + e.getMessage());
            albumsLabel.setText("Albums (0)");
        }
    }

    // =========================================================================
    // Reviews (reuses feed post cards)
    // =========================================================================

    /**
     * Two-step loader that shows reviews for the current media item with full post
     * data (postId, authorName, likeCount, commentCount) so tapping a card opens
     * {@link PostViewActivity} with everything it needs — including comments.
     *
     * <p><b>Step 1</b> — {@code GET /reviews/type/{type}}: collect the IDs of reviews
     * whose FK (albumId / bookId / gameId / movieId) matches this media item.
     *
     * <p><b>Step 2</b> — {@code GET /feed/type/{type}}: keep only the published posts
     * whose {@code reviewId} is in the set from step 1. These posts carry the real
     * {@code postId}, {@code authorName}, {@code likeCount}, and {@code commentCount}.
     */
    private void loadReviewsForMedia(String type, long id) {
        ApiClient.getArray(this, "/reviews/type/" + type, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray reviewsJson) {
                try {
                    Gson gson = new Gson();
                    Set<Integer> matchingIds = new HashSet<>();
                    for (int i = 0; i < reviewsJson.length(); i++) {
                        DTOmodels.review r = gson.fromJson(
                                reviewsJson.getJSONObject(i).toString(), DTOmodels.review.class);
                        Long fkId;
                        switch (type) {
                            case "ALBUM": fkId = r.albumId; break;
                            case "BOOK":  fkId = r.bookId;  break;
                            case "GAME":  fkId = r.gameId;  break;
                            case "MOVIE":
                            case "SHOW":  fkId = r.movieId; break;
                            default:      fkId = null;
                        }
                        if (fkId != null && fkId == id) {
                            matchingIds.add(r.id);
                        }
                    }
                    if (matchingIds.isEmpty()) {
                        noReviewsText.setVisibility(View.VISIBLE);
                        return;
                    }
                    // Step 2 — fetch published posts and keep those wrapping a matched review
                    loadPostsForReviews(type, matchingIds);
                } catch (Exception e) {
                    Log.e(TAG, "Reviews parse error: " + e.getMessage());
                    noReviewsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Reviews load failed: " + message);
                noReviewsText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Second step of the review loader. Fetches {@code GET /feed/type/{type}} and
     * keeps posts whose {@code reviewId} is in {@code reviewIds}. Passes the
     * resulting list to {@link #showPostCards(List)}.
     */
    private void loadPostsForReviews(String type, Set<Integer> reviewIds) {
        ApiClient.getArray(this, "/feed/type/" + type, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray feedJson) {
                try {
                    Gson gson = new Gson();
                    List<DTOmodels.FeedItem> items = new ArrayList<>();
                    for (int i = 0; i < feedJson.length(); i++) {
                        DTOmodels.FeedPost post = gson.fromJson(
                                feedJson.getJSONObject(i).toString(), DTOmodels.FeedPost.class);
                        if (reviewIds.contains(post.reviewId)) {
                            items.add(DTOmodels.FeedItem.fromPost(post));
                        }
                    }
                    if (!items.isEmpty()) {
                        showPostCards(items);
                    } else {
                        // Reviews exist but haven't been published as posts yet
                        noReviewsText.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Feed parse error: " + e.getMessage());
                    noReviewsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Feed load failed: " + message);
                noReviewsText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Binds the reviews RecyclerView with full post data so tapping a card opens
     * {@link PostViewActivity} with a real {@code postId} (enabling comments),
     * the correct {@code authorName}, and accurate like / comment counts.
     */
    private void showPostCards(List<DTOmodels.FeedItem> items) {
        reviewsLabel.setText("Reviews (" + items.size() + ")");
        noReviewsText.setVisibility(View.GONE);

        FeedAdapter reviewAdapter = new FeedAdapter(this, items, new FeedAdapter.OnItemClickListener() {
            @Override
            public void onPostClick(DTOmodels.FeedPost post) {
                Intent intent = new Intent(MediaDetailActivity.this, PostViewActivity.class);
                intent.putExtra("postId",       post.id);          // real post ID — comments load
                intent.putExtra("reviewId",     post.reviewId);
                intent.putExtra("authorId",     post.authorId);
                intent.putExtra("authorName",   post.authorName);
                intent.putExtra("caption",      post.caption);
                intent.putExtra("mediaTitle",   post.mediaTitle);
                intent.putExtra("mediaType",    post.mediaType);
                intent.putExtra("rating",       post.rating);
                intent.putExtra("likeCount",    post.likeCount);
                intent.putExtra("commentCount", post.commentCount);
                intent.putExtra("publishedAt",  post.publishedAt);
                intent.putExtra("USER_ID",      currentUserId);
                startActivity(intent);
            }

            @Override
            public void onMediaClick(DTOmodels.FeedMedia media) {
                // No media cards inside the reviews section
            }
        });

        reviewsRecycler.setAdapter(reviewAdapter);
    }
}
