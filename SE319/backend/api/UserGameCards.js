// backend/api/UserGameCards.js
import express from "express";
import UserGameCard from "./UserGameCard.js";
import { requireAuth } from "./AuthMiddleware.js";

const router = express.Router();

// GET: return the logged-in user's cards
router.get("/", requireAuth, async (req, res) => {
  try {
    const cards = await UserGameCard.find({ ownerId: req.user.userId })
      .sort({ createdAt: -1 });

    res.status(200).json(cards);
  } catch (e) {
    res.status(500).json({ error: "Failed to fetch user cards" });
  }
});

// POST: create a new card owned by the logged-in user
router.post("/", requireAuth, async (req, res) => {
  try {
    const { title, platform, summary, coverVideo, videos, images } = req.body;

    /* ---------- validation ---------- */
    if (!title?.trim() || !platform?.trim() || !summary?.trim())
      return res.status(400).json({ error: "Title, Platform & Summary required" });

    const isStaff = ["admin", "reviewer"].includes(req.user.role);
    if (!isStaff)
      return res.status(403).json({ error: "Only admins / reviewers can create developer cards" });

    // 1 cover video (YouTube embed) required
    const cover = toYouTubeEmbed(coverVideo);
    if (!cover)
      return res.status(400).json({ error: "Valid YouTube embed URL required for cover video" });

    // at least 3 slide images
    const slideImgs = Array.isArray(images) ? images.filter(Boolean) : [];
    if (slideImgs.length < 3)
      return res.status(400).json({ error: "At least 3 slide image URLs required" });

    /* ---------- create ---------- */
    const created = await UserGameCard.create({
      ownerId: req.user.userId,
      title: title.trim(),
      platform: platform.trim(),
      releaseDate: req.body.releaseDate || "",
      publisher: req.body.publisher || "",
      summary: summary.trim(),
      coverVideo: cover,
      videos: [cover, ...(Array.isArray(videos) ? videos : [])]
        .map(toYouTubeEmbed)
        .filter(Boolean)
        .slice(0, 8),
      images: slideImgs.slice(0, 12),
      category: "developer",
    });

    res.status(201).json(created);
  } catch (e) {
    res.status(500).json({ error: e.message || "Create failed" });
  }
});

// DELETE: only the owner (or admin) can delete
router.delete("/:id", requireAuth, async (req, res) => {
  try {
    const card = await UserGameCard.findById(req.params.id);
    if (!card) return res.status(404).json({ error: "Card not found" });

    const isOwner = String(card.ownerId) === String(req.user.userId);
    const isAdmin = req.user.role === "admin";
    if (!isOwner && !isAdmin) {
      return res.status(403).json({ error: "Not allowed" });
    }

    await UserGameCard.findByIdAndDelete(req.params.id);
    res.status(200).json({ ok: true });
  } catch (e) {
    res.status(500).json({ error: "Failed to delete card" });
  }
});

export default router;
