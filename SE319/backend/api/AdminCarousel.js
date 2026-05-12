import express from "express";
import AdminCarousel from "./AdminCarouselSelection.js";
import { requireAuth, requireRole } from "./AuthMiddleware.js";

const router = express.Router();

// GET current selection
router.get("/", async (_req, res) => {
  const doc = await AdminCarousel.findOne({ _id: "carousel" });
  res.json(doc ? doc.games : []);
});

// PUT selection (admin only)
router.put("/", requireAuth, requireRole("admin"), async (req, res) => {
  const { games } = req.body;
  if (!Array.isArray(games) || games.length > 5)
    return res.status(400).json({ error: "Send 1-5 games" });

  const updated = await AdminCarousel.findOneAndUpdate(
    { _id: "carousel" },
    { games },
    { upsert: true, new: true }
  );

  res.json(updated.games);
});

export default router;
