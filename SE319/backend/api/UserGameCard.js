import mongoose from "mongoose";

const UserGameCardSchema = new mongoose.Schema(
  {
    ownerId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },

    title: { type: String, required: true },
    platform: String,
    releaseDate: String,
    publisher: String,
    summary: String,

    // Example: "https://www.youtube.com/embed/tavPnYeFrV4"
    coverVideo: { type: String, default: "" },
    videos: { type: [String], default: [] },
    images: { type: [String], default: [] },
    rating: { type: Number, default: null }, // 0-100 (or 1-10 if you want)
    cover: { type: String, default: "" }, // cover image (NOT slideshow)
    buy: { type: String, default: "" },   // store link
    ownerUsername: { type: String, default: "" },
    rawgUrl: { type: String, default: "" },
  },
  { timestamps: true }
);

export default mongoose.model("UserGameCard", UserGameCardSchema);
