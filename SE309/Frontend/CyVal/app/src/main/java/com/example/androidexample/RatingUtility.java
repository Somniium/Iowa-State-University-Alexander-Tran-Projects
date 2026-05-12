package com.example.androidexample;

/**
 * RatingUtility — stateless helper methods shared across all feed and detail views.
 *
 * <p>Centralises three concerns so they stay consistent app-wide:
 * <ol>
 *   <li><b>Rating colour</b> — maps a numeric score to a traffic-light colour used
 *       on card borders and rating badges.</li>
 *   <li><b>Timestamp formatting</b> — converts an ISO-8601 datetime string to a
 *       human-readable "time ago" label (e.g. "3h", "2d").</li>
 *   <li><b>User avatar colour</b> — derives a deterministic colour from a username
 *       so every user always gets the same avatar background.</li>
 * </ol>
 */
public class RatingUtility {

    // =========================================================================
    // Rating colour
    // =========================================================================

    /**
     * Maps a numeric rating score to a traffic-light colour.
     *
     * <ul>
     *   <li>0 (unrated) → grey   {@code #848482}</li>
     *   <li>1–59        → red    {@code #FF0000}</li>
     *   <li>60–79       → yellow {@code #FFFF00}</li>
     *   <li>80–100      → green  {@code #00FF00}</li>
     * </ul>
     *
     * <p>Used for both card-border tints and the circular rating badge overlay.
     *
     * @param rating numeric score (0 = unrated, otherwise 1–100)
     * @return an ARGB colour int
     */
    public static int getRatingColor(int rating) {
        if (rating == 0)   return android.graphics.Color.parseColor("#848482"); // unrated — grey
        if (rating >= 80)  return android.graphics.Color.parseColor("#00FF00"); // great   — green
        if (rating >= 60)  return android.graphics.Color.parseColor("#FFFF00"); // ok      — yellow
        return                    android.graphics.Color.parseColor("#FF0000"); // poor    — red
    }

    // =========================================================================
    // Media type colour
    // =========================================================================

    /**
     * Returns a distinctive colour for each media type, used to tint the
     * type badge chip on media detail and feed cards.
     *
     * <ul>
     *   <li>ALBUM / MUSIC → deep purple  {@code #7B1FA2}</li>
     *   <li>ARTIST        → darker purple {@code #4A148C}</li>
     *   <li>BOOK          → deep orange  {@code #E65100}</li>
     *   <li>GAME          → dark blue    {@code #1565C0}</li>
     *   <li>MOVIE / SHOW  → dark red     {@code #B71C1C}</li>
     *   <li>unknown       → blue-grey    {@code #546E7A}</li>
     * </ul>
     *
     * @param mediaType one of the TYPE_* strings used throughout the app
     * @return an ARGB colour int
     */
    public static int getMediaTypeColor(String mediaType) {
        if (mediaType == null) return android.graphics.Color.parseColor("#546E7A");
        switch (mediaType) {
            case "ALBUM":
            case "MUSIC":  return android.graphics.Color.parseColor("#7B1FA2");
            case "ARTIST": return android.graphics.Color.parseColor("#4A148C");
            case "BOOK":   return android.graphics.Color.parseColor("#E65100");
            case "GAME":   return android.graphics.Color.parseColor("#1565C0");
            case "MOVIE":
            case "SHOW":   return android.graphics.Color.parseColor("#B71C1C");
            default:       return android.graphics.Color.parseColor("#546E7A");
        }
    }

    // =========================================================================
    // Timestamp formatting
    // =========================================================================

    /**
     * Converts an ISO-8601 local datetime string (e.g. {@code "2025-04-01T14:30:00"})
     * to a compact relative label for display in feed cards.
     *
     * <ul>
     *   <li>&lt; 1 min  → "now"</li>
     *   <li>&lt; 1 hour → "Xm"</li>
     *   <li>&lt; 1 day  → "Xh"</li>
     *   <li>&lt; 1 week → "Xd"</li>
     *   <li>otherwise   → "Xw"</li>
     * </ul>
     *
     * <p>Returns an empty string if the timestamp is null, empty, or unparseable,
     * so the UI degrades gracefully without crashing.
     *
     * @param postTimestamp ISO-8601 datetime string from the backend
     * @return human-readable relative time, or {@code ""} on error
     */
    public static String timeAgo(String postTimestamp) {
        if (postTimestamp == null || postTimestamp.isEmpty()) return "";
        try {
            java.time.LocalDateTime then = java.time.LocalDateTime.parse(postTimestamp);
            java.time.LocalDateTime now  = java.time.LocalDateTime.now();
            long minutes = java.time.Duration.between(then, now).toMinutes();

            if (minutes < 1)    return "now";
            if (minutes < 60)   return minutes + "m";
            if (minutes < 1440) return (minutes / 60)   + "h";
            if (minutes < 10080)return (minutes / 1440) + "d";
            return                     (minutes / 10080)+ "w";
        } catch (Exception e) {
            return "";
        }
    }

    // =========================================================================
    // User avatar colour
    // =========================================================================

    /**
     * Returns a deterministic colour for a user's avatar background derived
     * from the hash of their display name.
     *
     * <p>The same name always produces the same colour, so avatars are consistent
     * across sessions and across devices without storing any extra data.
     *
     * <p>Falls back to CyVal red ({@code #E53935}) when the name is null or empty.
     *
     * @param name user's display name
     * @return an ARGB colour int from a fixed palette of 12 colours
     */
    public static int getUserColor(String name) {
        if (name == null || name.isEmpty()) {
            return android.graphics.Color.parseColor("#E53935"); // default — CyVal red
        }

        // Fixed palette — add entries here to expand variety
        String[] colors = {
            "#E53935", "#1E88E5", "#43A047", "#8E24AA", "#FB8C00",
            "#00ACC1", "#D81B60", "#3949AB", "#7CB342", "#F4511E",
            "#6D4C41", "#546E7A"
        };

        int index = Math.abs(name.hashCode()) % colors.length;
        return android.graphics.Color.parseColor(colors[index]);
    }
}
