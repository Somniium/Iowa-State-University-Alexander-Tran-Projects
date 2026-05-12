// backend/api/DeveloperCards.js
import express from "express";
import UserGameCard from "./UserGameCard.js";

const router = express.Router();

/* GET /api/developer-cards  (public) */
router.get("/", async (_req, res) => {
  try {
    const list = await UserGameCard.find({ category: "developer" })
      .populate("ownerId", "username")
      .sort({ createdAt: -1 });
    return res.json(list);
  } catch {
    return res.status(500).json({ error: "Failed to fetch developer cards" });
  }
});

router.get("/:id", async (req, res) => {
  try {
    const card = await UserGameCard.findOne({
      _id: req.params.id,
      category: "developer",
    }).populate("ownerId", "username");

    if (!card) return res.status(404).json({ error: "Not found" });
    return res.json(card);
  } catch {
    return res.status(400).json({ error: "Invalid id" });
  }
});

export default router;
