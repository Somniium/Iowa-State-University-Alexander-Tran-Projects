// backend/api/Game.js
import mongoose from "mongoose";

const GameSchema = new mongoose.Schema({
  title: String,
  platform: String,
  releaseDate: String,
  rating: Number,
  publisher: String,
  summary: String,
  cover: String,
  category: { type: [String], default: [] }
});

export default mongoose.model("Game", GameSchema);
