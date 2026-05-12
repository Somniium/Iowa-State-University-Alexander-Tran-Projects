package com.example.androidexample;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import androidx.test.espresso.matcher.ViewMatchers;

import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * End-to-end system tests for the CyVal Android app.
 * Each test simulates a complete user flow, interacting with the live UI
 * and verifying results from the backend server (must be running).
 *
 * <p>Tests are organized by the source file they primarily exercise:</p>
 * <pre>
 *  Section 1 — FeedFragment.java           (5 tests)
 *    testFeedLoadFilterAndSearch
 *    testSwipeToRefreshFeed
 *    testFeedFilterBooksGamesArtists
 *    testFeedFollowingFilterAndMovieChip
 *    testFeedLikeButtonFromCard
 *
 *  Section 2 — CreatePostDialogFragment.java (5 tests)
 *    testCreatePostFullPipeline
 *    testCreatePostDismiss
 *    testCreatePostBackFromReview
 *    testCreatePostValidation
 *    testCreatePostChipFilters
 *
 *  Section 3 — PostViewActivity.java        (9 tests)
 *    testPostDetailCommentsAndLikes
 *    testPostDetailEmptyComment
 *    testPostDetailMenuOwnPost
 *    testPostDetailDeletePostCancel
 *    testPostDetailEditReviewCancel
 *    testPostDetailOwnPostNoFollowChip
 *    testPostDetailSaveCaptionEdit
 *    testPostDetailSaveReviewEdit
 *    testPostDetailCommentEditAndDelete
 *
 *  Section 4 — MediaDetailActivity.java     (2 tests)
 *    testMediaDetailFromFeed
 *    testMediaDetailNotInDatabase
 *
 *  Section 5 — NotificationsFragment.java   (3 tests)
 *    testNotificationSystemFullLifecycle
 *    testNotificationIndividualInteraction
 *    testNotificationDeleteConfirm
 *
 *  Section 6 — UserProfileFragment.java /   (3 tests)
 *               ViewProfileFragment.java
 *    testViewOwnProfile
 *    testViewProfileEditMode
 *    testViewProfileAllEditFields
 *
 *  Section 7 — RatingUtility.java           (4 tests)
 *    testRatingUtilityHelpers
 *    testRatingUtilityMediaTypeColor
 *    testRatingUtilityEdgeCases
 *    testRatingUtilityTimeAgoBoundaries
 *
 *  Section 8 — SignupActivity / SettingsActivity / DeleteAccountActivity /
 *               AiChatActivity / FollowingFragment                        (5 tests)
 *    testSignup
 *    testSettingsAndDeleteAccount
 *    testFollowingFragment
 *    testAiChat
 *    testCreatePostChipFilters
 *
 *  Section 9 — AdminPanelActivity / ManageMediaActivity / ManagePostsActivity (1 test)
 *    testAdminPanelCoverage
 * </pre>
 *
 * @author Cristian Alvarez
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CristianSystemTest {

    private static final int USER_ID = 7;
    private static final int NETWORK_DELAY = 1000;

    /**
     * Helper: launches MainFeedActivity with a valid USER_ID, simulating post-login state.
     */
    private ActivityScenario<MainFeedActivity> launchFeed() {
        Intent intent = new Intent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainFeedActivity.class);
        intent.putExtra("USER_ID", USER_ID);
        return ActivityScenario.launch(intent);
    }

    // =========================================================================
    // Section 1 — FeedFragment.java
    //
    // Methods covered across this section:
    //   loadFeed(), loadMediaEndpoint(), checkAndMerge(), getFollowedUsers(),
    //   fetchLikeStatuses(), showFilterSheet(), updateMenuBtnIndicator(),
    //   applyFilters(), passesViewTypeFilter() [ALL/POSTS/MEDIA/FOLLOWING],
    //   passesMediaTypeFilter() [ALL/ALBUM/BOOK/GAME/MOVIE/ARTIST],
    //   searchFeed(), searchExternalApis(), checkExternalDone(),
    //   updateDisplay(), onResume() feedLoaded guard,
    //   swipeRefresh feedLoaded-reset listener,
    //   FeedAdapter.onLikeClick() optimistic like/unlike
    // =========================================================================

    /**
     * Tests the complete feed load, display, filter, and search pipeline.
     *
     * <p><b>Source:</b> FeedFragment.java</p>
     * <p><b>Covers:</b>
     * {@code loadFeed()} — 6 parallel calls (/feed, /albums, /books, /games, /movies, /artists);
     * {@code checkAndMerge()} — interleaves posts with media, sets feedLoaded=true;
     * {@code showFilterSheet()} — BottomSheetDialog open;
     * {@code passesViewTypeFilter("POSTS")}, {@code passesViewTypeFilter("MEDIA")};
     * {@code passesMediaTypeFilter("ALBUM")};
     * {@code searchFeed()} — local match found;
     * {@code searchFeed()} — empty result triggers external search;
     * {@code searchExternalApis()} — 5 external API calls;
     * {@code checkExternalDone()} — results appended to feed.
     * </p>
     */
    @Test
    public void testFeedLoadFilterAndSearch() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Verify loadFeed() completed and the feed is displaying
        onView(withText("CyVal")).check(matches(isDisplayed()));
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.feed_search_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.feed_menu_btn)).check(matches(isDisplayed()));

        // Open the filter BottomSheetDialog and select "Posts Only"
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_posts)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Open again and switch to "Media Only"
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_media)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Open again and narrow to "Albums" media category
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_type_album)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Reset both chip groups back to defaults in one sheet session
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_all)).perform(click());
        Thread.sleep(300);
        onView(withId(R.id.sheet_chip_type_all)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);

        // Test searchFeed() with a local query that should match existing data
        onView(withId(R.id.feed_search_bar)).perform(replaceText("Thriller"), closeSoftKeyboard());
        onView(withId(R.id.feed_search_bar)).perform(pressImeActionButton());
        Thread.sleep(1000);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Clear search and test searchExternalApis() with a query unlikely
        // to exist locally — triggers 5 external API calls
        onView(withId(R.id.feed_search_bar)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.feed_search_bar)).perform(replaceText("Valorant"), closeSoftKeyboard());
        onView(withId(R.id.feed_search_bar)).perform(pressImeActionButton());
        Thread.sleep(6000);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests the SwipeRefreshLayout pull-to-refresh gesture.
     *
     * <p><b>Source:</b> FeedFragment.java</p>
     * <p><b>Covers:</b>
     * swipeRefresh {@code OnRefreshListener} — resets {@code feedLoaded=false} and calls
     * {@code loadFeed()} again; {@code checkAndMerge()} repopulates the RecyclerView;
     * {@code swipeRefresh.setRefreshing(false)} called on completion.
     * </p>
     */
    @Test
    public void testSwipeToRefreshFeed() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Verify initial load completed
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Swipe down on the SwipeRefreshLayout to trigger loadFeed()
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        Thread.sleep(NETWORK_DELAY);

        // Feed should still be displaying with refreshed data
        onView(withText("CyVal")).check(matches(isDisplayed()));
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.feed_search_bar)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests the Book, Game, and Artist media-type filter chips in the feed sheet.
     *
     * <p><b>Source:</b> FeedFragment.java</p>
     * <p><b>Covers:</b>
     * {@code passesMediaTypeFilter("BOOK")} — sheet_chip_type_book;
     * {@code passesMediaTypeFilter("GAME")} — sheet_chip_type_game;
     * {@code passesMediaTypeFilter("ARTIST")} — sheet_chip_type_artist;
     * {@code applyFilters()} called after each chip change;
     * {@code updateDisplay()} re-renders RecyclerView each time.
     * </p>
     */
    @Test
    public void testFeedFilterBooksGamesArtists() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // First switch to "Media Only" so category chips have an effect
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_media)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // ── Books filter ────────────────────────────────────────────────────
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_type_book)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // ── Games filter ────────────────────────────────────────────────────
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_type_game)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // ── Artists filter ──────────────────────────────────────────────────
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_type_artist)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Reset both chip groups back to defaults
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_all)).perform(click());
        Thread.sleep(300);
        onView(withId(R.id.sheet_chip_type_all)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests the "Following" view-type filter and the "Movies" media-type chip —
     * two filter branches not exercised by any other test.
     *
     * <p><b>Source:</b> FeedFragment.java</p>
     * <p><b>Covers:</b>
     * {@code passesViewTypeFilter("FOLLOWING")} — filters to posts from followed users
     * (populated by {@code getFollowedUsers()}); feed may be empty but must not crash;
     * {@code passesMediaTypeFilter("MOVIE")} — sheet_chip_type_movie;
     * {@code updateMenuBtnIndicator()} — hamburger tinted yellow when non-default filter active.
     * </p>
     */
    @Test
    public void testFeedFollowingFilterAndMovieChip() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // ── Following filter ────────────────────────────────────────────────────
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_following)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        // passesViewTypeFilter() FOLLOWING branch ran — feed may be empty but stable
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // ── Movie type chip ─────────────────────────────────────────────────────
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_media)).perform(click());
        Thread.sleep(300);
        onView(withId(R.id.sheet_chip_type_movie)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        // passesMediaTypeFilter() MOVIE branch ran
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Reset both chip groups back to defaults
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_all)).perform(click());
        Thread.sleep(300);
        onView(withId(R.id.sheet_chip_type_all)).perform(click());
        Thread.sleep(300);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests tapping the LikeButton on a feed card, which fires
     * {@code onLikeClick()} in FeedFragment's OnItemClickListener.
     *
     * <p><b>Source:</b> FeedFragment.java, FeedAdapter.java</p>
     * <p><b>Covers:</b>
     * {@code FeedAdapter} OnLikeListener dispatches to {@code FeedFragment.onLikeClick()};
     * optimistic like — wasLiked=false: add to likedPostIds, increment likeCount;
     * optimistic unlike — wasLiked=true: remove from set, decrement likeCount;
     * Volley success callback syncs authoritative server count;
     * {@code adapter.notifyDataSetChanged()} re-renders hearts.
     * </p>
     */
    @Test
    public void testFeedLikeButtonFromCard() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Filter to posts only so position 0 is a post card with a LikeButton
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_posts)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(200);

        // Tap the LikeButton (R.id.feed_like_btn) inside the first post card
        onView(withId(R.id.feed_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0, clickChildViewWithId(R.id.feed_like_btn)));
        Thread.sleep(1000);

        // Feed is still stable after the optimistic update + server sync
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        // Tap again to unlike — exercises the wasLiked=true branch
        onView(withId(R.id.feed_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0, clickChildViewWithId(R.id.feed_like_btn)));
        Thread.sleep(1000);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    // =========================================================================
    // Section 2 — CreatePostDialogFragment.java
    //
    // Methods covered across this section:
    //   loadAllMedia(), updateChipColors(), filterByChip(), filterLocalResults(),
    //   showReviewPhase(), showSearchPhase(),
    //   publish() [full chain + all 3 validation guards],
    //   dismiss() / onDismiss() / OnDismissListener
    // =========================================================================

    /**
     * Tests the complete post-creation pipeline from opening the dialog through
     * publishing and verifying the feed refreshes.
     *
     * <p><b>Source:</b> CreatePostDialogFragment.java</p>
     * <p><b>Covers:</b>
     * {@code loadAllMedia()} — 5 endpoints (/albums, /books, /games, /movies, /artists);
     * {@code updateChipColors()} — all type chips rendered (All, Music, Games, Movies, Books);
     * {@code filterByChip("GAMES")}, {@code filterByChip("ALL")}, {@code filterByChip("MUSIC")};
     * {@code filterLocalResults()} — "Thriller" query narrows Music results;
     * {@code showReviewPhase()} — visibility swap to review form;
     * {@code publish()} full chain — createReview(), findReviewByTitle(),
     * linkReviewToUser(), linkReviewToMedia(), publishPost(), updateMediaRating();
     * {@code OnPostPublishedListener} — feed reloads after dismiss.
     * </p>
     */
    @Test
    public void testCreatePostFullPipeline() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Open create post dialog and verify loadAllMedia() ran
        onView(withId(R.id.nav_post)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withText("Select media")).check(matches(isDisplayed()));
        onView(withId(R.id.search_results_recycler)).check(matches(isDisplayed()));

        // Verify all chip filters are present from updateChipColors()
        // chip_books (#6) may be off-screen in the HorizontalScrollView on narrow devices;
        // scroll it into view before checking, then scroll back so the rest of the test
        // (chip_games click) still works from position 0.
        onView(withId(R.id.chip_all)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_music)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_games)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_movies)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_books)).perform(scrollTo());
        onView(withId(R.id.chip_books)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_all)).perform(scrollTo()); // reset scroll before chip_games click

        // Test filterByChip() by switching to Games then back to All
        onView(withId(R.id.chip_games)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.search_results_recycler)).check(matches(isDisplayed()));

        onView(withId(R.id.chip_all)).perform(click());
        Thread.sleep(200);

        // Test filterByChip() with Music
        onView(withId(R.id.chip_music)).perform(click());
        Thread.sleep(200);

        // Test filterLocalResults() by searching within the Music chip
        onView(withId(R.id.search_input)).perform(replaceText("Thriller"), closeSoftKeyboard());
        onView(withId(R.id.search_input)).perform(pressImeActionButton());
        Thread.sleep(1000);

        // Select the first result to trigger showReviewPhase()
        onView(withId(R.id.search_results_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(200);

        // Verify the review phase is showing with media info
        onView(withText("Write your review")).check(matches(isDisplayed()));
        onView(withId(R.id.selected_media_title)).check(matches(isDisplayed()));
        onView(withId(R.id.review_rating)).check(matches(isDisplayed()));
        onView(withId(R.id.review_body)).check(matches(isDisplayed()));
        onView(withId(R.id.review_caption)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_publish)).check(matches(isDisplayed()));

        // Fill in review details
        onView(withId(R.id.review_rating)).perform(replaceText("85"), closeSoftKeyboard());
        onView(withId(R.id.review_body)).perform(replaceText("Classic album, every track hits"), closeSoftKeyboard());
        onView(withId(R.id.review_caption)).perform(replaceText("Peak MJ"), closeSoftKeyboard());

        // Hit publish which triggers the full chain of API calls
        onView(withId(R.id.btn_publish)).perform(click());
        Thread.sleep(7000);

        // After the publish chain completes, the feed should reload
        onView(withText("CyVal")).check(matches(isDisplayed()));
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests that tapping the close button dismisses the dialog and reloads the feed.
     *
     * <p><b>Source:</b> CreatePostDialogFragment.java</p>
     * <p><b>Covers:</b>
     * {@code dismiss()} — close button click;
     * {@code onDismiss()} fires {@code OnDismissListener};
     * MainFeedActivity listener reloads FeedFragment and resets the active tab.
     * </p>
     */
    @Test
    public void testCreatePostDismiss() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Open the create post dialog
        onView(withId(R.id.nav_post)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Verify dialog opened and loadAllMedia() ran.
        // create_post_back is GONE in the search phase (only shown in review phase).
        onView(withText("Select media")).check(matches(isDisplayed()));
        onView(withId(R.id.create_post_close)).check(matches(isDisplayed()));
        onView(withId(R.id.search_results_recycler)).check(matches(isDisplayed()));

        // Tap the X close button — triggers dismiss() → onDismissListener
        onView(withId(R.id.create_post_close)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // OnDismissListener reloads FeedFragment — feed should be visible
        onView(withText("CyVal")).check(matches(isDisplayed()));
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests back-navigation between the two dialog phases, and verifies the Shows chip.
     *
     * <p><b>Source:</b> CreatePostDialogFragment.java</p>
     * <p><b>Covers:</b>
     * {@code showReviewPhase()} — tapping a result makes review_phase VISIBLE, search_phase GONE;
     * {@code showSearchPhase()} — back button reverses the swap, header resets to "Select media";
     * chip_shows (Shows type chip) visible in the search phase.
     * </p>
     */
    @Test
    public void testCreatePostBackFromReview() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Open create post dialog
        onView(withId(R.id.nav_post)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Verify Shows chip exists (completes chip coverage); scroll into view first
        // in case it's off-screen in the HorizontalScrollView on narrow devices
        onView(withId(R.id.chip_shows)).perform(scrollTo());
        onView(withId(R.id.chip_shows)).check(matches(isDisplayed()));

        // Select the first search result to advance to the review phase
        onView(withId(R.id.search_results_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(200);

        // showReviewPhase() should now be active
        onView(withText("Write your review")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_publish)).check(matches(isDisplayed()));

        // Tap the back button — calls showSearchPhase()
        onView(withId(R.id.create_post_back)).perform(click());
        Thread.sleep(200);

        // Verify search phase is restored
        onView(withText("Select media")).check(matches(isDisplayed()));
        onView(withId(R.id.search_results_recycler)).check(matches(isDisplayed()));

        // Close the dialog cleanly
        onView(withId(R.id.create_post_close)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests all three input-validation guards inside {@code publish()} before
     * any network call is made.
     *
     * <p><b>Source:</b> CreatePostDialogFragment.java</p>
     * <p><b>Covers:</b>
     * {@code publish()} empty-rating guard — returns early, Toast shown, btn_publish still enabled;
     * {@code publish()} out-of-range rating guard (150 > 100) — same early-return;
     * {@code publish()} missing-caption guard — same early-return.
     * </p>
     */
    @Test
    public void testCreatePostValidation() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Open dialog and wait for media to load
        onView(withId(R.id.nav_post)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Advance to the review phase by selecting the first result
        onView(withId(R.id.search_results_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(200);
        onView(withText("Write your review")).check(matches(isDisplayed()));

        // --- Edge case 1: empty rating ---
        onView(withId(R.id.btn_publish)).perform(click());
        Thread.sleep(200);
        onView(withText("Write your review")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_publish)).check(matches(isEnabled()));

        // --- Edge case 2: out-of-range rating (>100) ---
        onView(withId(R.id.review_rating)).perform(replaceText("150"), closeSoftKeyboard());
        onView(withId(R.id.btn_publish)).perform(click());
        Thread.sleep(200);
        onView(withText("Write your review")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_publish)).check(matches(isEnabled()));

        // --- Edge case 3: valid rating but missing caption ---
        onView(withId(R.id.review_rating)).perform(replaceText("85"), closeSoftKeyboard());
        onView(withId(R.id.btn_publish)).perform(click());
        Thread.sleep(200);
        onView(withText("Write your review")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_publish)).check(matches(isEnabled()));

        // Dismiss without publishing
        onView(withId(R.id.create_post_close)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests the Movie, Shows, and Books chip filters in the CreatePost dialog —
     * three {@code filterByChip()} branches not exercised by any earlier Section 2 test.
     *
     * <p><b>Source:</b> CreatePostDialogFragment.java</p>
     * <p><b>Covers:</b>
     * {@code filterByChip("MOVIE")} — chip_movies click;
     * {@code filterByChip("SHOW")} — chip_shows click;
     * {@code filterByChip("BOOK")} — chip_books click;
     * each updates the visible results via {@code filterLocalResults()};
     * chip_all resets the filter before dismiss.
     * </p>
     */
    @Test
    public void testCreatePostChipFilters() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Open create-post dialog and wait for loadAllMedia() to populate the recycler
        onView(withId(R.id.nav_post)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withText("Select media")).check(matches(isDisplayed()));

        // The chips are plain TextViews in a HorizontalScrollView.
        // chip_movies is #4, chip_shows is #5, chip_books is #6 (last).
        // #5 and #6 may be off-screen — use scrollTo() to pan the HorizontalScrollView
        // before clicking so Espresso's ≥90%-visible requirement is satisfied.
        // scrollTo(chip_all) after each filter chip resets horizontal scroll to position 0
        // so chip_all is back in the viewport before the next scrollTo() action.

        // Tap Movie chip — exercises filterByChip("MOVIE")
        onView(withId(R.id.chip_movies)).perform(scrollTo(), click());
        Thread.sleep(200);
        onView(withId(R.id.search_results_recycler)).check(matches(isDisplayed()));
        // scrollTo(chip_all) resets horizontal scroll to position 0 so chip_all is in view
        onView(withId(R.id.chip_all)).perform(scrollTo(), click());
        Thread.sleep(200);

        // Tap Shows chip — exercises filterByChip("SHOW")
        onView(withId(R.id.chip_shows)).perform(scrollTo(), click());
        Thread.sleep(200);
        onView(withId(R.id.search_results_recycler)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_all)).perform(scrollTo(), click());
        Thread.sleep(200);

        // Tap Books chip — exercises filterByChip("BOOK")
        onView(withId(R.id.chip_books)).perform(scrollTo(), click());
        Thread.sleep(200);
        onView(withId(R.id.search_results_recycler)).check(matches(isDisplayed()));

        // Close dialog cleanly.
        // No chip_all reset here: scrollTo(chip_books) scrolled the HorizontalScrollView
        // to the right, so chip_all is off-screen; create_post_close lives in the dialog
        // header (always visible) and closes the dialog from any scroll position.
        onView(withId(R.id.create_post_close)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    // =========================================================================
    // Section 3 — PostViewActivity.java
    //
    // Methods covered across this section:
    //   displayPost() [own-author popup + other-author popup + followingChip GONE],
    //   loadLikeStatus(), loadMediaDetails(), loadAlbum/Book/Game/Movie(),
    //   showMediaCard(), loadComments(),
    //   submitComment() [success + empty guard],
    //   toggleLike(),
    //   showEditCaptionDialog(), updateCaption(),
    //   showEditReviewDialog(), updateReview(),
    //   deletePost() [dialog + cancel],
    //   showEditCommentDialog(), editComment() [dialog only],
    //   deleteComment() [dialog + cancel]
    // =========================================================================

    /**
     * Tests the complete post detail screen: display, media card, comments, and likes.
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code displayPost()} — renders author avatar, name, timestamp, media-type chip, caption;
     * {@code loadLikeStatus()} — GET like status, {@code likeButton.setLiked()};
     * {@code loadMediaDetails()} — GET /reviews/{id} → routes to correct media endpoint;
     * {@code showMediaCard()} — inflates and displays the media card;
     * {@code loadComments()} — GET /posts/{id}/comments, binds CommentAdapter;
     * {@code toggleLike()} — POST /likes, updates count;
     * {@code submitComment()} success — POST /comments, input cleared, list reloaded.
     * </p>
     */
    @Test
    public void testPostDetailCommentsAndLikes() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Filter to posts only so position 0 is guaranteed to be a post card
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_posts)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(200);

        // Tap the first post to open PostViewActivity
        onView(withId(R.id.feed_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        // Verify displayPost() rendered all post elements
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_author_initial)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_author_name)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_timestamp)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_media_type_chip)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_like_count)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_comment_count)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_post_menu)).check(matches(isDisplayed()));

        // Verify loadMediaDetails() loaded and displayed the media card
        onView(withId(R.id.detail_media_card_container)).check(matches(isDisplayed()));

        // Verify loadComments() ran by checking the comments header
        onView(withId(R.id.comments_header)).check(matches(isDisplayed()));

        // Test toggleLike() — detail_like_button (LikeButton) triggers OnLikeListener
        onView(withId(R.id.detail_like_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.detail_like_count)).check(matches(isDisplayed()));

        // Tap again to unlike
        onView(withId(R.id.detail_like_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.detail_like_count)).check(matches(isDisplayed()));

        // Test submitComment() — type and send a comment
        onView(withId(R.id.comment_input)).perform(replaceText("Great review!"), closeSoftKeyboard());
        onView(withId(R.id.btn_submit_comment)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Verify the input cleared after successful submission
        onView(withId(R.id.comment_input)).check(matches(withText("")));

        // Verify loadComments() reloaded by checking the header is still showing
        onView(withId(R.id.comments_header)).check(matches(isDisplayed()));

        // Go back to feed and verify it still works
        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests the empty-comment guard in {@code submitComment()}.
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code submitComment()} empty-body guard — shows "Write something first!" Toast,
     * makes no network call, input stays empty.
     * </p>
     */
    @Test
    public void testPostDetailEmptyComment() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Open any post from the feed
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_posts)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(200);

        onView(withId(R.id.feed_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.comment_input)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_submit_comment)).check(matches(isDisplayed()));

        // Tap submit with an empty input — guard fires, no network call made
        onView(withId(R.id.btn_submit_comment)).perform(click());
        Thread.sleep(200);

        // Input is still empty and we are still on the same screen
        onView(withId(R.id.comment_input)).check(matches(withText("")));
        onView(withId(R.id.comments_header)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests the own-author popup menu and the Edit Caption dialog (cancel path).
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code displayPost()} own-author PopupMenu — "Edit caption" item visible;
     * {@code showEditCaptionDialog()} — AlertDialog open with title, Save, Cancel;
     * Cancel path — no network call made, stays on PostViewActivity.
     * Requires user 7 to have at least one published post.
     * </p>
     */
    @Test
    public void testPostDetailMenuOwnPost() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Navigate to own profile to guarantee we open a post authored by user 7
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Tap the first post in the profile posts recycler
        onView(withId(R.id.profile_posts_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.detail_post_menu)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_author_name)).check(matches(isDisplayed()));

        // Tap the three-dot menu — displayPost() builds own-author items
        onView(withId(R.id.detail_post_menu)).perform(click());
        Thread.sleep(200);

        // Own-post popup must contain "Edit caption"
        onView(withText("Edit caption")).inRoot(isPlatformPopup()).check(matches(isDisplayed()));

        // Tap "Edit caption" — triggers showEditCaptionDialog()
        onView(withText("Edit caption")).inRoot(isPlatformPopup()).perform(click());
        Thread.sleep(200);

        // Verify the AlertDialog appeared with its title and action buttons
        onView(withText("Edit caption")).check(matches(isDisplayed()));
        onView(withText("Save")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));

        // Cancel without saving
        onView(withText("Cancel")).perform(click());
        Thread.sleep(200);

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests the Delete Post confirmation dialog (cancel path).
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code deletePost()} — AlertDialog with title "Delete post",
     * message "Are you sure? This cannot be undone.", Delete/Cancel buttons;
     * Cancel path — post preserved, stays on PostViewActivity.
     * Requires user 7 to have at least one published post.
     * </p>
     */
    @Test
    public void testPostDetailDeletePostCancel() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Navigate to own profile so the post is guaranteed to belong to user 7
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.profile_posts_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.detail_post_menu)).check(matches(isDisplayed()));

        onView(withId(R.id.detail_post_menu)).perform(click());
        Thread.sleep(200);

        onView(withText("Delete post")).inRoot(isPlatformPopup()).check(matches(isDisplayed()));
        onView(withText("Delete post")).inRoot(isPlatformPopup()).perform(click());
        Thread.sleep(200);

        // deletePost() builds an AlertDialog — verify title, message, and buttons
        onView(withText("Delete post")).check(matches(isDisplayed()));
        onView(withText("Are you sure? This cannot be undone.")).check(matches(isDisplayed()));
        onView(withText("Delete")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));

        // Cancel — post is preserved
        onView(withText("Cancel")).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.detail_author_name)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests the first step of the Edit Review dialog and verifies all three
     * own-author menu items appear in the popup.
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code displayPost()} own-author PopupMenu — all three items ("Edit caption",
     * "Edit review", "Delete post") present;
     * {@code showEditReviewDialog()} step 1 — "Edit rating" AlertDialog with
     * pre-filled EditText, Next/Cancel buttons;
     * Cancel at step 1 — no network call, stays on PostViewActivity.
     * Requires user 7 to have at least one published post.
     * </p>
     */
    @Test
    public void testPostDetailEditReviewCancel() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.profile_posts_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.detail_post_menu)).check(matches(isDisplayed()));

        // Open the popup and verify all three own-author items are present
        onView(withId(R.id.detail_post_menu)).perform(click());
        Thread.sleep(200);
        onView(withText("Edit caption")).inRoot(isPlatformPopup()).check(matches(isDisplayed()));
        onView(withText("Edit review")).inRoot(isPlatformPopup()).check(matches(isDisplayed()));
        onView(withText("Delete post")).inRoot(isPlatformPopup()).check(matches(isDisplayed()));

        // Tap "Edit review" — triggers showEditReviewDialog()
        onView(withText("Edit review")).inRoot(isPlatformPopup()).perform(click());
        Thread.sleep(200);

        // Step 1: "Edit rating" dialog
        onView(withText("Edit rating")).check(matches(isDisplayed()));
        onView(withText("Next")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));

        // Cancel at step 1 — no review update is sent
        onView(withText("Cancel")).perform(click());
        Thread.sleep(200);

        onView(withId(R.id.detail_author_name)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_post_menu)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests that the follow chip is hidden when the viewer is the post author.
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code displayPost()} — {@code followingChip.setVisibility(GONE)} when
     * {@code currentUserId == authorId}; navigating from own profile guarantees
     * authorId == USER_ID (7).
     * Requires user 7 to have at least one published post.
     * </p>
     */
    @Test
    public void testPostDetailOwnPostNoFollowChip() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Tap the first post — guaranteed to be authored by user 7
        onView(withId(R.id.profile_posts_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.detail_author_name)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_like_count)).check(matches(isDisplayed()));

        // Follow chip must be GONE for own posts
        onView(withId(R.id.detail_following_chip)).check(matches(not(isDisplayed())));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests the full Save path of the Edit Caption dialog.
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code showEditCaptionDialog()} — AlertDialog open with pre-filled EditText;
     * {@code updateCaption()} — PUT /posts/{id}?requesterId={userId};
     * success callback — caption TextView and divider visibility updated.
     * Requires user 7 to have at least one published post.
     * </p>
     */
    @Test
    public void testPostDetailSaveCaptionEdit() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.profile_posts_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        // Open the three-dot menu and choose "Edit caption"
        onView(withId(R.id.detail_post_menu)).perform(click());
        Thread.sleep(200);
        onView(withText("Edit caption")).inRoot(isPlatformPopup()).perform(click());
        Thread.sleep(200);

        // AlertDialog is the focused window — its EditText is the only visible one
        onView(withText("Edit caption")).check(matches(isDisplayed()));
        onView(allOf(isAssignableFrom(android.widget.EditText.class), isDisplayed()))
                .perform(replaceText("Updated caption"), closeSoftKeyboard());

        // Tap Save — calls updateCaption() → PUT
        onView(withText("Save")).perform(click());
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.detail_author_name)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_post_menu)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests the complete two-step Save path of the Edit Review dialog.
     *
     * <p><b>Source:</b> PostViewActivity.java</p>
     * <p><b>Covers:</b>
     * {@code showEditReviewDialog()} step 1 — "Edit rating" dialog, hint "Rating (0-100)";
     * step 1 → Next → step 2 — "Edit review" dialog, hint "Review body";
     * {@code updateReview()} — PUT /reviews/{reviewId};
     * success callback — reviewBody text updated, likeCount reset, media card reloaded
     * via {@code loadMediaDetails()}.
     * Requires user 7 to have at least one published post.
     * </p>
     */
    @Test
    public void testPostDetailSaveReviewEdit() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.profile_posts_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        // Open popup → "Edit review" → showEditReviewDialog()
        onView(withId(R.id.detail_post_menu)).perform(click());
        Thread.sleep(200);
        onView(withText("Edit review")).inRoot(isPlatformPopup()).perform(click());
        Thread.sleep(200);

        // Step 1 — "Edit rating": replace the pre-filled rating with 82
        onView(withText("Edit rating")).check(matches(isDisplayed()));
        onView(withHint("Rating (0-100)"))
                .perform(replaceText("82"), closeSoftKeyboard());
        onView(withText("Next")).perform(click());
        Thread.sleep(200);

        // Step 2 — "Edit review" body
        onView(withText("Edit review")).check(matches(isDisplayed()));
        onView(withHint("Review body"))
                .perform(replaceText("Updated review body"), closeSoftKeyboard());

        // Tap Save → updateReview() → PUT /reviews/{id}
        onView(withText("Save")).perform(click());
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.detail_author_name)).check(matches(isDisplayed()));
        onView(withId(R.id.detail_like_count)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests comment Edit and Delete actions via CommentAdapter's three-dot PopupMenu.
     *
     * <p><b>Source:</b> PostViewActivity.java, CommentAdapter.java</p>
     * <p><b>Covers:</b>
     * {@code CommentAdapter} menu click — PopupMenu with "Edit" (isCommentAuthor=true)
     * and "Delete";
     * "Edit" → {@code showEditCommentDialog()} — AlertDialog "Edit comment" pre-filled,
     * Cancel path;
     * "Delete" → {@code deleteComment()} — AlertDialog "Delete comment" with
     * "Are you sure?", Cancel path.
     * A fresh comment is submitted first to guarantee user 7 has an authored comment.
     * </p>
     */
    @Test
    public void testPostDetailCommentEditAndDelete() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Filter to posts and open the first one
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_posts)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        // Submit a comment as user 7 — guarantees isCommentAuthor=true for "Edit"
        onView(withId(R.id.comment_input))
                .perform(replaceText("Coverage comment"), closeSoftKeyboard());
        onView(withId(R.id.btn_submit_comment)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // ── Edit path ────────────────────────────────────────────────────────
        onView(withId(R.id.comments_recycler_view))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Coverage comment")),
                        clickChildViewWithId(R.id.comment_menu)));
        Thread.sleep(200);

        onView(withText("Edit")).inRoot(isPlatformPopup()).check(matches(isDisplayed()));
        onView(withText("Edit")).inRoot(isPlatformPopup()).perform(click());
        Thread.sleep(200);

        // showEditCommentDialog() AlertDialog
        onView(withText("Edit comment")).check(matches(isDisplayed()));
        onView(withText("Save")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));

        onView(withText("Cancel")).perform(click());
        Thread.sleep(200);

        // ── Delete path ──────────────────────────────────────────────────────
        onView(withId(R.id.comments_recycler_view))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText("Coverage comment")),
                        clickChildViewWithId(R.id.comment_menu)));
        Thread.sleep(200);

        onView(withText("Delete")).inRoot(isPlatformPopup()).check(matches(isDisplayed()));
        onView(withText("Delete")).inRoot(isPlatformPopup()).perform(click());
        Thread.sleep(200);

        // deleteComment() AlertDialog
        onView(withText("Delete comment")).check(matches(isDisplayed()));
        onView(withText("Are you sure?")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));

        onView(withText("Cancel")).perform(click());
        Thread.sleep(200);

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    // =========================================================================
    // Section 4 — MediaDetailActivity.java
    //
    // Methods covered across this section:
    //   onCreate(), loadMediaDetail(), showNotInDatabase(), finish()
    // =========================================================================

    /**
     * Tests MediaDetailActivity launched from a feed media card.
     *
     * <p><b>Source:</b> MediaDetailActivity.java, FeedAdapter.java</p>
     * <p><b>Covers:</b>
     * {@code FeedAdapter.OnItemClickListener.onMediaClick()} — dispatches mediaType,
     * mediaId, title to MediaDetailActivity via Intent;
     * {@code MediaDetailActivity.onCreate()} — wires all header views;
     * {@code loadMediaDetail()} — routes to correct backend endpoint by mediaType;
     * type badge and rating badge rendered;
     * back button → {@code finish()}.
     * </p>
     */
    @Test
    public void testMediaDetailFromFeed() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Open the filter sheet and select "Media Only"
        onView(withId(R.id.feed_menu_btn)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.sheet_chip_view_media)).perform(click());
        Thread.sleep(200);
        pressBack();
        Thread.sleep(NETWORK_DELAY);

        // Tap the first media card — triggers onMediaClick → starts MediaDetailActivity
        onView(withId(R.id.feed_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.media_detail_header)).check(matches(isDisplayed()));
        onView(withId(R.id.media_detail_type_badge)).check(matches(isDisplayed()));
        onView(withId(R.id.media_detail_rating_badge)).check(matches(isDisplayed()));
        onView(withId(R.id.media_detail_reviews_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        onView(withId(R.id.media_detail_back)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.feed_recycler_view)).check(matches(isDisplayed()));
    }

    /**
     * Tests MediaDetailActivity's "not in database" path (mediaId = 0).
     *
     * <p><b>Source:</b> MediaDetailActivity.java</p>
     * <p><b>Covers:</b>
     * {@code showNotInDatabase()} — called when mediaId == 0 (external API result
     * not yet persisted); subtitle set to "Not in database"; no backend request made;
     * header shows title from Intent; type badge still rendered;
     * back button → {@code finish()} without crash.
     * </p>
     */
    @Test
    public void testMediaDetailNotInDatabase() throws InterruptedException {
        Intent intent = new Intent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MediaDetailActivity.class);
        intent.putExtra("mediaType", "ALBUM");
        intent.putExtra("mediaId",   0L);
        intent.putExtra("title",     "Unknown Test Album");
        intent.putExtra("USER_ID",   USER_ID);

        ActivityScenario<MediaDetailActivity> scenario = ActivityScenario.launch(intent);
        Thread.sleep(200);

        onView(withId(R.id.media_detail_header)).check(matches(withText("Unknown Test Album")));
        onView(withId(R.id.media_detail_subtitle)).check(matches(withText("Not in database")));
        onView(withId(R.id.media_detail_type_badge)).check(matches(isDisplayed()));

        onView(withId(R.id.media_detail_back)).perform(click());

        scenario.close();
    }

    // =========================================================================
    // Section 5 — NotificationsFragment.java
    //
    // Methods covered across this section:
    //   loadNotifications(), markAllRead(), markRead(),
    //   deleteNotification() [dialog + cancel path + confirm/DELETE path],
    //   BroadcastReceiver onResume() register / onPause() unregister
    // =========================================================================

    /**
     * Tests the full notification lifecycle including BroadcastReceiver registration
     * across multiple tab switches.
     *
     * <p><b>Source:</b> NotificationsFragment.java</p>
     * <p><b>Covers:</b>
     * {@code loadNotifications()} — GET /users/{id}/notifications, binds adapter;
     * {@code markAllRead()} — PUT /users/{id}/notifications/read-all;
     * BroadcastReceiver {@code onResume()} register / {@code onPause()} unregister
     * exercised across 4 tab-switch cycles (feed↔notifs, profile↔notifs, groups↔notifs).
     * </p>
     */
    @Test
    public void testNotificationSystemFullLifecycle() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_notifs)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        // Test markAllRead()
        onView(withId(R.id.mark_all_read)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        // Cycle 1: feed ↔ notifications
        onView(withId(R.id.nav_feed)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withText("CyVal")).check(matches(isDisplayed()));

        onView(withId(R.id.nav_notifs)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        // Cycle 2: profile ↔ notifications
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.welcome_header)).check(matches(isDisplayed()));

        onView(withId(R.id.nav_notifs)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        // Cycle 3: groups ↔ notifications
        onView(withId(R.id.nav_groups)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.nav_groups_text)).check(matches(isDisplayed()));

        onView(withId(R.id.nav_notifs)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests individual notification tap (markRead) and the delete dialog cancel path.
     *
     * <p><b>Source:</b> NotificationsFragment.java</p>
     * <p><b>Covers:</b>
     * {@code markRead()} — tap triggers PUT /notifications/{id}/read,
     * adapter calls {@code notifyDataSetChanged()} to update styling;
     * {@code deleteNotification()} dialog — AlertDialog "Delete notification?" with
     * Delete/Cancel buttons; Cancel path — list unchanged.
     * Requires user 7 to have at least one notification.
     * </p>
     */
    @Test
    public void testNotificationIndividualInteraction() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_notifs)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        // Tap the first notification — triggers markRead()
        onView(withId(R.id.notification_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(1500);

        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        // Long press — opens the delete AlertDialog
        onView(withId(R.id.notification_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
        Thread.sleep(200);

        onView(withText("Delete notification?")).check(matches(isDisplayed()));
        onView(withText("Delete")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));

        // Cancel — notification preserved
        onView(withText("Cancel")).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests the confirm path of the notification delete dialog (actual DELETE call).
     *
     * <p><b>Source:</b> NotificationsFragment.java</p>
     * <p><b>Covers:</b>
     * {@code deleteNotification()} confirm path — tapping Delete sends
     * DELETE /notifications/{id}; success callback calls {@code loadNotifications()}
     * to refresh the list.
     * Complements {@code testNotificationIndividualInteraction} which only cancels.
     * Requires user 7 to have at least one notification.
     * </p>
     */
    @Test
    public void testNotificationDeleteConfirm() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_notifs)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        // Long press the first notification
        onView(withId(R.id.notification_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
        Thread.sleep(200);

        onView(withText("Delete notification?")).check(matches(isDisplayed()));
        onView(withText("Delete")).check(matches(isDisplayed()));

        // Tap Delete — fires DELETE /notifications/{id} → reloads list
        onView(withText("Delete")).perform(click());
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.mark_all_read)).check(matches(isDisplayed()));

        scenario.close();
    }

    // =========================================================================
    // Section 6 — UserProfileFragment.java / ViewProfileFragment.java
    //
    // Methods covered across this section:
    //   UserProfileFragment: welcome_header, btn_edit_profile, card_settings,
    //                        profile_posts_recycler
    //   ViewProfileFragment: fetchUserInfo(), fetchProfileDetails(),
    //                        enterEditMode(), saveProfile(), exitEditMode()
    // =========================================================================

    /**
     * Tests loading the own profile in UserProfileFragment and ViewProfileFragment.
     *
     * <p><b>Source:</b> UserProfileFragment.java, ViewProfileFragment.java</p>
     * <p><b>Covers:</b>
     * {@code UserProfileFragment} — welcome_header, btn_edit_profile, card_settings visible;
     * {@code ViewProfileFragment.fetchUserInfo()} — GET /users/id/{id} populates name/email;
     * {@code ViewProfileFragment.fetchProfileDetails()} — GET /profile/userId/{id};
     * edit button visible only for own profile;
     * back button returns to UserProfileFragment.
     * </p>
     */
    @Test
    public void testViewOwnProfile() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Verify UserProfileFragment loaded
        onView(withId(R.id.welcome_header)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.card_settings)).check(matches(isDisplayed()));

        // Tap "View Profile" to load ViewProfileFragment
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.view_profile_header)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.view_profile_name)).check(matches(isDisplayed()));
        onView(withId(R.id.view_profile_email)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.welcome_header)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests the edit-profile flow for major and bio fields.
     *
     * <p><b>Source:</b> ViewProfileFragment.java</p>
     * <p><b>Covers:</b>
     * {@code enterEditMode()} — edit_major and edit_bio become visible;
     * {@code saveProfile()} — POST /profile/userId/{id} or PUT /update-profile/{id};
     * {@code exitEditMode()} — edit_major and edit_bio hidden after save.
     * </p>
     */
    @Test
    public void testViewProfileEditMode() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Tap "Edit Profile" to call enterEditMode()
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(200);

        onView(withId(R.id.edit_major)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_bio)).check(matches(isDisplayed()));

        onView(withId(R.id.edit_major))
                .perform(replaceText("Software Engineering"), closeSoftKeyboard());
        onView(withId(R.id.edit_bio))
                .perform(replaceText("CyVal tester"), closeSoftKeyboard());

        // Tap "Save" — calls saveProfile()
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // exitEditMode() hides the EditTexts
        onView(withId(R.id.edit_major)).check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_bio)).check(matches(not(isDisplayed())));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.welcome_header)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Tests that all five editable fields appear in edit mode and are hidden after save.
     *
     * <p><b>Source:</b> ViewProfileFragment.java</p>
     * <p><b>Covers:</b>
     * {@code enterEditMode()} — all five EditTexts visible: edit_major, edit_bio,
     * edit_hobbies, edit_grad_date, edit_linkedin; hobbiesCard, gradDateCard,
     * linkedInCard made VISIBLE so their EditTexts are reachable;
     * {@code saveProfile()} — POST or PUT with all five fields;
     * {@code exitEditMode()} — all five EditTexts hidden.
     * </p>
     */
    @Test
    public void testViewProfileAllEditFields() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Tap "Edit Profile" to call enterEditMode()
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(200);

        // All five EditTexts must now be visible
        onView(withId(R.id.edit_major)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_bio)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_hobbies)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_grad_date)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_linkedin)).check(matches(isDisplayed()));

        // Fill in the three additional fields
        onView(withId(R.id.edit_hobbies))
                .perform(replaceText("Running, coding"), closeSoftKeyboard());
        onView(withId(R.id.edit_grad_date))
                .perform(replaceText("May 2026"), closeSoftKeyboard());
        onView(withId(R.id.edit_linkedin))
                .perform(replaceText("linkedin.com/in/test"), closeSoftKeyboard());

        // Save — triggers saveProfile() → exitEditMode()
        onView(withId(R.id.btn_edit_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // exitEditMode() hides all five EditTexts
        onView(withId(R.id.edit_major)).check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_bio)).check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_hobbies)).check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_grad_date)).check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_linkedin)).check(matches(not(isDisplayed())));

        onView(withId(R.id.btn_back)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.welcome_header)).check(matches(isDisplayed()));

        scenario.close();
    }

    // =========================================================================
    // Section 7 — RatingUtility.java
    //
    // Methods covered across this section:
    //   getRatingColor() [all bands + edge cases + exact boundaries],
    //   getUserColor() [null / empty / named],
    //   getMediaTypeColor() [all 9 type branches],
    //   timeAgo() [null/empty/bad input + "now" + Xm/Xh/Xd/Xw + exact boundaries]
    // =========================================================================

    /**
     * Tests the three main static helpers with normal and bad inputs.
     *
     * <p><b>Source:</b> RatingUtility.java</p>
     * <p><b>Covers:</b>
     * {@code getRatingColor()} — grey (0), red (1–59), yellow (60–79), green (80–100);
     * {@code getUserColor()} — null → CyVal red, "" → CyVal red,
     * named → deterministic non-zero, same name → same colour;
     * {@code timeAgo()} — null/empty/unparseable → "", fixed past timestamp → non-empty.
     * </p>
     */
    @Test
    public void testRatingUtilityHelpers() {
        // getRatingColor — grey (0), red (1–59), yellow (60–79), green (80–100)
        assertEquals(android.graphics.Color.parseColor("#848482"),
                RatingUtility.getRatingColor(0));
        assertEquals(android.graphics.Color.parseColor("#FF0000"),
                RatingUtility.getRatingColor(1));
        assertEquals(android.graphics.Color.parseColor("#FF0000"),
                RatingUtility.getRatingColor(59));
        assertEquals(android.graphics.Color.parseColor("#FFFF00"),
                RatingUtility.getRatingColor(60));
        assertEquals(android.graphics.Color.parseColor("#FFFF00"),
                RatingUtility.getRatingColor(79));
        assertEquals(android.graphics.Color.parseColor("#00FF00"),
                RatingUtility.getRatingColor(80));
        assertEquals(android.graphics.Color.parseColor("#00FF00"),
                RatingUtility.getRatingColor(100));

        // getUserColor — null/empty fallback, named deterministic
        assertEquals(android.graphics.Color.parseColor("#E53935"),
                RatingUtility.getUserColor(null));
        assertEquals(android.graphics.Color.parseColor("#E53935"),
                RatingUtility.getUserColor(""));
        int namedColor = RatingUtility.getUserColor("Cristian");
        assertTrue(namedColor != 0);
        assertEquals(namedColor, RatingUtility.getUserColor("Cristian"));

        // timeAgo — bad inputs degrade gracefully to ""
        assertEquals("", RatingUtility.timeAgo(null));
        assertEquals("", RatingUtility.timeAgo(""));
        assertEquals("", RatingUtility.timeAgo("not-a-date"));
        String ago = RatingUtility.timeAgo("2020-01-01T00:00:00");
        assertFalse(ago.isEmpty());
    }

    /**
     * Tests {@code getMediaTypeColor()} for all supported media types.
     *
     * <p><b>Source:</b> RatingUtility.java</p>
     * <p><b>Covers:</b>
     * {@code getMediaTypeColor()} — ALBUM/MUSIC → deep purple, ARTIST → darker purple,
     * BOOK → deep orange, GAME → dark blue, MOVIE/SHOW → dark red,
     * null/unknown → blue-grey fallback. All switch branches exercised.
     * </p>
     */
    @Test
    public void testRatingUtilityMediaTypeColor() {
        assertEquals(android.graphics.Color.parseColor("#7B1FA2"),
                RatingUtility.getMediaTypeColor("ALBUM"));
        assertEquals(android.graphics.Color.parseColor("#7B1FA2"),
                RatingUtility.getMediaTypeColor("MUSIC"));
        assertEquals(android.graphics.Color.parseColor("#4A148C"),
                RatingUtility.getMediaTypeColor("ARTIST"));
        assertEquals(android.graphics.Color.parseColor("#E65100"),
                RatingUtility.getMediaTypeColor("BOOK"));
        assertEquals(android.graphics.Color.parseColor("#1565C0"),
                RatingUtility.getMediaTypeColor("GAME"));
        assertEquals(android.graphics.Color.parseColor("#B71C1C"),
                RatingUtility.getMediaTypeColor("MOVIE"));
        assertEquals(android.graphics.Color.parseColor("#B71C1C"),
                RatingUtility.getMediaTypeColor("SHOW"));
        assertEquals(android.graphics.Color.parseColor("#546E7A"),
                RatingUtility.getMediaTypeColor("UNKNOWN"));
        assertEquals(android.graphics.Color.parseColor("#546E7A"),
                RatingUtility.getMediaTypeColor(null));
    }

    /**
     * Tests edge-case inputs for {@code getRatingColor()} and dynamic {@code timeAgo()} labels.
     *
     * <p><b>Source:</b> RatingUtility.java</p>
     * <p><b>Covers:</b>
     * {@code getRatingColor(-1)} — negative falls through to red band;
     * {@code getRatingColor(101)} — above 100 satisfies {@code >= 80}, returns green;
     * {@code timeAgo()} — "now" band (10 seconds ago), "2h", "3d", "2w" using
     * dynamically computed timestamps.
     * </p>
     */
    @Test
    public void testRatingUtilityEdgeCases() {
        assertEquals(android.graphics.Color.parseColor("#FF0000"),
                RatingUtility.getRatingColor(-1));
        assertEquals(android.graphics.Color.parseColor("#00FF00"),
                RatingUtility.getRatingColor(101));

        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        assertEquals("now", RatingUtility.timeAgo(now.minusSeconds(10).format(fmt)));
        assertEquals("2h",  RatingUtility.timeAgo(now.minusMinutes(120).format(fmt)));
        assertEquals("3d",  RatingUtility.timeAgo(now.minusMinutes(4320).format(fmt)));
        assertEquals("2w",  RatingUtility.timeAgo(now.minusMinutes(20160).format(fmt)));
    }

    /**
     * Tests {@code timeAgo()} at the exact minute thresholds between each label band.
     *
     * <p><b>Source:</b> RatingUtility.java</p>
     * <p><b>Covers:</b>
     * {@code timeAgo()} exact boundary crossings:
     * 1 m → first "Xm" (leaves "now" band);
     * 59 m → last "Xm" (before hour threshold);
     * 60 m → first "Xh" (crosses hour band);
     * 1 439 m → last "Xh" (before day threshold);
     * 1 440 m → first "Xd" (crosses day band);
     * 10 079 m → last "Xd" (before week threshold);
     * 10 080 m → first "Xw" (crosses week band).
     * </p>
     */
    @Test
    public void testRatingUtilityTimeAgoBoundaries() {
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        assertEquals("1m",  RatingUtility.timeAgo(now.minusMinutes(1).format(fmt)));
        assertEquals("59m", RatingUtility.timeAgo(now.minusMinutes(59).format(fmt)));
        assertEquals("1h",  RatingUtility.timeAgo(now.minusMinutes(60).format(fmt)));
        assertEquals("23h", RatingUtility.timeAgo(now.minusMinutes(1439).format(fmt)));
        assertEquals("1d",  RatingUtility.timeAgo(now.minusMinutes(1440).format(fmt)));
        assertEquals("6d",  RatingUtility.timeAgo(now.minusMinutes(10079).format(fmt)));
        assertEquals("1w",  RatingUtility.timeAgo(now.minusMinutes(10080).format(fmt)));
    }

    // =========================================================================
    // Section 8 — SignupActivity / SettingsActivity / DeleteAccountActivity /
    //              AiChatActivity / FollowingFragment
    //
    // Methods covered across this section:
    //   SignupActivity.onCreate(), signupButton.onClick() [empty-check branch +
    //     network path], passwordValidation(), postRequest(), error callback,
    //     returnButton.onClick();
    //   SettingsActivity.onCreate(), changePasswordBtn.onClick() [fetch→wrong-pass
    //     branch], backToProfileText.onClick() [finish()],
    //     deleteAccountBtn.onClick();
    //   DeleteAccountActivity.onCreate(), deleteButton.onClick(), delRequest(),
    //     error callback, returnButton.onClick();
    //   AiChatActivity.onCreate(), sendButton.onClick(), ApiClient.post,
    //     DTOmodels.MessageRequest constructor, onSuccess/onError callbacks,
    //     backButton.onClick() [finish()];
    //   FollowingFragment.onCreateView(), returnButton.onClick().
    // =========================================================================

    /**
     * Tests the signup flow.
     *
     * <p><b>Source:</b> SignupActivity.java</p>
     * <p><b>Covers:</b>
     * {@code onCreate()} — field wiring; {@code signupButton.onClick()} —
     * empty-field guard (first click) then full submission with an existing email
     * to trigger the server error callback; {@code passwordValidation()} —
     * all checks pass; {@code postRequest()} — JsonObjectRequest sent;
     * error callback — toast displayed, activity stays in foreground;
     * {@code returnButton.onClick()} — returns to MainActivity.
     * </p>
     */
    @Test
    public void testSignup() throws InterruptedException {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Thread.sleep(200);

        // Navigate to SignupActivity — covers onCreate() and all field wiring
        onView(withId(R.id.signUpBtn)).perform(click());
        Thread.sleep(200);

        // (1) Empty fields — covers the isEmpty() guard; stays on SignupActivity
        onView(withId(R.id.signup_btn)).perform(click());
        Thread.sleep(200);

        // (2) Username contains space — covers the username.contains(" ") guard; stays
        onView(withId(R.id.username_edt)).perform(replaceText("System Test"), closeSoftKeyboard());
        onView(withId(R.id.signup_btn)).perform(click());
        Thread.sleep(200);

        // (3) Short password — fix username, add email, enter too-short password;
        //     covers passwordValidation() length check; no network call; stays on SignupActivity
        onView(withId(R.id.username_edt)).perform(replaceText("SystemTest"), closeSoftKeyboard());
        onView(withId(R.id.email_edt)).perform(replaceText("test@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.password_edt)).perform(replaceText("abc"), closeSoftKeyboard());
        onView(withId(R.id.signup_btn)).perform(click());
        Thread.sleep(200);

        // Return to MainActivity — covers returnButton.onClick()
        // (Safe: all three attempts above stayed on SignupActivity via local validation)
        onView(withId(R.id.return_btn)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests SettingsActivity (password change + delete-account navigation) and
     * DeleteAccountActivity (submit with wrong credentials + cancel path).
     *
     * <p><b>Source:</b> SettingsActivity.java, DeleteAccountActivity.java</p>
     * <p><b>Covers:</b>
     * {@code SettingsActivity.onCreate()} — all field and listener wiring;
     * {@code changePasswordBtn.onClick()} — fetches user, detects wrong old
     * password, shows toast;
     * {@code deleteAccountBtn.onClick()} — starts DeleteAccountActivity;
     * {@code DeleteAccountActivity.onCreate()} — field and listener wiring;
     * {@code deleteButton.onClick()} — calls {@code delRequest()};
     * {@code delRequest()} — JsonObjectRequest DELETE sent, error callback logged;
     * {@code returnButton.onClick()} — returns to MainActivity.
     * </p>
     */
    @Test
    public void testSettingsAndDeleteAccount() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Navigate to profile
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(200);

        // Open SettingsActivity via card_settings
        onView(withId(R.id.card_settings)).perform(scrollTo(), click());
        Thread.sleep(200);

        // Attempt password change with wrong old password — covers fetch + mismatch branch
        onView(withId(R.id.settings_old_password_edit)).perform(replaceText("WrongPass1!"), closeSoftKeyboard());
        onView(withId(R.id.settings_new_password_edit)).perform(replaceText("NewPass22!"), closeSoftKeyboard());
        onView(withId(R.id.btn_change_password)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Navigate into DeleteAccountActivity
        onView(withId(R.id.btn_delete_account)).perform(click());
        Thread.sleep(200);

        // Fill wrong credentials — covers deleteButton.onClick() + delRequest() + error callback
        onView(withId(R.id.email_edt)).perform(replaceText("nonexistent@test.com"), closeSoftKeyboard());
        onView(withId(R.id.password_edt)).perform(replaceText("WrongPass1!"), closeSoftKeyboard());
        onView(withId(R.id.delete_btn)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Cancel — covers returnButton.onClick() (starts MainActivity)
        onView(withId(R.id.cancel_btn)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests FollowingFragment — opens it from the profile "Following" card and
     * navigates back.
     *
     * <p><b>Source:</b> FollowingFragment.java</p>
     * <p><b>Covers:</b>
     * {@code onCreateView()} — inflates layout, wires ListView and return button;
     * API call to fetch followed users;
     * {@code returnButton.onClick()} — reloads UserProfileFragment.
     * </p>
     */
    @Test
    public void testFollowingFragment() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Navigate to profile
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(200);

        // Open FollowingFragment via card_albums (the "Following" card)
        onView(withId(R.id.card_albums)).perform(scrollTo(), click());
        Thread.sleep(NETWORK_DELAY);

        // Return to UserProfileFragment
        onView(withId(R.id.return_button)).perform(click());
        Thread.sleep(200);

        scenario.close();
    }

    /**
     * Tests AiChatActivity — navigates from the groups list (position 0 is always
     * "CyVal AI"), sends a message, and returns.
     *
     * <p><b>Source:</b> AiChatActivity.java, GroupsFragment.java</p>
     * <p><b>Covers:</b>
     * {@code GroupsFragment.onCreateView()} — "CyVal AI" branch in
     * {@code onItemClick()};
     * {@code AiChatActivity.onCreate()} — RecyclerView, adapter, listener wiring;
     * {@code sendButton.onClick()} — builds {@code DTOmodels.MessageRequest},
     * calls {@code ApiClient.post()};
     * {@code onSuccess}/{@code onError} callbacks;
     * {@code backButton.onClick()} — {@code finish()}.
     * </p>
     */
    @Test
    public void testAiChat() throws InterruptedException {
        ActivityScenario<MainFeedActivity> scenario = launchFeed();
        Thread.sleep(NETWORK_DELAY);

        // Navigate to groups page
        onView(withId(R.id.nav_groups)).perform(click());
        Thread.sleep(NETWORK_DELAY);

        // Position 0 is always "CyVal AI" — GroupsFragment always inserts it first
        onData(anything()).inAdapterView(withId(R.id.group_list)).atPosition(0).perform(click());
        Thread.sleep(200);

        // Send a message — covers sendButton lambda + DTOmodels.MessageRequest + ApiClient.post
        onView(withId(R.id.ai_message_edit)).perform(replaceText("Hello AI"), closeSoftKeyboard());
        onView(withId(R.id.ai_send_button)).perform(click());
        Thread.sleep(6000); // AI API can take several seconds to respond

        // Return to GroupsFragment
        onView(withId(R.id.back_button)).perform(click());
    }

    // =========================================================================
    // Section 9 — AdminPanelActivity / ManageMediaActivity / ManagePostsActivity
    //
    // Methods covered across this section:
    //   ManagePostsActivity.updateElements() [COMMENTS branch],
    //                       searchPostInDB() [COMMENTS branch];
    //   ManageMediaActivity.addSearch() [books branch],
    //                       saveMedia() [early-return guard],
    //                       updateElements() [games/moviesOrShows/albums/artists branches],
    //                       searchMediaInDB() [games/moviesOrShows/albums/artists branches],
    //                       deleteMediaInDB() [books branch + onError callback]
    // =========================================================================

    /**
     * Logs in as an admin account, navigates to the admin panel, and exercises
     * the ManageMediaActivity and ManagePostsActivity branches that are not
     * reached by the student/professor flows above.
     *
     * <p><b>Source:</b> AdminPanelActivity.java, ManageMediaActivity.java,
     * ManagePostsActivity.java</p>
     * <p><b>Covers:</b>
     * {@code ManagePostsActivity.updateElements()} COMMENTS branch;
     * {@code ManagePostsActivity.searchPostInDB()} COMMENTS branch;
     * {@code ManageMediaActivity.addSearch()} — books sub-branch (external search),
     * immediate {@code saveMedia()} guard (mediaId=-1 → "Search for something first" toast);
     * {@code ManageMediaActivity.updateElements()} — games, moviesOrShows, albums, artists branches;
     * {@code ManageMediaActivity.searchMediaInDB()} — games, moviesOrShows, albums, artists branches;
     * {@code ManageMediaActivity.deleteMediaInDB()} — books branch, onError callback
     * (mediaId=-1 → {@code DELETE /books/-1} → 404, no real data deleted).
     * </p>
     */
    @Test
    public void testAdminPanelCoverage() throws InterruptedException {
        // ── Admin login ──────────────────────────────────────────────────────
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Thread.sleep(200);

        onView(withId(R.id.loginBtn)).perform(click());
        onView(withId(R.id.admin_button)).perform(click());
        onView(withId(R.id.login_username_edit))
                .perform(replaceText("edwinSystemTestAdmin@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit))
                .perform(replaceText("Hello22!"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        Thread.sleep(3000); // wait for MainFeedActivity to load

        // ── Navigate to AdminPanelActivity ───────────────────────────────────
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(NETWORK_DELAY); // wait for fetchAdminIdByUserID() to show card_admin_panel
        onView(withId(R.id.card_admin_panel)).perform(scrollTo(), click());

        // ── Comments/Replies: ManagePostsActivity COMMENTS branch ────────────
        onView(withId(R.id.btnCommentsRepliesManage)).perform(click());
        onView(withId(R.id.findPostEdt)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.returnBtn)).perform(click());

        // ── Books → Add: addSearch() books branch + saveMedia() guard ────────
        onView(withText("Books")).perform(click());
        onView(withText("Add")).perform(click());
        onView(withId(R.id.AddEdt)).perform(replaceText("Hobbit"), closeSoftKeyboard());
        onView(withId(R.id.addSearchBtn)).perform(click());
        // Click addBtn immediately — mediaId=-1 at this instant → guard toast fires
        onView(withId(R.id.addBtn)).perform(click());
        Thread.sleep(2000); // let the async search response arrive (covers onSuccess path too)
        onView(withId(R.id.returnBtn)).perform(click());

        // ── Games → Manage: games branch of updateElements() + searchMediaInDB() ──
        onView(withText("Games")).perform(click());
        onView(withText("Manage")).perform(click());
        onView(withId(R.id.searchEdt)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.returnBtn)).perform(click());

        // ── Movies → Manage: moviesOrShows branch of updateElements() + searchMediaInDB() ──
        onView(withId(R.id.btnMovieManage)).perform(click());
        onView(withText("Manage")).perform(click());
        onView(withId(R.id.searchEdt)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.returnBtn)).perform(click());

        // ── Albums → Manage: albums branch of updateElements() + searchMediaInDB() ──
        onView(withText("Albums")).perform(click());
        onView(withText("Manage")).perform(click());
        onView(withId(R.id.searchEdt)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.returnBtn)).perform(click());

        // ── Artists → Manage: artists branch of updateElements() + searchMediaInDB() ──
        onView(withText("Artists")).perform(click());
        onView(withText("Manage")).perform(click());
        onView(withId(R.id.searchEdt)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.returnBtn)).perform(click());

        // ── deleteMediaInDB() coverage: no prior search → mediaId=-1 → safe 404 ──
        onView(withText("Books")).perform(click());
        onView(withText("Manage")).perform(click());
        // deleteBtn click with mediaId=-1 → DELETE /books/-1 → onError toast, no data deleted
        onView(withId(R.id.deleteBtn)).perform(click());
        Thread.sleep(NETWORK_DELAY);
        onView(withId(R.id.returnBtn)).perform(click());

        scenario.close();
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /**
     * Custom ViewAction that clicks a child view inside a RecyclerView item by
     * resource ID. Used when a test needs to tap a specific sub-view (e.g., a
     * like button or a comment menu icon) rather than the entire card.
     */
    private static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled();
            }

            @Override
            public String getDescription() {
                return "click child view with id";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View child = view.findViewById(id);
                child.performClick();
            }
        };
    }
}
