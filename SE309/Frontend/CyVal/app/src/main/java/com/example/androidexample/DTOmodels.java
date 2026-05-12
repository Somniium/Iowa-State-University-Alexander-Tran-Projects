package com.example.androidexample;

import java.util.List;

/**
 * @author Cristian Alvarez
 * @version 1.2
 * A central class that holds all the Data Transfer Objects.
 * These static classes are the references that Gson uses
 * to convert between Java objects and JSON with the Spring
 * boot backend.
 */
public class DTOmodels {

    /**
     * Represents the credentials inputted by the user in the Login Activity
     * to be sent to the backend database for authentication.
     */
    public static class LoginRequest {
        public String emailId;
        public String password;

        public LoginRequest(String emailId, String password) {
            this.emailId = emailId;
            this.password = password;
        }
    }

    /**
     * Represents the data received from the backend database after a successful
     * Login Request. It contains essential user profile data.
     */
    public static class LoginResponse {
        public int id;
        public String name;
        public String emailId;
        public boolean active;

        public LoginResponse() {
        }
    }

    /**
     * Represents the payload sent to the backend database when registering
     * a new user account.
     */
    public static class SignUpRequest {
        public String name;
        public String emailId;
        public String password;

        public SignUpRequest(String name, String emailId, String password) {
            this.name = name;
            this.emailId = emailId;
            this.password = password;
        }
    }

    /**
     * Represents the payload used to update an existing user's profile
     * information in the backend database.
     */
    public static class UserUpdate {
        public int id;
        public String name;
        public String emailId;
        public String password;

        public UserUpdate() {
        }

        public UserUpdate(int id, String name, String emailId, String password) {
            this.id = id;
            this.name = name;
            this.emailId = emailId;
            this.password = password;
        }
    }

    /**
     * Represents an album, containing details such as the title, artist,
     * genre, and its associated list of user reviews.
     */
    public static class album {
        public Long albumId;
        public int rating;
        public String artist;
        public String coverURL;
        public String name;
        public String releaseDate;
        public String genre;
        public String spotifyId;

        public List<review> albumReviews;

        public album() {
        }
    }

    /**
     * Represents a book retrieved from the Open Library API
     * or stored in the local database.
     */
    public static class book {
        public int id;
        public String volumeid;
        public String title;
        public String authors;
        public String publisheddate;
        public String description;
        public String thumbnailurl;
        public String isbn13;

        public book() {
        }
    }

    public static class game {
        public long id;
        public String rawgId;
        public String title;
        public String genre;
        public String releaseDate;
        public String developer;
        public String description;
        public String coverUrl;
        public int metacriticScore;
        public int rating;

        public game() {
        }
    }

    public static class movie {
        public long id;
        public String tmdbId;
        public String mediaType;
        public String title;
        public String genre;
        public String releaseDate;
        public String director;
        public String overview;
        public String posterUrl;
        public double tmdbScore;
        public int rating;

        public movie() {
        }
    }

    /**
     * Represents a user review for a specific piece of media, detailing
     * the rating score and text body submitted by the user.
     *
     * <p>The FK ID fields (userId, albumId, bookId, gameId, movieId) are populated
     * by the server-side Review entity's convenience getters and are used on the
     * client to filter reviews by specific media items (see MediaDetailActivity).
     */
    public static class review {
        public int id;
        public String mediaType;
        public String title;
        public int rating;
        public String body;

        // FK IDs exposed by server-side convenience getters on the Review entity
        public Integer userId;
        public Long albumId;
        public Long bookId;
        public Long gameId;
        public Long movieId;

        public review() {
        }

        public review(String mediaType, String title, int rating, String body) {
            this.mediaType = mediaType;
            this.title = title;
            this.rating = rating;
            this.body = body;
        }
    }

    // =========================================================================
    // Feed models
    // =========================================================================

    /**
     * A single published post as returned by {@code GET /feed}.
     *
     * <p>A post is a user's review that has been made public. It carries
     * the review's rating + media info alongside social metadata (likes,
     * comments, author) so the feed card can be fully rendered from this
     * object alone — no secondary API call required.
     */
    public static class FeedPost {
        public int    id;            // post ID (used to like, comment, etc.)
        public String caption;       // optional user-written caption
        public String visibility;    // "PUBLIC" | "PRIVATE" | "FRIENDS"
        public String publishedAt;   // ISO-8601 datetime, e.g. "2025-04-01T14:30:00"
        public String updatedAt;
        public int    reviewId;      // the underlying review this post wraps
        public int    authorId;      // user ID of the poster
        public String authorName;    // display name of the poster
        public String mediaTitle;    // title of the reviewed media
        public String mediaType;     // "ALBUM" | "BOOK" | "GAME" | "MOVIE" | "SHOW" | "ARTIST"
        public int    rating;        // 0 = unrated, 1–100 otherwise
        public int    commentCount;
        public int    likeCount;
    }

