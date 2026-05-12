// backend/api/User.js
import mongoose from "mongoose";

const UserSchema = new mongoose.Schema(
  {
    email: { type: String, required: true, unique: true, lowercase: true, trim: true },
    username: { type: String, required: true, unique: true, trim: true },
    passwordHash: { type: String, required: true },

    role: { type: String, enum: ["user", "reviewer", "admin"], default: "user" },

    anonymous: { type: Boolean, default: false },
    theme: { type: String, enum: ["light", "dark"], default: "light" },
    avatarUrl: { type: String, default: "" }
  },
  { timestamps: true }
);

export default mongoose.model("User", UserSchema);
