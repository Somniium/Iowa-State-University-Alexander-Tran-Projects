import express from "express";
import Review from "./Review.js";
import Game from "./Game.js";
import { requireAuth, requireRole } from "./AuthMiddleware.js";

const router = express.Router();

/* ========= GET /api/reviews/me ========= */
router.get("/me", requireAuth, async (req, res) => {
  const list = await Review.find({ userId: req.user.userId })
    .sort({ createdAt: -1 })
    .exec();
  res.json(list);
});

/* ========= GET /api/reviews?gameId=... ========= */
router.get("/", async (req, res) => {
  try {
    const { gameId } = req.query;

    const filter = gameId ? { gameId } : {};
    const reviews = await Review.find(filter)
      .populate("userId", "username")
      .sort({ createdAt: -1 })
      .exec();

    res.status(200).json(reviews);
  } catch {
    res.status(500).json({ error: "Failed to fetch reviews" });
  }
});

// POST /api/reviews (AUTH REQUIRED)
// backend/api/Reviews.js
router.post("/", requireAuth, async (req, res) => {
  try {
    const { gameId, gameTitle = "", cover = "", score, text } = req.body;

    if (!gameId || score == null || !text) {
      return res.status(400).json({ error: "gameId, score, text are required" });
    }

    const review = await Review.create({
      gameId,
      gameTitle,
      cover,
      userId: req.user.userId,
      score: Number(score),
      text,
      official: false,
    });

    try {
      await Game.findOneAndUpdate({ id: gameId }, { category: "latest" });
    } catch (e) {
      console.warn("Mark latest failed:", e.message);
    }

    return res.status(201).json(review);
  } catch (e) {
    console.error(e);
    return res.status(400).json({ error: "Failed to create review" });
  }
});


/* ========= PUT /api/reviews/:id (AUTH REQUIRED) ========= */
router.put("/:id", requireAuth, async (req, res) => {
  try {
    const { id } = req.params;

    const review = await Review.findById(id);
    if (!review) return res.status(404).json({ error: "Review not found" });

    if (String(review.userId) !== String(req.user.userId)) {
      return res.status(403).json({ error: "Not allowed to edit this review" });
    }

    const { score, text } = req.body;
    if (score != null) review.score = score;
    if (text != null) review.text = text;

    await review.save();
    res.status(200).json(review);
  } catch {
    res.status(400).json({ error: "Failed to update review" });
  }
});
/* ========= DELETE /api/reviews/:id (AUTH REQUIRED - owner can delete) ========= */
router.delete("/:id", requireAuth, async (req, res) => {
  try {
    const { id } = req.params;

    const review = await Review.findById(id);
    if (!review) return res.status(404).json({ error: "Review not found" });

    const isOwner = String(review.userId) === String(req.user.userId);
    const isAdmin = req.user.role === "admin";

    if (!isOwner && !isAdmin) {
      return res.status(403).json({ error: "Not allowed to delete this review" });
    }

    await Review.findByIdAndDelete(id);
    return res.status(200).json({ ok: true });
  } catch (e) {
    return res.status(500).json({ error: "Failed to delete review" });
  }
});

/* ========= DELETE /api/reviews/:reviewId/comments/:commentId (admin only) ========= */
router.delete(
  "/:reviewId/comments/:commentId",
  requireAuth,
  requireRole("admin"),
  async (req, res) => {
    try {
      const { reviewId, commentId } = req.params;

      const review = await Review.findById(reviewId);
      if (!review) return res.status(404).json({ error: "Review not found" });

      // Find and remove the comment
      const commentIndex = review.comments.findIndex(
        (comment) => comment._id.toString() === commentId
      );

      if (commentIndex === -1) {
        return res.status(404).json({ error: "Comment not found" });
      }

      review.comments.splice(commentIndex, 1);
      await review.save();

      const populated = await Review.findById(reviewId)
        .populate("userId", "username")
        .exec();

      return res.status(200).json(populated);
    } catch (e) {
      return res.status(400).json({ error: "Failed to delete comment" });
    }
  }
);

/* ========= POST /api/reviews/:id/comment (reviewer/admin) ========= */
router.post(
  "/:id/comment",
  requireAuth,
  requireRole("reviewer", "admin"),
  async (req, res) => {
    try {
      const { id } = req.params;
      const text = String(req.body?.text || "").trim();

      if (!text) {
        return res.status(400).json({ error: "text is required" });
      }

      const review = await Review.findById(id);
      if (!review) return res.status(404).json({ error: "Review not found" });

      review.comments.push({
        userId: req.user.userId,
        username: req.user.username,
        role: req.user.role,
        text,
      });

      await review.save();

      const populated = await Review.findById(id)
        .populate("userId", "username")
        .exec();

      return res.status(201).json(populated);
    } catch (e) {
      return res.status(400).json({ error: "Failed to add comment" });
    }
  }
);

/* ========= POST /api/reviews/official (reviewer/admin) ========= */
router.post("/official", requireAuth, requireRole("reviewer", "admin"), async (req, res) => {
  try {
    const { gameId, gameTitle = "", cover = "", score, text } = req.body;

    if (!gameId || score == null || !text) {
      return res.status(400).json({ error: "gameId, score, text are required" });
    }

    const review = await Review.create({
      gameId,
      gameTitle,
      cover,
      userId: req.user.userId,
      score: Number(score),
      text,
      official: true,
    });
    // mark game as latest (optional)
    try {
      await Game.findOneAndUpdate({ id: gameId }, { category: "latest" });
    } catch { }

    res.status(201).json(review);
  } catch (e) {
    res.status(400).json({ error: "Failed to create official review" });
  }
});

/* ========= GET /api/reviews/latest ========= */
router.get("/latest", async (req, res) => {
  try {
    const limit = Math.min(Number(req.query.limit || 12), 50);

    const list = await Review.find({})
      .populate("userId", "username")
      .sort({ createdAt: -1 })
      .limit(limit)
      .exec();

    res.json(list);
  } catch {
    res.status(500).json({ error: "Failed to fetch latest reviews" });
  }
});
/* ========= DELETE /api/reviews/:id/admin (admin only - delete entire review) ========= */
router.delete("/:id/admin", requireAuth, requireRole("admin"), async (req, res) => {
  try {
    const { id } = req.params;

    console.log("Admin delete attempt - Review ID:", id); // Debug log

    // Validate ID format
    if (!id || !id.match(/^[0-9a-fA-F]{24}$/)) {
      console.log("Invalid ID format:", id);
      return res.status(400).json({ error: "Invalid review ID format" });
    }

    const review = await Review.findById(id);
    if (!review) {
      console.log("Review not found:", id);
      return res.status(404).json({ error: "Review not found" });
    }

    await Review.findByIdAndDelete(id);
    console.log("Review deleted successfully:", id);
    res.status(200).json({ message: "Review deleted by admin" });
  } catch (e) {
    console.error("Admin delete review error:", e);
    res.status(500).json({ error: "Failed to delete review" });
  }
});
export default router;
