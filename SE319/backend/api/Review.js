import mongoose from "mongoose";

const CommentSchema = new mongoose.Schema(
  {
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    username: { type: String, required: true },
    role: { type: String, default: "user" },
    text: { type: String, required: true, trim: true }
  },
  { timestamps: true }
);

const ReviewSchema = new mongoose.Schema(
  {
    gameId: { type: String, required: true },
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    gameTitle: { type: String, default: "" },
    cover: { type: String, default: "" },
    score: { type: Number, required: true, min: 0, max: 100 },
    text: { type: String, required: true, trim: true },

    official: { type: Boolean, default: false },
    comments: { type: [CommentSchema], default: [] }
  },
  { timestamps: true }
);

export default mongoose.model("Review", ReviewSchema);
