// backend/api/DeveloperCardsAdmin.js
import express from "express";
import UserGameCard from "./UserGameCard.js";
import { requireAuth, requireRole } from "./AuthMiddleware.js";

const router = express.Router();

/* helper – converts normal YouTube links to embed */
function toYouTubeEmbed(url = "") {
  const u = String(url).trim();
  if (!u) return "";
  if (u.includes("youtube.com/embed/")) return u;

  const shortMatch = u.match(/youtu\.be\/([a-zA-Z0-9_-]{6,})/);
  if (shortMatch?.[1]) return `https://www.youtube.com/embed/${shortMatch[1]}`;

  const watchMatch = u.match(/[?&]v=([a-zA-Z0-9_-]{6,})/);
  if (watchMatch?.[1]) return `https://www.youtube.com/embed/${watchMatch[1]}`;

  return "";
}

/* POST /api/developer-cards/admin  (admin OR reviewer – create dev card) */
router.post("/admin", requireAuth, requireRole("admin", "reviewer"), async (req, res) => {
  try {
    const {
      title,
      platform,
      summary,
      coverVideo,
      images,
      releaseDate,
      publisher,
      rating,
      cover,   // NEW cover image (not in slideshow)
      buy,     // NEW store link
    } = req.body;

    if (!title?.trim() || !platform?.trim() || !summary?.trim()) {
      return res.status(400).json({ error: "Title, Platform & Summary required" });
    }

    const embed = toYouTubeEmbed(coverVideo);
    if (!embed) return res.status(400).json({ error: "Valid YouTube URL required" });

    const slides = Array.isArray(images) ? images.map(String).map(s => s.trim()).filter(Boolean) : [];
    if (slides.length < 3) return res.status(400).json({ error: "At least 3 slide image URLs required" });

    const nRating = rating === "" || rating == null ? null : Number(rating);
    if (nRating != null && (!Number.isFinite(nRating) || nRating < 0 || nRating > 100)) {
      return res.status(400).json({ error: "Rating must be 0–100" });
    }

    const card = await UserGameCard.create({
      ownerId: req.user.userId,
      ownerUsername: req.user.username || "",
      category: "developer",

      title: title.trim(),
      platform: platform.trim(),
      summary: summary.trim(),

      // video
      coverVideo: embed,
      videos: [embed],

      // images
      images: slides.slice(0, 12),

      // optional metadata
      releaseDate: (releaseDate || "").trim(),
      publisher: (publisher || "").trim(),
      rating: nRating,

      // NEW fields
      cover: (cover || "").trim(),
      buy: (buy || "").trim(),
    });

    return res.status(201).json(card);
  } catch (e) {
    return res.status(500).json({ error: e.message || "Create failed" });
  }
});

/* PUT /api/developer-cards/admin/:id  (admin can edit any, reviewer only their own) */
router.put("/admin/:id", requireAuth, requireRole("admin", "reviewer"), async (req, res) => {
  try {
    const {
      title,
      platform,
      summary,
      coverVideo,
      images,
      releaseDate,
      publisher,
      rating,
      cover,
      buy,
    } = req.body;

    if (!title?.trim() || !platform?.trim() || !summary?.trim()) {
      return res.status(400).json({ error: "Title, Platform & Summary required" });
    }

    const embed = toYouTubeEmbed(coverVideo);
    if (!embed) return res.status(400).json({ error: "Valid YouTube URL required" });

    const slides = Array.isArray(images) ? images.map(String).map(s => s.trim()).filter(Boolean) : [];
    if (slides.length < 3) return res.status(400).json({ error: "At least 3 slide image URLs required" });

    const nRating = rating === "" || rating == null ? null : Number(rating);
    if (nRating != null && (!Number.isFinite(nRating) || nRating < 0 || nRating > 100)) {
      return res.status(400).json({ error: "Rating must be 0–100" });
    }

    // admin can update any dev card; reviewer can only update their own
    const filter = { _id: req.params.id, category: "developer" };
    if (req.user.role === "reviewer") filter.ownerId = req.user.userId;

    const updated = await UserGameCard.findOneAndUpdate(
      filter,
      {
        title: title.trim(),
        platform: platform.trim(),
        summary: summary.trim(),

        coverVideo: embed,
        videos: [embed],
        images: slides.slice(0, 12),

        releaseDate: (releaseDate || "").trim(),
        publisher: (publisher || "").trim(),
        rating: nRating,

        cover: (cover || "").trim(),
        buy: (buy || "").trim(),
      },
      { new: true }
    );

    if (!updated) return res.status(404).json({ error: "Not found (or not permitted)" });
    return res.json(updated);
  } catch (e) {
    return res.status(500).json({ error: e.message || "Update failed" });
  }
});

/* DELETE /api/developer-cards/admin/:id  (admin can delete any, reviewer only their own) */
router.delete(
  "/admin/:id",
  requireAuth,
  requireRole("admin", "reviewer"),
  async (req, res) => {
    try {
      const card = await UserGameCard.findById(req.params.id);
      if (!card) return res.status(404).json({ error: "Not found" });

      const isOwner = String(card.ownerId) === String(req.user.userId);
      const isAdmin = req.user.role === "admin";
      if (!isOwner && !isAdmin) {
        return res.status(403).json({ error: "Not allowed" });
      }

      await UserGameCard.findByIdAndDelete(req.params.id);
      return res.json({ ok: true });
    } catch (e) {
      return res.status(400).json({ error: "Invalid id" });
    }
  }
);

export default router;
