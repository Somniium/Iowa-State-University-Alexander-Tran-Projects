import express from "express";
import UserGameCard from "./UserGameCard.js";
import { requireAuth } from "./AuthMiddleware.js";

const router = express.Router();

// ---- helpers ----
function toYouTubeEmbed(url = "") {
  const u = String(url).trim();
  if (!u) return "";

  // Already embed
  if (u.includes("youtube.com/embed/")) return u;

  // youtu.be/<id>
  const shortMatch = u.match(/youtu\.be\/([a-zA-Z0-9_-]{6,})/);
  if (shortMatch?.[1]) return `https://www.youtube.com/embed/${shortMatch[1]}`;

  // youtube watch?v=<id>
  const watchMatch = u.match(/[?&]v=([a-zA-Z0-9_-]{6,})/);
  if (watchMatch?.[1]) return `https://www.youtube.com/embed/${watchMatch[1]}`;

  // Fallback: return as-is (lets you support other embed providers later)
  return u;
}

function normalizeVideos(input) {
  const arr = Array.isArray(input) ? input : (input ? [input] : []);
  return arr
    .map(toYouTubeEmbed)
    .map(s => s.trim())
    .filter(Boolean)
    .slice(0, 8); // prevent huge payloads
}

// list my cards
router.get("/", requireAuth, async (req, res) => {
  const cards = await UserGameCard
    .find({ ownerId: req.user.userId })
    .sort({ createdAt: -1 });

  res.json(cards);
});

// create card
router.post("/", requireAuth, async (req, res) => {
  const body = req.body || {};

  const videos = normalizeVideos(body.videos);
  const coverVideo = toYouTubeEmbed(body.coverVideo) || videos[0] || "";

  const card = await UserGameCard.create({
    ownerId: req.user.userId,

    title: body.title,
    platform: body.platform,
    releaseDate: body.releaseDate,
    publisher: body.publisher,
    summary: body.summary,

    coverVideo,
    videos,

    // ignore image uploads going forward, but keep if you want legacy support:
    images: Array.isArray(body.images) ? body.images : []
  });

  res.status(201).json(card);
});

// update card
router.put("/:id", requireAuth, async (req, res) => {
  const body = req.body || {};

  const update = {
    title: body.title,
    platform: body.platform,
    releaseDate: body.releaseDate,
    publisher: body.publisher,
    summary: body.summary
  };

  // Only update videos if provided
  if ("videos" in body) {
    update.videos = normalizeVideos(body.videos);
  }
  if ("coverVideo" in body) {
    update.coverVideo = toYouTubeEmbed(body.coverVideo);
  }

  // If coverVideo is blank but videos exist, default coverVideo to first video
  if (update.coverVideo === "" && Array.isArray(update.videos) && update.videos[0]) {
    update.coverVideo = update.videos[0];
  }

  const card = await UserGameCard.findOneAndUpdate(
    { _id: req.params.id, ownerId: req.user.userId },
    update,
    { new: true }
  );

  if (!card) return res.status(404).json({ error: "Not found" });
  res.json(card);
});

// delete
router.delete("/:id", requireAuth, async (req, res) => {
  const ok = await UserGameCard.deleteOne({ _id: req.params.id, ownerId: req.user.userId });
  res.json({ ok: ok.deletedCount === 1 });
});

export default router;
