package com.example.androidexample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CreatePostDialogFragment — two-phase dialog for creating a new post.
 *
 * <p><b>Phase 1 – Search:</b> the user picks a media item from the local database
 * or searches external APIs (RAWG, TMDB, Hardcover). Seven chip buttons filter by
 * media type; a search bar narrows results by title.
 *
 * <p><b>Phase 2 – Review:</b> the user fills in a rating (0–100), an optional
 * review body, and a required caption, then taps Publish.
 *
 * <p><b>Publish flow (sequential steps):</b>
 * <ol>
 *   <li>If the selected item came from an external API (externalId ≠ null, id == 0),
 *       save it to the database first — {@link #saveMediaThenPublish}.</li>
 *   <li>{@link #createReview} — POST /reviews; reads the returned {@code id} field
 *       directly, falling back to {@link #findReviewByTitle} if absent.</li>
 *   <li>{@link #linkReviewToUser} — PUT /reviews/{id}/user/{userId}</li>
 *   <li>{@link #linkReviewToMedia} — PUT /reviews/{id}/{mediaType}/{mediaId}</li>
 *   <li>{@link #publishPost} — POST /reviews/{id}/publish</li>
 *   <li>{@link #updateMediaRating} — fire-and-forget rating recalculation</li>
 * </ol>
 *
 * <p><b>Thread safety:</b> Volley callbacks in this app run on a background thread.
 * All RecyclerView adapter updates and Toast calls are wrapped in
 * {@code requireActivity().runOnUiThread()}. {@link #pendingCalls} uses
 * {@link AtomicInteger} so it is safe to decrement from multiple callbacks.
 */
public class CreatePostDialogFragment extends DialogFragment {

    private static final String TAG = "CreatePost";

    // =========================================================================
    // Fields
    // =========================================================================

    /** Full set of media items loaded from the local database on dialog open. */
    private final List<DTOmodels.SearchResult> allMediaResults = new ArrayList<>();

    // Shared header controls
    private TextView headerText;
    private ImageView backBtn, closeBtn;

    // Phase containers (only one visible at a time)
    private View searchPhase, reviewPhase;

    // Search phase views
    private EditText searchInput;
    private RecyclerView resultsRecycler;
    private SearchResultAdapter resultAdapter;
    private List<DTOmodels.SearchResult> searchResults;

    // Review phase views
    private ImageView selectedCover;
    private TextView selectedTitle, selectedSubtitle;
    private EditText ratingInput, bodyInput, captionInput;
    private Button publishBtn;

    // Media-type filter chips (TextViews styled as pills in dialog_create_post.xml)
    private TextView chipAll, chipMusic, chipGames, chipMovies, chipShows, chipBooks;

    // State
    private String selectedChip = "ALL";  // active chip value
    private DTOmodels.SearchResult selectedMedia;
    private int currentUserId;

    /**
     * Tracks how many async calls are still outstanding.
     * Used in two separate contexts:
     * <ul>
     *   <li>{@link #loadAllMedia} — set to 5 (one per media type), decremented per callback</li>
     *   <li>{@link #searchMedia} — set to the number of chip-active external API calls</li>
     * </ul>
     */
    private final AtomicInteger pendingCalls = new AtomicInteger(0);

    // Callbacks wired by the host fragment/activity
    private OnPostPublishedListener publishListener;
    private OnDismissListener dismissListener;

    // =========================================================================
    // Listener interfaces
    // =========================================================================

    /** Notified when the review is published successfully. */
    public interface OnPostPublishedListener {
        void onPostPublished();
    }

    /** Notified when the dialog is dismissed (success or cancel). */
    public interface OnDismissListener {
        void onDismissed();
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    public void setOnPostPublishedListener(OnPostPublishedListener listener) {
        this.publishListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDismissed();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.85));
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_post, container, false);

        // Read USER_ID from the hosting activity's intent
        if (getActivity() != null && getActivity().getIntent() != null) {
            currentUserId = getActivity().getIntent().getIntExtra("USER_ID", -1);
        } else {
            currentUserId = -1;
        }

        // Shared header controls
        headerText = view.findViewById(R.id.create_post_header);
        backBtn    = view.findViewById(R.id.create_post_back);
        closeBtn   = view.findViewById(R.id.create_post_close);

        // Phase containers
        searchPhase = view.findViewById(R.id.search_phase);
        reviewPhase = view.findViewById(R.id.review_phase);

        // Search phase
        searchInput      = view.findViewById(R.id.search_input);
        resultsRecycler  = view.findViewById(R.id.search_results_recycler);

        // Review phase
        selectedCover    = view.findViewById(R.id.selected_media_cover);
        selectedTitle    = view.findViewById(R.id.selected_media_title);
        selectedSubtitle = view.findViewById(R.id.selected_media_subtitle);
        ratingInput      = view.findViewById(R.id.review_rating);
        bodyInput        = view.findViewById(R.id.review_body);
        captionInput     = view.findViewById(R.id.review_caption);
        publishBtn       = view.findViewById(R.id.btn_publish);

        // Media-type filter chips
        chipAll    = view.findViewById(R.id.chip_all);
        chipMusic  = view.findViewById(R.id.chip_music);
        chipGames  = view.findViewById(R.id.chip_games);
        chipMovies = view.findViewById(R.id.chip_movies);
        chipShows  = view.findViewById(R.id.chip_shows);
        chipBooks  = view.findViewById(R.id.chip_books);

        // Search results list — tapping a result advances to the review phase
        searchResults = new ArrayList<>();
        resultAdapter = new SearchResultAdapter(getContext(), searchResults, result -> {
            selectedMedia = result;
            showReviewPhase();
        });
        resultsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        resultsRecycler.setAdapter(resultAdapter);

        // Chip listeners — change selectedChip, clear the search bar, refresh the list
        View.OnClickListener chipListener = v -> {
            if      (v == chipAll)    selectedChip = "ALL";
            else if (v == chipMusic)  selectedChip = "ALBUM";
            else if (v == chipGames)  selectedChip = "GAME";
            else if (v == chipMovies) selectedChip = "MOVIE";
            else if (v == chipShows)  selectedChip = "SHOW";
            else if (v == chipBooks)  selectedChip = "BOOK";
            updateChipColors();
            searchInput.setText("");
            if (!allMediaResults.isEmpty()) {
                filterByChip();
            } else {
                loadAllMedia();
            }
        };
        chipAll.setOnClickListener(chipListener);
        chipMusic.setOnClickListener(chipListener);
        chipGames.setOnClickListener(chipListener);
        chipMovies.setOnClickListener(chipListener);
        chipShows.setOnClickListener(chipListener);
        chipBooks.setOnClickListener(chipListener);

        // Search bar — filter locally first, then fall back to external APIs
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchInput.getText().toString().trim();
                if (!query.isEmpty()) {
                    filterLocalResults(query);
                } else {
                    filterByChip(); // empty search → reset to chip view
                }
                return true;
            }
            return false;
        });

        backBtn.setOnClickListener(v -> {
            if (reviewPhase.getVisibility() == View.VISIBLE) {
                showSearchPhase();
            } else {
                dismiss();
            }
        });
        closeBtn.setOnClickListener(v -> dismiss());
        publishBtn.setOnClickListener(v -> publish());

        // Pre-load all local media so the list is ready when the dialog opens
        loadAllMedia();

        return view;
    }

    // =========================================================================
    // UI state
    // =========================================================================

    /** Switches to the search phase (first screen). */
    private void showSearchPhase() {
        searchPhase.setVisibility(View.VISIBLE);
        reviewPhase.setVisibility(View.GONE);
        headerText.setText("Select media");
        backBtn.setVisibility(View.GONE);
    }

    /** Switches to the review phase (second screen) and populates the selected-media header. */
    private void showReviewPhase() {
        searchPhase.setVisibility(View.GONE);
        reviewPhase.setVisibility(View.VISIBLE);
        headerText.setText("Write your review");
        backBtn.setVisibility(View.VISIBLE);

        selectedTitle.setText(selectedMedia.title);
        selectedSubtitle.setText(selectedMedia.subtitle + " · " + selectedMedia.mediaType);
        if (selectedMedia.imageUrl != null) {
            ApiClient.loadImage(getContext(), selectedMedia.imageUrl, selectedCover);
        }
    }

    /**
     * Updates each chip's background tint and text colour to reflect {@link #selectedChip}.
     * Active chips show the media-type brand colour with white text; inactive chips are grey.
     * The Artist chip uses a tinted inactive state to preserve its orange brand feel.
     */
    private void updateChipColors() {
        int inactive = android.graphics.Color.parseColor("#F0F0F0");
        applyChip(chipAll,    "ALL",   android.graphics.Color.parseColor("#E53935"), inactive);
        applyChip(chipMusic,  "ALBUM", android.graphics.Color.parseColor("#E53935"), inactive);
        applyChip(chipGames,  "GAME",  android.graphics.Color.parseColor("#8E24AA"), inactive);
        applyChip(chipMovies, "MOVIE", android.graphics.Color.parseColor("#1E88E5"), inactive);
        applyChip(chipShows,  "SHOW",  android.graphics.Color.parseColor("#1E88E5"), inactive);
        applyChip(chipBooks,  "BOOK",  android.graphics.Color.parseColor("#43A047"), inactive);
    }

    /** Applies active/inactive colour to a single chip. */
    private void applyChip(TextView chip, String chipId, int activeColor, int inactiveColor) {
        boolean active = selectedChip.equals(chipId);
        chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                active ? activeColor : inactiveColor));
        chip.setTextColor(active ? 0xFFFFFFFF : 0xFF000000);
    }

    // =========================================================================
    // Load all media (called on dialog open)
    // =========================================================================

    /**
     * Fetches all five media type lists from the backend in parallel.
     * Each callback decrements {@link #pendingCalls} and calls {@link #onAllMediaLoaded}
     * when the counter reaches zero.
     *
     * <p>Movies and shows share the {@code /movies} endpoint; the {@code mediaType}
     * field in the JSON response distinguishes them.
     */
    private void loadAllMedia() {
        searchResults.clear();
        allMediaResults.clear();
        resultAdapter.notifyDataSetChanged();
        pendingCalls.set(4); // albums, games, movies+shows, books

        // --- Albums ---
        ApiClient.getArray(getContext(), "/albums", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < response.length(); i++) {
                        DTOmodels.album album = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.album.class);
                        synchronized (allMediaResults) {
                            allMediaResults.add(new DTOmodels.SearchResult(
                                    album.albumId, album.name, album.genre, "ALBUM", album.coverURL));
                        }
                    }
                    Log.d(TAG, "Loaded " + response.length() + " albums");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading albums: " + e.getMessage());
                }
                onAllMediaLoaded();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Albums load failed: " + message);
                onAllMediaLoaded();
            }
        });

        // --- Games ---
        ApiClient.getArray(getContext(), "/games", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < response.length(); i++) {
                        DTOmodels.game game = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.game.class);
                        synchronized (allMediaResults) {
                            allMediaResults.add(new DTOmodels.SearchResult(
                                    game.id, game.title, game.developer, "GAME", game.coverUrl));
                        }
                    }
                    Log.d(TAG, "Loaded " + response.length() + " games");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading games: " + e.getMessage());
                }
                onAllMediaLoaded();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Games load failed: " + message);
                onAllMediaLoaded();
            }
        });

        // --- Movies & Shows (same /movies endpoint, differentiated by mediaType field) ---
        ApiClient.getArray(getContext(), "/movies", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.getJSONObject(i);
                        DTOmodels.movie movie = gson.fromJson(obj.toString(), DTOmodels.movie.class);
                        String type = obj.getString("mediaType"); // "MOVIE" or "SHOW"
                        synchronized (allMediaResults) {
                            if ("MOVIE".equals(type) || "SHOW".equals(type)) {
                                allMediaResults.add(new DTOmodels.SearchResult(
                                        movie.id, movie.title, movie.director, type, movie.posterUrl));
                            }
                        }
                    }
                    Log.d(TAG, "Loaded " + response.length() + " movies/shows");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading movies: " + e.getMessage());
                }
                onAllMediaLoaded();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Movies load failed: " + message);
                onAllMediaLoaded();
            }
        });

        // --- Books ---
        ApiClient.getArray(getContext(), "/books", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < response.length(); i++) {
                        DTOmodels.book book = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.book.class);
                        // Normalise empty thumbnail strings to null so image loader skips them
                        String thumbnail = (book.thumbnailurl != null && !book.thumbnailurl.isEmpty())
                                ? book.thumbnailurl : null;
                        synchronized (allMediaResults) {
                            allMediaResults.add(new DTOmodels.SearchResult(
                                    book.id, book.title, book.authors, "BOOK", thumbnail));
                        }
                    }
                    Log.d(TAG, "Loaded " + response.length() + " books");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading books: " + e.getMessage());
                }
                onAllMediaLoaded();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Books load failed: " + message);
                onAllMediaLoaded();
            }
        });

    }

    /**
     * Called by every {@link #loadAllMedia} callback (success or error).
     * Displays the full list once all five fetches have returned.
     * {@link #filterByChip} is posted to the main thread since Volley callbacks
     * run on a background thread.
     */
    private void onAllMediaLoaded() {
        if (pendingCalls.decrementAndGet() > 0) return;

        Log.d(TAG, "All media loaded: " + allMediaResults.size() + " items total");
        if (!isAdded()) return;
        requireActivity().runOnUiThread(this::filterByChip);
    }

    // =========================================================================
    // Filtering
    // =========================================================================

    /**
     * Rebuilds {@link #searchResults} from {@link #allMediaResults} applying the
     * current chip selection. Must be called on the main thread (notifies adapter).
     */
    private void filterByChip() {
        searchResults.clear();
        if (selectedChip.equals("ALL")) {
            searchResults.addAll(allMediaResults);
        } else {
            for (DTOmodels.SearchResult item : allMediaResults) {
                if (selectedChip.equals(item.mediaType)) {
                    searchResults.add(item);
                }
            }
        }
        resultAdapter.notifyDataSetChanged();
        Log.d(TAG, "Showing " + searchResults.size() + " results for chip: " + selectedChip);
    }

    /**
     * Filters {@link #allMediaResults} by {@code query} (local, instant) and then
     * fires external API searches in parallel. External results are appended via
     * {@link #addSearchResult} as each response arrives.
     */
    private void filterLocalResults(String query) {
        searchResults.clear();
        String lower = query.toLowerCase();
        for (DTOmodels.SearchResult item : allMediaResults) {
            if (!selectedChip.equals("ALL") && !selectedChip.equals(item.mediaType)) continue;
            if (item.title != null && item.title.toLowerCase().contains(lower)) {
                searchResults.add(item);
            }
        }
        resultAdapter.notifyDataSetChanged();
        Log.d(TAG, "Local results: " + searchResults.size() + ", also searching APIs…");
        searchMedia(query);
    }

    // =========================================================================
    // API search (external fallback for media not yet in the database)
    // =========================================================================

    /**
     * Kicks off external API searches for the given query.
     * Only the chip-active media types are queried; {@link #pendingCalls} is set
     * to the number of calls about to be made so {@link #onSearchCallDone} knows
     * when all responses have arrived.
     */
    private void searchMedia(String query) {
        pendingCalls.set(0);

        if (selectedChip.equals("ALL") || selectedChip.equals("ALBUM")) {
            pendingCalls.incrementAndGet();
            searchAlbums(query);
        }
        if (selectedChip.equals("ALL") || selectedChip.equals("GAME")) {
            pendingCalls.incrementAndGet();
            searchGames(query);
        }
        if (selectedChip.equals("ALL") || selectedChip.equals("MOVIE")) {
            pendingCalls.incrementAndGet();
            searchMovies(query);
        }
        if (selectedChip.equals("ALL") || selectedChip.equals("SHOW")) {
            pendingCalls.incrementAndGet();
            searchShows(query);
        }
        if (selectedChip.equals("ALL") || selectedChip.equals("BOOK")) {
            pendingCalls.incrementAndGet();
            searchBooks(query);
        }
    }

    private void searchAlbums(String query) {
        ApiClient.get(getContext(), "/search-albums-and-save?album_name=" + query, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    DTOmodels.album album = new Gson().fromJson(response.toString(), DTOmodels.album.class);
                    addSearchResult(new DTOmodels.SearchResult(
                            album.albumId, album.name, album.genre, "ALBUM", album.coverURL));
                    Log.d(TAG, "Album found via API: " + album.name);
                } catch (Exception e) {
                    Log.e(TAG, "Album parse error: " + e.getMessage());
                }
                onSearchCallDone();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Album search failed: " + message);
                onSearchCallDone();
            }
        });
    }

    private void searchGames(String query) {
        ApiClient.getArray(getContext(), "/search-games?q=" + query, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < Math.min(response.length(), 5); i++) {
                        DTOmodels.game game = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.game.class);
                        addSearchResult(new DTOmodels.SearchResult(
                                game.id, game.title, game.developer, "GAME", game.coverUrl, game.rawgId));
                    }
                    Log.d(TAG, "Games found via API: " + response.length());
                } catch (Exception e) {
                    Log.e(TAG, "Game parse error: " + e.getMessage());
                }
                onSearchCallDone();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Game search failed: " + message);
                onSearchCallDone();
            }
        });
    }

    private void searchMovies(String query) {
        ApiClient.getArray(getContext(), "/search-movies?q=" + query, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < Math.min(response.length(), 5); i++) {
                        DTOmodels.movie movie = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.movie.class);
                        addSearchResult(new DTOmodels.SearchResult(
                                movie.id, movie.title, movie.director, "MOVIE", movie.posterUrl, movie.tmdbId));
                    }
                    Log.d(TAG, "Movies found via API: " + response.length());
                } catch (Exception e) {
                    Log.e(TAG, "Movie parse error: " + e.getMessage());
                }
                onSearchCallDone();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Movie search failed: " + message);
                onSearchCallDone();
            }
        });
    }

    private void searchShows(String query) {
        ApiClient.getArray(getContext(), "/search-shows?q=" + query, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < Math.min(response.length(), 5); i++) {
                        // Shows share the movie DTO; mediaType is forced to "SHOW"
                        DTOmodels.movie movie = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.movie.class);
                        addSearchResult(new DTOmodels.SearchResult(
                                movie.id, movie.title, movie.director, "SHOW", movie.posterUrl, movie.tmdbId));
                    }
                    Log.d(TAG, "Shows found via API: " + response.length());
                } catch (Exception e) {
                    Log.e(TAG, "Show parse error: " + e.getMessage());
                }
                onSearchCallDone();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Shows search failed: " + message);
                onSearchCallDone();
            }
        });
    }

    private void searchBooks(String query) {
        ApiClient.getArray(getContext(), "/search-books?q=" + query, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    Gson gson = new Gson();
                    for (int i = 0; i < Math.min(response.length(), 5); i++) {
                        DTOmodels.book book = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.book.class);
                        String thumbnail = (book.thumbnailurl != null && !book.thumbnailurl.isEmpty())
                                ? book.thumbnailurl : null;
                        addSearchResult(new DTOmodels.SearchResult(
                                book.id, book.title, book.authors, "BOOK", thumbnail, book.volumeid));
                    }
                    Log.d(TAG, "Books found via API: " + response.length());
                } catch (Exception e) {
                    Log.e(TAG, "Book parse error: " + e.getMessage());
                }
                onSearchCallDone();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Book search failed: " + message);
                onSearchCallDone();
            }
        });
    }

    /**
     * Called by every {@link #searchMedia} callback (success or error).
     * Once all external searches have returned, shows a "no results" toast if
     * the list is still empty. Toast is posted to the main thread.
     */
    private void onSearchCallDone() {
        if (pendingCalls.decrementAndGet() > 0) return;
        if (searchResults.isEmpty() && isAdded()) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Adds a search result to the visible list, skipping duplicates.
     * Deduplication checks both title+type and externalId.
     * Adapter notification is posted to the main thread since this is called
     * from Volley background callbacks.
     */
    private void addSearchResult(DTOmodels.SearchResult result) {
        synchronized (searchResults) {
            for (DTOmodels.SearchResult existing : searchResults) {
                // Same title and type — already shown
                if (existing.title.equals(result.title) && existing.mediaType.equals(result.mediaType)) {
                    return;
                }
                // Same external API id — same item under a different local id
                if (existing.externalId != null && result.externalId != null
                        && existing.externalId.equals(result.externalId)) {
                    return;
                }
            }
            searchResults.add(result);
        }
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> resultAdapter.notifyDataSetChanged());
        }
    }

    // =========================================================================
    // Publish flow
    // =========================================================================

    /**
     * Validates user inputs and starts the publish chain.
     * If the selected media item came from an external API (externalId set, no db id yet),
     * saves it to the database first via {@link #saveMediaThenPublish}.
     */
    private void publish() {
        String ratingStr = ratingInput.getText().toString().trim();
        String body      = bodyInput.getText().toString().trim();
        String caption   = captionInput.getText().toString().trim();

        if (ratingStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        int rating;
        try {
            rating = Integer.parseInt(ratingStr);
            if (rating < 0 || rating > 100) {
                Toast.makeText(getContext(), "Rating must be 0-100", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid rating", Toast.LENGTH_SHORT).show();
            return;
        }
        if (caption.isEmpty()) {
            Toast.makeText(getContext(), "Caption is required", Toast.LENGTH_SHORT).show();
            return;
        }

        publishBtn.setEnabled(false);
        publishBtn.setText("Publishing…");

        // Use the media type directly — no switch needed
        String mediaType = selectedMedia.mediaType;

        // External-only items (from RAWG/TMDB/Hardcover) have no DB id yet
        if (selectedMedia.externalId != null && !selectedMedia.externalId.isEmpty()
                && selectedMedia.id == 0) {
            saveMediaThenPublish(rating, body, caption, mediaType);
            return;
        }

        createReview(rating, body, caption, mediaType);
    }

    /**
     * Step 1: POST /reviews — creates the review record.
     * Uses the {@code id} field returned in the response body directly; falls back to
     * {@link #findReviewByTitle} when the response has no id (handles older backend versions).
     */
    private void createReview(int rating, String body, String caption, String mediaType) {
        Log.d(TAG, "Creating review for " + selectedMedia.title
                + " rating=" + rating + " dbId=" + selectedMedia.id);

        DTOmodels.review reviewRequest = new DTOmodels.review(mediaType, selectedMedia.title, rating, body);

        ApiClient.post(getContext(), "/reviews", reviewRequest, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                // Prefer the id returned by the POST response
                int reviewId = response.optInt("id", -1);
                if (reviewId != -1) {
                    Log.d(TAG, "Review created with id " + reviewId);
                    linkReviewToUser(reviewId, caption);
                } else {
                    // Fallback: scan all reviews and match by title (slower but safe)
                    Log.d(TAG, "POST /reviews returned no id — scanning all reviews for: " + selectedMedia.title);
                    findReviewByTitle(selectedMedia.title, caption);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Review creation failed: " + message);
                publishFailed();
            }
        });
    }

    /**
     * Fallback for {@link #createReview}: scans GET /reviews and finds the most
     * recently created review whose title matches. Scans in reverse so the newest
     * entry wins when duplicate titles exist.
     */
    private void findReviewByTitle(String title, String caption) {
        ApiClient.getArray(getContext(), "/reviews", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    int reviewId = -1;
                    for (int i = response.length() - 1; i >= 0; i--) {
                        JSONObject obj = response.getJSONObject(i);
                        if (title.equals(obj.getString("title"))) {
                            reviewId = obj.getInt("id");
                            break;
                        }
                    }
                    if (reviewId == -1) {
                        Log.e(TAG, "Could not find created review for title: " + title);
                        publishFailed();
                        return;
                    }
                    Log.d(TAG, "Found review id: " + reviewId);
                    linkReviewToUser(reviewId, caption);
                } catch (Exception e) {
                    Log.e(TAG, "Error finding review: " + e.getMessage());
                    publishFailed();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to fetch reviews: " + message);
                publishFailed();
            }
        });
    }

    /**
     * Step 2: PUT /reviews/{id}/user/{userId} — links the review to the current user.
     * On failure, proceeds to {@link #publishPost} anyway so the post is not permanently lost.
     */
    private void linkReviewToUser(int reviewId, String caption) {
        ApiClient.putString(getContext(),
                "/reviews/" + reviewId + "/user/" + currentUserId + "?requesterId=" + currentUserId, "",
                new Api_String_Interface() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "Review linked to user");
                        linkReviewToMedia(reviewId, caption);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "User link failed (continuing): " + message);
                        publishPost(reviewId, caption); // still attempt to publish
                    }
                });
    }

    /**
     * Step 3: PUT /reviews/{id}/{mediaType}/{mediaId} — links the review to the media item.
     * On failure, proceeds to {@link #publishPost} so the post is not permanently lost.
     *
     * <p>Note: shows use the {@code /movie/} endpoint because they share the movie table.
     */
    private void linkReviewToMedia(int reviewId, String caption) {
        String endpoint;
        String req = "?requesterId=" + currentUserId;
        switch (selectedMedia.mediaType) {
            case "ALBUM": endpoint = "/reviews/" + reviewId + "/album/" + selectedMedia.id + req; break;
            case "GAME":  endpoint = "/reviews/" + reviewId + "/game/"  + selectedMedia.id + req; break;
            case "MOVIE":
            case "SHOW":  endpoint = "/reviews/" + reviewId + "/movie/" + selectedMedia.id + req; break;
            case "BOOK":  endpoint = "/reviews/" + reviewId + "/book/"  + selectedMedia.id + req; break;
            default:
                Log.w(TAG, "No media link endpoint for type: " + selectedMedia.mediaType);
                publishPost(reviewId, caption);
                return;
        }

        Log.d(TAG, "Linking review to media: " + endpoint);

        ApiClient.putString(getContext(), endpoint, "", new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Review linked to media");
                publishPost(reviewId, caption);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Media link failed (continuing): " + message);
                publishPost(reviewId, caption); // still attempt to publish
            }
        });
    }

    /**
     * Step 4: POST /reviews/{id}/publish — creates the public post.
     * On success, triggers a fire-and-forget rating update and dismisses the dialog.
     */
    private void publishPost(int reviewId, String caption) {
        String endpoint = "/reviews/" + reviewId + "/publish?authorId=" + currentUserId;
        DTOmodels.PostPublishRequest request = new DTOmodels.PostPublishRequest(caption, "PUBLIC");

        ApiClient.post(getContext(), endpoint, request, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.d(TAG, "Post published successfully");
                updateMediaRating(); // fire-and-forget
                if (isAdded() && getContext() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Post published!", Toast.LENGTH_SHORT).show());
                }
                if (publishListener != null) {
                    publishListener.onPostPublished();
                }
                dismiss();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Publish failed: " + message);
                publishFailed();
            }
        });
    }

    /** Re-enables the publish button and shows an error toast on the main thread. */
    private void publishFailed() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                publishBtn.setEnabled(true);
                publishBtn.setText("Publish");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to publish", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Pre-step: saves an external (RAWG/TMDB/Hardcover) media item to the database
     * before creating the review. Updates {@link DTOmodels.SearchResult#id} with the
     * new database id so the rest of the publish chain can link correctly.
     *
     * <p>Albums auto-save during search and should never reach this method.
     * The {@code externalId} format for movies/shows is {@code "TYPE-tmdbId"}
     * (e.g. {@code "MOVIE-12345"}).
     */
    private void saveMediaThenPublish(int rating, String body, String caption, String mediaType) {
        String saveEndpoint;
        switch (selectedMedia.mediaType) {
            case "GAME":
                saveEndpoint = "/games/save?rawgId=" + selectedMedia.externalId;
                break;
            case "MOVIE":
            case "SHOW": {
                // externalId is "TYPE-tmdbId" — split into its two parts
                if (selectedMedia.externalId == null || !selectedMedia.externalId.contains("-")) {
                    Log.e(TAG, "Invalid externalId for movie/show: " + selectedMedia.externalId);
                    publishFailed();
                    return;
                }
                int dash = selectedMedia.externalId.indexOf("-");
                String mediaTypeToken = selectedMedia.externalId.substring(0, dash);
                String tmdbIdToken    = selectedMedia.externalId.substring(dash + 1);
                saveEndpoint = "/movies/save?tmdbId=" + tmdbIdToken + "&type=" + mediaTypeToken;
                break;
            }
            case "BOOK":
                saveEndpoint = "/books/save?hardcoverId=" + selectedMedia.externalId;
                break;
            default:
                Log.e(TAG, "Unknown media type for external save: " + selectedMedia.mediaType);
                publishFailed();
                return;
        }

        Log.d(TAG, "Saving external media to database: " + saveEndpoint);

        ApiClient.post(getContext(), saveEndpoint, null, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    // Extract the database id — field name varies by media type
                    long savedId = response.optLong("id", 0);
                    if (savedId == 0) savedId = response.optLong("gameId", 0);
                    if (savedId == 0) savedId = response.optLong("movieId", 0);
                    if (savedId == 0) savedId = response.optLong("bookId", 0);

                    selectedMedia.id = savedId;
                    Log.d(TAG, "Media saved with db id: " + savedId);
                    createReview(rating, body, caption, mediaType);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing save response: " + e.getMessage());
                    publishFailed();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Save media failed: " + message);
                publishFailed();
            }
        });
    }

    /**
     * Step 5 (fire-and-forget): recalculates the media item's aggregate rating.
     * Errors are logged but do not affect the UI — the post has already been published.
     */
    private void updateMediaRating() {
        String endpoint;
        switch (selectedMedia.mediaType) {
            case "ALBUM": endpoint = "/album/update-average-rating/" + selectedMedia.id; break;
            case "GAME":  endpoint = "/games/"  + selectedMedia.id + "/update-rating";   break;
            case "MOVIE":
            case "SHOW":  endpoint = "/movies/" + selectedMedia.id + "/update-rating";   break;
            case "BOOK":  endpoint = "/books/"  + selectedMedia.id + "/update-rating";   break;
            default:
                Log.w(TAG, "No rating update endpoint for type: " + selectedMedia.mediaType);
                return;
        }

        Log.d(TAG, "Updating media rating: " + endpoint);

        ApiClient.putString(getContext(), endpoint, "", new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Media rating updated");
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Rating update failed (non-fatal): " + message);
            }
        });
    }
}
