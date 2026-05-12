import mongoose from "mongoose";

const schema = new mongoose.Schema(
  {
    _id: { type: String, default: "carousel" },   // single doc
    games: [
      {
        rawgId: String,
        title: String,
        cover: String,
        releaseDate: String,
      },
    ],
  },
  { timestamps: true }
);

export default mongoose.model("AdminCarousel", schema);