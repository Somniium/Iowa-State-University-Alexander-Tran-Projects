// backend/api/ReviewerRequest.js
import mongoose from "mongoose";

const ReviewerRequestSchema = new mongoose.Schema(
  {
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    username: { type: String, required: true },
    message: { type: String, default: "" },
    status: { type: String, enum: ["pending", "approved", "rejected"], default: "pending" },
    decisionNote: { type: String, default: "" }
  },
  { timestamps: true }
);

export default mongoose.model("ReviewerRequest", ReviewerRequestSchema);