    /**
     * A comment on a post as returned by {@code GET /posts/{postId}/comments}.
     */
    public static class FeedComment {
        public int    id;
        public String body;
        public int    authorId;
        public String authorName;
        public int    postId;
        public String createdAt;
        public String updatedAt;
    }

    /**
     * A normalised media object used inside the feed.
     *
     * <p>Albums, books, games, movies, shows, and artists all have different
     * backend shapes. The {@code FeedItem.from*()} factory methods flatten each
     * type into this common struct so {@link FeedAdapter} only needs to know
     * about one class.
     *
     * <p>Field mapping by media type:
     * <pre>
     *  mediaType | title       | subtitle   | imageUrl   | extraInfo
     *  --------- | ----------- | ---------- | ---------- | ---------
     *  ALBUM     | album name  | genre      | coverURL   | releaseDate
     *  BOOK      | title       | authors    | thumbnailurl | publisheddate
     *  GAME      | title       | developer  | coverUrl   | genre
     *  MOVIE     | title       | director   | posterUrl  | genre
     *  ARTIST    | name        | genre      | photoURL   | (empty)
     * </pre>
     */
    public static class FeedMedia {
        public long   id;
        public String mediaType;  // "ALBUM" | "BOOK" | "GAME" | "MOVIE" | "SHOW" | "ARTIST"
        public String title;
        public String subtitle;   // secondary line — see mapping above
        public String imageUrl;   // cover / poster / photo URL
        public String extraInfo;  // tertiary line — see mapping above
        public int    rating;     // community average (0 = none)

        public FeedMedia() {}
    }

    /**
     * Discriminated union used as the item type for the feed RecyclerView.
     *
     * <p>Each instance is either a user post or a media card:
     * <ul>
     *   <li>{@code type == TYPE_POST  (0)} → {@code post}  is populated, {@code media} is null</li>
     *   <li>{@code type == TYPE_MEDIA (1)} → {@code media} is populated, {@code post}  is null</li>
     * </ul>
     *
     * <p>Use the static factory methods to construct instances — never set
     * {@code type}, {@code post}, or {@code media} directly.
     */
    public static class FeedItem {

        /** Item is a published user post — adapter inflates {@code item_feed_user_post.xml}. */
        public static final int TYPE_POST  = 0;
        /** Item is a media card — adapter inflates {@code item_feed_post.xml}. */
        public static final int TYPE_MEDIA = 1;

        public int       type;   // TYPE_POST or TYPE_MEDIA
        public FeedPost  post;   // non-null when type == TYPE_POST
        public FeedMedia media;  // non-null when type == TYPE_MEDIA

        // -----------------------------------------------------------------
        // Factory methods
        // -----------------------------------------------------------------

        /** Wraps a {@link FeedPost} as a feed list item. */
        public static FeedItem fromPost(FeedPost p) {
            FeedItem item = new FeedItem();
            item.type = TYPE_POST;
            item.post = p;
            return item;
        }

        /**
         * Converts an {@link album} to a feed media card.
         * subtitle = genre, extraInfo = releaseDate.
         */
        public static FeedItem fromAlbum(album a) {
            FeedMedia media = new FeedMedia();
            media.id        = a.albumId;
            media.mediaType = "ALBUM";
            media.title     = a.name;
            media.subtitle  = a.genre;
            media.imageUrl  = a.coverURL;
            media.extraInfo = a.releaseDate;
            media.rating    = a.rating;
            return wrapMedia(media);
        }

        /**
         * Converts a {@link book} to a feed media card.
         * subtitle = authors, extraInfo = publisheddate.
         * Rating is 0 — books do not yet carry a community score.
         */
        public static FeedItem fromBook(book b) {
            FeedMedia media = new FeedMedia();
            media.id        = b.id;
            media.mediaType = "BOOK";
            media.title     = b.title;
            media.subtitle  = b.authors;
            media.imageUrl  = b.thumbnailurl;
            media.extraInfo = b.publisheddate;
            media.rating    = 0;
            return wrapMedia(media);
        }

