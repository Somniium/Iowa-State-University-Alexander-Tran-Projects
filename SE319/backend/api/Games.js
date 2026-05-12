import express from "express";
import Game from "./Game.js";

const router = express.Router();

router.get("/", async (req, res) => {
  try {
    const games = await Game.find();
    res.status(200).json(games);
  } catch (e) {
    res.status(500).json({ error: "Failed to fetch games" });
  }
});

router.post("/", async (req, res) => {
  try {
    const created = await Game.create(req.body);
    res.status(201).json(created);
  } catch (e) {
    res.status(400).json({ error: "Failed to create game" });
  }
});

router.put("/:id", async (req, res) => {
  try {
    const updated = await Game.findByIdAndUpdate(req.params.id, req.body, { new: true });
    if (!updated) return res.status(404).json({ error: "Game not found" });
    res.status(200).json(updated);
  } catch (e) {
    res.status(400).json({ error: "Failed to update game" });
  }
});

router.delete("/:id", async (req, res) => {
  try {
    const deleted = await Game.findByIdAndDelete(req.params.id);
    if (!deleted) return res.status(404).json({ error: "Game not found" });
    res.status(200).json({ message: "Game deleted" });
  } catch (e) {
    res.status(500).json({ error: "Failed to delete game" });
  }
});

export default router;
