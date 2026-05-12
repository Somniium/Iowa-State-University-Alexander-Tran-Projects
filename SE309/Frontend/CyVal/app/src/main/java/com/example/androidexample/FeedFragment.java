package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FeedFragment — the main scrollable feed shown on the home tab.
 *
 * Data flow:
 *   1. loadFeed() fires 6 parallel API calls (posts + 5 media types).
 *   2. Each callback decrements pendingCalls; when it hits 0, checkAndMerge()
 *      interleaves posts with media cards and stores the result in allItems.
 *   3. applyFilters() narrows allItems into displayedItems for the RecyclerView.
 *   4. If a search finds nothing locally, searchExternalApis() hits the backend's
 *      third-party API proxies and appends results.
 */
public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    private final List<DTOmodels.FeedItem> postItems  = new ArrayList<>();
    private final List<DTOmodels.FeedItem> mediaItems = new ArrayList<>();
    private final List<DTOmodels.FeedItem> allItems   = new ArrayList<>();
    private List<DTOmodels.FeedItem> displayedItems;

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------

    private RecyclerView recyclerView;
    private FeedAdapter adapter;
    private EditText searchBar;
    private SwipeRefreshLayout swipeRefresh;
    /** Hamburger button — tinted yellow when a non-default filter is active. */
    private ImageView menuBtn;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /**
     * Counts in-flight API calls. Decremented atomically from Volley threads;
     * checkAndMerge() merges data when it reaches zero.
     */
    private final AtomicInteger pendingCalls = new AtomicInteger(0);

    /** True once the first successful load completes. Prevents onResume from
     *  triggering a full 6-call reload every time the user navigates back. */
    private boolean feedLoaded = false;

    /** Current view-type filter: "ALL" | "POSTS" | "MEDIA" | "FOLLOWING". */
    private String currentViewType = "ALL";

    /** Current media-type filter: "ALL" | "ALBUM" | "BOOK" | "GAME" | "MOVIE" | "ARTIST". */
    private String currentFilter = "ALL";

    /** IDs of users the current user follows — used by the FOLLOWING filter at display time. */
    private final List<Integer> followedIDs = new ArrayList<>();

    /**
     * Post IDs the current user has liked during this session.
     * Shared with {@link FeedAdapter} by reference so the adapter always reflects the
     * latest state without needing a full re-bind. Reset on each full feed reload.
     */
    private final Set<Integer> likedPostIds = new HashSet<>();

    private int currentUserId;

    // -------------------------------------------------------------------------
    // Functional interface for the generic media loader
    // -------------------------------------------------------------------------

    private interface FeedItemParser {
        DTOmodels.FeedItem parse(JSONObject obj) throws Exception;
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        currentUserId = requireActivity().getIntent().getIntExtra("USER_ID", -1);

        recyclerView = view.findViewById(R.id.feed_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchBar = view.findViewById(R.id.feed_search_bar);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchBar.getText().toString().trim();
                if (!query.isEmpty()) searchFeed(query);
                else applyFilters();
                return true;
            }
            return false;
        });

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(Color.parseColor("#E53935"));
        swipeRefresh.setOnRefreshListener(() -> {
            feedLoaded = false; // force a real reload on the next loadFeed() call
            loadFeed();
        });

        menuBtn = view.findViewById(R.id.feed_menu_btn);
        menuBtn.setOnClickListener(v -> showFilterSheet());

        displayedItems = new ArrayList<>();
        adapter = new FeedAdapter(getContext(), displayedItems, new FeedAdapter.OnItemClickListener() {
            @Override
            public void onPostClick(DTOmodels.FeedPost post) {
                Intent intent = new Intent(getContext(), PostViewActivity.class);
                intent.putExtra("postId",       post.id);
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
                Intent intent = new Intent(getContext(), MediaDetailActivity.class);
                intent.putExtra("mediaType", media.mediaType);
                intent.putExtra("mediaId",   media.id);
                intent.putExtra("title",     media.title);
                intent.putExtra("USER_ID",   currentUserId);
                startActivity(intent);
            }

            @Override
            public void onLikeClick(DTOmodels.FeedPost post) {
                if (currentUserId == -1 || post.id == 0) return;

                // Optimistic toggle — update UI immediately so it feels instant
                boolean wasLiked = likedPostIds.contains(post.id);
                if (wasLiked) {
                    likedPostIds.remove(post.id);
                    post.likeCount = Math.max(0, post.likeCount - 1);
                } else {
                    likedPostIds.add(post.id);
                    post.likeCount++;
                }
                adapter.notifyDataSetChanged();

                ApiClient.post(getContext(),
                        "/posts/" + post.id + "/likes?userId=" + currentUserId,
                        null,
                        new Api_Interface() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                try {
                                    // Sync with the authoritative server count
                                    boolean liked = response.getBoolean("liked");
                                    int count     = response.getInt("count");
                                    if (liked) likedPostIds.add(post.id);
                                    else       likedPostIds.remove(post.id);
                                    post.likeCount = count;
                                    if (getActivity() != null)
                                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                                } catch (Exception ignored) {}
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "Like toggle failed: " + message);
                                // Revert the optimistic update
                                if (wasLiked) {
                                    likedPostIds.add(post.id);
                                    post.likeCount++;
                                } else {
                                    likedPostIds.remove(post.id);
                                    post.likeCount = Math.max(0, post.likeCount - 1);
                                }
                                if (getActivity() != null)
                                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                                if (getContext() != null)
                                    Toast.makeText(getContext(), "Could not update like",
                                            Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }, likedPostIds);
        recyclerView.setAdapter(adapter);

        // Fetch followed-user IDs upfront so the FOLLOWING filter works instantly.
        getFollowedUsers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only do a full reload the first time (or after a pull-to-refresh).
        // Subsequent onResume calls (back-navigation from PostView, etc.) skip
        // the 6+N network requests that were hammering the server on every tap.
        if (!feedLoaded) {
            loadFeed();
        }
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadFeed() {
        if (!isAdded()) return;

        postItems.clear();
        mediaItems.clear();
        likedPostIds.clear(); // reset so stale liked state doesn't survive a full reload
        pendingCalls.set(6);  // must match the number of API calls below

        // --- Posts ---
        ApiClient.getArray(getContext(), "/feed", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                Gson gson = new Gson();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        DTOmodels.FeedPost post = gson.fromJson(
                                response.getJSONObject(i).toString(), DTOmodels.FeedPost.class);
                        synchronized (postItems) {
                            postItems.add(DTOmodels.FeedItem.fromPost(post));
                        }
                    }
                    Log.d(TAG, "Loaded " + response.length() + " posts");
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing posts: " + e.getMessage());
                }
                checkAndMerge();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Posts failed: " + message);
                checkAndMerge();
            }
        });

        // --- Media (albums, books, games, movies, artists) ---
        Gson gson = new Gson();
        loadMediaEndpoint("/albums",  "albums",
                obj -> DTOmodels.FeedItem.fromAlbum(gson.fromJson(obj.toString(),  DTOmodels.album.class)));
        loadMediaEndpoint("/books",   "books",
                obj -> DTOmodels.FeedItem.fromBook(gson.fromJson(obj.toString(),   DTOmodels.book.class)));
        loadMediaEndpoint("/games",   "games",
                obj -> DTOmodels.FeedItem.fromGame(gson.fromJson(obj.toString(),   DTOmodels.game.class)));
        loadMediaEndpoint("/movies",  "movies",
                obj -> DTOmodels.FeedItem.fromMovie(gson.fromJson(obj.toString(),  DTOmodels.movie.class)));
        loadMediaEndpoint("/artists", "artists",
                obj -> DTOmodels.FeedItem.fromArtist(gson.fromJson(obj.toString(), DTOmodels.artist.class)));
    }

    private void loadMediaEndpoint(String endpoint, String label, FeedItemParser parser) {
        ApiClient.getArray(getContext(), endpoint, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        DTOmodels.FeedItem item = parser.parse(response.getJSONObject(i));
                        synchronized (mediaItems) { mediaItems.add(item); }
                    }
                    Log.d(TAG, "Loaded " + response.length() + " " + label);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing " + label + ": " + e.getMessage());
                }
                checkAndMerge();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, label + " failed: " + message);
                checkAndMerge();
            }
        });
    }

    /**
     * Called by every API callback. Merges and displays data once all 6 calls finish.
     */
    private void checkAndMerge() {
        if (pendingCalls.decrementAndGet() > 0) return;

        Log.d(TAG, "All calls done — posts: " + postItems.size() + ", media: " + mediaItems.size());

        Collections.shuffle(mediaItems);

        List<DTOmodels.FeedItem> merged = new ArrayList<>();
        Random random = new Random();
        int mediaIndex = 0;

        for (DTOmodels.FeedItem post : postItems) {
            merged.add(post);
            if (mediaIndex < mediaItems.size() && random.nextInt(3) == 0) {
                merged.add(mediaItems.get(mediaIndex++));
            }
        }
        while (mediaIndex < mediaItems.size()) {
            merged.add(mediaItems.get(mediaIndex++));
        }

        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            allItems.clear();
            allItems.addAll(merged);
            feedLoaded = true; // suppress future onResume reloads until pull-to-refresh
            applyFilters();
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            Log.d(TAG, "Feed ready: " + merged.size() + " items");
            fetchLikeStatuses();
        });
    }

    /**
     * Fetches followed-user IDs. The FOLLOWING filter uses these at display time,
     * so there's no race with loadFeed().
     */
    private void getFollowedUsers() {
        if (!isAdded()) return;
        ApiClient.getArray(getContext(), "/follower/get-following/" + currentUserId,
                new Api_Array_Interface() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        try {
                            followedIDs.clear();
                            for (int i = 0; i < response.length(); i++) {
                                followedIDs.add(response.getJSONObject(i).getInt("id"));
                            }
                            Log.d(TAG, "Following " + followedIDs.size() + " users");
                            // Re-apply filters if FOLLOWING is active so the list updates.
                            if (currentViewType.equals("FOLLOWING") && getActivity() != null) {
                                getActivity().runOnUiThread(() -> applyFilters());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing followed users: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Get following error: " + message);
                    }
                });
    }

    /**
     * For every post currently in the feed, checks whether the signed-in user
     * has already liked it ({@code GET /posts/{id}/likes/status?userId=…}).
     *
     * <p>Called once per feed reload, after {@link #checkAndMerge()} has finished
     * building {@code postItems}. Each callback updates {@link #likedPostIds} and
     * the authoritative server like-count on the {@link DTOmodels.FeedPost} object
     * (which is shared by reference through {@code allItems} / {@code displayedItems}),
     * then notifies the adapter so the ♥/♡ re-renders correctly.
     *
     * <p>Failures are silently ignored — the worst outcome is a heart that stays
     * empty when the user already liked the post (corrects itself on next tap).
     */
    private void fetchLikeStatuses() {
        if (currentUserId == -1 || !isAdded()) return;

        // Count how many requests we're firing so we can do a single
        // notifyDataSetChanged() after the last one comes back instead of
        // one redraw per post (which was causing N full redraws per load).
        List<DTOmodels.FeedPost> postsToCheck = new ArrayList<>();
        for (DTOmodels.FeedItem item : postItems) {
            if (item.type == DTOmodels.FeedItem.TYPE_POST
                    && item.post != null && item.post.id != 0) {
                postsToCheck.add(item.post);
            }
        }
        if (postsToCheck.isEmpty()) return;

        AtomicInteger remaining = new AtomicInteger(postsToCheck.size());

        for (DTOmodels.FeedPost post : postsToCheck) {
            ApiClient.get(getContext(),
                    "/posts/" + post.id + "/likes/status?userId=" + currentUserId,
                    new Api_Interface() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                boolean liked = response.getBoolean("liked");
                                int count     = response.getInt("count");
                                post.likeCount = count;
                                if (liked) likedPostIds.add(post.id);
                            } catch (Exception ignored) {}
                            // Only notify the adapter once — after the last response
                            if (remaining.decrementAndGet() == 0 && getActivity() != null) {
                                getActivity().runOnUiThread(
                                        () -> adapter.notifyDataSetChanged());
                            }
                        }

                        @Override
                        public void onError(String message) {
                            // Leave heart empty — still decrement so the final notify fires
                            remaining.decrementAndGet();
                        }
                    });
        }
    }

    // =========================================================================
    // Filter bottom sheet
    // =========================================================================

    private void showFilterSheet() {
        if (!isAdded()) return;

        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_feed_filter, null);
        sheet.setContentView(sheetView);

        ChipGroup chipViewType  = sheetView.findViewById(R.id.sheet_chip_view_type);
        ChipGroup chipMediaType = sheetView.findViewById(R.id.sheet_chip_media_type);

        // Restore current state BEFORE attaching listeners so check() doesn't fire them.
        int viewChipId;
        switch (currentViewType) {
            case "FOLLOWING": viewChipId = R.id.sheet_chip_view_following; break;
            case "POSTS":     viewChipId = R.id.sheet_chip_view_posts;     break;
            case "MEDIA":     viewChipId = R.id.sheet_chip_view_media;     break;
            default:          viewChipId = R.id.sheet_chip_view_all;       break;
        }
        chipViewType.check(viewChipId);

        int typeChipId;
        switch (currentFilter) {
            case "ALBUM":  typeChipId = R.id.sheet_chip_type_album;  break;
            case "MOVIE":  typeChipId = R.id.sheet_chip_type_movie;  break;
            case "BOOK":   typeChipId = R.id.sheet_chip_type_book;   break;
            case "GAME":   typeChipId = R.id.sheet_chip_type_game;   break;
            case "ARTIST": typeChipId = R.id.sheet_chip_type_artist; break;
            default:       typeChipId = R.id.sheet_chip_type_all;    break;
        }
        chipMediaType.check(typeChipId);

        chipViewType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? R.id.sheet_chip_view_all : checkedIds.get(0);
            if      (id == R.id.sheet_chip_view_following) currentViewType = "FOLLOWING";
            else if (id == R.id.sheet_chip_view_posts)     currentViewType = "POSTS";
            else if (id == R.id.sheet_chip_view_media)     currentViewType = "MEDIA";
            else                                           currentViewType = "ALL";
            Log.d(TAG, "View filter → " + currentViewType);
            applyFilters();
            updateMenuBtnIndicator();
        });

        chipMediaType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? R.id.sheet_chip_type_all : checkedIds.get(0);
            if      (id == R.id.sheet_chip_type_album)  currentFilter = "ALBUM";
            else if (id == R.id.sheet_chip_type_movie)  currentFilter = "MOVIE";
            else if (id == R.id.sheet_chip_type_book)   currentFilter = "BOOK";
            else if (id == R.id.sheet_chip_type_game)   currentFilter = "GAME";
            else if (id == R.id.sheet_chip_type_artist) currentFilter = "ARTIST";
            else                                         currentFilter = "ALL";
            Log.d(TAG, "Media filter → " + currentFilter);
            applyFilters();
            updateMenuBtnIndicator();
        });

        sheet.show();
    }

    /** Tints the hamburger yellow when any non-default filter is active. */
    private void updateMenuBtnIndicator() {
        if (menuBtn == null) return;
        boolean active = !currentViewType.equals("ALL") || !currentFilter.equals("ALL");
        menuBtn.setColorFilter(
                active ? Color.parseColor("#FFD54F") : Color.WHITE,
                PorterDuff.Mode.SRC_IN);
    }

    // =========================================================================
    // Filtering & search
    // =========================================================================

    private void applyFilters() {
        List<DTOmodels.FeedItem> filtered = new ArrayList<>();
        for (DTOmodels.FeedItem item : allItems) {
            if (passesViewTypeFilter(item) && passesMediaTypeFilter(item)) {
                filtered.add(item);
            }
        }
        updateDisplay(filtered);
    }

    private void searchFeed(String query) {
        String lower = query.toLowerCase();
        List<DTOmodels.FeedItem> filtered = new ArrayList<>();

        for (DTOmodels.FeedItem item : allItems) {
            if (!passesViewTypeFilter(item) || !passesMediaTypeFilter(item)) continue;

            String title = null;
            if (item.type == DTOmodels.FeedItem.TYPE_POST && item.post != null) {
                title = item.post.mediaTitle;
            } else if (item.type == DTOmodels.FeedItem.TYPE_MEDIA && item.media != null) {
                title = item.media.title;
            }
            if (title != null && title.toLowerCase().contains(lower)) {
                filtered.add(item);
            }
        }

        updateDisplay(filtered);
        searchBar.clearFocus();

        if (filtered.isEmpty()) {
            Log.d(TAG, "No local results for '" + query + "', searching external APIs...");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Searching for '" + query + "'...", Toast.LENGTH_SHORT).show();
            }
            searchExternalApis(query);
        }
    }

    private void searchExternalApis(String query) {
        if (!isAdded()) return;

        final List<DTOmodels.FeedItem> results = new ArrayList<>();
        Gson gson = new Gson();

        int searchCount = 0;
        if (currentFilter.equals("ALL") || currentFilter.equals("ALBUM"))  searchCount++;
        if (currentFilter.equals("ALL") || currentFilter.equals("GAME"))   searchCount++;
        if (currentFilter.equals("ALL") || currentFilter.equals("MOVIE"))  searchCount++;
        if (currentFilter.equals("ALL") || currentFilter.equals("BOOK"))   searchCount++;
        if (currentFilter.equals("ALL") || currentFilter.equals("ARTIST")) searchCount++;

        if (searchCount == 0) return;

        final int[] pending = {searchCount};

        if (currentFilter.equals("ALL") || currentFilter.equals("ALBUM")) {
            ApiClient.get(getContext(), "/search-albums-and-save?album_name=" + query, new Api_Interface() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        DTOmodels.album album = gson.fromJson(response.toString(), DTOmodels.album.class);
                        synchronized (results) { results.add(DTOmodels.FeedItem.fromAlbum(album)); }
                    } catch (Exception e) { Log.e(TAG, "Album search parse error: " + e.getMessage()); }
                    checkExternalDone(pending, results);
                }
                @Override
                public void onError(String message) { checkExternalDone(pending, results); }
            });
        }

        if (currentFilter.equals("ALL") || currentFilter.equals("GAME")) {
            ApiClient.getArray(getContext(), "/search-games?q=" + query, new Api_Array_Interface() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        for (int i = 0; i < Math.min(response.length(), 3); i++) {
                            DTOmodels.game game = gson.fromJson(response.getJSONObject(i).toString(), DTOmodels.game.class);
                            synchronized (results) { results.add(DTOmodels.FeedItem.fromGame(game)); }
                        }
                    } catch (Exception e) { Log.e(TAG, "Game search parse error: " + e.getMessage()); }
                    checkExternalDone(pending, results);
                }
                @Override
                public void onError(String message) { checkExternalDone(pending, results); }
            });
        }

        if (currentFilter.equals("ALL") || currentFilter.equals("MOVIE")) {
            ApiClient.getArray(getContext(), "/search-movies?q=" + query, new Api_Array_Interface() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        for (int i = 0; i < Math.min(response.length(), 3); i++) {
                            DTOmodels.movie movie = gson.fromJson(response.getJSONObject(i).toString(), DTOmodels.movie.class);
                            synchronized (results) { results.add(DTOmodels.FeedItem.fromMovie(movie)); }
                        }
                    } catch (Exception e) { Log.e(TAG, "Movie search parse error: " + e.getMessage()); }
                    checkExternalDone(pending, results);
                }
                @Override
                public void onError(String message) { checkExternalDone(pending, results); }
            });
        }

        if (currentFilter.equals("ALL") || currentFilter.equals("BOOK")) {
            ApiClient.getArray(getContext(), "/search-books?q=" + query, new Api_Array_Interface() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        for (int i = 0; i < Math.min(response.length(), 3); i++) {
                            DTOmodels.book book = gson.fromJson(response.getJSONObject(i).toString(), DTOmodels.book.class);
                            synchronized (results) { results.add(DTOmodels.FeedItem.fromBook(book)); }
                        }
                    } catch (Exception e) { Log.e(TAG, "Book search parse error: " + e.getMessage()); }
                    checkExternalDone(pending, results);
                }
                @Override
                public void onError(String message) { checkExternalDone(pending, results); }
            });
        }

        if (currentFilter.equals("ALL") || currentFilter.equals("ARTIST")) {
            ApiClient.get(getContext(), "/search-artist-and-save?artist_name=" + query, new Api_Interface() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        DTOmodels.artist artist = gson.fromJson(response.toString(), DTOmodels.artist.class);
                        synchronized (results) { results.add(DTOmodels.FeedItem.fromArtist(artist)); }
                    } catch (Exception e) { Log.e(TAG, "Artist search parse error: " + e.getMessage()); }
                    checkExternalDone(pending, results);
                }
                @Override
                public void onError(String message) { checkExternalDone(pending, results); }
            });
        }
    }

    private void checkExternalDone(int[] pending, List<DTOmodels.FeedItem> results) {
        synchronized (pending) { pending[0]--; if (pending[0] > 0) return; }
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            if (!isAdded()) return;
            if (!results.isEmpty()) {
                allItems.addAll(results);
                mediaItems.addAll(results);
                updateDisplay(results);
                Toast.makeText(getContext(),
                        "Found " + results.size() + " result(s)!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================================
    // Filter helpers
    // =========================================================================

    private boolean passesViewTypeFilter(DTOmodels.FeedItem item) {
        switch (currentViewType) {
            case "POSTS":
                return item.type == DTOmodels.FeedItem.TYPE_POST;
            case "FOLLOWING":
                // Only posts from users the current user follows.
                return item.type == DTOmodels.FeedItem.TYPE_POST
                        && item.post != null
                        && followedIDs.contains(item.post.authorId);
            case "MEDIA":
                return item.type == DTOmodels.FeedItem.TYPE_MEDIA;
            default: // "ALL"
                return true;
        }
    }

    private boolean passesMediaTypeFilter(DTOmodels.FeedItem item) {
        if (currentFilter.equals("ALL")) return true;
        if (item.type == DTOmodels.FeedItem.TYPE_POST && item.post != null) {
            return currentFilter.equals(item.post.mediaType);
        }
        if (item.type == DTOmodels.FeedItem.TYPE_MEDIA && item.media != null) {
            return currentFilter.equals(item.media.mediaType);
        }
        return false;
    }

    private void updateDisplay(List<DTOmodels.FeedItem> items) {
        displayedItems.clear();
        displayedItems.addAll(items);
        adapter.notifyDataSetChanged();
    }
}