        /**
         * Converts a {@link game} to a feed media card.
         * subtitle = developer, extraInfo = genre.
         */
        public static FeedItem fromGame(game g) {
            FeedMedia media = new FeedMedia();
            media.id        = g.id;
            media.mediaType = "GAME";
            media.title     = g.title;
            media.subtitle  = g.developer;
            media.imageUrl  = g.coverUrl;
            media.extraInfo = g.genre;
            media.rating    = g.rating;
            return wrapMedia(media);
        }

        /**
         * Converts a {@link movie} to a feed media card.
         * subtitle = director, extraInfo = genre.
         * Falls back to "MOVIE" if {@code movie.mediaType} is null (e.g. legacy rows).
         */
        public static FeedItem fromMovie(movie m) {
            FeedMedia media = new FeedMedia();
            media.id        = m.id;
            media.mediaType = m.mediaType != null ? m.mediaType : "MOVIE";
            media.title     = m.title;
            media.subtitle  = m.director;
            media.imageUrl  = m.posterUrl;
            media.extraInfo = m.genre;
            media.rating    = m.rating;
            return wrapMedia(media);
        }

        /**
         * Converts an {@link artist} to a feed media card.
         * subtitle = genre, extraInfo = "" (artists have no release date).
         */
        public static FeedItem fromArtist(artist a) {
            FeedMedia media = new FeedMedia();
            media.id        = a.artistId;
            media.mediaType = "ARTIST";
            media.title     = a.name;
            media.subtitle  = a.genre;
            media.imageUrl  = a.photoURL;
            media.extraInfo = "";
            media.rating    = a.rating;
            return wrapMedia(media);
        }

        /** Shared boilerplate for all fromX() methods that produce a media card. */
        private static FeedItem wrapMedia(FeedMedia media) {
            FeedItem item = new FeedItem();
            item.type  = TYPE_MEDIA;
            item.media = media;
            return item;
        }
    }

    public static class CommentRequest {
        public String body;

        public CommentRequest(String body) {
            this.body = body;
        }
    }

    public static class PostUpdateRequest {
        public String caption;
        public String visibility;

        public PostUpdateRequest(String caption) {
            this.caption = caption;
        }
    }

    public static class ReviewUpdateRequest {
        public int id;
        public String mediaType;
        public String title;
        public int rating;
        public String body;

        public ReviewUpdateRequest(int id, String mediaType, String title, int rating, String body) {
            this.id = id;
            this.mediaType = mediaType;
            this.title = title;
            this.rating = rating;
            this.body = body;
        }
    }

    public static class PostPublishRequest {
        public String caption;
        public String visibility;

        public PostPublishRequest(String caption, String visibility) {
            this.caption = caption;
            this.visibility = visibility;
        }
    }

    //not a DTO for JSON BUT AM REALLY LAZY
    public static class SearchResult {
        public long id;
        public String title;
        public String subtitle;
        public String mediaType;
        public String imageUrl;
        public String externalId; // rawgId, tmdbId, volumeId, etc.

        public SearchResult(long id, String title, String subtitle, String mediaType, String imageUrl) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.mediaType = mediaType;
            this.imageUrl = imageUrl;
        }

        public SearchResult(long id, String title, String subtitle, String mediaType, String imageUrl, String externalId) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.mediaType = mediaType;
            this.imageUrl = imageUrl;
            this.externalId = externalId;
        }
    }

    public static class Notification {
        public int id;
        public int recipientId;
        public String type;
        public String message;
        public boolean read;
        public String createdAt;

        public Notification() {}
    }

    public static class artist {
        public long artistId;
        public String spotifyId;
        public String name;
        public String genre;
        public String photoURL;
        public int rating;

        public artist() {}
    }

    public static class MessageRequest {
        long userId;
        Long sessionId;
        String message;
        MessageRequest(long userId, Long sessionId, String message) {
            this.userId = userId; this.sessionId = sessionId; this.message = message;
        }
    }

    public static class ProfileResponse {
        public int profileId;
        public String bio;
        public String major;
        public String hobbies;
        public String gradDate;
        public String linkedInURL;

        public ProfileResponse() {}
    }

    public static class ProfileRequest {
        public String bio;
        public String major;
        public String hobbies;
        public String gradDate;
        public String linkedInURL;

        public ProfileRequest(String bio, String major, String hobbies, String gradDate, String linkedInURL) {
            this.bio = bio;
            this.major = major;
            this.hobbies = hobbies;
            this.gradDate = gradDate;
            this.linkedInURL = linkedInURL;
        }
    }
}
